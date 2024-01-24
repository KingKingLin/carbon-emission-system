package cn.cetasas.db.mapper;

import cn.cetasas.db.pojo.Total;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TotalMapper {

    int insert(@Param("tableName") String tableName, @Param("total") Total total);

    List<Total> getDataByYear(String tableName);
}