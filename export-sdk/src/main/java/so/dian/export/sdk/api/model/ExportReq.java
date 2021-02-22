package so.dian.export.sdk.api.model;

import lombok.Data;
import so.dian.export.sdk.core.model.ExportException;
import so.dian.export.sdk.core.upload.FileUpload;

/**
 * @author XR
 * Created  on 2020/12/15.
 */
@Data
public class ExportReq {

    private String fileName;

    private FileUpload fileUpload;

    public String getFileType() {
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            throw new ExportException("缺失文件格式fileName:" + fileName);
        }
        return fileName.substring(index + 1);
    }

    public String getFileName() {
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            throw new ExportException("文件格式不正确:" + fileName);
        }
        return fileName.substring(0, index);
    }


}
