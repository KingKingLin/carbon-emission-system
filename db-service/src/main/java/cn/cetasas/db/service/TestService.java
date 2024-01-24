//package cn.cetasas.db.service;
//
//import cn.cetasas.db.mapper.CESMapper;
//import cn.cetasas.db.mapper.TestMapper;
//import cn.cetasas.db.pojo.*;
//import cn.cetasas.db.util.CarbonEmissionStatistics;
//import cn.cetasas.db.util.InsertBatch;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class TestService {
//
//    private final static Logger LOG = LoggerFactory.getLogger(TestService.class);
//
//    @Resource
//    private TestMapper testMapper;
//
//    public List<Test> getAll() {
//        return testMapper.selectByExample(null);
//    }
//
//    @Resource
//    private TableService tableService;
//
//    public boolean isExitCESTable(String tableName) {
//        return tableService.isExitCESTable(tableName);
//    }
//
//    public void createCESTable(String year) {
//        tableService.createCESTable(year);
//    }
//
//
//    @Resource
//    private CarbonEmissionStatistics carbonEmissionStatistics;
//
//    @Resource
//    private InsertBatch insertBatch;
//
//    /**
//     * 数据初始化
//     */
//    public void init() {
////        initTotal();
////        initEES();
//    }
//
//    private void initTotal() {
//        // 获取所有全球能源排放数据
//        StandardData<ZB> standardData = carbonEmissionStatistics.getTotalCarbonEmissionStatistics("");
//        HashMap<String, ECode> ZB = standardData.getZb();
//        CESData<ZB> data = standardData.getData();
//
//        LOG.info("全球能源排放数据：{}", data);
//        LOG.info("全球能源排放的指标代码：{}", ZB);
//
//        // 1) 初始化能源代码
//        LOG.info("正在初始化e_code表，导入{}条数据", ZB.values());
//        insertBatch.insertBatch_EnergyCode(ZB.values());
//
//        // 2) 创建省份能源排放表，并导入数据
//        for (String sj : data.keySet()) {
//            tableService.createTotalTable(sj);
//            List<Total> totalList = new ArrayList<>();
//            SJ<ZB> zb = data.get(sj);
//            for (String zb_code : zb.keySet()) {
//                Total total = new Total();
//                total.setECode(zb_code);
//                total.setValue(zb.get(zb_code).getValue());
//
//                totalList.add(total);
//            }
//            LOG.info("正在导入{}条数据", totalList.size());
//            insertBatch.insertBatch_Total(totalList, sj);
//        }
//    }
//
//    private void initEES() {
//        // 获取所有省份碳排放数据
//        StandardData<Reg> standardData = carbonEmissionStatistics.getAllProvinceCES("");
//        HashMap<String, ECode> ZB = standardData.getZb();
//        CESData<Reg> data = standardData.getData();
//        // 获取地区代码映射表
//        Map<String, RCode> REG = carbonEmissionStatistics.getRegCode();
//
//        LOG.info("所有省份的碳排放数据：{}", data);
//        LOG.info("所有省份的对应代码：{}", REG);
//        LOG.info("所有省份能源排放的指标代码：{}", ZB);
//
//        // 1) 初始化地区代码
//        LOG.info("正在初始化r_code表，导入{}条数据", REG.values().size());
//        insertBatch.insertBatch_RegCode(REG.values());
//
//        // 2) 初始化能源代码
//        LOG.info("正在初始化e_code表，导入{}条数据", ZB.values().size());
//        insertBatch.insertBatch_EnergyCode(ZB.values());
//
//        // 3) 创建省份能源排放表，以及导入数据
//        for (String sj : data.keySet()) {
//            tableService.createEESTable(sj);
//            List<EES> eesList = new ArrayList<>();
//            SJ<Reg> reg = data.get(sj);
//            for (String reg_code : reg.keySet()) {
//                Reg zb = reg.get(reg_code);
//                for (String zb_code : zb.keySet()) {
//                    EES ees = new EES();
//                    ees.setRCode(reg_code);
//                    ees.setECode(zb_code);
//                    ees.setValue(zb.get(zb_code).getValue());
//
//                    eesList.add(ees);
//                }
//            }
//            LOG.info("正在导入{}条数据", eesList.size());
//            insertBatch.insertBatch_EES(eesList, sj);
//        }
//    }
//
//    @Resource
//    private CESMapper cesMapper;
//
//    public int insertCES(String tableName, CES ces) {
//        return cesMapper.insert(tableName, ces);
//    }
//}
