package so.dian.export.sdk.core.model;

import lombok.Data;

import java.util.List;

/**
 * @author XR
 * Created  on 2020/12/15.
 */
@Data
public class ExportResult<T,R> {

    private boolean hasMore;

    private List<R> data;

    private T nextRequest;
}
