package so.dian.export.sdk.api.model;

import lombok.Data;
import so.dian.export.sdk.core.ExportFunction;

/**
 * @author XR
 * Created  on 2020/12/25.
 */
@Data
public class ExportLoopReq<T, R> extends ExportReq {

    private ExportFunction<T, R> exportFunction;

    private T request;

}
