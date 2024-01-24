package cn.cetasas.db.service;

import cn.cetasas.db.exception.BusinessException;
import cn.cetasas.db.exception.BusinessExceptionCode;
import cn.cetasas.db.mapper.TotalMapper;
import cn.cetasas.db.pojo.*;
import cn.cetasas.db.util.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 负责总能源排放相关请求
 */
@Service
public class TotalService {

    private final static Logger LOG = LoggerFactory.getLogger(TotalService.class);

    // 锁
    private final ReentrantLock lock = new ReentrantLock();

    private final static List<String> title = new ArrayList<>();

    private final static String downloadPath = "/total";

    private final static String downloadSuffix = "年年度总能源排放数据";

    static {
        title.add("指标");
        title.add("排放量(万吨/亿立方米/亿千瓦小时)");
    }

    @Resource
    private TableService tableService;

    @Resource
    private CarbonEmissionStatistics carbonEmissionStatistics;

    @Resource
    private InsertBatch insertBatch;

    @Resource
    private TotalMapper totalMapper;

    /**
     * 初始化一段时间的年度总能源排放数据
     * @param year 时间 => "2013-", "LAST5", "2019", "2013-2019" 等等
     */
    @Deprecated
    public void init(String year) {
        CESData<ZB> data = initCESData(year);

        // 3) 创建能源排放表，以及导入数据
        for (String sj : data.keySet()) {
            if (tableService.isExitTotalTable(year)) {
                LOG.info("表【{}{}】已经存在库中，不需要初始化！", sj, TableSuffix.TOTAL);
            } else {
                createTotal(sj, data);
            }
        }
    }
    
    /**
     * 初始某一年份的数据
     * @param year 时间 => "2019", "2018"
     */
    public void initOneYear(String year) {
        if (tableService.isExitTotalTable(year)) {
            LOG.info("表【{}{}】已经存在库中，不需要初始化！", year, TableSuffix.TOTAL);
            return;
        }
        CESData<ZB> data = initCESData(year);

        // 3) 创建能源排放表，以及导入数据
        createTotal(year, data);
    }

    private CESData<ZB> initCESData(String year) {
        LOG.info("表【{}{}}】并不存在库中，需要初始化！", year, TableSuffix.TOTAL);
        // 获取所有全球能源排放数据
        StandardData<ZB> standardData = carbonEmissionStatistics.getTotalCarbonEmissionStatistics(year);
        HashMap<String, ECode> ZB = standardData.getZb();
        CESData<ZB> data = standardData.getData();

        LOG.info("全球能源排放数据：{}", data);
        LOG.info("全球能源排放的指标代码：{}", ZB);

        lock.lock(); // 加锁，保证 isInit 和 update 的原子性
        try {
            // 1) 初始化能源代码
            if (!tableService.isInitECodeTotal()) {
                LOG.info("e_code 表还未初始化！！！");
                LOG.info("正在初始化e_code表，导入{}条数据", ZB.values());
                insertBatch.insertBatch_ECode(ZB.values());
                tableService.updateInitECodeTotal(); // 置为已经初始化
            }
        } finally {
            lock.unlock();
        }
        return data;
    }

