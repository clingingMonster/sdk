package so.dian.scm.batch.sdk.service;

import com.alibaba.fastjson.JSON;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import so.dian.scm.batch.sdk.api.DDExecuteTask;
import so.dian.scm.batch.sdk.api.ExecuteTask;
import so.dian.scm.batch.sdk.common.dao.AsyncBatchTaskOrderDAO;
import so.dian.scm.batch.sdk.common.enums.TaskOrderStatusEnum;
import so.dian.scm.batch.sdk.common.exception.TaskException;
import so.dian.scm.batch.sdk.common.model.TaskOrderSubmit;
import so.dian.scm.batch.sdk.common.model.TaskResult;
import so.dian.scm.batch.sdk.controller.BatchTaskController;
import so.dian.scm.batch.sdk.manager.AsyncBatchTaskOrderManager;
import so.dian.scm.batch.sdk.serializer.KryoSerializerUtil;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;

/**
 * @author XR
 * Created  on 2021/1/20.
 */
@Component
@Slf4j
@JobHandler("batchTaskJob")
public class JobTaskHandler extends IJobHandler implements TaskHandler, ApplicationContextAware {

    @Resource
    private AsyncBatchTaskOrderManager asyncBatchTaskOrderManager;

    @Resource(name = "batchTask")
    private Executor executor;

    private ApplicationContext applicationContext;


