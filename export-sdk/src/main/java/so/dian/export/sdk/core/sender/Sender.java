package so.dian.export.sdk.core.sender;

import so.dian.export.sdk.core.model.SimpleMessage;

/**
 * @author XR
 * Created  on 2020/12/15.
 */
public interface Sender {

    boolean sendMessage(SimpleMessage simpleMessage);

}
