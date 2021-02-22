package so.dian.export.sdk.core.handler.convert;

import org.springframework.util.CollectionUtils;
import so.dian.export.sdk.core.enums.ExportFiled;
import so.dian.export.sdk.core.model.ExportException;
import so.dian.export.sdk.core.utils.ReflexUtils;
import so.dian.export.sdk.core.utils.StringBuilderExt;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author XR
 * Created  on 2020/12/15.
 */

public class CsvConvert<T> implements Convert<T> {

    private StringBuilder headLine;

    private final String SPLIT = ",";

    private final String OTHER_LINE = "\n";

    private StringBuilder cacheContent = new StringBuilder();

    private List<Field> fields;

    @Override
    public void init(Class<T> cla) {
        fields = ReflexUtils.getFiledByAnnoation(cla, ExportFiled.class);
        if (CollectionUtils.isEmpty(fields)) {
            throw new ExportException("没有可以导出的属性class:" + cla.getName());
        }

        StringBuilderExt strBuilder = new StringBuilderExt(SPLIT);
        fields.forEach(field -> {
            ExportFiled annotation = field.getAnnotation(ExportFiled.class);
            strBuilder.append(annotation.headName());
        });
        headLine = strBuilder.appendEnd(OTHER_LINE).getStringBuilder();
        cacheContent.append(headLine);

    }


    @Override
    public byte[] convert(List<T> list) {
        return headLine.append(convertSb(list)).toString().getBytes();
    }

    private StringBuilder convertSb(List<T> list) {
        StringBuilderExt builder = new StringBuilderExt(SPLIT);
        list.forEach(obj -> {
            fields.forEach(field -> builder.append(ReflexUtils.getValue(field, obj)));
            builder.appendEnd(OTHER_LINE);
        });
        return builder.getStringBuilder();
    }


    @Override
    public boolean cacheConvert(List<T> list) {
        StringBuilder builder = convertSb(list);
        cacheContent.append(builder);
        return true;
    }


    @Override
    public byte[] getCacheBytes() {
        byte[] bytes = cacheContent.toString().getBytes();
        cacheContent = new StringBuilder(headLine);
        return bytes;
    }

}
