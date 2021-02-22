package so.dian.scm.batch.sdk.service;

import so.dian.scm.batch.sdk.common.enums.TaskOrderStatusEnum;
import so.dian.scm.batch.sdk.common.model.TaskOrderSubmit;

/**
 * @author XR
 * Created  on 2021/1/20.
 */
public interface TaskHandler {

    /**
     * 任务提交
     *
     * @param taskOrderSubmit 提交任务参数
     * @return Boolean
     */
    Boolean submitTaskOrder(TaskOrderSubmit<?> taskOrderSubmit);

    /**
     * 查看任务状态
     *
     * @param orderNo 订单号
     * @param extMark 标记
     * @return TaskOrderStatusEnum
     */
    TaskOrderStatusEnum checkTaskOrder(String orderNo, String extMark);


}
