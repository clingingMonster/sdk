package so.dian.scm.batch.sdk.controller;

import lombok.Data;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import so.dian.scm.batch.sdk.common.exception.TaskException;
import so.dian.scm.batch.sdk.service.JobTaskHandler;

import javax.annotation.Resource;

/**
 * @author XR
 * Created  on 2021/1/22.
 */
@RestController
public class BatchTaskController {


    @Resource
    private JobTaskHandler jobTaskHandler;


    @PostMapping("/batch/task/update")
    public Boolean updateTask(@RequestBody @Validated UpdateTask updateTask) {
        String orderNo = updateTask.getOrderNo();
        if (StringUtils.isEmpty(orderNo)) {
            throw new TaskException("订单号不能为空");
        }
        if (updateTask.extMark == null) {
            updateTask.extMark = "";
        }
        return jobTaskHandler.updateTask(updateTask);
    }


    @Data
    public static class UpdateTask {
        private String orderNo;
        private String extMark;
        private Integer failTime;
        private Integer status;
        private Long nextExecuteTime;
        private String requestParam;
        private String requestParamClass;
        private Integer deleted;
    }


    @GetMapping("/batch/task/execute")
    public Boolean executeTask(@RequestParam("orderNo") String orderNo,
                               @RequestParam("extMark") String extMark) {
        if (StringUtils.isEmpty(orderNo)) {
            throw new TaskException("订单号为空");
        }
        if (extMark == null) {
            extMark = "";
        }
        return jobTaskHandler.executeTask(orderNo, extMark);
    }
}
