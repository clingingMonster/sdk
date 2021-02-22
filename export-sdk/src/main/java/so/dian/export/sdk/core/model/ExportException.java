package so.dian.export.sdk.core.model;

/**
 * @author XR
 * Created  on 2020/12/14.
 */
public class ExportException extends RuntimeException {

    public ExportException(String message) {
        super(message);
    }

    public ExportException(Throwable cause) {
        super(cause);
    }
}
