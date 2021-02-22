package so.dian.export.sdk.core.enums;

import java.lang.annotation.*;

/**
 * @author XR
 * Created  on 2020/12/15.
 */
@Documented
@Inherited
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExportFiled {

    /**
     * 字段对应表头名称
     *
     * @return 名称
     */
    String headName();


}
