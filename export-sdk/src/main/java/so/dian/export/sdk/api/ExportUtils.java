package so.dian.export.sdk.api;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.mail.MailSender;
import so.dian.export.sdk.api.model.AsyncExportReq;
import so.dian.export.sdk.api.model.ExportLoopReq;
import so.dian.export.sdk.api.model.ExportReq;
import so.dian.export.sdk.core.ExportFunction;
import so.dian.export.sdk.core.handler.DefaultFileHandler;
import so.dian.export.sdk.core.handler.FileHandler;
import so.dian.export.sdk.core.model.ExportException;
import so.dian.export.sdk.core.upload.FileUpload;
import so.dian.export.sdk.core.sender.Sender;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * @author XR
 * Created  on 2020/12/11.
 */
public class ExportUtils {

    ExportUtils() {

    }

    private static FileHandler defaultFileHandler = new DefaultFileHandler();
    private static FileUpload defaultFileUpload;
    private static Sender sender;
    // 异步执行线程池
    private static Executor executor;


    /**
     * 当需要使用oss文件服务器时需要调用次接口</p>
     * 初始化oss文件服务器配置
     * 当接入springBoot可以使用自动加载无需调用此接口,但是要配置对应的配置文件
     *
     * @param fileUpload 文件上传客户端
     * @see ConfigUtils
     */
    static void initOssClient(FileUpload fileUpload) {
        defaultFileUpload = fileUpload;
    }

    /**
     * 当需要使用文件地址发送服务时 需要调用次接口</p>
     * 当接入springBoot时并且配置了{@link MailSender} 那么默认会采用mailSender发送信息
     *
     * @param sender 发送客户端
     * @see ConfigUtils
     */
    static void initSender(Sender sender) {
        ExportUtils.sender = sender;
    }

    static void initExecutor(Executor executor) {
        ExportUtils.executor = executor;
    }

    /**
     * 文件上传
     *
     * @param fileName 文件名称 要带上文件格式
     * @param supplier 数据
     *                 类型 要导出的field要加上{@link so.dian.export.sdk.core.enums.ExportFiled}
     * @return 文件路径
     * <pre>
     *     {@code
     *     @ExportFiled(headName = "名字")
     *     private String name;
     *     }
     * </pre>
     *
     * </code>
     */
    public static <T>  String export(String fileName, Supplier<List<T>> supplier) {
        return defaultFileHandler.syncHandle(buildUploadReq(fileName), supplier);
    }

    /**
     * 循环处理导出
     *
     * @param fileName       文件名称 要带上文件格式
     * @param requestParam   请求参数
     * @param exportFunction 获取数据函数
     * @param <T>            请求参数类
     * @param <R>            返回类型类
     * @return String 文件地址
     */
    public static <T, R> String exportLoop(String fileName, T requestParam, ExportFunction<T, R> exportFunction) {
        return defaultFileHandler.syncLoopHandle(buildLoopExportReq(fileName, requestParam, exportFunction));
    }

    /**
     * 异步导出
     *
     * @param fileName       文件名称 要带上文件格式
     * @param title          发送的主题
     * @param address        地址
     * @param requestParam   函数请求的参数
     * @param exportFunction 函数
     * @param <T>            入参类型
     * @param <R>            结果类型
     */
    public static <T, R> void asyncSendExport(String fileName, String title, List<String> address, T requestParam, ExportFunction<T, R> exportFunction) {
        AsyncExportReq<T, R> asyncUploadReq = buildAsyncSendUploadReq(fileName, title, address, requestParam, exportFunction);
        executor.execute(() -> defaultFileHandler.asyncHandle(asyncUploadReq));
    }


    private static void validateFileName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw new ExportException("文件名称为空");
        }
    }

    private static ExportReq buildUploadReq(String fileName) {
        validateFileName(fileName);
        ExportReq exportReq = new ExportReq();
        exportReq.setFileUpload(defaultFileUpload);
        exportReq.setFileName(fileName);
        return exportReq;
    }


    private static <T, R> AsyncExportReq<T, R> buildAsyncSendUploadReq(String fileName, String title, List<String> address, T requestParam,
                                                                       ExportFunction<T, R> exportFunction) {
        validateFileName(fileName);
        if (CollectionUtils.isEmpty(address)) {
            throw new ExportException("地址为空");
        }
        AsyncExportReq<T, R> asyncUploadReq = new AsyncExportReq<>();
        asyncUploadReq.setAddress(address);
        asyncUploadReq.setFileName(fileName);
        asyncUploadReq.setSender(sender);
        asyncUploadReq.setTitle(title);
        asyncUploadReq.setRequest(requestParam);
        asyncUploadReq.setExportFunction(exportFunction);
        if (defaultFileUpload == null) {
            throw new ExportException("文件上传客户端为空");
        }
        asyncUploadReq.setFileUpload(defaultFileUpload);
        return asyncUploadReq;
    }


    private static <T, R> ExportLoopReq<T, R> buildLoopExportReq(String fileName, T requestParam, ExportFunction<T, R> exportFunction) {
        validateFileName(fileName);
        ExportLoopReq<T, R> exportReq = new ExportLoopReq<>();
        exportReq.setFileUpload(defaultFileUpload);
        exportReq.setFileName(fileName);
        exportReq.setRequest(requestParam);
        exportReq.setExportFunction(exportFunction);
        if (defaultFileUpload == null) {
            throw new ExportException("文件上传客户端为空");
        }
        exportReq.setFileUpload(defaultFileUpload);
        return exportReq;
    }

}
