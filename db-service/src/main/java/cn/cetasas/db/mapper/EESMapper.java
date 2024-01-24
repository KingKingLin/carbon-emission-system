package cn.cetasas.db.mapper;

import cn.cetasas.db.pojo.EES;
import cn.cetasas.db.resp.ZBResp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EESMapper {

    int insert(@Param("tableName") String tableName, @Param("ees") EES ees);

    List<EES> getAllProvince(String tableName);

    List<ZBResp> getDataByRegAndYear(String reg, String tableName);
}