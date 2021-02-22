package so.dian.export.sdk.core.handler.convert;

import java.util.List;

/**
 * @author XR
 * Created  on 2020/12/15.
 */
public interface Convert<T> {

    void init(Class<T> cla);

    byte[] convert(List<T> list);

    boolean cacheConvert(List<T> list);

    byte[] getCacheBytes();
}
