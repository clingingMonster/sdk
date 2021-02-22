package so.dian.export.sdk.auto;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import so.dian.export.sdk.api.ConfigUtils;
import so.dian.export.sdk.core.sender.EmailSender;
import so.dian.export.sdk.core.upload.OssFileUpload;

import java.util.concurrent.Executor;

/**
 * @author XR
 * Created  on 2020/12/11.
 */
@Configuration
@Slf4j
public class ExportDefaultAutoConfig implements InitializingBean, ApplicationContextAware {


    private ApplicationContext applicationContext;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.mail.username:小电科技}")
    private String emailFrom;

    // oss配置
    @Value("${aliyun.oss.endPoint: }")
    private String endPoint;

    @Value("${aliyun.oss.accessId: }")
    private String accessId;

    @Value("${aliyun.oss.accessKey: }")
    private String accessKey;

    @Value("${aliyun.oss.bucketName: }")
    private String bucketName;

    @Value("${aliyun.oss.protocolOss: }")
    private String protocolOss;


    /**
     * 初始化默认实现
     */
    @Override
    public void afterPropertiesSet() {
        initSender();
        initOssConfig();
        initExecutor();
    }

    private void initSender() {
        try {
            org.springframework.mail.MailSender bean = applicationContext.getBean(MailSender.class);
            ConfigUtils.initSender(new EmailSender(bean, emailFrom));
        } catch (Exception e) {
            log.warn("java mail不存在");
        }
    }

    private void initOssConfig() {
        if (StringUtils.isNotBlank(accessId) && StringUtils.isNotBlank(accessKey) &&
                StringUtils.isNotBlank(endPoint) && StringUtils.isNotBlank(accessId)) {
            OssFileUpload.OssConfig ossConfig = new OssFileUpload.OssConfig();
            ossConfig.setAccessId(accessId);
            ossConfig.setAccessKey(accessKey);
            ossConfig.setEndPoint(endPoint);
            ossConfig.setBucketName(bucketName);
            ossConfig.setApplicationName(applicationName);
            if (StringUtils.isNotBlank(protocolOss)) {
                ossConfig.IMAGE_DOMAIN_WITH_PROTOCOL_OSS(protocolOss);
            }
            ConfigUtils.initOssClient(new OssFileUpload(ossConfig));
        }
    }

    private void initExecutor() {
        try {
            ConfigUtils.initExecutor(applicationContext.getBean("exportExecutor", Executor.class));
        } catch (Exception e) {
            log.warn("spring自动加载 导出线程池未初始化");
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Bean("exportExecutor")
    @ConditionalOnProperty(name = "export.executor", matchIfMissing = true)
    public Executor getExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(0);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        return executor;
    }


}
