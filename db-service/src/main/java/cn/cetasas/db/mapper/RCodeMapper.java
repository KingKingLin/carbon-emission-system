package cn.cetasas.db.mapper;

import cn.cetasas.db.pojo.RCode;

import java.util.List;

public interface RCodeMapper {

    int insert(RCode record);

    List<RCode> getList();
}