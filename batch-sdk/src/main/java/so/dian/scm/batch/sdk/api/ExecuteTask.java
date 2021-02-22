package so.dian.scm.batch.sdk.api;

import so.dian.scm.batch.sdk.common.model.TaskResult;

/**
 * @author XR
 * Created  on 2021/1/20.
 */
public interface ExecuteTask<R> {

    /**
     * 当首次处理失败后,后续再次发起重试任务<p>
     * 校验参数是否可用 若不可用,返回可用的参数,
     * 若是任务已经完成 <code>TaskResult.hasMore</>请赋值false
     * <p>
     * 接口只有任务失败的下次再次发起时第一次执行子任务时会调用
     * 若是执行接口已经实现幂等 可直接返回 param
     *
     * @param param 请求参数
     * @return TaskResult
     */
    TaskResult<R> checkAndGetParam(R param);

    /**
     * 执行任务
     *
     * @param param 请求参数
     * @return TaskResult
     */
    TaskResult<R> executeTask(R param);

    /**
     * 任务全部执行完成 回调接口
     *
     * @param orderNo 订单号
     * @param extMark 额外标记,若是提交时没有传次参数 则参数是空字符串""
     */
    void callBackSuc(String orderNo, String extMark);

    /**
     * 任务执行失败 回调接口
     *
     * @param orderNo 订单号
     * @param extMark 额外标记,若是提交时没有传次参数 则参数是空字符串""
     */
    void callBackFail(String orderNo, String extMark);

}
