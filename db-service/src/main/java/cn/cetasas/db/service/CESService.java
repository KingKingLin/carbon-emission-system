package cn.cetasas.db.service;

import cn.cetasas.db.exception.BusinessException;
import cn.cetasas.db.exception.BusinessExceptionCode;
import cn.cetasas.db.mapper.CESMapper;
import cn.cetasas.db.pojo.CES;
import cn.cetasas.db.pojo.EES;
import cn.cetasas.db.util.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 负责碳排放有关请求
 */
@Service
public class CESService {

    private final static Logger LOG = LoggerFactory.getLogger(CESService.class);

    private final ReentrantLock lock = new ReentrantLock();

    private final static List<String> title = new ArrayList<>();

    private final static String downloadPath = "/ces";

    private final static String downloadSuffix = "年碳排放数据";

    static {
        title.add("省份");
        title.add("碳排放量(万吨CO₂)");
    }

    @Resource
    private CESMapper cesMapper;

    @Resource
    private TableService tableService;

    @Resource
    private EESService eesService;

    @Resource
    private InsertBatch insertBatch;

    /**
     * 获取该年份所有碳排放数据
     * @param year 年份
     * @return 该年份所有碳排放数据
     */
//    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    @Transactional
    public List<CES> getAllProvince(String year) {
        String tableName = year + TableSuffix.CES;
        List<CES> ces = null;

//        eesService.initOneYear(year); // 如果存在该表就会直接返回

        lock.lock();
        try {
            if (!tableService.isExitCESTable(year)) {
                // 如果不存在该年份的碳排放表则创建
                LOG.info("表【{}】不存在库中，需要创建并获取数据...", tableName);
                tableService.createCESTable(year);
                LOG.info("表【{}】创建成功！", tableName);

//                throw new RuntimeException("哈哈哈，出现异常了！！");
//                int i = 1/ 0;

                // 从库中获取所有该年份的能源排放数据
                List<EES> datas = eesService.getAllProvince(year);
                LOG.info("查询到该年份的能源排放数据：{}", datas);
                LOG.info("正在计算{}年的碳排放数据", year);
                ces = calc(datas);
                LOG.info("计算结果如下：{}", ces);
                // 异步处理
                // 可能存在bug：如果同时大量请求 2019_ces 并且 2019_ces 还未初始化。
                // 此刻虽然使用 ReentrantLock 锁住了，但是 insertBatch_CES 是异步操作！
                // 所以第一个请求返回时，2019_ces 可能还未初始化完毕，此刻其他的请求得到 cpu 的响应时，就得不到完整的数据。
                // 而具体会不会出现如上问题，得看 mysql 的具体的配置。
                // 如：Read committed 读已提交则可以解决该问题！（在本业务中）
                insertBatch.insertBatch_CES(ces, year);
            } else {
                // 如果存在则直接从库中取出
                LOG.info("正在从库【{}】中查询...", tableName);
                ces = cesMapper.getAllProvince(tableName);
                LOG.info("查询到如下数据：{}", ces);
                LOG.info("共计：{}条", ces.size());
            }
        } finally {
            lock.unlock(); // 解锁
        }

        return ces;
    }

    private List<CES> calc(List<EES> datas) {
        Map<String, CES> map = new HashMap<>();

        for (EES data : datas) {
            CES ces = null;
            String code = data.getRCode();
            if (map.containsKey(code)) {
                ces = map.get(code);
            } else {
                ces = new CES();
                ces.setRCode(code);
                ces.setReg(data.getReg());

                map.put(code, ces);
            }

            double value = ces.getValue();
            value += data.getValue() * data.getCef();

            // 保留两位小数
//            value = NumFormat.twoDecimal(value);
            ces.setValue(value);
        }

        // 构建返回值
        return new ArrayList<>(map.values());
    }

    public List<String> getList() {
        List<String> list = new ArrayList<>();
        String suffix = TableSuffix.tableMap.get(TableSuffix.CES);
        int min = DateRange.min, max = DateRange.max;
        for (int i = max; i >= min; i--) {
            list.add(i + "年" + suffix);
        }
        return list;
    }

    /**
     * ${year}_ces 表的数据 => 装换成 excel
     * @param year 年份 => "2019", "2018"
     * @return
     */
    public String export(String year) {
        // 判断该年份的碳排放数据 excel 表是否存在，如果存在则直接返回
        if (!ExportExcel.exit(downloadPath, year + downloadSuffix)) {
            List<CES> list = getAllProvince(year); // 获取该年份的碳排放数据

//            final List<CESResp> list1 = CopyUtil.copyList(list, CESResp.class);

            return ExportExcel.exportPlus(downloadPath, year + downloadSuffix, list.size(),
                    (row)-> {
                        for (int i = 0; i < title.size(); i++) {
                            row.createCell(i).setCellValue(title.get(i));
                        }
                    },
                    (row, i)->{
                        CES resp = list.get(i);
                        row.createCell(0).setCellValue(resp.getReg());
                        row.createCell(1).setCellValue(resp.getValue());
                    });
        } else return ExportExcel.getFullName(year + downloadSuffix);
    }

//    public void download(String fileName, HttpServletResponse request) {
//        String absolutePath = ExportExcel.getBasePath() + downloadPath + "/" + fileName;
//        try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(absolutePath));
//            BufferedOutputStream out = new BufferedOutputStream(request.getOutputStream())) {
//            int bytes = IOUtils.copy(in, out);
//            out.flush();
//            LOG.info("File Written with {} bytes", bytes);
//        } catch (IOException e) {
//            throw new BusinessException(BusinessExceptionCode.DOWNLOAD_FILE_FAILED);
//        }
//    }

    /**
     * 但是默认的请求，浏览器只返回默认的响应头：
     *      Cache-Control
     *      Content-Language
     *      Content-Type
     *      Expires
     *      Last-Modified
     *      Pragma
     * response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
     * @param fileName
     * @param response
     */
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
