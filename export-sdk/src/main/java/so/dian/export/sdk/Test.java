package so.dian.export.sdk;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import so.dian.export.sdk.api.ConfigUtils;
import so.dian.export.sdk.api.ExportUtils;
import so.dian.export.sdk.core.enums.ExportFiled;
import so.dian.export.sdk.core.model.ExportResult;
import so.dian.export.sdk.core.model.SimpleMessage;
import so.dian.export.sdk.core.sender.Sender;
import so.dian.export.sdk.core.upload.FileUpload;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author XR
 * Created  on 2020/12/16.
 */
public class Test {

    public static void main(String[] args) throws InterruptedException {
        ConfigUtils.initOssClient(new FakeFileUpload());
        Test test = new Test();
        Long time = System.currentTimeMillis();
        System.out.println(test.testUpload());
        System.out.println("耗时:" + (System.currentTimeMillis() - time));

//        ThreadPoolTaskExecutor executor = getExecutor();
//        executor.initialize();
//        ConfigUtils.initExecutor(executor);
//        ConfigUtils.initSender(new StrEchoSender());
//        Integer num = 0;
//        List<String> addresses = new ArrayList<>();
//        addresses.add("1");
//        ExportFunction<Integer, Student> exportFunction = (param) -> test.getStudent1(param);
//        ExportUtils.asyncSendExport("test.xlsx", "数据导出", addresses, num, exportFunction);
//        TimeUnit.SECONDS.sleep(10);
//        executor.destroy();
    }

    public String testUpload() {
        List<Student> list = getStudent(1);
        return ExportUtils.export("工厂盘点.xlsx", () -> list);
    }


    public void testEmailUpload(String email) {
        List<String> addresses = new ArrayList<>();
        addresses.add(email);
        ExportUtils.asyncSendExport("test.csv", "数据导出", addresses, 1, (param) -> getStudent1(param));
    }


    public static ThreadPoolTaskExecutor getExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(0);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        return executor;
    }

    public ExportResult getStudent1(int i) {
        ExportResult exportResult = new ExportResult();
        if (i == 2) {
            exportResult.setData(getStudent(i));
            return exportResult;
        }
        exportResult.setHasMore(false);
        exportResult.setData(getStudent(i));
        exportResult.setNextRequest(++i);
        return exportResult;
    }

    public List<Student> getStudent(int j) {
        List<Student> list = new ArrayList<>();
        for (int i = 0; i < 18000; i++) {
            Student student = new Student("zhangsan", i, "other");
            student.setParent(100);
            list.add(student);
        }
        return list;
    }


    public static class StrEchoSender implements Sender {

        @Override
        public boolean sendMessage(SimpleMessage simpleMessage) {
            System.out.println(simpleMessage.getContent());
            return true;
        }
    }

    public static class FakeFileUpload implements FileUpload {

        @Override
        public String upload(byte[] bytes, String fileName, String fileType) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream("/Users/mac/Downloads/11.xlsx");
                fileOutputStream.write(bytes);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("文件内容：" + new String(bytes));
            return fileName.concat(".").concat(fileType);
        }
    }


    @AllArgsConstructor
    @Data
    public static class Student extends Parent {

        @ExportFiled(headName = "名字")
        private String name;

        @ExportFiled(headName = "性别")
        private Integer sex;

        @ExportFiled(headName = "其他")
        private String other;
    }

    @Data
    public static class Parent {

        @ExportFiled(headName = "父类")
        private Integer parent;
    }
}
