package so.dian.scm.batch.sdk.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author XR
 * Created  on 2021/1/20.
 */
@AllArgsConstructor
@Getter
public enum TaskOrderStatusEnum {

    TASK_NOT_EXIST(0, "任务不存在"),
    TASK_SUBMIT_UN_CONFIRM(1, "任务提交,未确认"),
    TASK_GOING_ON(2, "任务进行中"),
    TASK_EXECUTE_CALLBACK_FAIL(3, "任务执行成功,回调失败"),
    TASK_EXECUTE_SUCCESS(4, "任务执行成功,回调成功"),
    TASK_EXECUTE_FAIL(5, "任务执行失败");

    private int code;

    private String desc;

    public static TaskOrderStatusEnum getByCode(int code) {
        Optional<TaskOrderStatusEnum> optional = Arrays.stream(TaskOrderStatusEnum.values()).filter(a -> a.code == code).findFirst();
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

}

