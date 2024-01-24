package cn.cetasas.db;

import cn.cetasas.db.req.QueryReq;
import cn.cetasas.db.resp.CESQueryResp;
import cn.cetasas.db.util.CarbonEmissionStatistics;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 测试获取时间
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AT {
    @Test
    public void test1() {
        Date date = new Date();
        int year = date.getYear();
        System.out.println(year + 1900);
    }

    @Test
    public void test2() {
        Calendar calendar = Calendar.getInstance();
        System.out.println(calendar.get(Calendar.YEAR));
    }


    @Resource
    private RestTemplate restTemplate;

    private static final String url = "https://data.stats.gov.cn/easyquery.htm";

    private Map<?, ?> getRegCode(String zb) {
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

    /**
     * 测试Python脚本和Java脚本爬虫效率
     */
    @Test
    public void test3() {
        long start, end;

        System.out.println("Java start");
        start = System.currentTimeMillis();
        System.out.println(this.getRegCode("A0706"));
        end = System.currentTimeMillis();
        System.out.println("Java 脚本运行时间：" + (end - start));
        System.out.println("Java end");

        System.out.println();

        System.out.println("Python start");
        start = System.currentTimeMillis();
        try {
            String[] args1=new String[]{"python", System.getProperty("user.dir") + "/src/main/resources/python/reg_code.py", "A0706"};
            Process pr = Runtime.getRuntime().exec(args1);

            BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream(), "gbk"/*"GB2312"*/));
            String json = in.lines().collect(Collectors.joining());
            json = json.replaceAll("'", "\"").replaceAll("True", "true").replaceAll("False", "false");//.replaceAll(" ", "");
            System.out.println(JSON.parseObject(json, Map.class));
            in.close();
            int i = pr.waitFor();
//            System.out.println(i == 0 ? "调用python脚本成功!" : "调用python脚本失败!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        end = System.currentTimeMillis();
        System.out.println("Python 脚本运行时间：" + (end - start));
        System.out.println("Python end");
    }
}
