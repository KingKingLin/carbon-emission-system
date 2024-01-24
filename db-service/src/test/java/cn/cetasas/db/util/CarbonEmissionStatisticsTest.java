package cn.cetasas.db.util;

import cn.cetasas.db.pojo.*;
import cn.cetasas.db.req.QueryReq;
import cn.cetasas.db.resp.CESQueryResp;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CarbonEmissionStatisticsTest {
    
    @Resource
    private RestTemplate restTemplate;

    private static final String url = "https://data.stats.gov.cn/easyquery.htm";

    public Map<?, ?> getCarbonEmissionStatistics(String reg, String zb, String sj) {
        QueryReq params = new QueryReq();

        params.put("m", "QueryData");
        params.put("dbcode", "fsnd");

        List<CESQueryResp.WD> wds = new ArrayList<>();
        if(!StringUtils.isEmpty(reg)) {
            CESQueryResp.WD wd = new CESQueryResp.WD();
            wd.setWdcode(CarbonEmissionStatistics.WDCode.REG);
            wd.setValuecode(reg);
            wds.add(wd);
        }
        params.put("wds", JSON.toJSONString(wds));

        List<CESQueryResp.WD> dfwds = new ArrayList<>();
        if(!StringUtils.isEmpty(zb)) {
            CESQueryResp.WD wd = new CESQueryResp.WD();
            wd.setWdcode(CarbonEmissionStatistics.WDCode.zb);
            wd.setValuecode(zb);
            dfwds.add(wd);
        }
        if(!StringUtils.isEmpty(sj)) {
            CESQueryResp.WD wd = new CESQueryResp.WD();
            wd.setWdcode(CarbonEmissionStatistics.WDCode.SJ);
            wd.setValuecode(sj);
            dfwds.add(wd);
        }
        params.put("dfwds", JSON.toJSONString(dfwds));

//        System.out.println("https://data.stats.gov.cn/easyquery.htm?m=QueryData&dbcode=fsnd&rowcode=zb&colcode=sj&wds="+ JSON.toJSONString(wds) +"&dfwds="+ JSON.toJSONString(dfwds) +"&k1=1678722198592");

        return restTemplate.getForObject(url + "?m={m}&dbcode={dbcode}&rowcode={rowcode}&colcode={colcode}&wds={wds}&dfwds={dfwds}&k1={k1}",
                Map.class, params);
    }

    public Map<?, ?> getRegCode(String zb) {
        QueryReq params = new QueryReq();

        params.put("m", "getOtherWds");
        params.put("dbcode", "fsnd");

        List<CESQueryResp.WD> wds = new ArrayList<CESQueryResp.WD>();
        if(!StringUtils.isEmpty(zb)) {
            CESQueryResp.WD wd = new CESQueryResp.WD();
            wd.setWdcode(CarbonEmissionStatistics.WDCode.zb);
            wd.setValuecode(zb);
            wds.add(wd);
        }
        params.put("wds", JSON.toJSONString(wds));

        return restTemplate.getForObject(url + "?m={m}&dbcode={dbcode}&rowcode={rowcode}&colcode={colcode}&wds={wds}&k1={k1}",
                Map.class, params);
    }

    public static final Map<String, String> reg = new HashMap<>();

    public static final Map<String, String> zb = new HashMap<>();

    public static final CESDataTest treeDataTest = CESDataTest.getInstance();

    @Test
    public void test_reg() {
        Map<?, ?> reg = this.getRegCode("A0706");
        System.out.println(reg);
    }

    @Test
    public void test_110000() {
        Map<?, ?> statistics = this.getCarbonEmissionStatistics("110000", "A0706", "");
        System.out.println(statistics);
    }

    @Test
    public void testStatistics() throws NoSuchFieldException {
        // reg=110000&zb=A0706&sj=2019
//        Map<String, Object> data = this.getCarbonEmissionStatistics("110000", "A0706", "2019");
//        System.out.println(data);

        Map<?, ?> reg = this.getRegCode("A0706");
        if ("200".equals(reg.get("returncode").toString())) {
            if (reg.get("returndata") instanceof List) {
                Object data = ((List<?>) reg.get("returndata")).get(0);
                if (data instanceof Map) {
                    Object nodes = ((Map<?, ?>) data).get("nodes");
                    if (nodes instanceof List) {
                        List<?> nodes1 = (List<?>) nodes;
                        for (Object o : nodes1) {
                            if (o instanceof Map) {
                                Map<?, ?> o1 = (Map<?, ?>) o;
                                Object code = o1.get("code");
                                Object name = o1.get("name");
                                CarbonEmissionStatisticsTest.reg.put(code.toString(), name.toString());
                            }
                        }
                    }
                }
            }
        }

        System.out.println(CarbonEmissionStatisticsTest.reg);

        // 通过 keys 获取所有省份的能源排放数据
        Set<String> keys = CarbonEmissionStatisticsTest.reg.keySet();
        for (String key : keys) {
            Map<?, ?> statistics = this.getCarbonEmissionStatistics(key, "A0706", "");
            if ("200".equals(statistics.get("returncode").toString())) {
                if (statistics.get("returndata") instanceof Map) {
                    Map<?, ?> returndata = (Map<?, ?>) statistics.get("returndata");
                    if (CarbonEmissionStatisticsTest.zb.isEmpty() && returndata.get("wdnodes") instanceof List) {
                        Object wdnodes = ((List<?>) returndata.get("wdnodes")).get(0);
                        if (wdnodes instanceof Map) {
                            Object nodes = ((Map<?, ?>) wdnodes).get("nodes");
                            if (nodes instanceof List) {
                                List<?> nodes1 = (List<?>) nodes;
                                for (Object o : nodes1) {
                                    if (o instanceof Map) {
                                        Map<?, ?> o1 = (Map<?, ?>) o;
                                        CarbonEmissionStatisticsTest.zb.put(o1.get("code").toString(), o1.get("name").toString());
                                    }
                                }
                            }
                        }
                    }
                    if (returndata.get("datanodes") instanceof List) {
                        List<?> nodes = (List<?>) returndata.get("datanodes");
                        for (Object node : nodes) {
                            if (node instanceof Map) {
                                Map<?, ?> node1 = (Map<?, ?>) node;
                                double dataValue = 0.0;
                                if (node1.get("data") instanceof Map) {
                                    Map<?, ?> data = (Map<?, ?>) node1.get("data");
                                    dataValue = Double.parseDouble(data.get("data").toString());
                                }
                                if (node1.get("wds") instanceof List) {
                                    List<?> wds = (List<?>) node1.get("wds");
                                    WDTest wdsTest = new WDTest();
                                    Class<? extends WDTest> wdsTestClass = wdsTest.getClass();
                                    for (Object wd : wds) {
                                        if (wd instanceof Map) {
                                            Map<?, ?> wd1 = (Map<?, ?>) wd;
                                            Field field = wdsTestClass.getField(wd1.get("wdcode").toString());
                                            field.setAccessible(true);
                                            try {
                                                String valuecode = wd1.get("valuecode").toString();
                                                field.set(wdsTest,valuecode);
                                            } catch (IllegalAccessException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    treeDataTest.putIfAbsent(wdsTest.sj, new SJTest());
                                    SJTest sjTest = treeDataTest.get(wdsTest.sj);
                                    sjTest.putIfAbsent(wdsTest.reg, new RegTest());
                                    RegTest regTest = sjTest.get(wdsTest.reg);
                                    regTest.putIfAbsent(wdsTest.zb, new ZBTest(dataValue));
                                }
                            }
                        }
                    }
                }
            }
        }


        System.out.println(CarbonEmissionStatisticsTest.zb);
        System.out.println(CarbonEmissionStatisticsTest.treeDataTest);

        // 指标代码转换成中文
        for (String sj : treeDataTest.keySet()) {
            System.out.println("第" + sj + "的各省份能源排放数据如下");
            SJTest sjTest = treeDataTest.get(sj);
            for (String reg_code : sjTest.keySet()) {
                String reg_name = CarbonEmissionStatisticsTest.reg.get(reg_code);
                System.out.println("\t" + reg_name);
                RegTest regTest = sjTest.get(reg_code);
                for (String zb_code : regTest.keySet()) {
                    String zb_name = CarbonEmissionStatisticsTest.zb.get(zb_code);
                    System.out.println("\t\t" + zb_name + ":" + regTest.get(zb_code).value);
                }
            }
        }
    }
}