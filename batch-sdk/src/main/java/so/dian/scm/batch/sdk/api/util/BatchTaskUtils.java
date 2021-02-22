package so.dian.scm.batch.sdk.api.util;

import org.springframework.util.StringUtils;
import so.dian.scm.batch.sdk.api.DDExecuteTask;
import so.dian.scm.batch.sdk.api.ExecuteTask;
import so.dian.scm.batch.sdk.common.enums.TaskOrderStatusEnum;
import so.dian.scm.batch.sdk.common.exception.TaskException;
import so.dian.scm.batch.sdk.common.model.TaskOrderSubmit;
import so.dian.scm.batch.sdk.service.TaskHandler;

/**
 * @author XR
 * Created  on 2021/1/20.
 */
public class BatchTaskUtils {

    private static TaskHandler taskHandler;

    static void initTaskHandler(TaskHandler taskHandler) {
        BatchTaskUtils.taskHandler = taskHandler;
    }

    private BatchTaskUtils() {

    }

    /**
     * 不同库提交任务
     *
     * @param orderNo      订单号
     * @param request      第一次请求参数
     * @param executeClass 执行任务类
     * @param <R>          参数类型
     * @return Boolean
     */
    public static <R> Boolean submitDDTask(String orderNo, R request, Class<? extends DDExecuteTask<R>> executeClass) {
        return submitDDTask(orderNo, "", request, executeClass);
    }


    /**
     * 不同库提交任务
     *
     * @param orderNo      订单号
     * @param extMark      额外标记和 orderNo组成唯一键
     * @param request      第一次请求参数
     * @param executeClass 执行任务类
     * @param <R>          参数类型
     * @return Boolean
     */
    public static <R> Boolean submitDDTask(String orderNo, String extMark, R request, Class<? extends DDExecuteTask<R>> executeClass) {
        return submitDDTask(orderNo, extMark, request, 3, executeClass);
    }

    /**
     * 不同库提交任务
     *
     * @param orderNo       订单号
     * @param extMark       额外标记和 orderNo组成唯一键
     * @param request       第一次请求参数
     * @param maxRetryTimes 失败最大重试参数
     * @param executeClass  执行任务类
     * @param <R>           参数类型
     * @return Boolean
     */
    public static <R> Boolean submitDDTask(String orderNo, String extMark, R request, int maxRetryTimes, Class<? extends DDExecuteTask<R>> executeClass) {
        return submitDDTask(orderNo, extMark, request, maxRetryTimes, 20, executeClass);
    }

    /**
     * 不同库提交任务
     *
     * @param orderNo            订单号
     * @param extMark            额外标记和 orderNo组成唯一键
     * @param request            第一次请求参数
     * @param maxRetryTimes      失败最大重试参数
     * @param lowestIntervalTime 任务失败下次发起最小间隔
     * @param executeClass       执行任务类
     * @param <R>                参数类型
     * @return Boolean
     */
    public static <R> Boolean submitDDTask(String orderNo, String extMark, R request, int maxRetryTimes, int lowestIntervalTime, Class<? extends DDExecuteTask<R>> executeClass) {
        return taskHandler.submitTaskOrder(build(orderNo, extMark, request, maxRetryTimes, lowestIntervalTime, executeClass, Boolean.TRUE));
    }

    private static final Integer EXT_MARK_LENGTH = 64;
    private static final Integer ORDER_NO_LENGTH = 64;
    private static final Integer MAX_RETRY_TIMES = 10;