    @Override
    public Boolean submitTaskOrder(TaskOrderSubmit<?> taskOrderSubmit) {
        AsyncBatchTaskOrderDAO taskOrderDAO = convert(taskOrderSubmit);
        String orderNo = taskOrderDAO.getOrderNo();
        String extMark = taskOrderDAO.getExtMark();
        //  判断当前是否有事务
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // 不同库任务
            if (taskOrderSubmit.getDiffDatabase()) {
                // 注册监听器 事务执行完毕触发订单确认同时触发异步执行任务
                registerDDSynchronization(orderNo, extMark);
                taskOrderDAO.setStatus(TaskOrderStatusEnum.TASK_SUBMIT_UN_CONFIRM.getCode());
                // 开启新的事务保存任务 以免干涉到原有的事务
                asyncBatchTaskOrderManager.addOrderNewTR(taskOrderDAO);
            }
            // 同库任务
            else {
                taskOrderDAO.setStatus(TaskOrderStatusEnum.TASK_GOING_ON.getCode());
                asyncBatchTaskOrderManager.addOrder(taskOrderDAO);
                // 注册监听器 事务执行完毕触发执行任务
                registerSDSynchronization(orderNo, extMark);
            }
        } else {
            // 没有事务则直接提交任务,异步触发执行任务
            taskOrderDAO.setStatus(TaskOrderStatusEnum.TASK_GOING_ON.getCode());
            asyncBatchTaskOrderManager.addOrder(taskOrderDAO);
            executor.execute(() -> executeTask(taskOrderDAO));
        }
        return Boolean.TRUE;
    }


    private void registerSDSynchronization(String orderNo, String extMark) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCompletion(int status) {
                        if (STATUS_COMMITTED == status) {
                            AsyncBatchTaskOrderDAO taskOrderDAO = asyncBatchTaskOrderManager.getOrder(orderNo, extMark);
                            executor.execute(() -> executeTask(taskOrderDAO));
                        }

                    }
                }
        );
    }

    private void registerDDSynchronization(String orderNo, String extMark) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCompletion(int status) {
                        if (STATUS_COMMITTED == status) {
                            asyncBatchTaskOrderManager.confirmOrder(orderNo, extMark);
                            AsyncBatchTaskOrderDAO taskOrderDAO = asyncBatchTaskOrderManager.getOrder(orderNo, extMark);
                            executor.execute(() -> executeTask(taskOrderDAO));
                        } else {
                            asyncBatchTaskOrderManager.cancelOrder(orderNo, extMark);
                        }
                    }
                }
        );
    }


    private void executeTask(AsyncBatchTaskOrderDAO asyncBatchTaskOrderDAO) {
        Integer status = asyncBatchTaskOrderDAO.getStatus();
        String springHandlerClassName = asyncBatchTaskOrderDAO.getSpringHandlerClassName();
        String orderNo = asyncBatchTaskOrderDAO.getOrderNo();
        String extMark = asyncBatchTaskOrderDAO.getExtMark();
        try {
            ExecuteTask<?> executeBean = applicationContext.getBean(springHandlerClassName, ExecuteTask.class);
            // 判断需要处理的订单
            if (TaskOrderStatusEnum.TASK_GOING_ON.getCode() == status) {
                Object executeParam = KryoSerializerUtil.deserialize(asyncBatchTaskOrderDAO.getRequestParam());
                loopExecuteSubTask(executeParam, executeBean, asyncBatchTaskOrderDAO);
                return;
            }
            // 判断是否是待确认订单 反查确认
            if (TaskOrderStatusEnum.TASK_SUBMIT_UN_CONFIRM.getCode() == status) {
                DDExecuteTask ddExecuteTask = (DDExecuteTask) executeBean;
                if (!checkOrder(orderNo, extMark, ddExecuteTask)) {
                    log.warn("订单取消=orderNo:{}.extMark:{}", orderNo, extMark);
                    asyncBatchTaskOrderManager.cancelOrder(orderNo, extMark);
                    return;
                }
                asyncBatchTaskOrderManager.confirmOrder(orderNo, extMark);
                log.info("订单确认成功,开始执行任务=orderNo:{},extMark:{}", orderNo, extMark);
                Object executeParam = KryoSerializerUtil.deserialize(asyncBatchTaskOrderDAO.getRequestParam());
                loopExecuteSubTask(executeParam, executeBean, asyncBatchTaskOrderDAO);
                return;
            }
            if (TaskOrderStatusEnum.TASK_EXECUTE_CALLBACK_FAIL.getCode() == status) {
                callback(asyncBatchTaskOrderDAO, executeBean);
            }
        } catch (Exception e) {
            log.error("执行任务异常=orderNo:{},extMark:{}", orderNo, extMark, e);
        }
    }


    private Boolean checkOrder(String orderNo, String extMark, DDExecuteTask<?> ddExecuteTask) {
        try {
            return ddExecuteTask.checkTaskOrderSubmit(orderNo, extMark);
        } catch (Exception e) {
            log.error("确认订单异常=orderNo:{},extMark:{}", orderNo, extMark, e);
        }
        return Boolean.FALSE;
    }


    private void loopExecuteSubTask(Object executeParam, ExecuteTask executeTaskBean, AsyncBatchTaskOrderDAO asyncBatchTaskOrderDAO) {
        TaskResult<?> taskResult;
        String orderNo = asyncBatchTaskOrderDAO.getOrderNo();
        String extMark = asyncBatchTaskOrderDAO.getExtMark();
        Date date = new Date();
        // 判断当前时间 当大于预定时间 则需要进行第一次参数校验
        if (date.getTime() > asyncBatchTaskOrderDAO.getNextExecuteTime().getTime()) {
            try {
                TaskResult checkResult = executeTaskBean.checkAndGetParam(executeParam);
                if (!checkResult.getHasMore()) {
                    callback(asyncBatchTaskOrderDAO, executeTaskBean);
                    return;
                }
                executeParam = checkResult.getNextParam();
            } catch (Exception e) {
                log.error("执行参数校验失败=orderNo:{},extMark:{},param:{}", orderNo, extMark, JSON.toJSONString(executeParam));
                updateExecuteFail(asyncBatchTaskOrderDAO, executeTaskBean);
                return;
            }
        }

        while (true) {
            // 执行任务
            try {
                taskResult = executeTaskBean.executeTask(executeParam);
            } catch (Exception e) {
                log.error("执行任务异常失败=orderNo:{},extMark:{},param:{}", orderNo, extMark, JSON.toJSONString(executeParam), e);
                updateExecuteFail(asyncBatchTaskOrderDAO, executeTaskBean);
                return;
            }
            if (taskResult == null || !taskResult.getSuccess()) {
                log.error("执行任务失败=orderNo:{},extMark:{},param:{}", orderNo, extMark, JSON.toJSONString(executeParam));
                updateExecuteFail(asyncBatchTaskOrderDAO, executeTaskBean);
                return;
            }
            if (!taskResult.getHasMore()) {
                callback(asyncBatchTaskOrderDAO, executeTaskBean);
                return;
            }
            // 子任务完成 更新下次请求参数 以防任务突然挂掉供下次查询再次执行任务
            executeParam = taskResult.getNextParam();
            updateExecuteSuccess(asyncBatchTaskOrderDAO, executeParam);
        }

    }

    private void callback(AsyncBatchTaskOrderDAO asyncBatchTaskOrderDAO, ExecuteTask executeTask) {
        String orderNo = asyncBatchTaskOrderDAO.getOrderNo();
        String extMark = asyncBatchTaskOrderDAO.getExtMark();
        try {
            executeTask.callBackSuc(orderNo, extMark);
        } catch (Exception e) {
            log.warn("任务完成回调失败=orderNo:{},extMark:{}", orderNo, extMark, e);
            asyncBatchTaskOrderManager.updateCallbackFail(orderNo, extMark);
            return;
        }
        asyncBatchTaskOrderManager.updateCallbackSuccess(orderNo, extMark);
    }

    private void updateExecuteFail(AsyncBatchTaskOrderDAO asyncBatchTaskOrderDAO, ExecuteTask executeTaskBean) {
        Integer maxRetryTimes = asyncBatchTaskOrderDAO.getMaxRetryTimes();
        Integer failTimes = asyncBatchTaskOrderDAO.getFailTimes() + 1;
        String orderNo = asyncBatchTaskOrderDAO.getOrderNo();
        String extMark = asyncBatchTaskOrderDAO.getExtMark();
        // 大于失败次数则更新订单失败 回调执行任务失败
        if (failTimes >= maxRetryTimes) {
            asyncBatchTaskOrderManager.updateExecuteFail(orderNo, extMark, TaskOrderStatusEnum.TASK_EXECUTE_FAIL);
            try {
                executeTaskBean.callBackFail(orderNo, extMark);
            } catch (Exception e) {
                log.error("调用执行失败任务回调失败=orderNo:{},extMark:{}", orderNo, extMark);
            }
        } else {
            asyncBatchTaskOrderManager.updateExecuteFail(orderNo, extMark, TaskOrderStatusEnum.TASK_GOING_ON);
        }
    }

    private void updateExecuteSuccess(AsyncBatchTaskOrderDAO asyncBatchTaskOrderDAO, Object executeParam) {
        String serializerParam = KryoSerializerUtil.serializer(executeParam);
        String originalParam = JSON.toJSONString(executeParam);
        Integer intervalTime = asyncBatchTaskOrderDAO.getLowestIntervalTime();
        Date date = new Date(System.currentTimeMillis() + intervalTime * 60000);
        asyncBatchTaskOrderManager.updateExecuteParam(serializerParam, originalParam, date, asyncBatchTaskOrderDAO.getOrderNo(), asyncBatchTaskOrderDAO.getExtMark());
    }


    private AsyncBatchTaskOrderDAO convert(TaskOrderSubmit taskOrderSubmit) {
        AsyncBatchTaskOrderDAO asyncBatchTaskOrderDAO = new AsyncBatchTaskOrderDAO();
        asyncBatchTaskOrderDAO.setExtMark(taskOrderSubmit.getExtMark());
        long timeMillis = System.currentTimeMillis();
        asyncBatchTaskOrderDAO.setGmtCreate(timeMillis);
        asyncBatchTaskOrderDAO.setGmtUpdate(timeMillis);
        Integer lowestIntervalTime = taskOrderSubmit.getLowestIntervalTime();
        asyncBatchTaskOrderDAO.setLowestIntervalTime(lowestIntervalTime);
        asyncBatchTaskOrderDAO.setNextExecuteTime(new Date(timeMillis + lowestIntervalTime * 60000));
        asyncBatchTaskOrderDAO.setMaxRetryTimes(taskOrderSubmit.getMaxRetryTimes());
        asyncBatchTaskOrderDAO.setOrderNo(taskOrderSubmit.getOrderNo());
        asyncBatchTaskOrderDAO.setRequestParam(KryoSerializerUtil.serializer(taskOrderSubmit.getParam()));
        asyncBatchTaskOrderDAO.setOriginalRequestParam(JSON.toJSONString(taskOrderSubmit.getParam()));
        asyncBatchTaskOrderDAO.setSpringHandlerClassName(getSpringClassName(taskOrderSubmit.getExecuteClass()));
        return asyncBatchTaskOrderDAO;
    }


    private String getSpringClassName(Class cla) {
        String simpleName = cla.getSimpleName();
        String springClassName = simpleName.substring(0, 1).toLowerCase().concat(simpleName.substring(1));
        try {
            applicationContext.getBean(springClassName);
        } catch (Exception e) {
            throw new TaskException("spring获取类不存在" + springClassName);
        }
        return springClassName;
    }


    @Override
    public TaskOrderStatusEnum checkTaskOrder(String orderNo, String extMark) {
        AsyncBatchTaskOrderDAO taskOrderDAO = asyncBatchTaskOrderManager.getOrder(orderNo, extMark);
        if (taskOrderDAO == null) {
            return TaskOrderStatusEnum.TASK_NOT_EXIST;
        }
        Integer status = taskOrderDAO.getStatus();
        return TaskOrderStatusEnum.getByCode(status);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    //    --------------------------------------定时任务---------------------------------------------------------------

    private volatile String jobParam;
    private volatile Long jobExecuteTime;
    private final Integer jobInterval = 300000;
    private final Object lock = new Object();


    @Override
    public ReturnT<String> execute(String param) {
        // 并发锁 同一参数5分钟内不允许执行两次
        synchronized (lock) {
            long currentTimeMillis = System.currentTimeMillis();
            if (jobExecuteTime != null) {
                if (currentTimeMillis - jobExecuteTime < jobInterval) {
                    if (!StringUtils.isEmpty(param)) {
                        if (param.equals(this.jobParam)) {
                            return new ReturnT(ReturnT.FAIL_CODE, "同一参数五分钟内不允许重复执行");
                        }
                    } else if (jobParam == null) {
                        return new ReturnT(ReturnT.FAIL_CODE, "同一参数五分钟内不允许重复执行");
                    }
                }
            }
            jobParam = param;
            jobExecuteTime = currentTimeMillis;
        }
        Date date;
        if (!StringUtils.isEmpty(param)) {
            date = new Date(Long.valueOf(param));
        } else {
            date = new Date();
        }
        log.info("job执行任务=date:{}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date.getTime()));
        jobExecuteTask(date);
        return ReturnT.SUCCESS;
    }


    private final Integer taskPeriodTime = 86400000;

    private Boolean jobExecuteTask(Date nowDate) {
        int id = 0;
        while (true) {
            //  因为只有在第一次执行失败的才会在定时任务再次执行 数据量不会很大
            Date startDate = new Date(nowDate.getTime() - taskPeriodTime);
            AsyncBatchTaskOrderDAO executeTask = asyncBatchTaskOrderManager.getExecuteTask(id, startDate, nowDate);
            if (executeTask == null) {
                return Boolean.TRUE;
            }
            String orderNo = executeTask.getOrderNo();
            String extMark = executeTask.getExtMark();
            Date nextExecuteDate = new Date(nowDate.getTime() + 60000 * executeTask.getLowestIntervalTime());
            if (!asyncBatchTaskOrderManager.updateNextExecuteTime(orderNo, extMark, nextExecuteDate)) {
                log.warn("更新执行时间失败=orderNo:{},extMark:{}", orderNo, extMark);
                continue;
            }
            log.info("执行任务orderNo:{},extMark:{}", orderNo, extMark);
            id = executeTask.getId();
            // 异步执行 以防单个任务执行时间太长
            executor.execute(() -> executeTask(executeTask));
        }
    }


    //--------------------------------------------------------controller------------------------------------------------


    public Boolean executeTask(String orderNo, String extMark) {
        AsyncBatchTaskOrderDAO batchTaskOrderDAO = asyncBatchTaskOrderManager.getOrder(orderNo, extMark);
        if (batchTaskOrderDAO == null) {
            throw new TaskException("订单不存在=orderNo:" + orderNo + ",extMark:" + extMark);
        }
        executeTask(batchTaskOrderDAO);
        return Boolean.TRUE;
    }


    public Boolean updateTask(BatchTaskController.UpdateTask updateTask) {
        String orderNo = updateTask.getOrderNo();
        String extMark = updateTask.getExtMark();
        AsyncBatchTaskOrderDAO taskOrderDAO = asyncBatchTaskOrderManager.getOrder(orderNo, extMark);
        if (taskOrderDAO == null) {
            throw new TaskException("订单不存在=orderNo:" + orderNo + "extMark:" + extMark);
        }
        AsyncBatchTaskOrderDAO updateTaskOrder = new AsyncBatchTaskOrderDAO();
        updateTaskOrder.setId(taskOrderDAO.getId());
        updateTaskOrder.setDeleted(updateTask.getDeleted());
        Integer status = updateTask.getStatus();
        if (status != null) {
            TaskOrderStatusEnum statusEnum = TaskOrderStatusEnum.getByCode(status);
            if (statusEnum == null) {
                throw new TaskException("传入状态不存在=status:" + status);
            }
            updateTaskOrder.setStatus(status);
        }
        updateTaskOrder.setFailTimes(updateTask.getFailTime());
        if (updateTask.getNextExecuteTime() != null) {
            updateTaskOrder.setNextExecuteTime(new Date(updateTask.getNextExecuteTime()));
        }
        String requestParam = updateTask.getRequestParam();
        if (!StringUtils.isEmpty(requestParam)) {
            if (StringUtils.isEmpty(updateTask.getRequestParamClass())) {
                throw new TaskException("参数类名不能为空");
            }
            try {
                Class<?> loadClass = this.getClass().getClassLoader().loadClass(updateTask.getRequestParamClass());
                String serializer = KryoSerializerUtil.serializer(JSON.parseObject(requestParam, loadClass));
                updateTaskOrder.setRequestParam(serializer);
                updateTaskOrder.setOriginalRequestParam(requestParam);
            } catch (ClassNotFoundException e) {
                throw new TaskException(e);
            }
        }
        return asyncBatchTaskOrderManager.update(updateTaskOrder);
    }


}
