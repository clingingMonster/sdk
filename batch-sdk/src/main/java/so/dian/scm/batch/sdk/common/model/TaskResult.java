package so.dian.scm.batch.sdk.common.model;

import lombok.Data;

/**
 * @author XR
 * Created  on 2021/1/20.
 */
@Data
public class TaskResult<T> {

    private Boolean success;

    private T nextParam;

    private Boolean hasMore;

    private TaskResult() {
    }

    public static <T> TaskResult<T> suc(T param) {
        TaskResult taskResult = new TaskResult();
        taskResult.success = Boolean.TRUE;
        taskResult.hasMore = Boolean.TRUE;
        taskResult.nextParam = param;
        return taskResult;
    }

    public static <T> TaskResult<T> fail() {
        TaskResult taskResult = new TaskResult();
        taskResult.success = Boolean.FALSE;
        taskResult.hasMore = Boolean.FALSE;
        return taskResult;
    }

    public static <T> TaskResult<T> notMore() {
        TaskResult taskResult = new TaskResult();
        taskResult.success = Boolean.TRUE;
        taskResult.hasMore = Boolean.FALSE;
        return taskResult;
    }


}
