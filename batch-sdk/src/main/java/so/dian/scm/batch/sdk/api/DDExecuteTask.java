package so.dian.scm.batch.sdk.api;

/**
 * 当业务和任务订单不在同一个库时,使用了事务监听器去确认任务和业务订单一致
 * <p>
 * 但是没有事务的强一致无法保证当前订单和业务订单一定是一致的,所以当这种小概率
 * 事件发生时,需要实现 DDExecuteTask.class来再次确认订单是否存在
 *
 * @author XR
 * Created  on 2021/1/20.
 */
public interface DDExecuteTask<R> extends ExecuteTask<R> {

    /**
     * 检查当前订单是否存在
     *
     * @param orderNo 订单号
     * @param extMark 标记
     * @return
     */
    Boolean checkTaskOrderSubmit(String orderNo, String extMark);

}
