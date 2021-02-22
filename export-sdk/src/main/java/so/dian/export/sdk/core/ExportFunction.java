package so.dian.export.sdk.core;

import so.dian.export.sdk.core.model.ExportResult;

/**
 * @author XR
 * Created  on 2020/12/15.
 */
@FunctionalInterface
public interface ExportFunction<T, R> {


    ExportResult<T, R> apply(T param);
}
