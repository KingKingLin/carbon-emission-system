package cn.cetasas.db.service;

import cn.cetasas.db.mapper.GEOMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 负责碳排放有关请求
 */
@Service
public class GEOService {

    @Resource
    private GEOMapper geoMapper;

    public String getGeo(String nation) {
        return geoMapper.getGeo(nation);
    }
}
