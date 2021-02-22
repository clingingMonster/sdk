package so.dian.export.sdk.api;

import org.springframework.mail.MailSender;
import so.dian.export.sdk.core.sender.Sender;
import so.dian.export.sdk.core.upload.FileUpload;

import java.util.concurrent.Executor;

/**
 * @author XR
 * Created  on 2020/12/16.
 */
public class ConfigUtils {

    /**
     * 当需要使用oss文件服务器时需要调用次接口</p>
     * 初始化oss文件服务器配置
     * 当接入springBoot可以使用自动加载无需调用此接口,但是要配置对应的配置文件
     *
     * @param fileUpload 配置
     */
    public static void initOssClient(FileUpload fileUpload) {
        ExportUtils.initOssClient(fileUpload);
    }

    /**
     * 当需要使用文件地址发送服务时 需要调用次接口</p>
     * 当接入springBoot时并且配置了{@link MailSender} 那么默认会采用mailSender发送信息
     *
     * @param sender 发送客户端
     */
    public static void initSender(Sender sender) {
        ExportUtils.initSender(sender);
    }

    /**
     * 初始化异步导出执行的线程池
     * 接入springBoot可不用调用次线程
     *
     * @param executor 线程池
     */
    public static void initExecutor(Executor executor) {
        ExportUtils.initExecutor(executor);
    }
}
