package so.dian.scm.batch.sdk.common.exception;

/**
 * @author XR
 * Created  on 2020/12/14.
 */
public class TaskException extends RuntimeException {

    public TaskException(String message) {
        super(message);
    }

    public TaskException(Throwable cause) {
        super(cause);
    }
}
