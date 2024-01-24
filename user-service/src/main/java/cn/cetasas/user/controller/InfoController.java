package cn.cetasas.user.controller;

import cn.cetasas.user.pojo.Info;
import cn.cetasas.user.req.InfoUpdateRequest;
import cn.cetasas.user.resp.CommonResp;
import cn.cetasas.user.service.InfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 获取用户的 其他基本信息
 */
@RestController
@RequestMapping("/user/info")
public class InfoController {

    private final static Logger LOG = LoggerFactory.getLogger(InfoController.class);

    @Resource
    private InfoService infoService;

    @GetMapping("/{userid}")
    public CommonResp<Info> getInfo(@PathVariable("userid") Integer userid) {
        LOG.info("正在获取用户【{}】的其他基本信息...", userid);
        CommonResp<Info> resp = new CommonResp<>();
        Info content = infoService.getInfo(userid);
        resp.setContent(content);
        return resp;
    }

    // 更新
    @PutMapping
    public CommonResp<Boolean> update(@Valid @RequestBody InfoUpdateRequest info) {
        LOG.info("正在修改 id 为【{}】的信息", info.getId());
        final CommonResp<Boolean> resp = new CommonResp<>();
        infoService.update(info);
        resp.setMessage("修改成功");
        resp.setContent(true);
        LOG.info("修改成功");
        return resp;
    }
}
