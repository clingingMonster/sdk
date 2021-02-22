package so.dian.export.sdk.core.upload;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import so.dian.export.sdk.core.model.ExportException;
import so.dian.export.sdk.core.utils.FileUtils;

import java.io.ByteArrayInputStream;

/**
 * @author XR
 * Created  on 2020/12/11.
 */
public class OssFileUpload implements FileUpload {

    private OssConfig ossConfig;

    /**
     * 初始化oss配置
     *
     * @param ossConfig oss配置
     */

    public OssFileUpload(OssConfig ossConfig) {
        this.ossConfig = ossConfig;
    }


    public static class OssConfig {
        // 这个是默认值 可在初始化的时候换掉
        private String IMAGE_DOMAIN_WITH_PROTOCOL_OSS = "https://lhc-image.oss-cn-beijing.aliyuncs.com/";
        private String accessId;
        private String accessKey;
        private String endPoint;
        private String bucketName;
        private String applicationName;


        public void IMAGE_DOMAIN_WITH_PROTOCOL_OSS(String IMAGE_DOMAIN_WITH_PROTOCOL_OSS) {
            this.IMAGE_DOMAIN_WITH_PROTOCOL_OSS = IMAGE_DOMAIN_WITH_PROTOCOL_OSS;
        }

        public void setAccessId(String accessId) {
            this.accessId = accessId;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }


        public void setEndPoint(String endPoint) {
            this.endPoint = endPoint;
        }


        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }


        public void setApplicationName(String applicationName) {
            this.applicationName = applicationName;
        }
    }

    @Override
    public String upload(byte[] bytes, String fileName, String fileType) {
        OSSClient ossClient = getOssClient();
        ObjectMetadata objectMeta = new ObjectMetadata();
        objectMeta.setContentLength(bytes.length);
        objectMeta.setContentType(FileUtils.getMimeType(fileType));
        String fullFileName = ossConfig.applicationName + FileUtils.SPLIT + fileName + "." + fileType;
        String[] subStr = fullFileName.split(FileUtils.SPLIT);
        objectMeta.setContentDisposition("inline; filename=" + subStr[subStr.length - 1]);
        ossClient.putObject(ossConfig.bucketName, fullFileName, new ByteArrayInputStream(bytes), objectMeta);
        return ossConfig.IMAGE_DOMAIN_WITH_PROTOCOL_OSS + fullFileName;
    }

    private OSSClient getOssClient() {
        if (ossConfig == null) {
            throw new ExportException("oss客户端配置信息为空");
        }
        return new OSSClient(ossConfig.endPoint, ossConfig.accessId, ossConfig.accessKey);
    }

}
