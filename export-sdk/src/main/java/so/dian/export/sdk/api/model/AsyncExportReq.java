package so.dian.export.sdk.api.model;

import lombok.Data;
import so.dian.export.sdk.core.ExportFunction;
import so.dian.export.sdk.core.sender.Sender;

import java.util.List;

/**
 * @author XR
 * Created  on 2020/12/15.
 */
@Data
public class AsyncExportReq<T, R> extends ExportReq {

    private List<String> address;

    private String title;

    private Sender sender;

    private ExportFunction<T, R> exportFunction;

    private T request;

}
