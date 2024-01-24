package cn.cetasas.db.service;

import cn.cetasas.db.exception.BusinessException;
import cn.cetasas.db.exception.BusinessExceptionCode;
import cn.cetasas.db.mapper.EESMapper;
import cn.cetasas.db.pojo.*;
import cn.cetasas.db.resp.ZBResp;
import cn.cetasas.db.util.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 负责能源排放相关请求
 */
@Service
public class EESService {

    private final static Logger LOG = LoggerFactory.getLogger(EESService.class);

    // 锁
    private final ReentrantLock lock = new ReentrantLock();

    @Resource
    private TableService tableService;

    @Resource
    private CarbonEmissionStatistics carbonEmissionStatistics;

    @Resource
    private InsertBatch insertBatch;

    @Resource
    private EESMapper eesMapper;

    private final static String downloadPath = "/ees";

    private final static String downloadSuffix = "年能源排放数据";

    /**
     * 初始化一段时间的能源排放表
     * @param year 时间 => "2013-", "LAST5", "2019", "2013-2019" 等等
     */
    @Deprecated
    public void init(String year) {
        CESData<Reg> data = initCESData(year);

        // 3) 创建能源排放表，以及导入数据
        for (String sj : data.keySet()) {
            if (tableService.isExitEESTable(year)) {
                LOG.info("表【{}{}】已经存在库中，不需要初始化！", sj, TableSuffix.EES);
            } else {
                createEES(sj, data);
            }
        }
    }

    /**
     * 初始某一年份的数据
     * @param year 时间 => "2019", "2018"
     */
    private void initOneYear(String year) {
        if (tableService.isExitEESTable(year)) {
            LOG.info("表【{}{}】已经存在库中，不需要初始化！", year, TableSuffix.EES);
            return;
        }
        CESData<Reg> data = initCESData(year);

        // 3) 创建能源排放表，以及导入数据
        createEES(year, data);
    }

    private CESData<Reg> initCESData(String year) {
        LOG.info("表【{}{}}】并不存在库中，需要初始化！", year, TableSuffix.EES);
        // 获取该年份所有省份碳排放数据
        LOG.info("正在获取{}所有省份碳排放的数据...", year);
        StandardData<Reg> standardData = carbonEmissionStatistics.getAllProvinceCES(year);
        HashMap<String, ECode> ZB = standardData.getZb();
        CESData<Reg> data = standardData.getData();
        LOG.info("所有省份的碳排放数据：{}", data);
        LOG.info("所有省份能源排放的指标代码：{}", ZB);

        lock.lock(); // 加锁，保证 isInit 和 update 的原子性
        try {
            // 1) 初始化地区代码
            if (!tableService.isInitRCode()) { // isInitRCode 返回 r_code 表是否初始化
                LOG.info("r_code 表还未初始化！！！");
                // 获取地区代码映射表
                LOG.info("正在获取该地区代码映射表...");
                Map<String, RCode> REG = carbonEmissionStatistics.getRegCode();
                LOG.info("所有省份的对应代码：{}", REG);
                LOG.info("正在初始化r_code表，导入{}条数据", REG.values().size());
                insertBatch.insertBatch_RCode(REG.values());
                tableService.updateInitRCode(); // 置为已经初始化
            }
            // 2) 初始化能源代码
            if (!tableService.isInitECodeEES()) {
                LOG.info("e_code 表还未初始化！！！");
                LOG.info("正在初始化e_code表，导入{}条数据", ZB.values().size());
                insertBatch.insertBatch_ECode(ZB.values());
                tableService.updateInitECodeEES(); // 置为已经初始化
            }
        } finally {
            lock.unlock();
        }
        return data;
    }

    private void createEES(String year, CESData<Reg> data) {
        // 前面已经判断了该年份的能源排放表是否存在库中了，所以可以直接创建
        String tableName = year + TableSuffix.EES;
        LOG.info("正在创建表【{}】...", tableName);
        tableService.createEESTable(year);
        LOG.info("创建表【{}】完成!!!", tableName);

        List<EES> eesList = new ArrayList<>();
        SJ<Reg> reg = data.get(year);
        for (String reg_code : reg.keySet()) {
            Reg zb = reg.get(reg_code);
            for (String zb_code : zb.keySet()) {
                EES ees = new EES();
                ees.setRCode(reg_code);
                ees.setECode(zb_code);
                ees.setValue(zb.get(zb_code).getValue());

                eesList.add(ees);
            }
        }
        LOG.info("正在导入{}条数据", eesList.size());
        insertBatch.insertBatch_EES(eesList, year);
    }

    /**
     * 懒加载
     * @param year
     * @return
     */
    public List<EES> getAllProvince(String year) {
        initOneYear(year);
        String tableName = year + TableSuffix.EES;
        LOG.info("正在查询表【{}】...", tableName);
        return eesMapper.getAllProvince(tableName);
    }

    public List<String> getList() {
        List<String> list = new ArrayList<>();
        String suffix = TableSuffix.tableMap.get(TableSuffix.EES);
        int min = DateRange.min, max = DateRange.max;
        for (int i = max; i >= min; i--) {
            list.add(i + "年" + suffix);
        }
        return list;
    }

    /**
     * ${year}_ees 表的数据 => 装换成 excel
     * @param year 年份 => "2019", "2018"
     * @return
     */
    public String export(String year) {
        // 判断该年份的碳排放数据 excel 表是否存在，如果存在则直接返回
        if (!ExportExcel.exit(downloadPath, year + downloadSuffix)) {
            List<EES> list = getAllProvince(year); // 获取该年份的碳排放数据

            final Map<String, Map<String, Double>> map = cast(list); // [{r_code e_code reg zb value cef}] => {reg:{zb:value}}}
            final List<String> column = new ArrayList<>(map.keySet()); // ["北京市","天津市"...]
//        final List<String> title = new ArrayList<>(map.get("北京市").keySet());
            final List<String> title = new ArrayList<>(map.get(column.get(0)).keySet());
            return ExportExcel.exportPlus(downloadPath, year + downloadSuffix, column.size(),
                    (row)-> {
                        row.createCell(0).setCellValue("省份\\能源(万吨/亿立方米/亿千瓦小时)");
                        for (int i = 0; i < title.size(); i++) {
                            row.createCell(i + 1).setCellValue(title.get(i));
                        }
                    },
                    (row, i)->{
                        String reg = column.get(i);
                        Map<String, Double> zbs = map.get(reg);
                        row.createCell(0).setCellValue(reg);
                        for (int i1 = 0; i1 < title.size(); i1++) {
                            String s = title.get(i1);
                            Double value = zbs.get(s);
                            row.createCell(i1 + 1).setCellValue(value);
                        }
                    });
        } else return ExportExcel.getFullName(year + downloadSuffix);
    }

    // 字符串开销大
    private Map<String, Map<String, Double>> cast(List<EES> list) {
        Map<String, Map<String, Double>> map = new HashMap<>();
        for (EES ees : list) {
            String reg = ees.getReg();
            String zb = ees.getZb();
            double value = ees.getValue();

            if (!map.containsKey(reg)) {
                map.put(reg, new HashMap<>());
            }
            map.get(reg).put(zb, value);
        }
        return map; // {reg:{zb:value}}
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

    public List<ZBResp> getDataByRegAndYear(String reg, String year) {
        String tableName = year + TableSuffix.EES;
        return eesMapper.getDataByRegAndYear(reg, tableName);
    }
}
