//package cn.cetasas.db.web;
//
//import cn.cetasas.db.pojo.*;
//import cn.cetasas.db.service.TableService;
//import cn.cetasas.db.service.TestService;
//import cn.cetasas.db.resp.CommonResp;
//import cn.cetasas.db.util.CarbonEmissionStatistics;
//import cn.cetasas.db.util.TableSuffix;
//import org.apache.ibatis.annotations.Param;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.web.bind.annotation.*;
//
//import javax.annotation.Resource;
//import javax.websocket.server.PathParam;
//import java.util.List;
//
//@RestController
//@RequestMapping("/db/test")
//public class TestController {
//
//    private static final Logger LOG = LoggerFactory.getLogger(TestController.class);
//
//    @Resource
//    private TestService testService;
//
//    @Resource
//    private CarbonEmissionStatistics carbonEmissionStatistics;
//
//    @GetMapping("/dbs")
//    public CommonResp<List<Test>> getAll() {
//        CommonResp<List<Test>> resp = new CommonResp<>();
//        List<Test> content = testService.getAll();
//        resp.setContent(content);
//        return resp;
//    }
//
//    @GetMapping("/db/table/ces/{year}")
//    public CommonResp<Boolean> isExitCESTable(@PathVariable("year") String year) {
//        CommonResp<Boolean> resp = new CommonResp<>();
//        boolean content = testService.isExitCESTable(year);
//        if (content) {
//            resp.setMessage("表【" + year + "】存在");
//        } else {
//            resp.setMessage("表【" + year + "】不存在");
//        }
//        resp.setContent(content);
//        return resp;
//    }
//
//    @PostMapping("/db/table/ces")
//    public CommonResp<Boolean> createTable(@PathParam("year") String year) {
//        CommonResp<Boolean> resp = new CommonResp<>();
//        testService.createCESTable(year);
//        return resp;
//    }
//
//    @PostMapping("/db/pojo/ces/{year}")
//    public CommonResp<Boolean> insertCES(@PathVariable("year") String year, CES ces) {
//        CommonResp<Boolean> resp = new CommonResp<>();
//        int i = testService.insertCES(year + TableSuffix.CES, ces);
//        resp.setContent(i == 1);
//        return resp;
//    }
//
//    /**
//     *
//     * @param reg 地区代码 NOT NULL
//     * @param sj 时间 NOT NULL
//     * @return 根据地区代码和时间范围获取碳排放数据
//     */
//    @GetMapping("/data/ces")
//    public CommonResp<StandardData<Reg>> getCes(@PathParam("reg") String reg, @PathParam("sj") String sj) {
//        CommonResp<StandardData<Reg>> resp = new CommonResp<>();
//        StandardData<Reg> content = carbonEmissionStatistics.getRegCarbonEmissionStatistics(reg, sj);
//        resp.setContent(content);
//        return resp;
//    }
//
//    @GetMapping("/data/total")
//    public CommonResp<StandardData<ZB>> getTotal(@PathParam("sj") String sj) {
//        CommonResp<StandardData<ZB>> resp = new CommonResp<>();
//        StandardData<ZB> content = carbonEmissionStatistics.getTotalCarbonEmissionStatistics(sj);
//        resp.setContent(content);
//        return resp;
//    }
//
//    @GetMapping("/data/ces/init")
//    public void init(@PathParam("sj") String sj) {
//        LOG.info("初始化开始...");
//        testService.init();
//        LOG.info("初始化结束...");
//    }
//
//    @GetMapping("")
//    public String hello() {
//        return "hello, world!";
//    }
//}
