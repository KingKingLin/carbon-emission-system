package cn.cetasas.db.web;

import cn.cetasas.db.pojo.RCode;
import cn.cetasas.db.resp.CommonResp;
import cn.cetasas.db.service.RCodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 返回中国的省份代码
 */
@RestController
@RequestMapping("/db/reg")
public class RCodeController {

    @Resource
    private RCodeService rCodeService;

    @GetMapping
    public CommonResp<List<RCode>> getList() {
        CommonResp<List<RCode>> resp = new CommonResp<>();
        List<RCode> content = rCodeService.getList();
        resp.setContent(content);
        return resp;
    }
}
