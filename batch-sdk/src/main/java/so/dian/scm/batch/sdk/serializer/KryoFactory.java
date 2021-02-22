package so.dian.scm.batch.sdk.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.esotericsoftware.kryo.serializers.BeanSerializer;
import org.objenesis.strategy.StdInstantiatorStrategy;

/**
 * @author XR
 * Created  on 2020/11/23.
 */
public class KryoFactory {

    private static volatile KryoFactory poolFactory = null;

    private com.esotericsoftware.kryo.pool.KryoFactory factory = () -> {
        Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.setDefaultSerializer(BeanSerializer.class);
        // 注册bean
        kryo.register(KryoSerializerUtil.SerializerContainer.class);
        Kryo.DefaultInstantiatorStrategy strategy = (Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy();
        strategy.setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
        return kryo;
    };

    private KryoPool pool = new KryoPool.Builder(factory).build();

    private KryoFactory() {
    }

    public static KryoPool getKryoPoolInstance() {
        if (poolFactory == null) {
            synchronized (KryoFactory.class) {
                if (poolFactory == null) {
                    poolFactory = new KryoFactory();
                }
            }
        }
        return poolFactory.getPool();
    }

    public KryoPool getPool() {
        return pool;
    }
}
