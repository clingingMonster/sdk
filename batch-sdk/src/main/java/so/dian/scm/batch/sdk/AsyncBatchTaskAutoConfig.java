package so.dian.scm.batch.sdk;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import so.dian.scm.batch.sdk.api.util.ConfigUtils;
import so.dian.scm.batch.sdk.service.JobTaskHandler;

import javax.annotation.Resource;
import java.util.concurrent.Executor;

/**
 * @author XR
 * Created  on 2021/1/20.
 */
@ComponentScan(basePackages = "so.dian.scm.batch.sdk")
public class AsyncBatchTaskAutoConfig implements InitializingBean {

    @Resource
    private JobTaskHandler jobTaskHandler;

    @Override
    public void afterPropertiesSet() {
        ConfigUtils.initTaskHandler(jobTaskHandler);
    }

    @Bean("batchTask")
    @ConditionalOnProperty(name = "batchTask.executor", matchIfMissing = true)
    public Executor getExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        return executor;
    }


}
