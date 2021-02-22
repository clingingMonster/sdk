package so.dian.export.sdk.core.handler;

import so.dian.export.sdk.api.model.AsyncExportReq;
import so.dian.export.sdk.api.model.ExportLoopReq;
import so.dian.export.sdk.api.model.ExportReq;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author XR
 * Created  on 2020/12/14.
 */
public interface FileHandler {

    /**
     * 同步处理
     *
     * @param exportReq 同步处理请求
     * @param supplier  获取参数的方法
     * @return 上传地址url
     */
    <T> String syncHandle(ExportReq exportReq, Supplier<List<T>> supplier);


    /**
     * 同步循环处理
     *
     * @param exportLoopReq 获取参数的方法
     * @param <T>           返回值
     * @return 上传地址url
     */

    <T, R> String syncLoopHandle(ExportLoopReq<T, R> exportLoopReq);

    /**
     * 异步处理 同时发送
     *
     * @param asyncExportReq 异步处理请求
     * @param <R>            方法的返回数据类型
     * @param <T>            方法请求参数类型
     */
    <T, R> void asyncHandle(AsyncExportReq<T, R> asyncExportReq);
}
