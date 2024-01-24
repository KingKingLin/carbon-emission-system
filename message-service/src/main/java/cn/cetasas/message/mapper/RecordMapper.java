package cn.cetasas.message.mapper;

import cn.cetasas.message.pojo.Record;
import cn.cetasas.message.pojo.RecordExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface RecordMapper {
    long countByExample(RecordExample example);

    int deleteByExample(RecordExample example);

    int deleteByPrimaryKey(@Param("id") Long id, @Param("userid") Integer userid);

    int insert(Record record);

    int insertSelective(Record record);

    List<Record> selectByExample(RecordExample example);

    int updateByExampleSelective(@Param("record") Record record, @Param("example") RecordExample example);

    int updateByExample(@Param("record") Record record, @Param("example") RecordExample example);
}