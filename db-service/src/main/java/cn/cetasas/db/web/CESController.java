package cn.cetasas.db.web;

import cn.cetasas.db.exception.BusinessException;
import cn.cetasas.db.exception.BusinessExceptionCode;
import cn.cetasas.db.pojo.CES;
import cn.cetasas.db.resp.CESResp;
import cn.cetasas.db.resp.CommonResp;
import cn.cetasas.db.service.CESService;
import cn.cetasas.db.util.CopyUtil;
import cn.cetasas.db.util.DateRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 负责碳排放有关请求
 */
@RestController
@RequestMapping("/db/ces")
public class CESController {

    private final static Logger LOG = LoggerFactory.getLogger(CESController.class);

    @Resource
    private CESService cesService;

    @GetMapping("/{year}")
    public CommonResp<List<CESResp>> getAllProvince(@PathVariable("year") String year) {
        CommonResp<List<CESResp>> resp = new CommonResp<>();
        if (DateRange.validate(year)) {
            throw new BusinessException(BusinessExceptionCode.OUT_OF_DATE);
        } else {
            List<CES> ces = cesService.getAllProvince(year);
            LOG.info("查询到如下数据：{}", ces);
            LOG.info("正在将其处理并返回给前端...");
            List<CESResp> content = CopyUtil.copyList(ces, CESResp.class);
            LOG.info("处理结果：{}", content);
            resp.setContent(content);
        }
        return resp;
    }

    @GetMapping("/list")
    public CommonResp<List<String>> getList() {
        LOG.info("正在获取所有可下载的碳排放数据信息...");
        CommonResp<List<String>> resp = new CommonResp<>();
        List<String> content = cesService.getList();
        LOG.info("获取的数据如下: {}", content);
        resp.setContent(content);
        return resp;
    }

//    /**
//     * 先从 db-service 获取 ${year}_ces.xls 文件是否存在，如果不存在就通过数据库里的数据导出一份 ${year}_ces.xls 文件
//     * 并给前端返回可以下载的信息
//     * 最后前端通过 /db/excel/ces/${fileName} 这个 url 完成文件下载
//     * @param year
//     * @return
//     */
//    @GetMapping("/export")
//    public CommonResp<String> export(@RequestParam(value = "year", required = true) String year) {
//        if (!DateRange.validate(year)) throw new BusinessException(BusinessExceptionCode.OUT_OF_DATE);
//        LOG.info("用户正在请求下载{}年的碳排放数据", year);
//        LOG.info("正在加载数据...");
//        CommonResp<String> resp = new CommonResp<>();
//        String fileName = cesService.export(year);
//        resp.setContent(fileName);
//        LOG.info("数据导出完毕！返回，用户可以下载");
//        resp.setMessage("数据导出完毕！返回，用户可以下载");
//        return resp;
//    }

    @GetMapping("/download")
    public void download(@RequestParam(value = "year") String year, HttpServletResponse response) {
        if (DateRange.validate(year)) throw new BusinessException(BusinessExceptionCode.OUT_OF_DATE);
        LOG.info("用户正在请求下载{}年的碳排放数据", year);
        LOG.info("正在加载数据...");
        String fileName = cesService.export(year);
        LOG.info("数据导出完毕！返回，用户可以下载");
        LOG.info("正在下载");
        cesService.download(fileName, response);
        LOG.info("下载完毕");
    }
}
