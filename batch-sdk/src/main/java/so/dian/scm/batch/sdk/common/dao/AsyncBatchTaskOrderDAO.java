package so.dian.scm.batch.sdk.common.dao;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * asyn_batch_task_order
 *
 * @author
 */
@Data
public class AsyncBatchTaskOrderDAO implements Serializable {
    /**
     * 主键
     */
    private int id;

    /**
     * 单号
     */
    private String orderNo;


    /**
     * 额外标记与orderNo组成唯一索引
     */
    private String extMark;

    /**
     * 执行类名称
     */
    private String springHandlerClassName;

    /**
     * 参数值
     */
    private String requestParam;

    /**
     * 参数类名
     */
    private String originalRequestParam;

    /**
     * 重试次数
     */
    private Integer maxRetryTimes;

    /**
     * 失败次数
     */
    private Integer failTimes;

    /**
     * 上次任务和下次执行之间间隔最短时间(分钟)
     */
    private Integer lowestIntervalTime;

    /**
     * 下次执行时间
     */
    private Date nextExecuteTime;

    /**
     * 任务状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer deleted;

    /**
     * 创建时间戳
     */
    private Long gmtCreate;

    /**
     * 更新时间戳
     */
    private Long gmtUpdate;


    private static final long serialVersionUID = 1L;

}