    private static <R> TaskOrderSubmit<R> build(String orderNo, String extMark, R request, int maxRetryTimes, int lowestIntervalTime, Class<? extends ExecuteTask<R>> executeClass, Boolean diffDatabase) {
        if (StringUtils.isEmpty(orderNo)) {
            throw new TaskException("orderNo订单号为空");
        }
        orderNo = orderNo.trim();
        if (orderNo.length() > ORDER_NO_LENGTH) {
            throw new TaskException("orderNo长度超过64位:" + extMark);
        }
        TaskOrderSubmit<R> taskOrderSubmit = new TaskOrderSubmit<>();
        taskOrderSubmit.setExecuteClass(executeClass);
        if (!StringUtils.isEmpty(extMark)) {
            if (extMark.length() > EXT_MARK_LENGTH) {
                throw new TaskException("extMark长度超过64位:" + extMark);
            }
        }
        if (maxRetryTimes < 0) {
            throw new TaskException("最大重试次数不能为负数");
        }
        if (maxRetryTimes > BatchTaskUtils.MAX_RETRY_TIMES) {
            throw new TaskException("最大重试次数不能超过" + BatchTaskUtils.MAX_RETRY_TIMES + "次");
        }
        if (lowestIntervalTime < 0) {
            throw new TaskException("任务最低间隔时间不能为负数");
        }
        taskOrderSubmit.setExtMark(extMark);
        taskOrderSubmit.setOrderNo(orderNo);
        taskOrderSubmit.setParam(request);
        taskOrderSubmit.setDiffDatabase(diffDatabase);
        taskOrderSubmit.setMaxRetryTimes(maxRetryTimes);
        taskOrderSubmit.setLowestIntervalTime(lowestIntervalTime);
        return taskOrderSubmit;
    }

    /**
     * 同库提交任务
     *
     * @param orderNo      订单号
     * @param request      第一次请求参数
     * @param executeClass 执行任务类
     * @param <R>          参数类型
     * @return Boolean
     */
    public static <R> Boolean submitSDTask(String orderNo, R request, Class<? extends ExecuteTask<R>> executeClass) {
        return submitSDTask(orderNo, "", request, executeClass);
    }

    /**
     * 同库提交任务
     *
     * @param orderNo      订单号
     * @param extMark      额外标记和 orderNo组成唯一键
     * @param request      第一次请求参数
     * @param executeClass 执行任务类
     * @param <R>          参数类型
     * @return Boolean
     */
    public static <R> Boolean submitSDTask(String orderNo, String extMark, R request, Class<? extends ExecuteTask<R>> executeClass) {
        return submitSDTask(orderNo, extMark, request, 3, executeClass);
    }

    /**
     * 同库提交任务
     *
     * @param orderNo       订单号
     * @param extMark       额外标记和 orderNo组成唯一键
     * @param request       第一次请求参数
     * @param maxRetryTimes 失败最大重试参数
     * @param executeClass  执行任务类
     * @param <R>           参数类型
     * @return Boolean
     */
    public static <R> Boolean submitSDTask(String orderNo, String extMark, R request, int maxRetryTimes, Class<? extends ExecuteTask<R>> executeClass) {
        return submitSDTask(orderNo, extMark, request, maxRetryTimes, 20, executeClass);
    }

    /**
     * 同库提交任务
     *
     * @param orderNo            订单号
     * @param extMark            额外标记和 orderNo组成唯一键
     * @param request            第一次请求参数
     * @param maxRetryTimes      失败最大重试参数
     * @param lowestIntervalTime 任务失败下次发起最小间隔
     * @param executeClass       执行任务类
     * @param <R>                参数类型
     * @return Boolean
     */
    public static <R> Boolean submitSDTask(String orderNo, String extMark, R request, int maxRetryTimes, int lowestIntervalTime, Class<? extends ExecuteTask<R>> executeClass) {
        return taskHandler.submitTaskOrder(build(orderNo, extMark, request, maxRetryTimes, lowestIntervalTime, executeClass, Boolean.FALSE));
    }


    /**
     * 检查任务
     *
     * @param orderNo 订单号
     * @return TaskOrderStatusEnum
     */
    public TaskOrderStatusEnum checkOrder(String orderNo) {
        return checkOrder(orderNo, "");
    }

    /**
     * 检查任务
     *
     * @param orderNo 订单号
     * @param extMark 额外标记和 orderNo组成唯一键
     * @return TaskOrderStatusEnum
     */
    public TaskOrderStatusEnum checkOrder(String orderNo, String extMark) {
        if (StringUtils.isEmpty(orderNo)) {
            throw new TaskException("orderNo订单号为空");
        }
        if (extMark == null) {
            throw new TaskException("extMark不能为null");
        }
        return taskHandler.checkTaskOrder(orderNo, extMark);
    }
}
