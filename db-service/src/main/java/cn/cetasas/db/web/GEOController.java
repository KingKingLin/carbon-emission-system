package cn.cetasas.db.web;

import cn.cetasas.db.exception.BusinessException;
import cn.cetasas.db.exception.BusinessExceptionCode;
import cn.cetasas.db.resp.CommonResp;
import cn.cetasas.db.service.GEOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 负责地图信息有关请求
 */
@RestController
@RequestMapping("/db/geo")
public class GEOController {

    private final static Logger LOG = LoggerFactory.getLogger(GEOController.class);

    @Resource
    private GEOService geoService;

    @GetMapping
    public CommonResp<String> getGeo(@RequestParam(value = "nation", required = true) String nation) {
        if (ObjectUtils.isEmpty(nation)) {
            throw new BusinessException(BusinessExceptionCode.NULL_POINTER);
        }
        nation = nation.toLowerCase(); // 小写
        CommonResp<String> resp = new CommonResp<>();
        LOG.info("正在获取{}的地图信息...", nation);
        String content = geoService.getGeo(nation);
        LOG.info("处理结果：{}", content);
        resp.setContent(content);
        return resp;
    }
}
