package so.dian.export.sdk.core.model;

import lombok.Data;

/**
 * @author XR
 * Created  on 2020/12/16.
 */
@Data
public class SimpleMessage {

    private String title;

    private String content;

    private String[] addresses;

}
