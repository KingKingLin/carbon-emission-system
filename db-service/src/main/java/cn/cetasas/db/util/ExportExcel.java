package cn.cetasas.db.util;

import cn.cetasas.db.exception.BusinessException;
import cn.cetasas.db.exception.BusinessExceptionCode;
import cn.cetasas.db.resp.CESResp;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.net.URLDecoder;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 将 Map 对象转换成 Excel 文件
 *
 * SpringBoot 默认提供了四种静态资源目录:
 * classpath:/public/
 * classpath:/static/
 * classpath:/resources/
 * classpath:/META-INF/resources/
 *
 * 我们的项目不希望用户可以直接用过网络 url 下载对应的 excel 表格，所以在本项目中会将对应的 excel 表格放入 /excel 目录下
 *
 * 实例代码：
 * ExportExcel.export(list1, title, "/ces", "2019年碳排放数据");
 * ExportExcel.exportPlus("/ces", "2019年碳排放数据", list1.size(),
 *         (row)-> {
 *             for (int i = 0; i < title.size(); i++) {
 *                 row.createCell(i).setCellValue(title.get(i));
 *             }
 *         },
 *         (row, i)->{
 *             CESResp resp = list1.get(i);
 *             row.createCell(0).setCellValue(resp.getReg());
 *             row.createCell(1).setCellValue(resp.getValue());
 *         });
 */
public class ExportExcel {

    private final static Logger LOG = LoggerFactory.getLogger(ExportExcel.class);

//    /E:/docs/college/%e6%af%95%e4%b8%9a%e8%ae%be%e8%ae%a1/%e4%bb%a3%e7%a0%81/carbon-emission-system/db-service/target/test-classes/
//    private static String basePath;
//
//    static {
//        try {
//            basePath = ResourceUtils.getURL("classpath:").getPath();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

    private static String basePath;

    static {
        try {
            basePath = URLDecoder.decode(ResourceUtils.getURL("classpath:").getPath().substring(1), "utf-8") + "excel";
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 缺点：只能导出碳排放数据表 => 不够通用
     * 改造方法：将第 2 步和第 3 步交由调用者去实现
     * @param list 要导入的数据列表
     * @param title 表头，传入形式如: {props: value}
     * @param path 需要存放的路径, 传入形式如: /ces
     * @param fileName 保存的文件名
     */
    @Deprecated
    public static void export(List<CESResp> list, List<String> title, String path, String fileName) {
        File file = createFile(path, fileName);
        try(// 1）创建一个工作簿
            Workbook wb = new HSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
            // 2）在工作簿中创建一个 sheet => 表
            Sheet sheet = wb.createSheet(fileName);
            // 3）设置表头
            Row tableTile = sheet.createRow(0);
            for (int i = 0; i < title.size(); i++) {
                Cell cell = tableTile.createCell(i);
                cell.setCellValue(title.get(i));
            }
            // 4）设置表的内容
            for (CESResp obj : list) {
                Row row = sheet.createRow(sheet.getLastRowNum() + 1);
                row.createCell(0).setCellValue(obj.getReg());
                row.createCell(1).setCellValue(obj.getValue());
            }
            // 5）导出
            wb.write(fos);
        } catch (IOException e) {
            throw new BusinessException(BusinessExceptionCode.EXPORT_FAILED);
        }
    }

    /**
     *
     * @param path 存放路径, 传入形式如: /ces
     * @param fileName 文件名
     * @param size 导入数据长度
     * @param setTitle 自定义设置表头的方法
     * @param setContent 自定义设置表的数据的方法，接受参数 行和第几行元素
     * @return 返回导出的文件名
     */
    public static String exportPlus(String path, String fileName, int size, Consumer<Row> setTitle, BiConsumer<Row, Integer> setContent) {
        File file = createFile(path, fileName);
        try(// 1）创建一个工作簿
            Workbook wb = new HSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
            // 2）在工作簿中创建一个 sheet => 表
            Sheet sheet = wb.createSheet(fileName);
            // 3）设置表头
            setTitle.accept(sheet.createRow(0));
            // 4）设置表的内容
            for (int i = 0; i < size; i++) { // i 是第 i 行
                setContent.accept(sheet.createRow(sheet.getLastRowNum() + 1), i);
            }
            // 5）导出
            wb.write(fos);
        } catch (IOException e) {
            throw new BusinessException(BusinessExceptionCode.EXPORT_FAILED);
        }
        return file.getName();
    }

    private static File createFile(String path, String fileName) {
        String pathName = basePath + path;
        File file = new File(pathName);
        if (!file.exists()) { // 如果文件夹不存在、则新建
            LOG.info("创建目录 or 文件夹：{}", pathName);
            boolean newFile = file.mkdirs();
            LOG.info("创建结果：{}", newFile);
        }
        return new File(basePath + path + "/" + fileName + ".xls");
    }

    /**
     * 判断 basePath + path + "/" 路径下的文件【fileName.xls】是否存在
     * @param path 存放路径
     * @param fileName 文件名
     * @return
     */
    public static boolean exit(String path, String fileName) {
        File file = new File(getFullName(basePath + path + "/" + fileName));
        return file.exists();
    }

    public static String getBasePath() {
        return basePath;
    }

    public static String getFullName(String s) {
        return s + ".xls";
    }
}
