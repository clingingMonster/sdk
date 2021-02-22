package so.dian.export.sdk.core.handler.convert;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
public class XlsxConvert<T> implements Convert<T> {

    private List<Field> fields;
    private XSSFWorkbook workBook;
    private XSSFSheet sheet;
    private int row;


    @Override
    public void init(Class<T> cla) {
        fields = ReflexUtils.getFiledByAnnoation(cla, ExportFiled.class);
        if (CollectionUtils.isEmpty(fields)) {
            throw new ExportException("没有可以导出的属性class:" + cla.getName());
        }
        initXlsx();
    }

    private void initXlsx() {
        workBook = new XSSFWorkbook();
        sheet = workBook.createSheet();
        workBook.setSheetName(0, "数据");
        XSSFRow titleRow = sheet.createRow(0);
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

    @Override
    public boolean cacheConvert(List<T> list) {
        setValue(list);
        return Boolean.TRUE;
    }

    private <T> void setValue(List<T> dataList) {
        for (T aDataList : dataList) {
            XSSFRow titleRow = sheet.createRow(++row);
            for (int j = 0; j < fields.size(); j++) {
                Field field = fields.get(j);
                String value = ReflexUtils.getValue(field, aDataList);
                titleRow.createCell(j).setCellValue(value);
            }
        }
    }


    private byte[] getBytes() {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        try {
            workBook.write(byteOutputStream);
            byteOutputStream.flush();
            byteOutputStream.close();
        } catch (IOException e) {
            throw new ExportException("xlsx读取数据流失败");
        }
        return byteOutputStream.toByteArray();
    }


    @Override
    public byte[] getCacheBytes() {
        byte[] bytes = getBytes();
        initXlsx();
        return bytes;
    }


}
