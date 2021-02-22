package so.dian.export.sdk.core.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import so.dian.export.sdk.api.model.AsyncExportReq;
import so.dian.export.sdk.api.model.ExportLoopReq;
import so.dian.export.sdk.api.model.ExportReq;
import so.dian.export.sdk.core.ExportFunction;
import so.dian.export.sdk.core.handler.convert.Convert;
import so.dian.export.sdk.core.handler.convert.CsvConvert;
import so.dian.export.sdk.core.handler.convert.XlsConvert;
import so.dian.export.sdk.core.handler.convert.XlsxConvert;
import so.dian.export.sdk.core.model.ExportException;
import so.dian.export.sdk.core.model.ExportResult;
import so.dian.export.sdk.core.model.SimpleMessage;
import so.dian.export.sdk.core.sender.Sender;
import so.dian.export.sdk.core.upload.FileUpload;
import so.dian.export.sdk.core.utils.StringBuilderExt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author XR
 * Created  on 2020/12/15.
 */
@Slf4j
public class DefaultFileHandler implements FileHandler {


    @Override
    public <T> String syncHandle(ExportReq uploadReq, Supplier<List<T>> supplier) {
        String fileType = uploadReq.getFileType();
        FileUpload fileUpload = uploadReq.getFileUpload();
        ConvertConfig convertConfig = getConvert(fileType);
        Convert convert = convertConfig.getInstance();
        List<T> list = supplier.get();
        if (CollectionUtils.isEmpty(list)) {
            throw new ExportException("没有数据可以导出");
        }
        convert.init(list.get(0).getClass());
        return fileUpload.upload(convert.convert(list), uploadReq.getFileName(), convertConfig.fileType);
    }



    @Override
    public <T, R> String syncLoopHandle(ExportLoopReq<T, R> exportLoopReq) {
        String fileType = exportLoopReq.getFileType();
        ConvertConfig convertConfig = getConvert(fileType);
        Convert convert = convertConfig.getInstance();
        ExportFunction<T, R> exportFunction = exportLoopReq.getExportFunction();
        boolean notInit = true;
        while (true) {
            ExportResult<T, R> result = exportFunction.apply(exportLoopReq.getRequest());
            List<R> data = result.getData();
            // 返回结果都是空
            if (CollectionUtils.isEmpty(data)) {
                break;
            }
            if (notInit) {
                convert.init(data.get(0).getClass());
                notInit = false;
            }
            convert.cacheConvert(data);
            if (!result.isHasMore() || result.getNextRequest() == null) {
                break;
            }
            exportLoopReq.setRequest(result.getNextRequest());
        }
        String fileName = exportLoopReq.getFileName();
        FileUpload fileUpload = exportLoopReq.getFileUpload();
        return fileUpload.upload(convert.getCacheBytes(), fileName, convertConfig.fileType);
    }


    @Override
    public <T, R> void asyncHandle(AsyncExportReq<T, R> asyncExportReq) {
        String fileType = asyncExportReq.getFileType();
        FileUpload fileUpload = asyncExportReq.getFileUpload();
        ConvertConfig convertConfig = getConvert(fileType);
        Convert convert = convertConfig.getInstance();
        ExportFunction<T, R> exportFunction = asyncExportReq.getExportFunction();
        Integer lineNum = 0;
        Integer suffix = 0;
        boolean notInit = true;
        List<String> paths = new ArrayList<>();
        while (true) {
            ExportResult<T, R> result = exportFunction.apply(asyncExportReq.getRequest());
            List<R> data = result.getData();
            // 返回结果都是空
            if (CollectionUtils.isEmpty(data)) {
                break;
            }
            if (notInit) {
                convert.init(data.get(0).getClass());
                notInit = false;
            }
            lineNum += data.size();
            // 判断cache里面最大存放行数 大于行数要先上传 在继续写值
            if (lineNum > convertConfig.oneMaxCount) {
                String fileName = asyncExportReq.getFileName().concat("_").concat(String.valueOf(++suffix));
                paths.add(fileUpload.upload(convert.getCacheBytes(), fileName, fileType));
                lineNum = data.size();
            }
            convert.cacheConvert(data);
            if (!result.isHasMore() || result.getNextRequest() == null) {
                break;
            }
            asyncExportReq.setRequest(result.getNextRequest());
        }
        // lineNum不等于0 说明cache里面缓存还是有值的 需要上传
        if (lineNum != 0) {
            String fileName = asyncExportReq.getFileName().concat("_").concat(String.valueOf(++suffix));
            paths.add(fileUpload.upload(convert.getCacheBytes(), fileName, fileType));
        }
        // 发送
        send(asyncExportReq, paths);
    }

    private <R, T> void send(AsyncExportReq<R, T> asyncUploadReq, List<String> paths) {
        Sender sender = asyncUploadReq.getSender();
        SimpleMessage simpleMessage = new SimpleMessage();
        String[] addressArr = new String[asyncUploadReq.getAddress().size()];
        simpleMessage.setAddresses(asyncUploadReq.getAddress().toArray(addressArr));
        simpleMessage.setTitle(asyncUploadReq.getTitle());
        StringBuilderExt builder = new StringBuilderExt("\n");
        builder.append("文件地址:");
        paths.forEach(builder::append);
        simpleMessage.setContent(builder.toString());
        sender.sendMessage(simpleMessage);
    }

    private ConvertConfig getConvert(String fileType) {
        ConvertConfig convertConfig = convertConfigMap.get(fileType);
        if (convertConfig == null) {
            throw new ExportException("该文件格式暂不支持:" + fileType);
        }
        return convertConfig;
    }

    private static Map<String, ConvertConfig> convertConfigMap = new HashMap<>();

    static {
        convertConfigMap.put("csv", new ConvertConfig(CsvConvert.class, 20000, "csv"));
        convertConfigMap.put("xls", new ConvertConfig(XlsConvert.class, 20000, "xls"));
        convertConfigMap.put("xlsx", new ConvertConfig(XlsxConvert.class, 20000, "xlsx"));
    }


    @AllArgsConstructor
    @Getter
    static class ConvertConfig {
        private Class<? extends Convert> aClass;
        private Integer oneMaxCount;
        private String fileType;

        Convert getInstance() {
            try {
                return aClass.newInstance();
            } catch (Exception e) {
                throw new ExportException(e);
            }
        }
    }


}
