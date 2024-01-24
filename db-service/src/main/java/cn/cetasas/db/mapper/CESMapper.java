package cn.cetasas.db.mapper;

import cn.cetasas.db.pojo.CES;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CESMapper {

    int insert(@Param("tableName") String tableName, @Param("ces")CES ces);

    List<CES> getAllProvince(String s);
}