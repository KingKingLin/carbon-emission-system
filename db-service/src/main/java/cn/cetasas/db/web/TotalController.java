package cn.cetasas.db.web;

import cn.cetasas.db.exception.BusinessException;
import cn.cetasas.db.exception.BusinessExceptionCode;
import cn.cetasas.db.pojo.Total;
import cn.cetasas.db.resp.CommonResp;
import cn.cetasas.db.resp.TotalResp;
import cn.cetasas.db.service.TotalService;
import cn.cetasas.db.util.CopyUtil;
import cn.cetasas.db.util.DateRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/db/total")
public class TotalController {

    private final static Logger LOG = LoggerFactory.getLogger(TotalController.class);

    @Resource
    private TotalService totalService;

    @GetMapping("/list")
    public CommonResp<List<String>> getList() {
        LOG.info("正在获取所有可下载的总能源排放数据信息...");
        CommonResp<List<String>> resp = new CommonResp<>();
        List<String> content = totalService.getList();
        LOG.info("获取的数据如下: {}", content);
        resp.setContent(content);
        return resp;
    }

    @GetMapping("/{year}")
    public CommonResp<List<TotalResp>> getDataByYear(@PathVariable("year") String year) {
        CommonResp<List<TotalResp>> resp = new CommonResp<>();
        if (DateRange.validate(year)) {
            throw new BusinessException(BusinessExceptionCode.OUT_OF_DATE);
        } else {
            List<Total> total = totalService.getDataByYear(year);
            LOG.info("查询到如下数据：{}", total);
            LOG.info("正在将其处理并返回给前端...");
            List<TotalResp> content = CopyUtil.copyList(total, TotalResp.class);
            LOG.info("处理结果：{}", content);
            resp.setContent(content);
        }
        return resp;
    }

    @GetMapping("/download")
    public void download(@RequestParam(value = "year") String year, HttpServletResponse response) {
        if (DateRange.validate(year)) throw new BusinessException(BusinessExceptionCode.OUT_OF_DATE);
        LOG.info("用户正在请求下载{}年的年度总能源排放数据", year);
        LOG.info("正在加载数据...");
        String fileName = totalService.export(year);
        LOG.info("数据导出完毕！返回，用户可以下载");
        LOG.info("正在下载");
        totalService.download(fileName, response);
        LOG.info("下载完毕");
    }
}
