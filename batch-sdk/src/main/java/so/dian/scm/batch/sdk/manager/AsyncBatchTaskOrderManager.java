package so.dian.scm.batch.sdk.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import so.dian.scm.batch.sdk.common.dao.AsyncBatchTaskOrderDAO;
import so.dian.scm.batch.sdk.common.enums.TaskOrderStatusEnum;
import so.dian.scm.batch.sdk.common.exception.TaskException;
import so.dian.scm.batch.sdk.mapper.AsyncBatchTaskOrderDAOMapper;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author XR
 * Created  on 2021/1/20.
 */
@Component
@Slf4j
public class AsyncBatchTaskOrderManager {

    @Resource
    private AsyncBatchTaskOrderDAOMapper asyncBatchTaskOrderDAOMapper;


    public void addOrder(AsyncBatchTaskOrderDAO taskOrderDAO) {
        if (1 != asyncBatchTaskOrderDAOMapper.insert(taskOrderDAO)) {
            throw new TaskException("订单保存失败=orderNo:" + taskOrderDAO.getOrderNo() + ",extMark:" + taskOrderDAO.getExtMark());
        }
    }


    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void addOrderNewTR(AsyncBatchTaskOrderDAO taskOrderDAO) {
        if (1 != asyncBatchTaskOrderDAOMapper.insert(taskOrderDAO)) {
            throw new TaskException("订单保存失败=orderNo:" + taskOrderDAO.getOrderNo() + ",extMark:" + taskOrderDAO.getExtMark());
        }
    }

    public AsyncBatchTaskOrderDAO getOrder(String orderNo, String extMark) {
        return asyncBatchTaskOrderDAOMapper.selectByOrderNo(orderNo, extMark);
    }

    public void confirmOrder(String orderNo, String extMark) {
        if (1 != asyncBatchTaskOrderDAOMapper.confirmOrder(orderNo, extMark)) {
            throw new TaskException("确认订单失败=orderNo:" + orderNo + ",extMark:" + extMark);
        }
    }

    public void cancelOrder(String orderNo, String extMark) {
        if (1 != asyncBatchTaskOrderDAOMapper.cancelOrder(orderNo, extMark)) {
            throw new TaskException("取消订单失败=orderNo:" + orderNo + ",extMark:" + extMark);
        }

    }

    public AsyncBatchTaskOrderDAO getExecuteTask(int id, Date startDate,Date endDate) {
        return asyncBatchTaskOrderDAOMapper.getExecuteTask(id,startDate, endDate);
    }

    public void updateExecuteParam(String param, String originalParam, Date nextExecuteTime, String orderNo, String extMark) {
        if (1 != asyncBatchTaskOrderDAOMapper.updateExecuteParam(orderNo, extMark, param, originalParam, nextExecuteTime)) {
            log.warn("取消订单失败=orderNo:{},extMark:{}", orderNo, extMark);
        }
    }

    public void updateExecuteFail(String orderNo, String extMark, TaskOrderStatusEnum taskOrderStatusEnum) {
        if (1 != asyncBatchTaskOrderDAOMapper.updateExecuteFail(orderNo, extMark, taskOrderStatusEnum.getCode())) {
            log.warn("更新订单失败次数和状态失败=orderNo:{},extMark:{}", orderNo, extMark);
        }
    }

    public void updateCallbackFail(String orderNo, String extMark) {
        if (1 != asyncBatchTaskOrderDAOMapper.updateExecuteFail(orderNo, extMark, TaskOrderStatusEnum.TASK_EXECUTE_CALLBACK_FAIL.getCode())) {
            log.error("更新订单完成,回调失败状态失败=orderNo:{},extMark:{}", orderNo, extMark);
        }
    }

    public void updateCallbackSuccess(String orderNo, String extMark) {
        if (1 != asyncBatchTaskOrderDAOMapper.updateOrderStatus(orderNo, extMark, TaskOrderStatusEnum.TASK_EXECUTE_SUCCESS.getCode())) {
            log.error("更新订单完成,回调成功状态失败=orderNo:{},extMark:{}", orderNo, extMark);
        }
    }

    public Boolean updateNextExecuteTime(String orderNo, String extMark, Date nextExecuteDate) {
        return 1 == asyncBatchTaskOrderDAOMapper.updateNextExecuteDate(orderNo, extMark, nextExecuteDate);
    }

    public Boolean update(AsyncBatchTaskOrderDAO asyncBatchTaskOrderDAO) {
        return 1 == asyncBatchTaskOrderDAOMapper.update(asyncBatchTaskOrderDAO);
    }

}
