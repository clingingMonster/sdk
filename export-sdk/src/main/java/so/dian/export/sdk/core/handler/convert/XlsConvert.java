package so.dian.export.sdk.core.handler.convert;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.util.CollectionUtils;
import so.dian.export.sdk.core.enums.ExportFiled;
import so.dian.export.sdk.core.model.ExportException;
import so.dian.export.sdk.core.utils.ReflexUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author XR
 * Created  on 2021/1/12.
 */
public class XlsConvert<T> implements Convert<T> {

    private HSSFWorkbook wb;

    private HSSFSheet sheet;

    private List<Field> fields;

    private int row;

    @Override
    public void init(Class<T> cla) {
        fields = ReflexUtils.getFiledByAnnoation(cla, ExportFiled.class);
        if (CollectionUtils.isEmpty(fields)) {
            throw new ExportException("没有可以导出的属性class:" + cla.getName());
        }
        initXls();
    }

    private void initXls() {
        wb = new HSSFWorkbook();
        sheet = wb.createSheet("数据");
        HSSFRow titleRow = sheet.createRow(0);
        row = 0;
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            ExportFiled annotation = field.getAnnotation(ExportFiled.class);
            titleRow.createCell(i).setCellValue(annotation.headName());
        }
    }


    @Override
    public byte[] convert(List<T> list) {
        setValue(list);
        return getBytes();
    }


    private void setValue(List<T> dataList) {
        for (Object aDataList : dataList) {
            HSSFRow hssfRow = sheet.createRow(++row);
            for (int j = 0; j < fields.size(); j++) {
                Field field = fields.get(j);
                hssfRow.createCell(j).setCellValue(ReflexUtils.getValue(field, aDataList));
            }
        }
    }

    private byte[] getBytes() {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        try {
            wb.write(byteOutputStream);
            byteOutputStream.flush();
            byteOutputStream.close();
        } catch (IOException e) {
            throw new ExportException("xls读取数据流失败");
        }
        return byteOutputStream.toByteArray();
    }


    @Override
    public boolean cacheConvert(List<T> list) {
        setValue(list);
        return Boolean.TRUE;
    }


    @Override
    public byte[] getCacheBytes() {
        byte[] bytes = getBytes();
        initXls();
        return bytes;
    }


}
