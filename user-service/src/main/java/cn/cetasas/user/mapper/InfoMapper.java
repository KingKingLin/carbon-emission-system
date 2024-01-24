package cn.cetasas.user.mapper;

import cn.cetasas.user.pojo.Info;
import cn.cetasas.user.req.InfoCreateRequest;
import cn.cetasas.user.req.InfoUpdateRequest;

public interface InfoMapper {

    Info getInfoByUserId(Integer user_id);

    void createInfo(InfoCreateRequest info);

    void update(InfoUpdateRequest info);
}