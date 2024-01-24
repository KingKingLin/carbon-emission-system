package cn.cetasas.db.web;

import cn.cetasas.db.exception.BusinessException;
import cn.cetasas.db.exception.BusinessExceptionCode;
import cn.cetasas.db.pojo.EES;
import cn.cetasas.db.resp.CommonResp;
import cn.cetasas.db.resp.EESResp;
import cn.cetasas.db.resp.ZBResp;
import cn.cetasas.db.service.EESService;
import cn.cetasas.db.util.CopyUtil;
import cn.cetasas.db.util.DateRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/db/ees")
public class EESController {

    private final static Logger LOG = LoggerFactory.getLogger(EESController.class);

    @Resource
    private EESService eesService;

//    @GetMapping("/init/{year}")
//    public CommonResp<Boolean> init(@PathVariable("year") String year) {
//        CommonResp<Boolean> resp = new CommonResp<>();
//        if (!DateRange.validate(year)) {
//            throw new BusinessException(BusinessExceptionCode.OUT_OF_DATE);
//        } else {
//            LOG.info("正在初始化{}年份的能源排放数据...", year);
//            eesService.initOneYear(year);
//            LOG.info("退出初始化...");
//            resp.setMessage("库中已经存在" + year + TableSuffix.EES + "表");
//        }
//        return resp;
//    }

    @GetMapping("/{year}")
    public CommonResp<List<EESResp>> getAllProvince(@PathVariable("year") String year) {
        CommonResp<List<EESResp>> resp = new CommonResp<>();
        if (DateRange.validate(year)) {
            throw new BusinessException(BusinessExceptionCode.OUT_OF_DATE);
        } else {
            List<EES> ees = eesService.getAllProvince(year);
            LOG.info("查询到如下数据：{}", ees);
            LOG.info("正在将其处理并返回给前端...");
            List<EESResp> content = CopyUtil.copyList(ees, EESResp.class);
            LOG.info("处理结果：{}", content);
            resp.setContent(content);
        }
        return resp;
    }

    @GetMapping
    public CommonResp<List<ZBResp>> getDataByRegAndYear(@RequestParam("reg") String reg, @RequestParam("year") String year) {
        CommonResp<List<ZBResp>> resp = new CommonResp<>();
        if (DateRange.validate(year)) {
            throw new BusinessException(BusinessExceptionCode.OUT_OF_DATE);
        } else {
            List<ZBResp> content = eesService.getDataByRegAndYear(reg, year);
            LOG.info("查询到如下数据：{}", content);
            LOG.info("正在将其处理并返回给前端...");
            LOG.info("处理结果：{}", content);
            resp.setContent(content);
        }
        return resp;
    }

    @GetMapping("/list")
    public CommonResp<List<String>> getList() {
        LOG.info("正在获取所有可下载的能源排放数据信息...");
        CommonResp<List<String>> resp = new CommonResp<>();
        List<String> content = eesService.getList();
        LOG.info("获取的数据如下: {}", content);
        resp.setContent(content);
        return resp;
    }

//    /**
//     * 先从 db-service 获取 ${year}_ees.xls 文件是否存在，如果不存在就通过数据库里的数据导出一份 ${year}_ees.xls 文件
//     * 并给前端返回可以下载的信息
//     * 最后前端通过 /db/static/ees/${fileName} 这个 url 完成文件下载
//     * @param year
//     * @return
//     */
//    @GetMapping("/export")
//    public CommonResp<String> export(@RequestParam(value = "year", required = true) String year) {
//        if (!DateRange.validate(year)) throw new BusinessException(BusinessExceptionCode.OUT_OF_DATE);
//        LOG.info("用户正在请求下载{}年的能源排放数据", year);
//        LOG.info("正在加载数据...");
//        CommonResp<String> resp = new CommonResp<>();
//        eesService.export(year);
//        LOG.info("数据导出完毕！返回，用户可以下载");
//        resp.setMessage("数据导出完毕！返回，用户可以下载");
//        return resp;
//    }

    @GetMapping("/download")
    public void download(@RequestParam(value = "year") String year, HttpServletResponse response) {
        if (DateRange.validate(year)) throw new BusinessException(BusinessExceptionCode.OUT_OF_DATE);
        LOG.info("用户正在请求下载{}年的能源排放数据", year);
        LOG.info("正在加载数据...");
        String fileName = eesService.export(year);
        LOG.info("数据导出完毕！返回，用户可以下载");
        LOG.info("正在下载");
        eesService.download(fileName, response);
        LOG.info("下载完毕");
    }
}
