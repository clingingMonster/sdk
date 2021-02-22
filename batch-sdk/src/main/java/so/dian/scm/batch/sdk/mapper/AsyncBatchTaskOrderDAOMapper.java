package so.dian.scm.batch.sdk.mapper;

import org.apache.ibatis.annotations.Param;
import so.dian.scm.batch.sdk.common.dao.AsyncBatchTaskOrderDAO;

import java.util.Date;


public interface AsyncBatchTaskOrderDAOMapper {


    int insert(AsyncBatchTaskOrderDAO record);


    AsyncBatchTaskOrderDAO selectByPrimaryKey(Long id);


    AsyncBatchTaskOrderDAO selectByOrderNo(@Param("orderNo") String orderNo,
                                           @Param("extMark") String extMark);

    int confirmOrder(@Param("orderNo") String orderNo,
                     @Param("extMark") String extMark);

    int cancelOrder(@Param("orderNo") String orderNo,
                    @Param("extMark") String extMark);

    AsyncBatchTaskOrderDAO getExecuteTask(@Param("id") int id,
                                          @Param("startDate") Date startDate,
                                          @Param("endDate") Date endDate);

    int updateExecuteParam(@Param("orderNo") String orderNo,
                           @Param("extMark") String extMark,
                           @Param("requestParam") String requestParam,
                           @Param("originalRequestParam") String originalRequestParam,
                           @Param("nextExecuteDate") Date nextExecuteDate);

    int updateExecuteFail(@Param("orderNo") String orderNo,
                          @Param("extMark") String extMark,
                          @Param("status") int status);

    int updateOrderStatus(@Param("orderNo") String orderNo,
                          @Param("extMark") String extMark,
                          @Param("status") int status);

    int updateNextExecuteDate(@Param("orderNo") String orderNo,
                              @Param("extMark") String extMark,
                              @Param("nextExecuteDate") Date nextExecuteDate);

    int update(AsyncBatchTaskOrderDAO asyncBatchTaskOrderDAO);


}