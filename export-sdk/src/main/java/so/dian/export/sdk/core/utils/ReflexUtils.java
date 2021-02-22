package so.dian.export.sdk.core.utils;

import lombok.extern.slf4j.Slf4j;
import so.dian.export.sdk.core.model.ExportException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author XR
 * Created  on 2020/12/15.
 */
@Slf4j
public class ReflexUtils {

    public static <T extends Annotation, R> List<Field> getFiledByAnnoation(Class<R> sourceClass, Class<T> hasAnnotation) {
        if (sourceClass == Object.class) {
            return Collections.emptyList();
        }
        List<Field> fields = new ArrayList<>();
        Class superClass = sourceClass;
        merge(sourceClass, fields, hasAnnotation);
        for (int i = 0; i < 5; i++) {
            superClass = superClass.getSuperclass();
            if (superClass == Object.class) {
                break;
            }
            merge(superClass, fields, hasAnnotation);
        }
        return fields;
    }


    private static <T extends Annotation> void merge(Class cla, List<Field> fields, Class<T> annotationClass) {
        for (Field field : cla.getDeclaredFields()) {
            T annotation = field.getAnnotation(annotationClass);
            if (annotation != null) {
                fields.add(field);
                field.setAccessible(true);
            }
        }
    }

    public static String getValue(Field field, Object obj) {
        try {
            Object value = field.get(obj);
            if (value == null) {
                return "";
            } else {
                return String.valueOf(value);
            }
        } catch (IllegalAccessException e) {
            throw new ExportException(e);
        }
    }
}
