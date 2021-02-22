package so.dian.scm.batch.sdk.serializer;

import lombok.Data;
import so.dian.scm.batch.sdk.common.exception.TaskException;

/**
 * @author XR
 * Created  on 2021/1/22.
 */
public class KryoSerializerUtil {

    private static KryoSerializer kryoSerializer = new KryoSerializer();


    public static <T> String serializer(T obj) {
        SerializerContainer serializerContainer = new SerializerContainer();
        serializerContainer.setParam(obj);
        try {
            return new String(kryoSerializer.serialize(serializerContainer), "ISO-8859-1");
        } catch (Exception e) {
            throw new TaskException(e);
        }
    }

    public static Object deserialize(String input) {
        try {
            return kryoSerializer.deserialize(input.getBytes("ISO-8859-1"), SerializerContainer.class).param;
        } catch (Exception e) {
            throw new TaskException(e);
        }
    }


    @Data
    public static class SerializerContainer<T> {
        private T param;

    }


}
