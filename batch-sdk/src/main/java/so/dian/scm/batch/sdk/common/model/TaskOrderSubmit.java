package so.dian.scm.batch.sdk.common.model;

import lombok.Data;
import so.dian.scm.batch.sdk.api.ExecuteTask;

/**
 * @author XR
 * Created  on 2021/1/20.
 */
@Data
public class TaskOrderSubmit<R> {

    private R param;

    private Class<? extends ExecuteTask<R>> executeClass;

    private String orderNo;

    private Integer maxRetryTimes;

    private Integer lowestIntervalTime;

    private String extMark;

    private Boolean diffDatabase;
}
