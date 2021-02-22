package so.dian.export.sdk.core.upload;

/**
 * @author XR
 * Created  on 2020/12/11.
 */
public interface FileUpload {

    /**
     * 文件上传
     *
     * @param bytes    字节
     * @param fileName 文件名称
     * @param fileType 文件类型
     * @return 路径
     */
    String upload(byte[] bytes, String fileName, String fileType);


}
