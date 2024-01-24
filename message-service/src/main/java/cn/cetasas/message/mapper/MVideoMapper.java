package cn.cetasas.message.mapper;

import cn.cetasas.message.pojo.MVideo;
import cn.cetasas.message.req.MVideoInsertRequest;

public interface MVideoMapper {

    void insert(MVideoInsertRequest mvideo);

    MVideo selectByPrimaryKey(long id);
}