package so.dian.scm.batch.sdk.api.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import so.dian.scm.batch.sdk.common.exception.TaskException;
import so.dian.scm.batch.sdk.service.TaskHandler;

import java.util.Arrays;

/**
 * @author XR
 * Created  on 2021/1/20.
 */
public class ConfigUtils {

    private ConfigUtils() {

    }

    public static final String SCAN_PATH = "so.dian.scm.batch.sdk.mapper";
    public static final String MAPPER_LOCATION_PATH = "classpath*:/batch/mapper/*.xml";

    public static String mapperScanPath() {
        return SCAN_PATH;
    }

    /**
     * 数据源路径获取 同时包装batchTask的mapper路径
     *
     * @param localPath
     * @return
     */
    public static Resource[] getResources(String localPath) {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources(localPath);
            Resource[] resources1 = resolver.getResources(MAPPER_LOCATION_PATH);
            int length = resources.length + resources1.length;
            Resource[] resources2 = Arrays.copyOf(resources, length);
            resources2[length - 1] = resources1[0];
            return resources2;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 初始化BatchTaskUtils TaskHandler的实现类
     *
     * @param taskHandler
     */
    public static void initTaskHandler(TaskHandler taskHandler) {
        if (taskHandler != null) {
            BatchTaskUtils.initTaskHandler(taskHandler);
            return;
        }
        throw new TaskException("initTaskHandler参数为空");
    }


}
