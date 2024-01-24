package cn.cetasas.db.service;

import cn.cetasas.db.mapper.RCodeMapper;
import cn.cetasas.db.pojo.RCode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RCodeService {

    @Resource
    private RCodeMapper rCodeMapper;

    public List<RCode> getList() {
        return rCodeMapper.getList();
    }
}
