package cn.cetasas.message.mapper;

import cn.cetasas.message.pojo.MImage;
import cn.cetasas.message.req.MImageInsertRequest;

public interface MImageMapper {

    void insert(MImageInsertRequest mfile);

    MImage selectByPrimaryKey(long id);
}