    private void createTotal(String year, CESData<ZB> data) {
        // 2) 创建省份能源排放表，并导入数据
        for (String sj : data.keySet()) {
            // 前面已经判断过了，该年份的 total 表并不存在库中，所以可以直接创建
            String tableName = sj + TableSuffix.TOTAL;
            LOG.info("正在创建表【{}】...", tableName);
            tableService.createTotalTable(sj);
            LOG.info("创建表【{}】完成!!!", tableName);

            List<Total> totalList = new ArrayList<>();
            SJ<ZB> zb = data.get(sj);
            for (String zb_code : zb.keySet()) {
                Total total = new Total();
                total.setECode(zb_code);
                total.setValue(zb.get(zb_code).getValue());

                totalList.add(total);
            }
            LOG.info("正在导入{}条数据", totalList.size());
            insertBatch.insertBatch_Total(totalList, sj);
        }
    }

//    public void init(String year) {
//        if (tableService.isExitTotalTable(year)) {
//            return;
//        }
//        LOG.info("表【{}{}}】并不存在库中，需要初始化！", year, TableSuffix.TOTAL);
//        // 获取所有全球能源排放数据
//        StandardData<ZB> standardData = carbonEmissionStatistics.getTotalCarbonEmissionStatistics(year);
//        HashMap<String, ECode> ZB = standardData.getZb();
//        CESData<ZB> data = standardData.getData();
//
//        LOG.info("全球能源排放数据：{}", data);
//        LOG.info("全球能源排放的指标代码：{}", ZB);
//
//        lock.lock();
//        try {
//            // 1) 初始化能源代码
//            if (!tableService.isInitECodeTotal()) {
//                LOG.info("e_code 表还未初始化！！！");
//                LOG.info("正在初始化e_code表，导入{}条数据", ZB.values());
//                insertBatch.insertBatch_ECode(ZB.values());
//                tableService.updateInitECodeTotal(); // 置为已经初始化
//            }
//            // 2) 创建省份能源排放表，并导入数据
//            for (String sj : data.keySet()) {
//                // 前面已经判断过了，该年份的 total 表并不存在库中，所以可以直接创建
//                String tableName = sj + TableSuffix.TOTAL;
//                LOG.info("正在创建表【{}】...", tableName);
//                tableService.createTotalTable(sj);
//                LOG.info("创建表【{}】完成!!!", tableName);
//
//                List<Total> totalList = new ArrayList<>();
//                SJ<ZB> zb = data.get(sj);
//                for (String zb_code : zb.keySet()) {
//                    Total total = new Total();
//                    total.setECode(zb_code);
//                    total.setValue(zb.get(zb_code).getValue());
//
//                    totalList.add(total);
//                }
//                LOG.info("正在导入{}条数据", totalList.size());
//                insertBatch.insertBatch_Total(totalList, sj);
//            }
//        } finally {
//            lock.unlock();
//        }
//    }

    public List<String> getList() {
        List<String> list = new ArrayList<>();
        String suffix = TableSuffix.tableMap.get(TableSuffix.TOTAL);
        int min = DateRange.min, max = DateRange.max;
        for (int i = max; i >= min; i--) {
            list.add(i + "年" + suffix);
        }
        return list;
    }

    /**
     * ${year}_total 表的数据 => 装换成 excel
     * @param year 年份 => "2019", "2018"
     * @return
     */
    public String export(String year) {
        // 判断该年份的碳排放数据 excel 表是否存在，如果存在则直接返回
        if (!ExportExcel.exit(downloadPath, year + downloadSuffix)) {
            List<Total> list = getDataByYear(year); // 获取该年份的碳排放数据

            return ExportExcel.exportPlus(downloadPath, year + downloadSuffix, list.size(),
                    (row)-> {
                        for (int i = 0; i < title.size(); i++) {
                            row.createCell(i).setCellValue(title.get(i));
                        }
                    },
                    (row, i)->{
                        Total resp = list.get(i);
                        row.createCell(0).setCellValue(resp.getZb());
                        row.createCell(1).setCellValue(resp.getValue());
                    });
        } else return ExportExcel.getFullName(year + downloadSuffix);
    }

    /**
     * 懒加载
     * @param year
     * @return
     */
    public List<Total> getDataByYear(String year) {
        initOneYear(year);
        String tableName = year + TableSuffix.TOTAL;
        LOG.info("正在查询表【{}】...", tableName);
        return totalMapper.getDataByYear(tableName);
    }

    public void download(String fileName, HttpServletResponse response) {
        String absolutePath = ExportExcel.getBasePath() + downloadPath + "/" + fileName;
        InputStream in = null;
        OutputStream out = null;
        try {
            // 1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
            response.setContentType("multipart/form-data");
            // 2.设置文件头：最后一个参数是设置下载文件名
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            // 3.将 Content-Disposition 放出让前端可以访问
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            // 4.传输数据
            in = new FileInputStream(absolutePath);
            out = response.getOutputStream();
            int bytes = IOUtils.copy(in, out);
            LOG.info("File Written with {} bytes", bytes);
            out.flush();
        } catch (IOException e) {
            throw new BusinessException(BusinessExceptionCode.DOWNLOAD_FILE_FAILED);
//            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
