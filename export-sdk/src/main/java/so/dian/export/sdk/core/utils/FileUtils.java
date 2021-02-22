package so.dian.export.sdk.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import so.dian.export.sdk.core.model.ExportException;

/**
 * @author XR
 * Created  on 2020/12/14.
 */


@Slf4j
public class FileUtils {

    public static final String SPLIT = "/";


    public static String getMimeType(String type) {
        if (StringUtils.isBlank(type)) {
            throw new ExportException("文件类型为空");
        }
        switch (type) {
            case "csv":
                return "text/comma-separated-values";
            case "xls":
            case "xlsx":
                return "application/vnd.ms-excel";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "gif":
                return "gif";
            case "png":
                return "png";
            case "jpg":
                return "jpg";
            default:
                log.warn("没有配置文件类型映射关系type:" + type);
                return type;

        }
    }

    public static String getFileName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw new ExportException("文件名称为空");
        }
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            throw new ExportException("文件格式不正确:" + fileName);
        }
        return fileName.substring(0, index);
    }

    public static String getFileType(String fileName) {

        if (StringUtils.isBlank(fileName)) {
            throw new ExportException("文件名称为空");
        }
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            throw new ExportException("缺失文件格式fileName:" + fileName);
        }
        return fileName.substring(index);
    }


}
