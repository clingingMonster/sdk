package so.dian.export.sdk.core.utils;

/**
 * @author XR
 * Created  on 2020/12/15.
 */
public class StringBuilderExt {

    private StringBuilder stringBuilder;
    private String split;

    public StringBuilderExt(String split) {
        this.stringBuilder = new StringBuilder();
        this.split = split;
    }

    public StringBuilderExt append(Object content) {
        stringBuilder.append(content).append(split);
        return this;
    }

    public StringBuilderExt appendEnd(Object content) {
        stringBuilder.append(content);
        return this;
    }


    public StringBuilderExt appendBefore(Object content) {
        stringBuilder.append(split).append(content);
        return this;
    }


    public StringBuilderExt appendFirst(Object content) {
        stringBuilder.append(content);
        return this;
    }


    @Override
    public String toString() {
        return stringBuilder.toString();
    }

    public StringBuilder getStringBuilder() {
        return this.stringBuilder;
    }

}
