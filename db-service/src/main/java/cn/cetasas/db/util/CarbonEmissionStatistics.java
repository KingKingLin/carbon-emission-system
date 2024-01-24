package cn.cetasas.db.util;

import cn.cetasas.db.exception.BusinessException;
import cn.cetasas.db.exception.BusinessExceptionCode;
import cn.cetasas.db.pojo.*;
import cn.cetasas.db.req.QueryReq;
import cn.cetasas.db.resp.CESQueryResp;
import cn.cetasas.db.resp.RegQueryResp;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.*;

public class CarbonEmissionStatistics {

    private static final Logger LOG = LoggerFactory.getLogger(CarbonEmissionStatistics.class);

    private final RestTemplate restTemplate;

    private static final String url = "https://data.stats.gov.cn/easyquery.htm";

    public CarbonEmissionStatistics(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public static class WDCode {
        public static final String REG = "reg";
        public static final String SJ = "sj";
        public static final String zb = "zb";
    }

    /**
     *
     * @param dbcode 数据库代码
     * @param reg 省份地区代码 NOT NULL
     * @param zb 指标
     * @param sj 时间范围
     * @return 单独某个省份在某个时间范围内的原始碳排放数据或者全球碳排放在某个时间范围内的原始数据（国家统计局原始数据）
     */
    private CESQueryResp getOriginalCarbonEmissionStatistics(String dbcode, String reg, String zb, String sj) {
        QueryReq params = new QueryReq();

        params.put("m", "QueryData");
        params.put("dbcode", dbcode);

        List<CESQueryResp.WD> wds = new ArrayList<>();
        if(!ObjectUtils.isEmpty(reg)) {
            CESQueryResp.WD wd = new CESQueryResp.WD();
            wd.setWdcode(WDCode.REG);
            wd.setValuecode(reg);
            wds.add(wd);
        }
        params.put("wds", JSON.toJSONString(wds));

        List<CESQueryResp.WD> dfwds = new ArrayList<>();
        if(!ObjectUtils.isEmpty(zb)) {
            CESQueryResp.WD wd = new CESQueryResp.WD();
            wd.setWdcode(WDCode.zb);
            wd.setValuecode(zb);
            dfwds.add(wd);
        }
        if(!ObjectUtils.isEmpty(sj)) {
            CESQueryResp.WD wd = new CESQueryResp.WD();
            wd.setWdcode(WDCode.SJ);
            wd.setValuecode(sj);
            dfwds.add(wd);
        }
        params.put("dfwds", JSON.toJSONString(dfwds));

        LOG.info("构建请求参数：{}【{}】", params, url);

//        System.out.println("https://data.stats.gov.cn/easyquery.htm?m=QueryData&dbcode=fsnd&rowcode=zb&colcode=sj&wds="+ JSON.toJSONString(wds) +"&dfwds="+ JSON.toJSONString(dfwds) +"&k1=1678722198592");

        return restTemplate.getForObject(url + "?m={m}&dbcode={dbcode}&rowcode={rowcode}&colcode={colcode}&wds={wds}&dfwds={dfwds}&k1={k1}",
                CESQueryResp.class, params);
    }

    private RegQueryResp getOriginalRegCode() {
        QueryReq params = new QueryReq();

        params.put("m", "getOtherWds");
        params.put("dbcode", "fsnd");

        List<CESQueryResp.WD> wds = new ArrayList<CESQueryResp.WD>();
        CESQueryResp.WD wd = new CESQueryResp.WD();
        wd.setWdcode(WDCode.zb);
        wd.setValuecode("A0706");
        wds.add(wd);
        params.put("wds", JSON.toJSONString(wds));

        LOG.info("构建请求参数：{}【{}】", params, url);

        return restTemplate.getForObject(url + "?m={m}&dbcode={dbcode}&rowcode={rowcode}&colcode={colcode}&wds={wds}&k1={k1}",
                RegQueryResp.class, params);
    }

    /**
     *
     * @return 地区代码映射表
     */
    public Map<String, RCode> getRegCode() {
        HashMap<String, RCode> cesData = new HashMap<>();
        RegQueryResp reg = getOriginalRegCode();
        if (reg.getReturncode() != 200) {
            throw new BusinessException(BusinessExceptionCode.CRAWLER_FALSE);
        } else {
            List<RegQueryResp.Node> nodes = reg.getReturndata().get(0).getNodes();
            for (RegQueryResp.Node node : nodes) {
                RCode rCode = new RCode();
                rCode.setRCode(node.getCode());
                rCode.setRName(node.getName());
                cesData.put(node.getCode(), rCode);
            }
        }
        return cesData;
    }

    /**
     *
     * @param reg 省份地区代码
     * @param sj 时间范围
     * @return 单独某个省份在某个时间范围内的碳排放数据(国家统计局处理后的数据）
     */
    public StandardData<Reg> getRegCarbonEmissionStatistics(String reg, String sj) {
        StandardData<Reg> standardData = new StandardData<>();

        CESQueryResp statistics = getOriginalCarbonEmissionStatistics("fsnd", reg, "A0706", sj);

        CESData<Reg> cesData = new CESData<>();

        func01(standardData, statistics, cesData);

        standardData.setData(cesData);

        return standardData;
    }

    private void func01(StandardData<Reg> standardData, CESQueryResp statistics, CESData<Reg> cesData) {
        if (statistics.getReturncode() != 200) {
            throw new BusinessException(BusinessExceptionCode.CRAWLER_FALSE);
        } else {
            CESQueryResp.ReturnData returndata = statistics.getReturndata();
            setZB(standardData, returndata);
            List<CESQueryResp.DataNode> datanodes = returndata.getDatanodes();
            for (CESQueryResp.DataNode datanode : datanodes) {
                double data = datanode.getData().getData();
                List<CESQueryResp.WD> wds = datanode.getWds();
                WD wd = getWDByWDS(wds);
                setCESData_Reg(cesData, wd, data);
            }
        }
    }

    private void setCESData_Reg(CESData<Reg> cesData, WD wd, double data) {
        cesData.putIfAbsent(wd.getSj(), new SJ<Reg>());
        SJ<Reg> sj = cesData.get(wd.getSj());
        sj.putIfAbsent(wd.getReg(), new Reg());
        Reg reg = sj.get(wd.getReg());
        reg.put(wd.getZb(), new ZB(data));
    }

    private void setCESData_ZB(CESData<ZB> cesData, WD wd, double data) {
        cesData.putIfAbsent(wd.getSj(), new SJ<ZB>());
        SJ<ZB> sj = cesData.get(wd.getSj());
        sj.put(wd.getZb(), new ZB(data));
    }

    /**
     *
     * @param standardData 处理后的碳排放数据
     * @param returndata 设置碳排放指标
     */
    private void setZB(StandardData<?> standardData, CESQueryResp.ReturnData returndata) {
        if (ObjectUtils.isEmpty(standardData.getZb())) {
            HashMap<String, ECode> zb = new HashMap<>();
            List<CESQueryResp.Node> nodes = returndata.getWdnodes().get(0).getNodes();
            for (CESQueryResp.Node node : nodes) {
                ECode eCode = new ECode();
                eCode.setECode(node.getCode());
                eCode.setEName(node.getName());
                zb.put(node.getCode(), eCode);
            }
            standardData.setZb(zb);
        }
    }

    private WD getWDByWDS(List<CESQueryResp.WD> wds) {
        WD wdObject = new WD();
        Class<? extends WD> wdClass = wdObject.getClass();
        for (CESQueryResp.WD wd : wds) {
            try {
                Field field = wdClass.getDeclaredField(wd.getWdcode());
                field.setAccessible(true);
                field.set(wdObject, wd.getValuecode());
            } catch (NoSuchFieldException | IllegalAccessException e) { // 异常处理
                throw new BusinessException(BusinessExceptionCode.REFLECT_FIELD_ERROR);
            }
        }
        return wdObject;
    }

    /**
     *
     * @param sj 时间范围
     * @return 全球碳排放在某个时间范围内的数据(国家统计局处理后的数据)
     */
    public StandardData<ZB> getTotalCarbonEmissionStatistics(String sj) {
        StandardData<ZB> standardData = new StandardData<>();

        LOG.info("正在获取{}年国家总能源排放数据...", sj);
        CESQueryResp statistics = getOriginalCarbonEmissionStatistics("hgnd", "", "A070E", sj);

        CESData<ZB> cesData = new CESData<>();
        if (statistics.getReturncode() != 200) {
            throw new BusinessException(BusinessExceptionCode.CRAWLER_FALSE);
        } else {
            CESQueryResp.ReturnData returndata = statistics.getReturndata();
            setZB(standardData, returndata);
            List<CESQueryResp.DataNode> datanodes = returndata.getDatanodes();
            for (CESQueryResp.DataNode datanode : datanodes) {
                double data = datanode.getData().getData();
                List<CESQueryResp.WD> wds = datanode.getWds();
                WD wd = getWDByWDS(wds);
                setCESData_ZB(cesData, wd, data);
            }
        }

        standardData.setData(cesData);

        return standardData;
    }

    /**
     *
     * @param sj 时间
     * @return 返回某个时间段所有省份的碳排放数据
     */
    public StandardData<Reg> getAllProvinceCES(String sj) {
        StandardData<Reg> standardData = new StandardData<>(); // 返回值
        CESData<Reg> cesData = new CESData<>(); // 所有省份碳排放数据
        Map<String, RCode> regs = getRegCode();
        for (String reg : regs.keySet()) {
            // 某个地区碳排放数据
            LOG.info("正在获取{}地区的相关碳排放数据", regs.get(reg).getRName());
            CESQueryResp statistics = getOriginalCarbonEmissionStatistics("fsnd", reg, "A0706", sj);

            func01(standardData, statistics, cesData);
        }
        standardData.setData(cesData);
        return standardData;
    }
}
