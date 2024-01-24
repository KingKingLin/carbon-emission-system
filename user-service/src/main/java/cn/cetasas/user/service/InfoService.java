package cn.cetasas.user.service;

import cn.cetasas.user.mapper.InfoMapper;
import cn.cetasas.user.pojo.Info;
import cn.cetasas.user.req.InfoCreateRequest;
import cn.cetasas.user.req.InfoUpdateRequest;
import cn.cetasas.user.util.SnowFlake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;

@Service
public class InfoService {

    private final static Logger LOG = LoggerFactory.getLogger(InfoService.class);

    @Resource
    private InfoMapper infoMapper;

    @Resource
    private SnowFlake snowFlake;

    public Info getInfo(Integer userid) {
        Info info = infoMapper.getInfoByUserId(userid);

        if (ObjectUtils.isEmpty(info)) {
            LOG.info("查询出来的信息表为空");
            LOG.info("正在为该用户创建信息表...");
            // 如果查询出来的 info 为 空，则代表还未给其创建相关附加信息
            // 新建
            createInfo(userid);
            info = infoMapper.getInfoByUserId(userid); // 重新更新一下，因为初始有默认值
        }
        LOG.info("返回信息表");
        return info;
    }

    private void createInfo(Integer userid) {
        InfoCreateRequest info = new InfoCreateRequest();
        info.setId(snowFlake.nextId());
        info.setUserid(userid);
        infoMapper.createInfo(info);
    }

    public void update(InfoUpdateRequest info) {
        LOG.info("正在修改...");
        infoMapper.update(info);
    }
}
