# 中国省际碳排放时空分析系统设计与实现

## 一、数据获取

> 目前只有分省年度数据有能源排放相关的数据，故这里我们只处理年度数据，剩下的季度、月度其实和年度的处理相近。

分省年度数据：[国家数据 (stats.gov.cn)](https://data.stats.gov.cn/easyquery.htm?cn=E0101) √

分省季度数据：[国家数据 (stats.gov.cn)](https://data.stats.gov.cn/easyquery.htm?cn=E0102)

分省月度数据：[国家数据 (stats.gov.cn)](https://data.stats.gov.cn/easyquery.htm?cn=E0101)

### 1）主要能源的碳排放系数

> [各类能源碳排放系数（二氧化碳排放系数）-碳中和资讯网 (tzixun.com.cn)](https://www.tzixun.com.cn/11302.html)
>
> [各种能源碳排放参考系数以及计算方法和公式_碳排放交易网——全球领先的碳市场门户中文网站 (tanpaifang.com)](http://www.tanpaifang.com/tanjiliang/2014/0914/38053.html)
>
> [电力的能耗单位是吨标准煤，排放因子kgco2/kwh，算碳排放量直接乘吗？？ - 知乎 (zhihu.com)](https://www.zhihu.com/question/458218302)
>
> [基于IPCC的常用碳排放系数 - 环境经济学 - 经管之家(原人大经济论坛) (pinggu.org)](https://bbs.pinggu.org/thread-631499-1-1.html)
>
> [能源单位热值含碳量与碳氧化率对照表 - 百度文库 (baidu.com)](https://wenku.baidu.com/view/53da30755bfb770bf78a6529647d27284b7337b7.html?_wkts_=1680535386042)

| 能源   | NCV(PJ/万吨，PJ/10^8^m^3^) | CC(吨碳/TJ) | O(%)煤炭 |
| ------ | -------------------------- | ----------- | -------- |
| 煤炭   | 0.21                       | 26.32       | 91.80    |
| 汽油   | 0.44                       | 18.90       | 98.60    |
| 柴油   | 0.43                       | 20.20       | 98.20    |
| 天然气 | 0.39                       | 15.32       | 99.00    |
| 煤油   | 0.44                       | 19.60       | 98.00    |
| 燃料油 | 0.43                       | 21.10       | 98.50    |
| 原油   | 0.43                       | 20.08       | 97.90    |
| 焦炭   | 0.28                       | 31.38       | 92.80    |

```
全国电网排放因子调整，
在核算2021及2022年度碳排放量时，全国电网排放因子由0.6101tCO2/MWh调整为最新的0.5810tCO2/MWh。

1PJ = 1000TJ（1 拍焦耳 it's 1000 太焦耳）
```

![image-20230403224814614](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230403224814614.png)

```
以“原煤”为例：
1.9003=20908*0.000000001*26.37*0.94*1000*3.66667（参考）
	  =0.20908*26.37/10*0.94*3.6667
```

### 2）碳排放量核算公式

```
CE = ∑EC × C = ∑EC × NCV × CC × O × T
NCV 平均热力低位值
CC  每单位能源热值的碳含量
O   碳氧化率
T   碳和二氧化碳之间的转换系数，为常数，值为 44/12
```

### 3）计算碳排放系数

| 能源   | 碳排放系数(万吨CO2，万吨CO2/亿kWh，万吨CO2/亿立方米) |
| ------ | ---------------------------------------------------- |
| 煤炭   | 1.8605                                               |
| 焦炭   | 2.9898                                               |
| 原油   | 3.0995                                               |
| 汽油   | 3.0065                                               |
| 煤油   | 3.0989                                               |
| 柴油   | 3.1275                                               |
| 燃料油 | 3.2769                                               |
| 天然气 | 2.1633                                               |
| 电力   | 5.810                                                |

## 二、爬虫代码实现

### 1）Python

> 参考文献：[(1条消息) 史上超详细python爬取国家统计局数据_python 网 提取 统计_王小明爱吃大菠萝的博客-CSDN博客](https://blog.csdn.net/qq_41988893/article/details/103017854)

> sj 对应的 code: LAST5、LAST10、LAST20、XXXX(具体年份)、XXXX-XXXX

查询能源排放相关数据

```python
#引包
import requests
import time
#生成时间戳
def getTime():
    return int(round(time.time() * 1000))
#爬虫代码
url='https://data.stats.gov.cn/easyquery.htm'
headers={'User-Agent':'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.69'}
key={} #参数键值对
key['m']='QueryData'
key['dbcode']='fsnd'
key['rowcode']='zb'
key['colcode']='sj'
#reg 指地区, 110000 是北京的代码
key['wds']='[{"wdcode":"reg","valuecode":"110000"}]'
#zb 指标, sj 指时间
key['dfwds']='[{"wdcode":"zb","valuecode":"A0706"},{"wdcode":"sj","valuecode":"2019"}]'
key['k1']=str(getTime())
requests.packages.urllib3.disable_warnings() #忽视ssl-warnings(Adding certificate verification is strongly advised)
r=requests.get(url,headers=headers,params=key,verify=False)
print(r.json())
```

查询各地区的区域代码

```python
#引包
import requests
import time
#生成时间戳
def getTime():
    return int(round(time.time() * 1000))
#爬虫代码
url='https://data.stats.gov.cn/easyquery.htm'
headers={'User-Agent':'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.69'}
key={}#参数键值对
key['m']='getOtherWds'
key['dbcode']='fsnd'
key['rowcode']='zb'
key['colcode']='sj'
key['wds']='[{"wdcode":"zb","valuecode":"A0706"}]'
key['k1']=str(getTime())
requests.packages.urllib3.disable_warnings()  #忽视ssl-warnings(Adding certificate verification is strongly advised)
r=requests.get(url,headers=headers,params=key,verify=False)
print(r.json())
```

查询中国的能源排放总值

```python
#引包
import requests
import time
#生成时间戳
def getTime():
    return int(round(time.time() * 1000))
#爬虫代码
#该爬虫数据可以用来绘制扇形图
if __name__ == '__main__':
    url='https://data.stats.gov.cn/easyquery.htm'
    headers={'User-Agent':'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.69'}
    key={}#参数键值对
    key['m']='QueryData'
    key['dbcode']='hgnd'
    key['rowcode']='zb'
    key['colcode']='sj'
    key['wds']='[]'
    key['dfwds']='[{"wdcode":"zb","valuecode":"A0706"}]' # 有sj参数
    key['k1']=str(getTime())
    requests.packages.urllib3.disable_warnings()
    r=requests.get(url,headers=headers,params=key,verify=False)
    print(r.json())
    #key['dfwds']='[{"wdcode":"sj","valuecode":"2019"}]'
    r.close()
```

### 2）Java(测试)

#### 1. 修改 RestTemplate 配置，取消 SSL 证书

> 为什么要取消 SSL 证书？
>
> 因为国家统计局页面存在证书问题，如果不取消 SSL 证书，则我们无法爬虫到想要的数据。

```java
package cn.cetasas.user.config;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Configuration
public class RestTemplateConfig {
    @Bean
    @LoadBalanced // 负载均衡
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        RestTemplate restTemplate = new RestTemplate(factory);
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(
                MediaType.TEXT_HTML,
                MediaType.TEXT_PLAIN));
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);
        return restTemplate;
    }

//    @Bean
//    public ClientHttpRequestFactory simpleClientHttpRequestFactory(){
//        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
//        factory.setConnectTimeout(15000);
//        factory.setReadTimeout(5000);
//        return factory;
//    }

    @Bean
    public HttpComponentsClientHttpRequestFactory generateHttpRequestFactory()
            throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException
    {
        TrustStrategy acceptingTrustStrategy = (x509Certificates, authType) -> true;
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        SSLConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());

        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setSSLSocketFactory(connectionSocketFactory);
        CloseableHttpClient httpClient = httpClientBuilder.build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);
        return factory;
    }
}
```

#### 2. 创建实体类

```java
package cn.cetasas.user.pojo;

import java.util.HashMap;

public class QueryParam extends HashMap<String, Object> {
    public QueryParam() {
        this.put("dbcode", "fsnd");
        this.put("rowcode", "zb");
        this.put("colcode", "sj");
        this.put("k1", System.nanoTime()); // 时间戳
    }
}
```

```java
package cn.cetasas.user.pojo;

import lombok.Data;

@Data
public class WD {
    public String wdcode;
    public String valuecode;
}
```

#### 3. 爬虫代码实现（测试）

```java
package cn.cetasas.user.web;

import cn.cetasas.user.pojo.QueryParam;
import cn.cetasas.user.pojo.WD;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.websocket.server.PathParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ces")
public class CarbonEmissionStatisticsController {

    @Autowired
    private RestTemplate restTemplate;

    private String url = "https://data.stats.gov.cn/easyquery.htm";

    @GetMapping("/data")
    public  Map getCarbonEmissionStatistics(@PathParam("reg") String reg, @PathParam("zb") String zb, @PathParam("sj") String sj) {
        QueryParam params = new QueryParam();

        params.put("m", "QueryData");

        List<WD> wds = new ArrayList<WD>();
        if(!StringUtils.isEmpty(reg)) {
            WD wd = new WD();
            wd.setWdcode("reg");
            wd.setValuecode(reg);
            wds.add(wd);
        }
        params.put("wds", JSON.toJSONString(wds));

        List<WD> dfwds = new ArrayList<WD>();
        if(!StringUtils.isEmpty(zb)) {
            WD wd = new WD();
            wd.setWdcode("zb");
            wd.setValuecode(zb);
            dfwds.add(wd);
        }
        if(!StringUtils.isEmpty(sj)) {
            WD wd = new WD();
            wd.setWdcode("sj");
            wd.setValuecode(sj);
            dfwds.add(wd);
        }
        params.put("dfwds", JSON.toJSONString(dfwds));

//        System.out.println("https://data.stats.gov.cn/easyquery.htm?m=QueryData&dbcode=fsnd&rowcode=zb&colcode=sj&wds="+ JSON.toJSONString(wds) +"&dfwds="+ JSON.toJSONString(dfwds) +"&k1=1678722198592");

        Map res = restTemplate.getForObject(url + "?m={m}&dbcode={dbcode}&rowcode={rowcode}&colcode={colcode}&wds={wds}&dfwds={dfwds}&k1={k1}",
                Map.class, params);

        return res;
    }

    @GetMapping("/reg")
    public  Map getRegCode(@PathParam("zb") String zb) {
        QueryParam params = new QueryParam();

        params.put("m", "getOtherWds");

        List<WD> wds = new ArrayList<WD>();
        if(!StringUtils.isEmpty(zb)) {
            WD wd = new WD();
            wd.setWdcode("zb");
            wd.setValuecode(zb);
            wds.add(wd);
        }
        params.put("wds", JSON.toJSONString(wds));

        Map res = restTemplate.getForObject(url + "?m={m}&dbcode={dbcode}&rowcode={rowcode}&colcode={colcode}&wds={wds}&k1={k1}",
                Map.class, params);

        return res;
    }
}
```

## 三、自动爬虫建立碳排放数据库

### 1）测试碳排放数据处理

#### 1. 建立实体类

##### TreeDataTest

```java
package cn.cetasas.user.util;

import java.util.HashMap;

public class TreeDataTest extends HashMap<String, SJTest> {
    private TreeDataTest() {}

    private static class Singleton {
        private static final TreeDataTest treeDataTest = new TreeDataTest();
    }

    public static TreeDataTest getInstance() {
        return Singleton.treeDataTest;
    }
}
```

##### SJTest

```java
package cn.cetasas.user.util;

import java.util.HashMap;

public class SJTest extends HashMap<String, RegTest> {
    public SJTest() {}

//    private static class Singleton {
//        private static final SJTest sjTest = new SJTest();
//    }
//
//    public static SJTest getInstance() {
//        return Singleton.sjTest;
//    }
}
```

##### RegTest

```java
package cn.cetasas.user.util;

import java.util.HashMap;

public class RegTest extends HashMap<String, ZBTest> {

    public RegTest() {}

//    private static class Singleton {
//        private static final RegTest regTest = new RegTest();
//    }
//
//    public static RegTest getInstance() {
//        return Singleton.regTest;
//    }
}
```

##### ZBTest

```java
package cn.cetasas.user.util;

public class ZBTest {
    public double value;

    public ZBTest() {}

    public ZBTest(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "{" +
                "value=" + value +
                '}';
    }
}
```

##### WDSTest

```java
package cn.cetasas.user.util;

import lombok.Data;

@Data
public class WDSTest {
    public String zb;
    public String reg;
    public String sj;
}
```

#### 2. 测试代码

```java
package cn.cetasas.user.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CarbonEmissionStatisticsTest {

    @Autowired
    private CarbonEmissionStatistics carbonEmissionStatistics;

    public static final Map<String, String> reg = new HashMap<>();

    public static final Map<String, String> zb = new HashMap<>();

    public static final TreeDataTest treeDataTest = TreeDataTest.getInstance();

    @Test
    public void test_reg() {
        Map<?, ?> reg = carbonEmissionStatistics.getRegCode("A0706");
        System.out.println(reg);
    }

    @Test
    public void test_110000() {
        Map<?, ?> statistics = carbonEmissionStatistics.getCarbonEmissionStatistics("110000", "A0706", "");
        System.out.println(statistics);
    }

    @Test
    public void testStatistics() throws NoSuchFieldException {
        // reg=110000&zb=A0706&sj=2019
//        Map<String, Object> data = carbonEmissionStatistics.getCarbonEmissionStatistics("110000", "A0706", "2019");
//        System.out.println(data);

        Map<?, ?> reg = carbonEmissionStatistics.getRegCode("A0706");
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
            Map<?, ?> statistics = carbonEmissionStatistics.getCarbonEmissionStatistics(key, "A0706", "");
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
                                    WDSTest wdsTest = new WDSTest();
                                    Class<? extends WDSTest> wdsTestClass = wdsTest.getClass();
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
```

#### 3. 测试结果示意图

![image-20230318214930773](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230318214930773.png)

### 2）在JVM运行Python脚本

#### 1. 使用到的Python脚本

##### I. test.py

```python
a = "hello, world!"
print(a)
```

##### II. test_with_params

```python
import numpy as np
import sys

a = np.ones(3)
print(a)
print('恭喜您！java调用python代码成功')
print('脚本名为：%s'%(sys.argv[0]))
print('传入的参数为：')
for i in range(1, len(sys.argv)):
    print('参数:%s'%(sys.argv[i]))
```

#### 2. 使用Jython ×

> 比较鸡肋

##### I. 引入Jython依赖

```xml
<dependency>
    <groupId>org.python</groupId>
    <artifactId>jython-standalone</artifactId>
    <version>2.7.1</version>
</dependency>
```

##### II. 测试代码

```java
package cn.cetasas.user.util;

import org.junit.Test;
import org.python.util.PythonInterpreter

public class PythonRunnerTest {

    @Test
    public void pythonRunnerTest1() {
        // python 解释器
        PythonInterpreter interpreter = new PythonInterpreter();
        // 1）选择执行的Python语句
        System.out.println("执行python语句:");
        interpreter.exec("a='hello world';");
        interpreter.exec("print a;");
        // 2）选择执行的Python文件
        System.out.println("执行python脚本:");
        interpreter.execfile("E:/python/test.py");
    }
}
```

##### III. 存在的不足和缺陷

​	使用Jypthon的PythonIntercepreter不能带参数的调用python脚本

> 究其原因：
>
> ​	**Jython是纯Java实现的。**
>
> ​	**Python的代码可以用Jython运行，只要没有用到扩展库。**
>
> ​	**因为Jython实现了一个Python解析器，可以解析执行Python的代码。**
>
> ​	**Jython在import 一个Python文件的时候，会编译生成.class文件，而不是.pyc文件。**
>
> ​	**这些.class文件就是普通的Java Class，只不过调用了Jython VM.**
>
> ​	**Jython的限制是无法使用用C写的Python扩展库，因为没有在ABI层兼容CPython。**
>
> ​	**换句话说，Jython没法给.so提供C的内存模型。**

##### IV. 获取运行目录的绝对路径(Java)

```java
// 1) /E:/docs/college/毕业设计/代码/carbon-emission-system/user-service/target/classes/
// or /E:/docs/college/毕业设计/代码/carbon-emission-system/user-service/target/test-classes/
String realPath = ClassUtils.getDefaultClassLoader().getResource("").getPath(); // 有问题
// 2) E:\docs\college\毕业设计\代码\carbon-emission-system\user-service
String realPath = System.getProperty("user.dir");
// 3) /E:/docs/college/毕业设计/代码/carbon-emission-system/user-service/target/classes/
// or /E:/docs/college/毕业设计/代码/carbon-emission-system/user-service/target/test-classes/
String path = ResourceUtils.getURL("classpath:").getPath();
```

> 存在中文乱码问题

```java
private static String realPath;

static {
    try {
        realPath = URLDecoder.decode(ClassUtils.getDefaultClassLoader().getResource("").getFile(), "utf-8");
    } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
    }
}
```

> 建议使用（以上都不好）

```java
ResourceUtils.getURL("classpath:").getPath()
```

#### 3. 有参数的运行Python脚本 √

> 这里执行 pythonRunnerTest2WithParams 时会出错，其原因是：
>
> ​	我使用的是anaconda中的python，而我使用的python中没有numpy库，所以会造成相应的代码不会执行的问题。

##### I. 测试代码

​	pythonRunnerTest2 在使用到如 numpy 等库的时候可能会出错，其原因是：所使用的 python 中并未有 numpy 库，所以需要给环境变量添加：

```
Path=D:\Anaconda3;D:\Anaconda3\Library\mingw-w64\bin;D:\Anaconda3\Library\usr\bin;D:\Anaconda3\Library\bin;D:\Anaconda3\Scripts;
```

```java
package cn.cetasas.user.util;

import org.junit.Test;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PythonRunnerTest {

    /**
     * pythonRunnerTest2 在使用到如 numpy 等库的时候可能会出错，其原因是：所使用的 python 中并未有 numpy 库
     * 需要给环境变量添加Path=D:\Anaconda3;D:\Anaconda3\Library\mingw-w64\bin;D:\Anaconda3\Library\usr\bin;D:\Anaconda3\Library\bin;D:\Anaconda3\Scripts;
     */
    @Test
    public void pythonRunnerTest2() {
        try {
            System.out.println("start");
            // 运行环境 和 python script
//            String[] args1=new String[]{"D:/anaconda3/python", realPath + "/src/main/resources/python/test.py"};
//            String[] args1=new String[]{"python", realPath + "/src/main/resources/python/test.py"};
            String[] args1=new String[]{"python", realPath + "/src/main/resources/python/reg_code.py"};
//            String[] args1=new String[]{"python", realPath + "/src/main/resources/python/get_data.py"};
            Process pr=Runtime.getRuntime().exec(args1);

            BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream(), "gbk"/*"GB2312"*/));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            int i = pr.waitFor();
            System.out.println(i == 0 ? "调用python脚本成功!" : "调用python脚本失败!");
            System.out.println("end");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void pythonRunnerTest2WithParams() {
        try {
            System.out.println("start");
            String para1="time";
            String para2="sfdjk";
//            String[] args1=new String[]{"D:/anaconda3/python", realPath + "/src/main/resources/python/test_with_params.py", para1, para2};
            String[] args1=new String[]{"python", realPath + "/src/main/resources/python/test_with_params.py", para1, para2};
            Process pr = Runtime.getRuntime().exec(args1);

            BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream(), "gbk"/*"GB2312"*/));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            int i = pr.waitFor();
            System.out.println(i == 0 ? "调用python脚本成功!" : "调用python脚本失败!");
            System.out.println("end");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

```

##### II. 解决开头提出的引用库问题

（1）给系统环境Path添加如下配置

![image-20230319222347819](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230319222347819.png)

（2）并将python的运行路径修改成python

![image-20230319222552851](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230319222552851.png)

（3）运行结果

![image-20230319223808265](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230319223808265.png)

**或者**

（1）在idea中的"Edit Configurations"

![image-20230319225218687](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230319225218687.png)

（2）添加如下环境，一样将python的运行路径改为"python"

![image-20230319225324508](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230319225324508.png)

#### 4、将BufferedReader流转换成字符串的三种方式

##### I. apache的IOUtils

> 当然, 请注意, 使用此机制, 可能会通过发送填满服务器内存的永无止境的数据流来进行拒绝服务攻击

```java
String json = IOUtils.toString(in);
```

##### II. 通过JDK1.8的Stream

```java
String json = in.lines().collect(Collectors.joining());
```

##### III. JDK1.7以前的老方法

```java
String json, line;
while ((line = in.readLine()) != null) {
	json += line;
}
```

#### 5、测试在JVM运行Python脚本和Java代码的耗费时间

##### I. Python脚本

> 存在问题：
>
> python脚本返回的json字符串是用单引号引起来的，还会多出空格，以及False和True等问题，如：
>
> {"returncode": 200, "returndata": [{"issj": False, "nodes": [{"code": "110000", "name": "北京市", "sort": "1"}]}]}
>
> 而com.alibaba.fastjson.JSON或者说Java的json只能处理双引号的数据，如：
>
> {"returncode":200,"returndata":[{"issj":false,"nodes":[{"code":"110000","name":"北京市","sort":"1"}]}]}
>
> 因此python脚本会多出一个单引号转换成双引号和去除空格的过程

最后得出的结论，Python返回的json字符串不太适合Java

##### II. Java代码

![image-20230320004603951](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230320004603951.png)

![image-20230320005028685](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230320005028685.png)

### 3） 数据库设计

#### 1. 方案一：三大范式 3NF

![image-20230403222554683](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230403222554683.png)

#### 2. 方案二：分表

![image-20230403222814032](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230403222814032.png)

#### 3.  两种方案的优缺点

**方案一：**

​	优点：简单易实现

​	缺点：各省份碳排放数据表和各省份能源排放表这两张表的数据过于庞大，查询效率随着时间的增长而增大

**方案二：**

​	优点：将各省份碳排放数据表和各省份能源排放表按照年份分表，每张表都不会太大，查询效率稳定

​	缺点：设计较为复杂

### 4）数据库的建表代码以及有关初始数据的导入

> Spring 提供 jdbcTemplate 进行有关数据库操作，但在这里我们选择使用 mybatis 框架来做 dao 层（Data Access Object，数据访问对象）

#### 1.  配置 mybatis-generator

![image-20230326220852831](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230326220852831.png)

![image-20230326220940893](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230326220940893.png)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
        "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">
<generatorConfiguration>
    <context id="Mysql" targetRuntime="MyBatis3"  defaultModelType="flat">

        <!-- 自动检查关键字，为关键字增加反引号 -->
        <property name="autoDelimitKeywords" value="true"/>
        <property name="beginningDelimiter" value="`"/>
        <property name="endingDelimiter" value="`"/>

        <!-- 覆盖生成XML文件-->
        <plugin type="org.mybatis.generator.plugins.UnmergeableXmlMappersPlugin" />
        <!-- 生成的实体类添加toString()方法 -->
        <plugin type="org.mybatis.generator.plugins.ToStringPlugin" />

        <!-- 不生成注释 -->
        <commentGenerator>
<!--            <property name="suppressDate" value="true" />-->
            <!-- 是否去除自动生成的注释 true：是 ： false:否 -->
            <property name="suppressAllComments" value="true" />
        </commentGenerator>

        <!-- 数据库链接配置 -->
        <jdbcConnection driverClass="com.mysql.cj.jdbc.Driver"
                        connectionURL="jdbc:mysql://localhost:3306/bysj"
                        userId="root" password="123456">
        </jdbcConnection>

        <!-- domain类的位置 -->
        <javaModelGenerator targetPackage="cn.cetasas.user.pojo"
                            targetProject="src\main\java"/>

        <!-- mapper xml 的位置 -->
        <sqlMapGenerator targetPackage="mapper"
                         targetProject="src\main\resources"/>

        <!-- mapper 类的位置 -->
        <javaClientGenerator type="XMLMAPPER"
                             targetPackage="cn.cetasas.user.mapper"
                             targetProject="src\main\java"/>

        <!-- 需要生成对应pojo、mapper等实体类对应的表 -->
        <table tableName="test"/>
    </context>
</generatorConfiguration>
```

#### 2. 修改爬虫代码(Java 非测试)

##### I 实体类

```java
package cn.cetasas.user.pojo;

import java.util.HashMap;

public class CESData<T> extends HashMap<String, SJ<T>> {

    public CESData() {}

//        private static class Singleton {
//        private static final CESData cesData = new CESData();
//    }
//
//    public static CESData getInstance() {
//        return Singleton.cesData;
//    }
}
```

```java
package cn.cetasas.user.pojo;

import java.util.HashMap;

public class Reg extends HashMap<String, ZB> {

    public Reg() {}
}
```

```java
package cn.cetasas.user.pojo;

import java.util.HashMap;

public class SJ<T> extends HashMap<String, T> {

    public SJ() {}
}
```

```java
package cn.cetasas.user.pojo;

import java.util.HashMap;

public class StandardData<T> {

    private HashMap<String, String> zb;

    private CESData<T> data;

    public HashMap<String, String> getZb() {
        return zb;
    }

    public void setZb(HashMap<String, String> zb) {
        this.zb = zb;
    }

    public CESData<T> getData() {
        return data;
    }

    public void setData(CESData<T> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "StandardData{" +
                "zb=" + zb +
                ", data=" + data +
                '}';
    }
}
```

```java
package cn.cetasas.user.pojo;

public class ZB {
    public double value;

    public ZB() {}

    public ZB(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "{" +
                "value=" + value +
                '}';
    }
}
```

##### II 代码实现

```java
package cn.cetasas.user.util;

import cn.cetasas.user.exception.BusinessException;
import cn.cetasas.user.exception.BusinessExceptionCode;
import cn.cetasas.user.pojo.*;
import cn.cetasas.user.req.QueryReq;
import cn.cetasas.user.resp.CESQueryResp;
import cn.cetasas.user.resp.RegQueryResp;
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
                rCode.setrCode(node.getCode());
                rCode.setrName(node.getName());
                cesData.put(node.getCode(), rCode);
            }
        }
        return cesData;
    }

    /**
     *
     * @param reg 省份地区代码
     * @param sj 时间范围
     * @return 单独某个省份在某个时间范围内的碳排放数据国家统计局处理后的数据）
     */
    public StandardData<Reg> getRegCarbonEmissionStatistics(String reg, String sj) {
        StandardData<Reg> standardData = new StandardData<>();

        CESQueryResp statistics = getOriginalCarbonEmissionStatistics("fsnd", reg, "A0706", sj);

        CESData<Reg> cesData = new CESData<>();
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

        standardData.setData(cesData);

        return standardData;
    }

    private void setCESData_Reg(CESData<Reg> cesData, WD wd, double data) {
        cesData.putIfAbsent(wd.getSj(), new SJ<Reg>());
        SJ<Reg> sj = cesData.get(wd.getSj());
        sj.putIfAbsent(wd.getReg(), new Reg());
        Reg reg = sj.get(wd.getReg());
        reg.put(wd.getZb(), new ZB(data));
    }

    /**
     *
     * @param sj 时间范围
     * @return 全球碳排放在某个时间范围内的数据（国家统计局处理后的数据）
     */
    public StandardData<ZB> getTotalCarbonEmissionStatistics(String sj) {
        StandardData<ZB> standardData = new StandardData<>();

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
                eCode.seteCode(node.getCode());
                eCode.seteName(node.getName());
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
     * @param sj 时间
     * @return 返回某个时间段所有省份的碳排放数据
     */
    public StandardData<Reg> getAllProvinceCES(String sj) {
        StandardData<Reg> standardData = new StandardData<>(); // 返回值
        CESData<Reg> cesData = new CESData<>(); // 所有省份碳排放数据
        Map<String, RCode> regs = getRegCode();
        for (String reg : regs.keySet()) {
            // 某个地区碳排放数据
            CESQueryResp statistics = getOriginalCarbonEmissionStatistics("fsnd", reg, "A0706", sj);

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
        standardData.setData(cesData);
        return standardData;
    }
}
```

#### 3.  表设计

```sql
# 初始化数据库
# 准备 reg code 表和 energy code 表
# ces 原碳排放数据表
CREATE TABLE IF NOT EXISTS `r_code` (
  `r_code` CHAR(6) NOT NULL COMMENT '主键',
  `r_name` VARCHAR(20) COMMENT '名称',
  PRIMARY KEY (`r_code`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '省份代码';

CREATE TABLE IF NOT EXISTS `e_code` (
   `e_code` CHAR(7) NOT NULL COMMENT '主键',
   `e_name` VARCHAR(20) COMMENT '名称',
  PRIMARY KEY (`e_code`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '能源代码';

# 判断 e_code 和 r_code 表是否初始化
DROP TABLE IF EXISTS `init`;
CREATE TABLE IF NOT EXISTS  `init` (
  `table_name` VARCHAR(15) NOT NULL COMMENT '主键',
  `isInit` TINYINT DEFAULT 0 COMMENT '是否初始化',
  PRIMARY KEY (`table_name`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '表是否初始化';

INSERT INTO `init` (`table_name`) VALUES ('r_code');

INSERT INTO `init` (`table_name`) VALUES ('e_code_ees');

INSERT INTO `init` (`table_name`) VALUES ('e_code_total');

# 需要等待 e_code 表初始化完毕后
# 准备碳排放因子表 Carbon Emission Factor => 扩展表
CREATE TABLE IF NOT EXISTS `cef` (
  `e_code` CHAR(7) NOT NULL COMMENT '主键',
  `cef` DOUBLE DEFAULT 0 COMMENT '碳排放因子',
  PRIMARY KEY (`e_code`),
  FOREIGN KEY (`e_code`) REFERENCES `e_code`(`e_code`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '碳排放因子';

# 煤炭
INSERT INTO `cef` (`e_code`, `cef`) VALUES ('A070601', 1.8605);
# 焦炭
INSERT INTO `cef` (`e_code`, `cef`) VALUES ('A070602', 2.9898);
# 原油
INSERT INTO `cef` (`e_code`, `cef`) VALUES ('A070603', 3.0995);
# 汽油
INSERT INTO `cef` (`e_code`, `cef`) VALUES ('A070604', 3.0065);
# 煤油
INSERT INTO `cef` (`e_code`, `cef`) VALUES ('A070605', 3.0989);
# 柴油
INSERT INTO `cef` (`e_code`, `cef`) VALUES ('A070606', 3.1275);
# 燃料油
INSERT INTO `cef` (`e_code`, `cef`) VALUES ('A070607', 3.2769);
# 天然气
INSERT INTO `cef` (`e_code`, `cef`) VALUES ('A070608', 2.1633);
# 电力
INSERT INTO `cef` (`e_code`, `cef`) VALUES ('A070609', 5.8100);

# 现在服务器判断是否存在该表，再创建(包括：XXXX_ces 表和 XXXX_ees 表，即能源排放表和碳排放表)
# ees 省际碳排放数据表
CREATE TABLE IF NOT EXISTS `${tableName}` (
  `r_code` CHAR(6) NOT NULL COMMENT '省份代码',
  `e_code` CHAR(7) NOT NULL COMMENT '能源代码',
  `value` DOUBLE DEFAULT 0 COMMENT '数据',
  FOREIGN KEY (`r_code`) REFERENCES `r_code`(`r_code`),
  FOREIGN KEY (`e_code`) REFERENCES `e_code`(`e_code`),
  PRIMARY KEY (`r_code`, `e_code`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = #{tableName};
# ces 省际能源排放数据表
CREATE TABLE IF NOT EXISTS `${tableName}` (
  `r_code` CHAR(6) NOT NULL COMMENT '省份代码',
  `value` DOUBLE DEFAULT 0 COMMENT '数据',
  FOREIGN KEY (`r_code`) REFERENCES `r_code`(`r_code`),
  PRIMARY KEY (`r_code`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = #{tableName};
# 国家总能源排放数据表
CREATE TABLE IF NOT EXISTS `${tableName}` (
  `e_code` CHAR(7) NOT NULL COMMENT '能源代码',
  `value` DOUBLE DEFAULT 0 COMMENT '数据',
  FOREIGN KEY (`e_code`) REFERENCES `e_code`(`e_code`),
  PRIMARY KEY (`e_code`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = #{tableName};
# 创建 geo 表, 配置 China 的地图信息
CREATE TABLE IF NOT EXISTS  `geo` (
    `nation` VARCHAR(20) NOT NULL COMMENT '主键',
    `geo` LONGTEXT COMMENT '地图信息',
    PRIMARY KEY (`nation`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '地图信息';
```

#### 4. MyBatis重要代码

##### I. TableMapper

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="cn.cetasas.user.mapper.TableMapper">
    <select id="isExitTable" resultType="java.lang.Integer">
        select count(*) from information_schema.TABLES
        where TABLE_NAME = #{tableName};
    </select>

    <update id="createTable" parameterType="java.lang.String" >
        CREATE TABLE IF NOT EXISTS `${tableName}` (
             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
             `name` varchar(50) COMMENT '名称',
             primary key (`id`)
        ) ENGINE = Innodb
          default
          CHARSET = utf8mb4 COMMENT = #{tableName};
    </update>
    <!-- 作用：变量之间驼峰命名法的映射-->
<!--    <resultMap id="ExperimentResultMap" type="cn.cestasas.user.XXX">-->
<!--        <id column="e_id" jdbcType="BIGINT" property="eId" />-->
<!--    </resultMap>-->
</mapper>
```

##### II. CESMapper

> [(1条消息) Mybatis 三种批量插入数据 方式_mybatis 批量插入_FH-Admin的博客-CSDN博客](https://blog.csdn.net/u010253246/article/details/115752049)
>
> <font color='crimson'>通过以上的文章，我们可以得知：在MyBaits中对大量数据使用批处理的效率是最高的，因此在本方案中，我们将采用批处理的形式对碳排放数据的导入</font>
>
> 在MyBatis的foreach标签中，foreach标签是通过拼接SQL语句的方式完成批量操作的。但是当拼接的SQL过多，导致SQL大小超过了MySQL服务器中**max_allowed_packet**变量的值时，会导致操作失败，抛出PacketTooBigException异常。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="cn.cetasas.user.mapper.CESMapper">
    <insert id="insert" parameterType="cn.cetasas.user.pojo.ES" >
        INSERT INTO `${tableName}` (`r_code`, `e_code`, `value`)
        VALUES (#{rCode}, #{eCode}, #{value})
    </insert>
</mapper>
```

故写了一个批处理的工具类

```java
package cn.cetasas.user.util;

import cn.cetasas.user.mapper.CESMapper;
import cn.cetasas.user.mapper.EnergyCodeMapper;
import cn.cetasas.user.mapper.RegCodeMapper;
import cn.cetasas.user.pojo.CES;
import cn.cetasas.user.pojo.EnergyCode;
import cn.cetasas.user.pojo.RegCode;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;

@Component
public class InsertBatch {
    private static final Logger LOG = LoggerFactory.getLogger(InsertBatch.class);
    
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    
    public void insertBatch_CES(Collection<CES> ces) {
        long start = System.currentTimeMillis();
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        CESMapper mapper = sqlSession.getMapper(CESMapper.class);
        ces.forEach(mapper::insert);
        long end = System.currentTimeMillis();
        sqlSession.commit();
        sqlSession.clearCache();
        LOG.info("共耗时：{}", start - end);
    }

    public void insertBatch_RegCode(Collection<RegCode> regCodes) {
        long start = System.currentTimeMillis();
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        RegCodeMapper mapper = sqlSession.getMapper(RegCodeMapper.class);
        regCodes.forEach(mapper::insert);
        long end = System.currentTimeMillis();
        sqlSession.commit();
        sqlSession.clearCache();
        LOG.info("共耗时：{}", start - end);
    }

    public void insertBatch_EnergyCode(Collection<EnergyCode> energyCodes) {
        long start = System.currentTimeMillis();
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        EnergyCodeMapper mapper = sqlSession.getMapper(EnergyCodeMapper.class);
        energyCodes.forEach(mapper::insert);
        long end = System.currentTimeMillis();
        sqlSession.commit();
        sqlSession.clearCache();
        LOG.info("共耗时：{}", start - end);
    }
}
```

#### 5. 一些常用的SQL语句

```sql
# (1) 判断数据库是否存在 精准查询
select * from information_schema.SCHEMATA
where SCHEMA_NAME = '需要查找的数据库名'
# (2) 判断数据库是否存在 模糊查询
select * from information_schema.SCHEMATA
where SCHEMA_NAME like '%需要查询的数据库名的部分名称%' -- 但是索引失效，查询效率低下
# (3) 判断数据表是否存在 精准查询
select * from information_schema.TABLES
where TABLE_NAME = '需要查找的数据表名'
# (4) 判断数据表是否存在 模糊查询
select * from information_schema.TABLES
where TABLE_NAME like '%需要查询的数据表名的部分名称%'

# 进阶
select * from information_schema.TABLES
where TABLE_SCHEMA = 'dbName' and
	  TABLE_NAME = 'tableName';
```

## 四、计算碳排放

### 1）能源碳排放数据初始化

```java
private void initCES() {
    // 获取所有省份碳排放数据
    StandardData<Reg> standardData = carbonEmissionStatistics.getAllProvinceCES("");
    HashMap<String, ECode> ZB = standardData.getZb();
    CESData<Reg> data = standardData.getData();
    // 获取地区代码映射表
    Map<String, RCode> REG = carbonEmissionStatistics.getRegCode();

    LOG.info("所有省份的碳排放数据：{}", data);
    LOG.info("所有省份的对应代码：{}", REG);
    LOG.info("所有省份能源排放的指标代码：{}", ZB);

    // 1) 初始化地区代码
    LOG.info("正在初始化r_code表，导入{}条数据", REG.values().size());
    insertBatch.insertBatch_RegCode(REG.values());

    // 2) 初始化能源代码
    LOG.info("正在初始化e_code表，导入{}条数据", ZB.values().size());
    insertBatch.insertBatch_EnergyCode(ZB.values());

    // 3) 创建省份能源排放表，以及导入数据
    for (String sj : data.keySet()) {
        String tableName = sj + "_ees";
        if (tableMapper.isExitTable(tableName) == 0) {
            LOG.info("正在创建表【{}】...", tableName);
            tableMapper.createCESTable(tableName);
            LOG.info("表【{}】创建完成...", tableName);
        }
        List<ES> esList = new ArrayList<>();
        SJ<Reg> reg = data.get(sj);
        for (String reg_code : reg.keySet()) {
            Reg zb = reg.get(reg_code);
            for (String zb_code : zb.keySet()) {
                ES ces = new ES();
                ces.setTableName(tableName);
                ces.setRCode(reg_code);
                ces.setECode(zb_code);
                ces.setValue(zb.get(zb_code).getValue());

                esList.add(ces);
            }
        }
        LOG.info("正在导入{}条数据", esList.size());
        insertBatch.insertBatch_ES(esList);
    }
}
```

### 2）计算碳排放数据

```java
private List<CES> calc(List<EES> datas) {
    Map<String, CES> map = new HashMap<>();

    for (EES data : datas) {
        CES ces = null;
        String code = data.getRCode();
        if (map.containsKey(code)) {
            ces = map.get(code);
        } else {
            ces = new CES();
            ces.setRCode(code);
            ces.setReg(data.getReg());

            map.put(code, ces);
        }

        double value = ces.getValue();
        value += data.getValue() * data.getCef();
        ces.setValue(value);
    }

    // 构建返回值
    return new ArrayList<>(map.values());
}
```

## 五、界面设计

### 1）首页

### 2）时空分析图

![image-20230421004400100](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230421004400100.png)

![image-20230421004414903](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230421004414903.png)

![image-20230421004441518](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230421004441518.png)

![image-20230421004455768](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230421004455768.png)

### 3）数据下载

> 只有登录后的账户才有权利下载数据

![image-20230421004528361](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230421004528361.png)

![image-20230421004546046](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230421004546046.png)

### 4）消息中心



### 5）我的账户



## 六、界面实现

> [如何删除vue-cli保存的自定义项目配置_vuecli怎么删除项目_insomnia_war的博客-CSDN博客](https://blog.csdn.net/w731030162/article/details/118229677#:~:text=打开资源管理器，在如下文件夹下有一个名为. vue rc的文件，里面 保存 着预设信息。,打开文件，可以看到预设信息 清除掉所有预设 再次创建就没有 自定义 的预设了)

### 1）Vue + Vue CLI 项目搭建

#### 1. 查看版本，修改镜像

```
# 查看 vue 的版本
vue --version
# 显示当前的镜像网址
npm get reigstry
# 使用淘宝的镜像网址
npm config set registry http://registry.npm.taobao.org
# 安装 vue 脚手架
npm install -g @vue/cli
npm install -g @vue/cli@4.5.9
# 查看安装过的模块
npm ls
```

#### 2. 创建前端项目

`vue create web`

![image-20211109180059575](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20211109180059575.png)

![image-20211109180125755](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20211109180125755.png)

![image-20211109180705261](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20211109180705261.png)

#### 3. vue 项目启动

> package.json 是 vue 项目的启动文件

![image-20211109183509934](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20211109183509934.png)

![image-20211109183520696](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20211109183520696.png)

#### 4. router 里的懒加载

> 只有访问到这个路径时，才会去加载其相关代码
>
> 可以使用懒加载，减少前端DOM的压力

![image-20211109184218804](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20211109184218804.png)

#### 5. vue 项目结构

![image-20211109184844084](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20211109184844084.png)

### 2）地图页面初步实现

> echarts 配置项手册：[Documentation - Apache ECharts](https://echarts.apache.org/zh/option.html#title)

#### 1. 引入 echarts 模块

```
# 安装 echarts 模块
npm install echarts -s
```

#### 2. 地图页面初步实现

> 参考文献：[用Echarts绘制地图-绘制省级地图 - 知乎 (zhihu.com)](https://zhuanlan.zhihu.com/p/128553525)
>
> geo数据：[DataV.GeoAtlas地理小工具系列 (aliyun.com)](http://datav.aliyun.com/portal/school/atlas/area_selector#&lat=30.230594564932193&lng=98.316650390625&zoom=6)

##### I echarts 官网测试案例 USA

```js
usaJson = {"type":"FeatureCollection","features":[
  {"type":"Feature","id":"01","properties":{"name":"Alabama"},"geometry":{"type":"Polygon","coordinates":[[[-87.359296,35.00118],[-85.606675,34.984749],[-85.431413,34.124869],[-85.184951,32.859696],[-85.069935,32.580372],[-84.960397,32.421541],[-85.004212,32.322956],[-84.889196,32.262709],[-85.058981,32.13674],[-85.053504,32.01077],[-85.141136,31.840985],[-85.042551,31.539753],[-85.113751,31.27686],[-85.004212,31.003013],[-85.497137,30.997536],[-87.600282,30.997536],[-87.633143,30.86609],[-87.408589,30.674397],[-87.446927,30.510088],[-87.37025,30.427934],[-87.518128,30.280057],[-87.655051,30.247195],[-87.90699,30.411504],[-87.934375,30.657966],[-88.011052,30.685351],[-88.10416,30.499135],[-88.137022,30.318396],[-88.394438,30.367688],[-88.471115,31.895754],[-88.241084,33.796253],[-88.098683,34.891641],[-88.202745,34.995703],[-87.359296,35.00118]]]}},
  {"type":"Feature","id":"02","properties":{"name":"Alaska"},"geometry":{"type":"MultiPolygon","coordinates":[[[[-131.602021,55.117982],[-131.569159,55.28229],[-131.355558,55.183705],[-131.38842,55.01392],[-131.645836,55.035827],[-131.602021,55.117982]]],[[[-131.832052,55.42469],[-131.645836,55.304197],[-131.749898,55.128935],[-131.832052,55.189182],[-131.832052,55.42469]]],[[[-132.976733,56.437924],[-132.735747,56.459832],[-132.631685,56.421493],[-132.664547,56.273616],[-132.878148,56.240754],[-133.069841,56.333862],[-132.976733,56.437924]]],[[[-133.595627,56.350293],[-133.162949,56.317431],[-133.05341,56.125739],[-132.620732,55.912138],[-132.472854,55.780691],[-132.4619,55.671152],[-132.357838,55.649245],[-132.341408,55.506844],[-132.166146,55.364444],[-132.144238,55.238474],[-132.029222,55.276813],[-131.97993,55.178228],[-131.958022,54.789365],[-132.029222,54.701734],[-132.308546,54.718165],[-132.385223,54.915335],[-132.483808,54.898904],[-132.686455,55.046781],[-132.746701,54.997489],[-132.916486,55.046781],[-132.889102,54.898904],[-132.73027,54.937242],[-132.626209,54.882473],[-132.675501,54.679826],[-132.867194,54.701734],[-133.157472,54.95915],[-133.239626,55.090597],[-133.223195,55.22752],[-133.453227,55.216566],[-133.453227,55.320628],[-133.277964,55.331582],[-133.102702,55.42469],[-133.17938,55.588998],[-133.387503,55.62186],[-133.420365,55.884753],[-133.497042,56.0162],[-133.639442,55.923092],[-133.694212,56.070969],[-133.546335,56.142169],[-133.666827,56.311955],[-133.595627,56.350293]]],[[[-133.738027,55.556137],[-133.546335,55.490413],[-133.414888,55.572568],[-133.283441,55.534229],[-133.420365,55.386352],[-133.633966,55.430167],[-133.738027,55.556137]]],[[[-133.907813,56.930849],[-134.050213,57.029434],[-133.885905,57.095157],[-133.343688,57.002049],[-133.102702,57.007526],[-132.932917,56.82131],[-132.620732,56.667956],[-132.653593,56.55294],[-132.817901,56.492694],[-133.042456,56.520078],[-133.201287,56.448878],[-133.420365,56.492694],[-133.66135,56.448878],[-133.710643,56.684386],[-133.688735,56.837741],[-133.869474,56.843218],[-133.907813,56.930849]]],[[[-134.115936,56.48174],[-134.25286,56.558417],[-134.400737,56.722725],[-134.417168,56.848695],[-134.296675,56.908941],[-134.170706,56.848695],[-134.143321,56.952757],[-133.748981,56.772017],[-133.710643,56.596755],[-133.847566,56.574848],[-133.935197,56.377678],[-133.836612,56.322908],[-133.957105,56.092877],[-134.110459,56.142169],[-134.132367,55.999769],[-134.230952,56.070969],[-134.291198,56.350293],[-134.115936,56.48174]]],[[[-134.636246,56.28457],[-134.669107,56.169554],[-134.806031,56.235277],[-135.178463,56.67891],[-135.413971,56.810356],[-135.331817,56.914418],[-135.424925,57.166357],[-135.687818,57.369004],[-135.419448,57.566174],[-135.298955,57.48402],[-135.063447,57.418296],[-134.849846,57.407343],[-134.844369,57.248511],[-134.636246,56.728202],[-134.636246,56.28457]]],[[[-134.712923,58.223407],[-134.373353,58.14673],[-134.176183,58.157683],[-134.187137,58.081006],[-133.902336,57.807159],[-134.099505,57.850975],[-134.148798,57.757867],[-133.935197,57.615466],[-133.869474,57.363527],[-134.083075,57.297804],[-134.154275,57.210173],[-134.499322,57.029434],[-134.603384,57.034911],[-134.6472,57.226604],[-134.575999,57.341619],[-134.608861,57.511404],[-134.729354,57.719528],[-134.707446,57.829067],[-134.784123,58.097437],[-134.91557,58.212453],[-134.953908,58.409623],[-134.712923,58.223407]]],[[[-135.857603,57.330665],[-135.715203,57.330665],[-135.567326,57.149926],[-135.633049,57.023957],[-135.857603,56.996572],[-135.824742,57.193742],[-135.857603,57.330665]]],[[[-136.279328,58.206976],[-135.978096,58.201499],[-135.780926,58.28913],[-135.496125,58.168637],[-135.64948,58.037191],[-135.59471,57.987898],[-135.45231,58.135776],[-135.107263,58.086483],[-134.91557,57.976944],[-135.025108,57.779775],[-134.937477,57.763344],[-134.822462,57.500451],[-135.085355,57.462112],[-135.572802,57.675713],[-135.556372,57.456635],[-135.709726,57.369004],[-135.890465,57.407343],[-136.000004,57.544266],[-136.208128,57.637374],[-136.366959,57.829067],[-136.569606,57.916698],[-136.558652,58.075529],[-136.421728,58.130299],[-136.377913,58.267222],[-136.279328,58.206976]]],[[[-147.079854,60.200582],[-147.501579,59.948643],[-147.53444,59.850058],[-147.874011,59.784335],[-147.80281,59.937689],[-147.435855,60.09652],[-147.205824,60.271782],[-147.079854,60.200582]]],[[[-147.561825,60.578491],[-147.616594,60.370367],[-147.758995,60.156767],[-147.956165,60.227967],[-147.791856,60.474429],[-147.561825,60.578491]]],[[[-147.786379,70.245291],[-147.682318,70.201475],[-147.162008,70.15766],[-146.888161,70.185044],[-146.510252,70.185044],[-146.099482,70.146706],[-145.858496,70.168614],[-145.622988,70.08646],[-145.195787,69.993352],[-144.620708,69.971444],[-144.461877,70.026213],[-144.078491,70.059075],[-143.914183,70.130275],[-143.497935,70.141229],[-143.503412,70.091936],[-143.25695,70.119321],[-142.747594,70.042644],[-142.402547,69.916674],[-142.079408,69.856428],[-142.008207,69.801659],[-141.712453,69.790705],[-141.433129,69.697597],[-141.378359,69.63735],[-141.208574,69.686643],[-141.00045,69.648304],[-141.00045,60.304644],[-140.53491,60.22249],[-140.474664,60.310121],[-139.987216,60.184151],[-139.696939,60.342983],[-139.088998,60.359413],[-139.198537,60.091043],[-139.045183,59.997935],[-138.700135,59.910304],[-138.623458,59.767904],[-137.604747,59.242118],[-137.445916,58.908024],[-137.265177,59.001132],[-136.827022,59.159963],[-136.580559,59.16544],[-136.465544,59.285933],[-136.476498,59.466672],[-136.301236,59.466672],[-136.25742,59.625503],[-135.945234,59.663842],[-135.479694,59.800766],[-135.025108,59.565257],[-135.068924,59.422857],[-134.959385,59.280456],[-134.701969,59.247595],[-134.378829,59.033994],[-134.400737,58.973748],[-134.25286,58.858732],[-133.842089,58.727285],[-133.173903,58.152206],[-133.075318,57.998852],[-132.867194,57.845498],[-132.560485,57.505928],[-132.253777,57.21565],[-132.368792,57.095157],[-132.05113,57.051341],[-132.127807,56.876079],[-131.870391,56.804879],[-131.837529,56.602232],[-131.580113,56.613186],[-131.087188,56.405062],[-130.78048,56.366724],[-130.621648,56.268139],[-130.468294,56.240754],[-130.424478,56.142169],[-130.101339,56.114785],[-130.002754,55.994292],[-130.150631,55.769737],[-130.128724,55.583521],[-129.986323,55.276813],[-130.095862,55.200136],[-130.336847,54.920812],[-130.687372,54.718165],[-130.785957,54.822227],[-130.917403,54.789365],[-131.010511,54.997489],[-130.983126,55.08512],[-131.092665,55.189182],[-130.862634,55.298721],[-130.928357,55.337059],[-131.158389,55.200136],[-131.284358,55.287767],[-131.426759,55.238474],[-131.843006,55.457552],[-131.700606,55.698537],[-131.963499,55.616383],[-131.974453,55.49589],[-132.182576,55.588998],[-132.226392,55.704014],[-132.083991,55.829984],[-132.127807,55.955953],[-132.324977,55.851892],[-132.522147,56.076446],[-132.642639,56.032631],[-132.719317,56.218847],[-132.527624,56.339339],[-132.341408,56.339339],[-132.396177,56.487217],[-132.297592,56.67891],[-132.450946,56.673433],[-132.768609,56.837741],[-132.993164,57.034911],[-133.51895,57.177311],[-133.507996,57.577128],[-133.677781,57.62642],[-133.639442,57.790728],[-133.814705,57.834544],[-134.072121,58.053622],[-134.143321,58.168637],[-134.586953,58.206976],[-135.074401,58.502731],[-135.282525,59.192825],[-135.38111,59.033994],[-135.337294,58.891593],[-135.140124,58.617746],[-135.189417,58.573931],[-135.05797,58.349376],[-135.085355,58.201499],[-135.277048,58.234361],[-135.430402,58.398669],[-135.633049,58.426053],[-135.91785,58.382238],[-135.912373,58.617746],[-136.087635,58.814916],[-136.246466,58.75467],[-136.876314,58.962794],[-136.931084,58.902547],[-136.586036,58.836824],[-136.317666,58.672516],[-136.213604,58.667039],[-136.180743,58.535592],[-136.043819,58.382238],[-136.388867,58.294607],[-136.591513,58.349376],[-136.59699,58.212453],[-136.859883,58.316515],[-136.947514,58.393192],[-137.111823,58.393192],[-137.566409,58.590362],[-137.900502,58.765624],[-137.933364,58.869686],[-138.11958,59.02304],[-138.634412,59.132579],[-138.919213,59.247595],[-139.417615,59.379041],[-139.746231,59.505011],[-139.718846,59.641934],[-139.625738,59.598119],[-139.5162,59.68575],[-139.625738,59.88292],[-139.488815,59.992458],[-139.554538,60.041751],[-139.801,59.833627],[-140.315833,59.696704],[-140.92925,59.745996],[-141.444083,59.871966],[-141.46599,59.970551],[-141.706976,59.948643],[-141.964392,60.019843],[-142.539471,60.085566],[-142.873564,60.091043],[-143.623905,60.036274],[-143.892275,59.997935],[-144.231845,60.140336],[-144.65357,60.206059],[-144.785016,60.29369],[-144.834309,60.441568],[-145.124586,60.430614],[-145.223171,60.299167],[-145.738004,60.474429],[-145.820158,60.551106],[-146.351421,60.408706],[-146.608837,60.238921],[-146.718376,60.397752],[-146.608837,60.485383],[-146.455483,60.463475],[-145.951604,60.578491],[-146.017328,60.666122],[-146.252836,60.622307],[-146.345944,60.737322],[-146.565022,60.753753],[-146.784099,61.044031],[-146.866253,60.972831],[-147.172962,60.934492],[-147.271547,60.972831],[-147.375609,60.879723],[-147.758995,60.912584],[-147.775426,60.808523],[-148.032842,60.781138],[-148.153334,60.819476],[-148.065703,61.005692],[-148.175242,61.000215],[-148.350504,60.803046],[-148.109519,60.737322],[-148.087611,60.594922],[-147.939734,60.441568],[-148.027365,60.277259],[-148.219058,60.332029],[-148.273827,60.249875],[-148.087611,60.217013],[-147.983549,59.997935],[-148.251919,59.95412],[-148.399797,59.997935],[-148.635305,59.937689],[-148.755798,59.986981],[-149.067984,59.981505],[-149.05703,60.063659],[-149.204907,60.008889],[-149.287061,59.904827],[-149.418508,59.997935],[-149.582816,59.866489],[-149.511616,59.806242],[-149.741647,59.729565],[-149.949771,59.718611],[-150.031925,59.61455],[-150.25648,59.521442],[-150.409834,59.554303],[-150.579619,59.444764],[-150.716543,59.450241],[-151.001343,59.225687],[-151.308052,59.209256],[-151.406637,59.280456],[-151.592853,59.159963],[-151.976239,59.253071],[-151.888608,59.422857],[-151.636669,59.483103],[-151.47236,59.472149],[-151.423068,59.537872],[-151.127313,59.669319],[-151.116359,59.778858],[-151.505222,59.63098],[-151.828361,59.718611],[-151.8667,59.778858],[-151.702392,60.030797],[-151.423068,60.211536],[-151.379252,60.359413],[-151.297098,60.386798],[-151.264237,60.545629],[-151.406637,60.720892],[-151.06159,60.786615],[-150.404357,61.038554],[-150.245526,60.939969],[-150.042879,60.912584],[-149.741647,61.016646],[-150.075741,61.15357],[-150.207187,61.257632],[-150.47008,61.246678],[-150.656296,61.29597],[-150.711066,61.252155],[-151.023251,61.180954],[-151.165652,61.044031],[-151.477837,61.011169],[-151.800977,60.852338],[-151.833838,60.748276],[-152.080301,60.693507],[-152.13507,60.578491],[-152.310332,60.507291],[-152.392486,60.304644],[-152.732057,60.173197],[-152.567748,60.069136],[-152.704672,59.915781],[-153.022334,59.888397],[-153.049719,59.691227],[-153.345474,59.620026],[-153.438582,59.702181],[-153.586459,59.548826],[-153.761721,59.543349],[-153.72886,59.433811],[-154.117723,59.368087],[-154.1944,59.066856],[-153.750768,59.050425],[-153.400243,58.968271],[-153.301658,58.869686],[-153.444059,58.710854],[-153.679567,58.612269],[-153.898645,58.606793],[-153.920553,58.519161],[-154.062953,58.4863],[-153.99723,58.376761],[-154.145107,58.212453],[-154.46277,58.059098],[-154.643509,58.059098],[-154.818771,58.004329],[-154.988556,58.015283],[-155.120003,57.955037],[-155.081664,57.872883],[-155.328126,57.829067],[-155.377419,57.708574],[-155.547204,57.785251],[-155.73342,57.549743],[-156.045606,57.566174],[-156.023698,57.440204],[-156.209914,57.473066],[-156.34136,57.418296],[-156.34136,57.248511],[-156.549484,56.985618],[-156.883577,56.952757],[-157.157424,56.832264],[-157.20124,56.766541],[-157.376502,56.859649],[-157.672257,56.607709],[-157.754411,56.67891],[-157.918719,56.657002],[-157.957058,56.514601],[-158.126843,56.459832],[-158.32949,56.48174],[-158.488321,56.339339],[-158.208997,56.295524],[-158.510229,55.977861],[-159.375585,55.873799],[-159.616571,55.594475],[-159.676817,55.654722],[-159.643955,55.829984],[-159.813741,55.857368],[-160.027341,55.791645],[-160.060203,55.720445],[-160.394296,55.605429],[-160.536697,55.473983],[-160.580512,55.567091],[-160.668143,55.457552],[-160.865313,55.528752],[-161.232268,55.358967],[-161.506115,55.364444],[-161.467776,55.49589],[-161.588269,55.62186],[-161.697808,55.517798],[-161.686854,55.408259],[-162.053809,55.074166],[-162.179779,55.15632],[-162.218117,55.03035],[-162.470057,55.052258],[-162.508395,55.249428],[-162.661749,55.293244],[-162.716519,55.222043],[-162.579595,55.134412],[-162.645319,54.997489],[-162.847965,54.926289],[-163.00132,55.079643],[-163.187536,55.090597],[-163.220397,55.03035],[-163.034181,54.942719],[-163.373752,54.800319],[-163.14372,54.76198],[-163.138243,54.696257],[-163.329936,54.74555],[-163.587352,54.614103],[-164.085754,54.61958],[-164.332216,54.531949],[-164.354124,54.466226],[-164.638925,54.389548],[-164.847049,54.416933],[-164.918249,54.603149],[-164.710125,54.663395],[-164.551294,54.88795],[-164.34317,54.893427],[-163.894061,55.041304],[-163.532583,55.046781],[-163.39566,54.904381],[-163.291598,55.008443],[-163.313505,55.128935],[-163.105382,55.183705],[-162.880827,55.183705],[-162.579595,55.446598],[-162.245502,55.682106],[-161.807347,55.89023],[-161.292514,55.983338],[-161.078914,55.939523],[-160.87079,55.999769],[-160.816021,55.912138],[-160.931036,55.813553],[-160.805067,55.736876],[-160.766728,55.857368],[-160.509312,55.868322],[-160.438112,55.791645],[-160.27928,55.76426],[-160.273803,55.857368],[-160.536697,55.939523],[-160.558604,55.994292],[-160.383342,56.251708],[-160.147834,56.399586],[-159.830171,56.541986],[-159.326293,56.667956],[-158.959338,56.848695],[-158.784076,56.782971],[-158.641675,56.810356],[-158.701922,56.925372],[-158.658106,57.034911],[-158.378782,57.264942],[-157.995396,57.41282],[-157.688688,57.609989],[-157.705118,57.719528],[-157.458656,58.497254],[-157.07527,58.705377],[-157.119086,58.869686],[-158.039212,58.634177],[-158.32949,58.661562],[-158.40069,58.760147],[-158.564998,58.803962],[-158.619768,58.913501],[-158.767645,58.864209],[-158.860753,58.694424],[-158.701922,58.480823],[-158.893615,58.387715],[-159.0634,58.420577],[-159.392016,58.760147],[-159.616571,58.929932],[-159.731586,58.929932],[-159.808264,58.803962],[-159.906848,58.782055],[-160.054726,58.886116],[-160.235465,58.902547],[-160.317619,59.072332],[-160.854359,58.88064],[-161.33633,58.743716],[-161.374669,58.667039],[-161.752577,58.552023],[-161.938793,58.656085],[-161.769008,58.776578],[-161.829255,59.061379],[-161.955224,59.36261],[-161.703285,59.48858],[-161.911409,59.740519],[-162.092148,59.88292],[-162.234548,60.091043],[-162.448149,60.178674],[-162.502918,59.997935],[-162.760334,59.959597],[-163.171105,59.844581],[-163.66403,59.795289],[-163.9324,59.806242],[-164.162431,59.866489],[-164.189816,60.02532],[-164.386986,60.074613],[-164.699171,60.29369],[-164.962064,60.337506],[-165.268773,60.578491],[-165.060649,60.68803],[-165.016834,60.890677],[-165.175665,60.846861],[-165.197573,60.972831],[-165.120896,61.076893],[-165.323543,61.170001],[-165.34545,61.071416],[-165.591913,61.109754],[-165.624774,61.279539],[-165.816467,61.301447],[-165.920529,61.416463],[-165.915052,61.558863],[-166.106745,61.49314],[-166.139607,61.630064],[-165.904098,61.662925],[-166.095791,61.81628],[-165.756221,61.827233],[-165.756221,62.013449],[-165.674067,62.139419],[-165.044219,62.539236],[-164.912772,62.659728],[-164.819664,62.637821],[-164.874433,62.807606],[-164.633448,63.097884],[-164.425324,63.212899],[-164.036462,63.262192],[-163.73523,63.212899],[-163.313505,63.037637],[-163.039658,63.059545],[-162.661749,63.22933],[-162.272887,63.486746],[-162.075717,63.514131],[-162.026424,63.448408],[-161.555408,63.448408],[-161.13916,63.503177],[-160.766728,63.771547],[-160.766728,63.837271],[-160.952944,64.08921],[-160.974852,64.237087],[-161.26513,64.395918],[-161.374669,64.532842],[-161.078914,64.494503],[-160.79959,64.609519],[-160.783159,64.719058],[-161.144637,64.921705],[-161.413007,64.762873],[-161.664946,64.790258],[-161.900455,64.702627],[-162.168825,64.680719],[-162.234548,64.620473],[-162.541257,64.532842],[-162.634365,64.384965],[-162.787719,64.324718],[-162.858919,64.49998],[-163.045135,64.538319],[-163.176582,64.401395],[-163.253259,64.467119],[-163.598306,64.565704],[-164.304832,64.560227],[-164.80871,64.450688],[-165.000403,64.434257],[-165.411174,64.49998],[-166.188899,64.576658],[-166.391546,64.636904],[-166.484654,64.735489],[-166.413454,64.872412],[-166.692778,64.987428],[-166.638008,65.113398],[-166.462746,65.179121],[-166.517516,65.337952],[-166.796839,65.337952],[-167.026871,65.381768],[-167.47598,65.414629],[-167.711489,65.496784],[-168.072967,65.578938],[-168.105828,65.682999],[-167.541703,65.819923],[-166.829701,66.049954],[-166.3313,66.186878],[-166.046499,66.110201],[-165.756221,66.09377],[-165.690498,66.203309],[-165.86576,66.21974],[-165.88219,66.312848],[-165.186619,66.466202],[-164.403417,66.581218],[-163.981692,66.592172],[-163.751661,66.553833],[-163.872153,66.389525],[-163.828338,66.274509],[-163.915969,66.192355],[-163.768091,66.060908],[-163.494244,66.082816],[-163.149197,66.060908],[-162.749381,66.088293],[-162.634365,66.039001],[-162.371472,66.028047],[-162.14144,66.077339],[-161.840208,66.02257],[-161.549931,66.241647],[-161.341807,66.252601],[-161.199406,66.208786],[-161.128206,66.334755],[-161.528023,66.395002],[-161.911409,66.345709],[-161.87307,66.510017],[-162.174302,66.68528],[-162.502918,66.740049],[-162.601503,66.89888],[-162.344087,66.937219],[-162.015471,66.778388],[-162.075717,66.652418],[-161.916886,66.553833],[-161.571838,66.438817],[-161.489684,66.55931],[-161.884024,66.718141],[-161.714239,67.002942],[-161.851162,67.052235],[-162.240025,66.991988],[-162.639842,67.008419],[-162.700088,67.057712],[-162.902735,67.008419],[-163.740707,67.128912],[-163.757138,67.254881],[-164.009077,67.534205],[-164.211724,67.638267],[-164.534863,67.725898],[-165.192096,67.966884],[-165.493328,68.059992],[-165.794559,68.081899],[-166.243668,68.246208],[-166.681824,68.339316],[-166.703731,68.372177],[-166.375115,68.42147],[-166.227238,68.574824],[-166.216284,68.881533],[-165.329019,68.859625],[-164.255539,68.930825],[-163.976215,68.985595],[-163.532583,69.138949],[-163.110859,69.374457],[-163.023228,69.609966],[-162.842489,69.812613],[-162.470057,69.982398],[-162.311225,70.108367],[-161.851162,70.311014],[-161.779962,70.256245],[-161.396576,70.239814],[-160.837928,70.343876],[-160.487404,70.453415],[-159.649432,70.792985],[-159.33177,70.809416],[-159.298908,70.760123],[-158.975769,70.798462],[-158.658106,70.787508],[-158.033735,70.831323],[-157.420318,70.979201],[-156.812377,71.285909],[-156.565915,71.351633],[-156.522099,71.296863],[-155.585543,71.170894],[-155.508865,71.083263],[-155.832005,70.968247],[-155.979882,70.96277],[-155.974405,70.809416],[-155.503388,70.858708],[-155.476004,70.940862],[-155.262403,71.017539],[-155.191203,70.973724],[-155.032372,71.148986],[-154.566832,70.990155],[-154.643509,70.869662],[-154.353231,70.8368],[-154.183446,70.7656],[-153.931507,70.880616],[-153.487874,70.886093],[-153.235935,70.924431],[-152.589656,70.886093],[-152.26104,70.842277],[-152.419871,70.606769],[-151.817408,70.546523],[-151.773592,70.486276],[-151.187559,70.382214],[-151.182082,70.431507],[-150.760358,70.49723],[-150.355064,70.491753],[-150.349588,70.436984],[-150.114079,70.431507],[-149.867617,70.508184],[-149.462323,70.519138],[-149.177522,70.486276],[-148.78866,70.404122],[-148.607921,70.420553],[-148.350504,70.305537],[-148.202627,70.349353],[-147.961642,70.316491],[-147.786379,70.245291]]],[[[-152.94018,58.026237],[-152.945657,57.982421],[-153.290705,58.048145],[-153.044242,58.305561],[-152.819688,58.327469],[-152.666333,58.562977],[-152.496548,58.354853],[-152.354148,58.426053],[-152.080301,58.311038],[-152.080301,58.152206],[-152.480117,58.130299],[-152.655379,58.059098],[-152.94018,58.026237]]],[[[-153.958891,57.538789],[-153.67409,57.670236],[-153.931507,57.69762],[-153.936983,57.812636],[-153.723383,57.889313],[-153.570028,57.834544],[-153.548121,57.719528],[-153.46049,57.796205],[-153.455013,57.96599],[-153.268797,57.889313],[-153.235935,57.998852],[-153.071627,57.933129],[-152.874457,57.933129],[-152.721103,57.993375],[-152.469163,57.889313],[-152.469163,57.599035],[-152.151501,57.620943],[-152.359625,57.42925],[-152.74301,57.505928],[-152.60061,57.379958],[-152.710149,57.275896],[-152.907319,57.325188],[-152.912796,57.128019],[-153.214027,57.073249],[-153.312612,56.991095],[-153.498828,57.067772],[-153.695998,56.859649],[-153.849352,56.837741],[-154.013661,56.744633],[-154.073907,56.969187],[-154.303938,56.848695],[-154.314892,56.919895],[-154.523016,56.991095],[-154.539447,57.193742],[-154.742094,57.275896],[-154.627078,57.511404],[-154.227261,57.659282],[-153.980799,57.648328],[-153.958891,57.538789]]],[[[-154.53397,56.602232],[-154.742094,56.399586],[-154.807817,56.432447],[-154.53397,56.602232]]],[[[-155.634835,55.923092],[-155.476004,55.912138],[-155.530773,55.704014],[-155.793666,55.731399],[-155.837482,55.802599],[-155.634835,55.923092]]],[[[-159.890418,55.28229],[-159.950664,55.068689],[-160.257373,54.893427],[-160.109495,55.161797],[-160.005433,55.134412],[-159.890418,55.28229]]],[[[-160.520266,55.358967],[-160.33405,55.358967],[-160.339527,55.249428],[-160.525743,55.128935],[-160.690051,55.211089],[-160.794113,55.134412],[-160.854359,55.320628],[-160.79959,55.380875],[-160.520266,55.358967]]],[[[-162.256456,54.981058],[-162.234548,54.893427],[-162.349564,54.838658],[-162.437195,54.931766],[-162.256456,54.981058]]],[[[-162.415287,63.634624],[-162.563165,63.536039],[-162.612457,63.62367],[-162.415287,63.634624]]],[[[-162.80415,54.488133],[-162.590549,54.449795],[-162.612457,54.367641],[-162.782242,54.373118],[-162.80415,54.488133]]],[[[-165.548097,54.29644],[-165.476897,54.181425],[-165.630251,54.132132],[-165.685021,54.252625],[-165.548097,54.29644]]],[[[-165.73979,54.15404],[-166.046499,54.044501],[-166.112222,54.121178],[-165.980775,54.219763],[-165.73979,54.15404]]],[[[-166.364161,60.359413],[-166.13413,60.397752],[-166.084837,60.326552],[-165.88219,60.342983],[-165.685021,60.277259],[-165.646682,59.992458],[-165.750744,59.89935],[-166.00816,59.844581],[-166.062929,59.745996],[-166.440838,59.855535],[-166.6161,59.850058],[-166.994009,59.992458],[-167.125456,59.992458],[-167.344534,60.074613],[-167.421211,60.206059],[-167.311672,60.238921],[-166.93924,60.206059],[-166.763978,60.310121],[-166.577762,60.321075],[-166.495608,60.392275],[-166.364161,60.359413]]],[[[-166.375115,54.01164],[-166.210807,53.934962],[-166.5449,53.748746],[-166.539423,53.715885],[-166.117699,53.852808],[-166.112222,53.776131],[-166.282007,53.683023],[-166.555854,53.622777],[-166.583239,53.529669],[-166.878994,53.431084],[-167.13641,53.425607],[-167.306195,53.332499],[-167.623857,53.250345],[-167.793643,53.337976],[-167.459549,53.442038],[-167.355487,53.425607],[-167.103548,53.513238],[-167.163794,53.611823],[-167.021394,53.715885],[-166.807793,53.666592],[-166.785886,53.732316],[-167.015917,53.754223],[-167.141887,53.825424],[-167.032348,53.945916],[-166.643485,54.017116],[-166.561331,53.880193],[-166.375115,54.01164]]],[[[-168.790446,53.157237],[-168.40706,53.34893],[-168.385152,53.431084],[-168.237275,53.524192],[-168.007243,53.568007],[-167.886751,53.518715],[-167.842935,53.387268],[-168.270136,53.244868],[-168.500168,53.036744],[-168.686384,52.965544],[-168.790446,53.157237]]],[[[-169.74891,52.894344],[-169.705095,52.795759],[-169.962511,52.790282],[-169.989896,52.856005],[-169.74891,52.894344]]],[[[-170.148727,57.221127],[-170.28565,57.128019],[-170.313035,57.221127],[-170.148727,57.221127]]],[[[-170.669036,52.697174],[-170.603313,52.604066],[-170.789529,52.538343],[-170.816914,52.636928],[-170.669036,52.697174]]],[[[-171.742517,63.716778],[-170.94836,63.5689],[-170.488297,63.69487],[-170.280174,63.683916],[-170.093958,63.612716],[-170.044665,63.492223],[-169.644848,63.4265],[-169.518879,63.366254],[-168.99857,63.338869],[-168.686384,63.295053],[-168.856169,63.147176],[-169.108108,63.180038],[-169.376478,63.152653],[-169.513402,63.08693],[-169.639372,62.939052],[-169.831064,63.075976],[-170.055619,63.169084],[-170.263743,63.180038],[-170.362328,63.2841],[-170.866206,63.415546],[-171.101715,63.421023],[-171.463193,63.306007],[-171.73704,63.366254],[-171.852055,63.486746],[-171.742517,63.716778]]],[[[-172.432611,52.390465],[-172.41618,52.275449],[-172.607873,52.253542],[-172.569535,52.352127],[-172.432611,52.390465]]],[[[-173.626584,52.14948],[-173.495138,52.105664],[-173.122706,52.111141],[-173.106275,52.07828],[-173.549907,52.028987],[-173.626584,52.14948]]],[[[-174.322156,52.280926],[-174.327632,52.379511],[-174.185232,52.41785],[-173.982585,52.319265],[-174.059262,52.226157],[-174.179755,52.231634],[-174.141417,52.127572],[-174.333109,52.116618],[-174.738403,52.007079],[-174.968435,52.039941],[-174.902711,52.116618],[-174.656249,52.105664],[-174.322156,52.280926]]],[[[-176.469116,51.853725],[-176.288377,51.870156],[-176.288377,51.744186],[-176.518409,51.760617],[-176.80321,51.61274],[-176.912748,51.80991],[-176.792256,51.815386],[-176.775825,51.963264],[-176.627947,51.968741],[-176.627947,51.859202],[-176.469116,51.853725]]],[[[-177.153734,51.946833],[-177.044195,51.897541],[-177.120872,51.727755],[-177.274226,51.678463],[-177.279703,51.782525],[-177.153734,51.946833]]],[[[-178.123152,51.919448],[-177.953367,51.913971],[-177.800013,51.793479],[-177.964321,51.651078],[-178.123152,51.919448]]],[[[-186.892443, 52.992929],[-186.706227, 52.927205],[-186.695274, 52.823143],[-187.09509, 52.762897],[-187.357983, 52.927205],[-187.357983, 53.003883],[-186.892443, 52.992929]]]]}},
  {"type":"Feature","id":"04","properties":{"name":"Arizona"},"geometry":{"type":"Polygon","coordinates":[[[-109.042503,37.000263],[-109.04798,31.331629],[-111.074448,31.331629],[-112.246513,31.704061],[-114.815198,32.492741],[-114.72209,32.717295],[-114.524921,32.755634],[-114.470151,32.843265],[-114.524921,33.029481],[-114.661844,33.034958],[-114.727567,33.40739],[-114.524921,33.54979],[-114.497536,33.697668],[-114.535874,33.933176],[-114.415382,34.108438],[-114.256551,34.174162],[-114.136058,34.305608],[-114.333228,34.448009],[-114.470151,34.710902],[-114.634459,34.87521],[-114.634459,35.00118],[-114.574213,35.138103],[-114.596121,35.324319],[-114.678275,35.516012],[-114.738521,36.102045],[-114.371566,36.140383],[-114.251074,36.01989],[-114.152489,36.025367],[-114.048427,36.195153],[-114.048427,37.000263],[-110.499369,37.00574],[-109.042503,37.000263]]]}},
  {"type":"Feature","id":"05","properties":{"name":"Arkansas"},"geometry":{"type":"Polygon","coordinates":[[[-94.473842,36.501861],[-90.152536,36.496384],[-90.064905,36.304691],[-90.218259,36.184199],[-90.377091,35.997983],[-89.730812,35.997983],[-89.763673,35.811767],[-89.911551,35.756997],[-89.944412,35.603643],[-90.130628,35.439335],[-90.114197,35.198349],[-90.212782,35.023087],[-90.311367,34.995703],[-90.251121,34.908072],[-90.409952,34.831394],[-90.481152,34.661609],[-90.585214,34.617794],[-90.568783,34.420624],[-90.749522,34.365854],[-90.744046,34.300131],[-90.952169,34.135823],[-90.891923,34.026284],[-91.072662,33.867453],[-91.231493,33.560744],[-91.056231,33.429298],[-91.143862,33.347144],[-91.089093,33.13902],[-91.16577,33.002096],[-93.608485,33.018527],[-94.041164,33.018527],[-94.041164,33.54979],[-94.183564,33.593606],[-94.380734,33.544313],[-94.484796,33.637421],[-94.430026,35.395519],[-94.616242,36.501861],[-94.473842,36.501861]]]}},
  {"type":"Feature","id":"06","properties":{"name":"California"},"geometry":{"type":"Polygon","coordinates":[[[-123.233256,42.006186],[-122.378853,42.011663],[-121.037003,41.995232],[-120.001861,41.995232],[-119.996384,40.264519],[-120.001861,38.999346],[-118.71478,38.101128],[-117.498899,37.21934],[-116.540435,36.501861],[-115.85034,35.970598],[-114.634459,35.00118],[-114.634459,34.87521],[-114.470151,34.710902],[-114.333228,34.448009],[-114.136058,34.305608],[-114.256551,34.174162],[-114.415382,34.108438],[-114.535874,33.933176],[-114.497536,33.697668],[-114.524921,33.54979],[-114.727567,33.40739],[-114.661844,33.034958],[-114.524921,33.029481],[-114.470151,32.843265],[-114.524921,32.755634],[-114.72209,32.717295],[-116.04751,32.624187],[-117.126467,32.536556],[-117.24696,32.668003],[-117.252437,32.876127],[-117.329114,33.122589],[-117.471515,33.297851],[-117.7837,33.538836],[-118.183517,33.763391],[-118.260194,33.703145],[-118.413548,33.741483],[-118.391641,33.840068],[-118.566903,34.042715],[-118.802411,33.998899],[-119.218659,34.146777],[-119.278905,34.26727],[-119.558229,34.415147],[-119.875891,34.40967],[-120.138784,34.475393],[-120.472878,34.448009],[-120.64814,34.579455],[-120.609801,34.858779],[-120.670048,34.902595],[-120.631709,35.099764],[-120.894602,35.247642],[-120.905556,35.450289],[-121.004141,35.461243],[-121.168449,35.636505],[-121.283465,35.674843],[-121.332757,35.784382],[-121.716143,36.195153],[-121.896882,36.315645],[-121.935221,36.638785],[-121.858544,36.6114],[-121.787344,36.803093],[-121.929744,36.978355],[-122.105006,36.956447],[-122.335038,37.115279],[-122.417192,37.241248],[-122.400761,37.361741],[-122.515777,37.520572],[-122.515777,37.783465],[-122.329561,37.783465],[-122.406238,38.15042],[-122.488392,38.112082],[-122.504823,37.931343],[-122.701993,37.893004],[-122.937501,38.029928],[-122.97584,38.265436],[-123.129194,38.451652],[-123.331841,38.566668],[-123.44138,38.698114],[-123.737134,38.95553],[-123.687842,39.032208],[-123.824765,39.366301],[-123.764519,39.552517],[-123.85215,39.831841],[-124.109566,40.105688],[-124.361506,40.259042],[-124.410798,40.439781],[-124.158859,40.877937],[-124.109566,41.025814],[-124.158859,41.14083],[-124.065751,41.442061],[-124.147905,41.715908],[-124.257444,41.781632],[-124.213628,42.000709],[-123.233256,42.006186]]]}},
  {"type":"Feature","id":"08","properties":{"name":"Colorado"},"geometry":{"type":"Polygon","coordinates":[[[-107.919731,41.003906],[-105.728954,40.998429],[-104.053011,41.003906],[-102.053927,41.003906],[-102.053927,40.001626],[-102.042974,36.994786],[-103.001438,37.000263],[-104.337812,36.994786],[-106.868158,36.994786],[-107.421329,37.000263],[-109.042503,37.000263],[-109.042503,38.166851],[-109.058934,38.27639],[-109.053457,39.125316],[-109.04798,40.998429],[-107.919731,41.003906]]]}},
  {"type":"Feature","id":"09","properties":{"name":"Connecticut"},"geometry":{"type":"Polygon","coordinates":[[[-73.053528,42.039048],[-71.799309,42.022617],[-71.799309,42.006186],[-71.799309,41.414677],[-71.859555,41.321569],[-71.947186,41.338],[-72.385341,41.261322],[-72.905651,41.28323],[-73.130205,41.146307],[-73.371191,41.102491],[-73.655992,40.987475],[-73.727192,41.102491],[-73.48073,41.21203],[-73.55193,41.294184],[-73.486206,42.050002],[-73.053528,42.039048]]]}},
  {"type":"Feature","id":"10","properties":{"name":"Delaware"},"geometry":{"type":"Polygon","coordinates":[[[-75.414089,39.804456],[-75.507197,39.683964],[-75.611259,39.61824],[-75.589352,39.459409],[-75.441474,39.311532],[-75.403136,39.065069],[-75.189535,38.807653],[-75.09095,38.796699],[-75.047134,38.451652],[-75.693413,38.462606],[-75.786521,39.722302],[-75.616736,39.831841],[-75.414089,39.804456]]]}},
  {"type":"Feature","id":"11","properties":{"name":"District of Columbia"},"geometry":{"type":"Polygon","coordinates":[[[-77.035264,38.993869],[-76.909294,38.895284],[-77.040741,38.791222],[-77.117418,38.933623],[-77.035264,38.993869]]]}},
  {"type":"Feature","id":"12","properties":{"name":"Florida"},"geometry":{"type":"Polygon","coordinates":[[[-85.497137,30.997536],[-85.004212,31.003013],[-84.867289,30.712735],[-83.498053,30.647012],[-82.216449,30.570335],[-82.167157,30.356734],[-82.046664,30.362211],[-82.002849,30.564858],[-82.041187,30.751074],[-81.948079,30.827751],[-81.718048,30.745597],[-81.444201,30.707258],[-81.383954,30.27458],[-81.257985,29.787132],[-80.967707,29.14633],[-80.524075,28.461713],[-80.589798,28.41242],[-80.56789,28.094758],[-80.381674,27.738757],[-80.091397,27.021277],[-80.03115,26.796723],[-80.036627,26.566691],[-80.146166,25.739673],[-80.239274,25.723243],[-80.337859,25.465826],[-80.304997,25.383672],[-80.49669,25.197456],[-80.573367,25.241272],[-80.759583,25.164595],[-81.077246,25.120779],[-81.170354,25.224841],[-81.126538,25.378195],[-81.351093,25.821827],[-81.526355,25.903982],[-81.679709,25.843735],[-81.800202,26.090198],[-81.833064,26.292844],[-82.041187,26.517399],[-82.09048,26.665276],[-82.057618,26.878877],[-82.172634,26.917216],[-82.145249,26.791246],[-82.249311,26.758384],[-82.566974,27.300601],[-82.692943,27.437525],[-82.391711,27.837342],[-82.588881,27.815434],[-82.720328,27.689464],[-82.851774,27.886634],[-82.676512,28.434328],[-82.643651,28.888914],[-82.764143,28.998453],[-82.802482,29.14633],[-82.994175,29.179192],[-83.218729,29.420177],[-83.399469,29.518762],[-83.410422,29.66664],[-83.536392,29.721409],[-83.640454,29.885717],[-84.02384,30.104795],[-84.357933,30.055502],[-84.341502,29.902148],[-84.451041,29.929533],[-84.867289,29.743317],[-85.310921,29.699501],[-85.299967,29.80904],[-85.404029,29.940487],[-85.924338,30.236241],[-86.29677,30.362211],[-86.630863,30.395073],[-86.910187,30.373165],[-87.518128,30.280057],[-87.37025,30.427934],[-87.446927,30.510088],[-87.408589,30.674397],[-87.633143,30.86609],[-87.600282,30.997536],[-85.497137,30.997536]]]}},
  {"type":"Feature","id":"13","properties":{"name":"Georgia"},"geometry":{"type":"Polygon","coordinates":[[[-83.109191,35.00118],[-83.322791,34.787579],[-83.339222,34.683517],[-83.005129,34.469916],[-82.901067,34.486347],[-82.747713,34.26727],[-82.714851,34.152254],[-82.55602,33.94413],[-82.325988,33.81816],[-82.194542,33.631944],[-81.926172,33.462159],[-81.937125,33.347144],[-81.761863,33.160928],[-81.493493,33.007573],[-81.42777,32.843265],[-81.416816,32.629664],[-81.279893,32.558464],[-81.121061,32.290094],[-81.115584,32.120309],[-80.885553,32.032678],[-81.132015,31.693108],[-81.175831,31.517845],[-81.279893,31.364491],[-81.290846,31.20566],[-81.400385,31.13446],[-81.444201,30.707258],[-81.718048,30.745597],[-81.948079,30.827751],[-82.041187,30.751074],[-82.002849,30.564858],[-82.046664,30.362211],[-82.167157,30.356734],[-82.216449,30.570335],[-83.498053,30.647012],[-84.867289,30.712735],[-85.004212,31.003013],[-85.113751,31.27686],[-85.042551,31.539753],[-85.141136,31.840985],[-85.053504,32.01077],[-85.058981,32.13674],[-84.889196,32.262709],[-85.004212,32.322956],[-84.960397,32.421541],[-85.069935,32.580372],[-85.184951,32.859696],[-85.431413,34.124869],[-85.606675,34.984749],[-84.319594,34.990226],[-83.618546,34.984749],[-83.109191,35.00118]]]}},
  {"type":"Feature","id":"15","properties":{"name":"Hawaii"},"geometry":{"type":"MultiPolygon","coordinates":[[[[-155.634835,18.948267],[-155.881297,19.035898],[-155.919636,19.123529],[-155.886774,19.348084],[-156.062036,19.73147],[-155.925113,19.857439],[-155.826528,20.032702],[-155.897728,20.147717],[-155.87582,20.26821],[-155.596496,20.12581],[-155.284311,20.021748],[-155.092618,19.868393],[-155.092618,19.736947],[-154.807817,19.523346],[-154.983079,19.348084],[-155.295265,19.26593],[-155.514342,19.134483],[-155.634835,18.948267]]],[[[-156.587823,21.029505],[-156.472807,20.892581],[-156.324929,20.952827],[-156.00179,20.793996],[-156.051082,20.651596],[-156.379699,20.580396],[-156.445422,20.60778],[-156.461853,20.783042],[-156.631638,20.821381],[-156.697361,20.919966],[-156.587823,21.029505]]],[[[-156.982162,21.210244],[-157.080747,21.106182],[-157.310779,21.106182],[-157.239579,21.221198],[-156.982162,21.210244]]],[[[-157.951581,21.697691],[-157.842042,21.462183],[-157.896811,21.325259],[-158.110412,21.303352],[-158.252813,21.582676],[-158.126843,21.588153],[-157.951581,21.697691]]],[[[-159.468693,22.228955],[-159.353678,22.218001],[-159.298908,22.113939],[-159.33177,21.966061],[-159.446786,21.872953],[-159.764448,21.987969],[-159.726109,22.152277],[-159.468693,22.228955]]]]}},
  {"type":"Feature","id":"16","properties":{"name":"Idaho"},"geometry":{"type":"Polygon","coordinates":[[[-116.04751,49.000239],[-116.04751,47.976051],[-115.724371,47.696727],[-115.718894,47.42288],[-115.527201,47.302388],[-115.324554,47.258572],[-115.302646,47.187372],[-114.930214,46.919002],[-114.886399,46.809463],[-114.623506,46.705401],[-114.612552,46.639678],[-114.322274,46.645155],[-114.464674,46.272723],[-114.492059,46.037214],[-114.387997,45.88386],[-114.568736,45.774321],[-114.497536,45.670259],[-114.546828,45.560721],[-114.333228,45.456659],[-114.086765,45.593582],[-113.98818,45.703121],[-113.807441,45.604536],[-113.834826,45.522382],[-113.736241,45.330689],[-113.571933,45.128042],[-113.45144,45.056842],[-113.456917,44.865149],[-113.341901,44.782995],[-113.133778,44.772041],[-113.002331,44.448902],[-112.887315,44.394132],[-112.783254,44.48724],[-112.471068,44.481763],[-112.241036,44.569394],[-112.104113,44.520102],[-111.868605,44.563917],[-111.819312,44.509148],[-111.616665,44.547487],[-111.386634,44.75561],[-111.227803,44.580348],[-111.047063,44.476286],[-111.047063,42.000709],[-112.164359,41.995232],[-114.04295,41.995232],[-117.027882,42.000709],[-117.027882,43.830007],[-116.896436,44.158624],[-116.97859,44.240778],[-117.170283,44.257209],[-117.241483,44.394132],[-117.038836,44.750133],[-116.934774,44.782995],[-116.830713,44.930872],[-116.847143,45.02398],[-116.732128,45.144473],[-116.671881,45.319735],[-116.463758,45.61549],[-116.545912,45.752413],[-116.78142,45.823614],[-116.918344,45.993399],[-116.92382,46.168661],[-117.055267,46.343923],[-117.038836,46.426077],[-117.044313,47.762451],[-117.033359,49.000239],[-116.04751,49.000239]]]}},
  {"type":"Feature","id":"17","properties":{"name":"Illinois"},"geometry":{"type":"Polygon","coordinates":[[[-90.639984,42.510065],[-88.788778,42.493634],[-87.802929,42.493634],[-87.83579,42.301941],[-87.682436,42.077386],[-87.523605,41.710431],[-87.529082,39.34987],[-87.63862,39.169131],[-87.512651,38.95553],[-87.49622,38.780268],[-87.62219,38.637868],[-87.655051,38.506421],[-87.83579,38.292821],[-87.950806,38.27639],[-87.923421,38.15042],[-88.000098,38.101128],[-88.060345,37.865619],[-88.027483,37.799896],[-88.15893,37.657496],[-88.065822,37.482234],[-88.476592,37.389126],[-88.514931,37.285064],[-88.421823,37.153617],[-88.547792,37.071463],[-88.914747,37.224817],[-89.029763,37.213863],[-89.183118,37.038601],[-89.133825,36.983832],[-89.292656,36.994786],[-89.517211,37.279587],[-89.435057,37.34531],[-89.517211,37.537003],[-89.517211,37.690357],[-89.84035,37.903958],[-89.949889,37.88205],[-90.059428,38.013497],[-90.355183,38.216144],[-90.349706,38.374975],[-90.179921,38.632391],[-90.207305,38.725499],[-90.10872,38.845992],[-90.251121,38.917192],[-90.470199,38.961007],[-90.585214,38.867899],[-90.661891,38.928146],[-90.727615,39.256762],[-91.061708,39.470363],[-91.368417,39.727779],[-91.494386,40.034488],[-91.50534,40.237135],[-91.417709,40.379535],[-91.401278,40.560274],[-91.121954,40.669813],[-91.09457,40.823167],[-90.963123,40.921752],[-90.946692,41.097014],[-91.111001,41.239415],[-91.045277,41.414677],[-90.656414,41.463969],[-90.344229,41.589939],[-90.311367,41.743293],[-90.179921,41.809016],[-90.141582,42.000709],[-90.168967,42.126679],[-90.393521,42.225264],[-90.420906,42.329326],[-90.639984,42.510065]]]}},
  {"type":"Feature","id":"18","properties":{"name":"Indiana"},"geometry":{"type":"Polygon","coordinates":[[[-85.990061,41.759724],[-84.807042,41.759724],[-84.807042,41.694001],[-84.801565,40.500028],[-84.817996,39.103408],[-84.894673,39.059592],[-84.812519,38.785745],[-84.987781,38.780268],[-85.173997,38.68716],[-85.431413,38.730976],[-85.42046,38.533806],[-85.590245,38.451652],[-85.655968,38.325682],[-85.83123,38.27639],[-85.924338,38.024451],[-86.039354,37.958727],[-86.263908,38.051835],[-86.302247,38.166851],[-86.521325,38.040881],[-86.504894,37.931343],[-86.729448,37.893004],[-86.795172,37.991589],[-87.047111,37.893004],[-87.129265,37.788942],[-87.381204,37.93682],[-87.512651,37.903958],[-87.600282,37.975158],[-87.682436,37.903958],[-87.934375,37.893004],[-88.027483,37.799896],[-88.060345,37.865619],[-88.000098,38.101128],[-87.923421,38.15042],[-87.950806,38.27639],[-87.83579,38.292821],[-87.655051,38.506421],[-87.62219,38.637868],[-87.49622,38.780268],[-87.512651,38.95553],[-87.63862,39.169131],[-87.529082,39.34987],[-87.523605,41.710431],[-87.42502,41.644708],[-87.118311,41.644708],[-86.822556,41.759724],[-85.990061,41.759724]]]}},
  {"type":"Feature","id":"19","properties":{"name":"Iowa"},"geometry":{"type":"Polygon","coordinates":[[[-91.368417,43.501391],[-91.215062,43.501391],[-91.204109,43.353514],[-91.056231,43.254929],[-91.176724,43.134436],[-91.143862,42.909881],[-91.067185,42.75105],[-90.711184,42.636034],[-90.639984,42.510065],[-90.420906,42.329326],[-90.393521,42.225264],[-90.168967,42.126679],[-90.141582,42.000709],[-90.179921,41.809016],[-90.311367,41.743293],[-90.344229,41.589939],[-90.656414,41.463969],[-91.045277,41.414677],[-91.111001,41.239415],[-90.946692,41.097014],[-90.963123,40.921752],[-91.09457,40.823167],[-91.121954,40.669813],[-91.401278,40.560274],[-91.417709,40.379535],[-91.527248,40.412397],[-91.729895,40.615043],[-91.833957,40.609566],[-93.257961,40.582182],[-94.632673,40.571228],[-95.7664,40.587659],[-95.881416,40.719105],[-95.826646,40.976521],[-95.925231,41.201076],[-95.919754,41.453015],[-96.095016,41.540646],[-96.122401,41.67757],[-96.062155,41.798063],[-96.127878,41.973325],[-96.264801,42.039048],[-96.44554,42.488157],[-96.631756,42.707235],[-96.544125,42.855112],[-96.511264,43.052282],[-96.434587,43.123482],[-96.560556,43.222067],[-96.527695,43.397329],[-96.582464,43.479483],[-96.451017,43.501391],[-91.368417,43.501391]]]}},
  {"type":"Feature","id":"20","properties":{"name":"Kansas"},"geometry":{"type":"Polygon","coordinates":[[[-101.90605,40.001626],[-95.306337,40.001626],[-95.207752,39.908518],[-94.884612,39.831841],[-95.109167,39.541563],[-94.983197,39.442978],[-94.824366,39.20747],[-94.610765,39.158177],[-94.616242,37.000263],[-100.087706,37.000263],[-102.042974,36.994786],[-102.053927,40.001626],[-101.90605,40.001626]]]}},
  {"type":"Feature","id":"21","properties":{"name":"Kentucky"},"geometry":{"type":"Polygon","coordinates":[[[-83.903347,38.769315],[-83.678792,38.632391],[-83.519961,38.703591],[-83.142052,38.626914],[-83.032514,38.725499],[-82.890113,38.758361],[-82.846298,38.588575],[-82.731282,38.561191],[-82.594358,38.424267],[-82.621743,38.123036],[-82.50125,37.931343],[-82.342419,37.783465],[-82.293127,37.668449],[-82.101434,37.553434],[-81.969987,37.537003],[-82.353373,37.268633],[-82.720328,37.120755],[-82.720328,37.044078],[-82.868205,36.978355],[-82.879159,36.890724],[-83.070852,36.852385],[-83.136575,36.742847],[-83.673316,36.600446],[-83.689746,36.584015],[-84.544149,36.594969],[-85.289013,36.627831],[-85.486183,36.616877],[-86.592525,36.655216],[-87.852221,36.633308],[-88.071299,36.677123],[-88.054868,36.496384],[-89.298133,36.507338],[-89.418626,36.496384],[-89.363857,36.622354],[-89.215979,36.578538],[-89.133825,36.983832],[-89.183118,37.038601],[-89.029763,37.213863],[-88.914747,37.224817],[-88.547792,37.071463],[-88.421823,37.153617],[-88.514931,37.285064],[-88.476592,37.389126],[-88.065822,37.482234],[-88.15893,37.657496],[-88.027483,37.799896],[-87.934375,37.893004],[-87.682436,37.903958],[-87.600282,37.975158],[-87.512651,37.903958],[-87.381204,37.93682],[-87.129265,37.788942],[-87.047111,37.893004],[-86.795172,37.991589],[-86.729448,37.893004],[-86.504894,37.931343],[-86.521325,38.040881],[-86.302247,38.166851],[-86.263908,38.051835],[-86.039354,37.958727],[-85.924338,38.024451],[-85.83123,38.27639],[-85.655968,38.325682],[-85.590245,38.451652],[-85.42046,38.533806],[-85.431413,38.730976],[-85.173997,38.68716],[-84.987781,38.780268],[-84.812519,38.785745],[-84.894673,39.059592],[-84.817996,39.103408],[-84.43461,39.103408],[-84.231963,38.895284],[-84.215533,38.807653],[-83.903347,38.769315]]]}},
  {"type":"Feature","id":"22","properties":{"name":"Louisiana"},"geometry":{"type":"Polygon","coordinates":[[[-93.608485,33.018527],[-91.16577,33.002096],[-91.072662,32.887081],[-91.143862,32.843265],[-91.154816,32.640618],[-91.006939,32.514649],[-90.985031,32.218894],[-91.105524,31.988862],[-91.341032,31.846462],[-91.401278,31.621907],[-91.499863,31.643815],[-91.516294,31.27686],[-91.636787,31.265906],[-91.565587,31.068736],[-91.636787,30.997536],[-89.747242,30.997536],[-89.845827,30.66892],[-89.681519,30.449842],[-89.643181,30.285534],[-89.522688,30.181472],[-89.818443,30.044549],[-89.84035,29.945964],[-89.599365,29.88024],[-89.495303,30.039072],[-89.287179,29.88024],[-89.30361,29.754271],[-89.424103,29.699501],[-89.648657,29.748794],[-89.621273,29.655686],[-89.69795,29.513285],[-89.506257,29.387316],[-89.199548,29.348977],[-89.09001,29.2011],[-89.002379,29.179192],[-89.16121,29.009407],[-89.336472,29.042268],[-89.484349,29.217531],[-89.851304,29.310638],[-89.851304,29.480424],[-90.032043,29.425654],[-90.021089,29.283254],[-90.103244,29.151807],[-90.23469,29.129899],[-90.333275,29.277777],[-90.563307,29.283254],[-90.645461,29.129899],[-90.798815,29.086084],[-90.963123,29.179192],[-91.09457,29.190146],[-91.220539,29.436608],[-91.445094,29.546147],[-91.532725,29.529716],[-91.620356,29.73784],[-91.883249,29.710455],[-91.888726,29.836425],[-92.146142,29.715932],[-92.113281,29.622824],[-92.31045,29.535193],[-92.617159,29.579009],[-92.97316,29.715932],[-93.2251,29.776178],[-93.767317,29.726886],[-93.838517,29.688547],[-93.926148,29.787132],[-93.690639,30.143133],[-93.767317,30.334826],[-93.696116,30.438888],[-93.728978,30.575812],[-93.630393,30.679874],[-93.526331,30.93729],[-93.542762,31.15089],[-93.816609,31.556184],[-93.822086,31.775262],[-94.041164,31.994339],[-94.041164,33.018527],[-93.608485,33.018527]]]}},
  {"type":"Feature","id":"23","properties":{"name":"Maine"},"geometry":{"type":"Polygon","coordinates":[[[-70.703921,43.057759],[-70.824413,43.128959],[-70.807983,43.227544],[-70.966814,43.34256],[-71.032537,44.657025],[-71.08183,45.303304],[-70.649151,45.440228],[-70.720352,45.511428],[-70.556043,45.664782],[-70.386258,45.735983],[-70.41912,45.796229],[-70.260289,45.889337],[-70.309581,46.064599],[-70.210996,46.327492],[-70.057642,46.415123],[-69.997395,46.694447],[-69.225147,47.461219],[-69.044408,47.428357],[-69.033454,47.242141],[-68.902007,47.176418],[-68.578868,47.285957],[-68.376221,47.285957],[-68.233821,47.357157],[-67.954497,47.198326],[-67.790188,47.066879],[-67.779235,45.944106],[-67.801142,45.675736],[-67.456095,45.604536],[-67.505388,45.48952],[-67.417757,45.379982],[-67.488957,45.281397],[-67.346556,45.128042],[-67.16034,45.160904],[-66.979601,44.804903],[-67.187725,44.646072],[-67.308218,44.706318],[-67.406803,44.596779],[-67.549203,44.624164],[-67.565634,44.531056],[-67.75185,44.54201],[-68.047605,44.328409],[-68.118805,44.476286],[-68.222867,44.48724],[-68.173574,44.328409],[-68.403606,44.251732],[-68.458375,44.377701],[-68.567914,44.311978],[-68.82533,44.311978],[-68.830807,44.459856],[-68.984161,44.426994],[-68.956777,44.322932],[-69.099177,44.103854],[-69.071793,44.043608],[-69.258008,43.923115],[-69.444224,43.966931],[-69.553763,43.840961],[-69.707118,43.82453],[-69.833087,43.720469],[-69.986442,43.742376],[-70.030257,43.851915],[-70.254812,43.676653],[-70.194565,43.567114],[-70.358873,43.528776],[-70.369827,43.435668],[-70.556043,43.320652],[-70.703921,43.057759]]]}},
  {"type":"Feature","id":"24","properties":{"name":"Maryland"},"geometry":{"type":"MultiPolygon","coordinates":[[[[-75.994645,37.95325],[-76.016553,37.95325],[-76.043938,37.95325],[-75.994645,37.95325]]],[[[-79.477979,39.722302],[-75.786521,39.722302],[-75.693413,38.462606],[-75.047134,38.451652],[-75.244304,38.029928],[-75.397659,38.013497],[-75.671506,37.95325],[-75.885106,37.909435],[-75.879629,38.073743],[-75.961783,38.139466],[-75.846768,38.210667],[-76.000122,38.374975],[-76.049415,38.303775],[-76.257538,38.320205],[-76.328738,38.500944],[-76.263015,38.500944],[-76.257538,38.736453],[-76.191815,38.829561],[-76.279446,39.147223],[-76.169907,39.333439],[-76.000122,39.366301],[-75.972737,39.557994],[-76.098707,39.536086],[-76.104184,39.437501],[-76.367077,39.311532],[-76.443754,39.196516],[-76.460185,38.906238],[-76.55877,38.769315],[-76.514954,38.539283],[-76.383508,38.380452],[-76.399939,38.259959],[-76.317785,38.139466],[-76.3616,38.057312],[-76.591632,38.216144],[-76.920248,38.292821],[-77.018833,38.446175],[-77.205049,38.358544],[-77.276249,38.479037],[-77.128372,38.632391],[-77.040741,38.791222],[-76.909294,38.895284],[-77.035264,38.993869],[-77.117418,38.933623],[-77.248864,39.026731],[-77.456988,39.076023],[-77.456988,39.223901],[-77.566527,39.306055],[-77.719881,39.322485],[-77.834897,39.601809],[-78.004682,39.601809],[-78.174467,39.694917],[-78.267575,39.61824],[-78.431884,39.623717],[-78.470222,39.514178],[-78.765977,39.585379],[-78.963147,39.437501],[-79.094593,39.470363],[-79.291763,39.300578],[-79.488933,39.20747],[-79.477979,39.722302]]]]}},
  {"type":"Feature","id":"25","properties":{"name":"Massachusetts"},"geometry":{"type":"Polygon","coordinates":[[[-70.917521,42.887974],[-70.818936,42.871543],[-70.780598,42.696281],[-70.824413,42.55388],[-70.983245,42.422434],[-70.988722,42.269079],[-70.769644,42.247172],[-70.638197,42.08834],[-70.660105,41.962371],[-70.550566,41.929509],[-70.539613,41.814493],[-70.260289,41.715908],[-69.937149,41.809016],[-70.008349,41.672093],[-70.484843,41.5516],[-70.660105,41.546123],[-70.764167,41.639231],[-70.928475,41.611847],[-70.933952,41.540646],[-71.120168,41.496831],[-71.196845,41.67757],[-71.22423,41.710431],[-71.328292,41.781632],[-71.383061,42.01714],[-71.530939,42.01714],[-71.799309,42.006186],[-71.799309,42.022617],[-73.053528,42.039048],[-73.486206,42.050002],[-73.508114,42.08834],[-73.267129,42.745573],[-72.456542,42.729142],[-71.29543,42.696281],[-71.185891,42.789389],[-70.917521,42.887974]]]}},
  {"type":"Feature","id":"26","properties":{"name":"Michigan"},"geometry":{"type":"MultiPolygon","coordinates":[[[[-83.454238,41.732339],[-84.807042,41.694001],[-84.807042,41.759724],[-85.990061,41.759724],[-86.822556,41.759724],[-86.619909,41.891171],[-86.482986,42.115725],[-86.357016,42.252649],[-86.263908,42.444341],[-86.209139,42.718189],[-86.231047,43.013943],[-86.526801,43.594499],[-86.433693,43.813577],[-86.499417,44.07647],[-86.269385,44.34484],[-86.220093,44.569394],[-86.252954,44.689887],[-86.088646,44.73918],[-86.066738,44.903488],[-85.809322,44.947303],[-85.612152,45.128042],[-85.628583,44.766564],[-85.524521,44.750133],[-85.393075,44.930872],[-85.387598,45.237581],[-85.305444,45.314258],[-85.031597,45.363551],[-85.119228,45.577151],[-84.938489,45.75789],[-84.713934,45.768844],[-84.461995,45.653829],[-84.215533,45.637398],[-84.09504,45.494997],[-83.908824,45.484043],[-83.596638,45.352597],[-83.4871,45.358074],[-83.317314,45.144473],[-83.454238,45.029457],[-83.322791,44.88158],[-83.273499,44.711795],[-83.333745,44.339363],[-83.536392,44.246255],[-83.585684,44.054562],[-83.82667,43.988839],[-83.958116,43.758807],[-83.908824,43.671176],[-83.667839,43.589022],[-83.481623,43.714992],[-83.262545,43.972408],[-82.917498,44.070993],[-82.747713,43.994316],[-82.643651,43.851915],[-82.539589,43.435668],[-82.523158,43.227544],[-82.413619,42.975605],[-82.517681,42.614127],[-82.681989,42.559357],[-82.687466,42.690804],[-82.797005,42.652465],[-82.922975,42.351234],[-83.125621,42.236218],[-83.185868,42.006186],[-83.437807,41.814493],[-83.454238,41.732339]]],[[[-85.508091,45.730506],[-85.49166,45.610013],[-85.623106,45.588105],[-85.568337,45.75789],[-85.508091,45.730506]]],[[[-87.589328,45.095181],[-87.742682,45.199243],[-87.649574,45.341643],[-87.885083,45.363551],[-87.791975,45.500474],[-87.781021,45.675736],[-87.989145,45.796229],[-88.10416,45.922199],[-88.531362,46.020784],[-88.662808,45.987922],[-89.09001,46.135799],[-90.119674,46.338446],[-90.229213,46.508231],[-90.415429,46.568478],[-90.026566,46.672539],[-89.851304,46.793032],[-89.413149,46.842325],[-89.128348,46.990202],[-88.996902,46.995679],[-88.887363,47.099741],[-88.575177,47.247618],[-88.416346,47.373588],[-88.180837,47.455742],[-87.956283,47.384542],[-88.350623,47.077833],[-88.443731,46.973771],[-88.438254,46.787555],[-88.246561,46.929956],[-87.901513,46.908048],[-87.633143,46.809463],[-87.392158,46.535616],[-87.260711,46.486323],[-87.008772,46.530139],[-86.948526,46.469893],[-86.696587,46.437031],[-86.159846,46.667063],[-85.880522,46.68897],[-85.508091,46.678016],[-85.256151,46.754694],[-85.064458,46.760171],[-85.02612,46.480847],[-84.82895,46.442508],[-84.63178,46.486323],[-84.549626,46.4206],[-84.418179,46.502754],[-84.127902,46.530139],[-84.122425,46.179615],[-83.990978,46.031737],[-83.793808,45.993399],[-83.7719,46.091984],[-83.580208,46.091984],[-83.476146,45.987922],[-83.563777,45.911245],[-84.111471,45.976968],[-84.374364,45.933153],[-84.659165,46.053645],[-84.741319,45.944106],[-84.70298,45.850998],[-84.82895,45.872906],[-85.015166,46.00983],[-85.338305,46.091984],[-85.502614,46.097461],[-85.661445,45.966014],[-85.924338,45.933153],[-86.209139,45.960537],[-86.324155,45.905768],[-86.351539,45.796229],[-86.663725,45.703121],[-86.647294,45.834568],[-86.784218,45.861952],[-86.838987,45.725029],[-87.069019,45.719552],[-87.17308,45.659305],[-87.326435,45.423797],[-87.611236,45.122565],[-87.589328,45.095181]]],[[[-88.805209,47.976051],[-89.057148,47.850082],[-89.188594,47.833651],[-89.177641,47.937713],[-88.547792,48.173221],[-88.668285,48.008913],[-88.805209,47.976051]]]]}},
  {"type":"Feature","id":"27","properties":{"name":"Minnesota"},"geometry":{"type":"Polygon","coordinates":[[[-92.014696,46.705401],[-92.091373,46.749217],[-92.29402,46.667063],[-92.29402,46.075553],[-92.354266,46.015307],[-92.639067,45.933153],[-92.869098,45.719552],[-92.885529,45.577151],[-92.770513,45.566198],[-92.644544,45.440228],[-92.75956,45.286874],[-92.737652,45.117088],[-92.808852,44.750133],[-92.545959,44.569394],[-92.337835,44.552964],[-92.233773,44.443425],[-91.927065,44.333886],[-91.877772,44.202439],[-91.592971,44.032654],[-91.43414,43.994316],[-91.242447,43.775238],[-91.269832,43.616407],[-91.215062,43.501391],[-91.368417,43.501391],[-96.451017,43.501391],[-96.451017,45.297827],[-96.681049,45.412843],[-96.856311,45.604536],[-96.582464,45.818137],[-96.560556,45.933153],[-96.598895,46.332969],[-96.719387,46.437031],[-96.801542,46.656109],[-96.785111,46.924479],[-96.823449,46.968294],[-96.856311,47.609096],[-97.053481,47.948667],[-97.130158,48.140359],[-97.16302,48.545653],[-97.097296,48.682577],[-97.228743,49.000239],[-95.152983,49.000239],[-95.152983,49.383625],[-94.955813,49.372671],[-94.824366,49.295994],[-94.69292,48.775685],[-94.588858,48.715438],[-94.260241,48.699007],[-94.221903,48.649715],[-93.838517,48.627807],[-93.794701,48.518268],[-93.466085,48.545653],[-93.466085,48.589469],[-93.208669,48.644238],[-92.984114,48.62233],[-92.726698,48.540176],[-92.655498,48.436114],[-92.50762,48.447068],[-92.370697,48.222514],[-92.304974,48.315622],[-92.053034,48.359437],[-92.009219,48.266329],[-91.713464,48.200606],[-91.713464,48.112975],[-91.565587,48.041775],[-91.264355,48.080113],[-91.083616,48.178698],[-90.837154,48.238944],[-90.749522,48.091067],[-90.579737,48.123929],[-90.377091,48.091067],[-90.141582,48.112975],[-89.873212,47.987005],[-89.615796,48.008913],[-89.637704,47.954144],[-89.971797,47.828174],[-90.437337,47.729589],[-90.738569,47.625527],[-91.171247,47.368111],[-91.357463,47.20928],[-91.642264,47.028541],[-92.091373,46.787555],[-92.014696,46.705401]]]}},
  {"type":"Feature","id":"28","properties":{"name":"Mississippi"},"geometry":{"type":"Polygon","coordinates":[[[-88.471115,34.995703],[-88.202745,34.995703],[-88.098683,34.891641],[-88.241084,33.796253],[-88.471115,31.895754],[-88.394438,30.367688],[-88.503977,30.323872],[-88.744962,30.34578],[-88.843547,30.411504],[-89.084533,30.367688],[-89.418626,30.252672],[-89.522688,30.181472],[-89.643181,30.285534],[-89.681519,30.449842],[-89.845827,30.66892],[-89.747242,30.997536],[-91.636787,30.997536],[-91.565587,31.068736],[-91.636787,31.265906],[-91.516294,31.27686],[-91.499863,31.643815],[-91.401278,31.621907],[-91.341032,31.846462],[-91.105524,31.988862],[-90.985031,32.218894],[-91.006939,32.514649],[-91.154816,32.640618],[-91.143862,32.843265],[-91.072662,32.887081],[-91.16577,33.002096],[-91.089093,33.13902],[-91.143862,33.347144],[-91.056231,33.429298],[-91.231493,33.560744],[-91.072662,33.867453],[-90.891923,34.026284],[-90.952169,34.135823],[-90.744046,34.300131],[-90.749522,34.365854],[-90.568783,34.420624],[-90.585214,34.617794],[-90.481152,34.661609],[-90.409952,34.831394],[-90.251121,34.908072],[-90.311367,34.995703],[-88.471115,34.995703]]]}},
  {"type":"Feature","id":"29","properties":{"name":"Missouri"},"geometry":{"type":"Polygon","coordinates":[[[-91.833957,40.609566],[-91.729895,40.615043],[-91.527248,40.412397],[-91.417709,40.379535],[-91.50534,40.237135],[-91.494386,40.034488],[-91.368417,39.727779],[-91.061708,39.470363],[-90.727615,39.256762],[-90.661891,38.928146],[-90.585214,38.867899],[-90.470199,38.961007],[-90.251121,38.917192],[-90.10872,38.845992],[-90.207305,38.725499],[-90.179921,38.632391],[-90.349706,38.374975],[-90.355183,38.216144],[-90.059428,38.013497],[-89.949889,37.88205],[-89.84035,37.903958],[-89.517211,37.690357],[-89.517211,37.537003],[-89.435057,37.34531],[-89.517211,37.279587],[-89.292656,36.994786],[-89.133825,36.983832],[-89.215979,36.578538],[-89.363857,36.622354],[-89.418626,36.496384],[-89.484349,36.496384],[-89.539119,36.496384],[-89.533642,36.249922],[-89.730812,35.997983],[-90.377091,35.997983],[-90.218259,36.184199],[-90.064905,36.304691],[-90.152536,36.496384],[-94.473842,36.501861],[-94.616242,36.501861],[-94.616242,37.000263],[-94.610765,39.158177],[-94.824366,39.20747],[-94.983197,39.442978],[-95.109167,39.541563],[-94.884612,39.831841],[-95.207752,39.908518],[-95.306337,40.001626],[-95.552799,40.264519],[-95.7664,40.587659],[-94.632673,40.571228],[-93.257961,40.582182],[-91.833957,40.609566]]]}},
  {"type":"Feature","id":"30","properties":{"name":"Montana"},"geometry":{"type":"Polygon","coordinates":[[[-104.047534,49.000239],[-104.042057,47.861036],[-104.047534,45.944106],[-104.042057,44.996596],[-104.058488,44.996596],[-105.91517,45.002073],[-109.080842,45.002073],[-111.05254,45.002073],[-111.047063,44.476286],[-111.227803,44.580348],[-111.386634,44.75561],[-111.616665,44.547487],[-111.819312,44.509148],[-111.868605,44.563917],[-112.104113,44.520102],[-112.241036,44.569394],[-112.471068,44.481763],[-112.783254,44.48724],[-112.887315,44.394132],[-113.002331,44.448902],[-113.133778,44.772041],[-113.341901,44.782995],[-113.456917,44.865149],[-113.45144,45.056842],[-113.571933,45.128042],[-113.736241,45.330689],[-113.834826,45.522382],[-113.807441,45.604536],[-113.98818,45.703121],[-114.086765,45.593582],[-114.333228,45.456659],[-114.546828,45.560721],[-114.497536,45.670259],[-114.568736,45.774321],[-114.387997,45.88386],[-114.492059,46.037214],[-114.464674,46.272723],[-114.322274,46.645155],[-114.612552,46.639678],[-114.623506,46.705401],[-114.886399,46.809463],[-114.930214,46.919002],[-115.302646,47.187372],[-115.324554,47.258572],[-115.527201,47.302388],[-115.718894,47.42288],[-115.724371,47.696727],[-116.04751,47.976051],[-116.04751,49.000239],[-111.50165,48.994762],[-109.453274,49.000239],[-104.047534,49.000239]]]}},
  {"type":"Feature","id":"31","properties":{"name":"Nebraska"},"geometry":{"type":"Polygon","coordinates":[[[-103.324578,43.002989],[-101.626726,42.997512],[-98.499393,42.997512],[-98.466531,42.94822],[-97.951699,42.767481],[-97.831206,42.866066],[-97.688806,42.844158],[-97.217789,42.844158],[-96.692003,42.657942],[-96.626279,42.515542],[-96.44554,42.488157],[-96.264801,42.039048],[-96.127878,41.973325],[-96.062155,41.798063],[-96.122401,41.67757],[-96.095016,41.540646],[-95.919754,41.453015],[-95.925231,41.201076],[-95.826646,40.976521],[-95.881416,40.719105],[-95.7664,40.587659],[-95.552799,40.264519],[-95.306337,40.001626],[-101.90605,40.001626],[-102.053927,40.001626],[-102.053927,41.003906],[-104.053011,41.003906],[-104.053011,43.002989],[-103.324578,43.002989]]]}},
  {"type":"Feature","id":"32","properties":{"name":"Nevada"},"geometry":{"type":"Polygon","coordinates":[[[-117.027882,42.000709],[-114.04295,41.995232],[-114.048427,37.000263],[-114.048427,36.195153],[-114.152489,36.025367],[-114.251074,36.01989],[-114.371566,36.140383],[-114.738521,36.102045],[-114.678275,35.516012],[-114.596121,35.324319],[-114.574213,35.138103],[-114.634459,35.00118],[-115.85034,35.970598],[-116.540435,36.501861],[-117.498899,37.21934],[-118.71478,38.101128],[-120.001861,38.999346],[-119.996384,40.264519],[-120.001861,41.995232],[-118.698349,41.989755],[-117.027882,42.000709]]]}},
  {"type":"Feature","id":"33","properties":{"name":"New Hampshire"},"geometry":{"type":"Polygon","coordinates":[[[-71.08183,45.303304],[-71.032537,44.657025],[-70.966814,43.34256],[-70.807983,43.227544],[-70.824413,43.128959],[-70.703921,43.057759],[-70.818936,42.871543],[-70.917521,42.887974],[-71.185891,42.789389],[-71.29543,42.696281],[-72.456542,42.729142],[-72.544173,42.80582],[-72.533219,42.953697],[-72.445588,43.008466],[-72.456542,43.150867],[-72.379864,43.572591],[-72.204602,43.769761],[-72.116971,43.994316],[-72.02934,44.07647],[-72.034817,44.322932],[-71.700724,44.41604],[-71.536416,44.585825],[-71.629524,44.750133],[-71.4926,44.914442],[-71.503554,45.013027],[-71.361154,45.270443],[-71.131122,45.243058],[-71.08183,45.303304]]]}},
  {"type":"Feature","id":"34","properties":{"name":"New Jersey"},"geometry":{"type":"Polygon","coordinates":[[[-74.236547,41.14083],[-73.902454,40.998429],[-74.022947,40.708151],[-74.187255,40.642428],[-74.274886,40.489074],[-74.001039,40.412397],[-73.979131,40.297381],[-74.099624,39.760641],[-74.411809,39.360824],[-74.614456,39.245808],[-74.795195,38.993869],[-74.888303,39.158177],[-75.178581,39.240331],[-75.534582,39.459409],[-75.55649,39.607286],[-75.561967,39.629194],[-75.507197,39.683964],[-75.414089,39.804456],[-75.145719,39.88661],[-75.129289,39.963288],[-74.82258,40.127596],[-74.773287,40.215227],[-75.058088,40.417874],[-75.069042,40.543843],[-75.195012,40.576705],[-75.205966,40.691721],[-75.052611,40.866983],[-75.134765,40.971045],[-74.882826,41.179168],[-74.828057,41.288707],[-74.69661,41.359907],[-74.236547,41.14083]]]}},
  {"type":"Feature","id":"35","properties":{"name":"New Mexico"},"geometry":{"type":"Polygon","coordinates":[[[-107.421329,37.000263],[-106.868158,36.994786],[-104.337812,36.994786],[-103.001438,37.000263],[-103.001438,36.501861],[-103.039777,36.501861],[-103.045254,34.01533],[-103.067161,33.002096],[-103.067161,31.999816],[-106.616219,31.999816],[-106.643603,31.901231],[-106.528588,31.786216],[-108.210008,31.786216],[-108.210008,31.331629],[-109.04798,31.331629],[-109.042503,37.000263],[-107.421329,37.000263]]]}},
  {"type":"Feature","id":"36","properties":{"name":"New York"},"geometry":{"type":"Polygon","coordinates":[[[-73.343806,45.013027],[-73.332852,44.804903],[-73.387622,44.618687],[-73.294514,44.437948],[-73.321898,44.246255],[-73.436914,44.043608],[-73.349283,43.769761],[-73.404052,43.687607],[-73.245221,43.523299],[-73.278083,42.833204],[-73.267129,42.745573],[-73.508114,42.08834],[-73.486206,42.050002],[-73.55193,41.294184],[-73.48073,41.21203],[-73.727192,41.102491],[-73.655992,40.987475],[-73.22879,40.905321],[-73.141159,40.965568],[-72.774204,40.965568],[-72.587988,40.998429],[-72.28128,41.157261],[-72.259372,41.042245],[-72.100541,40.992952],[-72.467496,40.845075],[-73.239744,40.625997],[-73.562884,40.582182],[-73.776484,40.593136],[-73.935316,40.543843],[-74.022947,40.708151],[-73.902454,40.998429],[-74.236547,41.14083],[-74.69661,41.359907],[-74.740426,41.431108],[-74.89378,41.436584],[-75.074519,41.60637],[-75.052611,41.754247],[-75.173104,41.869263],[-75.249781,41.863786],[-75.35932,42.000709],[-79.76278,42.000709],[-79.76278,42.252649],[-79.76278,42.269079],[-79.149363,42.55388],[-79.050778,42.690804],[-78.853608,42.783912],[-78.930285,42.953697],[-79.012439,42.986559],[-79.072686,43.260406],[-78.486653,43.375421],[-77.966344,43.369944],[-77.75822,43.34256],[-77.533665,43.233021],[-77.391265,43.276836],[-76.958587,43.271359],[-76.695693,43.34256],[-76.41637,43.523299],[-76.235631,43.528776],[-76.230154,43.802623],[-76.137046,43.961454],[-76.3616,44.070993],[-76.312308,44.196962],[-75.912491,44.366748],[-75.764614,44.514625],[-75.282643,44.848718],[-74.828057,45.018503],[-74.148916,44.991119],[-73.343806,45.013027]]]}},
  {"type":"Feature","id":"37","properties":{"name":"North Carolina"},"geometry":{"type":"Polygon","coordinates":[[[-80.978661,36.562108],[-80.294043,36.545677],[-79.510841,36.5402],[-75.868676,36.551154],[-75.75366,36.151337],[-76.032984,36.189676],[-76.071322,36.140383],[-76.410893,36.080137],[-76.460185,36.025367],[-76.68474,36.008937],[-76.673786,35.937736],[-76.399939,35.987029],[-76.3616,35.943213],[-76.060368,35.992506],[-75.961783,35.899398],[-75.781044,35.937736],[-75.715321,35.696751],[-75.775568,35.581735],[-75.89606,35.570781],[-76.147999,35.324319],[-76.482093,35.313365],[-76.536862,35.14358],[-76.394462,34.973795],[-76.279446,34.940933],[-76.493047,34.661609],[-76.673786,34.694471],[-76.991448,34.667086],[-77.210526,34.60684],[-77.555573,34.415147],[-77.82942,34.163208],[-77.971821,33.845545],[-78.179944,33.916745],[-78.541422,33.851022],[-79.675149,34.80401],[-80.797922,34.820441],[-80.781491,34.935456],[-80.934845,35.105241],[-81.038907,35.044995],[-81.044384,35.149057],[-82.276696,35.198349],[-82.550543,35.160011],[-82.764143,35.066903],[-83.109191,35.00118],[-83.618546,34.984749],[-84.319594,34.990226],[-84.29221,35.225734],[-84.09504,35.247642],[-84.018363,35.41195],[-83.7719,35.559827],[-83.498053,35.565304],[-83.251591,35.718659],[-82.994175,35.773428],[-82.775097,35.997983],[-82.638174,36.063706],[-82.610789,35.965121],[-82.216449,36.156814],[-82.03571,36.118475],[-81.909741,36.304691],[-81.723525,36.353984],[-81.679709,36.589492],[-80.978661,36.562108]]]}},
  {"type":"Feature","id":"38","properties":{"name":"North Dakota"},"geometry":{"type":"Polygon","coordinates":[[[-97.228743,49.000239],[-97.097296,48.682577],[-97.16302,48.545653],[-97.130158,48.140359],[-97.053481,47.948667],[-96.856311,47.609096],[-96.823449,46.968294],[-96.785111,46.924479],[-96.801542,46.656109],[-96.719387,46.437031],[-96.598895,46.332969],[-96.560556,45.933153],[-104.047534,45.944106],[-104.042057,47.861036],[-104.047534,49.000239],[-97.228743,49.000239]]]}},
  {"type":"Feature","id":"39","properties":{"name":"Ohio"},"geometry":{"type":"Polygon","coordinates":[[[-80.518598,41.978802],[-80.518598,40.636951],[-80.666475,40.582182],[-80.595275,40.472643],[-80.600752,40.319289],[-80.737675,40.078303],[-80.830783,39.711348],[-81.219646,39.388209],[-81.345616,39.344393],[-81.455155,39.410117],[-81.57017,39.267716],[-81.685186,39.273193],[-81.811156,39.0815],[-81.783771,38.966484],[-81.887833,38.873376],[-82.03571,39.026731],[-82.221926,38.785745],[-82.172634,38.632391],[-82.293127,38.577622],[-82.331465,38.446175],[-82.594358,38.424267],[-82.731282,38.561191],[-82.846298,38.588575],[-82.890113,38.758361],[-83.032514,38.725499],[-83.142052,38.626914],[-83.519961,38.703591],[-83.678792,38.632391],[-83.903347,38.769315],[-84.215533,38.807653],[-84.231963,38.895284],[-84.43461,39.103408],[-84.817996,39.103408],[-84.801565,40.500028],[-84.807042,41.694001],[-83.454238,41.732339],[-83.065375,41.595416],[-82.933929,41.513262],[-82.835344,41.589939],[-82.616266,41.431108],[-82.479343,41.381815],[-82.013803,41.513262],[-81.739956,41.485877],[-81.444201,41.672093],[-81.011523,41.852832],[-80.518598,41.978802],[-80.518598,41.978802]]]}},
  {"type":"Feature","id":"40","properties":{"name":"Oklahoma"},"geometry":{"type":"Polygon","coordinates":[[[-100.087706,37.000263],[-94.616242,37.000263],[-94.616242,36.501861],[-94.430026,35.395519],[-94.484796,33.637421],[-94.868182,33.74696],[-94.966767,33.861976],[-95.224183,33.960561],[-95.289906,33.87293],[-95.547322,33.878407],[-95.602092,33.933176],[-95.8376,33.834591],[-95.936185,33.889361],[-96.149786,33.840068],[-96.346956,33.686714],[-96.423633,33.774345],[-96.631756,33.845545],[-96.850834,33.845545],[-96.922034,33.960561],[-97.173974,33.736006],[-97.256128,33.861976],[-97.371143,33.823637],[-97.458774,33.905791],[-97.694283,33.982469],[-97.869545,33.851022],[-97.946222,33.987946],[-98.088623,34.004376],[-98.170777,34.113915],[-98.36247,34.157731],[-98.488439,34.064623],[-98.570593,34.146777],[-98.767763,34.135823],[-98.986841,34.223454],[-99.189488,34.2125],[-99.260688,34.404193],[-99.57835,34.415147],[-99.698843,34.382285],[-99.923398,34.573978],[-100.000075,34.563024],[-100.000075,36.501861],[-101.812942,36.501861],[-103.001438,36.501861],[-103.001438,37.000263],[-102.042974,36.994786],[-100.087706,37.000263]]]}},
  {"type":"Feature","id":"41","properties":{"name":"Oregon"},"geometry":{"type":"Polygon","coordinates":[[[-123.211348,46.174138],[-123.11824,46.185092],[-122.904639,46.08103],[-122.811531,45.960537],[-122.762239,45.659305],[-122.247407,45.549767],[-121.809251,45.708598],[-121.535404,45.725029],[-121.217742,45.670259],[-121.18488,45.604536],[-120.637186,45.746937],[-120.505739,45.697644],[-120.209985,45.725029],[-119.963522,45.823614],[-119.525367,45.911245],[-119.125551,45.933153],[-118.988627,45.998876],[-116.918344,45.993399],[-116.78142,45.823614],[-116.545912,45.752413],[-116.463758,45.61549],[-116.671881,45.319735],[-116.732128,45.144473],[-116.847143,45.02398],[-116.830713,44.930872],[-116.934774,44.782995],[-117.038836,44.750133],[-117.241483,44.394132],[-117.170283,44.257209],[-116.97859,44.240778],[-116.896436,44.158624],[-117.027882,43.830007],[-117.027882,42.000709],[-118.698349,41.989755],[-120.001861,41.995232],[-121.037003,41.995232],[-122.378853,42.011663],[-123.233256,42.006186],[-124.213628,42.000709],[-124.356029,42.115725],[-124.432706,42.438865],[-124.416275,42.663419],[-124.553198,42.838681],[-124.454613,43.002989],[-124.383413,43.271359],[-124.235536,43.55616],[-124.169813,43.8081],[-124.060274,44.657025],[-124.076705,44.772041],[-123.97812,45.144473],[-123.939781,45.659305],[-123.994551,45.944106],[-123.945258,46.113892],[-123.545441,46.261769],[-123.370179,46.146753],[-123.211348,46.174138]]]}},
  {"type":"Feature","id":"42","properties":{"name":"Pennsylvania"},"geometry":{"type":"Polygon","coordinates":[[[-79.76278,42.252649],[-79.76278,42.000709],[-75.35932,42.000709],[-75.249781,41.863786],[-75.173104,41.869263],[-75.052611,41.754247],[-75.074519,41.60637],[-74.89378,41.436584],[-74.740426,41.431108],[-74.69661,41.359907],[-74.828057,41.288707],[-74.882826,41.179168],[-75.134765,40.971045],[-75.052611,40.866983],[-75.205966,40.691721],[-75.195012,40.576705],[-75.069042,40.543843],[-75.058088,40.417874],[-74.773287,40.215227],[-74.82258,40.127596],[-75.129289,39.963288],[-75.145719,39.88661],[-75.414089,39.804456],[-75.616736,39.831841],[-75.786521,39.722302],[-79.477979,39.722302],[-80.518598,39.722302],[-80.518598,40.636951],[-80.518598,41.978802],[-80.518598,41.978802],[-80.332382,42.033571],[-79.76278,42.269079],[-79.76278,42.252649]]]}},
  {"type":"Feature","id":"44","properties":{"name":"Rhode Island"},"geometry":{"type":"MultiPolygon","coordinates":[[[[-71.196845,41.67757],[-71.120168,41.496831],[-71.317338,41.474923],[-71.196845,41.67757]]],[[[-71.530939,42.01714],[-71.383061,42.01714],[-71.328292,41.781632],[-71.22423,41.710431],[-71.344723,41.726862],[-71.448785,41.578985],[-71.481646,41.370861],[-71.859555,41.321569],[-71.799309,41.414677],[-71.799309,42.006186],[-71.530939,42.01714]]]]}},
  {"type":"Feature","id":"45","properties":{"name":"South Carolina"},"geometry":{"type":"Polygon","coordinates":[[[-82.764143,35.066903],[-82.550543,35.160011],[-82.276696,35.198349],[-81.044384,35.149057],[-81.038907,35.044995],[-80.934845,35.105241],[-80.781491,34.935456],[-80.797922,34.820441],[-79.675149,34.80401],[-78.541422,33.851022],[-78.716684,33.80173],[-78.935762,33.637421],[-79.149363,33.380005],[-79.187701,33.171881],[-79.357487,33.007573],[-79.582041,33.007573],[-79.631334,32.887081],[-79.866842,32.755634],[-79.998289,32.613234],[-80.206412,32.552987],[-80.430967,32.399633],[-80.452875,32.328433],[-80.660998,32.246279],[-80.885553,32.032678],[-81.115584,32.120309],[-81.121061,32.290094],[-81.279893,32.558464],[-81.416816,32.629664],[-81.42777,32.843265],[-81.493493,33.007573],[-81.761863,33.160928],[-81.937125,33.347144],[-81.926172,33.462159],[-82.194542,33.631944],[-82.325988,33.81816],[-82.55602,33.94413],[-82.714851,34.152254],[-82.747713,34.26727],[-82.901067,34.486347],[-83.005129,34.469916],[-83.339222,34.683517],[-83.322791,34.787579],[-83.109191,35.00118],[-82.764143,35.066903]]]}},
  {"type":"Feature","id":"46","properties":{"name":"South Dakota"},"geometry":{"type":"Polygon","coordinates":[[[-104.047534,45.944106],[-96.560556,45.933153],[-96.582464,45.818137],[-96.856311,45.604536],[-96.681049,45.412843],[-96.451017,45.297827],[-96.451017,43.501391],[-96.582464,43.479483],[-96.527695,43.397329],[-96.560556,43.222067],[-96.434587,43.123482],[-96.511264,43.052282],[-96.544125,42.855112],[-96.631756,42.707235],[-96.44554,42.488157],[-96.626279,42.515542],[-96.692003,42.657942],[-97.217789,42.844158],[-97.688806,42.844158],[-97.831206,42.866066],[-97.951699,42.767481],[-98.466531,42.94822],[-98.499393,42.997512],[-101.626726,42.997512],[-103.324578,43.002989],[-104.053011,43.002989],[-104.058488,44.996596],[-104.042057,44.996596],[-104.047534,45.944106]]]}},
  {"type":"Feature","id":"47","properties":{"name":"Tennessee"},"geometry":{"type":"Polygon","coordinates":[[[-88.054868,36.496384],[-88.071299,36.677123],[-87.852221,36.633308],[-86.592525,36.655216],[-85.486183,36.616877],[-85.289013,36.627831],[-84.544149,36.594969],[-83.689746,36.584015],[-83.673316,36.600446],[-81.679709,36.589492],[-81.723525,36.353984],[-81.909741,36.304691],[-82.03571,36.118475],[-82.216449,36.156814],[-82.610789,35.965121],[-82.638174,36.063706],[-82.775097,35.997983],[-82.994175,35.773428],[-83.251591,35.718659],[-83.498053,35.565304],[-83.7719,35.559827],[-84.018363,35.41195],[-84.09504,35.247642],[-84.29221,35.225734],[-84.319594,34.990226],[-85.606675,34.984749],[-87.359296,35.00118],[-88.202745,34.995703],[-88.471115,34.995703],[-90.311367,34.995703],[-90.212782,35.023087],[-90.114197,35.198349],[-90.130628,35.439335],[-89.944412,35.603643],[-89.911551,35.756997],[-89.763673,35.811767],[-89.730812,35.997983],[-89.533642,36.249922],[-89.539119,36.496384],[-89.484349,36.496384],[-89.418626,36.496384],[-89.298133,36.507338],[-88.054868,36.496384]]]}},
  {"type":"Feature","id":"48","properties":{"name":"Texas"},"geometry":{"type":"Polygon","coordinates":[[[-101.812942,36.501861],[-100.000075,36.501861],[-100.000075,34.563024],[-99.923398,34.573978],[-99.698843,34.382285],[-99.57835,34.415147],[-99.260688,34.404193],[-99.189488,34.2125],[-98.986841,34.223454],[-98.767763,34.135823],[-98.570593,34.146777],[-98.488439,34.064623],[-98.36247,34.157731],[-98.170777,34.113915],[-98.088623,34.004376],[-97.946222,33.987946],[-97.869545,33.851022],[-97.694283,33.982469],[-97.458774,33.905791],[-97.371143,33.823637],[-97.256128,33.861976],[-97.173974,33.736006],[-96.922034,33.960561],[-96.850834,33.845545],[-96.631756,33.845545],[-96.423633,33.774345],[-96.346956,33.686714],[-96.149786,33.840068],[-95.936185,33.889361],[-95.8376,33.834591],[-95.602092,33.933176],[-95.547322,33.878407],[-95.289906,33.87293],[-95.224183,33.960561],[-94.966767,33.861976],[-94.868182,33.74696],[-94.484796,33.637421],[-94.380734,33.544313],[-94.183564,33.593606],[-94.041164,33.54979],[-94.041164,33.018527],[-94.041164,31.994339],[-93.822086,31.775262],[-93.816609,31.556184],[-93.542762,31.15089],[-93.526331,30.93729],[-93.630393,30.679874],[-93.728978,30.575812],[-93.696116,30.438888],[-93.767317,30.334826],[-93.690639,30.143133],[-93.926148,29.787132],[-93.838517,29.688547],[-94.002825,29.68307],[-94.523134,29.546147],[-94.70935,29.622824],[-94.742212,29.787132],[-94.873659,29.672117],[-94.966767,29.699501],[-95.016059,29.557101],[-94.911997,29.496854],[-94.895566,29.310638],[-95.081782,29.113469],[-95.383014,28.867006],[-95.985477,28.604113],[-96.045724,28.647929],[-96.226463,28.582205],[-96.23194,28.642452],[-96.478402,28.598636],[-96.593418,28.724606],[-96.664618,28.697221],[-96.401725,28.439805],[-96.593418,28.357651],[-96.774157,28.406943],[-96.801542,28.226204],[-97.026096,28.039988],[-97.256128,27.694941],[-97.404005,27.333463],[-97.513544,27.360848],[-97.540929,27.229401],[-97.425913,27.262263],[-97.480682,26.99937],[-97.557359,26.988416],[-97.562836,26.840538],[-97.469728,26.758384],[-97.442344,26.457153],[-97.332805,26.353091],[-97.30542,26.161398],[-97.217789,25.991613],[-97.524498,25.887551],[-97.650467,26.018997],[-97.885976,26.06829],[-98.198161,26.057336],[-98.466531,26.221644],[-98.669178,26.238075],[-98.822533,26.369522],[-99.030656,26.413337],[-99.173057,26.539307],[-99.266165,26.840538],[-99.446904,27.021277],[-99.424996,27.174632],[-99.50715,27.33894],[-99.479765,27.48134],[-99.605735,27.640172],[-99.709797,27.656603],[-99.879582,27.799003],[-99.934351,27.979742],[-100.082229,28.14405],[-100.29583,28.280974],[-100.399891,28.582205],[-100.498476,28.66436],[-100.629923,28.905345],[-100.673738,29.102515],[-100.799708,29.244915],[-101.013309,29.370885],[-101.062601,29.458516],[-101.259771,29.535193],[-101.413125,29.754271],[-101.851281,29.803563],[-102.114174,29.792609],[-102.338728,29.869286],[-102.388021,29.765225],[-102.629006,29.732363],[-102.809745,29.524239],[-102.919284,29.190146],[-102.97953,29.184669],[-103.116454,28.987499],[-103.280762,28.982022],[-103.527224,29.135376],[-104.146119,29.381839],[-104.266611,29.513285],[-104.507597,29.639255],[-104.677382,29.924056],[-104.688336,30.181472],[-104.858121,30.389596],[-104.896459,30.570335],[-105.005998,30.685351],[-105.394861,30.855136],[-105.602985,31.085167],[-105.77277,31.167321],[-105.953509,31.364491],[-106.205448,31.468553],[-106.38071,31.731446],[-106.528588,31.786216],[-106.643603,31.901231],[-106.616219,31.999816],[-103.067161,31.999816],[-103.067161,33.002096],[-103.045254,34.01533],[-103.039777,36.501861],[-103.001438,36.501861],[-101.812942,36.501861]]]}},
  {"type":"Feature","id":"49","properties":{"name":"Utah"},"geometry":{"type":"Polygon","coordinates":[[[-112.164359,41.995232],[-111.047063,42.000709],[-111.047063,40.998429],[-109.04798,40.998429],[-109.053457,39.125316],[-109.058934,38.27639],[-109.042503,38.166851],[-109.042503,37.000263],[-110.499369,37.00574],[-114.048427,37.000263],[-114.04295,41.995232],[-112.164359,41.995232]]]}},
  {"type":"Feature","id":"50","properties":{"name":"Vermont"},"geometry":{"type":"Polygon","coordinates":[[[-71.503554,45.013027],[-71.4926,44.914442],[-71.629524,44.750133],[-71.536416,44.585825],[-71.700724,44.41604],[-72.034817,44.322932],[-72.02934,44.07647],[-72.116971,43.994316],[-72.204602,43.769761],[-72.379864,43.572591],[-72.456542,43.150867],[-72.445588,43.008466],[-72.533219,42.953697],[-72.544173,42.80582],[-72.456542,42.729142],[-73.267129,42.745573],[-73.278083,42.833204],[-73.245221,43.523299],[-73.404052,43.687607],[-73.349283,43.769761],[-73.436914,44.043608],[-73.321898,44.246255],[-73.294514,44.437948],[-73.387622,44.618687],[-73.332852,44.804903],[-73.343806,45.013027],[-72.308664,45.002073],[-71.503554,45.013027]]]}},
  {"type":"Feature","id":"51","properties":{"name":"Virginia"},"geometry":{"type":"MultiPolygon","coordinates":[[[[-75.397659,38.013497],[-75.244304,38.029928],[-75.375751,37.860142],[-75.512674,37.799896],[-75.594828,37.569865],[-75.802952,37.197433],[-75.972737,37.120755],[-76.027507,37.257679],[-75.939876,37.564388],[-75.671506,37.95325],[-75.397659,38.013497]]],[[[-76.016553,37.95325],[-75.994645,37.95325],[-76.043938,37.95325],[-76.016553,37.95325]]],[[[-78.349729,39.464886],[-77.82942,39.130793],[-77.719881,39.322485],[-77.566527,39.306055],[-77.456988,39.223901],[-77.456988,39.076023],[-77.248864,39.026731],[-77.117418,38.933623],[-77.040741,38.791222],[-77.128372,38.632391],[-77.248864,38.588575],[-77.325542,38.446175],[-77.281726,38.342113],[-77.013356,38.374975],[-76.964064,38.216144],[-76.613539,38.15042],[-76.514954,38.024451],[-76.235631,37.887527],[-76.3616,37.608203],[-76.246584,37.389126],[-76.383508,37.285064],[-76.399939,37.159094],[-76.273969,37.082417],[-76.410893,36.961924],[-76.619016,37.120755],[-76.668309,37.065986],[-76.48757,36.95097],[-75.994645,36.923586],[-75.868676,36.551154],[-79.510841,36.5402],[-80.294043,36.545677],[-80.978661,36.562108],[-81.679709,36.589492],[-83.673316,36.600446],[-83.136575,36.742847],[-83.070852,36.852385],[-82.879159,36.890724],[-82.868205,36.978355],[-82.720328,37.044078],[-82.720328,37.120755],[-82.353373,37.268633],[-81.969987,37.537003],[-81.986418,37.454849],[-81.849494,37.285064],[-81.679709,37.20291],[-81.55374,37.208387],[-81.362047,37.339833],[-81.225123,37.235771],[-80.967707,37.290541],[-80.513121,37.482234],[-80.474782,37.421987],[-80.29952,37.509618],[-80.294043,37.690357],[-80.184505,37.849189],[-79.998289,37.997066],[-79.921611,38.177805],[-79.724442,38.364021],[-79.647764,38.594052],[-79.477979,38.457129],[-79.313671,38.413313],[-79.209609,38.495467],[-78.996008,38.851469],[-78.870039,38.763838],[-78.404499,39.169131],[-78.349729,39.464886]]]]}},
  {"type":"Feature","id":"53","properties":{"name":"Washington"},"geometry":{"type":"MultiPolygon","coordinates":[[[[-117.033359,49.000239],[-117.044313,47.762451],[-117.038836,46.426077],[-117.055267,46.343923],[-116.92382,46.168661],[-116.918344,45.993399],[-118.988627,45.998876],[-119.125551,45.933153],[-119.525367,45.911245],[-119.963522,45.823614],[-120.209985,45.725029],[-120.505739,45.697644],[-120.637186,45.746937],[-121.18488,45.604536],[-121.217742,45.670259],[-121.535404,45.725029],[-121.809251,45.708598],[-122.247407,45.549767],[-122.762239,45.659305],[-122.811531,45.960537],[-122.904639,46.08103],[-123.11824,46.185092],[-123.211348,46.174138],[-123.370179,46.146753],[-123.545441,46.261769],[-123.72618,46.300108],[-123.874058,46.239861],[-124.065751,46.327492],[-124.027412,46.464416],[-123.895966,46.535616],[-124.098612,46.74374],[-124.235536,47.285957],[-124.31769,47.357157],[-124.427229,47.740543],[-124.624399,47.88842],[-124.706553,48.184175],[-124.597014,48.381345],[-124.394367,48.288237],[-123.983597,48.162267],[-123.704273,48.167744],[-123.424949,48.118452],[-123.162056,48.167744],[-123.036086,48.080113],[-122.800578,48.08559],[-122.636269,47.866512],[-122.515777,47.882943],[-122.493869,47.587189],[-122.422669,47.318818],[-122.324084,47.346203],[-122.422669,47.576235],[-122.395284,47.800789],[-122.230976,48.030821],[-122.362422,48.123929],[-122.373376,48.288237],[-122.471961,48.468976],[-122.422669,48.600422],[-122.488392,48.753777],[-122.647223,48.775685],[-122.795101,48.8907],[-122.756762,49.000239],[-117.033359,49.000239]]],[[[-122.718423,48.310145],[-122.586977,48.35396],[-122.608885,48.151313],[-122.767716,48.227991],[-122.718423,48.310145]]],[[[-123.025132,48.583992],[-122.915593,48.715438],[-122.767716,48.556607],[-122.811531,48.419683],[-123.041563,48.458022],[-123.025132,48.583992]]]]}},
  {"type":"Feature","id":"54","properties":{"name":"West Virginia"},"geometry":{"type":"Polygon","coordinates":[[[-80.518598,40.636951],[-80.518598,39.722302],[-79.477979,39.722302],[-79.488933,39.20747],[-79.291763,39.300578],[-79.094593,39.470363],[-78.963147,39.437501],[-78.765977,39.585379],[-78.470222,39.514178],[-78.431884,39.623717],[-78.267575,39.61824],[-78.174467,39.694917],[-78.004682,39.601809],[-77.834897,39.601809],[-77.719881,39.322485],[-77.82942,39.130793],[-78.349729,39.464886],[-78.404499,39.169131],[-78.870039,38.763838],[-78.996008,38.851469],[-79.209609,38.495467],[-79.313671,38.413313],[-79.477979,38.457129],[-79.647764,38.594052],[-79.724442,38.364021],[-79.921611,38.177805],[-79.998289,37.997066],[-80.184505,37.849189],[-80.294043,37.690357],[-80.29952,37.509618],[-80.474782,37.421987],[-80.513121,37.482234],[-80.967707,37.290541],[-81.225123,37.235771],[-81.362047,37.339833],[-81.55374,37.208387],[-81.679709,37.20291],[-81.849494,37.285064],[-81.986418,37.454849],[-81.969987,37.537003],[-82.101434,37.553434],[-82.293127,37.668449],[-82.342419,37.783465],[-82.50125,37.931343],[-82.621743,38.123036],[-82.594358,38.424267],[-82.331465,38.446175],[-82.293127,38.577622],[-82.172634,38.632391],[-82.221926,38.785745],[-82.03571,39.026731],[-81.887833,38.873376],[-81.783771,38.966484],[-81.811156,39.0815],[-81.685186,39.273193],[-81.57017,39.267716],[-81.455155,39.410117],[-81.345616,39.344393],[-81.219646,39.388209],[-80.830783,39.711348],[-80.737675,40.078303],[-80.600752,40.319289],[-80.595275,40.472643],[-80.666475,40.582182],[-80.518598,40.636951]]]}},
  {"type":"Feature","id":"55","properties":{"name":"Wisconsin"},"geometry":{"type":"Polygon","coordinates":[[[-90.415429,46.568478],[-90.229213,46.508231],[-90.119674,46.338446],[-89.09001,46.135799],[-88.662808,45.987922],[-88.531362,46.020784],[-88.10416,45.922199],[-87.989145,45.796229],[-87.781021,45.675736],[-87.791975,45.500474],[-87.885083,45.363551],[-87.649574,45.341643],[-87.742682,45.199243],[-87.589328,45.095181],[-87.627666,44.974688],[-87.819359,44.95278],[-87.983668,44.722749],[-88.043914,44.563917],[-87.928898,44.536533],[-87.775544,44.640595],[-87.611236,44.837764],[-87.403112,44.914442],[-87.238804,45.166381],[-87.03068,45.22115],[-87.047111,45.089704],[-87.189511,44.969211],[-87.468835,44.552964],[-87.545512,44.322932],[-87.540035,44.158624],[-87.644097,44.103854],[-87.737205,43.8793],[-87.704344,43.687607],[-87.791975,43.561637],[-87.912467,43.249452],[-87.885083,43.002989],[-87.76459,42.783912],[-87.802929,42.493634],[-88.788778,42.493634],[-90.639984,42.510065],[-90.711184,42.636034],[-91.067185,42.75105],[-91.143862,42.909881],[-91.176724,43.134436],[-91.056231,43.254929],[-91.204109,43.353514],[-91.215062,43.501391],[-91.269832,43.616407],[-91.242447,43.775238],[-91.43414,43.994316],[-91.592971,44.032654],[-91.877772,44.202439],[-91.927065,44.333886],[-92.233773,44.443425],[-92.337835,44.552964],[-92.545959,44.569394],[-92.808852,44.750133],[-92.737652,45.117088],[-92.75956,45.286874],[-92.644544,45.440228],[-92.770513,45.566198],[-92.885529,45.577151],[-92.869098,45.719552],[-92.639067,45.933153],[-92.354266,46.015307],[-92.29402,46.075553],[-92.29402,46.667063],[-92.091373,46.749217],[-92.014696,46.705401],[-91.790141,46.694447],[-91.09457,46.864232],[-90.837154,46.95734],[-90.749522,46.88614],[-90.886446,46.754694],[-90.55783,46.584908],[-90.415429,46.568478]]]}},
  {"type":"Feature","id":"56","properties":{"name":"Wyoming"},"geometry":{"type":"Polygon","coordinates":[[[-109.080842,45.002073],[-105.91517,45.002073],[-104.058488,44.996596],[-104.053011,43.002989],[-104.053011,41.003906],[-105.728954,40.998429],[-107.919731,41.003906],[-109.04798,40.998429],[-111.047063,40.998429],[-111.047063,42.000709],[-111.047063,44.476286],[-111.05254,45.002073],[-109.080842,45.002073]]]}},
  {"type":"Feature","id":"72","properties":{"name":"Puerto Rico"},"geometry":{"type":"Polygon","coordinates":[[[-66.448338,17.984326],[-66.771478,18.006234],[-66.924832,17.929556],[-66.985078,17.973372],[-67.209633,17.956941],[-67.154863,18.19245],[-67.269879,18.362235],[-67.094617,18.515589],[-66.957694,18.488204],[-66.409999,18.488204],[-65.840398,18.433435],[-65.632274,18.367712],[-65.626797,18.203403],[-65.730859,18.186973],[-65.834921,18.017187],[-66.234737,17.929556],[-66.448338,17.984326]]]}}
]};

myChart.showLoading();
echarts.registerMap('USA', usaJson, {
  Alaska: {
    left: -131,
    top: 25,
    width: 15
  },
  Hawaii: {
    left: -110,
    top: 28,
    width: 5
  },
  'Puerto Rico': {
    left: -76,
    top: 26,
    width: 2
  }
});
option = {
  title: {
    text: 'USA Population Estimates (2012)',
    subtext: 'Data from www.census.gov',
    sublink: 'http://www.census.gov/popest/data/datasets.html',
    left: 'right'
  },
  tooltip: {
    trigger: 'item',
    showDelay: 0,
    transitionDuration: 0.2
  },
  visualMap: {
    left: 'right',
    min: 500000,
    max: 38000000,
    inRange: {
      color: [
        '#313695',
        '#4575b4',
        '#74add1',
        '#abd9e9',
        '#e0f3f8',
        '#ffffbf',
        '#fee090',
        '#fdae61',
        '#f46d43',
        '#d73027',
        '#a50026'
      ]
    },
    text: ['High', 'Low'],
    calculable: true
  },
  toolbox: {
    show: true,
    //orient: 'vertical',
    left: 'left',
    top: 'top',
    feature: {
      dataView: { readOnly: false },
      restore: {},
      saveAsImage: {}
    }
  },
  series: [
    {
      name: 'USA PopEstimates',
      type: 'map',
      roam: true,
      map: 'USA',
      emphasis: {
        label: {
          show: true
        }
      },
      data: [
        { name: 'Alabama', value: 4822023 },
        { name: 'Alaska', value: 731449 },
        { name: 'Arizona', value: 6553255 },
        { name: 'Arkansas', value: 2949131 },
        { name: 'California', value: 38041430 },
        { name: 'Colorado', value: 5187582 },
        { name: 'Connecticut', value: 3590347 },
        { name: 'Delaware', value: 917092 },
        { name: 'District of Columbia', value: 632323 },
        { name: 'Florida', value: 19317568 },
        { name: 'Georgia', value: 9919945 },
        { name: 'Hawaii', value: 1392313 },
        { name: 'Idaho', value: 1595728 },
        { name: 'Illinois', value: 12875255 },
        { name: 'Indiana', value: 6537334 },
        { name: 'Iowa', value: 3074186 },
        { name: 'Kansas', value: 2885905 },
        { name: 'Kentucky', value: 4380415 },
        { name: 'Louisiana', value: 4601893 },
        { name: 'Maine', value: 1329192 },
        { name: 'Maryland', value: 5884563 },
        { name: 'Massachusetts', value: 6646144 },
        { name: 'Michigan', value: 9883360 },
        { name: 'Minnesota', value: 5379139 },
        { name: 'Mississippi', value: 2984926 },
        { name: 'Missouri', value: 6021988 },
        { name: 'Montana', value: 1005141 },
        { name: 'Nebraska', value: 1855525 },
        { name: 'Nevada', value: 2758931 },
        { name: 'New Hampshire', value: 1320718 },
        { name: 'New Jersey', value: 8864590 },
        { name: 'New Mexico', value: 2085538 },
        { name: 'New York', value: 19570261 },
        { name: 'North Carolina', value: 9752073 },
        { name: 'North Dakota', value: 699628 },
        { name: 'Ohio', value: 11544225 },
        { name: 'Oklahoma', value: 3814820 },
        { name: 'Oregon', value: 3899353 },
        { name: 'Pennsylvania', value: 12763536 },
        { name: 'Rhode Island', value: 1050292 },
        { name: 'South Carolina', value: 4723723 },
        { name: 'South Dakota', value: 833354 },
        { name: 'Tennessee', value: 6456243 },
        { name: 'Texas', value: 26059203 },
        { name: 'Utah', value: 2855287 },
        { name: 'Vermont', value: 626011 },
        { name: 'Virginia', value: 8185867 },
        { name: 'Washington', value: 6897012 },
        { name: 'West Virginia', value: 1855413 },
        { name: 'Wisconsin', value: 5726398 },
        { name: 'Wyoming', value: 576412 },
        { name: 'Puerto Rico', value: 3667084 }
      ]
    }
  ]
};
myChart.setOption(option);
myChart.hideLoading();
```

##### II 测试示意图 USA

![image-20230418020122252](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230418020122252.png)

##### III 中国的省际 geo 数据

```json
{"type":"FeatureCollection","features":[
    {"type":"Feature","properties":{"adcode":110000,"name":"北京市","center":[116.405285,39.904989],"centroid":[116.41995,40.18994],"childrenNum":16,"level":"province","parent":{"adcode":100000},"subFeatureIndex":0,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[117.348611,40.581141],[117.389879,40.561593],[117.429915,40.576141],[117.412669,40.605226],[117.467487,40.649738],[117.467487,40.649738],[117.501364,40.636569],[117.514914,40.660181],[117.493973,40.675161],[117.408973,40.686961],[117.342451,40.673799],[117.319662,40.657911],[117.278394,40.664267],[117.208177,40.694675],[117.117018,40.70012],[117.11209,40.707379],[117.012308,40.693767],[116.964881,40.709647],[116.926692,40.745022],[116.924229,40.773581],[116.848468,40.839264],[116.81336,40.848319],[116.759773,40.889954],[116.713577,40.909858],[116.722201,40.927495],[116.677853,40.970888],[116.698795,41.021477],[116.688324,41.044501],[116.647672,41.059394],[116.615643,41.053076],[116.623034,41.021026],[116.598397,40.974503],[116.5676,40.992574],[116.519557,40.98128],[116.519557,40.98128],[116.455499,40.980828],[116.447492,40.953715],[116.477057,40.899907],[116.398216,40.90624],[116.370499,40.94377],[116.339702,40.929303],[116.334159,40.90443],[116.438253,40.81934],[116.46597,40.774487],[116.453651,40.765876],[116.316912,40.772221],[116.311369,40.754996],[116.273181,40.762703],[116.247311,40.791707],[116.22021,40.744115],[116.204812,40.740035],[116.171551,40.695582],[116.162928,40.662451],[116.133979,40.666536],[116.09887,40.630665],[116.005247,40.583868],[115.982457,40.578868],[115.971986,40.6025],[115.907929,40.617493],[115.885139,40.595229],[115.827857,40.587504],[115.819849,40.55932],[115.784741,40.55841],[115.755176,40.540221],[115.736082,40.503372],[115.781045,40.49336],[115.771806,40.443734],[115.864197,40.359422],[115.917784,40.354405],[115.95166,40.281852],[115.968907,40.264045],[115.89869,40.234354],[115.870356,40.185909],[115.855574,40.188652],[115.847567,40.147036],[115.806299,40.15344],[115.773654,40.176307],[115.75456,40.145663],[115.75456,40.145663],[115.599959,40.119583],[115.59072,40.096239],[115.527278,40.076092],[115.485394,40.040364],[115.454597,40.029825],[115.450286,39.992697],[115.428728,39.984443],[115.426264,39.950502],[115.481083,39.935819],[115.522967,39.899099],[115.515575,39.892212],[115.515575,39.892212],[115.526046,39.87568],[115.514344,39.837549],[115.567314,39.816407],[115.552532,39.794799],[115.50572,39.784222],[115.483547,39.798477],[115.483547,39.798477],[115.443511,39.785601],[115.439815,39.752022],[115.486626,39.741899],[115.491554,39.670074],[115.478619,39.650723],[115.478619,39.650723],[115.522351,39.640124],[115.518039,39.597252],[115.545756,39.618922],[115.587024,39.589873],[115.633836,39.599557],[115.633836,39.599557],[115.667712,39.615234],[115.698509,39.577881],[115.698509,39.577881],[115.699125,39.570039],[115.699125,39.570039],[115.716988,39.56035],[115.716988,39.56035],[115.718835,39.553891],[115.718835,39.553891],[115.720683,39.551122],[115.720683,39.551122],[115.722531,39.5442],[115.721299,39.543738],[115.722531,39.5442],[115.722531,39.543738],[115.721299,39.543738],[115.722531,39.543738],[115.724995,39.5442],[115.724995,39.5442],[115.738545,39.540046],[115.738545,39.539585],[115.738545,39.540046],[115.738545,39.539585],[115.752712,39.515581],[115.806299,39.510041],[115.806299,39.510041],[115.821081,39.522968],[115.821081,39.522968],[115.828473,39.541431],[115.867893,39.546507],[115.867893,39.546507],[115.91532,39.582955],[115.91532,39.582955],[115.910393,39.600479],[115.910393,39.600479],[115.957204,39.560812],[115.978146,39.595868],[115.995392,39.576958],[116.026189,39.587567],[116.036044,39.571884],[116.09887,39.575113],[116.130283,39.567732],[116.151841,39.583416],[116.198652,39.589412],[116.240536,39.564041],[116.257782,39.500344],[116.307057,39.488337],[116.337854,39.455536],[116.361876,39.455074],[116.361876,39.455074],[116.434557,39.442597],[116.454883,39.453226],[116.444412,39.482332],[116.411767,39.482794],[116.401912,39.528046],[116.443796,39.510041],[116.437637,39.526661],[116.478289,39.535431],[116.473361,39.552968],[116.50847,39.551122],[116.524484,39.596329],[116.592237,39.621227],[116.592237,39.621227],[116.620571,39.601863],[116.664918,39.605552],[116.723432,39.59264],[116.724048,39.59264],[116.723432,39.59264],[116.724048,39.59264],[116.726512,39.595407],[116.726512,39.595407],[116.709266,39.618],[116.748686,39.619844],[116.79057,39.595868],[116.812128,39.615695],[116.8497,39.66777],[116.906366,39.677444],[116.90575,39.688037],[116.889736,39.687576],[116.887272,39.72533],[116.916837,39.731314],[116.902055,39.763523],[116.949482,39.778703],[116.918069,39.84628],[116.907598,39.832494],[116.865714,39.843982],[116.812128,39.889916],[116.78441,39.891294],[116.782563,39.947749],[116.757925,39.967934],[116.781331,40.034866],[116.820135,40.02845],[116.831222,40.051359],[116.867562,40.041739],[116.927924,40.055024],[116.945171,40.04128],[117.025243,40.030283],[117.051728,40.059605],[117.105315,40.074261],[117.105315,40.074261],[117.140423,40.064185],[117.159517,40.077008],[117.204481,40.069681],[117.210024,40.082045],[117.224191,40.094865],[117.224191,40.094865],[117.254988,40.114548],[117.254988,40.114548],[117.254988,40.114548],[117.274082,40.105852],[117.307343,40.136971],[117.349227,40.136513],[117.367089,40.172649],[117.367089,40.173106],[117.367089,40.173106],[117.367089,40.172649],[117.383719,40.188195],[117.389879,40.227958],[117.351075,40.229786],[117.331365,40.289613],[117.295024,40.2782],[117.271618,40.325211],[117.271618,40.325211],[117.243285,40.369453],[117.226039,40.368997],[117.234046,40.417312],[117.263611,40.442367],[117.208793,40.501552],[117.262995,40.512927],[117.247597,40.539766],[117.269771,40.560684],[117.348611,40.581141],[117.348611,40.581141]]]]}},
    {"type":"Feature","properties":{"adcode":120000,"name":"天津市","center":[117.190182,39.125596],"centroid":[117.347043,39.288036],"childrenNum":16,"level":"province","parent":{"adcode":100000},"subFeatureIndex":1,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[117.765602,39.400527],[117.846906,39.407926],[117.871543,39.411625],[117.870311,39.455074],[117.899877,39.474479],[117.912195,39.517428],[117.912195,39.517428],[117.904804,39.533585],[117.933753,39.574191],[117.868464,39.59679],[117.829659,39.589873],[117.766834,39.598635],[117.753899,39.579726],[117.753899,39.579726],[117.745276,39.547892],[117.715711,39.529892],[117.707088,39.576036],[117.684914,39.58895],[117.654117,39.575113],[117.637486,39.603246],[117.66274,39.636437],[117.668899,39.666849],[117.627015,39.703693],[117.57774,39.726711],[117.595603,39.74604],[117.56111,39.754782],[117.546327,39.775943],[117.561726,39.799856],[117.529081,39.859144],[117.529081,39.859144],[117.508139,39.901854],[117.508139,39.901854],[117.512451,39.90874],[117.512451,39.90874],[117.513067,39.910576],[117.513067,39.910576],[117.514914,39.946832],[117.534625,39.954631],[117.546327,39.999116],[117.594987,39.994531],[117.594987,39.994531],[117.614697,39.97252],[117.671363,39.973896],[117.691073,39.984902],[117.756363,39.965181],[117.781616,39.966558],[117.781616,39.966558],[117.795167,39.996823],[117.795167,39.996823],[117.793319,40.005534],[117.793319,40.005534],[117.768681,40.022034],[117.768681,40.022034],[117.744044,40.018368],[117.74774,40.047236],[117.776073,40.059605],[117.752667,40.081588],[117.71879,40.082045],[117.71879,40.082045],[117.675059,40.082045],[117.655965,40.109514],[117.655965,40.109514],[117.654117,40.114548],[117.654117,40.114548],[117.651653,40.122786],[117.651653,40.122786],[117.613465,40.158014],[117.613465,40.158014],[117.609769,40.160301],[117.609769,40.160301],[117.576508,40.178593],[117.571581,40.219276],[117.548791,40.232527],[117.505059,40.227044],[117.450241,40.252627],[117.415748,40.248973],[117.389879,40.227958],[117.383719,40.188195],[117.367089,40.172649],[117.367089,40.173106],[117.367089,40.173106],[117.367089,40.172649],[117.349227,40.136513],[117.307343,40.136971],[117.274082,40.105852],[117.254988,40.114548],[117.254988,40.114548],[117.254988,40.114548],[117.224191,40.094865],[117.224191,40.094865],[117.210024,40.082045],[117.192162,40.066475],[117.198322,39.992697],[117.150894,39.944996],[117.162597,39.876598],[117.162597,39.876598],[117.227887,39.852712],[117.247597,39.860981],[117.251908,39.834332],[117.192162,39.832953],[117.156438,39.817326],[117.15767,39.796638],[117.205713,39.763984],[117.161981,39.748801],[117.165061,39.718886],[117.165061,39.718886],[117.177996,39.645194],[117.152742,39.623532],[117.10901,39.625375],[117.10901,39.625375],[117.016004,39.653949],[116.983359,39.638742],[116.983359,39.638742],[116.964265,39.64335],[116.948866,39.680668],[116.948866,39.680668],[116.944555,39.695405],[116.944555,39.695405],[116.932236,39.706456],[116.932236,39.706456],[116.90575,39.688037],[116.906366,39.677444],[116.8497,39.66777],[116.812128,39.615695],[116.808432,39.576497],[116.78749,39.554352],[116.819519,39.528507],[116.820751,39.482332],[116.785026,39.465702],[116.832454,39.435664],[116.876185,39.43474],[116.839845,39.413474],[116.840461,39.378326],[116.818287,39.3737],[116.829374,39.338994],[116.870642,39.357506],[116.889736,39.338068],[116.87249,39.291304],[116.881729,39.225966],[116.881729,39.225966],[116.855859,39.215766],[116.870026,39.153607],[116.909446,39.150822],[116.912526,39.110898],[116.91191,39.111362],[116.91191,39.111362],[116.912526,39.110898],[116.871874,39.054688],[116.812744,39.05097],[116.812744,39.05097],[116.783179,39.05097],[116.783179,39.05097],[116.754229,39.034701],[116.754229,39.034701],[116.754845,39.003084],[116.72836,38.975174],[116.708034,38.931892],[116.722201,38.896968],[116.723432,38.852706],[116.75115,38.831264],[116.737599,38.784629],[116.746222,38.754299],[116.794265,38.744498],[116.794265,38.744498],[116.858939,38.741231],[116.877417,38.680522],[116.948866,38.689398],[116.950714,38.689398],[116.95133,38.689398],[116.950714,38.689398],[116.948866,38.689398],[116.95133,38.689398],[117.038793,38.688464],[117.068358,38.680522],[117.055424,38.639398],[117.070822,38.608072],[117.109626,38.584685],[117.150894,38.617892],[117.183539,38.61836],[117.183539,38.61836],[117.213104,38.639866],[117.213104,38.639866],[117.258684,38.608072],[117.258684,38.608072],[117.238358,38.580943],[117.25314,38.556143],[117.368937,38.564566],[117.432379,38.601524],[117.47919,38.616489],[117.55803,38.613683],[117.639334,38.626776],[117.65658,38.66043],[117.729261,38.680055],[117.740964,38.700141],[117.740964,38.753833],[117.671363,38.772032],[117.646725,38.788827],[117.64611,38.828933],[117.752051,38.847579],[117.778536,38.869016],[117.847522,38.855502],[117.875855,38.920252],[117.898029,38.948649],[117.855529,38.957492],[117.837667,39.057011],[117.871543,39.122506],[117.96455,39.172631],[117.977485,39.206028],[118.032919,39.219939],[118.034767,39.218548],[118.064948,39.231065],[118.064948,39.256094],[118.036615,39.264898],[118.024296,39.289451],[118.024296,39.289451],[117.982412,39.298714],[117.982412,39.298714],[117.979333,39.300566],[117.979333,39.300566],[117.973173,39.312143],[117.973173,39.312143],[117.965782,39.314921],[117.965782,39.314921],[117.919587,39.318162],[117.919587,39.318162],[117.88879,39.332051],[117.854913,39.328348],[117.854297,39.328348],[117.854913,39.328348],[117.854297,39.328348],[117.850601,39.363984],[117.850601,39.363984],[117.810565,39.354729],[117.805022,39.373237],[117.784696,39.376938],[117.74466,39.354729],[117.670747,39.357969],[117.669515,39.322792],[117.594987,39.349176],[117.536472,39.338068],[117.521074,39.357043],[117.570965,39.404689],[117.601146,39.419485],[117.614081,39.407001],[117.668899,39.412087],[117.673211,39.386652],[117.699696,39.407463],[117.765602,39.400527]]],[[[117.805022,39.373237],[117.852449,39.380639],[117.846906,39.407926],[117.765602,39.400527],[117.784696,39.376938],[117.805022,39.373237]]]]}},
    {"type":"Feature","properties":{"adcode":130000,"name":"河北省","center":[114.502461,38.045474],"childrenNum":11,"level":"province","parent":{"adcode":100000},"subFeatureIndex":2,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[117.467487,40.649738],[117.412669,40.605226],[117.429915,40.576141],[117.389879,40.561593],[117.348611,40.581141],[117.348611,40.581141],[117.269771,40.560684],[117.247597,40.539766],[117.262995,40.512927],[117.208793,40.501552],[117.263611,40.442367],[117.234046,40.417312],[117.226039,40.368997],[117.243285,40.369453],[117.271618,40.325211],[117.271618,40.325211],[117.295024,40.2782],[117.331365,40.289613],[117.351075,40.229786],[117.389879,40.227958],[117.415748,40.248973],[117.450241,40.252627],[117.505059,40.227044],[117.548791,40.232527],[117.571581,40.219276],[117.576508,40.178593],[117.609769,40.160301],[117.609769,40.160301],[117.613465,40.158014],[117.613465,40.158014],[117.651653,40.122786],[117.651653,40.122786],[117.654117,40.114548],[117.654117,40.114548],[117.655965,40.109514],[117.655965,40.109514],[117.675059,40.082045],[117.71879,40.082045],[117.71879,40.082045],[117.752667,40.081588],[117.776073,40.059605],[117.74774,40.047236],[117.744044,40.018368],[117.768681,40.022034],[117.768681,40.022034],[117.793319,40.005534],[117.793319,40.005534],[117.795167,39.996823],[117.795167,39.996823],[117.781616,39.966558],[117.781616,39.966558],[117.756363,39.965181],[117.691073,39.984902],[117.671363,39.973896],[117.614697,39.97252],[117.594987,39.994531],[117.594987,39.994531],[117.546327,39.999116],[117.534625,39.954631],[117.514914,39.946832],[117.513067,39.910576],[117.513067,39.910576],[117.512451,39.90874],[117.512451,39.90874],[117.508139,39.901854],[117.508139,39.901854],[117.529081,39.859144],[117.529081,39.859144],[117.561726,39.799856],[117.546327,39.775943],[117.56111,39.754782],[117.595603,39.74604],[117.57774,39.726711],[117.627015,39.703693],[117.668899,39.666849],[117.66274,39.636437],[117.637486,39.603246],[117.654117,39.575113],[117.684914,39.58895],[117.707088,39.576036],[117.715711,39.529892],[117.745276,39.547892],[117.753899,39.579726],[117.753899,39.579726],[117.766834,39.598635],[117.829659,39.589873],[117.868464,39.59679],[117.933753,39.574191],[117.904804,39.533585],[117.912195,39.517428],[117.912195,39.517428],[117.899877,39.474479],[117.870311,39.455074],[117.871543,39.411625],[117.846906,39.407926],[117.852449,39.380639],[117.805022,39.373237],[117.810565,39.354729],[117.850601,39.363984],[117.850601,39.363984],[117.854297,39.328348],[117.854913,39.328348],[117.854297,39.328348],[117.854913,39.328348],[117.88879,39.332051],[117.919587,39.318162],[117.919587,39.318162],[117.965782,39.314921],[117.965782,39.314921],[117.973173,39.312143],[117.973173,39.312143],[117.979333,39.300566],[117.979333,39.300566],[117.982412,39.298714],[117.982412,39.298714],[118.024296,39.289451],[118.024296,39.289451],[118.036615,39.264898],[118.064948,39.256094],[118.064948,39.231065],[118.034767,39.218548],[118.026144,39.201854],[118.070492,39.213911],[118.077883,39.201854],[118.12531,39.182838],[118.162883,39.136433],[118.1906,39.080708],[118.225092,39.034701],[118.319331,39.009594],[118.366143,39.016104],[118.377845,38.971917],[118.491178,38.909077],[118.539837,38.910008],[118.604511,38.971452],[118.570634,38.999363],[118.533062,39.090928],[118.588497,39.107648],[118.578642,39.130863],[118.637156,39.157319],[118.76096,39.133648],[118.814546,39.138754],[118.857662,39.162888],[118.897082,39.151286],[118.920488,39.171703],[118.951285,39.178662],[118.896466,39.139683],[118.890307,39.118792],[118.926031,39.123435],[118.97777,39.163352],[119.023966,39.187012],[119.038132,39.211593],[119.096031,39.24219],[119.121284,39.281576],[119.185342,39.342234],[119.272805,39.363521],[119.317153,39.4107],[119.316537,39.437051],[119.269726,39.498497],[119.366428,39.734996],[119.474217,39.813189],[119.536427,39.809052],[119.520413,39.840306],[119.540739,39.888079],[119.588166,39.910576],[119.620195,39.904609],[119.642369,39.925264],[119.681789,39.922511],[119.726137,39.940867],[119.787115,39.950502],[119.820375,39.979399],[119.842549,39.956007],[119.872114,39.960594],[119.854252,39.98857],[119.845629,40.000949],[119.845629,40.000949],[119.854252,40.033033],[119.81668,40.050443],[119.81668,40.050443],[119.787115,40.041739],[119.787115,40.041739],[119.783419,40.046778],[119.783419,40.046778],[119.772332,40.08113],[119.736608,40.104936],[119.760629,40.136056],[119.745847,40.207851],[119.716898,40.195966],[119.671934,40.23938],[119.639289,40.231613],[119.639289,40.231613],[119.651608,40.271808],[119.598021,40.334335],[119.586934,40.375381],[119.604797,40.455119],[119.553674,40.502007],[119.572152,40.523846],[119.559217,40.547952],[119.503783,40.553864],[119.477913,40.533399],[119.429254,40.540221],[119.30237,40.530215],[119.256175,40.543404],[119.22045,40.569322],[119.230921,40.603863],[119.177951,40.609315],[119.162552,40.600228],[119.14469,40.632482],[119.184726,40.680153],[119.165632,40.69286],[119.115125,40.666536],[119.054763,40.664721],[119.028277,40.692406],[119.011031,40.687414],[118.96114,40.72008],[118.950053,40.747743],[118.895234,40.75409],[118.907553,40.775394],[118.878604,40.783098],[118.845959,40.822057],[118.873061,40.847866],[118.90201,40.960946],[118.916792,40.969984],[118.977154,40.959138],[118.977154,40.959138],[119.00056,40.967273],[119.013495,41.007479],[118.951901,41.018317],[118.937118,41.052625],[118.964836,41.079246],[119.037516,41.067516],[119.080632,41.095936],[119.081248,41.131555],[119.126212,41.138767],[119.189038,41.198234],[119.169943,41.222996],[119.204436,41.222546],[119.209364,41.244599],[119.2494,41.279689],[119.239545,41.31431],[119.211827,41.308016],[119.197661,41.282837],[119.168712,41.294978],[119.092951,41.293629],[118.980234,41.305769],[118.949437,41.317906],[118.890923,41.300823],[118.844727,41.342622],[118.843496,41.374516],[118.770199,41.352956],[118.741866,41.324198],[118.677192,41.35026],[118.629765,41.346666],[118.528135,41.355202],[118.412338,41.331838],[118.380309,41.312062],[118.348896,41.342622],[118.361215,41.384844],[118.348896,41.428384],[118.327338,41.450816],[118.271904,41.471446],[118.315636,41.512688],[118.302701,41.55256],[118.215237,41.59554],[118.206614,41.650566],[118.159187,41.67605],[118.155491,41.712694],[118.132702,41.733241],[118.140093,41.784134],[118.178281,41.814917],[118.236179,41.80778],[118.247266,41.773869],[118.29223,41.772976],[118.335346,41.845241],[118.340273,41.87243],[118.268824,41.930336],[118.306396,41.940131],[118.313788,41.98819],[118.291614,42.007759],[118.239875,42.024655],[118.286686,42.033991],[118.296541,42.057545],[118.27252,42.083312],[118.239259,42.092639],[118.212774,42.081091],[118.220165,42.058434],[118.194296,42.031324],[118.116687,42.037102],[118.155491,42.081091],[118.097593,42.105072],[118.089586,42.12283],[118.106216,42.172082],[118.033535,42.199132],[117.977485,42.229716],[117.974405,42.25054],[118.047702,42.280656],[118.060021,42.298364],[118.008898,42.346595],[118.024296,42.385064],[117.997811,42.416884],[117.874007,42.510038],[117.856761,42.539148],[117.797631,42.585431],[117.801326,42.612744],[117.779768,42.61847],[117.708935,42.588515],[117.667051,42.582347],[117.60053,42.603054],[117.537088,42.603054],[117.530313,42.590278],[117.475494,42.602613],[117.435458,42.585431],[117.434226,42.557224],[117.387415,42.517537],[117.387415,42.517537],[117.410205,42.519743],[117.413284,42.471645],[117.390495,42.461933],[117.332596,42.46105],[117.332596,42.46105],[117.275314,42.481797],[117.275314,42.481797],[117.188467,42.468114],[117.188467,42.468114],[117.135496,42.468996],[117.09546,42.484004],[117.080061,42.463699],[117.080061,42.463699],[117.01662,42.456193],[117.01662,42.456193],[117.009228,42.44957],[117.009228,42.44957],[117.005533,42.43367],[117.005533,42.43367],[116.99075,42.425719],[116.99075,42.425719],[116.974736,42.426603],[116.974736,42.426603],[116.97104,42.427486],[116.97104,42.427486],[116.944555,42.415116],[116.944555,42.415116],[116.936547,42.410256],[116.936547,42.410256],[116.921765,42.403628],[116.921765,42.403628],[116.910062,42.395231],[116.910062,42.395231],[116.910678,42.394789],[116.910678,42.394789],[116.886656,42.366496],[116.897743,42.297479],[116.918685,42.229716],[116.903287,42.190708],[116.789338,42.200462],[116.825062,42.155669],[116.850316,42.156556],[116.890352,42.092639],[116.879881,42.018431],[116.796113,41.977958],[116.748686,41.984186],[116.727744,41.951259],[116.66923,41.947698],[116.639049,41.929891],[116.597165,41.935679],[116.553433,41.928555],[116.510933,41.974399],[116.4826,41.975734],[116.453651,41.945917],[116.393289,41.942802],[116.414231,41.982407],[116.373579,42.009983],[116.310137,41.997086],[116.298434,41.96817],[116.223906,41.932562],[116.212819,41.885352],[116.194341,41.861734],[116.122892,41.861734],[116.106877,41.831419],[116.129051,41.805996],[116.09887,41.776547],[116.034196,41.782795],[116.007095,41.79752],[116.007095,41.797966],[116.007095,41.79752],[116.007095,41.797966],[115.994776,41.828743],[115.954124,41.874213],[115.916552,41.945027],[115.85311,41.927665],[115.834632,41.93835],[115.811226,41.912525],[115.726227,41.870202],[115.688038,41.867528],[115.654162,41.829189],[115.57409,41.80555],[115.519887,41.76762],[115.488474,41.760924],[115.42996,41.728775],[115.346808,41.712247],[115.319091,41.691693],[115.360975,41.661297],[115.345576,41.635807],[115.377605,41.603148],[115.310468,41.592854],[115.290142,41.622835],[115.26612,41.616124],[115.256881,41.580768],[115.20391,41.571367],[115.195287,41.602253],[115.0992,41.62373],[115.056085,41.602253],[115.016049,41.615229],[114.860832,41.60091],[114.895325,41.636255],[114.902716,41.695715],[114.89594,41.76762],[114.868839,41.813579],[114.922426,41.825175],[114.939056,41.846132],[114.923658,41.871093],[114.915035,41.960605],[114.9021,42.015763],[114.860832,42.054879],[114.86268,42.097967],[114.825723,42.139695],[114.79431,42.149457],[114.789383,42.130819],[114.75489,42.115727],[114.675434,42.12061],[114.647717,42.109512],[114.560254,42.132595],[114.510978,42.110844],[114.502355,42.06732],[114.480181,42.064654],[114.467863,42.025989],[114.511594,41.981962],[114.478334,41.951704],[114.419203,41.942356],[114.352066,41.953484],[114.343443,41.926774],[114.282465,41.863517],[114.200545,41.789934],[114.215328,41.75646],[114.206704,41.7386],[114.237501,41.698843],[114.215328,41.68499],[114.259059,41.623282],[114.226414,41.616572],[114.221487,41.582111],[114.230726,41.513584],[114.101379,41.537779],[114.032394,41.529715],[113.976959,41.505966],[113.953553,41.483553],[113.933227,41.487139],[113.919677,41.454404],[113.877793,41.431076],[113.871017,41.413126],[113.94493,41.392477],[113.92522,41.325546],[113.899351,41.316108],[113.914749,41.294529],[113.95109,41.282837],[113.971416,41.239649],[113.992357,41.269794],[114.016379,41.231999],[113.996669,41.19238],[113.960945,41.171211],[113.920293,41.172112],[113.877793,41.115777],[113.819279,41.09774],[113.868554,41.06887],[113.973263,40.983087],[113.994821,40.938798],[114.057647,40.925234],[114.041633,40.917546],[114.055183,40.867782],[114.073661,40.857372],[114.044712,40.830661],[114.080437,40.790348],[114.104458,40.797597],[114.103227,40.770861],[114.134639,40.737314],[114.162357,40.71373],[114.183299,40.67153],[114.236269,40.607043],[114.283081,40.590685],[114.273842,40.552954],[114.293552,40.55159],[114.282465,40.494725],[114.267066,40.474242],[114.299711,40.44009],[114.286161,40.425057],[114.31203,40.372645],[114.381015,40.36307],[114.390254,40.351213],[114.438914,40.371733],[114.481413,40.34802],[114.530688,40.345283],[114.510978,40.302851],[114.46971,40.268155],[114.406269,40.246232],[114.362537,40.249886],[114.292936,40.230242],[114.255364,40.236182],[114.235654,40.198252],[114.180219,40.191395],[114.135871,40.175392],[114.097683,40.193681],[114.073046,40.168533],[114.073046,40.168533],[114.101995,40.099901],[114.086596,40.071513],[114.045944,40.056856],[114.018227,40.103563],[113.989278,40.11226],[113.959097,40.033491],[113.910438,40.015618],[114.029314,39.985819],[114.028082,39.959218],[114.047176,39.916085],[114.067502,39.922511],[114.17406,39.897722],[114.212248,39.918839],[114.229494,39.899558],[114.204241,39.885324],[114.215943,39.8619],[114.286776,39.871087],[114.285545,39.858225],[114.395182,39.867412],[114.406885,39.833413],[114.390254,39.819165],[114.41674,39.775943],[114.409964,39.761683],[114.408117,39.652106],[114.431522,39.613851],[114.49558,39.608318],[114.51529,39.564964],[114.568877,39.573729],[114.532536,39.486027],[114.501739,39.476789],[114.496812,39.438437],[114.469095,39.400989],[114.466631,39.329736],[114.430906,39.307513],[114.437066,39.259337],[114.416124,39.242654],[114.47587,39.21623],[114.443841,39.174023],[114.388406,39.176807],[114.360689,39.134112],[114.369928,39.107648],[114.345907,39.075133],[114.252284,39.073739],[114.180835,39.049111],[114.157429,39.061194],[114.10877,39.052364],[114.082901,39.09325],[114.082901,39.09325],[114.064422,39.094179],[114.050872,39.135969],[114.006524,39.122971],[113.994821,39.095572],[113.961561,39.100681],[113.930148,39.063517],[113.898119,39.067699],[113.80696,38.989595],[113.776779,38.986804],[113.76754,38.959819],[113.776163,38.885788],[113.795257,38.860628],[113.855619,38.828933],[113.836525,38.795824],[113.839605,38.7585],[113.802648,38.763166],[113.775547,38.709949],[113.720728,38.713218],[113.70225,38.651551],[113.612939,38.645942],[113.603084,38.587024],[113.561816,38.558483],[113.546417,38.492936],[113.583374,38.459671],[113.537794,38.417952],[113.525475,38.383245],[113.557504,38.343359],[113.54457,38.270569],[113.570439,38.237202],[113.598772,38.22733],[113.64312,38.232031],[113.678844,38.20523],[113.711489,38.213695],[113.720728,38.174656],[113.797105,38.162894],[113.831597,38.16854],[113.811271,38.117707],[113.876561,38.055059],[113.872249,37.990471],[113.901198,37.984811],[113.936307,37.922993],[113.959097,37.906468],[113.976959,37.816696],[114.006524,37.813386],[114.044712,37.761834],[113.996669,37.730128],[113.993589,37.706932],[114.068118,37.721608],[114.12848,37.698409],[114.139567,37.675676],[114.115545,37.619761],[114.118625,37.59084],[114.036705,37.494037],[114.014531,37.42468],[113.973879,37.40329],[113.962792,37.355734],[113.90243,37.310052],[113.886416,37.239095],[113.853155,37.215269],[113.832213,37.167594],[113.773083,37.151855],[113.773699,37.107004],[113.758301,37.075497],[113.788482,37.059739],[113.771851,37.016745],[113.791561,36.98759],[113.76138,36.956034],[113.792793,36.894796],[113.773083,36.85506],[113.731815,36.858891],[113.731815,36.878521],[113.696707,36.882351],[113.676381,36.855539],[113.680692,36.789907],[113.600004,36.752995],[113.549497,36.752515],[113.535946,36.732373],[113.499606,36.740527],[113.465113,36.707908],[113.506997,36.705029],[113.476816,36.655114],[113.486671,36.635427],[113.54457,36.62342],[113.539642,36.594116],[113.569823,36.585947],[113.588917,36.547974],[113.559968,36.528741],[113.554425,36.494589],[113.587069,36.460904],[113.635729,36.451277],[113.670221,36.425278],[113.708409,36.423352],[113.731199,36.363135],[113.755221,36.366026],[113.813119,36.332285],[113.856851,36.329392],[113.84946,36.347711],[113.882104,36.353977],[113.911054,36.314927],[113.962792,36.353977],[113.981887,36.31782],[114.002828,36.334214],[114.056415,36.329392],[114.04348,36.303353],[114.080437,36.269585],[114.129096,36.280199],[114.175907,36.264759],[114.170364,36.245938],[114.170364,36.245938],[114.203009,36.245456],[114.2104,36.272962],[114.241197,36.251247],[114.257827,36.263794],[114.299095,36.245938],[114.345291,36.255591],[114.356378,36.230492],[114.408117,36.224699],[114.417356,36.205868],[114.466015,36.197658],[114.480181,36.177855],[114.533152,36.171575],[114.586739,36.141133],[114.588587,36.118414],[114.640326,36.137266],[114.720398,36.140166],[114.734564,36.15563],[114.771521,36.124699],[114.857752,36.127599],[114.858368,36.144516],[114.912571,36.140649],[114.926737,36.089403],[114.914419,36.052155],[114.998186,36.069572],[115.04623,36.112613],[115.048693,36.161912],[115.06286,36.178338],[115.104744,36.172058],[115.12507,36.209731],[115.1842,36.193312],[115.201446,36.210214],[115.201446,36.210214],[115.202678,36.209248],[115.202678,36.209248],[115.202678,36.208765],[115.202678,36.208765],[115.242098,36.19138],[115.279055,36.13775],[115.30246,36.127599],[115.312931,36.088436],[115.365902,36.099074],[115.376989,36.128083],[115.450902,36.152248],[115.465068,36.170125],[115.483547,36.148865],[115.474923,36.248352],[115.466916,36.258969],[115.466916,36.258969],[115.462605,36.276339],[115.417025,36.292742],[115.423185,36.32216],[115.366518,36.30914],[115.368982,36.342409],[115.340033,36.398307],[115.297533,36.413239],[115.317243,36.454166],[115.291374,36.460423],[115.272895,36.497476],[115.33141,36.550378],[115.355431,36.627262],[115.365902,36.621979],[115.420105,36.686795],[115.451518,36.702151],[115.479851,36.760187],[115.524815,36.763543],[115.683727,36.808117],[115.71206,36.883308],[115.75764,36.902453],[115.79706,36.968945],[115.776734,36.992848],[115.85619,37.060694],[115.888219,37.112254],[115.879596,37.150901],[115.91224,37.177132],[115.909777,37.20669],[115.969523,37.239572],[115.975682,37.337179],[116.024341,37.360015],[116.085935,37.373809],[116.106261,37.368577],[116.169087,37.384271],[116.193109,37.365723],[116.236224,37.361442],[116.2855,37.404241],[116.226369,37.428007],[116.243,37.447965],[116.224522,37.479791],[116.240536,37.489764],[116.240536,37.489764],[116.27626,37.466967],[116.290427,37.484065],[116.278724,37.524895],[116.295355,37.554316],[116.336007,37.581355],[116.36742,37.566177],[116.379738,37.522047],[116.38097,37.522522],[116.379738,37.522047],[116.38097,37.522522],[116.433941,37.473142],[116.448108,37.503059],[116.4826,37.521573],[116.575607,37.610754],[116.604556,37.624975],[116.66307,37.686096],[116.679085,37.728708],[116.724664,37.744327],[116.753613,37.77035],[116.753613,37.793054],[116.804736,37.848837],[116.837997,37.835132],[116.919301,37.846002],[117.027091,37.832296],[117.074518,37.848837],[117.150278,37.839385],[117.185387,37.849783],[117.271618,37.839858],[117.320278,37.861596],[117.400966,37.844584],[117.438538,37.854035],[117.481038,37.914967],[117.513067,37.94329],[117.524154,37.989527],[117.557414,38.046105],[117.557414,38.046105],[117.586979,38.071551],[117.704624,38.076262],[117.746508,38.12524],[117.771145,38.134655],[117.766834,38.158658],[117.789007,38.180772],[117.808718,38.22827],[117.848754,38.255062],[117.895565,38.301572],[117.948536,38.346644],[117.957775,38.376208],[117.937449,38.387936],[117.84629,38.368232],[117.781,38.373862],[117.730493,38.424985],[117.72495,38.457328],[117.678754,38.477008],[117.644878,38.52759],[117.68553,38.539293],[117.638102,38.54491],[117.639334,38.626776],[117.55803,38.613683],[117.47919,38.616489],[117.432379,38.601524],[117.368937,38.564566],[117.25314,38.556143],[117.238358,38.580943],[117.258684,38.608072],[117.258684,38.608072],[117.213104,38.639866],[117.213104,38.639866],[117.183539,38.61836],[117.183539,38.61836],[117.150894,38.617892],[117.109626,38.584685],[117.070822,38.608072],[117.055424,38.639398],[117.068358,38.680522],[117.038793,38.688464],[116.95133,38.689398],[116.948866,38.689398],[116.950714,38.689398],[116.95133,38.689398],[116.950714,38.689398],[116.948866,38.689398],[116.877417,38.680522],[116.858939,38.741231],[116.794265,38.744498],[116.794265,38.744498],[116.746222,38.754299],[116.737599,38.784629],[116.75115,38.831264],[116.723432,38.852706],[116.722201,38.896968],[116.708034,38.931892],[116.72836,38.975174],[116.754845,39.003084],[116.754229,39.034701],[116.754229,39.034701],[116.783179,39.05097],[116.783179,39.05097],[116.812744,39.05097],[116.812744,39.05097],[116.871874,39.054688],[116.912526,39.110898],[116.91191,39.111362],[116.91191,39.111362],[116.912526,39.110898],[116.909446,39.150822],[116.870026,39.153607],[116.855859,39.215766],[116.881729,39.225966],[116.881729,39.225966],[116.87249,39.291304],[116.889736,39.338068],[116.870642,39.357506],[116.829374,39.338994],[116.818287,39.3737],[116.840461,39.378326],[116.839845,39.413474],[116.876185,39.43474],[116.832454,39.435664],[116.785026,39.465702],[116.820751,39.482332],[116.819519,39.528507],[116.78749,39.554352],[116.808432,39.576497],[116.812128,39.615695],[116.79057,39.595868],[116.748686,39.619844],[116.709266,39.618],[116.726512,39.595407],[116.726512,39.595407],[116.724048,39.59264],[116.723432,39.59264],[116.724048,39.59264],[116.723432,39.59264],[116.664918,39.605552],[116.620571,39.601863],[116.592237,39.621227],[116.592237,39.621227],[116.524484,39.596329],[116.50847,39.551122],[116.473361,39.552968],[116.478289,39.535431],[116.437637,39.526661],[116.443796,39.510041],[116.401912,39.528046],[116.411767,39.482794],[116.444412,39.482332],[116.454883,39.453226],[116.434557,39.442597],[116.361876,39.455074],[116.361876,39.455074],[116.337854,39.455536],[116.307057,39.488337],[116.257782,39.500344],[116.240536,39.564041],[116.198652,39.589412],[116.151841,39.583416],[116.130283,39.567732],[116.09887,39.575113],[116.036044,39.571884],[116.026189,39.587567],[115.995392,39.576958],[115.978146,39.595868],[115.957204,39.560812],[115.910393,39.600479],[115.910393,39.600479],[115.91532,39.582955],[115.91532,39.582955],[115.867893,39.546507],[115.867893,39.546507],[115.828473,39.541431],[115.821081,39.522968],[115.821081,39.522968],[115.806299,39.510041],[115.806299,39.510041],[115.752712,39.515581],[115.738545,39.539585],[115.738545,39.540046],[115.738545,39.539585],[115.738545,39.540046],[115.724995,39.5442],[115.724995,39.5442],[115.722531,39.543738],[115.721299,39.543738],[115.722531,39.543738],[115.722531,39.5442],[115.721299,39.543738],[115.722531,39.5442],[115.720683,39.551122],[115.720683,39.551122],[115.718835,39.553891],[115.718835,39.553891],[115.716988,39.56035],[115.716988,39.56035],[115.699125,39.570039],[115.699125,39.570039],[115.698509,39.577881],[115.698509,39.577881],[115.667712,39.615234],[115.633836,39.599557],[115.633836,39.599557],[115.587024,39.589873],[115.545756,39.618922],[115.518039,39.597252],[115.522351,39.640124],[115.478619,39.650723],[115.478619,39.650723],[115.491554,39.670074],[115.486626,39.741899],[115.439815,39.752022],[115.443511,39.785601],[115.483547,39.798477],[115.483547,39.798477],[115.50572,39.784222],[115.552532,39.794799],[115.567314,39.816407],[115.514344,39.837549],[115.526046,39.87568],[115.515575,39.892212],[115.515575,39.892212],[115.522967,39.899099],[115.481083,39.935819],[115.426264,39.950502],[115.428728,39.984443],[115.450286,39.992697],[115.454597,40.029825],[115.485394,40.040364],[115.527278,40.076092],[115.59072,40.096239],[115.599959,40.119583],[115.75456,40.145663],[115.75456,40.145663],[115.773654,40.176307],[115.806299,40.15344],[115.847567,40.147036],[115.855574,40.188652],[115.870356,40.185909],[115.89869,40.234354],[115.968907,40.264045],[115.95166,40.281852],[115.917784,40.354405],[115.864197,40.359422],[115.771806,40.443734],[115.781045,40.49336],[115.736082,40.503372],[115.755176,40.540221],[115.784741,40.55841],[115.819849,40.55932],[115.827857,40.587504],[115.885139,40.595229],[115.907929,40.617493],[115.971986,40.6025],[115.982457,40.578868],[116.005247,40.583868],[116.09887,40.630665],[116.133979,40.666536],[116.162928,40.662451],[116.171551,40.695582],[116.204812,40.740035],[116.22021,40.744115],[116.247311,40.791707],[116.273181,40.762703],[116.311369,40.754996],[116.316912,40.772221],[116.453651,40.765876],[116.46597,40.774487],[116.438253,40.81934],[116.334159,40.90443],[116.339702,40.929303],[116.370499,40.94377],[116.398216,40.90624],[116.477057,40.899907],[116.447492,40.953715],[116.455499,40.980828],[116.519557,40.98128],[116.519557,40.98128],[116.5676,40.992574],[116.598397,40.974503],[116.623034,41.021026],[116.615643,41.053076],[116.647672,41.059394],[116.688324,41.044501],[116.698795,41.021477],[116.677853,40.970888],[116.722201,40.927495],[116.713577,40.909858],[116.759773,40.889954],[116.81336,40.848319],[116.848468,40.839264],[116.924229,40.773581],[116.926692,40.745022],[116.964881,40.709647],[117.012308,40.693767],[117.11209,40.707379],[117.117018,40.70012],[117.208177,40.694675],[117.278394,40.664267],[117.319662,40.657911],[117.342451,40.673799],[117.408973,40.686961],[117.493973,40.675161],[117.514914,40.660181],[117.501364,40.636569],[117.467487,40.649738],[117.467487,40.649738]]],[[[117.210024,40.082045],[117.204481,40.069681],[117.159517,40.077008],[117.140423,40.064185],[117.105315,40.074261],[117.105315,40.074261],[117.051728,40.059605],[117.025243,40.030283],[116.945171,40.04128],[116.927924,40.055024],[116.867562,40.041739],[116.831222,40.051359],[116.820135,40.02845],[116.781331,40.034866],[116.757925,39.967934],[116.782563,39.947749],[116.78441,39.891294],[116.812128,39.889916],[116.865714,39.843982],[116.907598,39.832494],[116.918069,39.84628],[116.949482,39.778703],[116.902055,39.763523],[116.916837,39.731314],[116.887272,39.72533],[116.889736,39.687576],[116.90575,39.688037],[116.932236,39.706456],[116.932236,39.706456],[116.944555,39.695405],[116.944555,39.695405],[116.948866,39.680668],[116.948866,39.680668],[116.964265,39.64335],[116.983359,39.638742],[116.983359,39.638742],[117.016004,39.653949],[117.10901,39.625375],[117.10901,39.625375],[117.152742,39.623532],[117.177996,39.645194],[117.165061,39.718886],[117.165061,39.718886],[117.161981,39.748801],[117.205713,39.763984],[117.15767,39.796638],[117.156438,39.817326],[117.192162,39.832953],[117.251908,39.834332],[117.247597,39.860981],[117.227887,39.852712],[117.162597,39.876598],[117.162597,39.876598],[117.150894,39.944996],[117.198322,39.992697],[117.192162,40.066475],[117.210024,40.082045]]],[[[117.784696,39.376938],[117.765602,39.400527],[117.699696,39.407463],[117.673211,39.386652],[117.668899,39.412087],[117.614081,39.407001],[117.601146,39.419485],[117.570965,39.404689],[117.521074,39.357043],[117.536472,39.338068],[117.594987,39.349176],[117.669515,39.322792],[117.670747,39.357969],[117.74466,39.354729],[117.784696,39.376938]]],[[[118.869365,39.142932],[118.82009,39.108576],[118.857662,39.098824],[118.869365,39.142932]]]]}},
    {"type":"Feature","properties":{"adcode":140000,"name":"山西省","center":[112.549248,37.857014],"centroid":[112.304436,37.618179],"childrenNum":11,"level":"province","parent":{"adcode":100000},"subFeatureIndex":3,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[110.379257,34.600612],[110.424837,34.588295],[110.488279,34.610956],[110.533242,34.583368],[110.610851,34.607508],[110.710017,34.605045],[110.749437,34.65232],[110.791937,34.649858],[110.824582,34.615881],[110.883712,34.64395],[110.903422,34.669056],[110.920052,34.730068],[110.976103,34.706456],[111.035233,34.740887],[111.118385,34.756623],[111.148566,34.807742],[111.232949,34.789559],[111.255123,34.819535],[111.29208,34.806759],[111.345666,34.831816],[111.389398,34.815113],[111.439289,34.838202],[111.502731,34.829851],[111.543999,34.853428],[111.570484,34.843114],[111.592042,34.881416],[111.617911,34.894671],[111.646861,34.938836],[111.681969,34.9511],[111.664107,34.984449],[111.740483,35.00455],[111.807005,35.032977],[111.810084,35.062374],[111.933272,35.083435],[111.97762,35.067272],[112.018888,35.068742],[112.039214,35.045717],[112.062004,35.056005],[112.05646,35.098615],[112.066315,35.153437],[112.03983,35.194039],[112.078634,35.219467],[112.058924,35.280069],[112.13838,35.271275],[112.21722,35.253195],[112.242474,35.234622],[112.304684,35.251728],[112.288053,35.219956],[112.36751,35.219956],[112.390915,35.239021],[112.513487,35.218489],[112.637291,35.225822],[112.628052,35.263457],[112.720443,35.206265],[112.772798,35.207732],[112.822073,35.258082],[112.884283,35.243909],[112.934174,35.262968],[112.936022,35.284466],[112.992072,35.29619],[112.985913,35.33965],[112.996384,35.362104],[113.067217,35.353806],[113.126347,35.332327],[113.149137,35.350878],[113.165151,35.412845],[113.185477,35.409431],[113.189789,35.44893],[113.243375,35.449418],[113.304353,35.426989],[113.31236,35.481101],[113.348085,35.468429],[113.391817,35.506925],[113.439244,35.507412],[113.49899,35.532254],[113.513773,35.57364],[113.55812,35.621816],[113.547649,35.656835],[113.578446,35.633491],[113.625258,35.632518],[113.622794,35.674825],[113.592613,35.691838],[113.587685,35.736542],[113.604932,35.797727],[113.582758,35.818111],[113.660982,35.837035],[113.637576,35.870019],[113.654207,35.931586],[113.648663,35.994073],[113.678844,35.985841],[113.694859,36.026991],[113.660366,36.034735],[113.68562,36.056026],[113.671453,36.115514],[113.655439,36.125182],[113.712721,36.129533],[113.705946,36.148865],[113.651127,36.174473],[113.697939,36.181719],[113.681924,36.216491],[113.716417,36.262347],[113.712105,36.303353],[113.736127,36.324571],[113.731199,36.363135],[113.708409,36.423352],[113.670221,36.425278],[113.635729,36.451277],[113.587069,36.460904],[113.554425,36.494589],[113.559968,36.528741],[113.588917,36.547974],[113.569823,36.585947],[113.539642,36.594116],[113.54457,36.62342],[113.486671,36.635427],[113.476816,36.655114],[113.506997,36.705029],[113.465113,36.707908],[113.499606,36.740527],[113.535946,36.732373],[113.549497,36.752515],[113.600004,36.752995],[113.680692,36.789907],[113.676381,36.855539],[113.696707,36.882351],[113.731815,36.878521],[113.731815,36.858891],[113.773083,36.85506],[113.792793,36.894796],[113.76138,36.956034],[113.791561,36.98759],[113.771851,37.016745],[113.788482,37.059739],[113.758301,37.075497],[113.773699,37.107004],[113.773083,37.151855],[113.832213,37.167594],[113.853155,37.215269],[113.886416,37.239095],[113.90243,37.310052],[113.962792,37.355734],[113.973879,37.40329],[114.014531,37.42468],[114.036705,37.494037],[114.118625,37.59084],[114.115545,37.619761],[114.139567,37.675676],[114.12848,37.698409],[114.068118,37.721608],[113.993589,37.706932],[113.996669,37.730128],[114.044712,37.761834],[114.006524,37.813386],[113.976959,37.816696],[113.959097,37.906468],[113.936307,37.922993],[113.901198,37.984811],[113.872249,37.990471],[113.876561,38.055059],[113.811271,38.117707],[113.831597,38.16854],[113.797105,38.162894],[113.720728,38.174656],[113.711489,38.213695],[113.678844,38.20523],[113.64312,38.232031],[113.598772,38.22733],[113.570439,38.237202],[113.54457,38.270569],[113.557504,38.343359],[113.525475,38.383245],[113.537794,38.417952],[113.583374,38.459671],[113.546417,38.492936],[113.561816,38.558483],[113.603084,38.587024],[113.612939,38.645942],[113.70225,38.651551],[113.720728,38.713218],[113.775547,38.709949],[113.802648,38.763166],[113.839605,38.7585],[113.836525,38.795824],[113.855619,38.828933],[113.795257,38.860628],[113.776163,38.885788],[113.76754,38.959819],[113.776779,38.986804],[113.80696,38.989595],[113.898119,39.067699],[113.930148,39.063517],[113.961561,39.100681],[113.994821,39.095572],[114.006524,39.122971],[114.050872,39.135969],[114.064422,39.094179],[114.082901,39.09325],[114.082901,39.09325],[114.10877,39.052364],[114.157429,39.061194],[114.180835,39.049111],[114.252284,39.073739],[114.345907,39.075133],[114.369928,39.107648],[114.360689,39.134112],[114.388406,39.176807],[114.443841,39.174023],[114.47587,39.21623],[114.416124,39.242654],[114.437066,39.259337],[114.430906,39.307513],[114.466631,39.329736],[114.469095,39.400989],[114.496812,39.438437],[114.501739,39.476789],[114.532536,39.486027],[114.568877,39.573729],[114.51529,39.564964],[114.49558,39.608318],[114.431522,39.613851],[114.408117,39.652106],[114.409964,39.761683],[114.41674,39.775943],[114.390254,39.819165],[114.406885,39.833413],[114.395182,39.867412],[114.285545,39.858225],[114.286776,39.871087],[114.215943,39.8619],[114.204241,39.885324],[114.229494,39.899558],[114.212248,39.918839],[114.17406,39.897722],[114.067502,39.922511],[114.047176,39.916085],[114.028082,39.959218],[114.029314,39.985819],[113.910438,40.015618],[113.959097,40.033491],[113.989278,40.11226],[114.018227,40.103563],[114.045944,40.056856],[114.086596,40.071513],[114.101995,40.099901],[114.073046,40.168533],[114.073046,40.168533],[114.097683,40.193681],[114.135871,40.175392],[114.180219,40.191395],[114.235654,40.198252],[114.255364,40.236182],[114.292936,40.230242],[114.362537,40.249886],[114.406269,40.246232],[114.46971,40.268155],[114.510978,40.302851],[114.530688,40.345283],[114.481413,40.34802],[114.438914,40.371733],[114.390254,40.351213],[114.381015,40.36307],[114.31203,40.372645],[114.286161,40.425057],[114.299711,40.44009],[114.267066,40.474242],[114.282465,40.494725],[114.293552,40.55159],[114.273842,40.552954],[114.283081,40.590685],[114.236269,40.607043],[114.183299,40.67153],[114.162357,40.71373],[114.134639,40.737314],[114.084748,40.729605],[114.063806,40.706925],[114.07243,40.679246],[114.041633,40.608861],[114.076741,40.575686],[114.080437,40.547952],[114.061959,40.52885],[114.011452,40.515657],[113.948626,40.514747],[113.890112,40.466503],[113.850691,40.460583],[113.794641,40.517932],[113.763228,40.473787],[113.688699,40.448288],[113.559968,40.348476],[113.500222,40.334335],[113.387505,40.319279],[113.316672,40.319736],[113.27602,40.388601],[113.251382,40.413211],[113.083231,40.374925],[113.03334,40.368997],[112.898449,40.329317],[112.848558,40.206937],[112.744464,40.167161],[112.712436,40.178593],[112.6299,40.235725],[112.511639,40.269068],[112.456205,40.300112],[112.418017,40.295091],[112.349031,40.257194],[112.310227,40.256281],[112.299756,40.21105],[112.232619,40.169905],[112.232003,40.133311],[112.183344,40.083877],[112.182112,40.061437],[112.142076,40.027076],[112.133453,40.001866],[112.07617,39.919298],[112.042294,39.886243],[112.012729,39.827438],[111.970229,39.796638],[111.959758,39.692642],[111.925265,39.66731],[111.9382,39.623071],[111.87907,39.606013],[111.842729,39.620305],[111.783599,39.58895],[111.722621,39.606013],[111.659179,39.641507],[111.625303,39.633672],[111.525521,39.662242],[111.497187,39.661781],[111.445448,39.640124],[111.460847,39.606935],[111.441137,39.59679],[111.422043,39.539123],[111.431282,39.508656],[111.372152,39.479099],[111.358601,39.432428],[111.337043,39.420872],[111.171971,39.423183],[111.143022,39.407926],[111.125776,39.366297],[111.159037,39.362596],[111.155341,39.338531],[111.186138,39.35149],[111.179363,39.326959],[111.202152,39.305197],[111.247732,39.302419],[111.213239,39.257021],[111.219399,39.244044],[111.163348,39.152678],[111.173819,39.135041],[111.147334,39.100681],[111.138095,39.064447],[111.094363,39.030053],[111.038313,39.020289],[110.998276,38.998433],[110.980414,38.970056],[111.009979,38.932823],[111.016755,38.889981],[110.995813,38.868084],[111.009363,38.847579],[110.965016,38.755699],[110.915125,38.704345],[110.916357,38.673981],[110.880632,38.626776],[110.898494,38.587024],[110.920052,38.581878],[110.907733,38.521035],[110.870777,38.510265],[110.874473,38.453579],[110.840596,38.439986],[110.796864,38.453579],[110.77777,38.440924],[110.746973,38.366355],[110.701394,38.353215],[110.661358,38.308617],[110.601612,38.308147],[110.57759,38.297345],[110.565887,38.215105],[110.528315,38.211814],[110.509221,38.192061],[110.519692,38.130889],[110.501829,38.097929],[110.507989,38.013107],[110.528315,37.990471],[110.522771,37.955088],[110.59422,37.922049],[110.680452,37.790216],[110.735886,37.77035],[110.750669,37.736281],[110.716792,37.728708],[110.706321,37.705511],[110.775306,37.680886],[110.793169,37.650567],[110.763604,37.639668],[110.771611,37.594634],[110.795017,37.558586],[110.770995,37.538184],[110.759292,37.474567],[110.740198,37.44939],[110.644111,37.435135],[110.630561,37.372858],[110.641648,37.360015],[110.695234,37.34955],[110.678604,37.317668],[110.690307,37.287201],[110.661974,37.281963],[110.651503,37.256722],[110.590525,37.187145],[110.53509,37.138021],[110.535706,37.115118],[110.49567,37.086956],[110.460561,37.044932],[110.417446,37.027257],[110.426685,37.008621],[110.382953,37.022001],[110.381721,37.002408],[110.424221,36.963685],[110.408823,36.892403],[110.376178,36.882351],[110.424221,36.855539],[110.406975,36.824886],[110.423605,36.818179],[110.407591,36.776007],[110.447011,36.737649],[110.438388,36.685835],[110.402663,36.697352],[110.394656,36.676716],[110.426685,36.657514],[110.447627,36.621018],[110.496902,36.582102],[110.488895,36.556628],[110.503677,36.488335],[110.47288,36.453203],[110.489511,36.430094],[110.487047,36.393972],[110.459946,36.327946],[110.474112,36.306729],[110.474112,36.248352],[110.45625,36.22663],[110.447011,36.164328],[110.467953,36.074893],[110.491974,36.034735],[110.49259,35.994073],[110.516612,35.971796],[110.502445,35.947575],[110.516612,35.918501],[110.511684,35.879718],[110.549257,35.877778],[110.550489,35.838005],[110.571431,35.800639],[110.57759,35.701559],[110.609619,35.632031],[110.589293,35.602355],[110.567735,35.539559],[110.531394,35.511309],[110.477808,35.413821],[110.45009,35.327933],[110.374946,35.251728],[110.378642,35.210666],[110.364475,35.197952],[110.373714,35.134351],[110.320743,35.00504],[110.262229,34.944233],[110.230816,34.880925],[110.246831,34.789068],[110.243135,34.725641],[110.229584,34.692679],[110.269004,34.629671],[110.29549,34.610956],[110.379257,34.600612]]]]}},
    {"type":"Feature","properties":{"adcode":150000,"name":"内蒙古自治区","center":[111.670801,40.818311],"centroid":[114.077429,44.331087],"childrenNum":12,"level":"province","parent":{"adcode":100000},"subFeatureIndex":4,"acroutes":[100000]},"geometry":{"type":"Polygon","coordinates":[[[97.172903,42.795257],[97.371235,42.457076],[97.500582,42.243894],[97.653335,41.986856],[97.84674,41.656379],[97.613915,41.477276],[97.629314,41.440498],[97.903407,41.168057],[97.971776,41.09774],[98.142391,41.001607],[98.184891,40.988056],[98.25018,40.93925],[98.333332,40.918903],[98.344419,40.568413],[98.627751,40.677884],[98.569853,40.746836],[98.668403,40.773128],[98.689345,40.691952],[98.72199,40.657911],[98.762642,40.639748],[98.802678,40.607043],[98.80699,40.660181],[98.790975,40.705564],[98.984996,40.782644],[99.041662,40.693767],[99.102025,40.676522],[99.12543,40.715091],[99.172858,40.747289],[99.174705,40.858278],[99.565827,40.846961],[99.673,40.93292],[99.985897,40.909858],[100.057346,40.908049],[100.107853,40.875475],[100.224882,40.727337],[100.237201,40.716905],[100.242744,40.618855],[100.169447,40.541131],[100.169447,40.277743],[100.007455,40.20008],[99.955716,40.150695],[99.927383,40.063727],[99.841152,40.013326],[99.751225,40.006909],[99.714268,39.972061],[99.533182,39.891753],[99.491298,39.884406],[99.459885,39.898181],[99.440791,39.885783],[99.469124,39.875221],[99.672384,39.888079],[99.822058,39.860063],[99.904593,39.785601],[99.958796,39.769504],[100.040716,39.757083],[100.128179,39.702312],[100.250135,39.685274],[100.314193,39.606935],[100.301258,39.572345],[100.326512,39.509118],[100.44354,39.485565],[100.500823,39.481408],[100.498975,39.400527],[100.606764,39.387577],[100.707778,39.404689],[100.842053,39.405614],[100.842669,39.199999],[100.864227,39.106719],[100.829118,39.075133],[100.835278,39.025869],[100.875314,39.002619],[100.901799,39.030053],[100.961545,39.005874],[100.969553,38.946788],[101.117378,38.975174],[101.228863,39.020754],[101.198682,38.943064],[101.237486,38.907214],[101.24303,38.860628],[101.33542,38.847113],[101.34158,38.822406],[101.307087,38.80282],[101.331109,38.777164],[101.412413,38.764099],[101.562702,38.713218],[101.601506,38.65529],[101.672955,38.6908],[101.777049,38.66043],[101.873751,38.733761],[101.941505,38.808883],[102.075164,38.891378],[102.045599,38.904885],[101.955055,38.985874],[101.926106,39.000758],[101.833715,39.08907],[101.902701,39.111827],[102.012338,39.127149],[102.050526,39.141075],[102.276576,39.188868],[102.3548,39.231993],[102.45335,39.255167],[102.579002,39.183301],[102.616574,39.171703],[102.883892,39.120649],[103.007696,39.099753],[103.133347,39.192579],[103.188166,39.215302],[103.259615,39.263971],[103.344615,39.331588],[103.428998,39.353341],[103.595302,39.386652],[103.728961,39.430117],[103.85338,39.461543],[103.955626,39.456923],[104.089901,39.419947],[104.073271,39.351953],[104.047401,39.297788],[104.171205,39.160567],[104.207546,39.083495],[104.190915,39.042139],[104.196459,38.9882],[104.173053,38.94446],[104.044322,38.895105],[104.011677,38.85923],[103.85954,38.64454],[103.416063,38.404821],[103.465339,38.353215],[103.507838,38.280905],[103.53494,38.156776],[103.368636,38.08898],[103.362477,38.037621],[103.40744,37.860651],[103.627947,37.797783],[103.683381,37.777919],[103.841062,37.64725],[103.874938,37.604117],[103.935916,37.572818],[104.089285,37.465067],[104.183524,37.406618],[104.237727,37.411847],[104.287002,37.428007],[104.322726,37.44844],[104.407726,37.464592],[104.419429,37.511604],[104.433595,37.515402],[104.623305,37.522522],[104.805007,37.539133],[104.866601,37.566651],[105.027977,37.580881],[105.111128,37.633981],[105.187505,37.657674],[105.221998,37.677097],[105.315004,37.702197],[105.4037,37.710246],[105.467141,37.695094],[105.598952,37.699356],[105.616199,37.722555],[105.622358,37.777919],[105.677177,37.771769],[105.760944,37.799674],[105.80406,37.862068],[105.799749,37.939986],[105.840401,38.004147],[105.780655,38.084741],[105.76772,38.121474],[105.775111,38.186887],[105.802828,38.220277],[105.842248,38.240962],[105.86627,38.296406],[105.821307,38.366824],[105.835473,38.387467],[105.827466,38.432486],[105.850872,38.443736],[105.836705,38.476071],[105.863806,38.53508],[105.856415,38.569714],[105.874277,38.593105],[105.852719,38.641735],[105.894603,38.696405],[105.88598,38.716953],[105.908154,38.737496],[105.909386,38.791159],[105.992538,38.857366],[105.97098,38.909077],[106.021487,38.953769],[106.060907,38.96866],[106.087392,39.006339],[106.078153,39.026333],[106.096631,39.084889],[106.145907,39.153142],[106.170544,39.163352],[106.192718,39.142932],[106.251232,39.131327],[106.285109,39.146181],[106.29558,39.167992],[106.280181,39.262118],[106.402753,39.291767],[106.511774,39.272311],[106.525325,39.308439],[106.556122,39.322329],[106.602318,39.37555],[106.643586,39.357969],[106.683622,39.357506],[106.751375,39.381564],[106.781556,39.371849],[106.806809,39.318625],[106.806193,39.277407],[106.790795,39.241263],[106.795723,39.214375],[106.825288,39.19397],[106.859164,39.107648],[106.878874,39.091392],[106.933693,39.076527],[106.96757,39.054688],[106.971881,39.026333],[106.954019,38.941202],[106.837606,38.847579],[106.756302,38.748699],[106.709491,38.718821],[106.66268,38.601524],[106.647897,38.470917],[106.599854,38.389812],[106.482209,38.319417],[106.555506,38.263521],[106.627571,38.232501],[106.654672,38.22921],[106.737824,38.197706],[106.779092,38.171833],[106.858548,38.156306],[106.942316,38.132302],[107.010069,38.120532],[107.051337,38.122886],[107.071047,38.138892],[107.119091,38.134185],[107.138801,38.161011],[107.19054,38.153953],[107.240431,38.111586],[107.33159,38.086625],[107.3938,38.014993],[107.440611,37.995659],[107.411662,37.948009],[107.448618,37.933378],[107.49235,37.944706],[107.560719,37.893717],[107.65003,37.86443],[107.684523,37.888522],[107.732566,37.84931],[107.842819,37.828987],[107.884703,37.808186],[107.982022,37.787378],[107.993109,37.735335],[108.025753,37.696041],[108.012819,37.66857],[108.025137,37.649619],[108.055318,37.652462],[108.134159,37.622131],[108.193905,37.638246],[108.205608,37.655779],[108.24626,37.665728],[108.293071,37.656726],[108.301078,37.640616],[108.422418,37.648672],[108.485244,37.678044],[108.532671,37.690832],[108.628142,37.651988],[108.699591,37.669518],[108.720533,37.683728],[108.777815,37.683728],[108.791982,37.700303],[108.784591,37.764673],[108.799989,37.784068],[108.791982,37.872934],[108.798141,37.93385],[108.82709,37.989056],[108.797525,38.04799],[108.830786,38.049875],[108.883141,38.01405],[108.893612,37.978207],[108.93488,37.922521],[108.9743,37.931962],[108.982923,37.964053],[109.018648,37.971602],[109.037742,38.021593],[109.06977,38.023008],[109.050676,38.055059],[109.069155,38.091336],[108.964445,38.154894],[108.938575,38.207582],[108.976148,38.245192],[108.961981,38.26493],[109.007561,38.359316],[109.051292,38.385122],[109.054372,38.433892],[109.128901,38.480288],[109.175712,38.518694],[109.196654,38.552867],[109.276726,38.623035],[109.331545,38.597783],[109.367269,38.627711],[109.329081,38.66043],[109.338936,38.701542],[109.404226,38.720689],[109.444262,38.782763],[109.511399,38.833595],[109.549587,38.805618],[109.624116,38.85457],[109.672159,38.928167],[109.685094,38.968195],[109.665384,38.981687],[109.72513,39.018429],[109.762086,39.057476],[109.793499,39.074204],[109.851397,39.122971],[109.890818,39.103932],[109.92223,39.107183],[109.893897,39.141075],[109.961035,39.191651],[109.871723,39.243581],[109.90252,39.271848],[109.962267,39.212056],[110.041107,39.21623],[110.109476,39.249606],[110.217881,39.281113],[110.184005,39.355192],[110.161831,39.387115],[110.136577,39.39174],[110.12549,39.432891],[110.152592,39.45415],[110.243751,39.423645],[110.257917,39.407001],[110.385417,39.310291],[110.429764,39.341308],[110.434692,39.381101],[110.482735,39.360745],[110.524003,39.382952],[110.559728,39.351027],[110.566503,39.320014],[110.596684,39.282966],[110.626249,39.266751],[110.702626,39.273701],[110.731575,39.30705],[110.73835,39.348713],[110.782698,39.38804],[110.869545,39.494341],[110.891103,39.509118],[110.958856,39.519275],[111.017371,39.552045],[111.101138,39.559428],[111.136863,39.587106],[111.154725,39.569116],[111.148566,39.531277],[111.10545,39.497573],[111.10545,39.472631],[111.058639,39.447681],[111.064182,39.400989],[111.098059,39.401914],[111.087588,39.376013],[111.125776,39.366297],[111.143022,39.407926],[111.171971,39.423183],[111.337043,39.420872],[111.358601,39.432428],[111.372152,39.479099],[111.431282,39.508656],[111.422043,39.539123],[111.441137,39.59679],[111.460847,39.606935],[111.445448,39.640124],[111.497187,39.661781],[111.525521,39.662242],[111.625303,39.633672],[111.659179,39.641507],[111.722621,39.606013],[111.783599,39.58895],[111.842729,39.620305],[111.87907,39.606013],[111.9382,39.623071],[111.925265,39.66731],[111.959758,39.692642],[111.970229,39.796638],[112.012729,39.827438],[112.042294,39.886243],[112.07617,39.919298],[112.133453,40.001866],[112.142076,40.027076],[112.182112,40.061437],[112.183344,40.083877],[112.232003,40.133311],[112.232619,40.169905],[112.299756,40.21105],[112.310227,40.256281],[112.349031,40.257194],[112.418017,40.295091],[112.456205,40.300112],[112.511639,40.269068],[112.6299,40.235725],[112.712436,40.178593],[112.744464,40.167161],[112.848558,40.206937],[112.898449,40.329317],[113.03334,40.368997],[113.083231,40.374925],[113.251382,40.413211],[113.27602,40.388601],[113.316672,40.319736],[113.387505,40.319279],[113.500222,40.334335],[113.559968,40.348476],[113.688699,40.448288],[113.763228,40.473787],[113.794641,40.517932],[113.850691,40.460583],[113.890112,40.466503],[113.948626,40.514747],[114.011452,40.515657],[114.061959,40.52885],[114.080437,40.547952],[114.076741,40.575686],[114.041633,40.608861],[114.07243,40.679246],[114.063806,40.706925],[114.084748,40.729605],[114.134639,40.737314],[114.103227,40.770861],[114.104458,40.797597],[114.080437,40.790348],[114.044712,40.830661],[114.073661,40.857372],[114.055183,40.867782],[114.041633,40.917546],[114.057647,40.925234],[113.994821,40.938798],[113.973263,40.983087],[113.868554,41.06887],[113.819279,41.09774],[113.877793,41.115777],[113.920293,41.172112],[113.960945,41.171211],[113.996669,41.19238],[114.016379,41.231999],[113.992357,41.269794],[113.971416,41.239649],[113.95109,41.282837],[113.914749,41.294529],[113.899351,41.316108],[113.92522,41.325546],[113.94493,41.392477],[113.871017,41.413126],[113.877793,41.431076],[113.919677,41.454404],[113.933227,41.487139],[113.953553,41.483553],[113.976959,41.505966],[114.032394,41.529715],[114.101379,41.537779],[114.230726,41.513584],[114.221487,41.582111],[114.226414,41.616572],[114.259059,41.623282],[114.215328,41.68499],[114.237501,41.698843],[114.206704,41.7386],[114.215328,41.75646],[114.200545,41.789934],[114.282465,41.863517],[114.343443,41.926774],[114.352066,41.953484],[114.419203,41.942356],[114.478334,41.951704],[114.511594,41.981962],[114.467863,42.025989],[114.480181,42.064654],[114.502355,42.06732],[114.510978,42.110844],[114.560254,42.132595],[114.647717,42.109512],[114.675434,42.12061],[114.75489,42.115727],[114.789383,42.130819],[114.79431,42.149457],[114.825723,42.139695],[114.86268,42.097967],[114.860832,42.054879],[114.9021,42.015763],[114.915035,41.960605],[114.923658,41.871093],[114.939056,41.846132],[114.922426,41.825175],[114.868839,41.813579],[114.89594,41.76762],[114.902716,41.695715],[114.895325,41.636255],[114.860832,41.60091],[115.016049,41.615229],[115.056085,41.602253],[115.0992,41.62373],[115.195287,41.602253],[115.20391,41.571367],[115.256881,41.580768],[115.26612,41.616124],[115.290142,41.622835],[115.310468,41.592854],[115.377605,41.603148],[115.345576,41.635807],[115.360975,41.661297],[115.319091,41.691693],[115.346808,41.712247],[115.42996,41.728775],[115.488474,41.760924],[115.519887,41.76762],[115.57409,41.80555],[115.654162,41.829189],[115.688038,41.867528],[115.726227,41.870202],[115.811226,41.912525],[115.834632,41.93835],[115.85311,41.927665],[115.916552,41.945027],[115.954124,41.874213],[115.994776,41.828743],[116.007095,41.797966],[116.007095,41.79752],[116.034196,41.782795],[116.09887,41.776547],[116.129051,41.805996],[116.106877,41.831419],[116.122892,41.861734],[116.194341,41.861734],[116.212819,41.885352],[116.223906,41.932562],[116.298434,41.96817],[116.310137,41.997086],[116.373579,42.009983],[116.414231,41.982407],[116.393289,41.942802],[116.453651,41.945917],[116.4826,41.975734],[116.510933,41.974399],[116.553433,41.928555],[116.597165,41.935679],[116.639049,41.929891],[116.66923,41.947698],[116.727744,41.951259],[116.748686,41.984186],[116.796113,41.977958],[116.879881,42.018431],[116.890352,42.092639],[116.850316,42.156556],[116.825062,42.155669],[116.789338,42.200462],[116.903287,42.190708],[116.918685,42.229716],[116.897743,42.297479],[116.886656,42.366496],[116.910678,42.394789],[116.910062,42.395231],[116.921765,42.403628],[116.936547,42.410256],[116.944555,42.415116],[116.97104,42.427486],[116.974736,42.426603],[116.99075,42.425719],[117.005533,42.43367],[117.009228,42.44957],[117.01662,42.456193],[117.080061,42.463699],[117.09546,42.484004],[117.135496,42.468996],[117.188467,42.468114],[117.275314,42.481797],[117.332596,42.46105],[117.390495,42.461933],[117.413284,42.471645],[117.410205,42.519743],[117.387415,42.517537],[117.434226,42.557224],[117.435458,42.585431],[117.475494,42.602613],[117.530313,42.590278],[117.537088,42.603054],[117.60053,42.603054],[117.667051,42.582347],[117.708935,42.588515],[117.779768,42.61847],[117.801326,42.612744],[117.797631,42.585431],[117.856761,42.539148],[117.874007,42.510038],[117.997811,42.416884],[118.024296,42.385064],[118.008898,42.346595],[118.060021,42.298364],[118.047702,42.280656],[117.974405,42.25054],[117.977485,42.229716],[118.033535,42.199132],[118.106216,42.172082],[118.089586,42.12283],[118.097593,42.105072],[118.155491,42.081091],[118.116687,42.037102],[118.194296,42.031324],[118.220165,42.058434],[118.212774,42.081091],[118.239259,42.092639],[118.27252,42.083312],[118.296541,42.057545],[118.286686,42.033991],[118.239875,42.024655],[118.291614,42.007759],[118.313788,41.98819],[118.306396,41.940131],[118.268824,41.930336],[118.340273,41.87243],[118.335346,41.845241],[118.29223,41.772976],[118.247266,41.773869],[118.236179,41.80778],[118.178281,41.814917],[118.140093,41.784134],[118.132702,41.733241],[118.155491,41.712694],[118.159187,41.67605],[118.206614,41.650566],[118.215237,41.59554],[118.302701,41.55256],[118.315636,41.512688],[118.271904,41.471446],[118.327338,41.450816],[118.348896,41.428384],[118.361215,41.384844],[118.348896,41.342622],[118.380309,41.312062],[118.412338,41.331838],[118.528135,41.355202],[118.629765,41.346666],[118.677192,41.35026],[118.741866,41.324198],[118.770199,41.352956],[118.843496,41.374516],[118.844727,41.342622],[118.890923,41.300823],[118.949437,41.317906],[118.980234,41.305769],[119.092951,41.293629],[119.168712,41.294978],[119.197661,41.282837],[119.211827,41.308016],[119.239545,41.31431],[119.296211,41.325097],[119.330704,41.385293],[119.309762,41.405944],[119.376283,41.422102],[119.378131,41.459787],[119.401537,41.472343],[119.406464,41.503276],[119.361501,41.545841],[119.362116,41.566442],[119.420015,41.567785],[119.415703,41.590169],[119.342406,41.617914],[119.307914,41.657273],[119.299907,41.705545],[119.319001,41.727435],[119.317769,41.764049],[119.292515,41.790827],[119.312841,41.80555],[119.334399,41.871539],[119.323312,41.889807],[119.340559,41.926774],[119.323928,41.937014],[119.324544,41.969505],[119.375667,42.023322],[119.384906,42.08953],[119.352261,42.118391],[119.314689,42.119723],[119.30853,42.147239],[119.286972,42.154781],[119.277733,42.185387],[119.237697,42.200905],[119.274037,42.239021],[119.280197,42.260728],[119.34795,42.300578],[119.432949,42.317396],[119.482841,42.347037],[119.502551,42.388159],[119.540123,42.363401],[119.572152,42.359421],[119.571536,42.335536],[119.539507,42.297922],[119.557985,42.289068],[119.609108,42.276671],[119.617115,42.252755],[119.679941,42.240793],[119.744615,42.211545],[119.841933,42.215534],[119.854868,42.170308],[119.837622,42.135257],[119.845629,42.097079],[119.87581,42.077982],[119.897368,42.030879],[119.921389,42.014429],[119.924469,41.98908],[119.950954,41.974399],[119.954034,41.923212],[119.989759,41.899163],[120.023019,41.816701],[120.041498,41.818932],[120.050737,41.776101],[120.024867,41.737707],[120.035954,41.708226],[120.096316,41.697056],[120.1382,41.729221],[120.127113,41.77253],[120.183164,41.826513],[120.188707,41.848361],[120.215808,41.853265],[120.251533,41.884016],[120.286641,41.880005],[120.290337,41.897381],[120.260156,41.904062],[120.271859,41.925439],[120.318054,41.93746],[120.309431,41.951704],[120.373489,41.994862],[120.399358,41.984631],[120.456641,42.016208],[120.450481,42.057101],[120.493597,42.073539],[120.466496,42.105516],[120.56751,42.152119],[120.58414,42.167203],[120.624792,42.154338],[120.72211,42.203565],[120.745516,42.223512],[120.79048,42.218636],[120.820661,42.227943],[120.8299,42.252755],[120.883487,42.242565],[120.883487,42.269585],[120.933994,42.27977],[120.992508,42.264714],[121.028848,42.242565],[121.070732,42.254083],[121.087978,42.278885],[121.120623,42.280656],[121.133558,42.300135],[121.184681,42.333324],[121.218558,42.371802],[121.285079,42.387717],[121.314644,42.42837],[121.304789,42.435879],[121.386093,42.474294],[121.434752,42.475176],[121.4791,42.49636],[121.506201,42.482239],[121.570875,42.487093],[121.607831,42.516214],[121.604136,42.495037],[121.66573,42.437204],[121.69899,42.438529],[121.747649,42.484887],[121.803084,42.514891],[121.817867,42.504303],[121.831417,42.533856],[121.844352,42.522389],[121.889931,42.556784],[121.921344,42.605697],[121.915801,42.656332],[121.94167,42.666014],[121.939207,42.688453],[122.018663,42.69901],[122.062394,42.723635],[122.072865,42.710444],[122.160945,42.684934],[122.204676,42.685374],[122.204676,42.732867],[122.261343,42.695931],[122.324785,42.684934],[122.338951,42.669975],[122.396234,42.684054],[122.396234,42.707366],[122.460907,42.755282],[122.439349,42.770221],[122.371596,42.776371],[122.35127,42.830378],[122.436886,42.843105],[122.556378,42.827745],[122.576088,42.819405],[122.580399,42.789987],[122.624747,42.773296],[122.653696,42.78252],[122.733152,42.786034],[122.73808,42.77066],[122.786123,42.757479],[122.848949,42.712203],[122.883442,42.751766],[122.887137,42.770221],[122.925941,42.772417],[122.945651,42.753524],[122.980144,42.777689],[123.058368,42.768903],[123.118114,42.801405],[123.227752,42.831695],[123.169853,42.859777],[123.188947,42.895739],[123.18402,42.925983],[123.259165,42.993431],[123.323222,43.000872],[123.434707,43.027565],[123.474743,43.042438],[123.536337,43.007],[123.572678,43.003498],[123.580685,43.036314],[123.631192,43.088346],[123.636119,43.141644],[123.666916,43.179623],[123.645974,43.208855],[123.676771,43.223684],[123.664453,43.264663],[123.698329,43.272071],[123.703873,43.37047],[123.608402,43.366119],[123.54496,43.415262],[123.519707,43.402219],[123.486446,43.44525],[123.442098,43.437863],[123.419925,43.410046],[123.382968,43.469143],[123.36449,43.483475],[123.315831,43.492159],[123.329998,43.519071],[123.304744,43.550742],[123.360179,43.567223],[123.452569,43.545971],[123.461193,43.568523],[123.434091,43.575461],[123.421157,43.598435],[123.5117,43.592801],[123.510468,43.624867],[123.536953,43.633964],[123.518475,43.682024],[123.520323,43.708419],[123.48275,43.737396],[123.498149,43.771114],[123.461809,43.822518],[123.467968,43.853599],[123.397135,43.954929],[123.37065,43.970006],[123.400831,43.979481],[123.365722,44.013922],[123.331229,44.028984],[123.32815,44.084035],[123.350939,44.092633],[123.362642,44.133452],[123.386664,44.161794],[123.323838,44.179823],[123.286882,44.211574],[123.277027,44.25274],[123.196955,44.34483],[123.128585,44.367081],[123.114419,44.40258],[123.142136,44.428228],[123.125506,44.455147],[123.137209,44.486322],[123.12489,44.5098],[123.06576,44.505959],[123.025108,44.493153],[122.85634,44.398304],[122.76087,44.369648],[122.702971,44.319145],[122.675254,44.285738],[122.641993,44.283595],[122.515726,44.251025],[122.483081,44.236877],[122.319241,44.233018],[122.271198,44.255741],[122.291524,44.310152],[122.294604,44.41113],[122.28598,44.477783],[122.228082,44.480345],[122.224386,44.526016],[122.196053,44.559712],[122.13138,44.577619],[122.113517,44.615546],[122.103046,44.67388],[122.117213,44.701961],[122.161561,44.728328],[122.152322,44.744057],[122.10243,44.736406],[122.110438,44.767856],[122.142467,44.753833],[122.168952,44.770405],[122.099967,44.7823],[122.098119,44.81882],[122.04946,44.912985],[122.079025,44.914256],[122.087032,44.95281],[122.074713,45.006573],[122.098735,45.02138],[122.119677,45.068739],[122.109822,45.142236],[122.143082,45.183167],[122.192358,45.180636],[122.22993,45.206784],[122.239169,45.276313],[122.147394,45.295682],[122.146778,45.374352],[122.180039,45.409655],[122.168336,45.439897],[122.064242,45.472641],[122.002648,45.507882],[121.993409,45.552741],[121.966308,45.596308],[121.995873,45.59882],[122.003264,45.623102],[121.970004,45.692956],[121.934279,45.71051],[121.867142,45.719703],[121.812323,45.704659],[121.811091,45.687103],[121.713773,45.701734],[121.666345,45.727641],[121.644172,45.752284],[121.657106,45.770238],[121.697142,45.76314],[121.754425,45.794862],[121.766744,45.830318],[121.769823,45.84366],[121.817251,45.875336],[121.805548,45.900746],[121.821562,45.918235],[121.809243,45.961102],[121.761816,45.998947],[121.819098,46.023054],[121.843736,46.024301],[121.864062,46.002272],[121.923808,46.004767],[121.92812,45.988552],[122.040221,45.959022],[122.085184,45.912406],[122.091344,45.882002],[122.200981,45.857],[122.236705,45.831569],[122.253952,45.7982],[122.301379,45.813218],[122.337719,45.859917],[122.372828,45.856166],[122.362357,45.917403],[122.446125,45.916986],[122.496016,45.85825],[122.504639,45.786933],[122.522501,45.786933],[122.556378,45.82156],[122.603189,45.778169],[122.640761,45.771072],[122.650001,45.731401],[122.671558,45.70048],[122.741775,45.705077],[122.751015,45.735996],[122.792283,45.766063],[122.752246,45.834905],[122.772572,45.856583],[122.80029,45.856583],[122.828623,45.912406],[122.792898,46.073313],[123.04605,46.099878],[123.070071,46.123527],[123.112571,46.130163],[123.102716,46.172037],[123.127354,46.174523],[123.128585,46.210565],[123.178476,46.248239],[123.142136,46.298293],[123.089781,46.347888],[123.011557,46.434984],[123.010325,46.524823],[123.002318,46.574624],[123.052825,46.579972],[123.04605,46.617803],[123.077462,46.622324],[123.098404,46.603002],[123.18094,46.614103],[123.228368,46.588198],[123.279491,46.616981],[123.276411,46.660947],[123.318295,46.662179],[123.366338,46.677784],[123.474743,46.686817],[123.603475,46.68928],[123.631808,46.728675],[123.629344,46.813524],[123.580069,46.827447],[123.625648,46.847508],[123.599163,46.868378],[123.605322,46.891286],[123.576989,46.891286],[123.575757,46.845461],[123.562823,46.82581],[123.506772,46.827038],[123.483366,46.84587],[123.52833,46.944836],[123.487678,46.959951],[123.42362,46.934212],[123.337389,46.988943],[123.301664,46.999965],[123.304128,46.964852],[123.360179,46.970978],[123.404526,46.935438],[123.40699,46.906416],[123.374345,46.837683],[123.341084,46.826628],[123.295505,46.865105],[123.221592,46.850373],[123.22344,46.821305],[123.198802,46.803283],[123.163694,46.74016],[123.103332,46.734828],[123.076846,46.745082],[123.026339,46.718829],[123.00355,46.730726],[122.996774,46.761483],[122.906847,46.80738],[122.893913,46.895376],[122.895144,46.960359],[122.83971,46.937072],[122.791051,46.941567],[122.798442,46.9575],[122.77442,46.973837],[122.778116,47.002822],[122.845869,47.046881],[122.852645,47.072158],[122.821232,47.065636],[122.710363,47.093349],[122.679566,47.094164],[122.615508,47.124306],[122.582863,47.158092],[122.531124,47.198771],[122.498479,47.255262],[122.462755,47.27841],[122.441197,47.310476],[122.418407,47.350632],[122.507103,47.401291],[122.543443,47.495589],[122.59395,47.54732],[122.765181,47.614333],[122.848949,47.67441],[122.926557,47.697777],[123.041122,47.746492],[123.161846,47.781892],[123.214201,47.824502],[123.256085,47.876711],[123.300432,47.953723],[123.537569,48.021816],[123.579453,48.045427],[123.705105,48.152142],[123.746373,48.197638],[123.862785,48.271782],[124.019234,48.39313],[124.07898,48.43603],[124.136878,48.463023],[124.25945,48.536385],[124.314269,48.503881],[124.302566,48.456673],[124.330283,48.435633],[124.309957,48.413393],[124.331515,48.380015],[124.317964,48.35099],[124.353689,48.315978],[124.365392,48.283731],[124.422058,48.245884],[124.412819,48.219175],[124.418978,48.181679],[124.475029,48.173698],[124.471333,48.133373],[124.430065,48.12099],[124.415899,48.08782],[124.46579,48.098213],[124.478108,48.123387],[124.505826,48.124985],[124.529847,48.146951],[124.512601,48.164518],[124.547094,48.200829],[124.579122,48.262221],[124.558796,48.268197],[124.579738,48.297269],[124.540934,48.335476],[124.547094,48.35775],[124.51876,48.378027],[124.52492,48.426897],[124.507674,48.445558],[124.555717,48.467784],[124.533543,48.515379],[124.548941,48.535593],[124.520608,48.556195],[124.579122,48.596582],[124.601912,48.632587],[124.624702,48.701755],[124.612383,48.747945],[124.656115,48.783842],[124.644412,48.80789],[124.654267,48.83429],[124.697383,48.841775],[124.715861,48.885475],[124.709086,48.920487],[124.744194,48.920487],[124.756513,48.967262],[124.808252,49.020666],[124.828578,49.077933],[124.809484,49.115943],[124.847672,49.129651],[124.860607,49.166448],[124.906802,49.184054],[124.983179,49.162535],[125.039845,49.17623],[125.034302,49.157056],[125.117453,49.126127],[125.158721,49.144921],[125.187671,49.186792],[125.219699,49.189139],[125.227707,49.248947],[125.214772,49.277066],[125.261583,49.322336],[125.256656,49.359769],[125.277598,49.379644],[125.25604,49.395227],[125.256656,49.437275],[125.270822,49.454395],[125.228323,49.487063],[125.211076,49.539908],[125.233866,49.536801],[125.23017,49.595411],[125.205533,49.593859],[125.16796,49.629923],[125.15441,49.616741],[125.127308,49.655113],[125.132236,49.672157],[125.164881,49.669446],[125.189518,49.652401],[125.185207,49.634574],[125.219699,49.669058],[125.225243,49.726349],[125.204301,49.734086],[125.221547,49.754969],[125.222779,49.799026],[125.177815,49.829533],[125.239409,49.844587],[125.225243,49.867351],[125.245569,49.87198],[125.212924,49.907452],[125.225859,49.922481],[125.199373,49.935194],[125.190134,49.959841],[125.231402,49.957531],[125.241873,49.987938],[125.278214,49.996402],[125.297924,50.014481],[125.283757,50.036012],[125.25296,50.041393],[125.289916,50.057917],[125.315786,50.04562],[125.328105,50.065985],[125.283757,50.070211],[125.287453,50.093636],[125.258504,50.103618],[125.27883,50.127411],[125.311474,50.140453],[125.376148,50.137385],[125.335496,50.161161],[125.382923,50.172278],[125.39093,50.199868],[125.417416,50.195654],[125.448829,50.216338],[125.442053,50.260357],[125.466075,50.266861],[125.463611,50.295925],[125.530749,50.331085],[125.520278,50.3498],[125.546763,50.358965],[125.522126,50.404759],[125.536292,50.420014],[125.567089,50.402852],[125.583104,50.409717],[125.562162,50.438314],[125.580024,50.449366],[125.627451,50.443268],[125.654553,50.471082],[125.699516,50.487078],[125.740784,50.523237],[125.754335,50.506874],[125.770349,50.531227],[125.794987,50.532748],[125.829479,50.56165],[125.807921,50.60383],[125.814697,50.62092],[125.793139,50.643316],[125.804226,50.658874],[125.789443,50.679735],[125.825784,50.70362],[125.78082,50.725598],[125.795603,50.738856],[125.758646,50.746809],[125.804226,50.773309],[125.828863,50.756654],[125.846726,50.769524],[125.836255,50.793363],[125.890457,50.805845],[125.878138,50.816812],[125.913247,50.825885],[125.939732,50.85423],[125.961906,50.901054],[125.997631,50.872738],[125.996399,50.906715],[126.02042,50.927466],[126.042594,50.92558],[126.068464,50.967434],[126.041978,50.981753],[126.033971,51.011132],[126.059225,51.043503],[125.976073,51.084498],[125.993935,51.119072],[125.970529,51.123955],[125.946508,51.108176],[125.909551,51.138977],[125.864588,51.146487],[125.850421,51.21364],[125.819008,51.227134],[125.761726,51.226385],[125.76111,51.261976],[125.740784,51.27583],[125.700132,51.327465],[125.626219,51.380163],[125.623756,51.387633],[125.62314,51.398089],[125.600966,51.410409],[125.60035,51.413396],[125.595422,51.416755],[125.559082,51.461521],[125.528285,51.488359],[125.424807,51.562827],[125.38046,51.585516],[125.35151,51.623801],[125.316402,51.610052],[125.289301,51.633831],[125.228938,51.640517],[125.214772,51.627888],[125.175968,51.639403],[125.130388,51.635317],[125.12854,51.659083],[125.098975,51.658341],[125.060171,51.59667],[125.073106,51.553526],[125.047236,51.529704],[125.004737,51.529332],[124.983795,51.508478],[124.928976,51.498419],[124.917889,51.474196],[124.942527,51.447349],[124.885244,51.40817],[124.864302,51.37979],[124.783614,51.392115],[124.76452,51.38726],[124.752817,51.35812],[124.693687,51.3327],[124.62655,51.327465],[124.58713,51.363725],[124.555717,51.375307],[124.490427,51.380537],[124.478108,51.36223],[124.443616,51.35812],[124.426985,51.331953],[124.430065,51.301281],[124.406659,51.272086],[124.339522,51.293422],[124.297638,51.298661],[124.271769,51.308389],[124.239124,51.344664],[124.192313,51.33943],[124.128255,51.347281],[124.090067,51.3413],[124.071588,51.320734],[123.994596,51.322604],[123.939777,51.313253],[123.926227,51.300532],[123.887423,51.320734],[123.842459,51.367462],[123.794416,51.361109],[123.711264,51.398089],[123.660141,51.342795],[123.661989,51.319237],[123.582533,51.306893],[123.582533,51.294545],[123.46304,51.286686],[123.440251,51.270963],[123.414381,51.278825],[123.376809,51.266844],[123.339853,51.27246],[123.294273,51.254111],[123.231447,51.268716],[123.231447,51.279199],[123.127969,51.297913],[123.069455,51.321108],[123.002934,51.31213],[122.965977,51.345786],[122.965977,51.386886],[122.946267,51.405183],[122.903768,51.415262],[122.900072,51.445112],[122.871123,51.455181],[122.854492,51.477551],[122.880362,51.511085],[122.858804,51.524864],[122.880362,51.537894],[122.874202,51.561339],[122.832935,51.581797],[122.85634,51.606707],[122.820616,51.633088],[122.816304,51.655371],[122.778732,51.698048],[122.749167,51.746613],[122.771957,51.779579],[122.732536,51.832495],[122.725761,51.87833],[122.706051,51.890151],[122.729457,51.919321],[122.726377,51.978709],[122.683877,51.974654],[122.664783,51.99861],[122.650616,52.058997],[122.625363,52.067459],[122.643841,52.111585],[122.629059,52.13657],[122.690653,52.140243],[122.73808,52.153464],[122.769493,52.179893],[122.766413,52.232705],[122.787355,52.252494],[122.76087,52.26678],[122.710979,52.256157],[122.67895,52.276667],[122.585943,52.266413],[122.560689,52.282526],[122.478153,52.29607],[122.484313,52.341432],[122.447356,52.394052],[122.419023,52.375057],[122.378987,52.395512],[122.367284,52.413768],[122.342031,52.414133],[122.326016,52.459374],[122.310618,52.475416],[122.207756,52.469218],[122.178191,52.48963],[122.168952,52.513674],[122.140003,52.510032],[122.142467,52.495096],[122.107358,52.452445],[122.080873,52.440407],[122.091344,52.427272],[122.040837,52.413038],[122.035909,52.377615],[121.976779,52.343626],[121.94783,52.298266],[121.901018,52.280695],[121.841272,52.282526],[121.769207,52.308147],[121.714389,52.318025],[121.715621,52.342894],[121.658338,52.3904],[121.678664,52.419973],[121.63986,52.44442],[121.590585,52.443326],[121.565331,52.460468],[121.519136,52.456821],[121.495114,52.484892],[121.474172,52.482706],[121.416274,52.499468],[121.411963,52.52205],[121.353448,52.534793],[121.323883,52.573727],[121.280151,52.586819],[121.225333,52.577364],[121.182217,52.59918],[121.237036,52.619167],[121.29247,52.651855],[121.309717,52.676173],[121.373158,52.683067],[121.455078,52.73528],[121.476636,52.772225],[121.511129,52.779104],[121.537614,52.801542],[121.591201,52.824693],[121.620766,52.853251],[121.604136,52.872401],[121.610295,52.892264],[121.66265,52.912478],[121.677432,52.948192],[121.715621,52.997926],[121.785838,53.018451],[121.817867,53.061631],[121.775367,53.089674],[121.784606,53.104408],[121.753193,53.147501],[121.722396,53.145706],[121.665114,53.170467],[121.660186,53.195213],[121.67928,53.199515],[121.679896,53.240722],[121.642324,53.262564],[121.615222,53.258984],[121.575802,53.29155],[121.504969,53.323018],[121.499426,53.337314],[121.416274,53.319443],[121.336818,53.325877],[121.308485,53.301565],[121.227797,53.280459],[121.155732,53.285468],[121.129246,53.277238],[121.098449,53.306929],[121.055334,53.29155],[120.950624,53.29763],[120.936457,53.28833],[120.882871,53.294411],[120.867472,53.278669],[120.820661,53.269007],[120.838523,53.239648],[120.821893,53.241797],[120.736277,53.204892],[120.690698,53.174771],[120.687002,53.142476],[120.659901,53.137091],[120.643886,53.106923],[120.562582,53.082845],[120.529321,53.045803],[120.452945,53.01017],[120.411061,52.957927],[120.363018,52.94134],[120.350699,52.906343],[120.295265,52.891542],[120.297112,52.869872],[120.222584,52.84277],[120.181316,52.806969],[120.14128,52.813119],[120.101244,52.788877],[120.031642,52.773674],[120.071063,52.70628],[120.035338,52.646409],[120.049505,52.598453],[120.07599,52.586092],[120.125265,52.586819],[120.194866,52.578819],[120.289721,52.623527],[120.396895,52.616261],[120.462184,52.64532],[120.483742,52.630066],[120.56135,52.595544],[120.605082,52.589364],[120.62664,52.570818],[120.658669,52.56718],[120.690698,52.547532],[120.734429,52.536977],[120.687002,52.511489],[120.706712,52.492909],[120.68269,52.464479],[120.688234,52.427637],[120.64943,52.3904],[120.653741,52.371038],[120.62356,52.361172],[120.627256,52.323878],[120.653741,52.302658],[120.695625,52.290214],[120.715951,52.261286],[120.755371,52.258355],[120.745516,52.20594],[120.786784,52.15787],[120.760299,52.136937],[120.76769,52.10938],[120.753523,52.085483],[120.717183,52.072978],[120.690698,52.047221],[120.691929,52.026973],[120.717799,52.015556],[120.704864,51.983501],[120.66298,51.958061],[120.656821,51.926333],[120.548416,51.907877],[120.549032,51.882394],[120.481278,51.885719],[120.480046,51.855049],[120.40059,51.833605],[120.40675,51.81659],[120.363634,51.789945],[120.317438,51.785873],[120.294649,51.752171],[120.226279,51.717703],[120.172693,51.679868],[120.087077,51.678013],[120.100628,51.649058],[120.05936,51.634203],[120.035954,51.583657],[120.052584,51.560967],[120.017476,51.52114],[119.985447,51.505125],[119.982367,51.482396],[120.002693,51.459283],[119.982983,51.445112],[119.97128,51.40033],[119.910918,51.390994],[119.914614,51.374187],[119.946643,51.360736],[119.883817,51.336813],[119.885049,51.302777],[119.811136,51.281071],[119.828383,51.263099],[119.797586,51.243622],[119.821607,51.21439],[119.784035,51.22601],[119.760629,51.212516],[119.788346,51.174636],[119.771716,51.124331],[119.752622,51.117193],[119.764325,51.092017],[119.719361,51.075099],[119.726753,51.051028],[119.678093,51.016404],[119.630666,51.00925],[119.598637,50.984767],[119.569688,50.933879],[119.491464,50.87878],[119.498855,50.827776],[119.515485,50.814165],[119.496391,50.771795],[119.506862,50.763846],[119.450196,50.695281],[119.430486,50.684286],[119.385522,50.682769],[119.394145,50.667219],[119.361501,50.632689],[119.298059,50.616743],[119.281428,50.601551],[119.295595,50.573814],[119.264182,50.536933],[119.262334,50.490124],[119.250631,50.448604],[119.22353,50.441363],[119.217371,50.414675],[119.165016,50.422683],[119.125596,50.389118],[119.176719,50.378814],[119.155777,50.364691],[119.188422,50.347509],[119.232153,50.365455],[119.259871,50.345218],[119.277117,50.366218],[119.322696,50.352474],[119.358421,50.358965],[119.381827,50.324208],[119.35103,50.303953],[119.339943,50.244668],[119.319001,50.220933],[119.358421,50.197953],[119.339327,50.192206],[119.350414,50.166145],[119.309762,50.161161],[119.290052,50.121655],[119.236465,50.075204],[119.190269,50.087877],[119.193965,50.069826],[119.163168,50.027554],[119.12498,50.019095],[119.090487,49.985629],[118.982082,49.979087],[118.964836,49.988708],[118.791757,49.955606],[118.761576,49.959456],[118.739402,49.946364],[118.672264,49.955991],[118.605127,49.926719],[118.574946,49.931342],[118.531214,49.887791],[118.485019,49.866194],[118.483787,49.830691],[118.443751,49.835709],[118.385853,49.827217],[118.398787,49.802502],[118.384005,49.783958],[118.315636,49.766953],[118.284223,49.743755],[118.220781,49.729831],[118.211542,49.690744],[118.156723,49.660149],[118.129622,49.669446],[118.082811,49.616741],[118.011362,49.614803],[117.995963,49.623332],[117.950999,49.596187],[117.866,49.591532],[117.849369,49.551557],[117.809333,49.521263],[117.638102,49.574847],[117.485349,49.633024],[117.278394,49.636512],[117.068974,49.695389],[116.736367,49.847674],[116.717889,49.847288],[116.428397,49.430659],[116.048363,48.873274],[116.077928,48.822471],[116.069305,48.811437],[115.83032,48.560156],[115.799523,48.514982],[115.822929,48.259432],[115.81061,48.257042],[115.529126,48.155336],[115.545141,48.134971],[115.539597,48.104607],[115.580249,47.921649],[115.939342,47.683275],[115.968291,47.689721],[116.111189,47.811642],[116.130283,47.823296],[116.26579,47.876711],[116.453035,47.837358],[116.669846,47.890758],[116.791186,47.89758],[116.879265,47.893968],[117.094844,47.8241],[117.384335,47.641356],[117.493357,47.758563],[117.519226,47.761782],[117.529081,47.782697],[117.813645,48.016212],[117.886942,48.025418],[117.96147,48.011007],[118.052014,48.01421],[118.107448,48.031021],[118.124694,48.047427],[118.150564,48.036224],[118.238643,48.041826],[118.238027,48.031422],[118.284839,48.011007],[118.351976,48.006203],[118.37415,48.016612],[118.422193,48.01461],[118.441903,47.995791],[118.568171,47.992187],[118.773278,47.771034],[119.134219,47.664335],[119.152081,47.540453],[119.205052,47.520249],[119.365812,47.47739],[119.32208,47.42721],[119.365812,47.423161],[119.386138,47.397645],[119.437877,47.378602],[119.450812,47.353065],[119.559217,47.303172],[119.56784,47.248357],[119.627586,47.247544],[119.716282,47.195518],[119.763093,47.13082],[119.806825,47.055037],[119.79081,47.04525],[119.795122,47.013024],[119.845013,46.964852],[119.859795,46.917046],[119.926933,46.903963],[119.920157,46.853238],[119.936172,46.790173],[119.917078,46.758203],[119.93494,46.712674],[119.911534,46.669572],[119.859179,46.669572],[119.804361,46.68189],[119.8136,46.66834],[119.783419,46.626023],[119.739687,46.615336],[119.677477,46.584908],[119.682405,46.605058],[119.656535,46.625612],[119.598637,46.618214],[119.557985,46.633832],[119.491464,46.629311],[119.431718,46.638763],[119.374435,46.603414],[119.357805,46.619447],[119.325776,46.608759],[119.26295,46.649034],[119.20074,46.648213],[119.152081,46.658072],[119.123132,46.642872],[119.073857,46.676552],[119.011647,46.745902],[118.951285,46.722111],[118.912481,46.733188],[118.914329,46.77501],[118.845343,46.771731],[118.788061,46.717598],[118.788061,46.687227],[118.677192,46.6979],[118.639004,46.721291],[118.586033,46.692975],[118.446831,46.704467],[118.41049,46.728265],[118.316252,46.73934],[118.274984,46.715957],[118.238643,46.709392],[118.192448,46.682711],[118.124078,46.678195],[118.04647,46.631366],[117.992883,46.631366],[117.982412,46.614925],[117.914659,46.607936],[117.868464,46.575447],[117.870927,46.549935],[117.813645,46.530588],[117.769913,46.537586],[117.748355,46.521941],[117.704008,46.516587],[117.641182,46.558166],[117.622704,46.596012],[117.596218,46.603414],[117.49582,46.600535],[117.42006,46.582029],[117.447777,46.528117],[117.392343,46.463023],[117.375712,46.416421],[117.383719,46.394962],[117.372017,46.36028],[117.247597,46.366888],[117.097308,46.356976],[116.876801,46.375559],[116.834302,46.384229],[116.81336,46.355737],[116.745606,46.327642],[116.673541,46.325163],[116.585462,46.292504],[116.573143,46.258998],[116.536187,46.23251],[116.439484,46.137628],[116.414231,46.133896],[116.271949,45.966926],[116.243,45.876169],[116.288579,45.839074],[116.278108,45.831152],[116.286731,45.775247],[116.260862,45.776082],[116.22329,45.747273],[116.217746,45.72221],[116.17463,45.688775],[116.1155,45.679577],[116.035428,45.685013],[116.026805,45.661177],[115.936878,45.632727],[115.864197,45.572853],[115.699741,45.45963],[115.586408,45.440317],[115.36467,45.392427],[115.178041,45.396209],[114.983404,45.379397],[114.920578,45.386122],[114.745035,45.438217],[114.600906,45.403773],[114.551014,45.387383],[114.539928,45.325985],[114.519602,45.283893],[114.459855,45.21353],[114.409348,45.179371],[114.347139,45.119436],[114.313262,45.107189],[114.19069,45.036607],[114.158045,44.994301],[114.116777,44.957045],[114.065038,44.931206],[113.907358,44.915104],[113.861778,44.863377],[113.798953,44.849377],[113.712105,44.788247],[113.631417,44.745333],[113.540874,44.759358],[113.503918,44.777628],[113.11526,44.799714],[113.037652,44.822641],[112.937869,44.840042],[112.850406,44.840466],[112.712436,44.879494],[112.599719,44.930783],[112.540589,45.001072],[112.438959,45.071697],[112.396459,45.064512],[112.113743,45.072965],[112.071243,45.096206],[112.002874,45.090713],[111.903707,45.052252],[111.764505,44.969325],[111.69244,44.859983],[111.624687,44.778477],[111.585267,44.705789],[111.560629,44.647062],[111.569868,44.57634],[111.530448,44.55033],[111.514434,44.507666],[111.478709,44.488884],[111.427586,44.394455],[111.415883,44.35724],[111.428818,44.319573],[111.507042,44.294305],[111.534144,44.26217],[111.541535,44.206855],[111.559397,44.171238],[111.662875,44.061247],[111.702295,44.034147],[111.773128,44.010479],[111.870447,43.940279],[111.959758,43.823382],[111.970845,43.748205],[111.951135,43.693275],[111.891388,43.6738],[111.79407,43.672068],[111.606209,43.513863],[111.564325,43.490422],[111.456535,43.494329],[111.400485,43.472618],[111.354289,43.436125],[111.183674,43.396132],[111.151029,43.38004],[111.069725,43.357852],[111.02045,43.329998],[110.82027,43.149067],[110.769763,43.099272],[110.736502,43.089657],[110.687227,43.036314],[110.689691,43.02144],[110.631177,42.936061],[110.469801,42.839156],[110.437156,42.781203],[110.34846,42.742098],[110.139657,42.674815],[110.108244,42.642687],[109.906216,42.635643],[109.733753,42.579262],[109.683862,42.558988],[109.544044,42.472528],[109.486761,42.458842],[109.291509,42.435879],[109.026039,42.458401],[108.983539,42.449128],[108.845569,42.395673],[108.798757,42.415116],[108.705134,42.413349],[108.532671,42.442945],[108.298614,42.438529],[108.238252,42.460167],[108.089195,42.436321],[108.022058,42.433229],[107.986949,42.413349],[107.939522,42.403628],[107.736262,42.415116],[107.57427,42.412907],[107.501589,42.456635],[107.46648,42.458842],[107.303872,42.412465],[107.271844,42.364285],[107.051337,42.319166],[106.785867,42.291281],[106.612789,42.241679],[106.372572,42.161436],[106.344855,42.149457],[106.01348,42.032213],[105.74185,41.949033],[105.589713,41.888471],[105.385221,41.797073],[105.291599,41.749763],[105.230621,41.751103],[105.009498,41.583007],[104.923267,41.654143],[104.803775,41.652355],[104.68921,41.6452],[104.524138,41.661745],[104.530298,41.875104],[104.418813,41.860397],[104.30856,41.840782],[104.080046,41.805104],[103.868779,41.802427],[103.454868,41.877332],[103.418527,41.882233],[103.20726,41.96283],[103.021862,42.028212],[102.712045,42.153007],[102.621502,42.154338],[102.540814,42.162323],[102.449039,42.144133],[102.093642,42.223512],[102.070236,42.232374],[101.877447,42.432345],[101.803534,42.503861],[101.770274,42.509597],[101.557775,42.529887],[101.291689,42.586312],[100.862995,42.671295],[100.826655,42.675255],[100.32528,42.690213],[100.272309,42.636523],[100.004376,42.648849],[99.969267,42.647969],[99.51224,42.568244],[98.962822,42.607018],[98.546447,42.638284],[98.195362,42.653251],[97.831958,42.706047],[97.28254,42.782081],[97.172903,42.795257]]]}},
    {"type":"Feature","properties":{"adcode":210000,"name":"辽宁省","center":[123.429096,41.796767],"centroid":[122.604994,41.299712],"childrenNum":14,"level":"province","parent":{"adcode":100000},"subFeatureIndex":5,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[123.534489,39.788361],[123.546808,39.756163],[123.579453,39.781002],[123.612714,39.775023],[123.642279,39.796178],[123.645358,39.823761],[123.674924,39.826979],[123.687858,39.808132],[123.795032,39.822842],[123.812278,39.831115],[123.95148,39.817786],[124.002603,39.800316],[124.103001,39.823302],[124.099306,39.777323],[124.151045,39.74558],[124.173218,39.841225],[124.214486,39.865116],[124.215102,39.883487],[124.21695,39.894049],[124.218182,39.895885],[124.219414,39.899099],[124.241588,39.928477],[124.286551,39.931689],[124.288399,39.962888],[124.349377,39.989029],[124.372167,40.021576],[124.336442,40.049985],[124.346913,40.079756],[124.428217,40.144291],[124.457782,40.177679],[124.490427,40.18408],[124.513833,40.218362],[124.515065,40.22019],[124.62655,40.291896],[124.722636,40.321561],[124.739267,40.371733],[124.834121,40.423235],[124.913578,40.481981],[124.945606,40.45603],[124.985642,40.475153],[125.044157,40.466503],[125.042925,40.483802],[125.004737,40.496091],[125.015823,40.533853],[125.076801,40.562048],[125.113758,40.569322],[125.181511,40.611132],[125.262815,40.620218],[125.279445,40.655187],[125.305315,40.661089],[125.329337,40.643835],[125.375532,40.658365],[125.422343,40.635661],[125.418648,40.673345],[125.453756,40.676522],[125.459916,40.707379],[125.49564,40.728697],[125.544915,40.729605],[125.551075,40.761796],[125.585567,40.788535],[125.61698,40.763609],[125.685349,40.769048],[125.67611,40.788082],[125.641002,40.798503],[125.648393,40.826133],[125.707523,40.866877],[125.687813,40.897645],[125.652089,40.91619],[125.584335,40.891764],[125.589263,40.931112],[125.635458,40.94151],[125.650241,40.970888],[125.674879,40.974503],[125.684118,41.021929],[125.726617,41.055332],[125.739552,41.08917],[125.712451,41.095485],[125.734009,41.125695],[125.759878,41.132908],[125.791291,41.167607],[125.73832,41.178418],[125.758646,41.232449],[125.749407,41.245499],[125.695205,41.244599],[125.685349,41.273842],[125.646545,41.264396],[125.642234,41.296327],[125.62006,41.318355],[125.637306,41.34442],[125.610205,41.365084],[125.589879,41.359245],[125.581256,41.396517],[125.547995,41.401006],[125.534444,41.428833],[125.533212,41.479069],[125.493176,41.509103],[125.507343,41.534195],[125.479626,41.544946],[125.450061,41.597777],[125.461148,41.642516],[125.446981,41.67605],[125.412488,41.691246],[125.344119,41.672474],[125.317018,41.676944],[125.332416,41.711354],[125.336112,41.768067],[125.336112,41.768067],[125.323177,41.771191],[125.323177,41.771191],[125.319482,41.776993],[125.319482,41.776993],[125.294844,41.822945],[125.307779,41.924548],[125.35151,41.92811],[125.291764,41.958825],[125.29854,41.974399],[125.369989,42.002868],[125.363213,42.017097],[125.416184,42.063766],[125.414336,42.101964],[125.446365,42.098411],[125.490097,42.136145],[125.458068,42.160105],[125.458068,42.160105],[125.41372,42.156112],[125.368141,42.182726],[125.357054,42.145464],[125.305931,42.146351],[125.312706,42.197359],[125.280677,42.175187],[125.312706,42.219966],[125.27575,42.231045],[125.27575,42.266928],[125.299156,42.289953],[125.264047,42.312528],[125.224011,42.30102],[125.175352,42.308102],[125.167345,42.351903],[125.203685,42.366938],[125.185823,42.38197],[125.186439,42.427928],[125.140243,42.44692],[125.150098,42.458842],[125.105135,42.490624],[125.068794,42.499449],[125.090968,42.515773],[125.066946,42.534738],[125.089736,42.567803],[125.082961,42.591159],[125.097127,42.622433],[125.038613,42.615387],[125.010896,42.63212],[125.014592,42.666014],[124.99057,42.677455],[124.968396,42.722756],[124.996729,42.745174],[124.975171,42.802722],[124.92836,42.819844],[124.897563,42.787791],[124.874157,42.789987],[124.856911,42.824234],[124.84952,42.882585],[124.87231,42.962344],[124.869846,42.988178],[124.840897,43.032377],[124.88894,43.074796],[124.882781,43.13422],[124.785462,43.117185],[124.755281,43.074359],[124.719557,43.069987],[124.686912,43.051185],[124.677673,43.002185],[124.658579,42.972854],[124.635173,42.972854],[124.632093,42.949642],[124.607456,42.937376],[124.586514,42.905384],[124.466406,42.847054],[124.435609,42.880831],[124.371551,42.880831],[124.38079,42.912835],[124.431913,42.930803],[124.442384,42.958841],[124.42329,42.975482],[124.369703,42.972854],[124.333363,42.997371],[124.425754,43.076107],[124.366007,43.121554],[124.273617,43.17875],[124.287167,43.207983],[124.27608,43.233278],[124.228653,43.235022],[124.215102,43.255947],[124.168291,43.244177],[124.114088,43.247229],[124.117168,43.2773],[124.099306,43.292983],[124.032784,43.280786],[123.964415,43.34088],[123.896046,43.361333],[123.881263,43.392218],[123.881263,43.392218],[123.852314,43.406133],[123.857858,43.459153],[123.857858,43.459153],[123.79688,43.489988],[123.747604,43.472184],[123.749452,43.439167],[123.710032,43.417001],[123.703873,43.37047],[123.698329,43.272071],[123.664453,43.264663],[123.676771,43.223684],[123.645974,43.208855],[123.666916,43.179623],[123.636119,43.141644],[123.631192,43.088346],[123.580685,43.036314],[123.572678,43.003498],[123.536337,43.007],[123.474743,43.042438],[123.434707,43.027565],[123.323222,43.000872],[123.259165,42.993431],[123.18402,42.925983],[123.188947,42.895739],[123.169853,42.859777],[123.227752,42.831695],[123.118114,42.801405],[123.058368,42.768903],[122.980144,42.777689],[122.945651,42.753524],[122.925941,42.772417],[122.887137,42.770221],[122.883442,42.751766],[122.883442,42.751766],[122.848949,42.712203],[122.848949,42.712203],[122.786123,42.757479],[122.73808,42.77066],[122.733152,42.786034],[122.653696,42.78252],[122.624747,42.773296],[122.580399,42.789987],[122.576088,42.819405],[122.556378,42.827745],[122.436886,42.843105],[122.35127,42.830378],[122.371596,42.776371],[122.439349,42.770221],[122.460907,42.755282],[122.396234,42.707366],[122.396234,42.684054],[122.338951,42.669975],[122.324785,42.684934],[122.261343,42.695931],[122.204676,42.732867],[122.204676,42.685374],[122.160945,42.684934],[122.072865,42.710444],[122.062394,42.723635],[122.018663,42.69901],[121.939207,42.688453],[121.94167,42.666014],[121.915801,42.656332],[121.921344,42.605697],[121.889931,42.556784],[121.844352,42.522389],[121.831417,42.533856],[121.817867,42.504303],[121.803084,42.514891],[121.747649,42.484887],[121.69899,42.438529],[121.66573,42.437204],[121.604136,42.495037],[121.607831,42.516214],[121.570875,42.487093],[121.506201,42.482239],[121.4791,42.49636],[121.434752,42.475176],[121.386093,42.474294],[121.304789,42.435879],[121.314644,42.42837],[121.285079,42.387717],[121.218558,42.371802],[121.184681,42.333324],[121.133558,42.300135],[121.120623,42.280656],[121.087978,42.278885],[121.070732,42.254083],[121.028848,42.242565],[120.992508,42.264714],[120.933994,42.27977],[120.883487,42.269585],[120.883487,42.269585],[120.883487,42.242565],[120.8299,42.252755],[120.820661,42.227943],[120.79048,42.218636],[120.745516,42.223512],[120.72211,42.203565],[120.624792,42.154338],[120.58414,42.167203],[120.56751,42.152119],[120.466496,42.105516],[120.493597,42.073539],[120.450481,42.057101],[120.456641,42.016208],[120.399358,41.984631],[120.373489,41.994862],[120.309431,41.951704],[120.318054,41.93746],[120.271859,41.925439],[120.260156,41.904062],[120.290337,41.897381],[120.286641,41.880005],[120.251533,41.884016],[120.215808,41.853265],[120.188707,41.848361],[120.183164,41.826513],[120.127113,41.77253],[120.1382,41.729221],[120.096316,41.697056],[120.035954,41.708226],[120.024867,41.737707],[120.050737,41.776101],[120.041498,41.818932],[120.023019,41.816701],[119.989759,41.899163],[119.954034,41.923212],[119.950954,41.974399],[119.924469,41.98908],[119.921389,42.014429],[119.897368,42.030879],[119.87581,42.077982],[119.845629,42.097079],[119.837622,42.135257],[119.854868,42.170308],[119.841933,42.215534],[119.744615,42.211545],[119.679941,42.240793],[119.617115,42.252755],[119.609108,42.276671],[119.557985,42.289068],[119.557985,42.289068],[119.539507,42.297922],[119.571536,42.335536],[119.572152,42.359421],[119.540123,42.363401],[119.502551,42.388159],[119.482841,42.347037],[119.432949,42.317396],[119.34795,42.300578],[119.280197,42.260728],[119.274037,42.239021],[119.237697,42.200905],[119.277733,42.185387],[119.286972,42.154781],[119.30853,42.147239],[119.314689,42.119723],[119.352261,42.118391],[119.384906,42.08953],[119.375667,42.023322],[119.324544,41.969505],[119.323928,41.937014],[119.340559,41.926774],[119.323312,41.889807],[119.334399,41.871539],[119.312841,41.80555],[119.292515,41.790827],[119.317769,41.764049],[119.319001,41.727435],[119.299907,41.705545],[119.307914,41.657273],[119.342406,41.617914],[119.415703,41.590169],[119.420015,41.567785],[119.362116,41.566442],[119.361501,41.545841],[119.406464,41.503276],[119.401537,41.472343],[119.378131,41.459787],[119.376283,41.422102],[119.309762,41.405944],[119.330704,41.385293],[119.296211,41.325097],[119.239545,41.31431],[119.2494,41.279689],[119.209364,41.244599],[119.204436,41.222546],[119.169943,41.222996],[119.189038,41.198234],[119.126212,41.138767],[119.081248,41.131555],[119.080632,41.095936],[119.037516,41.067516],[118.964836,41.079246],[118.937118,41.052625],[118.951901,41.018317],[119.013495,41.007479],[119.00056,40.967273],[118.977154,40.959138],[118.977154,40.959138],[118.916792,40.969984],[118.90201,40.960946],[118.873061,40.847866],[118.845959,40.822057],[118.878604,40.783098],[118.907553,40.775394],[118.895234,40.75409],[118.950053,40.747743],[118.96114,40.72008],[119.011031,40.687414],[119.028277,40.692406],[119.054763,40.664721],[119.115125,40.666536],[119.165632,40.69286],[119.184726,40.680153],[119.14469,40.632482],[119.162552,40.600228],[119.177951,40.609315],[119.230921,40.603863],[119.22045,40.569322],[119.256175,40.543404],[119.30237,40.530215],[119.429254,40.540221],[119.477913,40.533399],[119.503783,40.553864],[119.559217,40.547952],[119.572152,40.523846],[119.553674,40.502007],[119.604797,40.455119],[119.586934,40.375381],[119.598021,40.334335],[119.651608,40.271808],[119.639289,40.231613],[119.639289,40.231613],[119.671934,40.23938],[119.716898,40.195966],[119.745847,40.207851],[119.760629,40.136056],[119.736608,40.104936],[119.772332,40.08113],[119.783419,40.046778],[119.783419,40.046778],[119.787115,40.041739],[119.787115,40.041739],[119.81668,40.050443],[119.81668,40.050443],[119.854252,40.033033],[119.845629,40.000949],[119.845629,40.000949],[119.854252,39.98857],[119.91831,39.989946],[119.941715,40.009659],[119.947259,40.040364],[120.092005,40.077466],[120.134504,40.074719],[120.161606,40.096239],[120.273091,40.127362],[120.371641,40.174478],[120.451097,40.177679],[120.491749,40.20008],[120.523778,40.256737],[120.52193,40.304676],[120.537329,40.325211],[120.602618,40.36079],[120.596459,40.399084],[120.617401,40.41959],[120.616169,40.444645],[120.619249,40.460128],[120.666676,40.467413],[120.693777,40.505647],[120.72211,40.515657],[120.72827,40.539311],[120.822509,40.59432],[120.837291,40.644289],[120.8299,40.671076],[120.861313,40.684692],[120.939537,40.686507],[120.983269,40.712822],[121.032544,40.709193],[121.028848,40.746382],[120.991276,40.744115],[120.980189,40.766329],[120.994356,40.790801],[120.971566,40.805751],[121.00729,40.807563],[121.010986,40.784457],[121.086747,40.79805],[121.076892,40.815716],[121.096602,40.839717],[121.126167,40.86914],[121.177906,40.873665],[121.23642,40.851035],[121.290622,40.851488],[121.439064,40.830208],[121.440296,40.88181],[121.499426,40.880001],[121.526527,40.85194],[121.55486,40.849677],[121.553013,40.817528],[121.576418,40.837906],[121.626309,40.844244],[121.682976,40.829755],[121.732251,40.846961],[121.735331,40.862351],[121.778446,40.886787],[121.816019,40.894931],[121.84312,40.831567],[121.883772,40.802127],[121.934279,40.79805],[121.936127,40.711462],[121.951525,40.680607],[122.025438,40.674253],[122.06609,40.64883],[122.122141,40.657457],[122.148626,40.671983],[122.133843,40.614313],[122.150474,40.588413],[122.245944,40.519752],[122.231162,40.505192],[122.265038,40.48016],[122.221923,40.481071],[122.240401,40.461039],[122.250872,40.445555],[122.229314,40.424146],[122.186814,40.422779],[122.198517,40.382219],[122.152322,40.357597],[122.135691,40.374925],[122.111054,40.348932],[122.138155,40.338897],[122.110438,40.315629],[122.079641,40.332967],[122.040221,40.322017],[122.039605,40.260391],[122.02667,40.244862],[121.940438,40.242121],[121.950293,40.204194],[121.98109,40.173106],[122.003264,40.172191],[121.995257,40.128277],[121.956453,40.133311],[121.910257,40.072887],[121.824642,40.025701],[121.796309,39.999116],[121.779062,39.942702],[121.76428,39.933525],[121.699606,39.937196],[121.626925,39.882569],[121.572107,39.865116],[121.541926,39.874302],[121.530223,39.851334],[121.472325,39.802155],[121.487107,39.760303],[121.45939,39.747881],[121.502506,39.703233],[121.482796,39.659478],[121.451999,39.658095],[121.450151,39.624914],[121.325731,39.601402],[121.299246,39.606013],[121.263521,39.589873],[121.226565,39.554814],[121.224717,39.519275],[121.268449,39.482794],[121.286927,39.507271],[121.301709,39.476327],[121.245659,39.456923],[121.270296,39.434277],[121.246891,39.421334],[121.245659,39.389427],[121.270296,39.374162],[121.307869,39.391277],[121.324499,39.371386],[121.35468,39.377863],[121.432904,39.357506],[121.435984,39.329736],[121.466781,39.320014],[121.474788,39.296398],[121.508665,39.29223],[121.51544,39.286672],[121.562252,39.322792],[121.621382,39.326033],[121.72486,39.364447],[121.711925,39.33992],[121.7187,39.320477],[121.667577,39.310754],[121.672505,39.275554],[121.623846,39.285745],[121.589353,39.263044],[121.631237,39.22643],[121.591201,39.228748],[121.586889,39.193506],[121.604136,39.166136],[121.639244,39.166136],[121.68236,39.117863],[121.631853,39.077921],[121.605983,39.080708],[121.642324,39.11972],[121.590585,39.154999],[121.562252,39.127149],[121.599208,39.098824],[121.581962,39.075598],[121.508049,39.034237],[121.431057,39.027263],[121.370695,39.060264],[121.317108,39.012384],[121.341129,38.980757],[121.275224,38.971917],[121.204391,38.941202],[121.180369,38.959819],[121.128014,38.958888],[121.08921,38.922115],[121.094138,38.894173],[121.129862,38.879266],[121.110768,38.862026],[121.12863,38.799089],[121.112,38.776231],[121.13787,38.723023],[121.198848,38.721623],[121.259825,38.786495],[121.280767,38.786961],[121.288775,38.78976],[121.315876,38.793958],[121.359608,38.822406],[121.399028,38.812613],[121.509897,38.817743],[121.564715,38.874607],[121.618302,38.862492],[121.675585,38.86156],[121.708845,38.872744],[121.719316,38.920252],[121.655874,38.946788],[121.618918,38.950046],[121.66265,38.966333],[121.671273,39.010059],[121.73841,38.998898],[121.756889,39.025869],[121.790149,39.022614],[121.804932,38.970986],[121.863446,38.942598],[121.920728,38.969591],[121.905946,38.997503],[121.852975,39.035631],[121.8887,39.027263],[121.929352,39.024939],[121.907178,39.055617],[121.923192,39.053758],[121.963228,39.030053],[122.013735,39.073275],[122.061778,39.060264],[122.071634,39.074204],[122.048228,39.101146],[122.088264,39.112291],[122.127684,39.144788],[122.167104,39.158711],[122.123988,39.172631],[122.117213,39.213911],[122.160329,39.238019],[122.242865,39.267678],[122.274893,39.322329],[122.30877,39.346399],[122.366053,39.370461],[122.412864,39.411625],[122.455364,39.408388],[122.467682,39.403301],[122.51203,39.413474],[122.532972,39.419947],[122.581631,39.464316],[122.637066,39.488799],[122.649385,39.516505],[122.682645,39.514658],[122.808913,39.559889],[122.847101,39.581571],[122.860652,39.604629],[122.941956,39.604629],[122.972753,39.594946],[122.978912,39.616156],[123.021412,39.64335],[123.010941,39.655331],[123.103332,39.676983],[123.146448,39.647037],[123.166774,39.674219],[123.212969,39.665928],[123.215433,39.696786],[123.253005,39.689879],[123.286882,39.704154],[123.270251,39.714743],[123.274563,39.753862],[123.350939,39.750641],[123.388512,39.74742],[123.392823,39.723949],[123.477823,39.74696],[123.521555,39.772724],[123.534489,39.788361]]],[[[122.63953,39.286209],[122.593334,39.278334],[122.539131,39.308439],[122.50895,39.290377],[122.57732,39.269994],[122.67895,39.268605],[122.673406,39.269531],[122.662935,39.273701],[122.655544,39.277407],[122.640761,39.288061],[122.63953,39.286209]]],[[[122.318625,39.170775],[122.345111,39.144788],[122.366053,39.174951],[122.398697,39.16196],[122.383299,39.190723],[122.393154,39.213448],[122.343263,39.203246],[122.322321,39.177271],[122.322937,39.174487],[122.319241,39.172167],[122.318625,39.170775]]],[[[122.691884,39.23292],[122.696812,39.206492],[122.751631,39.229675],[122.740544,39.248679],[122.635834,39.241727],[122.628443,39.231993],[122.690037,39.234774],[122.691268,39.23431],[122.691884,39.23292]]],[[[122.738696,39.034701],[122.704819,39.044463],[122.733152,39.014244],[122.75779,39.009594],[122.739312,39.036561],[122.738696,39.034701]]],[[[123.022644,39.546507],[122.96105,39.551122],[122.945035,39.520198],[122.995542,39.495264],[123.036194,39.533123],[123.022644,39.546507]]],[[[122.503407,39.241263],[122.502175,39.224112],[122.547755,39.229211],[122.503407,39.241263]]],[[[120.786784,40.473787],[120.83298,40.491995],[120.8299,40.516112],[120.805262,40.525666],[120.774465,40.48016],[120.786784,40.473787]]],[[[123.086702,39.426881],[123.090397,39.450915],[123.054057,39.457847],[123.086702,39.426881]]],[[[123.160614,39.025404],[123.205578,39.057011],[123.20065,39.077921],[123.145832,39.091857],[123.143984,39.038885],[123.160614,39.025404]]],[[[123.716807,39.74512],[123.756843,39.754322],[123.719887,39.763063],[123.716807,39.74512]]]]}},
    {"type":"Feature","properties":{"adcode":220000,"name":"吉林省","center":[125.3245,43.886841],"centroid":[126.171208,43.703954],"childrenNum":9,"level":"province","parent":{"adcode":100000},"subFeatureIndex":6,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[129.601492,42.415116],[129.601492,42.422627],[129.591021,42.447803],[129.627361,42.462816],[129.651999,42.426603],[129.704354,42.427045],[129.748701,42.471204],[129.738846,42.500332],[129.749933,42.546644],[129.746237,42.58455],[129.786889,42.615387],[129.754245,42.645768],[129.796744,42.681854],[129.767179,42.707806],[129.78381,42.762752],[129.810911,42.795257],[129.816454,42.851003],[129.835549,42.866796],[129.846636,42.918533],[129.874969,42.923792],[129.856491,42.951833],[129.868193,42.97373],[129.903918,42.968475],[129.897143,43.001748],[129.954425,43.010938],[129.963664,42.978547],[130.002468,42.981174],[130.027106,42.9676],[130.072685,42.971541],[130.10841,42.989929],[130.144134,42.976357],[130.120729,42.954461],[130.127504,42.932556],[130.10225,42.922916],[130.136127,42.90363],[130.17062,42.912397],[130.21004,42.902315],[130.258083,42.90626],[130.277793,42.892232],[130.258083,42.860655],[130.245148,42.799209],[130.242069,42.738582],[130.257467,42.710884],[130.290112,42.702968],[130.333228,42.64973],[130.373264,42.630799],[130.388046,42.603054],[130.420691,42.617148],[130.44656,42.607459],[130.423771,42.574855],[130.435474,42.553257],[130.476125,42.570007],[130.459495,42.588075],[130.482285,42.626837],[130.522937,42.622433],[130.520473,42.593362],[130.558661,42.495919],[130.585763,42.485328],[130.581451,42.435437],[130.645509,42.426603],[130.600545,42.450453],[130.599929,42.486211],[130.565437,42.506509],[130.570364,42.557224],[130.622719,42.573092],[130.633806,42.603494],[130.592538,42.671295],[130.521089,42.702089],[130.464423,42.688453],[130.425003,42.706926],[130.40714,42.731548],[130.46627,42.772417],[130.532792,42.787352],[130.562357,42.815015],[130.603625,42.819405],[130.665835,42.847932],[130.708335,42.846615],[130.719422,42.831695],[130.75453,42.845738],[130.784095,42.842227],[130.801957,42.879515],[130.845073,42.881269],[130.890653,42.852758],[130.912826,42.870744],[130.949783,42.876884],[130.981812,42.857145],[131.043406,42.862848],[131.017536,42.915027],[131.034167,42.929051],[131.114855,42.915027],[131.145652,42.9365],[131.151195,42.968475],[131.115471,42.975482],[131.11855,43.007875],[131.102536,43.021002],[131.120398,43.068238],[131.171521,43.06955],[131.173985,43.111506],[131.207861,43.1316],[131.218948,43.191405],[131.201086,43.203185],[131.206014,43.237202],[131.255289,43.265099],[131.269455,43.297775],[131.275615,43.369165],[131.314419,43.392653],[131.295941,43.441774],[131.314419,43.461325],[131.31873,43.499539],[131.304564,43.502144],[131.294093,43.470012],[131.234963,43.475224],[131.201086,43.442209],[131.175217,43.444816],[131.142572,43.425695],[131.026775,43.508655],[130.959638,43.48608],[130.907283,43.434387],[130.864167,43.437863],[130.841378,43.454374],[130.822899,43.503446],[130.776704,43.52341],[130.727429,43.560284],[130.671378,43.565054],[130.665835,43.583698],[130.623335,43.589767],[130.630726,43.622268],[130.57098,43.626167],[130.57098,43.626167],[130.501995,43.636563],[130.488444,43.65605],[130.437937,43.646091],[130.412684,43.652586],[130.394206,43.703227],[130.423155,43.745179],[130.382503,43.777164],[130.381887,43.817768],[130.362793,43.844967],[130.386198,43.85403],[130.368336,43.894151],[130.381887,43.910106],[130.338155,43.963975],[130.364025,43.992399],[130.365256,44.044042],[130.319061,44.03974],[130.307358,44.002731],[130.27225,43.981634],[130.262395,43.949328],[130.208192,43.948466],[130.153373,43.915711],[130.143518,43.878624],[130.116417,43.878192],[130.110873,43.852735],[130.079461,43.835039],[130.027722,43.851872],[130.009243,43.889407],[130.022794,43.917866],[130.017867,43.961821],[129.979062,44.015644],[129.951345,44.027263],[129.907614,44.023821],[129.881128,44.000148],[129.868193,44.012631],[129.802904,43.964837],[129.780114,43.892857],[129.739462,43.895876],[129.743158,43.876035],[129.699426,43.8838],[129.650767,43.873016],[129.529427,43.870427],[129.467833,43.874741],[129.449971,43.850578],[129.417942,43.843672],[129.406855,43.819496],[129.348341,43.798333],[129.30892,43.812155],[129.289826,43.797038],[129.254718,43.819496],[129.211602,43.784509],[129.232544,43.709284],[129.214066,43.695006],[129.217146,43.648689],[129.232544,43.635263],[129.23008,43.593234],[129.169102,43.561585],[129.145081,43.570258],[129.093958,43.547706],[129.037907,43.540332],[129.013886,43.522976],[128.962763,43.53903],[128.949828,43.553779],[128.878379,43.539898],[128.834647,43.587599],[128.821097,43.637429],[128.78722,43.686784],[128.768126,43.732207],[128.729322,43.736964],[128.760119,43.755554],[128.739177,43.806972],[128.719467,43.816905],[128.760734,43.857482],[128.729938,43.889838],[128.696061,43.903207],[128.636315,43.891132],[128.64001,43.948035],[128.610445,43.960529],[128.584576,43.990246],[128.574721,44.047914],[128.529141,44.112401],[128.471859,44.157501],[128.450301,44.203423],[128.471859,44.247596],[128.453997,44.257884],[128.472475,44.320001],[128.446605,44.339694],[128.475555,44.346114],[128.481714,44.375637],[128.457076,44.409848],[128.463236,44.431647],[128.427511,44.473512],[128.397946,44.483761],[128.372693,44.514495],[128.295084,44.480772],[128.293237,44.467961],[128.228563,44.445748],[128.211317,44.431647],[128.172512,44.34697],[128.137404,44.357668],[128.094904,44.354673],[128.074578,44.370075],[128.049941,44.349965],[128.065339,44.307155],[128.101679,44.293449],[128.064107,44.251454],[128.104143,44.230017],[128.09244,44.181539],[128.060411,44.168663],[128.088129,44.158359],[128.091208,44.133022],[128.042549,44.103807],[127.950158,44.088334],[127.912586,44.064687],[127.862695,44.062967],[127.846065,44.081886],[127.808492,44.086615],[127.783239,44.071997],[127.729036,44.09908],[127.735811,44.11412],[127.712406,44.199133],[127.681609,44.166946],[127.641573,44.193555],[127.626174,44.187977],[127.59045,44.227872],[127.623711,44.278025],[127.579363,44.310581],[127.486356,44.410275],[127.50853,44.437202],[127.463566,44.484615],[127.465414,44.516628],[127.485124,44.528576],[127.536247,44.522176],[127.570124,44.55033],[127.557189,44.575488],[127.392733,44.632158],[127.275705,44.640249],[127.261538,44.61299],[127.214111,44.624917],[127.228893,44.642804],[127.182082,44.644507],[127.138966,44.607451],[127.094619,44.615972],[127.089691,44.593816],[127.049655,44.566961],[127.041648,44.591258],[127.044112,44.653874],[127.030561,44.673454],[127.041032,44.712169],[126.9973,44.764882],[126.984366,44.823914],[126.999764,44.87398],[127.021938,44.898997],[127.073061,44.907051],[127.092771,44.94688],[127.050271,45.004034],[127.018242,45.024341],[126.984981,45.067893],[126.970815,45.070852],[126.96404,45.132104],[126.85625,45.145613],[126.792808,45.135481],[126.787265,45.159118],[126.732446,45.187385],[126.685635,45.187807],[126.640055,45.214373],[126.644983,45.225334],[126.569222,45.252725],[126.540273,45.23882],[126.519331,45.248091],[126.402919,45.222805],[126.356107,45.185698],[126.293282,45.180214],[126.285274,45.162494],[126.235383,45.140125],[126.225528,45.154054],[126.166398,45.13337],[126.142992,45.147723],[126.091869,45.149411],[126.047522,45.170933],[125.998247,45.162072],[125.992703,45.192447],[125.957595,45.201303],[125.915095,45.196664],[125.849805,45.23882],[125.823936,45.237978],[125.815929,45.264942],[125.761726,45.291472],[125.726001,45.336503],[125.695205,45.352066],[125.712451,45.389485],[125.711835,45.477677],[125.687813,45.514173],[125.660096,45.507043],[125.61698,45.517947],[125.583104,45.491942],[125.497488,45.469283],[125.480242,45.486488],[125.424807,45.485649],[125.434662,45.462988],[125.398322,45.416797],[125.361981,45.392847],[125.319482,45.422678],[125.301619,45.402092],[125.248649,45.417637],[125.189518,45.39915],[125.137779,45.409655],[125.097127,45.38276],[125.06633,45.39915],[125.08912,45.420998],[125.0497,45.428558],[125.025678,45.493201],[124.961005,45.495299],[124.936983,45.53388],[124.911114,45.535976],[124.884628,45.495299],[124.886476,45.442836],[124.839665,45.455852],[124.792853,45.436958],[124.776223,45.468024],[124.729412,45.444096],[124.690607,45.452493],[124.625318,45.437377],[124.575427,45.451234],[124.579738,45.424358],[124.544014,45.411756],[124.507058,45.424778],[124.480572,45.456271],[124.398652,45.440737],[124.374015,45.45795],[124.352457,45.496557],[124.369087,45.512915],[124.348761,45.546874],[124.287783,45.539329],[124.264377,45.555256],[124.273001,45.584163],[124.238508,45.591702],[124.226805,45.633564],[124.162132,45.616404],[124.128255,45.641933],[124.147349,45.665359],[124.122096,45.669123],[124.13503,45.690448],[124.10177,45.700898],[124.098074,45.722628],[124.054342,45.751449],[124.014922,45.749779],[124.001987,45.770655],[124.064197,45.802372],[124.03648,45.83824],[124.067277,45.840325],[124.061118,45.886168],[123.996444,45.906993],[123.968727,45.936551],[123.973654,45.973997],[124.011842,45.981899],[123.989053,46.011833],[124.040176,46.01973],[124.034016,46.045074],[124.009995,46.057534],[124.015538,46.088257],[123.99398,46.101123],[124.01677,46.118549],[123.991516,46.143019],[124.001987,46.166649],[123.971806,46.170379],[123.956408,46.206009],[123.979814,46.228784],[123.952096,46.256516],[123.960103,46.288369],[123.936082,46.286715],[123.917604,46.25693],[123.896046,46.303668],[123.84985,46.302428],[123.775938,46.263136],[123.726047,46.255688],[123.673692,46.258585],[123.604706,46.251964],[123.569598,46.223816],[123.569598,46.223816],[123.499381,46.259826],[123.452569,46.233338],[123.430396,46.243687],[123.357099,46.232096],[123.357099,46.232096],[123.320758,46.254447],[123.286266,46.250308],[123.248078,46.273065],[123.178476,46.248239],[123.128585,46.210565],[123.127354,46.174523],[123.102716,46.172037],[123.112571,46.130163],[123.070071,46.123527],[123.04605,46.099878],[122.792898,46.073313],[122.828623,45.912406],[122.80029,45.856583],[122.772572,45.856583],[122.752246,45.834905],[122.792283,45.766063],[122.751015,45.735996],[122.741775,45.705077],[122.671558,45.70048],[122.650001,45.731401],[122.640761,45.771072],[122.603189,45.778169],[122.556378,45.82156],[122.522501,45.786933],[122.504639,45.786933],[122.496016,45.85825],[122.446125,45.916986],[122.362357,45.917403],[122.372828,45.856166],[122.337719,45.859917],[122.301379,45.813218],[122.253952,45.7982],[122.236705,45.831569],[122.200981,45.857],[122.091344,45.882002],[122.085184,45.912406],[122.040221,45.959022],[121.92812,45.988552],[121.923808,46.004767],[121.864062,46.002272],[121.843736,46.024301],[121.819098,46.023054],[121.761816,45.998947],[121.809243,45.961102],[121.821562,45.918235],[121.805548,45.900746],[121.817251,45.875336],[121.769823,45.84366],[121.766744,45.830318],[121.766744,45.830318],[121.754425,45.794862],[121.697142,45.76314],[121.657106,45.770238],[121.644172,45.752284],[121.666345,45.727641],[121.713773,45.701734],[121.811091,45.687103],[121.812323,45.704659],[121.867142,45.719703],[121.934279,45.71051],[121.970004,45.692956],[122.003264,45.623102],[121.995873,45.59882],[121.966308,45.596308],[121.993409,45.552741],[122.002648,45.507882],[122.064242,45.472641],[122.168336,45.439897],[122.180039,45.409655],[122.146778,45.374352],[122.147394,45.295682],[122.239169,45.276313],[122.22993,45.206784],[122.192358,45.180636],[122.143082,45.183167],[122.109822,45.142236],[122.119677,45.068739],[122.098735,45.02138],[122.074713,45.006573],[122.087032,44.95281],[122.079025,44.914256],[122.04946,44.912985],[122.098119,44.81882],[122.099967,44.7823],[122.168952,44.770405],[122.142467,44.753833],[122.110438,44.767856],[122.10243,44.736406],[122.152322,44.744057],[122.161561,44.728328],[122.117213,44.701961],[122.103046,44.67388],[122.113517,44.615546],[122.13138,44.577619],[122.196053,44.559712],[122.224386,44.526016],[122.228082,44.480345],[122.28598,44.477783],[122.294604,44.41113],[122.291524,44.310152],[122.271198,44.255741],[122.319241,44.233018],[122.483081,44.236877],[122.515726,44.251025],[122.641993,44.283595],[122.675254,44.285738],[122.702971,44.319145],[122.76087,44.369648],[122.85634,44.398304],[123.025108,44.493153],[123.06576,44.505959],[123.12489,44.5098],[123.137209,44.486322],[123.125506,44.455147],[123.142136,44.428228],[123.114419,44.40258],[123.128585,44.367081],[123.196955,44.34483],[123.277027,44.25274],[123.286882,44.211574],[123.323838,44.179823],[123.386664,44.161794],[123.362642,44.133452],[123.350939,44.092633],[123.32815,44.084035],[123.331229,44.028984],[123.365722,44.013922],[123.400831,43.979481],[123.37065,43.970006],[123.397135,43.954929],[123.467968,43.853599],[123.461809,43.822518],[123.498149,43.771114],[123.48275,43.737396],[123.520323,43.708419],[123.518475,43.682024],[123.536953,43.633964],[123.510468,43.624867],[123.5117,43.592801],[123.421157,43.598435],[123.434091,43.575461],[123.461193,43.568523],[123.452569,43.545971],[123.452569,43.545971],[123.360179,43.567223],[123.304744,43.550742],[123.329998,43.519071],[123.315831,43.492159],[123.36449,43.483475],[123.382968,43.469143],[123.419925,43.410046],[123.442098,43.437863],[123.486446,43.44525],[123.519707,43.402219],[123.54496,43.415262],[123.608402,43.366119],[123.703873,43.37047],[123.710032,43.417001],[123.749452,43.439167],[123.747604,43.472184],[123.79688,43.489988],[123.857858,43.459153],[123.857858,43.459153],[123.852314,43.406133],[123.881263,43.392218],[123.881263,43.392218],[123.896046,43.361333],[123.964415,43.34088],[124.032784,43.280786],[124.099306,43.292983],[124.117168,43.2773],[124.114088,43.247229],[124.168291,43.244177],[124.215102,43.255947],[124.228653,43.235022],[124.27608,43.233278],[124.287167,43.207983],[124.273617,43.17875],[124.366007,43.121554],[124.425754,43.076107],[124.333363,42.997371],[124.369703,42.972854],[124.42329,42.975482],[124.442384,42.958841],[124.431913,42.930803],[124.38079,42.912835],[124.371551,42.880831],[124.435609,42.880831],[124.466406,42.847054],[124.586514,42.905384],[124.607456,42.937376],[124.632093,42.949642],[124.635173,42.972854],[124.658579,42.972854],[124.677673,43.002185],[124.686912,43.051185],[124.719557,43.069987],[124.755281,43.074359],[124.785462,43.117185],[124.882781,43.13422],[124.88894,43.074796],[124.840897,43.032377],[124.869846,42.988178],[124.87231,42.962344],[124.84952,42.882585],[124.856911,42.824234],[124.874157,42.789987],[124.897563,42.787791],[124.92836,42.819844],[124.975171,42.802722],[124.996729,42.745174],[124.968396,42.722756],[124.99057,42.677455],[125.014592,42.666014],[125.010896,42.63212],[125.038613,42.615387],[125.097127,42.622433],[125.082961,42.591159],[125.089736,42.567803],[125.066946,42.534738],[125.090968,42.515773],[125.068794,42.499449],[125.105135,42.490624],[125.150098,42.458842],[125.140243,42.44692],[125.186439,42.427928],[125.185823,42.38197],[125.203685,42.366938],[125.167345,42.351903],[125.175352,42.308102],[125.224011,42.30102],[125.264047,42.312528],[125.299156,42.289953],[125.27575,42.266928],[125.27575,42.231045],[125.312706,42.219966],[125.280677,42.175187],[125.312706,42.197359],[125.305931,42.146351],[125.357054,42.145464],[125.368141,42.182726],[125.41372,42.156112],[125.458068,42.160105],[125.458068,42.160105],[125.490097,42.136145],[125.446365,42.098411],[125.414336,42.101964],[125.416184,42.063766],[125.363213,42.017097],[125.369989,42.002868],[125.29854,41.974399],[125.291764,41.958825],[125.35151,41.92811],[125.307779,41.924548],[125.294844,41.822945],[125.319482,41.776993],[125.319482,41.776993],[125.323177,41.771191],[125.323177,41.771191],[125.336112,41.768067],[125.336112,41.768067],[125.332416,41.711354],[125.317018,41.676944],[125.344119,41.672474],[125.412488,41.691246],[125.446981,41.67605],[125.461148,41.642516],[125.450061,41.597777],[125.479626,41.544946],[125.507343,41.534195],[125.493176,41.509103],[125.533212,41.479069],[125.534444,41.428833],[125.547995,41.401006],[125.581256,41.396517],[125.589879,41.359245],[125.610205,41.365084],[125.637306,41.34442],[125.62006,41.318355],[125.642234,41.296327],[125.646545,41.264396],[125.685349,41.273842],[125.695205,41.244599],[125.749407,41.245499],[125.758646,41.232449],[125.73832,41.178418],[125.791291,41.167607],[125.759878,41.132908],[125.734009,41.125695],[125.712451,41.095485],[125.739552,41.08917],[125.726617,41.055332],[125.684118,41.021929],[125.674879,40.974503],[125.650241,40.970888],[125.635458,40.94151],[125.589263,40.931112],[125.584335,40.891764],[125.652089,40.91619],[125.687813,40.897645],[125.707523,40.866877],[125.778356,40.897645],[125.817161,40.866877],[125.860892,40.888597],[125.875059,40.908501],[125.921254,40.882715],[125.959442,40.88181],[126.008102,40.936537],[126.041362,40.928851],[126.051833,40.96185],[126.08263,40.976762],[126.066,40.997542],[126.1085,41.011995],[126.099877,41.036376],[126.133753,41.063906],[126.124514,41.092327],[126.16763,41.094583],[126.187956,41.113072],[126.188572,41.114875],[126.295129,41.171661],[126.332086,41.236949],[126.35426,41.244599],[126.373354,41.289133],[126.437411,41.353405],[126.497158,41.374965],[126.524259,41.349362],[126.539041,41.366881],[126.497158,41.406842],[126.559983,41.548081],[126.582773,41.563307],[126.564295,41.608965],[126.592628,41.624624],[126.608027,41.669345],[126.644983,41.661297],[126.688099,41.674262],[126.724439,41.710907],[126.690562,41.728328],[126.694874,41.751103],[126.723207,41.753335],[126.8002,41.702865],[126.809439,41.749317],[126.848243,41.734134],[126.85625,41.760031],[126.887047,41.791719],[126.931395,41.812687],[126.952953,41.804212],[126.940018,41.773423],[126.979438,41.776993],[127.005923,41.749317],[127.050887,41.744852],[127.057662,41.703758],[127.037952,41.676944],[127.103242,41.647883],[127.093387,41.629993],[127.127263,41.622388],[127.135887,41.600463],[127.178386,41.600015],[127.125416,41.566442],[127.11864,41.540018],[127.164836,41.542706],[127.188241,41.527475],[127.241212,41.520754],[127.28864,41.501932],[127.253531,41.486691],[127.296031,41.486243],[127.360704,41.466065],[127.360088,41.479518],[127.405668,41.478621],[127.419835,41.460235],[127.459255,41.461581],[127.465414,41.479069],[127.526392,41.467859],[127.547334,41.477276],[127.563964,41.432871],[127.618783,41.432871],[127.636645,41.413575],[127.684073,41.422999],[127.780159,41.427038],[127.854688,41.420755],[127.86947,41.4037],[127.882405,41.448124],[127.909506,41.42973],[127.93168,41.444984],[127.970484,41.438704],[127.991426,41.421204],[128.000049,41.442741],[128.040085,41.393375],[128.110919,41.393375],[128.090593,41.374516],[128.114614,41.364186],[128.169433,41.404149],[128.203925,41.410882],[128.243345,41.477276],[128.238418,41.497898],[128.301244,41.540018],[128.317874,41.575844],[128.30186,41.627756],[128.248889,41.681414],[128.208853,41.688565],[128.163889,41.721628],[128.147875,41.78101],[128.112766,41.793504],[128.104143,41.843457],[128.115846,41.896935],[128.106607,41.949923],[128.033926,42.000199],[128.090593,42.022877],[128.294468,42.026434],[128.405338,42.018876],[128.466316,42.020654],[128.49896,42.000644],[128.598127,42.007315],[128.60675,42.02999],[128.637547,42.035324],[128.658489,42.018876],[128.70222,42.02021],[128.737945,42.050435],[128.779213,42.033546],[128.795227,42.042436],[128.898089,42.016653],[128.952908,42.025545],[128.954755,42.083756],[128.971386,42.097079],[129.008958,42.09175],[129.039139,42.107736],[129.048378,42.137476],[129.113668,42.140583],[129.166639,42.188047],[129.215914,42.208442],[129.209138,42.237692],[129.181421,42.242122],[129.183269,42.262056],[129.215914,42.265157],[129.231312,42.283755],[129.208522,42.293052],[129.260261,42.335536],[129.231312,42.356325],[129.240551,42.376223],[129.326167,42.389927],[129.30892,42.403628],[129.331094,42.429695],[129.356348,42.427045],[129.342181,42.441179],[129.368051,42.459284],[129.366203,42.428811],[129.392688,42.42837],[129.400695,42.449128],[129.452434,42.441179],[129.49863,42.412023],[129.546057,42.361632],[129.578086,42.380202],[129.569463,42.399208],[129.601492,42.415116]]]]}},
    {"type":"Feature","properties":{"adcode":230000,"name":"黑龙江省","center":[126.642464,45.756967],"centroid":[127.693027,48.040465],"childrenNum":13,"level":"province","parent":{"adcode":100000},"subFeatureIndex":7,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[123.569598,46.223816],[123.604706,46.251964],[123.673692,46.258585],[123.726047,46.255688],[123.775938,46.263136],[123.84985,46.302428],[123.896046,46.303668],[123.917604,46.25693],[123.936082,46.286715],[123.960103,46.288369],[123.952096,46.256516],[123.979814,46.228784],[123.956408,46.206009],[123.971806,46.170379],[124.001987,46.166649],[123.991516,46.143019],[124.01677,46.118549],[123.99398,46.101123],[124.015538,46.088257],[124.009995,46.057534],[124.034016,46.045074],[124.040176,46.01973],[123.989053,46.011833],[124.011842,45.981899],[123.973654,45.973997],[123.968727,45.936551],[123.996444,45.906993],[124.061118,45.886168],[124.067277,45.840325],[124.03648,45.83824],[124.064197,45.802372],[124.001987,45.770655],[124.014922,45.749779],[124.054342,45.751449],[124.098074,45.722628],[124.10177,45.700898],[124.13503,45.690448],[124.122096,45.669123],[124.147349,45.665359],[124.128255,45.641933],[124.162132,45.616404],[124.226805,45.633564],[124.238508,45.591702],[124.273001,45.584163],[124.264377,45.555256],[124.287783,45.539329],[124.348761,45.546874],[124.369087,45.512915],[124.352457,45.496557],[124.374015,45.45795],[124.398652,45.440737],[124.480572,45.456271],[124.507058,45.424778],[124.544014,45.411756],[124.579738,45.424358],[124.575427,45.451234],[124.625318,45.437377],[124.690607,45.452493],[124.729412,45.444096],[124.776223,45.468024],[124.792853,45.436958],[124.839665,45.455852],[124.886476,45.442836],[124.884628,45.495299],[124.911114,45.535976],[124.936983,45.53388],[124.961005,45.495299],[125.025678,45.493201],[125.0497,45.428558],[125.08912,45.420998],[125.06633,45.39915],[125.097127,45.38276],[125.137779,45.409655],[125.189518,45.39915],[125.248649,45.417637],[125.301619,45.402092],[125.319482,45.422678],[125.361981,45.392847],[125.398322,45.416797],[125.434662,45.462988],[125.424807,45.485649],[125.480242,45.486488],[125.497488,45.469283],[125.583104,45.491942],[125.61698,45.517947],[125.660096,45.507043],[125.687813,45.514173],[125.711835,45.477677],[125.712451,45.389485],[125.695205,45.352066],[125.726001,45.336503],[125.761726,45.291472],[125.815929,45.264942],[125.823936,45.237978],[125.849805,45.23882],[125.915095,45.196664],[125.957595,45.201303],[125.992703,45.192447],[125.998247,45.162072],[126.047522,45.170933],[126.091869,45.149411],[126.142992,45.147723],[126.166398,45.13337],[126.225528,45.154054],[126.235383,45.140125],[126.285274,45.162494],[126.293282,45.180214],[126.356107,45.185698],[126.402919,45.222805],[126.519331,45.248091],[126.540273,45.23882],[126.569222,45.252725],[126.644983,45.225334],[126.640055,45.214373],[126.685635,45.187807],[126.732446,45.187385],[126.787265,45.159118],[126.792808,45.135481],[126.85625,45.145613],[126.96404,45.132104],[126.970815,45.070852],[126.984981,45.067893],[127.018242,45.024341],[127.050271,45.004034],[127.092771,44.94688],[127.073061,44.907051],[127.021938,44.898997],[126.999764,44.87398],[126.984366,44.823914],[126.9973,44.764882],[127.041032,44.712169],[127.030561,44.673454],[127.044112,44.653874],[127.041648,44.591258],[127.049655,44.566961],[127.089691,44.593816],[127.094619,44.615972],[127.138966,44.607451],[127.182082,44.644507],[127.228893,44.642804],[127.214111,44.624917],[127.261538,44.61299],[127.275705,44.640249],[127.392733,44.632158],[127.557189,44.575488],[127.570124,44.55033],[127.536247,44.522176],[127.485124,44.528576],[127.465414,44.516628],[127.463566,44.484615],[127.50853,44.437202],[127.486356,44.410275],[127.579363,44.310581],[127.623711,44.278025],[127.59045,44.227872],[127.626174,44.187977],[127.641573,44.193555],[127.681609,44.166946],[127.712406,44.199133],[127.735811,44.11412],[127.729036,44.09908],[127.783239,44.071997],[127.808492,44.086615],[127.846065,44.081886],[127.862695,44.062967],[127.912586,44.064687],[127.950158,44.088334],[128.042549,44.103807],[128.091208,44.133022],[128.088129,44.158359],[128.060411,44.168663],[128.09244,44.181539],[128.104143,44.230017],[128.064107,44.251454],[128.101679,44.293449],[128.065339,44.307155],[128.049941,44.349965],[128.074578,44.370075],[128.094904,44.354673],[128.137404,44.357668],[128.172512,44.34697],[128.211317,44.431647],[128.228563,44.445748],[128.293237,44.467961],[128.295084,44.480772],[128.372693,44.514495],[128.397946,44.483761],[128.427511,44.473512],[128.463236,44.431647],[128.457076,44.409848],[128.481714,44.375637],[128.475555,44.346114],[128.446605,44.339694],[128.472475,44.320001],[128.453997,44.257884],[128.471859,44.247596],[128.450301,44.203423],[128.471859,44.157501],[128.529141,44.112401],[128.574721,44.047914],[128.584576,43.990246],[128.610445,43.960529],[128.64001,43.948035],[128.636315,43.891132],[128.696061,43.903207],[128.729938,43.889838],[128.760734,43.857482],[128.719467,43.816905],[128.739177,43.806972],[128.760119,43.755554],[128.729322,43.736964],[128.768126,43.732207],[128.78722,43.686784],[128.821097,43.637429],[128.834647,43.587599],[128.878379,43.539898],[128.949828,43.553779],[128.962763,43.53903],[129.013886,43.522976],[129.037907,43.540332],[129.093958,43.547706],[129.145081,43.570258],[129.169102,43.561585],[129.23008,43.593234],[129.232544,43.635263],[129.217146,43.648689],[129.214066,43.695006],[129.232544,43.709284],[129.211602,43.784509],[129.254718,43.819496],[129.289826,43.797038],[129.30892,43.812155],[129.348341,43.798333],[129.406855,43.819496],[129.417942,43.843672],[129.449971,43.850578],[129.467833,43.874741],[129.529427,43.870427],[129.650767,43.873016],[129.699426,43.8838],[129.743158,43.876035],[129.739462,43.895876],[129.780114,43.892857],[129.802904,43.964837],[129.868193,44.012631],[129.881128,44.000148],[129.907614,44.023821],[129.951345,44.027263],[129.979062,44.015644],[130.017867,43.961821],[130.022794,43.917866],[130.009243,43.889407],[130.027722,43.851872],[130.079461,43.835039],[130.110873,43.852735],[130.116417,43.878192],[130.143518,43.878624],[130.153373,43.915711],[130.208192,43.948466],[130.262395,43.949328],[130.27225,43.981634],[130.307358,44.002731],[130.319061,44.03974],[130.365256,44.044042],[130.364025,43.992399],[130.338155,43.963975],[130.381887,43.910106],[130.368336,43.894151],[130.386198,43.85403],[130.362793,43.844967],[130.381887,43.817768],[130.382503,43.777164],[130.423155,43.745179],[130.394206,43.703227],[130.412684,43.652586],[130.437937,43.646091],[130.488444,43.65605],[130.501995,43.636563],[130.57098,43.626167],[130.57098,43.626167],[130.630726,43.622268],[130.623335,43.589767],[130.665835,43.583698],[130.671378,43.565054],[130.727429,43.560284],[130.776704,43.52341],[130.822899,43.503446],[130.841378,43.454374],[130.864167,43.437863],[130.907283,43.434387],[130.959638,43.48608],[131.026775,43.508655],[131.142572,43.425695],[131.175217,43.444816],[131.201086,43.442209],[131.234963,43.475224],[131.294093,43.470012],[131.304564,43.502144],[131.276847,43.495632],[131.20047,43.532089],[131.222028,43.593234],[131.216485,43.613169],[131.239274,43.670337],[131.221412,43.682024],[131.215869,43.72745],[131.232499,43.742585],[131.213405,43.801357],[131.2171,43.836334],[131.254057,43.893289],[131.26268,43.948897],[131.245434,43.95579],[131.26576,44.034578],[131.28239,44.035868],[131.287318,44.03802],[131.293477,44.043182],[131.310723,44.046623],[131.111775,44.710042],[131.090833,44.717272],[131.093297,44.746183],[131.069275,44.759783],[131.064348,44.786973],[131.016304,44.789521],[131.015688,44.814999],[130.972573,44.820094],[130.965181,44.85065],[131.07913,44.881614],[131.10192,44.898997],[131.090217,44.924427],[131.16105,44.948151],[131.20355,44.932901],[131.207861,44.913833],[131.263296,44.929935],[131.274999,44.919766],[131.313803,44.950692],[131.313803,44.965938],[131.355071,44.990068],[131.380324,44.978216],[131.409889,44.985836],[131.464708,44.963397],[131.501664,44.977793],[131.484418,44.99557],[131.529382,45.012073],[131.566338,45.045487],[131.63286,45.075078],[131.695685,45.132104],[131.687678,45.1511],[131.650722,45.159962],[131.681519,45.215217],[131.721555,45.234606],[131.759127,45.213952],[131.79362,45.211844],[131.788692,45.245984],[131.825649,45.291472],[131.82996,45.311677],[131.887858,45.342393],[131.917423,45.339448],[131.93159,45.287683],[131.976554,45.277156],[132.003655,45.25441],[132.17427,45.216903],[132.394161,45.16376],[132.76434,45.081417],[132.867202,45.061976],[132.916477,45.031109],[132.954049,45.023072],[132.98731,45.043373],[133.035969,45.054366],[133.070462,45.097051],[133.089556,45.097473],[133.107418,45.124504],[133.139447,45.127459],[133.129592,45.211422],[133.095715,45.246827],[133.110498,45.266627],[133.097563,45.284735],[133.128976,45.336924],[133.119121,45.352908],[133.144991,45.367205],[133.143759,45.430658],[133.164701,45.437377],[133.170244,45.465506],[133.203505,45.516689],[133.246005,45.517528],[133.333468,45.562379],[133.342707,45.554836],[133.393214,45.580393],[133.423395,45.584163],[133.412924,45.618079],[133.471438,45.631053],[133.448649,45.647372],[133.485605,45.658667],[133.484989,45.691702],[133.445569,45.705077],[133.454192,45.731819],[133.486837,45.740173],[133.469591,45.777751],[133.505315,45.785681],[133.469591,45.799451],[133.467743,45.834905],[133.494228,45.840325],[133.491764,45.867002],[133.51209,45.887001],[133.55459,45.893249],[133.583539,45.868669],[133.618032,45.903662],[133.614952,45.942794],[133.676546,45.94321],[133.681474,45.986473],[133.740604,46.048812],[133.745531,46.075389],[133.690713,46.133896],[133.706111,46.163333],[133.764626,46.17328],[133.794807,46.193583],[133.814517,46.230854],[133.849625,46.203939],[133.87919,46.233752],[133.867487,46.250722],[133.909987,46.254447],[133.91861,46.280924],[133.908139,46.308216],[133.922922,46.330948],[133.869335,46.338386],[133.876726,46.362345],[133.940784,46.38134],[133.948791,46.401153],[133.902596,46.446119],[133.852089,46.450242],[133.849625,46.475389],[133.890893,46.525235],[133.919842,46.596012],[134.011001,46.637941],[134.030711,46.708981],[134.033175,46.759023],[134.052885,46.779928],[134.025168,46.810657],[134.041182,46.848326],[134.042414,46.886787],[134.076291,46.938298],[134.063972,46.979962],[134.10216,47.005678],[134.118175,47.061968],[134.142812,47.093349],[134.222268,47.105164],[134.232739,47.134892],[134.230276,47.182097],[134.210566,47.210155],[134.156979,47.248357],[134.177305,47.326299],[134.203174,47.347389],[134.263536,47.371307],[134.266616,47.391974],[134.307268,47.428829],[134.339297,47.439759],[134.490202,47.446235],[134.522847,47.468086],[134.568426,47.478199],[134.576434,47.519036],[134.627556,47.546512],[134.678064,47.588507],[134.689766,47.63813],[134.779694,47.7159],[134.772918,47.763391],[134.678679,47.819278],[134.670056,47.864667],[134.677448,47.884738],[134.658969,47.901191],[134.607846,47.909214],[134.599839,47.947711],[134.55426,47.982173],[134.551796,48.032622],[134.632484,48.099412],[134.67252,48.170505],[134.679295,48.256245],[134.77107,48.288908],[134.864077,48.332293],[135.009439,48.365703],[135.090743,48.403461],[135.09567,48.437618],[135.068569,48.459451],[135.035924,48.440795],[134.996504,48.439603],[134.927519,48.451513],[134.886867,48.437618],[134.848679,48.393925],[134.820961,48.37604],[134.764295,48.370076],[134.704549,48.405448],[134.640491,48.409818],[134.578281,48.405448],[134.501905,48.418954],[134.438463,48.405448],[134.369478,48.382797],[134.20379,48.3824],[134.150819,48.346217],[134.116327,48.333089],[134.0689,48.338659],[134.029479,48.327519],[133.995603,48.303639],[133.940784,48.302047],[133.876111,48.282536],[133.824372,48.277359],[133.791111,48.261026],[133.740604,48.254651],[133.693177,48.186866],[133.667307,48.183275],[133.59709,48.194846],[133.573068,48.182078],[133.545967,48.121389],[133.451728,48.112999],[133.407997,48.124585],[133.302055,48.103009],[133.239845,48.126583],[133.182563,48.135769],[133.130208,48.134971],[133.053216,48.110202],[133.02673,48.085421],[133.016259,48.054228],[132.992238,48.035424],[132.883216,48.002599],[132.819159,47.936887],[132.769268,47.93849],[132.723072,47.962941],[132.691043,47.962941],[132.661478,47.944905],[132.662094,47.922451],[132.687348,47.88514],[132.662094,47.854227],[132.621442,47.82852],[132.599268,47.792347],[132.6005,47.740858],[132.558,47.718316],[132.469305,47.726368],[132.371987,47.765402],[132.325175,47.762184],[132.288835,47.742065],[132.272205,47.718718],[132.242639,47.70986],[132.19706,47.714289],[132.157024,47.70543],[132.086191,47.703013],[132.000575,47.712276],[131.976554,47.673201],[131.900793,47.685692],[131.825649,47.677231],[131.741881,47.706638],[131.690142,47.707041],[131.641483,47.663932],[131.59036,47.660707],[131.568186,47.682469],[131.559563,47.724757],[131.543548,47.736028],[131.456085,47.747297],[131.359998,47.730796],[131.273767,47.738846],[131.236811,47.733211],[131.183224,47.702611],[131.115471,47.689721],[131.029855,47.694555],[130.983659,47.713081],[130.966413,47.733211],[130.961486,47.828118],[130.891269,47.927263],[130.870943,47.943301],[130.770544,47.998194],[130.737284,48.034223],[130.699711,48.044227],[130.666451,48.105007],[130.673842,48.12818],[130.765617,48.18926],[130.769313,48.231136],[130.787791,48.256643],[130.817972,48.265409],[130.845073,48.296473],[130.81982,48.341444],[130.785327,48.357353],[130.747755,48.404256],[130.745907,48.449131],[130.776704,48.480084],[130.767465,48.507846],[130.711414,48.511414],[130.647357,48.484844],[130.620871,48.49595],[130.615944,48.575601],[130.605473,48.594207],[130.538335,48.612016],[130.538951,48.635751],[130.576524,48.688719],[130.622103,48.783842],[130.689856,48.849651],[130.680617,48.881146],[130.609168,48.881146],[130.559277,48.861071],[130.501995,48.865795],[130.471198,48.905541],[130.412068,48.905148],[130.279641,48.866976],[130.237757,48.868551],[130.219895,48.893739],[130.113337,48.956653],[130.059135,48.979047],[130.020946,49.021058],[129.937179,49.040285],[129.9187,49.060681],[129.934715,49.078717],[129.913157,49.1085],[129.866962,49.113985],[129.855259,49.133567],[129.864498,49.158621],[129.847867,49.181316],[129.784426,49.184054],[129.753629,49.208692],[129.761636,49.25754],[129.730223,49.288387],[129.696962,49.298535],[129.604571,49.279018],[129.562687,49.299706],[129.546057,49.395227],[129.51834,49.423652],[129.448739,49.441167],[129.390224,49.432605],[129.374826,49.414309],[129.379138,49.367175],[129.358196,49.355871],[129.320623,49.3586],[129.266421,49.396006],[129.215298,49.399122],[129.180805,49.386657],[129.143849,49.357431],[129.084719,49.359769],[129.061929,49.374189],[129.013886,49.457119],[128.932582,49.46801],[128.871604,49.492506],[128.792147,49.473065],[128.76135,49.482009],[128.763198,49.515824],[128.813089,49.558157],[128.802618,49.58222],[128.744104,49.595023],[128.715155,49.564756],[128.656025,49.577564],[128.619684,49.593471],[128.537764,49.604332],[128.500192,49.593859],[128.389939,49.58998],[128.343128,49.544956],[128.287077,49.566309],[128.243345,49.563203],[128.185447,49.53952],[128.122005,49.55311],[128.070882,49.556604],[128.001281,49.592307],[127.949542,49.596187],[127.897804,49.579116],[127.815268,49.593859],[127.782007,49.630698],[127.705015,49.665185],[127.677913,49.697712],[127.674833,49.764247],[127.653892,49.780094],[127.583059,49.786277],[127.531936,49.826059],[127.529472,49.864265],[127.547334,49.928645],[127.543638,49.944438],[127.495595,49.994479],[127.501755,50.056764],[127.58737,50.137768],[127.60708,50.178794],[127.603385,50.239309],[127.44632,50.270686],[127.371791,50.29669],[127.332371,50.340634],[127.369944,50.403996],[127.3644,50.438314],[127.30527,50.45432],[127.293567,50.46575],[127.323132,50.52552],[127.36132,50.547582],[127.370559,50.581415],[127.294799,50.663426],[127.28864,50.699451],[127.305886,50.733932],[127.295415,50.755139],[127.236285,50.781256],[127.143894,50.910111],[127.113713,50.93765],[127.052119,50.962911],[126.985597,51.029202],[126.922772,51.061937],[126.917844,51.138977],[126.899982,51.200518],[126.926467,51.246244],[126.976358,51.291551],[126.98375,51.318863],[126.970815,51.332327],[126.887047,51.321856],[126.877808,51.300906],[126.908605,51.283691],[126.92154,51.259729],[126.908605,51.246619],[126.863025,51.248492],[126.820526,51.281071],[126.813134,51.311756],[126.837156,51.345038],[126.904293,51.340552],[126.930163,51.359241],[126.908605,51.407423],[126.835308,51.413769],[126.791577,51.432428],[126.784185,51.448095],[126.812518,51.493948],[126.843931,51.521885],[126.837156,51.536033],[126.69549,51.57845],[126.67886,51.602246],[126.741069,51.642374],[126.723823,51.679126],[126.734294,51.711399],[126.724439,51.7266],[126.6727,51.73179],[126.658534,51.762544],[126.622809,51.777357],[126.580925,51.824728],[126.555056,51.874266],[126.510092,51.922274],[126.462665,51.948471],[126.468208,51.982395],[126.447882,52.009294],[126.450962,52.027709],[126.487918,52.041699],[126.514404,52.037282],[126.563679,52.119302],[126.556288,52.136203],[126.499005,52.16044],[126.457121,52.165212],[126.403535,52.185031],[126.34502,52.192002],[126.306832,52.205574],[126.312992,52.235271],[126.357955,52.264216],[126.401071,52.279597],[126.436795,52.277034],[126.4331,52.298632],[126.327774,52.310342],[126.320999,52.342163],[126.348716,52.357882],[126.353644,52.389304],[126.326542,52.424353],[126.268644,52.475051],[126.205202,52.466302],[126.192883,52.492181],[126.213209,52.525327],[126.147304,52.573],[126.066616,52.603905],[126.055529,52.582455],[126.030891,52.576273],[125.989008,52.603178],[125.968682,52.630429],[125.971145,52.654033],[125.995783,52.675085],[126.061688,52.673271],[126.072775,52.691048],[126.044442,52.739628],[126.112195,52.757016],[126.116507,52.768243],[126.052449,52.800095],[126.02042,52.795753],[125.985312,52.758465],[125.966834,52.759914],[125.937269,52.786705],[125.923718,52.815651],[125.855349,52.866259],[125.854117,52.891542],[125.827631,52.899123],[125.772197,52.89804],[125.751255,52.88143],[125.722306,52.880347],[125.678574,52.86084],[125.666871,52.869872],[125.665023,52.913561],[125.737088,52.943504],[125.742632,52.993964],[125.684118,53.00801],[125.643466,53.039686],[125.640386,53.06199],[125.613901,53.083564],[125.588647,53.081047],[125.530749,53.0512],[125.504263,53.061271],[125.503647,53.095424],[125.452524,53.107641],[125.343503,53.14463],[125.315786,53.144989],[125.252344,53.18051],[125.195062,53.198439],[125.142091,53.204175],[125.038613,53.202741],[124.970244,53.194137],[124.887708,53.164368],[124.909266,53.118059],[124.87231,53.099018],[124.832889,53.145347],[124.787926,53.140681],[124.734339,53.146783],[124.712165,53.162574],[124.720789,53.192344],[124.678905,53.207043],[124.590209,53.208476],[124.563108,53.201666],[124.496587,53.207759],[124.487348,53.217436],[124.435609,53.223886],[124.412203,53.248601],[124.375863,53.258984],[124.327819,53.331954],[124.239124,53.379817],[124.19416,53.37339],[124.125791,53.348033],[124.058038,53.404085],[124.01369,53.403371],[123.985973,53.434401],[123.865249,53.489627],[123.797495,53.489983],[123.746373,53.500308],[123.698329,53.498528],[123.668764,53.533756],[123.620721,53.550115],[123.58746,53.546915],[123.569598,53.505291],[123.53141,53.507071],[123.557895,53.531978],[123.546808,53.551537],[123.517243,53.558292],[123.490758,53.542648],[123.510468,53.509206],[123.499381,53.497816],[123.47228,53.509206],[123.454417,53.536602],[123.394055,53.538024],[123.309672,53.56078],[123.274563,53.563269],[123.231447,53.549404],[123.179092,53.509918],[123.137209,53.498172],[123.093477,53.508138],[123.052209,53.506715],[122.943804,53.483929],[122.894528,53.462914],[122.826775,53.457213],[122.763949,53.463626],[122.673406,53.459351],[122.608117,53.465408],[122.5379,53.453293],[122.496016,53.458638],[122.435038,53.444739],[122.37406,53.47467],[122.350038,53.505647],[122.266886,53.470039],[122.227466,53.461845],[122.161561,53.468614],[122.111054,53.426913],[122.077177,53.422277],[122.026054,53.428339],[121.875765,53.426556],[121.816019,53.41336],[121.754425,53.389454],[121.697758,53.392666],[121.589969,53.350891],[121.499426,53.337314],[121.504969,53.323018],[121.575802,53.29155],[121.615222,53.258984],[121.642324,53.262564],[121.679896,53.240722],[121.67928,53.199515],[121.660186,53.195213],[121.665114,53.170467],[121.722396,53.145706],[121.753193,53.147501],[121.784606,53.104408],[121.775367,53.089674],[121.817867,53.061631],[121.785838,53.018451],[121.715621,52.997926],[121.677432,52.948192],[121.66265,52.912478],[121.610295,52.892264],[121.604136,52.872401],[121.620766,52.853251],[121.591201,52.824693],[121.537614,52.801542],[121.511129,52.779104],[121.476636,52.772225],[121.455078,52.73528],[121.373158,52.683067],[121.309717,52.676173],[121.29247,52.651855],[121.237036,52.619167],[121.182217,52.59918],[121.225333,52.577364],[121.280151,52.586819],[121.323883,52.573727],[121.353448,52.534793],[121.411963,52.52205],[121.416274,52.499468],[121.474172,52.482706],[121.495114,52.484892],[121.519136,52.456821],[121.565331,52.460468],[121.590585,52.443326],[121.63986,52.44442],[121.678664,52.419973],[121.658338,52.3904],[121.715621,52.342894],[121.714389,52.318025],[121.769207,52.308147],[121.841272,52.282526],[121.901018,52.280695],[121.94783,52.298266],[121.976779,52.343626],[122.035909,52.377615],[122.040837,52.413038],[122.091344,52.427272],[122.080873,52.440407],[122.107358,52.452445],[122.142467,52.495096],[122.140003,52.510032],[122.168952,52.513674],[122.178191,52.48963],[122.207756,52.469218],[122.310618,52.475416],[122.326016,52.459374],[122.342031,52.414133],[122.367284,52.413768],[122.378987,52.395512],[122.419023,52.375057],[122.447356,52.394052],[122.484313,52.341432],[122.478153,52.29607],[122.560689,52.282526],[122.585943,52.266413],[122.67895,52.276667],[122.710979,52.256157],[122.76087,52.26678],[122.787355,52.252494],[122.766413,52.232705],[122.769493,52.179893],[122.73808,52.153464],[122.690653,52.140243],[122.629059,52.13657],[122.643841,52.111585],[122.625363,52.067459],[122.650616,52.058997],[122.664783,51.99861],[122.683877,51.974654],[122.726377,51.978709],[122.729457,51.919321],[122.706051,51.890151],[122.725761,51.87833],[122.732536,51.832495],[122.771957,51.779579],[122.749167,51.746613],[122.778732,51.698048],[122.816304,51.655371],[122.820616,51.633088],[122.85634,51.606707],[122.832935,51.581797],[122.874202,51.561339],[122.880362,51.537894],[122.858804,51.524864],[122.880362,51.511085],[122.854492,51.477551],[122.871123,51.455181],[122.900072,51.445112],[122.903768,51.415262],[122.946267,51.405183],[122.965977,51.386886],[122.965977,51.345786],[123.002934,51.31213],[123.069455,51.321108],[123.127969,51.297913],[123.231447,51.279199],[123.231447,51.268716],[123.294273,51.254111],[123.339853,51.27246],[123.376809,51.266844],[123.414381,51.278825],[123.440251,51.270963],[123.46304,51.286686],[123.582533,51.294545],[123.582533,51.306893],[123.661989,51.319237],[123.660141,51.342795],[123.711264,51.398089],[123.794416,51.361109],[123.842459,51.367462],[123.887423,51.320734],[123.926227,51.300532],[123.939777,51.313253],[123.994596,51.322604],[124.071588,51.320734],[124.090067,51.3413],[124.128255,51.347281],[124.192313,51.33943],[124.239124,51.344664],[124.271769,51.308389],[124.297638,51.298661],[124.339522,51.293422],[124.406659,51.272086],[124.430065,51.301281],[124.426985,51.331953],[124.443616,51.35812],[124.478108,51.36223],[124.490427,51.380537],[124.555717,51.375307],[124.58713,51.363725],[124.62655,51.327465],[124.693687,51.3327],[124.752817,51.35812],[124.76452,51.38726],[124.783614,51.392115],[124.864302,51.37979],[124.885244,51.40817],[124.942527,51.447349],[124.917889,51.474196],[124.928976,51.498419],[124.983795,51.508478],[125.004737,51.529332],[125.047236,51.529704],[125.073106,51.553526],[125.060171,51.59667],[125.098975,51.658341],[125.12854,51.659083],[125.130388,51.635317],[125.175968,51.639403],[125.214772,51.627888],[125.228938,51.640517],[125.289301,51.633831],[125.316402,51.610052],[125.35151,51.623801],[125.38046,51.585516],[125.424807,51.562827],[125.528285,51.488359],[125.559082,51.461521],[125.559082,51.461521],[125.595422,51.416755],[125.595422,51.416755],[125.60035,51.413396],[125.60035,51.413396],[125.600966,51.410409],[125.600966,51.410409],[125.62314,51.398089],[125.62314,51.398089],[125.623756,51.387633],[125.623756,51.387633],[125.626219,51.380163],[125.626219,51.380163],[125.700132,51.327465],[125.700132,51.327465],[125.740784,51.27583],[125.740784,51.27583],[125.76111,51.261976],[125.76111,51.261976],[125.761726,51.226385],[125.819008,51.227134],[125.850421,51.21364],[125.864588,51.146487],[125.909551,51.138977],[125.946508,51.108176],[125.970529,51.123955],[125.993935,51.119072],[125.976073,51.084498],[126.059225,51.043503],[126.033971,51.011132],[126.041978,50.981753],[126.068464,50.967434],[126.042594,50.92558],[126.02042,50.927466],[125.996399,50.906715],[125.997631,50.872738],[125.961906,50.901054],[125.939732,50.85423],[125.913247,50.825885],[125.878138,50.816812],[125.890457,50.805845],[125.836255,50.793363],[125.846726,50.769524],[125.828863,50.756654],[125.804226,50.773309],[125.758646,50.746809],[125.795603,50.738856],[125.78082,50.725598],[125.825784,50.70362],[125.789443,50.679735],[125.804226,50.658874],[125.793139,50.643316],[125.814697,50.62092],[125.807921,50.60383],[125.829479,50.56165],[125.794987,50.532748],[125.770349,50.531227],[125.754335,50.506874],[125.740784,50.523237],[125.699516,50.487078],[125.654553,50.471082],[125.627451,50.443268],[125.580024,50.449366],[125.562162,50.438314],[125.583104,50.409717],[125.567089,50.402852],[125.536292,50.420014],[125.522126,50.404759],[125.546763,50.358965],[125.520278,50.3498],[125.530749,50.331085],[125.463611,50.295925],[125.466075,50.266861],[125.442053,50.260357],[125.448829,50.216338],[125.417416,50.195654],[125.39093,50.199868],[125.382923,50.172278],[125.335496,50.161161],[125.376148,50.137385],[125.311474,50.140453],[125.27883,50.127411],[125.258504,50.103618],[125.287453,50.093636],[125.283757,50.070211],[125.328105,50.065985],[125.315786,50.04562],[125.289916,50.057917],[125.25296,50.041393],[125.283757,50.036012],[125.297924,50.014481],[125.278214,49.996402],[125.241873,49.987938],[125.231402,49.957531],[125.190134,49.959841],[125.199373,49.935194],[125.225859,49.922481],[125.212924,49.907452],[125.245569,49.87198],[125.225243,49.867351],[125.239409,49.844587],[125.177815,49.829533],[125.222779,49.799026],[125.221547,49.754969],[125.204301,49.734086],[125.225243,49.726349],[125.219699,49.669058],[125.185207,49.634574],[125.189518,49.652401],[125.164881,49.669446],[125.132236,49.672157],[125.127308,49.655113],[125.15441,49.616741],[125.16796,49.629923],[125.205533,49.593859],[125.23017,49.595411],[125.233866,49.536801],[125.211076,49.539908],[125.228323,49.487063],[125.270822,49.454395],[125.256656,49.437275],[125.25604,49.395227],[125.277598,49.379644],[125.256656,49.359769],[125.261583,49.322336],[125.214772,49.277066],[125.227707,49.248947],[125.219699,49.189139],[125.187671,49.186792],[125.158721,49.144921],[125.117453,49.126127],[125.034302,49.157056],[125.039845,49.17623],[124.983179,49.162535],[124.906802,49.184054],[124.860607,49.166448],[124.847672,49.129651],[124.809484,49.115943],[124.828578,49.077933],[124.808252,49.020666],[124.756513,48.967262],[124.744194,48.920487],[124.709086,48.920487],[124.715861,48.885475],[124.697383,48.841775],[124.654267,48.83429],[124.644412,48.80789],[124.656115,48.783842],[124.612383,48.747945],[124.624702,48.701755],[124.601912,48.632587],[124.579122,48.596582],[124.520608,48.556195],[124.548941,48.535593],[124.533543,48.515379],[124.555717,48.467784],[124.507674,48.445558],[124.52492,48.426897],[124.51876,48.378027],[124.547094,48.35775],[124.540934,48.335476],[124.579738,48.297269],[124.558796,48.268197],[124.579122,48.262221],[124.547094,48.200829],[124.512601,48.164518],[124.529847,48.146951],[124.505826,48.124985],[124.478108,48.123387],[124.46579,48.098213],[124.415899,48.08782],[124.430065,48.12099],[124.471333,48.133373],[124.475029,48.173698],[124.418978,48.181679],[124.412819,48.219175],[124.422058,48.245884],[124.365392,48.283731],[124.353689,48.315978],[124.317964,48.35099],[124.331515,48.380015],[124.309957,48.413393],[124.330283,48.435633],[124.302566,48.456673],[124.314269,48.503881],[124.25945,48.536385],[124.25945,48.536385],[124.136878,48.463023],[124.07898,48.43603],[124.019234,48.39313],[123.862785,48.271782],[123.746373,48.197638],[123.705105,48.152142],[123.579453,48.045427],[123.537569,48.021816],[123.300432,47.953723],[123.256085,47.876711],[123.214201,47.824502],[123.161846,47.781892],[123.041122,47.746492],[122.926557,47.697777],[122.848949,47.67441],[122.765181,47.614333],[122.59395,47.54732],[122.543443,47.495589],[122.507103,47.401291],[122.418407,47.350632],[122.441197,47.310476],[122.441197,47.310476],[122.462755,47.27841],[122.498479,47.255262],[122.531124,47.198771],[122.582863,47.158092],[122.582863,47.158092],[122.615508,47.124306],[122.679566,47.094164],[122.710363,47.093349],[122.710363,47.093349],[122.821232,47.065636],[122.852645,47.072158],[122.845869,47.046881],[122.778116,47.002822],[122.77442,46.973837],[122.798442,46.9575],[122.791051,46.941567],[122.83971,46.937072],[122.895144,46.960359],[122.893913,46.895376],[122.906847,46.80738],[122.996774,46.761483],[123.00355,46.730726],[123.026339,46.718829],[123.076846,46.745082],[123.103332,46.734828],[123.163694,46.74016],[123.198802,46.803283],[123.22344,46.821305],[123.221592,46.850373],[123.295505,46.865105],[123.341084,46.826628],[123.374345,46.837683],[123.40699,46.906416],[123.404526,46.935438],[123.360179,46.970978],[123.304128,46.964852],[123.301664,46.999965],[123.337389,46.988943],[123.42362,46.934212],[123.487678,46.959951],[123.52833,46.944836],[123.483366,46.84587],[123.506772,46.827038],[123.562823,46.82581],[123.575757,46.845461],[123.576989,46.891286],[123.605322,46.891286],[123.599163,46.868378],[123.625648,46.847508],[123.580069,46.827447],[123.629344,46.813524],[123.631808,46.728675],[123.603475,46.68928],[123.474743,46.686817],[123.366338,46.677784],[123.318295,46.662179],[123.276411,46.660947],[123.279491,46.616981],[123.228368,46.588198],[123.18094,46.614103],[123.098404,46.603002],[123.077462,46.622324],[123.04605,46.617803],[123.052825,46.579972],[123.002318,46.574624],[123.010325,46.524823],[123.011557,46.434984],[123.089781,46.347888],[123.142136,46.298293],[123.178476,46.248239],[123.248078,46.273065],[123.286266,46.250308],[123.320758,46.254447],[123.357099,46.232096],[123.357099,46.232096],[123.430396,46.243687],[123.452569,46.233338],[123.499381,46.259826],[123.569598,46.223816],[123.569598,46.223816]]]]}},
    {"type":"Feature","properties":{"adcode":310000,"name":"上海市","center":[121.472644,31.231706],"centroid":[121.438737,31.072559],"childrenNum":16,"level":"province","parent":{"adcode":100000},"subFeatureIndex":8,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[120.901349,31.017327],[120.940153,31.010146],[120.949392,31.030148],[120.989428,31.01425],[121.000515,30.938309],[120.993124,30.889532],[121.020225,30.872069],[120.991892,30.837133],[121.038087,30.814007],[121.060261,30.845354],[121.097833,30.857171],[121.13787,30.826342],[121.123087,30.77905],[121.174826,30.771851],[121.21671,30.785734],[121.232108,30.755909],[121.272144,30.723504],[121.274608,30.677191],[121.362071,30.679764],[121.426129,30.730192],[121.517288,30.775451],[121.601056,30.805269],[121.681128,30.818633],[121.904714,30.814007],[121.943518,30.776993],[121.970004,30.789333],[121.954605,30.825828],[121.994025,30.862823],[121.990945,30.96859],[121.977395,31.016301],[121.946598,31.066039],[121.809859,31.196669],[121.722396,31.3036],[121.599208,31.37465],[121.520984,31.394575],[121.404571,31.479337],[121.343593,31.511996],[121.301093,31.49873],[121.301093,31.49873],[121.247507,31.476785],[121.241963,31.493117],[121.174826,31.44922],[121.143413,31.392021],[121.113848,31.37465],[121.130478,31.343987],[121.142797,31.275472],[121.090442,31.291838],[121.060261,31.245289],[121.076892,31.158267],[121.018377,31.134194],[120.930298,31.141365],[120.881023,31.134706],[120.859465,31.100379],[120.890878,31.094229],[120.901349,31.017327]]],[[[121.974931,31.61704],[121.715005,31.673592],[121.64294,31.697527],[121.599824,31.703128],[121.49881,31.753012],[121.431673,31.769295],[121.384861,31.833382],[121.323267,31.868458],[121.265369,31.863883],[121.200079,31.834907],[121.118775,31.759119],[121.145261,31.75403],[121.289391,31.61653],[121.371926,31.553314],[121.395332,31.585437],[121.434136,31.590535],[121.547469,31.531382],[121.625693,31.501792],[121.682976,31.491075],[121.819098,31.437987],[121.890547,31.428795],[121.981706,31.464024],[121.995873,31.493117],[121.974931,31.61704]]],[[[121.795693,31.330186],[121.792613,31.363408],[121.742106,31.407345],[121.585657,31.454836],[121.567179,31.48342],[121.520984,31.494137],[121.509897,31.4824],[121.572107,31.435944],[121.727939,31.35472],[121.76428,31.31536],[121.785222,31.31127],[121.795693,31.330186]]],[[[121.801852,31.356765],[121.8037,31.328652],[121.840656,31.295418],[121.932431,31.283144],[122.016199,31.282121],[122.097503,31.255522],[122.122756,31.307179],[122.116597,31.320984],[122.040837,31.324051],[121.951525,31.337343],[121.845584,31.37465],[121.792613,31.377715],[121.801852,31.356765]]],[[[121.626925,31.445135],[121.631853,31.456878],[121.579498,31.479848],[121.626925,31.445135]]],[[[121.943518,31.215608],[121.959533,31.159291],[121.995873,31.160828],[122.008808,31.221238],[121.950909,31.228915],[121.943518,31.215608]]],[[[121.88254,31.240684],[121.909026,31.195133],[121.923808,31.234032],[121.88254,31.240684]]]]}},
    {"type":"Feature","properties":{"adcode":320000,"name":"江苏省","center":[118.767413,32.041544],"centroid":[119.486506,32.983991],"childrenNum":13,"level":"province","parent":{"adcode":100000},"subFeatureIndex":9,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[117.311654,34.561686],[117.27285,34.556757],[117.303647,34.542463],[117.267923,34.532603],[117.27285,34.499565],[117.252524,34.48674],[117.248213,34.451216],[117.166293,34.434435],[117.139191,34.526687],[117.15151,34.559222],[117.104083,34.648874],[117.073286,34.639026],[117.061583,34.675947],[117.070206,34.713835],[117.022163,34.759081],[116.969192,34.771864],[116.95133,34.81069],[116.979047,34.815113],[116.966113,34.844588],[116.929156,34.843114],[116.922381,34.894671],[116.858323,34.928533],[116.821983,34.929515],[116.815823,34.965324],[116.789338,34.975133],[116.781331,34.916757],[116.677853,34.939327],[116.622418,34.939818],[116.613795,34.922645],[116.557745,34.908905],[116.445028,34.895652],[116.408071,34.850972],[116.403144,34.756131],[116.369267,34.749247],[116.363724,34.715311],[116.392057,34.710391],[116.374195,34.640011],[116.430245,34.650843],[116.432709,34.630163],[116.477057,34.614896],[116.490607,34.573513],[116.594085,34.511894],[116.592237,34.493646],[116.662454,34.472927],[116.722816,34.472434],[116.773939,34.453683],[116.782563,34.429993],[116.828142,34.389012],[116.909446,34.408271],[116.969192,34.389012],[116.960569,34.363821],[116.983359,34.348011],[116.969192,34.283753],[117.051112,34.221425],[117.025243,34.167469],[117.046801,34.151622],[117.123793,34.128342],[117.130568,34.101586],[117.192162,34.068873],[117.257452,34.065899],[117.277162,34.078787],[117.311654,34.067882],[117.357234,34.088205],[117.404045,34.03218],[117.435458,34.028212],[117.514914,34.060941],[117.543248,34.038627],[117.569117,33.985051],[117.612849,34.000433],[117.629479,34.028708],[117.671363,33.992494],[117.672595,33.934916],[117.715095,33.879287],[117.753899,33.891211],[117.759442,33.874318],[117.739732,33.758467],[117.72495,33.74951],[117.750203,33.710688],[117.791471,33.733585],[117.843826,33.736074],[117.901724,33.720146],[117.972557,33.74951],[118.019985,33.738562],[118.065564,33.76593],[118.117919,33.766427],[118.161035,33.735576],[118.16781,33.663381],[118.112376,33.617045],[118.117919,33.594615],[118.107448,33.475391],[118.050782,33.491863],[118.027376,33.455421],[118.016905,33.402978],[118.029224,33.374995],[117.992883,33.333005],[117.974405,33.279487],[117.939297,33.262475],[117.942376,33.224936],[117.977485,33.226437],[117.988572,33.180869],[118.037231,33.152314],[118.038463,33.134776],[118.149332,33.169348],[118.178281,33.217926],[118.217085,33.191888],[118.219549,33.114227],[118.243571,33.027967],[118.244803,32.998359],[118.26944,32.969242],[118.303933,32.96874],[118.291614,32.946143],[118.252194,32.936601],[118.2331,32.914498],[118.250346,32.848157],[118.301469,32.846145],[118.300237,32.783275],[118.334114,32.761637],[118.363063,32.770695],[118.375382,32.718849],[118.411106,32.715828],[118.450526,32.743518],[118.483787,32.721367],[118.560163,32.729926],[118.572482,32.719856],[118.642699,32.744525],[118.707373,32.72036],[118.756648,32.737477],[118.73817,32.772708],[118.743097,32.853184],[118.743097,32.853184],[118.810235,32.853687],[118.821322,32.920527],[118.846575,32.922034],[118.849039,32.956689],[118.89585,32.957694],[118.89585,32.957694],[118.892771,32.941121],[118.934039,32.93861],[118.993169,32.958196],[119.020886,32.955685],[119.054763,32.8748],[119.113277,32.823014],[119.184726,32.825529],[119.211827,32.708275],[119.208748,32.641276],[119.230921,32.607001],[119.22045,32.576748],[119.152697,32.557582],[119.168096,32.536394],[119.142226,32.499556],[119.084944,32.452602],[119.041212,32.515201],[118.975923,32.505108],[118.922336,32.557078],[118.92172,32.557078],[118.922336,32.557078],[118.92172,32.557078],[118.890923,32.553042],[118.908169,32.59238],[118.84288,32.56767],[118.820706,32.60448],[118.784981,32.582295],[118.757264,32.603976],[118.73509,32.58885],[118.719076,32.614059],[118.719076,32.614059],[118.688895,32.588346],[118.658714,32.594397],[118.632844,32.578261],[118.59712,32.600951],[118.568787,32.585825],[118.564475,32.562122],[118.608823,32.536899],[118.592192,32.481383],[118.628533,32.467751],[118.691359,32.472295],[118.685199,32.403604],[118.703061,32.328792],[118.657482,32.30148],[118.674728,32.250375],[118.643931,32.209875],[118.510888,32.194176],[118.49549,32.165304],[118.501033,32.121726],[118.433896,32.086746],[118.394476,32.076098],[118.389548,31.985281],[118.363679,31.930443],[118.472084,31.879639],[118.466541,31.857784],[118.504729,31.841516],[118.481939,31.778453],[118.533678,31.76726],[118.521975,31.743343],[118.5577,31.73011],[118.571866,31.746397],[118.641467,31.75861],[118.653786,31.73011],[118.697518,31.709747],[118.643315,31.671555],[118.643315,31.649651],[118.736322,31.633347],[118.748025,31.675629],[118.773894,31.682759],[118.802844,31.619078],[118.858894,31.623665],[118.881684,31.564023],[118.885995,31.519139],[118.883532,31.500261],[118.852119,31.393553],[118.824401,31.375672],[118.767735,31.363919],[118.745561,31.372606],[118.720924,31.322518],[118.726467,31.282121],[118.756648,31.279564],[118.794836,31.229426],[118.870597,31.242219],[118.984546,31.237102],[119.014727,31.241707],[119.10527,31.235055],[119.107118,31.250917],[119.158241,31.294907],[119.197661,31.295418],[119.198277,31.270357],[119.266646,31.250405],[119.294363,31.263195],[119.338095,31.259103],[119.350414,31.301043],[119.374435,31.258591],[119.360269,31.213049],[119.391682,31.174142],[119.439109,31.177214],[119.461283,31.156219],[119.532732,31.159291],[119.599869,31.10909],[119.623891,31.130096],[119.678093,31.167997],[119.705811,31.152634],[119.715666,31.169533],[119.779723,31.17875],[119.809904,31.148536],[119.827151,31.174142],[119.878274,31.160828],[119.921389,31.170045],[119.946027,31.106016],[119.988527,31.059375],[120.001461,31.027071],[120.052584,31.00553],[120.111099,30.955761],[120.149903,30.937283],[120.223816,30.926502],[120.316206,30.933689],[120.371025,30.948575],[120.35809,30.886964],[120.42338,30.902884],[120.435083,30.920855],[120.441858,30.860768],[120.460336,30.839702],[120.489285,30.763624],[120.504684,30.757967],[120.563814,30.835592],[120.589684,30.854089],[120.654973,30.846896],[120.68269,30.882342],[120.713487,30.88491],[120.709176,30.933176],[120.684538,30.955247],[120.698089,30.970643],[120.746132,30.962432],[120.770154,30.996809],[120.820661,31.006556],[120.865624,30.989627],[120.901349,31.017327],[120.890878,31.094229],[120.859465,31.100379],[120.881023,31.134706],[120.930298,31.141365],[121.018377,31.134194],[121.076892,31.158267],[121.060261,31.245289],[121.090442,31.291838],[121.142797,31.275472],[121.130478,31.343987],[121.113848,31.37465],[121.143413,31.392021],[121.174826,31.44922],[121.241963,31.493117],[121.247507,31.476785],[121.301093,31.49873],[121.301093,31.49873],[121.343593,31.511996],[121.371926,31.553314],[121.289391,31.61653],[121.145261,31.75403],[121.118775,31.759119],[121.200079,31.834907],[121.265369,31.863883],[121.323267,31.868458],[121.384861,31.833382],[121.431673,31.769295],[121.49881,31.753012],[121.599824,31.703128],[121.64294,31.697527],[121.715005,31.673592],[121.974931,31.61704],[121.970004,31.718911],[121.889315,31.866425],[121.856055,31.955328],[121.772287,32.032984],[121.759352,32.059362],[121.525295,32.136423],[121.542542,32.152132],[121.458774,32.177462],[121.499426,32.211394],[121.493882,32.263533],[121.450151,32.282256],[121.425513,32.430885],[121.390405,32.460682],[121.352216,32.474315],[121.269681,32.483402],[121.153268,32.52933],[121.121855,32.569183],[121.076892,32.576243],[121.020225,32.605489],[120.961711,32.612042],[120.979573,32.636236],[120.963559,32.68259],[120.916131,32.701225],[120.953088,32.714318],[120.972182,32.761134],[120.981421,32.85972],[120.957399,32.893395],[120.932762,33.005887],[120.917979,33.02596],[120.871784,33.047032],[120.874247,33.093672],[120.843451,33.209915],[120.819429,33.237951],[120.833595,33.274984],[120.813885,33.303499],[120.769538,33.307],[120.741205,33.337505],[120.717183,33.436945],[120.680227,33.520306],[120.622944,33.615051],[120.611241,33.627012],[120.583524,33.668362],[120.534249,33.782346],[120.48559,33.859411],[120.367329,34.091674],[120.347619,34.179352],[120.314359,34.255563],[120.311895,34.306991],[120.103707,34.391481],[119.962657,34.459112],[119.811752,34.485754],[119.781571,34.515839],[119.641137,34.569078],[119.610956,34.592729],[119.569072,34.615389],[119.465594,34.672994],[119.525956,34.73351],[119.456971,34.748264],[119.381827,34.752198],[119.494543,34.754656],[119.497007,34.754164],[119.439725,34.785136],[119.440957,34.769406],[119.378747,34.764489],[119.312841,34.774813],[119.272189,34.797914],[119.238313,34.799388],[119.217371,34.827886],[119.202588,34.890253],[119.214907,34.925589],[119.211211,34.981507],[119.238313,35.048657],[119.285124,35.068252],[119.291899,35.028567],[119.307298,35.032977],[119.292515,35.068742],[119.306066,35.076578],[119.286972,35.115261],[119.250016,35.124562],[119.217371,35.106939],[119.137915,35.096167],[119.114509,35.055026],[119.027045,35.055516],[118.942662,35.040817],[118.928495,35.051106],[118.86259,35.025626],[118.860742,34.944233],[118.805307,34.87307],[118.80038,34.843114],[118.772047,34.794474],[118.739402,34.792508],[118.719076,34.745313],[118.764039,34.740396],[118.783749,34.723181],[118.739402,34.693663],[118.690127,34.678408],[118.664257,34.693663],[118.607591,34.694155],[118.601431,34.714327],[118.545997,34.705964],[118.460997,34.656258],[118.473932,34.623269],[118.439439,34.626223],[118.424657,34.595193],[118.439439,34.507949],[118.416034,34.473914],[118.404947,34.427525],[118.379693,34.415183],[118.290382,34.424563],[118.277447,34.404814],[118.220165,34.405802],[118.217701,34.379134],[118.179513,34.379628],[118.177665,34.45319],[118.132702,34.483287],[118.16473,34.50499],[118.185056,34.543942],[118.079115,34.569571],[118.114839,34.614404],[118.084042,34.655766],[118.053861,34.650843],[117.951615,34.678408],[117.909732,34.670533],[117.902956,34.644443],[117.793935,34.651827],[117.791471,34.583368],[117.801942,34.518798],[117.684298,34.547392],[117.659044,34.501044],[117.609769,34.490686],[117.592523,34.462566],[117.53832,34.467006],[117.465023,34.484767],[117.402813,34.550843],[117.402813,34.569571],[117.370785,34.584846],[117.325205,34.573021],[117.325205,34.573021],[117.32151,34.566614],[117.32151,34.566614],[117.311654,34.561686],[117.311654,34.561686]]]]}},
    {"type":"Feature","properties":{"adcode":330000,"name":"浙江省","center":[120.153576,30.287459],"centroid":[120.109913,29.181466],"childrenNum":11,"level":"province","parent":{"adcode":100000},"subFeatureIndex":10,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[118.433896,28.288335],[118.444367,28.253548],[118.490562,28.238259],[118.493026,28.262509],[118.588497,28.282538],[118.595272,28.258292],[118.651322,28.277267],[118.674728,28.27147],[118.699366,28.309939],[118.719692,28.312047],[118.756032,28.252493],[118.802228,28.240368],[118.804075,28.207675],[118.771431,28.188687],[118.805923,28.154923],[118.802228,28.117453],[118.767735,28.10584],[118.719076,28.063601],[118.733858,28.027684],[118.730163,27.970615],[118.753568,27.947885],[118.818242,27.916689],[118.829329,27.847921],[118.873677,27.733563],[118.879836,27.667859],[118.913713,27.619616],[118.909401,27.568168],[118.869365,27.540047],[118.907553,27.460952],[118.955597,27.4498],[118.986393,27.47582],[118.983314,27.498649],[119.020886,27.498118],[119.03998,27.478475],[119.092335,27.466262],[119.129907,27.475289],[119.121284,27.438115],[119.14777,27.424836],[119.224146,27.416868],[119.26911,27.42218],[119.285124,27.457766],[119.334399,27.480067],[119.360269,27.524657],[119.416935,27.539517],[119.438493,27.508734],[119.466826,27.526249],[119.501935,27.610601],[119.501319,27.649837],[119.541971,27.666799],[119.606028,27.674749],[119.644217,27.663619],[119.626354,27.620676],[119.630666,27.582491],[119.675014,27.574534],[119.659615,27.540578],[119.690412,27.537394],[119.70889,27.514042],[119.703347,27.446613],[119.685485,27.438646],[119.711354,27.403054],[119.750774,27.373829],[119.739687,27.362668],[119.782187,27.330241],[119.768636,27.307909],[119.843165,27.300464],[119.938636,27.329709],[119.960194,27.365857],[120.008237,27.375423],[120.026099,27.344063],[120.052584,27.338747],[120.096316,27.390302],[120.136968,27.402523],[120.134504,27.420055],[120.221352,27.420055],[120.26262,27.432804],[120.273091,27.38924],[120.340844,27.399867],[120.343924,27.363199],[120.430155,27.258976],[120.401822,27.250996],[120.404286,27.204166],[120.461568,27.142407],[120.492365,27.136016],[120.545952,27.156785],[120.574901,27.234501],[120.554575,27.25206],[120.580444,27.321203],[120.665444,27.357884],[120.673451,27.420055],[120.703016,27.478475],[120.637111,27.561271],[120.634647,27.577186],[120.685154,27.622797],[120.709176,27.682699],[120.771386,27.734623],[120.777545,27.774873],[120.809574,27.775402],[120.840371,27.758986],[120.910588,27.864852],[120.942001,27.896592],[120.97403,27.887071],[121.027616,27.832574],[121.070116,27.834162],[121.107688,27.81352],[121.152036,27.815638],[121.134174,27.787051],[121.13479,27.787051],[121.149572,27.801345],[121.149572,27.801875],[121.153268,27.809815],[121.152652,27.810344],[121.192072,27.822518],[121.193304,27.872259],[121.162507,27.879136],[121.162507,27.90717],[121.099681,27.895005],[121.05595,27.900294],[120.991892,27.95],[121.015298,27.981714],[121.059029,28.096338],[121.108304,28.139092],[121.121239,28.12537],[121.140949,28.031382],[121.176058,28.022401],[121.261057,28.034551],[121.299862,28.067297],[121.328195,28.134343],[121.373774,28.133287],[121.402107,28.197127],[121.45631,28.250385],[121.488955,28.301509],[121.538846,28.299401],[121.571491,28.279376],[121.580114,28.240368],[121.627541,28.251966],[121.669425,28.33312],[121.660186,28.355768],[121.634317,28.347868],[121.658954,28.392628],[121.692831,28.407368],[121.671273,28.472621],[121.646019,28.511544],[121.634317,28.562542],[121.596128,28.575156],[121.557324,28.645033],[121.540694,28.655537],[121.646019,28.682842],[121.689135,28.719062],[121.704534,28.804577],[121.687287,28.863294],[121.774751,28.863818],[121.772287,28.898404],[121.743338,28.954451],[121.711309,28.985865],[121.712541,29.028783],[121.658954,29.058606],[121.660186,29.118226],[121.616454,29.143318],[121.608447,29.168927],[121.715621,29.125022],[121.750113,29.136523],[121.767975,29.166837],[121.780294,29.10986],[121.811091,29.10986],[121.85975,29.086328],[121.884388,29.105677],[121.966308,29.052852],[121.970004,29.092604],[121.988482,29.110906],[121.986634,29.154817],[121.948446,29.193485],[121.971851,29.193485],[121.966924,29.249894],[122.002032,29.260336],[122.000185,29.278608],[121.94475,29.28435],[121.958301,29.334448],[121.936127,29.348012],[121.937975,29.384],[121.975547,29.411113],[121.993409,29.45229],[121.973083,29.477821],[121.968772,29.515846],[121.995257,29.545007],[122.000185,29.582486],[121.966308,29.636078],[121.909641,29.650122],[121.872685,29.632437],[121.833265,29.653242],[121.937359,29.748373],[122.003264,29.762401],[122.043916,29.822647],[122.10243,29.859504],[122.143082,29.877668],[122.140003,29.901535],[122.00696,29.891678],[122.00388,29.92021],[121.971235,29.955476],[121.919497,29.920729],[121.835113,29.958068],[121.78399,29.99332],[121.721164,29.992802],[121.699606,30.007832],[121.652795,30.071037],[121.635548,30.070002],[121.561636,30.184395],[121.497578,30.258861],[121.395332,30.338435],[121.371926,30.37097],[121.328195,30.397299],[121.225333,30.404526],[121.183449,30.434458],[121.092906,30.515952],[121.058413,30.563888],[121.148956,30.599953],[121.188992,30.632916],[121.239499,30.648878],[121.274608,30.677191],[121.272144,30.723504],[121.232108,30.755909],[121.21671,30.785734],[121.174826,30.771851],[121.123087,30.77905],[121.13787,30.826342],[121.097833,30.857171],[121.060261,30.845354],[121.038087,30.814007],[120.991892,30.837133],[121.020225,30.872069],[120.993124,30.889532],[121.000515,30.938309],[120.989428,31.01425],[120.949392,31.030148],[120.940153,31.010146],[120.901349,31.017327],[120.865624,30.989627],[120.820661,31.006556],[120.770154,30.996809],[120.746132,30.962432],[120.698089,30.970643],[120.684538,30.955247],[120.709176,30.933176],[120.713487,30.88491],[120.68269,30.882342],[120.654973,30.846896],[120.589684,30.854089],[120.563814,30.835592],[120.504684,30.757967],[120.489285,30.763624],[120.460336,30.839702],[120.441858,30.860768],[120.435083,30.920855],[120.42338,30.902884],[120.35809,30.886964],[120.371025,30.948575],[120.316206,30.933689],[120.223816,30.926502],[120.149903,30.937283],[120.111099,30.955761],[120.052584,31.00553],[120.001461,31.027071],[119.988527,31.059375],[119.946027,31.106016],[119.921389,31.170045],[119.878274,31.160828],[119.827151,31.174142],[119.809904,31.148536],[119.779723,31.17875],[119.715666,31.169533],[119.705811,31.152634],[119.678093,31.167997],[119.623891,31.130096],[119.649144,31.104991],[119.629434,31.085517],[119.633746,31.019379],[119.580159,30.967051],[119.582007,30.932149],[119.563529,30.919315],[119.557369,30.874124],[119.575847,30.829939],[119.55429,30.825828],[119.527188,30.77905],[119.479761,30.772365],[119.482841,30.704467],[119.444652,30.650422],[119.408312,30.645274],[119.39045,30.685941],[119.343022,30.664322],[119.323312,30.630341],[119.238929,30.609225],[119.265414,30.574709],[119.237081,30.546881],[119.272189,30.510281],[119.326392,30.532964],[119.336247,30.508734],[119.335015,30.448389],[119.36766,30.38491],[119.402768,30.374584],[119.349182,30.349281],[119.326392,30.372002],[119.277117,30.341018],[119.246936,30.341018],[119.236465,30.297106],[119.201356,30.290905],[119.126828,30.304856],[119.091719,30.323972],[119.06277,30.304856],[118.988857,30.332237],[118.954365,30.360126],[118.880452,30.31519],[118.877988,30.282637],[118.905089,30.216464],[118.929727,30.2025],[118.852735,30.166805],[118.852119,30.149729],[118.895234,30.148694],[118.873677,30.11505],[118.878604,30.064822],[118.902626,30.029078],[118.894619,29.937845],[118.838568,29.934733],[118.841032,29.891159],[118.740634,29.814859],[118.744945,29.73902],[118.700598,29.706277],[118.647011,29.64336],[118.61991,29.654282],[118.573714,29.638159],[118.532446,29.588731],[118.500417,29.57572],[118.496106,29.519492],[118.381541,29.504909],[118.347664,29.474174],[118.329802,29.495012],[118.306396,29.479384],[118.316252,29.422581],[118.248498,29.431443],[118.193064,29.395472],[118.205382,29.343839],[118.166578,29.314099],[118.178281,29.297921],[118.138861,29.283828],[118.077883,29.290614],[118.073571,29.216993],[118.042159,29.210202],[118.027992,29.167882],[118.045238,29.149068],[118.037847,29.102017],[118.076035,29.074822],[118.066796,29.053898],[118.097593,28.998952],[118.115455,29.009944],[118.115455,29.009944],[118.133933,28.983771],[118.165346,28.986912],[118.227556,28.942406],[118.195527,28.904167],[118.270056,28.918836],[118.300237,28.826075],[118.364295,28.813491],[118.403099,28.702791],[118.428352,28.681267],[118.428352,28.617193],[118.428352,28.617193],[118.412338,28.55676],[118.4302,28.515225],[118.414802,28.497344],[118.474548,28.478934],[118.456686,28.424738],[118.432048,28.402104],[118.455454,28.384204],[118.480091,28.327325],[118.433896,28.288335]]],[[[122.163408,29.988137],[122.239785,29.962735],[122.279205,29.937326],[122.322321,29.940438],[122.341415,29.976733],[122.343879,30.020269],[122.310002,30.039958],[122.290908,30.074663],[122.301379,30.086574],[122.293988,30.100554],[122.152938,30.113497],[122.095655,30.158008],[122.048844,30.147141],[121.955221,30.183878],[121.934895,30.161631],[121.983554,30.100554],[121.989714,30.077252],[121.978011,30.059125],[122.027902,29.991247],[122.106742,30.005759],[122.118445,29.986582],[122.163408,29.988137]]],[[[122.213915,30.186464],[122.178807,30.199396],[122.152938,30.19112],[122.143698,30.163183],[122.168336,30.138343],[122.213915,30.186464]]],[[[122.229314,29.711995],[122.210836,29.700559],[122.269966,29.685482],[122.231162,29.710435],[122.229314,29.711995]]],[[[122.427646,30.738422],[122.427031,30.697777],[122.532972,30.696748],[122.528045,30.725047],[122.475074,30.714243],[122.445509,30.745109],[122.427646,30.738422]]],[[[122.162793,30.329654],[122.058083,30.291938],[122.154169,30.244903],[122.231778,30.234562],[122.247176,30.30124],[122.228082,30.329654],[122.191126,30.329654],[122.176343,30.351863],[122.162793,30.329654]]],[[[122.317393,30.249556],[122.277973,30.242835],[122.358661,30.236113],[122.365437,30.255242],[122.417175,30.238699],[122.40732,30.272817],[122.333408,30.272817],[122.317393,30.249556]]],[[[122.026054,29.178333],[122.013119,29.151681],[122.056851,29.158476],[122.075945,29.176243],[122.036525,29.20759],[122.026054,29.178333]]],[[[122.372212,29.893234],[122.386379,29.834069],[122.415944,29.828877],[122.401777,29.869884],[122.433806,29.883376],[122.43319,29.919173],[122.411632,29.951846],[122.398081,29.9394],[122.351886,29.959105],[122.330944,29.937845],[122.338951,29.911911],[122.353734,29.89946],[122.362973,29.894272],[122.372212,29.893234]]],[[[122.43011,30.408655],[122.432574,30.445294],[122.37406,30.461802],[122.277973,30.471603],[122.281669,30.418461],[122.318625,30.407106],[122.352502,30.422074],[122.43011,30.408655]]],[[[121.837577,28.770484],[121.86283,28.782024],[121.861598,28.814016],[121.837577,28.770484]]],[[[122.265038,29.84549],[122.221307,29.832512],[122.248408,29.804473],[122.310002,29.766557],[122.325401,29.781621],[122.299531,29.819532],[122.319241,29.829397],[122.265038,29.84549]]],[[[121.790765,29.082144],[121.832649,29.050236],[121.84312,29.082144],[121.82033,29.099402],[121.790765,29.082144]]],[[[121.201311,27.623328],[121.197616,27.618025],[121.198848,27.616964],[121.203775,27.625979],[121.201311,27.623328]]],[[[121.943518,30.776993],[121.968156,30.688514],[121.997105,30.658659],[122.087032,30.602014],[122.133227,30.595317],[122.075329,30.647848],[122.011271,30.66947],[121.992793,30.695204],[121.987866,30.753338],[121.970004,30.789333],[121.943518,30.776993]]],[[[121.889315,28.471569],[121.918881,28.497344],[121.881924,28.502603],[121.889315,28.471569]]],[[[122.182503,29.650642],[122.211452,29.692241],[122.200365,29.712515],[122.146778,29.749412],[122.13138,29.788893],[122.083952,29.78318],[122.047612,29.719791],[122.074097,29.701599],[122.095655,29.716673],[122.138155,29.662083],[122.182503,29.650642]]],[[[122.461523,29.944068],[122.459675,29.944586],[122.460291,29.947179],[122.451668,29.943031],[122.451052,29.940956],[122.450436,29.940956],[122.449204,29.9394],[122.4529,29.936807],[122.452284,29.935252],[122.45598,29.926435],[122.457827,29.927472],[122.462755,29.927991],[122.467067,29.928509],[122.459059,29.938882],[122.461523,29.944068]]],[[[122.570544,30.644244],[122.559457,30.679764],[122.546523,30.651967],[122.570544,30.644244]]],[[[121.869605,28.423685],[121.910873,28.44],[121.889931,28.45105],[121.869605,28.423685]]],[[[122.065474,30.179739],[122.055619,30.200431],[122.017431,30.186464],[122.025438,30.161631],[122.065474,30.179739]]],[[[122.391306,29.970512],[122.411632,30.025969],[122.378371,30.023896],[122.3679,29.980361],[122.391306,29.970512]]],[[[121.850511,29.977251],[121.874533,29.964809],[121.933047,29.994875],[121.924424,30.052391],[121.88562,30.094859],[121.848663,30.101072],[121.84004,30.047211],[121.844968,29.982953],[121.850511,29.977251]]],[[[121.066421,27.478475],[121.066421,27.461483],[121.107073,27.443958],[121.067036,27.478475],[121.066421,27.478475]]],[[[121.952141,29.187738],[121.979243,29.160043],[121.976779,29.191918],[121.952141,29.187738]]],[[[122.038373,29.759284],[122.011271,29.746294],[122.02975,29.716673],[122.038373,29.759284]]],[[[121.940438,30.114533],[121.910257,30.089163],[121.945982,30.064304],[121.962612,30.106249],[121.940438,30.114533]]],[[[121.957685,30.287804],[122.0008,30.308473],[121.989098,30.339985],[121.94167,30.33327],[121.921344,30.30744],[121.957685,30.287804]]],[[[122.192974,29.965327],[122.163408,29.988137],[122.152322,29.97103],[122.154169,29.97103],[122.155401,29.970512],[122.18435,29.955476],[122.192974,29.965327]]],[[[122.287828,29.723949],[122.301379,29.748373],[122.258263,29.753569],[122.241633,29.784738],[122.2133,29.771752],[122.251488,29.731225],[122.287828,29.723949]]],[[[121.134174,27.787051],[121.134174,27.785992],[121.13479,27.787051],[121.134174,27.787051]]],[[[122.760254,30.141966],[122.784275,30.130062],[122.781196,30.13265],[122.778116,30.13679],[122.770725,30.138861],[122.763333,30.141966],[122.762101,30.142484],[122.760254,30.141966]]],[[[122.264423,30.269716],[122.253952,30.237147],[122.315545,30.250073],[122.300147,30.271266],[122.264423,30.269716]]],[[[122.282901,29.860542],[122.30877,29.849642],[122.343263,29.860542],[122.343263,29.882857],[122.301379,29.883895],[122.282901,29.860542]]],[[[122.781196,30.694175],[122.799674,30.716301],[122.778732,30.729677],[122.757174,30.713728],[122.781196,30.694175]]],[[[121.098449,27.937311],[121.152652,27.961629],[121.120623,27.986471],[121.0695,27.984357],[121.038087,27.948942],[121.098449,27.937311]]],[[[121.185913,27.963215],[121.237652,27.988056],[121.197616,28.000739],[121.17113,27.978543],[121.185913,27.963215]]],[[[122.454132,29.956513],[122.447972,29.955994],[122.445509,29.952365],[122.446741,29.951327],[122.447972,29.947698],[122.459059,29.950809],[122.458443,29.951846],[122.455364,29.955994],[122.454132,29.956513]]],[[[122.836014,30.698806],[122.831087,30.728648],[122.807681,30.714243],[122.836014,30.698806]]],[[[122.200365,29.969475],[122.233626,29.946661],[122.273662,29.93214],[122.239785,29.960142],[122.200365,29.969475]]],[[[122.029134,29.954957],[122.043916,29.930584],[122.058699,29.955994],[122.029134,29.954957]]],[[[121.044247,27.979072],[121.089826,27.998625],[121.073812,28.007608],[121.044247,27.979072]]],[[[122.471378,29.927472],[122.470762,29.925916],[122.473226,29.925397],[122.47261,29.927472],[122.471378,29.927472]]],[[[122.152322,29.97103],[122.155401,29.970512],[122.154169,29.97103],[122.152322,29.97103]]]]}},
    {"type":"Feature","properties":{"adcode":340000,"name":"安徽省","center":[117.283042,31.86119],"centroid":[117.226884,31.849254],"childrenNum":16,"level":"province","parent":{"adcode":100000},"subFeatureIndex":11,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[116.599629,34.014324],[116.641512,33.978103],[116.64336,33.896675],[116.631042,33.887733],[116.566984,33.9081],[116.558361,33.881274],[116.486296,33.869846],[116.437637,33.846489],[116.437021,33.801246],[116.408071,33.805721],[116.393905,33.782843],[116.316912,33.771402],[116.263326,33.730101],[116.230065,33.735078],[116.155536,33.709693],[116.132747,33.751501],[116.100102,33.782843],[116.074232,33.781351],[116.055754,33.804727],[116.05945,33.860902],[115.982457,33.917039],[116.00032,33.965199],[115.95782,34.007875],[115.904233,34.009859],[115.876516,34.028708],[115.877132,34.002913],[115.85003,34.004898],[115.846335,34.028708],[115.809378,34.062428],[115.768726,34.061932],[115.736082,34.076805],[115.705901,34.059949],[115.658473,34.061437],[115.642459,34.03218],[115.60735,34.030196],[115.579017,33.974133],[115.577785,33.950307],[115.547604,33.874815],[115.631988,33.869846],[115.614126,33.775879],[115.576553,33.787817],[115.563003,33.772895],[115.601807,33.718653],[115.601191,33.658898],[115.639995,33.585143],[115.564851,33.576169],[115.511264,33.55323],[115.463837,33.567193],[115.422569,33.557219],[115.394851,33.506335],[115.366518,33.5233],[115.345576,33.502842],[115.345576,33.449928],[115.324634,33.457418],[115.315395,33.431451],[115.328946,33.403477],[115.313547,33.376994],[115.341881,33.370997],[115.365286,33.336005],[115.361591,33.298497],[115.335105,33.297997],[115.340033,33.260973],[115.300613,33.204407],[115.303692,33.149809],[115.289526,33.131769],[115.245178,33.135778],[115.194671,33.120743],[115.168186,33.088658],[115.041302,33.086653],[114.990795,33.102195],[114.966158,33.147304],[114.932897,33.153817],[114.902716,33.129764],[114.897172,33.086653],[114.913187,33.083143],[114.925506,33.016928],[114.891629,33.020441],[114.883006,32.990328],[114.916266,32.971251],[114.943368,32.935094],[115.009273,32.940117],[115.035143,32.932582],[115.029599,32.906962],[115.139237,32.897917],[115.155867,32.864747],[115.197135,32.856201],[115.189744,32.812452],[115.211301,32.785791],[115.189744,32.770695],[115.179273,32.726402],[115.182968,32.666973],[115.20083,32.591876],[115.24333,32.593388],[115.267352,32.578261],[115.30554,32.583303],[115.304924,32.553042],[115.411482,32.575235],[115.409018,32.549007],[115.497713,32.492489],[115.5088,32.468761],[115.510648,32.468761],[115.510648,32.468256],[115.510648,32.467751],[115.509416,32.466741],[115.522967,32.441997],[115.57101,32.419266],[115.604271,32.425833],[115.626445,32.40512],[115.657857,32.428864],[115.667712,32.409667],[115.704669,32.495013],[115.742241,32.476335],[115.771806,32.505108],[115.789052,32.468761],[115.861117,32.537403],[115.891298,32.576243],[115.910393,32.567165],[115.8759,32.542448],[115.845719,32.501575],[115.883291,32.487946],[115.865429,32.458662],[115.899306,32.390971],[115.912856,32.227596],[115.941805,32.166318],[115.922095,32.049725],[115.928871,32.003046],[115.909161,31.94314],[115.920248,31.920285],[115.894994,31.8649],[115.893762,31.832365],[115.914704,31.814567],[115.886371,31.776418],[115.851878,31.786593],[115.808147,31.770313],[115.808147,31.770313],[115.767495,31.78761],[115.731154,31.76726],[115.676336,31.778453],[115.553764,31.69549],[115.534054,31.698545],[115.495249,31.673083],[115.476771,31.643028],[115.485394,31.608885],[115.439815,31.588496],[115.415793,31.525771],[115.371446,31.495668],[115.389924,31.450241],[115.373909,31.405813],[115.393004,31.389977],[115.372062,31.349098],[115.40717,31.337854],[115.443511,31.344498],[115.473076,31.265242],[115.507568,31.267799],[115.539597,31.231985],[115.540213,31.194621],[115.585793,31.143926],[115.603655,31.17363],[115.655394,31.211002],[115.700973,31.201276],[115.778582,31.112164],[115.797676,31.128047],[115.837712,31.127022],[115.867277,31.147512],[115.887603,31.10909],[115.939958,31.071678],[115.938726,31.04707],[116.006479,31.034764],[116.015102,31.011685],[116.058834,31.012711],[116.071769,30.956787],[116.03974,30.957813],[115.976298,30.931636],[115.932566,30.889532],[115.865429,30.864364],[115.848799,30.828397],[115.863581,30.815549],[115.851262,30.756938],[115.782893,30.751795],[115.762567,30.685426],[115.81369,30.637035],[115.819234,30.597893],[115.848799,30.602014],[115.876516,30.582438],[115.887603,30.542758],[115.910393,30.519046],[115.894994,30.452517],[115.921479,30.416397],[115.885139,30.379747],[115.91532,30.337919],[115.903001,30.31364],[115.985537,30.290905],[115.997856,30.252657],[116.065609,30.204569],[116.055754,30.180774],[116.088399,30.110391],[116.078544,30.062233],[116.091479,30.036331],[116.073616,29.969993],[116.128435,29.897904],[116.13521,29.819532],[116.172783,29.828358],[116.227601,29.816936],[116.250391,29.785777],[116.280572,29.788893],[116.342782,29.835626],[116.467818,29.896347],[116.525716,29.897385],[116.552201,29.909836],[116.585462,30.045657],[116.620571,30.073109],[116.666766,30.076734],[116.720353,30.053945],[116.747454,30.057053],[116.783794,30.030632],[116.802889,29.99643],[116.830606,30.004723],[116.83307,29.95755],[116.868794,29.980361],[116.900207,29.949253],[116.882961,29.893753],[116.780715,29.792529],[116.762237,29.802396],[116.673541,29.709916],[116.698795,29.707836],[116.70557,29.69692],[116.706802,29.6964],[116.704954,29.688602],[116.680317,29.681323],[116.651983,29.637118],[116.716657,29.590813],[116.721585,29.564789],[116.760389,29.599139],[116.780715,29.569994],[116.849084,29.57624],[116.873722,29.609546],[116.939627,29.648561],[116.974736,29.657403],[116.996294,29.683403],[117.041873,29.680803],[117.112706,29.711995],[117.108395,29.75201],[117.136728,29.775388],[117.123177,29.798761],[117.073286,29.831992],[117.127489,29.86158],[117.129952,29.89946],[117.171836,29.920729],[117.2168,29.926953],[117.246365,29.915023],[117.261763,29.880781],[117.25314,29.834588],[117.29256,29.822647],[117.338756,29.848085],[117.359082,29.812782],[117.382487,29.840818],[117.415132,29.85068],[117.408973,29.802396],[117.455168,29.749412],[117.453936,29.688082],[117.490277,29.660003],[117.530313,29.654282],[117.523538,29.630356],[117.543248,29.588731],[117.608537,29.591333],[117.647957,29.614749],[117.678754,29.595496],[117.690457,29.555939],[117.729877,29.550213],[117.795167,29.570515],[117.872775,29.54761],[117.933753,29.549172],[118.00397,29.578322],[118.042774,29.566351],[118.050782,29.542924],[118.095129,29.534072],[118.143788,29.489803],[118.127774,29.47209],[118.136397,29.418932],[118.193064,29.395472],[118.248498,29.431443],[118.316252,29.422581],[118.306396,29.479384],[118.329802,29.495012],[118.347664,29.474174],[118.381541,29.504909],[118.496106,29.519492],[118.500417,29.57572],[118.532446,29.588731],[118.573714,29.638159],[118.61991,29.654282],[118.647011,29.64336],[118.700598,29.706277],[118.744945,29.73902],[118.740634,29.814859],[118.841032,29.891159],[118.838568,29.934733],[118.894619,29.937845],[118.902626,30.029078],[118.878604,30.064822],[118.873677,30.11505],[118.895234,30.148694],[118.852119,30.149729],[118.852735,30.166805],[118.929727,30.2025],[118.905089,30.216464],[118.877988,30.282637],[118.880452,30.31519],[118.954365,30.360126],[118.988857,30.332237],[119.06277,30.304856],[119.091719,30.323972],[119.126828,30.304856],[119.201356,30.290905],[119.236465,30.297106],[119.246936,30.341018],[119.277117,30.341018],[119.326392,30.372002],[119.349182,30.349281],[119.402768,30.374584],[119.36766,30.38491],[119.335015,30.448389],[119.336247,30.508734],[119.326392,30.532964],[119.272189,30.510281],[119.237081,30.546881],[119.265414,30.574709],[119.238929,30.609225],[119.323312,30.630341],[119.343022,30.664322],[119.39045,30.685941],[119.408312,30.645274],[119.444652,30.650422],[119.482841,30.704467],[119.479761,30.772365],[119.527188,30.77905],[119.55429,30.825828],[119.575847,30.829939],[119.557369,30.874124],[119.563529,30.919315],[119.582007,30.932149],[119.580159,30.967051],[119.633746,31.019379],[119.629434,31.085517],[119.649144,31.104991],[119.623891,31.130096],[119.599869,31.10909],[119.532732,31.159291],[119.461283,31.156219],[119.439109,31.177214],[119.391682,31.174142],[119.360269,31.213049],[119.374435,31.258591],[119.350414,31.301043],[119.338095,31.259103],[119.294363,31.263195],[119.266646,31.250405],[119.198277,31.270357],[119.197661,31.295418],[119.158241,31.294907],[119.107118,31.250917],[119.10527,31.235055],[119.014727,31.241707],[118.984546,31.237102],[118.870597,31.242219],[118.794836,31.229426],[118.756648,31.279564],[118.726467,31.282121],[118.720924,31.322518],[118.745561,31.372606],[118.767735,31.363919],[118.824401,31.375672],[118.852119,31.393553],[118.883532,31.500261],[118.857046,31.506384],[118.865669,31.519139],[118.885995,31.519139],[118.881684,31.564023],[118.858894,31.623665],[118.802844,31.619078],[118.773894,31.682759],[118.748025,31.675629],[118.736322,31.633347],[118.643315,31.649651],[118.643315,31.671555],[118.697518,31.709747],[118.653786,31.73011],[118.641467,31.75861],[118.571866,31.746397],[118.5577,31.73011],[118.521975,31.743343],[118.533678,31.76726],[118.481939,31.778453],[118.504729,31.841516],[118.466541,31.857784],[118.472084,31.879639],[118.363679,31.930443],[118.389548,31.985281],[118.394476,32.076098],[118.433896,32.086746],[118.501033,32.121726],[118.49549,32.165304],[118.510888,32.194176],[118.643931,32.209875],[118.674728,32.250375],[118.657482,32.30148],[118.703061,32.328792],[118.685199,32.403604],[118.691359,32.472295],[118.628533,32.467751],[118.592192,32.481383],[118.608823,32.536899],[118.564475,32.562122],[118.568787,32.585825],[118.59712,32.600951],[118.632844,32.578261],[118.658714,32.594397],[118.688895,32.588346],[118.719076,32.614059],[118.719076,32.614059],[118.73509,32.58885],[118.757264,32.603976],[118.784981,32.582295],[118.820706,32.60448],[118.84288,32.56767],[118.908169,32.59238],[118.890923,32.553042],[118.92172,32.557078],[118.922336,32.557078],[118.92172,32.557078],[118.922336,32.557078],[118.975923,32.505108],[119.041212,32.515201],[119.084944,32.452602],[119.142226,32.499556],[119.168096,32.536394],[119.152697,32.557582],[119.22045,32.576748],[119.230921,32.607001],[119.208748,32.641276],[119.211827,32.708275],[119.184726,32.825529],[119.113277,32.823014],[119.054763,32.8748],[119.020886,32.955685],[118.993169,32.958196],[118.934039,32.93861],[118.892771,32.941121],[118.89585,32.957694],[118.89585,32.957694],[118.849039,32.956689],[118.846575,32.922034],[118.821322,32.920527],[118.810235,32.853687],[118.743097,32.853184],[118.743097,32.853184],[118.73817,32.772708],[118.756648,32.737477],[118.707373,32.72036],[118.642699,32.744525],[118.572482,32.719856],[118.560163,32.729926],[118.483787,32.721367],[118.450526,32.743518],[118.411106,32.715828],[118.375382,32.718849],[118.363063,32.770695],[118.334114,32.761637],[118.300237,32.783275],[118.301469,32.846145],[118.250346,32.848157],[118.2331,32.914498],[118.252194,32.936601],[118.291614,32.946143],[118.303933,32.96874],[118.26944,32.969242],[118.244803,32.998359],[118.243571,33.027967],[118.219549,33.114227],[118.217085,33.191888],[118.178281,33.217926],[118.149332,33.169348],[118.038463,33.134776],[118.037231,33.152314],[117.988572,33.180869],[117.977485,33.226437],[117.942376,33.224936],[117.939297,33.262475],[117.974405,33.279487],[117.992883,33.333005],[118.029224,33.374995],[118.016905,33.402978],[118.027376,33.455421],[118.050782,33.491863],[118.107448,33.475391],[118.117919,33.594615],[118.112376,33.617045],[118.16781,33.663381],[118.161035,33.735576],[118.117919,33.766427],[118.065564,33.76593],[118.019985,33.738562],[117.972557,33.74951],[117.901724,33.720146],[117.843826,33.736074],[117.791471,33.733585],[117.750203,33.710688],[117.72495,33.74951],[117.739732,33.758467],[117.759442,33.874318],[117.753899,33.891211],[117.715095,33.879287],[117.672595,33.934916],[117.671363,33.992494],[117.629479,34.028708],[117.612849,34.000433],[117.569117,33.985051],[117.543248,34.038627],[117.514914,34.060941],[117.435458,34.028212],[117.404045,34.03218],[117.357234,34.088205],[117.311654,34.067882],[117.277162,34.078787],[117.257452,34.065899],[117.192162,34.068873],[117.130568,34.101586],[117.123793,34.128342],[117.046801,34.151622],[117.025243,34.167469],[117.051112,34.221425],[116.969192,34.283753],[116.983359,34.348011],[116.960569,34.363821],[116.969192,34.389012],[116.909446,34.408271],[116.828142,34.389012],[116.782563,34.429993],[116.773939,34.453683],[116.722816,34.472434],[116.662454,34.472927],[116.592237,34.493646],[116.594085,34.511894],[116.490607,34.573513],[116.477057,34.614896],[116.432709,34.630163],[116.430245,34.650843],[116.374195,34.640011],[116.334159,34.620806],[116.32492,34.601104],[116.286116,34.608986],[116.247927,34.551829],[116.196804,34.575977],[116.191261,34.535561],[116.204196,34.508442],[116.178326,34.496112],[116.162312,34.459605],[116.178942,34.430487],[116.215898,34.403333],[116.213435,34.382098],[116.255934,34.376665],[116.301514,34.342082],[116.357564,34.319843],[116.372347,34.26595],[116.409303,34.273863],[116.409303,34.273863],[116.456731,34.268917],[116.516477,34.296114],[116.562056,34.285731],[116.582382,34.266444],[116.545426,34.241711],[116.542962,34.203608],[116.565752,34.16945],[116.536187,34.151127],[116.52818,34.122892],[116.576223,34.068873],[116.576223,34.068873],[116.599629,34.014324],[116.599629,34.014324]]],[[[118.865669,31.519139],[118.857046,31.506384],[118.883532,31.500261],[118.885995,31.519139],[118.865669,31.519139]]],[[[116.698795,29.707836],[116.673541,29.709916],[116.653831,29.694841],[116.680317,29.681323],[116.704954,29.688602],[116.706802,29.6964],[116.70557,29.69692],[116.698795,29.707836]]],[[[115.5088,32.468761],[115.509416,32.466741],[115.510648,32.467751],[115.510648,32.468256],[115.510648,32.468761],[115.5088,32.468761]]]]}},
    {"type":"Feature","properties":{"adcode":350000,"name":"福建省","center":[119.306239,26.075302],"centroid":[118.006468,26.069925],"childrenNum":9,"level":"province","parent":{"adcode":100000},"subFeatureIndex":12,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[119.004872,24.970009],[118.989473,24.973807],[119.023966,25.04377],[119.016575,25.058409],[118.974691,25.024792],[118.945126,25.028588],[118.892155,25.092558],[118.974691,25.115319],[118.951901,25.15162],[118.985162,25.168954],[118.985162,25.19495],[118.942046,25.211195],[118.940198,25.21715],[118.943278,25.221482],[118.903242,25.239347],[118.900162,25.242595],[118.919256,25.248008],[118.91556,25.256668],[118.918024,25.25721],[118.956212,25.272905],[118.996864,25.266411],[118.975307,25.237723],[118.990089,25.20199],[119.055379,25.219316],[119.074473,25.211195],[119.054147,25.168412],[119.032589,25.17437],[119.028893,25.139702],[119.06585,25.102855],[119.075705,25.099604],[119.134219,25.106107],[119.107118,25.075214],[119.119436,25.012861],[119.146538,25.056782],[119.165632,25.145661],[119.137299,25.15487],[119.108349,25.193867],[119.131755,25.223106],[119.190269,25.175995],[119.231537,25.188993],[119.26911,25.159746],[119.314689,25.190076],[119.294979,25.237182],[119.331935,25.230685],[119.380595,25.250173],[119.333167,25.287516],[119.299291,25.328634],[119.247552,25.333502],[119.240776,25.316733],[119.218603,25.368115],[119.14469,25.388121],[119.151465,25.426503],[119.191501,25.424341],[119.232153,25.442176],[119.219834,25.468654],[119.256175,25.488643],[119.275269,25.476758],[119.26295,25.428124],[119.288204,25.410827],[119.353493,25.411908],[119.343638,25.472436],[119.359037,25.521592],[119.400921,25.493505],[119.45266,25.493505],[119.438493,25.412449],[119.463131,25.448661],[119.491464,25.443257],[119.48592,25.418935],[119.507478,25.396231],[119.486536,25.369737],[119.548746,25.365952],[119.578927,25.400556],[119.555521,25.429205],[119.577695,25.445959],[119.59063,25.398394],[119.582623,25.374063],[119.597405,25.334584],[119.649144,25.342697],[119.665159,25.3719],[119.656535,25.396772],[119.670086,25.435691],[119.622659,25.434069],[119.675014,25.468113],[119.682405,25.445959],[119.688564,25.441095],[119.773564,25.395691],[119.764325,25.433529],[119.804977,25.457847],[119.866571,25.455145],[119.864107,25.469734],[119.862875,25.474597],[119.811136,25.507009],[119.81668,25.532393],[119.861027,25.531313],[119.883817,25.546432],[119.831462,25.579905],[119.843165,25.597717],[119.790194,25.614447],[119.785883,25.66786],[119.700267,25.616606],[119.683637,25.592859],[119.716898,25.551292],[119.715666,25.51187],[119.680557,25.497827],[119.675014,25.475137],[119.634362,25.475137],[119.611572,25.519972],[119.616499,25.556691],[119.586934,25.59232],[119.534579,25.585303],[119.541355,25.6247],[119.478529,25.631715],[119.472986,25.662466],[119.543819,25.684581],[119.602949,25.68512],[119.602949,25.714779],[119.626354,25.723406],[119.628202,25.87212],[119.638057,25.889888],[119.69534,25.904424],[119.723673,26.011503],[119.700267,26.032477],[119.668854,26.026024],[119.654688,26.090002],[119.618963,26.11956],[119.604181,26.168985],[119.664543,26.202282],[119.676246,26.262943],[119.7711,26.285481],[119.802513,26.268846],[119.806825,26.307479],[119.845013,26.323036],[119.862875,26.307479],[119.904143,26.308552],[119.95465,26.352534],[119.946027,26.374519],[119.893672,26.355752],[119.835774,26.434019],[119.83639,26.454381],[119.788346,26.583435],[119.740303,26.610727],[119.670086,26.618218],[119.605412,26.595744],[119.577695,26.622498],[119.619579,26.649246],[119.637441,26.703256],[119.664543,26.726243],[119.711354,26.686681],[119.833926,26.690959],[119.864107,26.671174],[119.873962,26.642827],[119.908455,26.661547],[119.899216,26.693098],[119.938636,26.747088],[119.942947,26.784492],[120.052584,26.786629],[120.061824,26.768997],[119.99407,26.720363],[119.969433,26.686681],[119.972512,26.654594],[119.949107,26.624638],[119.901679,26.624638],[119.851788,26.595209],[119.828383,26.524013],[119.867187,26.509019],[119.947875,26.56042],[119.93802,26.576478],[119.967585,26.597885],[120.007621,26.595744],[120.063671,26.627848],[120.093852,26.613938],[120.1382,26.638012],[120.110483,26.692563],[120.162222,26.717691],[120.151135,26.750829],[120.106787,26.752966],[120.136352,26.797847],[120.103707,26.794642],[120.102476,26.82669],[120.073526,26.823485],[120.054432,26.863533],[120.117874,26.882751],[120.126497,26.920644],[120.130193,26.917976],[120.1807,26.920644],[120.233055,26.907837],[120.25954,26.982526],[120.279866,26.987326],[120.275554,27.027315],[120.29588,27.035845],[120.282946,27.089671],[120.391967,27.081146],[120.403054,27.10086],[120.461568,27.142407],[120.404286,27.204166],[120.401822,27.250996],[120.430155,27.258976],[120.343924,27.363199],[120.340844,27.399867],[120.273091,27.38924],[120.26262,27.432804],[120.221352,27.420055],[120.134504,27.420055],[120.136968,27.402523],[120.096316,27.390302],[120.052584,27.338747],[120.026099,27.344063],[120.008237,27.375423],[119.960194,27.365857],[119.938636,27.329709],[119.843165,27.300464],[119.768636,27.307909],[119.782187,27.330241],[119.739687,27.362668],[119.750774,27.373829],[119.711354,27.403054],[119.685485,27.438646],[119.703347,27.446613],[119.70889,27.514042],[119.690412,27.537394],[119.659615,27.540578],[119.675014,27.574534],[119.630666,27.582491],[119.626354,27.620676],[119.644217,27.663619],[119.606028,27.674749],[119.541971,27.666799],[119.501319,27.649837],[119.501935,27.610601],[119.466826,27.526249],[119.438493,27.508734],[119.416935,27.539517],[119.360269,27.524657],[119.334399,27.480067],[119.285124,27.457766],[119.26911,27.42218],[119.224146,27.416868],[119.14777,27.424836],[119.121284,27.438115],[119.129907,27.475289],[119.092335,27.466262],[119.03998,27.478475],[119.020886,27.498118],[118.983314,27.498649],[118.986393,27.47582],[118.955597,27.4498],[118.907553,27.460952],[118.869365,27.540047],[118.909401,27.568168],[118.913713,27.619616],[118.879836,27.667859],[118.873677,27.733563],[118.829329,27.847921],[118.818242,27.916689],[118.753568,27.947885],[118.730163,27.970615],[118.733858,28.027684],[118.719076,28.063601],[118.767735,28.10584],[118.802228,28.117453],[118.805923,28.154923],[118.771431,28.188687],[118.804075,28.207675],[118.802228,28.240368],[118.756032,28.252493],[118.719692,28.312047],[118.699366,28.309939],[118.674728,28.27147],[118.651322,28.277267],[118.595272,28.258292],[118.588497,28.282538],[118.493026,28.262509],[118.490562,28.238259],[118.444367,28.253548],[118.433896,28.288335],[118.424041,28.291497],[118.314404,28.221913],[118.339041,28.193962],[118.375382,28.186577],[118.361215,28.155978],[118.356288,28.091586],[118.242339,28.075746],[118.199839,28.049869],[118.153644,28.062016],[118.120999,28.041946],[118.129006,28.017118],[118.094513,28.003909],[118.096977,27.970615],[117.999043,27.991227],[117.965166,27.962687],[117.942992,27.974315],[117.910963,27.949471],[117.856145,27.94577],[117.78716,27.896063],[117.788392,27.855858],[117.740348,27.800286],[117.704624,27.834162],[117.68245,27.823577],[117.649805,27.851625],[117.609769,27.863265],[117.556182,27.966387],[117.52169,27.982243],[117.477958,27.930966],[117.453936,27.939955],[117.407741,27.893948],[117.366473,27.88231],[117.341836,27.855858],[117.334444,27.8876],[117.280242,27.871201],[117.276546,27.847921],[117.303031,27.833103],[117.296256,27.764282],[117.245133,27.71926],[117.205097,27.714492],[117.204481,27.683759],[117.174916,27.677399],[117.114554,27.692238],[117.096076,27.667329],[117.11209,27.645596],[117.094228,27.627569],[117.065279,27.665739],[117.040641,27.669979],[117.003685,27.625449],[117.024627,27.592569],[117.01662,27.563393],[117.054808,27.5427],[117.076982,27.566046],[117.103467,27.533149],[117.110242,27.458828],[117.133032,27.42218],[117.107163,27.393491],[117.104699,27.330773],[117.140423,27.322798],[117.136728,27.303123],[117.171836,27.29036],[117.149662,27.241419],[117.044953,27.146667],[117.05296,27.100327],[116.967344,27.061962],[116.936547,27.019319],[116.910062,27.034779],[116.851548,27.009188],[116.817671,27.018252],[116.679085,26.978259],[116.632889,26.933984],[116.602092,26.888623],[116.548506,26.84004],[116.543578,26.803723],[116.557745,26.773806],[116.515245,26.720898],[116.520172,26.684543],[116.566368,26.650315],[116.553433,26.575942],[116.539267,26.559349],[116.597165,26.512768],[116.610716,26.476882],[116.638433,26.477418],[116.608252,26.429732],[116.601476,26.372911],[116.553433,26.365404],[116.553433,26.400253],[116.519557,26.410437],[116.499846,26.361651],[116.459194,26.345026],[116.437021,26.308016],[116.412999,26.297822],[116.385282,26.238253],[116.400064,26.202819],[116.392057,26.171133],[116.435789,26.159854],[116.476441,26.172745],[116.489375,26.113649],[116.384666,26.030864],[116.360028,25.991601],[116.369883,25.963088],[116.326152,25.956631],[116.303362,25.924341],[116.258398,25.902809],[116.225138,25.908731],[116.17771,25.894195],[116.132131,25.860273],[116.131515,25.824185],[116.18079,25.778926],[116.129667,25.758985],[116.106877,25.701299],[116.067457,25.703995],[116.068689,25.646282],[116.041588,25.62416],[116.063145,25.56317],[116.040356,25.548052],[116.03666,25.514571],[116.005247,25.490264],[116.023109,25.435691],[115.992928,25.374063],[116.008327,25.319437],[115.987385,25.290221],[115.949813,25.292386],[115.930719,25.236099],[115.855574,25.20957],[115.860501,25.165704],[115.888219,25.128866],[115.880212,25.092016],[115.908545,25.084428],[115.928255,25.050276],[115.873436,25.019911],[115.925175,24.960786],[115.870356,24.959701],[115.89253,24.936911],[115.907929,24.923343],[115.985537,24.899461],[116.015102,24.905975],[116.068073,24.850053],[116.153073,24.846795],[116.191877,24.877203],[116.221442,24.829959],[116.251007,24.82507],[116.244232,24.793563],[116.297202,24.801712],[116.345862,24.828872],[116.363724,24.87123],[116.395137,24.877746],[116.417927,24.840821],[116.381586,24.82507],[116.375427,24.803885],[116.419158,24.767482],[116.416079,24.744113],[116.44626,24.714216],[116.485064,24.720196],[116.517709,24.652225],[116.506622,24.621218],[116.530027,24.604895],[116.570679,24.621762],[116.600861,24.654401],[116.623034,24.64189],[116.667382,24.658752],[116.777635,24.679418],[116.815207,24.654944],[116.761005,24.583128],[116.759157,24.545572],[116.796729,24.502014],[116.83307,24.496568],[116.860787,24.460075],[116.839229,24.442097],[116.903903,24.369614],[116.895895,24.350533],[116.919301,24.321087],[116.914374,24.287817],[116.938395,24.28127],[116.933468,24.220157],[116.956257,24.216883],[116.998757,24.179217],[116.9347,24.126794],[116.930388,24.064514],[116.953178,24.008218],[116.981511,23.999471],[116.976583,23.931659],[116.955642,23.922359],[116.981511,23.855602],[117.012308,23.855054],[117.019083,23.801952],[117.048032,23.758687],[117.055424,23.694038],[117.123793,23.647448],[117.147199,23.654027],[117.192778,23.629356],[117.192778,23.5619],[117.291328,23.571225],[117.302415,23.550379],[117.387415,23.555317],[117.463791,23.584937],[117.454552,23.628259],[117.493357,23.642514],[117.501364,23.70445],[117.54448,23.715956],[117.601762,23.70171],[117.660276,23.789357],[117.651653,23.815093],[117.671979,23.878041],[117.691073,23.888985],[117.762522,23.886796],[117.792703,23.906494],[117.807486,23.947521],[117.864768,24.004938],[117.910347,24.012045],[117.927594,24.039922],[117.936217,24.100029],[118.000275,24.152462],[118.019369,24.197232],[118.074803,24.225615],[118.115455,24.229435],[118.158571,24.269814],[118.112376,24.357075],[118.081579,24.35653],[118.088354,24.408858],[118.048934,24.418122],[118.084042,24.528695],[118.121615,24.570067],[118.150564,24.583673],[118.169042,24.559725],[118.242955,24.51236],[118.375382,24.536317],[118.363679,24.567889],[118.444367,24.614689],[118.512736,24.60816],[118.557084,24.572788],[118.558316,24.51236],[118.614366,24.521617],[118.680272,24.58204],[118.687047,24.63373],[118.661178,24.622306],[118.652554,24.653857],[118.670417,24.679962],[118.703677,24.665278],[118.778822,24.743569],[118.786213,24.77672],[118.650707,24.808774],[118.647627,24.843536],[118.702445,24.865258],[118.69875,24.848967],[118.748641,24.84245],[118.807771,24.870687],[118.834256,24.854397],[118.864437,24.887518],[118.933423,24.870687],[118.988857,24.878831],[118.987009,24.898375],[118.932807,24.906518],[118.91864,24.932569],[118.945741,24.954275],[119.014111,24.941252],[119.032589,24.961328],[119.032589,24.961871],[119.007335,24.963499],[119.004872,24.970009]]],[[[118.412338,24.514538],[118.374766,24.458986],[118.318715,24.486765],[118.298389,24.477506],[118.31194,24.424661],[118.282375,24.413218],[118.329802,24.382152],[118.353208,24.415398],[118.405563,24.427931],[118.457918,24.412128],[118.477012,24.437738],[118.451758,24.506915],[118.412338,24.514538]]],[[[119.471138,25.197116],[119.507478,25.183036],[119.52534,25.157579],[119.549362,25.161912],[119.566608,25.210112],[119.540739,25.20199],[119.501319,25.21715],[119.473601,25.259916],[119.44342,25.238806],[119.444036,25.20199],[119.471138,25.197116]]],[[[119.580159,25.627398],[119.611572,25.669479],[119.580775,25.650059],[119.580159,25.627398]]],[[[119.976824,26.191005],[120.016244,26.217316],[119.998998,26.235569],[119.970665,26.217852],[119.976824,26.191005]]],[[[118.230636,24.401228],[118.273752,24.441007],[118.233716,24.445911],[118.230636,24.401228]]],[[[119.906607,26.68989],[119.926933,26.664756],[119.950954,26.692563],[119.906607,26.68989]]],[[[118.204151,24.504737],[118.191832,24.536861],[118.14502,24.560814],[118.093281,24.540672],[118.068644,24.463344],[118.084042,24.435559],[118.143173,24.420847],[118.19368,24.463344],[118.204151,24.504737]]],[[[119.929397,26.134067],[119.960194,26.146961],[119.919542,26.172208],[119.929397,26.134067]]],[[[119.642985,26.129231],[119.665159,26.155556],[119.62697,26.173282],[119.606028,26.15287],[119.642985,26.129231]]],[[[120.034106,26.488667],[120.066751,26.498308],[120.071679,26.521336],[120.035954,26.515981],[120.034106,26.488667]]],[[[119.662079,25.646822],[119.673782,25.632794],[119.718745,25.634952],[119.716898,25.664624],[119.662079,25.646822]]],[[[119.760629,26.613402],[119.776644,26.600025],[119.818527,26.616613],[119.796354,26.630523],[119.760629,26.613402]]],[[[120.135736,26.550784],[120.167149,26.571661],[120.153598,26.604841],[120.117874,26.568984],[120.135736,26.550784]]],[[[120.360554,26.916909],[120.394431,26.933984],[120.363018,26.967592],[120.327909,26.963858],[120.319286,26.944654],[120.360554,26.916909]]],[[[120.150519,26.798916],[120.140048,26.795176],[120.163454,26.798381],[120.161606,26.803189],[120.150519,26.798916]]],[[[119.668238,26.628383],[119.720593,26.635873],[119.758781,26.659408],[119.748926,26.681334],[119.712586,26.6685],[119.673782,26.680799],[119.651608,26.657269],[119.668238,26.628383]]]]}},
    {"type":"Feature","properties":{"adcode":360000,"name":"江西省","center":[115.892151,28.676493],"centroid":[115.732975,27.636112],"childrenNum":11,"level":"province","parent":{"adcode":100000},"subFeatureIndex":13,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[118.193064,29.395472],[118.136397,29.418932],[118.127774,29.47209],[118.143788,29.489803],[118.095129,29.534072],[118.050782,29.542924],[118.042774,29.566351],[118.00397,29.578322],[117.933753,29.549172],[117.872775,29.54761],[117.795167,29.570515],[117.729877,29.550213],[117.690457,29.555939],[117.678754,29.595496],[117.647957,29.614749],[117.608537,29.591333],[117.543248,29.588731],[117.523538,29.630356],[117.530313,29.654282],[117.490277,29.660003],[117.453936,29.688082],[117.455168,29.749412],[117.408973,29.802396],[117.415132,29.85068],[117.382487,29.840818],[117.359082,29.812782],[117.338756,29.848085],[117.29256,29.822647],[117.25314,29.834588],[117.261763,29.880781],[117.246365,29.915023],[117.2168,29.926953],[117.171836,29.920729],[117.129952,29.89946],[117.127489,29.86158],[117.073286,29.831992],[117.123177,29.798761],[117.136728,29.775388],[117.108395,29.75201],[117.112706,29.711995],[117.041873,29.680803],[116.996294,29.683403],[116.974736,29.657403],[116.939627,29.648561],[116.873722,29.609546],[116.849084,29.57624],[116.780715,29.569994],[116.760389,29.599139],[116.721585,29.564789],[116.716657,29.590813],[116.651983,29.637118],[116.680317,29.681323],[116.653831,29.694841],[116.673541,29.709916],[116.762237,29.802396],[116.780715,29.792529],[116.882961,29.893753],[116.900207,29.949253],[116.868794,29.980361],[116.83307,29.95755],[116.830606,30.004723],[116.802889,29.99643],[116.783794,30.030632],[116.747454,30.057053],[116.720353,30.053945],[116.666766,30.076734],[116.620571,30.073109],[116.585462,30.045657],[116.552201,29.909836],[116.525716,29.897385],[116.467818,29.896347],[116.342782,29.835626],[116.280572,29.788893],[116.250391,29.785777],[116.227601,29.816936],[116.172783,29.828358],[116.13521,29.819532],[116.087167,29.795125],[116.049595,29.761881],[115.965827,29.724469],[115.909777,29.723949],[115.837096,29.748373],[115.762567,29.793048],[115.706517,29.837703],[115.667712,29.850161],[115.611662,29.841337],[115.51188,29.840299],[115.479235,29.811224],[115.470612,29.739539],[115.412714,29.688602],[115.355431,29.649602],[115.304924,29.637118],[115.28583,29.618391],[115.250722,29.660003],[115.176809,29.654803],[115.113367,29.684963],[115.117679,29.655843],[115.143548,29.645961],[115.120142,29.597578],[115.157099,29.584568],[115.154019,29.510117],[115.086266,29.525741],[115.087498,29.560104],[115.033295,29.546568],[115.00065,29.572076],[114.947679,29.542924],[114.966773,29.522096],[114.940288,29.493971],[114.900868,29.505951],[114.860216,29.476258],[114.888549,29.436134],[114.918114,29.454374],[114.90518,29.473132],[114.935977,29.486678],[114.947063,29.465317],[114.931049,29.422581],[114.895325,29.397557],[114.866375,29.404335],[114.812173,29.383478],[114.784455,29.386086],[114.759818,29.363139],[114.740724,29.386607],[114.67297,29.395993],[114.621847,29.379828],[114.589819,29.352707],[114.519602,29.325578],[114.466015,29.324013],[114.440145,29.341752],[114.376088,29.322969],[114.341595,29.327665],[114.307102,29.365225],[114.259059,29.343839],[114.252284,29.23475],[114.169748,29.216993],[114.063191,29.204978],[114.034857,29.152204],[113.98743,29.126068],[113.952321,29.092604],[113.94185,29.047097],[113.961561,28.999476],[113.955401,28.978536],[113.973879,28.937692],[114.008988,28.955498],[114.005292,28.917788],[114.028082,28.891069],[114.060111,28.902596],[114.056415,28.872204],[114.076741,28.834464],[114.124784,28.843376],[114.153734,28.829221],[114.137719,28.779926],[114.157429,28.761566],[114.122321,28.623497],[114.132176,28.607211],[114.08598,28.558337],[114.138335,28.533629],[114.15435,28.507337],[114.218407,28.48472],[114.217175,28.466308],[114.172212,28.432632],[114.214712,28.403157],[114.252284,28.395787],[114.2529,28.319423],[114.198081,28.29097],[114.182067,28.249858],[114.143879,28.246694],[114.109386,28.205038],[114.107538,28.182885],[114.068734,28.171806],[114.012068,28.174972],[113.992357,28.161255],[114.025002,28.080499],[114.047176,28.057263],[114.025618,28.031382],[113.970184,28.041418],[113.966488,28.017646],[113.936307,28.018703],[113.914133,27.991227],[113.864242,28.004966],[113.845148,27.971672],[113.822974,27.982243],[113.752141,27.93361],[113.72812,27.874904],[113.756453,27.860091],[113.763228,27.799228],[113.69917,27.740979],[113.696707,27.71979],[113.652359,27.663619],[113.607395,27.625449],[113.608627,27.585143],[113.579062,27.545354],[113.583374,27.524657],[113.627105,27.49971],[113.591381,27.467855],[113.59754,27.428554],[113.632033,27.40518],[113.605548,27.38924],[113.616635,27.345658],[113.657902,27.347253],[113.699786,27.331836],[113.72812,27.350442],[113.872865,27.384988],[113.872865,27.346721],[113.854387,27.30525],[113.872865,27.289828],[113.846996,27.222262],[113.779242,27.137081],[113.771851,27.096598],[113.803264,27.099261],[113.824206,27.036378],[113.86301,27.018252],[113.892575,26.964925],[113.927068,26.948922],[113.890112,26.895562],[113.877177,26.859262],[113.835909,26.806394],[113.853771,26.769532],[113.860546,26.664221],[113.912901,26.613938],[113.996669,26.615543],[114.019459,26.587182],[114.10877,26.56952],[114.07243,26.480096],[114.110002,26.482775],[114.090292,26.455988],[114.085364,26.406149],[114.062575,26.406149],[114.030546,26.376664],[114.047792,26.337518],[114.021307,26.288701],[114.029314,26.266163],[113.978807,26.237716],[113.972647,26.20604],[113.949242,26.192616],[113.962792,26.150722],[114.013299,26.184023],[114.088444,26.168448],[114.102611,26.187783],[114.181451,26.214631],[114.216559,26.203355],[114.237501,26.152333],[114.188842,26.121172],[114.10569,26.097526],[114.121089,26.085702],[114.087828,26.06635],[114.044096,26.076564],[114.008372,26.015806],[114.028082,25.98138],[114.028082,25.893119],[113.971416,25.836036],[113.961561,25.77731],[113.920293,25.741197],[113.913517,25.701299],[113.957249,25.611749],[113.983118,25.599336],[113.986198,25.529153],[113.962792,25.528072],[113.94493,25.441635],[114.003444,25.442716],[113.983118,25.415152],[114.050256,25.36433],[114.029314,25.328093],[114.017611,25.273987],[114.039785,25.250714],[114.055799,25.277775],[114.083517,25.275611],[114.115545,25.302125],[114.190074,25.316733],[114.204857,25.29942],[114.260291,25.291845],[114.2954,25.299961],[114.31511,25.33837],[114.382863,25.317274],[114.43029,25.343779],[114.438914,25.376226],[114.477718,25.37136],[114.541159,25.416773],[114.599674,25.385959],[114.63663,25.324306],[114.714238,25.315651],[114.743188,25.274528],[114.73518,25.225813],[114.693912,25.213902],[114.685905,25.173287],[114.73518,25.155954],[114.735796,25.121822],[114.664963,25.10123],[114.640326,25.074129],[114.604601,25.083886],[114.561485,25.077382],[114.532536,25.022623],[114.506051,24.999844],[114.45616,24.99659],[114.454928,24.977062],[114.395798,24.951019],[114.403189,24.877746],[114.378551,24.861457],[114.342211,24.807145],[114.336052,24.749004],[114.281849,24.724001],[114.27261,24.700624],[114.169132,24.689749],[114.19069,24.656576],[114.258443,24.641346],[114.289856,24.619042],[114.300943,24.578775],[114.363769,24.582584],[114.391486,24.563535],[114.403189,24.497657],[114.429058,24.48622],[114.534384,24.559181],[114.589819,24.537406],[114.627391,24.576598],[114.664963,24.583673],[114.704999,24.525973],[114.73826,24.565168],[114.729637,24.608704],[114.781376,24.613057],[114.827571,24.588026],[114.846665,24.602719],[114.868839,24.562446],[114.893477,24.582584],[114.909491,24.661471],[114.940288,24.650049],[115.00373,24.679418],[115.024672,24.669085],[115.057317,24.703343],[115.083802,24.699537],[115.104744,24.667997],[115.1842,24.711498],[115.258729,24.728894],[115.269816,24.749548],[115.306772,24.758787],[115.358511,24.735416],[115.372678,24.774546],[115.412714,24.79302],[115.476771,24.762591],[115.522967,24.702799],[115.555611,24.683768],[115.569778,24.622306],[115.605503,24.62557],[115.671408,24.604895],[115.68927,24.545027],[115.752712,24.546116],[115.785357,24.567345],[115.843871,24.562446],[115.840791,24.584217],[115.797676,24.628834],[115.780429,24.663103],[115.801371,24.705517],[115.769342,24.708236],[115.756408,24.749004],[115.776734,24.774546],[115.764415,24.791933],[115.790284,24.856027],[115.807531,24.862543],[115.824161,24.909232],[115.863581,24.891318],[115.861733,24.863629],[115.907313,24.879917],[115.885139,24.898918],[115.89253,24.936911],[115.870356,24.959701],[115.925175,24.960786],[115.873436,25.019911],[115.928255,25.050276],[115.908545,25.084428],[115.880212,25.092016],[115.888219,25.128866],[115.860501,25.165704],[115.855574,25.20957],[115.930719,25.236099],[115.949813,25.292386],[115.987385,25.290221],[116.008327,25.319437],[115.992928,25.374063],[116.023109,25.435691],[116.005247,25.490264],[116.03666,25.514571],[116.040356,25.548052],[116.063145,25.56317],[116.041588,25.62416],[116.068689,25.646282],[116.067457,25.703995],[116.106877,25.701299],[116.129667,25.758985],[116.18079,25.778926],[116.131515,25.824185],[116.132131,25.860273],[116.17771,25.894195],[116.225138,25.908731],[116.258398,25.902809],[116.303362,25.924341],[116.326152,25.956631],[116.369883,25.963088],[116.360028,25.991601],[116.384666,26.030864],[116.489375,26.113649],[116.476441,26.172745],[116.435789,26.159854],[116.392057,26.171133],[116.400064,26.202819],[116.385282,26.238253],[116.412999,26.297822],[116.437021,26.308016],[116.459194,26.345026],[116.499846,26.361651],[116.519557,26.410437],[116.553433,26.400253],[116.553433,26.365404],[116.601476,26.372911],[116.608252,26.429732],[116.638433,26.477418],[116.610716,26.476882],[116.597165,26.512768],[116.539267,26.559349],[116.553433,26.575942],[116.566368,26.650315],[116.520172,26.684543],[116.515245,26.720898],[116.557745,26.773806],[116.543578,26.803723],[116.548506,26.84004],[116.602092,26.888623],[116.632889,26.933984],[116.679085,26.978259],[116.817671,27.018252],[116.851548,27.009188],[116.910062,27.034779],[116.936547,27.019319],[116.967344,27.061962],[117.05296,27.100327],[117.044953,27.146667],[117.149662,27.241419],[117.171836,27.29036],[117.136728,27.303123],[117.140423,27.322798],[117.104699,27.330773],[117.107163,27.393491],[117.133032,27.42218],[117.110242,27.458828],[117.103467,27.533149],[117.076982,27.566046],[117.054808,27.5427],[117.01662,27.563393],[117.024627,27.592569],[117.003685,27.625449],[117.040641,27.669979],[117.065279,27.665739],[117.094228,27.627569],[117.11209,27.645596],[117.096076,27.667329],[117.114554,27.692238],[117.174916,27.677399],[117.204481,27.683759],[117.205097,27.714492],[117.245133,27.71926],[117.296256,27.764282],[117.303031,27.833103],[117.276546,27.847921],[117.280242,27.871201],[117.334444,27.8876],[117.341836,27.855858],[117.366473,27.88231],[117.407741,27.893948],[117.453936,27.939955],[117.477958,27.930966],[117.52169,27.982243],[117.556182,27.966387],[117.609769,27.863265],[117.649805,27.851625],[117.68245,27.823577],[117.704624,27.834162],[117.740348,27.800286],[117.788392,27.855858],[117.78716,27.896063],[117.856145,27.94577],[117.910963,27.949471],[117.942992,27.974315],[117.965166,27.962687],[117.999043,27.991227],[118.096977,27.970615],[118.094513,28.003909],[118.129006,28.017118],[118.120999,28.041946],[118.153644,28.062016],[118.199839,28.049869],[118.242339,28.075746],[118.356288,28.091586],[118.361215,28.155978],[118.375382,28.186577],[118.339041,28.193962],[118.314404,28.221913],[118.424041,28.291497],[118.433896,28.288335],[118.480091,28.327325],[118.455454,28.384204],[118.432048,28.402104],[118.456686,28.424738],[118.474548,28.478934],[118.414802,28.497344],[118.4302,28.515225],[118.412338,28.55676],[118.428352,28.617193],[118.428352,28.617193],[118.428352,28.681267],[118.403099,28.702791],[118.364295,28.813491],[118.300237,28.826075],[118.270056,28.918836],[118.195527,28.904167],[118.227556,28.942406],[118.165346,28.986912],[118.133933,28.983771],[118.115455,29.009944],[118.115455,29.009944],[118.097593,28.998952],[118.066796,29.053898],[118.076035,29.074822],[118.037847,29.102017],[118.045238,29.149068],[118.027992,29.167882],[118.042159,29.210202],[118.073571,29.216993],[118.077883,29.290614],[118.138861,29.283828],[118.178281,29.297921],[118.166578,29.314099],[118.205382,29.343839],[118.193064,29.395472]]]]}},
    {"type":"Feature","properties":{"adcode":370000,"name":"山东省","center":[117.000923,36.675807],"centroid":[118.187759,36.376092],"childrenNum":16,"level":"province","parent":{"adcode":100000},"subFeatureIndex":14,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[116.374195,34.640011],[116.392057,34.710391],[116.363724,34.715311],[116.369267,34.749247],[116.403144,34.756131],[116.408071,34.850972],[116.445028,34.895652],[116.557745,34.908905],[116.613795,34.922645],[116.622418,34.939818],[116.677853,34.939327],[116.781331,34.916757],[116.789338,34.975133],[116.815823,34.965324],[116.821983,34.929515],[116.858323,34.928533],[116.922381,34.894671],[116.929156,34.843114],[116.966113,34.844588],[116.979047,34.815113],[116.95133,34.81069],[116.969192,34.771864],[117.022163,34.759081],[117.070206,34.713835],[117.061583,34.675947],[117.073286,34.639026],[117.104083,34.648874],[117.15151,34.559222],[117.139191,34.526687],[117.166293,34.434435],[117.248213,34.451216],[117.252524,34.48674],[117.27285,34.499565],[117.267923,34.532603],[117.303647,34.542463],[117.27285,34.556757],[117.311654,34.561686],[117.311654,34.561686],[117.32151,34.566614],[117.32151,34.566614],[117.325205,34.573021],[117.325205,34.573021],[117.370785,34.584846],[117.402813,34.569571],[117.402813,34.550843],[117.465023,34.484767],[117.53832,34.467006],[117.592523,34.462566],[117.609769,34.490686],[117.659044,34.501044],[117.684298,34.547392],[117.801942,34.518798],[117.791471,34.583368],[117.793935,34.651827],[117.902956,34.644443],[117.909732,34.670533],[117.951615,34.678408],[118.053861,34.650843],[118.084042,34.655766],[118.114839,34.614404],[118.079115,34.569571],[118.185056,34.543942],[118.16473,34.50499],[118.132702,34.483287],[118.177665,34.45319],[118.179513,34.379628],[118.217701,34.379134],[118.220165,34.405802],[118.277447,34.404814],[118.290382,34.424563],[118.379693,34.415183],[118.404947,34.427525],[118.416034,34.473914],[118.439439,34.507949],[118.424657,34.595193],[118.439439,34.626223],[118.473932,34.623269],[118.460997,34.656258],[118.545997,34.705964],[118.601431,34.714327],[118.607591,34.694155],[118.664257,34.693663],[118.690127,34.678408],[118.739402,34.693663],[118.783749,34.723181],[118.764039,34.740396],[118.719076,34.745313],[118.739402,34.792508],[118.772047,34.794474],[118.80038,34.843114],[118.805307,34.87307],[118.860742,34.944233],[118.86259,35.025626],[118.928495,35.051106],[118.942662,35.040817],[119.027045,35.055516],[119.114509,35.055026],[119.137915,35.096167],[119.217371,35.106939],[119.250016,35.124562],[119.286972,35.115261],[119.306066,35.076578],[119.354109,35.080007],[119.373819,35.078538],[119.428022,35.121136],[119.397841,35.137777],[119.411392,35.231689],[119.450812,35.285443],[119.493312,35.318655],[119.538275,35.296678],[119.543819,35.347949],[119.590014,35.37284],[119.579543,35.406504],[119.618963,35.459655],[119.663311,35.562931],[119.662079,35.589215],[119.718129,35.615492],[119.75139,35.617924],[119.772332,35.578995],[119.780339,35.584835],[119.792658,35.615492],[119.824071,35.646136],[119.83023,35.620357],[119.868419,35.60868],[119.925085,35.637382],[119.91215,35.660725],[119.950339,35.729741],[119.920157,35.739943],[119.926317,35.759856],[119.958346,35.760342],[120.01378,35.714193],[120.049505,35.786562],[120.032258,35.812288],[120.064287,35.873414],[120.112331,35.885052],[120.125265,35.906868],[120.152983,35.907353],[120.207801,35.947575],[120.169613,35.888446],[120.202258,35.89184],[120.209033,35.917531],[120.265699,35.966468],[120.30512,35.971796],[120.316206,36.002304],[120.289721,36.017311],[120.285409,36.01247],[120.249069,35.992136],[120.257076,36.025055],[120.198562,35.995525],[120.234902,36.030863],[120.239214,36.062316],[120.181316,36.066669],[120.152367,36.095206],[120.116642,36.102943],[120.108635,36.127599],[120.142512,36.143549],[120.140664,36.173507],[120.181316,36.203936],[120.22012,36.209248],[120.224432,36.19138],[120.260772,36.198624],[120.263236,36.182202],[120.310047,36.185101],[120.297112,36.225664],[120.319902,36.232423],[120.362402,36.196209],[120.35809,36.174956],[120.286025,36.047317],[120.337764,36.055058],[120.429539,36.056994],[120.468959,36.087952],[120.546568,36.091821],[120.546568,36.107778],[120.593995,36.100525],[120.615553,36.120348],[120.64327,36.114547],[120.672835,36.130016],[120.712255,36.126632],[120.696857,36.15563],[120.696857,36.203936],[120.680843,36.238698],[120.686386,36.279234],[120.657437,36.276339],[120.66298,36.331803],[120.744284,36.327946],[120.694393,36.390118],[120.759683,36.46283],[120.828668,36.46668],[120.837291,36.459942],[120.858849,36.424797],[120.848994,36.403124],[120.871784,36.36699],[120.911204,36.412276],[120.917979,36.417573],[120.90874,36.450315],[120.938305,36.447908],[120.965407,36.466199],[120.95432,36.507578],[120.983269,36.546051],[120.962327,36.562877],[120.909972,36.568645],[120.884718,36.601323],[120.847146,36.618617],[120.882255,36.627262],[120.926602,36.611892],[120.955551,36.575855],[121.028848,36.572971],[121.078123,36.607568],[121.161275,36.651273],[121.251818,36.671436],[121.29863,36.702151],[121.31218,36.702151],[121.35776,36.713186],[121.400876,36.701191],[121.3941,36.738129],[121.454462,36.752515],[121.496962,36.795179],[121.506817,36.803805],[121.565331,36.830635],[121.548701,36.807638],[121.485259,36.786073],[121.532071,36.73621],[121.575186,36.740047],[121.556092,36.764502],[121.651563,36.723739],[121.631853,36.80093],[121.6762,36.819137],[121.726092,36.826323],[121.762432,36.84644],[121.767975,36.874691],[121.927504,36.932597],[121.965076,36.938337],[122.008808,36.96225],[122.042684,36.871819],[122.051923,36.904846],[122.093191,36.913938],[122.115981,36.94025],[122.124604,36.944077],[122.141235,36.938337],[122.119677,36.891924],[122.175727,36.894317],[122.188662,36.866073],[122.174495,36.842609],[122.220691,36.848835],[122.275509,36.83734],[122.280437,36.835904],[122.344495,36.828239],[122.378371,36.844525],[122.383915,36.865595],[122.415944,36.85937],[122.454748,36.879],[122.452284,36.88618],[122.434422,36.914416],[122.483081,36.913938],[122.48924,36.886659],[122.532356,36.901496],[122.55761,36.968467],[122.544675,37.004797],[122.583479,37.037289],[122.575472,37.054485],[122.494168,37.033945],[122.467067,37.037289],[122.478769,37.058784],[122.484313,37.128956],[122.533588,37.153286],[122.581015,37.147562],[122.573624,37.176178],[122.624131,37.190959],[122.592718,37.261485],[122.567465,37.25958],[122.573624,37.296247],[122.611196,37.339558],[122.607501,37.364296],[122.650616,37.388551],[122.6925,37.373809],[122.714058,37.392355],[122.701739,37.418501],[122.67587,37.413273],[122.641377,37.428482],[122.553914,37.407093],[122.4954,37.413748],[122.487393,37.43466],[122.41656,37.414699],[122.337103,37.414223],[122.281053,37.430858],[122.287212,37.445114],[122.25272,37.467917],[122.194205,37.456041],[122.166488,37.438937],[122.131996,37.49926],[122.163408,37.519199],[122.150474,37.557163],[122.08888,37.554316],[122.075329,37.540556],[122.017431,37.531065],[121.997721,37.494512],[121.923808,37.473142],[121.772903,37.466492],[121.66573,37.473617],[121.635548,37.494037],[121.575802,37.460317],[121.571491,37.441313],[121.477252,37.475992],[121.460006,37.522522],[121.400876,37.557638],[121.395948,37.589891],[121.435368,37.592737],[121.391021,37.625449],[121.349137,37.635403],[121.358376,37.597479],[121.304789,37.582778],[121.217326,37.582778],[121.17421,37.597479],[121.148956,37.626397],[121.161891,37.646302],[121.142797,37.661464],[121.160043,37.698882],[121.136022,37.723501],[121.037471,37.718767],[120.994356,37.759468],[120.943233,37.785486],[120.940769,37.819533],[120.874863,37.833241],[120.845298,37.826623],[120.839139,37.82426],[120.733197,37.833714],[120.656821,37.793054],[120.634031,37.796364],[120.590915,37.7642],[120.517619,37.750005],[120.454793,37.757576],[120.367945,37.697935],[120.227511,37.693673],[120.22012,37.671886],[120.269395,37.658622],[120.272475,37.636824],[120.215192,37.621183],[120.208417,37.588469],[120.246605,37.556689],[120.222584,37.532963],[120.144359,37.481691],[120.086461,37.465067],[120.064903,37.448915],[120.010085,37.442263],[119.949723,37.419927],[119.926933,37.386649],[119.843781,37.376662],[119.837006,37.346695],[119.883201,37.311004],[119.89244,37.263866],[119.865339,37.233854],[119.83023,37.225754],[119.808057,37.196203],[119.740303,37.133727],[119.687332,37.143746],[119.678709,37.158056],[119.576463,37.127524],[119.489616,37.134681],[119.428022,37.125616],[119.361501,37.125616],[119.327624,37.115595],[119.301138,37.139452],[119.298675,37.197156],[119.2069,37.223371],[119.190885,37.25958],[119.204436,37.280058],[119.136683,37.230995],[119.12806,37.254816],[119.091103,37.257674],[119.084328,37.239572],[119.054147,37.254816],[119.03998,37.30434],[119.001176,37.31862],[118.942662,37.497361],[118.939582,37.527268],[118.988857,37.620709],[119.023966,37.642037],[119.153313,37.655305],[119.236465,37.651988],[119.262334,37.660517],[119.280197,37.692726],[119.309146,37.805349],[119.291899,37.869627],[119.24016,37.878131],[119.212443,37.838913],[119.16132,37.81906],[119.12806,37.847892],[119.110813,37.921577],[119.001792,37.99613],[118.974075,38.094162],[118.908169,38.139362],[118.811467,38.157717],[118.703677,38.151129],[118.626069,38.138421],[118.607591,38.129006],[118.597736,38.079088],[118.552156,38.05553],[118.534294,38.063541],[118.517048,38.088509],[118.504729,38.11394],[118.44991,38.124299],[118.431432,38.106406],[118.404331,38.121003],[118.331034,38.12524],[118.217085,38.146893],[118.177665,38.186417],[118.112376,38.210403],[118.045238,38.214165],[118.018753,38.202409],[117.896797,38.279495],[117.895565,38.301572],[117.848754,38.255062],[117.808718,38.22827],[117.789007,38.180772],[117.766834,38.158658],[117.771145,38.134655],[117.746508,38.12524],[117.704624,38.076262],[117.586979,38.071551],[117.557414,38.046105],[117.557414,38.046105],[117.524154,37.989527],[117.513067,37.94329],[117.481038,37.914967],[117.438538,37.854035],[117.400966,37.844584],[117.320278,37.861596],[117.271618,37.839858],[117.185387,37.849783],[117.150278,37.839385],[117.074518,37.848837],[117.027091,37.832296],[116.919301,37.846002],[116.837997,37.835132],[116.804736,37.848837],[116.753613,37.793054],[116.753613,37.77035],[116.724664,37.744327],[116.679085,37.728708],[116.66307,37.686096],[116.604556,37.624975],[116.575607,37.610754],[116.4826,37.521573],[116.448108,37.503059],[116.433941,37.473142],[116.38097,37.522522],[116.379738,37.522047],[116.38097,37.522522],[116.379738,37.522047],[116.36742,37.566177],[116.336007,37.581355],[116.295355,37.554316],[116.278724,37.524895],[116.290427,37.484065],[116.27626,37.466967],[116.240536,37.489764],[116.240536,37.489764],[116.224522,37.479791],[116.243,37.447965],[116.226369,37.428007],[116.2855,37.404241],[116.236224,37.361442],[116.193109,37.365723],[116.169087,37.384271],[116.106261,37.368577],[116.085935,37.373809],[116.024341,37.360015],[115.975682,37.337179],[115.969523,37.239572],[115.909777,37.20669],[115.91224,37.177132],[115.879596,37.150901],[115.888219,37.112254],[115.85619,37.060694],[115.776734,36.992848],[115.79706,36.968945],[115.75764,36.902453],[115.71206,36.883308],[115.683727,36.808117],[115.524815,36.763543],[115.479851,36.760187],[115.451518,36.702151],[115.420105,36.686795],[115.365902,36.621979],[115.355431,36.627262],[115.33141,36.550378],[115.272895,36.497476],[115.291374,36.460423],[115.317243,36.454166],[115.297533,36.413239],[115.340033,36.398307],[115.368982,36.342409],[115.366518,36.30914],[115.423185,36.32216],[115.417025,36.292742],[115.462605,36.276339],[115.466916,36.258969],[115.466916,36.258969],[115.474923,36.248352],[115.483547,36.148865],[115.484163,36.125666],[115.449054,36.047317],[115.447822,36.01247],[115.362822,35.971796],[115.353583,35.938854],[115.364054,35.894264],[115.335105,35.796756],[115.363438,35.779765],[115.407786,35.80889],[115.460141,35.867594],[115.487858,35.880688],[115.495249,35.896203],[115.505104,35.899112],[115.513112,35.890385],[115.583945,35.921893],[115.648618,35.922863],[115.699125,35.966468],[115.774886,35.974702],[115.779813,35.993588],[115.817386,36.012954],[115.859886,36.003756],[115.89869,36.026507],[115.989849,36.045381],[116.057602,36.104877],[116.099486,36.112129],[116.063145,36.028927],[116.048979,35.970343],[115.984921,35.974218],[115.911624,35.960171],[115.907929,35.92674],[115.873436,35.918985],[115.882675,35.879718],[115.859886,35.857894],[115.81677,35.844312],[115.773654,35.854014],[115.73485,35.833154],[115.696046,35.788989],[115.693582,35.754028],[115.622749,35.739457],[115.52851,35.733628],[115.48601,35.710306],[115.383148,35.568772],[115.34496,35.55368],[115.356047,35.490359],[115.307388,35.480126],[115.237171,35.423087],[115.172497,35.426501],[115.126302,35.41821],[115.117679,35.400163],[115.091809,35.416259],[115.073947,35.374304],[115.04315,35.376744],[114.957534,35.261014],[114.929201,35.244886],[114.932281,35.198441],[114.861448,35.182301],[114.841738,35.15099],[114.883006,35.098615],[114.835578,35.076578],[114.818948,35.051596],[114.852209,35.041797],[114.824492,35.012393],[114.880542,35.00357],[114.923658,34.968757],[114.950759,34.989843],[115.008041,34.988372],[115.028983,34.9717],[115.075179,35.000628],[115.12815,35.00455],[115.157099,34.957968],[115.219309,34.96042],[115.205142,34.914303],[115.251953,34.906451],[115.239019,34.87798],[115.256265,34.845079],[115.317243,34.859321],[115.42688,34.805285],[115.449054,34.74433],[115.433655,34.725149],[115.461373,34.637057],[115.515575,34.582383],[115.553148,34.568586],[115.622749,34.574499],[115.685575,34.556265],[115.697278,34.594207],[115.787821,34.580905],[115.827241,34.558236],[115.838328,34.5676],[115.984305,34.589281],[115.991081,34.615389],[116.037276,34.593222],[116.101334,34.60603],[116.134594,34.559715],[116.156768,34.5538],[116.196804,34.575977],[116.247927,34.551829],[116.286116,34.608986],[116.32492,34.601104],[116.334159,34.620806],[116.374195,34.640011]]],[[[120.729502,37.947065],[120.721495,37.917328],[120.76461,37.895134],[120.76461,37.923937],[120.729502,37.947065]]],[[[120.692545,37.983867],[120.732581,37.961694],[120.724574,37.987641],[120.692545,37.983867]]],[[[120.990044,36.413239],[120.978341,36.428649],[120.950624,36.414684],[120.990044,36.413239]]],[[[120.750444,38.150188],[120.7874,38.158658],[120.742436,38.199116],[120.750444,38.150188]]],[[[120.918595,38.345236],[120.914899,38.373393],[120.895189,38.36307],[120.918595,38.345236]]],[[[120.159142,35.765198],[120.169613,35.740428],[120.193019,35.756942],[120.172077,35.785591],[120.159142,35.765198]]],[[[120.62664,37.94565],[120.631567,37.981037],[120.602002,37.978678],[120.62664,37.94565]]],[[[120.802183,38.284193],[120.848378,38.305799],[120.816349,38.318008],[120.802183,38.284193]]],[[[121.489571,37.577086],[121.489571,37.577561],[121.489571,37.578509],[121.488955,37.578035],[121.489571,37.577086]]],[[[121.485875,37.578509],[121.487723,37.578035],[121.487723,37.578509],[121.485875,37.578509]]],[[[121.487723,37.578509],[121.487723,37.577561],[121.488955,37.578035],[121.488955,37.578509],[121.488339,37.578509],[121.487723,37.578509]]],[[[115.495249,35.896203],[115.487858,35.880688],[115.513112,35.890385],[115.505104,35.899112],[115.495249,35.896203]]]]}},
    {"type":"Feature","properties":{"adcode":410000,"name":"河南省","center":[113.665412,34.757975],"centroid":[113.619717,33.902648],"childrenNum":18,"level":"province","parent":{"adcode":100000},"subFeatureIndex":15,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[112.716747,32.357612],[112.735841,32.356095],[112.776493,32.358623],[112.860877,32.396024],[112.888594,32.37682],[112.912,32.390971],[112.992072,32.378336],[113.000695,32.41674],[113.025949,32.425328],[113.078919,32.394508],[113.107869,32.398551],[113.118956,32.375809],[113.155912,32.380863],[113.158992,32.410677],[113.211962,32.431895],[113.2366,32.407141],[113.333918,32.336377],[113.317904,32.327275],[113.353628,32.294904],[113.376418,32.298445],[113.428773,32.270618],[113.511925,32.316654],[113.624642,32.36115],[113.650511,32.412698],[113.700402,32.420782],[113.735511,32.410677],[113.76754,32.370249],[113.753989,32.328286],[113.768772,32.30148],[113.768156,32.284279],[113.758301,32.27669],[113.749061,32.272642],[113.73859,32.255942],[113.752757,32.215951],[113.782322,32.184553],[113.750293,32.11615],[113.722576,32.12426],[113.728735,32.083197],[113.791561,32.036028],[113.757685,31.98985],[113.817431,31.964467],[113.805728,31.929428],[113.832213,31.918761],[113.830981,31.87913],[113.854387,31.843042],[113.893807,31.847109],[113.914749,31.877098],[113.957865,31.852701],[113.952321,31.793714],[113.988662,31.749959],[114.017611,31.770822],[114.086596,31.782014],[114.121705,31.809482],[114.134024,31.843042],[114.191922,31.852192],[114.235654,31.833382],[114.292936,31.752503],[114.350218,31.755557],[114.403189,31.746906],[114.443841,31.728074],[114.530688,31.742834],[114.549783,31.766751],[114.586123,31.762172],[114.57134,31.660858],[114.547935,31.623665],[114.560869,31.560963],[114.572572,31.553824],[114.61692,31.585437],[114.641558,31.582378],[114.696376,31.525771],[114.778912,31.520669],[114.789383,31.480358],[114.830035,31.45892],[114.870071,31.479337],[114.884238,31.469129],[114.962462,31.494648],[114.995107,31.471171],[115.022824,31.527811],[115.096121,31.508425],[115.114599,31.530362],[115.106592,31.567592],[115.12507,31.599201],[115.16449,31.604808],[115.212533,31.555354],[115.235939,31.555354],[115.218077,31.515057],[115.211301,31.442072],[115.252569,31.421646],[115.250722,31.392021],[115.301229,31.383846],[115.338801,31.40428],[115.373909,31.405813],[115.389924,31.450241],[115.371446,31.495668],[115.415793,31.525771],[115.439815,31.588496],[115.485394,31.608885],[115.476771,31.643028],[115.495249,31.673083],[115.534054,31.698545],[115.553764,31.69549],[115.676336,31.778453],[115.731154,31.76726],[115.767495,31.78761],[115.808147,31.770313],[115.808147,31.770313],[115.851878,31.786593],[115.886371,31.776418],[115.914704,31.814567],[115.893762,31.832365],[115.894994,31.8649],[115.920248,31.920285],[115.909161,31.94314],[115.928871,32.003046],[115.922095,32.049725],[115.941805,32.166318],[115.912856,32.227596],[115.899306,32.390971],[115.865429,32.458662],[115.883291,32.487946],[115.845719,32.501575],[115.8759,32.542448],[115.910393,32.567165],[115.891298,32.576243],[115.861117,32.537403],[115.789052,32.468761],[115.771806,32.505108],[115.742241,32.476335],[115.704669,32.495013],[115.667712,32.409667],[115.657857,32.428864],[115.626445,32.40512],[115.604271,32.425833],[115.57101,32.419266],[115.522967,32.441997],[115.509416,32.466741],[115.5088,32.468761],[115.497713,32.492489],[115.409018,32.549007],[115.411482,32.575235],[115.304924,32.553042],[115.30554,32.583303],[115.267352,32.578261],[115.24333,32.593388],[115.20083,32.591876],[115.182968,32.666973],[115.179273,32.726402],[115.189744,32.770695],[115.211301,32.785791],[115.189744,32.812452],[115.197135,32.856201],[115.155867,32.864747],[115.139237,32.897917],[115.029599,32.906962],[115.035143,32.932582],[115.009273,32.940117],[114.943368,32.935094],[114.916266,32.971251],[114.883006,32.990328],[114.891629,33.020441],[114.925506,33.016928],[114.913187,33.083143],[114.897172,33.086653],[114.902716,33.129764],[114.932897,33.153817],[114.966158,33.147304],[114.990795,33.102195],[115.041302,33.086653],[115.168186,33.088658],[115.194671,33.120743],[115.245178,33.135778],[115.289526,33.131769],[115.303692,33.149809],[115.300613,33.204407],[115.340033,33.260973],[115.335105,33.297997],[115.361591,33.298497],[115.365286,33.336005],[115.341881,33.370997],[115.313547,33.376994],[115.328946,33.403477],[115.315395,33.431451],[115.324634,33.457418],[115.345576,33.449928],[115.345576,33.502842],[115.366518,33.5233],[115.394851,33.506335],[115.422569,33.557219],[115.463837,33.567193],[115.511264,33.55323],[115.564851,33.576169],[115.639995,33.585143],[115.601191,33.658898],[115.601807,33.718653],[115.563003,33.772895],[115.576553,33.787817],[115.614126,33.775879],[115.631988,33.869846],[115.547604,33.874815],[115.577785,33.950307],[115.579017,33.974133],[115.60735,34.030196],[115.642459,34.03218],[115.658473,34.061437],[115.705901,34.059949],[115.736082,34.076805],[115.768726,34.061932],[115.809378,34.062428],[115.846335,34.028708],[115.85003,34.004898],[115.877132,34.002913],[115.876516,34.028708],[115.904233,34.009859],[115.95782,34.007875],[116.00032,33.965199],[115.982457,33.917039],[116.05945,33.860902],[116.055754,33.804727],[116.074232,33.781351],[116.100102,33.782843],[116.132747,33.751501],[116.155536,33.709693],[116.230065,33.735078],[116.263326,33.730101],[116.316912,33.771402],[116.393905,33.782843],[116.408071,33.805721],[116.437021,33.801246],[116.437637,33.846489],[116.486296,33.869846],[116.558361,33.881274],[116.566984,33.9081],[116.631042,33.887733],[116.64336,33.896675],[116.641512,33.978103],[116.599629,34.014324],[116.599629,34.014324],[116.576223,34.068873],[116.576223,34.068873],[116.52818,34.122892],[116.536187,34.151127],[116.565752,34.16945],[116.542962,34.203608],[116.545426,34.241711],[116.582382,34.266444],[116.562056,34.285731],[116.516477,34.296114],[116.456731,34.268917],[116.409303,34.273863],[116.409303,34.273863],[116.372347,34.26595],[116.357564,34.319843],[116.301514,34.342082],[116.255934,34.376665],[116.213435,34.382098],[116.215898,34.403333],[116.178942,34.430487],[116.162312,34.459605],[116.178326,34.496112],[116.204196,34.508442],[116.191261,34.535561],[116.196804,34.575977],[116.156768,34.5538],[116.134594,34.559715],[116.101334,34.60603],[116.037276,34.593222],[115.991081,34.615389],[115.984305,34.589281],[115.838328,34.5676],[115.827241,34.558236],[115.787821,34.580905],[115.697278,34.594207],[115.685575,34.556265],[115.622749,34.574499],[115.553148,34.568586],[115.515575,34.582383],[115.461373,34.637057],[115.433655,34.725149],[115.449054,34.74433],[115.42688,34.805285],[115.317243,34.859321],[115.256265,34.845079],[115.239019,34.87798],[115.251953,34.906451],[115.205142,34.914303],[115.219309,34.96042],[115.157099,34.957968],[115.12815,35.00455],[115.075179,35.000628],[115.028983,34.9717],[115.008041,34.988372],[114.950759,34.989843],[114.923658,34.968757],[114.880542,35.00357],[114.824492,35.012393],[114.852209,35.041797],[114.818948,35.051596],[114.835578,35.076578],[114.883006,35.098615],[114.841738,35.15099],[114.861448,35.182301],[114.932281,35.198441],[114.929201,35.244886],[114.957534,35.261014],[115.04315,35.376744],[115.073947,35.374304],[115.091809,35.416259],[115.117679,35.400163],[115.126302,35.41821],[115.172497,35.426501],[115.237171,35.423087],[115.307388,35.480126],[115.356047,35.490359],[115.34496,35.55368],[115.383148,35.568772],[115.48601,35.710306],[115.52851,35.733628],[115.622749,35.739457],[115.693582,35.754028],[115.696046,35.788989],[115.73485,35.833154],[115.773654,35.854014],[115.81677,35.844312],[115.859886,35.857894],[115.882675,35.879718],[115.873436,35.918985],[115.907929,35.92674],[115.911624,35.960171],[115.984921,35.974218],[116.048979,35.970343],[116.063145,36.028927],[116.099486,36.112129],[116.057602,36.104877],[115.989849,36.045381],[115.89869,36.026507],[115.859886,36.003756],[115.817386,36.012954],[115.779813,35.993588],[115.774886,35.974702],[115.699125,35.966468],[115.648618,35.922863],[115.583945,35.921893],[115.513112,35.890385],[115.487858,35.880688],[115.460141,35.867594],[115.407786,35.80889],[115.363438,35.779765],[115.335105,35.796756],[115.364054,35.894264],[115.353583,35.938854],[115.362822,35.971796],[115.447822,36.01247],[115.449054,36.047317],[115.484163,36.125666],[115.483547,36.148865],[115.465068,36.170125],[115.450902,36.152248],[115.376989,36.128083],[115.365902,36.099074],[115.312931,36.088436],[115.30246,36.127599],[115.279055,36.13775],[115.242098,36.19138],[115.202678,36.208765],[115.202678,36.208765],[115.202678,36.209248],[115.202678,36.209248],[115.201446,36.210214],[115.201446,36.210214],[115.1842,36.193312],[115.12507,36.209731],[115.104744,36.172058],[115.06286,36.178338],[115.048693,36.161912],[115.04623,36.112613],[114.998186,36.069572],[114.914419,36.052155],[114.926737,36.089403],[114.912571,36.140649],[114.858368,36.144516],[114.857752,36.127599],[114.771521,36.124699],[114.734564,36.15563],[114.720398,36.140166],[114.640326,36.137266],[114.588587,36.118414],[114.586739,36.141133],[114.533152,36.171575],[114.480181,36.177855],[114.466015,36.197658],[114.417356,36.205868],[114.408117,36.224699],[114.356378,36.230492],[114.345291,36.255591],[114.299095,36.245938],[114.257827,36.263794],[114.241197,36.251247],[114.2104,36.272962],[114.203009,36.245456],[114.170364,36.245938],[114.170364,36.245938],[114.175907,36.264759],[114.129096,36.280199],[114.080437,36.269585],[114.04348,36.303353],[114.056415,36.329392],[114.002828,36.334214],[113.981887,36.31782],[113.962792,36.353977],[113.911054,36.314927],[113.882104,36.353977],[113.84946,36.347711],[113.856851,36.329392],[113.813119,36.332285],[113.755221,36.366026],[113.731199,36.363135],[113.736127,36.324571],[113.712105,36.303353],[113.716417,36.262347],[113.681924,36.216491],[113.697939,36.181719],[113.651127,36.174473],[113.705946,36.148865],[113.712721,36.129533],[113.655439,36.125182],[113.671453,36.115514],[113.68562,36.056026],[113.660366,36.034735],[113.694859,36.026991],[113.678844,35.985841],[113.648663,35.994073],[113.654207,35.931586],[113.637576,35.870019],[113.660982,35.837035],[113.582758,35.818111],[113.604932,35.797727],[113.587685,35.736542],[113.592613,35.691838],[113.622794,35.674825],[113.625258,35.632518],[113.578446,35.633491],[113.547649,35.656835],[113.55812,35.621816],[113.513773,35.57364],[113.49899,35.532254],[113.439244,35.507412],[113.391817,35.506925],[113.348085,35.468429],[113.31236,35.481101],[113.304353,35.426989],[113.243375,35.449418],[113.189789,35.44893],[113.185477,35.409431],[113.165151,35.412845],[113.149137,35.350878],[113.126347,35.332327],[113.067217,35.353806],[112.996384,35.362104],[112.985913,35.33965],[112.992072,35.29619],[112.936022,35.284466],[112.934174,35.262968],[112.884283,35.243909],[112.822073,35.258082],[112.772798,35.207732],[112.720443,35.206265],[112.628052,35.263457],[112.637291,35.225822],[112.513487,35.218489],[112.390915,35.239021],[112.36751,35.219956],[112.288053,35.219956],[112.304684,35.251728],[112.242474,35.234622],[112.21722,35.253195],[112.13838,35.271275],[112.058924,35.280069],[112.078634,35.219467],[112.03983,35.194039],[112.066315,35.153437],[112.05646,35.098615],[112.062004,35.056005],[112.039214,35.045717],[112.018888,35.068742],[111.97762,35.067272],[111.933272,35.083435],[111.810084,35.062374],[111.807005,35.032977],[111.740483,35.00455],[111.664107,34.984449],[111.681969,34.9511],[111.646861,34.938836],[111.617911,34.894671],[111.592042,34.881416],[111.570484,34.843114],[111.543999,34.853428],[111.502731,34.829851],[111.439289,34.838202],[111.389398,34.815113],[111.345666,34.831816],[111.29208,34.806759],[111.255123,34.819535],[111.232949,34.789559],[111.148566,34.807742],[111.118385,34.756623],[111.035233,34.740887],[110.976103,34.706456],[110.920052,34.730068],[110.903422,34.669056],[110.883712,34.64395],[110.824582,34.615881],[110.791937,34.649858],[110.749437,34.65232],[110.710017,34.605045],[110.610851,34.607508],[110.533242,34.583368],[110.488279,34.610956],[110.424837,34.588295],[110.379257,34.600612],[110.366939,34.566614],[110.404511,34.557743],[110.372482,34.544435],[110.360779,34.516825],[110.403279,34.433448],[110.403279,34.433448],[110.473496,34.393457],[110.503677,34.33714],[110.451938,34.292653],[110.428533,34.288203],[110.43962,34.243196],[110.507989,34.217466],[110.55172,34.213012],[110.55788,34.193214],[110.621938,34.177372],[110.642264,34.161032],[110.61393,34.113478],[110.591757,34.101586],[110.587445,34.023252],[110.620706,34.035652],[110.671213,33.966192],[110.665669,33.937895],[110.627481,33.925482],[110.628713,33.910086],[110.587445,33.887733],[110.612083,33.852453],[110.66259,33.85295],[110.712481,33.833564],[110.74143,33.798759],[110.782082,33.796272],[110.81719,33.751003],[110.831973,33.713675],[110.823966,33.685793],[110.878784,33.634486],[110.966864,33.609071],[111.00382,33.578662],[111.002588,33.535772],[111.02661,33.478386],[111.02661,33.467903],[110.996429,33.435946],[111.025994,33.375495],[111.025994,33.330504],[110.984726,33.255469],[111.046936,33.202905],[111.045704,33.169849],[111.08882,33.181871],[111.12824,33.15532],[111.146102,33.12375],[111.179363,33.115229],[111.192913,33.071609],[111.152877,33.039507],[111.221862,33.042517],[111.258819,33.006389],[111.273601,32.971753],[111.242804,32.930573],[111.255123,32.883846],[111.276065,32.903445],[111.293311,32.859217],[111.380159,32.829049],[111.41342,32.757108],[111.475629,32.760127],[111.458383,32.726402],[111.513202,32.674026],[111.530448,32.628172],[111.577875,32.593388],[111.640701,32.634724],[111.646245,32.605993],[111.713382,32.606497],[111.808853,32.536899],[111.858128,32.528826],[111.890157,32.503089],[111.948671,32.51722],[111.975772,32.471791],[112.014576,32.450077],[112.063851,32.474315],[112.081098,32.425833],[112.155626,32.377326],[112.150083,32.411688],[112.172873,32.385412],[112.206133,32.392992],[112.328089,32.321712],[112.360118,32.3657],[112.390915,32.37126],[112.448814,32.34295],[112.477147,32.380863],[112.530733,32.37682],[112.545516,32.404109],[112.589248,32.381369],[112.612037,32.386928],[112.645298,32.368227],[112.716747,32.357612]]],[[[113.768156,32.284279],[113.768772,32.30148],[113.749061,32.272642],[113.758301,32.27669],[113.768156,32.284279]]]]}},
    {"type":"Feature","properties":{"adcode":420000,"name":"湖北省","center":[114.298572,30.584355],"centroid":[112.271301,30.987527],"childrenNum":17,"level":"province","parent":{"adcode":100000},"subFeatureIndex":16,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[111.045704,33.169849],[111.034001,33.177864],[111.035849,33.187881],[111.046936,33.202905],[110.984726,33.255469],[110.960704,33.253967],[110.9219,33.203907],[110.865234,33.213921],[110.828893,33.201403],[110.824582,33.158327],[110.753133,33.15031],[110.702626,33.097182],[110.650887,33.157324],[110.623785,33.143796],[110.59422,33.168346],[110.57759,33.250464],[110.54125,33.255469],[110.471032,33.171352],[110.398352,33.176862],[110.398352,33.176862],[110.372482,33.186379],[110.33799,33.160331],[110.285635,33.171352],[110.218497,33.163336],[110.164911,33.209415],[110.031252,33.191888],[109.999223,33.212419],[109.973353,33.203907],[109.916687,33.229942],[109.852013,33.247961],[109.813209,33.236449],[109.732521,33.231443],[109.693101,33.254468],[109.649985,33.251465],[109.619804,33.275484],[109.60687,33.235949],[109.514479,33.237951],[109.498464,33.207412],[109.438718,33.152314],[109.468283,33.140288],[109.522486,33.138785],[109.576073,33.110216],[109.688174,33.116733],[109.704188,33.101694],[109.794731,33.067095],[109.785492,32.987316],[109.76455,32.909474],[109.789804,32.882339],[109.847702,32.893395],[109.856941,32.910479],[109.907448,32.903947],[109.927158,32.887364],[109.988752,32.886359],[110.051578,32.851676],[110.105164,32.832569],[110.142121,32.802895],[110.127338,32.77774],[110.159367,32.767173],[110.156903,32.683093],[110.206179,32.633212],[110.153824,32.593388],[110.124259,32.616579],[110.090382,32.617083],[110.084223,32.580782],[110.017701,32.546989],[109.97089,32.577756],[109.910528,32.592884],[109.816905,32.577252],[109.746072,32.594901],[109.726978,32.608513],[109.631507,32.599943],[109.619804,32.56767],[109.637051,32.540935],[109.575457,32.506622],[109.526797,32.43341],[109.529877,32.405625],[109.502776,32.38895],[109.513247,32.342444],[109.495385,32.300468],[109.528645,32.270112],[109.550203,32.225065],[109.592703,32.219495],[109.604406,32.199241],[109.58716,32.161251],[109.621652,32.106519],[109.590855,32.047696],[109.590855,32.012688],[109.631507,31.962436],[109.62042,31.928412],[109.584696,31.900472],[109.60379,31.885737],[109.633971,31.824738],[109.633971,31.804396],[109.592087,31.789136],[109.585928,31.726546],[109.622268,31.711783],[109.683246,31.719929],[109.731289,31.700582],[109.737449,31.628761],[109.76455,31.602769],[109.745456,31.598182],[109.727594,31.548214],[109.837847,31.555354],[109.894513,31.519139],[109.969658,31.508935],[109.94502,31.47066],[109.98752,31.474744],[110.036795,31.436966],[110.054042,31.410921],[110.118715,31.409899],[110.161831,31.314338],[110.155671,31.279564],[110.180309,31.179774],[110.200019,31.158779],[110.180309,31.121899],[110.147048,31.116776],[110.119947,31.088592],[110.120563,31.0322],[110.140273,31.030661],[110.140889,30.987062],[110.172918,30.978853],[110.153824,30.953708],[110.151976,30.911613],[110.082375,30.799614],[110.048498,30.800642],[110.019549,30.829425],[110.008462,30.883369],[109.943788,30.878746],[109.894513,30.899803],[109.828608,30.864364],[109.780564,30.848437],[109.701724,30.783677],[109.656761,30.760538],[109.661072,30.738936],[109.625348,30.702923],[109.590855,30.69366],[109.574225,30.646818],[109.543428,30.63961],[109.535421,30.664837],[109.435638,30.595832],[109.418392,30.559766],[109.35495,30.487076],[109.337088,30.521623],[109.36111,30.551004],[109.314298,30.599953],[109.299516,30.630341],[109.245313,30.580892],[109.191726,30.545851],[109.191726,30.545851],[109.143683,30.521108],[109.103647,30.565949],[109.09256,30.578831],[109.106111,30.61077],[109.111654,30.646303],[109.071002,30.640125],[109.042669,30.655571],[109.006329,30.626736],[108.971836,30.627766],[108.893612,30.565434],[108.838793,30.503062],[108.808612,30.491202],[108.789518,30.513374],[108.743939,30.494812],[108.698975,30.54482],[108.688504,30.58759],[108.642925,30.578831],[108.6497,30.53915],[108.56778,30.468508],[108.556077,30.487592],[108.512961,30.501515],[108.472925,30.487076],[108.42673,30.492233],[108.411331,30.438586],[108.430425,30.416397],[108.402092,30.376649],[108.431041,30.354446],[108.460606,30.35961],[108.501258,30.314673],[108.524048,30.309506],[108.54499,30.269716],[108.581947,30.255759],[108.551766,30.1637],[108.56778,30.157491],[108.546222,30.104178],[108.513577,30.057571],[108.532055,30.051873],[108.536367,29.983472],[108.517889,29.9394],[108.516041,29.885451],[108.467998,29.864175],[108.433505,29.880262],[108.371295,29.841337],[108.424266,29.815897],[108.422418,29.772791],[108.442744,29.778505],[108.437201,29.741098],[108.460606,29.741098],[108.504338,29.707836],[108.504954,29.728626],[108.548686,29.749412],[108.52528,29.770713],[108.556077,29.818493],[108.601041,29.863656],[108.658939,29.854833],[108.680497,29.800319],[108.676801,29.749412],[108.690968,29.689642],[108.752562,29.649082],[108.786438,29.691721],[108.797525,29.660003],[108.781511,29.635558],[108.844337,29.658443],[108.888068,29.628795],[108.870206,29.596537],[108.901003,29.604863],[108.913322,29.574679],[108.878213,29.539279],[108.888684,29.502305],[108.866511,29.470527],[108.884373,29.440824],[108.927488,29.435612],[108.934264,29.399643],[108.919481,29.3261],[108.983539,29.332883],[108.999553,29.36366],[109.034662,29.360531],[109.060531,29.403292],[109.11227,29.361053],[109.106727,29.288526],[109.141835,29.270256],[109.110422,29.21647],[109.139372,29.168927],[109.162777,29.180946],[109.215748,29.145409],[109.232378,29.119271],[109.274262,29.121885],[109.261328,29.161089],[109.275494,29.202366],[109.257632,29.222738],[109.312451,29.25146],[109.352487,29.284872],[109.343863,29.369398],[109.391291,29.372005],[109.368501,29.413719],[109.418392,29.453332],[109.415928,29.497617],[109.436254,29.488761],[109.433791,29.530948],[109.458428,29.513242],[109.467051,29.560104],[109.488609,29.553336],[109.516326,29.626194],[109.558826,29.606944],[109.578536,29.629836],[109.651833,29.625674],[109.664768,29.599659],[109.717739,29.615269],[109.701108,29.636078],[109.714659,29.673524],[109.760238,29.689122],[109.755311,29.733304],[109.779333,29.757725],[109.869876,29.774869],[109.908064,29.763959],[109.941325,29.774349],[110.02386,29.769674],[110.113788,29.789932],[110.160599,29.753569],[110.219729,29.746814],[110.289946,29.6964],[110.302265,29.661563],[110.339221,29.668324],[110.372482,29.633477],[110.447011,29.664684],[110.467337,29.713034],[110.507373,29.692241],[110.562807,29.712515],[110.642879,29.775907],[110.60038,29.839779],[110.549873,29.848085],[110.538786,29.895828],[110.49875,29.91243],[110.517228,29.961179],[110.557264,29.988137],[110.491358,30.019751],[110.497518,30.055499],[110.531394,30.061197],[110.600996,30.054463],[110.650887,30.07777],[110.712481,30.033223],[110.756212,30.054463],[110.746973,30.112979],[110.851067,30.126439],[110.924364,30.111426],[110.929907,30.063268],[111.031537,30.048765],[111.242188,30.040476],[111.266826,30.01146],[111.3315,29.970512],[111.342587,29.944586],[111.382623,29.95029],[111.394325,29.912948],[111.436825,29.930065],[111.475629,29.918654],[111.527368,29.925916],[111.553854,29.894272],[111.669034,29.888565],[111.669034,29.888565],[111.705375,29.890121],[111.723853,29.909317],[111.723853,29.909317],[111.75773,29.92021],[111.8107,29.901017],[111.861207,29.856909],[111.899396,29.855871],[111.899396,29.855871],[111.925881,29.836665],[111.965917,29.832512],[111.95483,29.796683],[112.008417,29.778505],[112.07617,29.743696],[112.065699,29.681323],[112.089721,29.685482],[112.111279,29.659483],[112.178416,29.656883],[112.202438,29.633997],[112.244322,29.659483],[112.233851,29.61631],[112.303452,29.585609],[112.281278,29.536676],[112.291133,29.517409],[112.333017,29.545007],[112.368741,29.541362],[112.424792,29.598619],[112.439574,29.633997],[112.499321,29.629316],[112.54182,29.60122],[112.572001,29.624113],[112.640371,29.607985],[112.650842,29.592374],[112.693957,29.601741],[112.714283,29.648561],[112.733378,29.645441],[112.788812,29.681323],[112.79374,29.735902],[112.861493,29.78318],[112.894138,29.783699],[112.902145,29.79149],[112.929246,29.77383],[112.923703,29.766557],[112.926782,29.692241],[112.944645,29.682883],[112.974826,29.732784],[113.025949,29.772791],[113.005007,29.693801],[112.915696,29.620992],[112.912,29.606944],[112.950188,29.473132],[113.034572,29.523658],[113.057362,29.522616],[113.078304,29.438218],[113.099861,29.459585],[113.145441,29.449163],[113.181781,29.485636],[113.222433,29.543965],[113.277252,29.594976],[113.37765,29.703158],[113.571671,29.849123],[113.575367,29.809147],[113.550729,29.768115],[113.558736,29.727067],[113.540258,29.699519],[113.547033,29.675603],[113.606164,29.666764],[113.663446,29.684443],[113.680692,29.64336],[113.704098,29.634518],[113.73859,29.579363],[113.710257,29.555419],[113.630801,29.523137],[113.677613,29.513763],[113.755221,29.446557],[113.731199,29.393907],[113.674533,29.388172],[113.660982,29.333405],[113.632033,29.316186],[113.609859,29.25146],[113.651743,29.225872],[113.693011,29.226394],[113.691779,29.19662],[113.66283,29.16945],[113.690547,29.114566],[113.696091,29.077437],[113.722576,29.104631],[113.749677,29.060699],[113.775547,29.095219],[113.816199,29.105154],[113.852539,29.058606],[113.882104,29.065407],[113.876561,29.038202],[113.898119,29.029307],[113.94185,29.047097],[113.952321,29.092604],[113.98743,29.126068],[114.034857,29.152204],[114.063191,29.204978],[114.169748,29.216993],[114.252284,29.23475],[114.259059,29.343839],[114.307102,29.365225],[114.341595,29.327665],[114.376088,29.322969],[114.440145,29.341752],[114.466015,29.324013],[114.519602,29.325578],[114.589819,29.352707],[114.621847,29.379828],[114.67297,29.395993],[114.740724,29.386607],[114.759818,29.363139],[114.784455,29.386086],[114.812173,29.383478],[114.866375,29.404335],[114.895325,29.397557],[114.931049,29.422581],[114.947063,29.465317],[114.935977,29.486678],[114.90518,29.473132],[114.918114,29.454374],[114.888549,29.436134],[114.860216,29.476258],[114.900868,29.505951],[114.940288,29.493971],[114.966773,29.522096],[114.947679,29.542924],[115.00065,29.572076],[115.033295,29.546568],[115.087498,29.560104],[115.086266,29.525741],[115.154019,29.510117],[115.157099,29.584568],[115.120142,29.597578],[115.143548,29.645961],[115.117679,29.655843],[115.113367,29.684963],[115.176809,29.654803],[115.250722,29.660003],[115.28583,29.618391],[115.304924,29.637118],[115.355431,29.649602],[115.412714,29.688602],[115.470612,29.739539],[115.479235,29.811224],[115.51188,29.840299],[115.611662,29.841337],[115.667712,29.850161],[115.706517,29.837703],[115.762567,29.793048],[115.837096,29.748373],[115.909777,29.723949],[115.965827,29.724469],[116.049595,29.761881],[116.087167,29.795125],[116.13521,29.819532],[116.128435,29.897904],[116.073616,29.969993],[116.091479,30.036331],[116.078544,30.062233],[116.088399,30.110391],[116.055754,30.180774],[116.065609,30.204569],[115.997856,30.252657],[115.985537,30.290905],[115.903001,30.31364],[115.91532,30.337919],[115.885139,30.379747],[115.921479,30.416397],[115.894994,30.452517],[115.910393,30.519046],[115.887603,30.542758],[115.876516,30.582438],[115.848799,30.602014],[115.819234,30.597893],[115.81369,30.637035],[115.762567,30.685426],[115.782893,30.751795],[115.851262,30.756938],[115.863581,30.815549],[115.848799,30.828397],[115.865429,30.864364],[115.932566,30.889532],[115.976298,30.931636],[116.03974,30.957813],[116.071769,30.956787],[116.058834,31.012711],[116.015102,31.011685],[116.006479,31.034764],[115.938726,31.04707],[115.939958,31.071678],[115.887603,31.10909],[115.867277,31.147512],[115.837712,31.127022],[115.797676,31.128047],[115.778582,31.112164],[115.700973,31.201276],[115.655394,31.211002],[115.603655,31.17363],[115.585793,31.143926],[115.540213,31.194621],[115.539597,31.231985],[115.507568,31.267799],[115.473076,31.265242],[115.443511,31.344498],[115.40717,31.337854],[115.372062,31.349098],[115.393004,31.389977],[115.373909,31.405813],[115.338801,31.40428],[115.301229,31.383846],[115.250722,31.392021],[115.252569,31.421646],[115.211301,31.442072],[115.218077,31.515057],[115.235939,31.555354],[115.212533,31.555354],[115.16449,31.604808],[115.12507,31.599201],[115.106592,31.567592],[115.114599,31.530362],[115.096121,31.508425],[115.022824,31.527811],[114.995107,31.471171],[114.962462,31.494648],[114.884238,31.469129],[114.870071,31.479337],[114.830035,31.45892],[114.789383,31.480358],[114.778912,31.520669],[114.696376,31.525771],[114.641558,31.582378],[114.61692,31.585437],[114.572572,31.553824],[114.560869,31.560963],[114.547935,31.623665],[114.57134,31.660858],[114.586123,31.762172],[114.549783,31.766751],[114.530688,31.742834],[114.443841,31.728074],[114.403189,31.746906],[114.350218,31.755557],[114.292936,31.752503],[114.235654,31.833382],[114.191922,31.852192],[114.134024,31.843042],[114.121705,31.809482],[114.086596,31.782014],[114.017611,31.770822],[113.988662,31.749959],[113.952321,31.793714],[113.957865,31.852701],[113.914749,31.877098],[113.893807,31.847109],[113.854387,31.843042],[113.830981,31.87913],[113.832213,31.918761],[113.805728,31.929428],[113.817431,31.964467],[113.757685,31.98985],[113.791561,32.036028],[113.728735,32.083197],[113.722576,32.12426],[113.750293,32.11615],[113.782322,32.184553],[113.752757,32.215951],[113.73859,32.255942],[113.749061,32.272642],[113.768772,32.30148],[113.753989,32.328286],[113.76754,32.370249],[113.735511,32.410677],[113.700402,32.420782],[113.650511,32.412698],[113.624642,32.36115],[113.511925,32.316654],[113.428773,32.270618],[113.376418,32.298445],[113.353628,32.294904],[113.317904,32.327275],[113.333918,32.336377],[113.2366,32.407141],[113.211962,32.431895],[113.158992,32.410677],[113.155912,32.380863],[113.118956,32.375809],[113.107869,32.398551],[113.078919,32.394508],[113.025949,32.425328],[113.000695,32.41674],[112.992072,32.378336],[112.912,32.390971],[112.888594,32.37682],[112.860877,32.396024],[112.776493,32.358623],[112.735841,32.356095],[112.733993,32.356601],[112.724138,32.358623],[112.716747,32.357612],[112.645298,32.368227],[112.612037,32.386928],[112.589248,32.381369],[112.545516,32.404109],[112.530733,32.37682],[112.477147,32.380863],[112.448814,32.34295],[112.390915,32.37126],[112.360118,32.3657],[112.328089,32.321712],[112.206133,32.392992],[112.172873,32.385412],[112.150083,32.411688],[112.155626,32.377326],[112.081098,32.425833],[112.063851,32.474315],[112.014576,32.450077],[111.975772,32.471791],[111.948671,32.51722],[111.890157,32.503089],[111.858128,32.528826],[111.808853,32.536899],[111.713382,32.606497],[111.646245,32.605993],[111.640701,32.634724],[111.577875,32.593388],[111.530448,32.628172],[111.513202,32.674026],[111.458383,32.726402],[111.475629,32.760127],[111.41342,32.757108],[111.380159,32.829049],[111.293311,32.859217],[111.276065,32.903445],[111.255123,32.883846],[111.242804,32.930573],[111.273601,32.971753],[111.258819,33.006389],[111.221862,33.042517],[111.152877,33.039507],[111.192913,33.071609],[111.179363,33.115229],[111.146102,33.12375],[111.12824,33.15532],[111.08882,33.181871],[111.045704,33.169849]]],[[[109.106111,30.570587],[109.101183,30.579346],[109.09872,30.579346],[109.106111,30.570587]]],[[[111.046936,33.202905],[111.035849,33.187881],[111.034001,33.177864],[111.045704,33.169849],[111.046936,33.202905]]],[[[112.716747,32.357612],[112.735841,32.356095],[112.733993,32.356601],[112.724138,32.358623],[112.716747,32.357612]]],[[[112.902145,29.79149],[112.894138,29.783699],[112.923703,29.766557],[112.929246,29.77383],[112.902145,29.79149]]]]}},
    {"type":"Feature","properties":{"adcode":430000,"name":"湖南省","center":[112.982279,28.19409],"centroid":[111.711649,27.629216],"childrenNum":14,"level":"province","parent":{"adcode":100000},"subFeatureIndex":17,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[112.024431,24.740308],[112.03367,24.771286],[112.124214,24.841364],[112.149467,24.837019],[112.167329,24.859828],[112.175337,24.927685],[112.119902,24.963499],[112.12175,24.989538],[112.155626,25.026419],[112.151931,25.055698],[112.177184,25.106649],[112.187039,25.182494],[112.246785,25.185202],[112.256025,25.159204],[112.302836,25.157037],[112.315771,25.175453],[112.365046,25.191701],[112.414937,25.14241],[112.44327,25.185744],[112.458053,25.152162],[112.562762,25.124531],[112.628052,25.140785],[112.660081,25.132658],[112.712436,25.083344],[112.714899,25.025876],[112.742001,24.99876],[112.743233,24.959701],[112.778341,24.947764],[112.780805,24.896747],[112.873812,24.896747],[112.904609,24.921715],[112.941565,24.915745],[112.994536,24.927142],[113.009934,24.977604],[112.979137,25.03401],[113.004391,25.089306],[112.96805,25.141869],[112.97421,25.168412],[113.034572,25.198199],[112.992688,25.247467],[112.958195,25.254503],[112.897833,25.238264],[112.867036,25.249632],[112.854718,25.337829],[112.891058,25.339993],[112.924319,25.296714],[112.93479,25.325929],[112.969898,25.350269],[113.013014,25.352432],[113.078304,25.382174],[113.096782,25.412449],[113.131274,25.414611],[113.11834,25.445418],[113.176854,25.471355],[113.226129,25.50971],[113.248919,25.514031],[113.311129,25.490264],[113.314208,25.442716],[113.341926,25.448661],[113.373338,25.402719],[113.407215,25.401637],[113.449715,25.359463],[113.479896,25.375145],[113.535946,25.368656],[113.579062,25.34432],[113.584606,25.306453],[113.611707,25.327552],[113.680076,25.334584],[113.686852,25.351891],[113.753373,25.362707],[113.76446,25.333502],[113.814967,25.328634],[113.839605,25.363248],[113.877177,25.380552],[113.887032,25.436772],[113.94493,25.441635],[113.962792,25.528072],[113.986198,25.529153],[113.983118,25.599336],[113.957249,25.611749],[113.913517,25.701299],[113.920293,25.741197],[113.961561,25.77731],[113.971416,25.836036],[114.028082,25.893119],[114.028082,25.98138],[114.008372,26.015806],[114.044096,26.076564],[114.087828,26.06635],[114.121089,26.085702],[114.10569,26.097526],[114.188842,26.121172],[114.237501,26.152333],[114.216559,26.203355],[114.181451,26.214631],[114.102611,26.187783],[114.088444,26.168448],[114.013299,26.184023],[113.962792,26.150722],[113.949242,26.192616],[113.972647,26.20604],[113.978807,26.237716],[114.029314,26.266163],[114.021307,26.288701],[114.047792,26.337518],[114.030546,26.376664],[114.062575,26.406149],[114.085364,26.406149],[114.090292,26.455988],[114.110002,26.482775],[114.07243,26.480096],[114.10877,26.56952],[114.019459,26.587182],[113.996669,26.615543],[113.912901,26.613938],[113.860546,26.664221],[113.853771,26.769532],[113.835909,26.806394],[113.877177,26.859262],[113.890112,26.895562],[113.927068,26.948922],[113.892575,26.964925],[113.86301,27.018252],[113.824206,27.036378],[113.803264,27.099261],[113.771851,27.096598],[113.779242,27.137081],[113.846996,27.222262],[113.872865,27.289828],[113.854387,27.30525],[113.872865,27.346721],[113.872865,27.384988],[113.72812,27.350442],[113.699786,27.331836],[113.657902,27.347253],[113.616635,27.345658],[113.605548,27.38924],[113.632033,27.40518],[113.59754,27.428554],[113.591381,27.467855],[113.627105,27.49971],[113.583374,27.524657],[113.579062,27.545354],[113.608627,27.585143],[113.607395,27.625449],[113.652359,27.663619],[113.696707,27.71979],[113.69917,27.740979],[113.763228,27.799228],[113.756453,27.860091],[113.72812,27.874904],[113.752141,27.93361],[113.822974,27.982243],[113.845148,27.971672],[113.864242,28.004966],[113.914133,27.991227],[113.936307,28.018703],[113.966488,28.017646],[113.970184,28.041418],[114.025618,28.031382],[114.047176,28.057263],[114.025002,28.080499],[113.992357,28.161255],[114.012068,28.174972],[114.068734,28.171806],[114.107538,28.182885],[114.109386,28.205038],[114.143879,28.246694],[114.182067,28.249858],[114.198081,28.29097],[114.2529,28.319423],[114.252284,28.395787],[114.214712,28.403157],[114.172212,28.432632],[114.217175,28.466308],[114.218407,28.48472],[114.15435,28.507337],[114.138335,28.533629],[114.08598,28.558337],[114.132176,28.607211],[114.122321,28.623497],[114.157429,28.761566],[114.137719,28.779926],[114.153734,28.829221],[114.124784,28.843376],[114.076741,28.834464],[114.056415,28.872204],[114.060111,28.902596],[114.028082,28.891069],[114.005292,28.917788],[114.008988,28.955498],[113.973879,28.937692],[113.955401,28.978536],[113.961561,28.999476],[113.94185,29.047097],[113.898119,29.029307],[113.876561,29.038202],[113.882104,29.065407],[113.852539,29.058606],[113.816199,29.105154],[113.775547,29.095219],[113.749677,29.060699],[113.722576,29.104631],[113.696091,29.077437],[113.690547,29.114566],[113.66283,29.16945],[113.691779,29.19662],[113.693011,29.226394],[113.651743,29.225872],[113.609859,29.25146],[113.632033,29.316186],[113.660982,29.333405],[113.674533,29.388172],[113.731199,29.393907],[113.755221,29.446557],[113.677613,29.513763],[113.630801,29.523137],[113.710257,29.555419],[113.73859,29.579363],[113.704098,29.634518],[113.680692,29.64336],[113.663446,29.684443],[113.606164,29.666764],[113.547033,29.675603],[113.540258,29.699519],[113.558736,29.727067],[113.550729,29.768115],[113.575367,29.809147],[113.571671,29.849123],[113.37765,29.703158],[113.277252,29.594976],[113.222433,29.543965],[113.181781,29.485636],[113.145441,29.449163],[113.099861,29.459585],[113.078304,29.438218],[113.057362,29.522616],[113.034572,29.523658],[112.950188,29.473132],[112.912,29.606944],[112.915696,29.620992],[113.005007,29.693801],[113.025949,29.772791],[112.974826,29.732784],[112.944645,29.682883],[112.926782,29.692241],[112.923703,29.766557],[112.894138,29.783699],[112.861493,29.78318],[112.79374,29.735902],[112.788812,29.681323],[112.733378,29.645441],[112.714283,29.648561],[112.693957,29.601741],[112.650842,29.592374],[112.640371,29.607985],[112.572001,29.624113],[112.54182,29.60122],[112.499321,29.629316],[112.439574,29.633997],[112.424792,29.598619],[112.368741,29.541362],[112.333017,29.545007],[112.291133,29.517409],[112.281278,29.536676],[112.303452,29.585609],[112.233851,29.61631],[112.244322,29.659483],[112.202438,29.633997],[112.178416,29.656883],[112.111279,29.659483],[112.089721,29.685482],[112.065699,29.681323],[112.07617,29.743696],[112.008417,29.778505],[111.95483,29.796683],[111.965917,29.832512],[111.925881,29.836665],[111.899396,29.855871],[111.899396,29.855871],[111.861207,29.856909],[111.8107,29.901017],[111.75773,29.92021],[111.723853,29.909317],[111.723853,29.909317],[111.705375,29.890121],[111.669034,29.888565],[111.669034,29.888565],[111.553854,29.894272],[111.527368,29.925916],[111.475629,29.918654],[111.436825,29.930065],[111.394325,29.912948],[111.382623,29.95029],[111.342587,29.944586],[111.3315,29.970512],[111.266826,30.01146],[111.242188,30.040476],[111.031537,30.048765],[110.929907,30.063268],[110.924364,30.111426],[110.851067,30.126439],[110.746973,30.112979],[110.756212,30.054463],[110.712481,30.033223],[110.650887,30.07777],[110.600996,30.054463],[110.531394,30.061197],[110.497518,30.055499],[110.491358,30.019751],[110.557264,29.988137],[110.517228,29.961179],[110.49875,29.91243],[110.538786,29.895828],[110.549873,29.848085],[110.60038,29.839779],[110.642879,29.775907],[110.562807,29.712515],[110.507373,29.692241],[110.467337,29.713034],[110.447011,29.664684],[110.372482,29.633477],[110.339221,29.668324],[110.302265,29.661563],[110.289946,29.6964],[110.219729,29.746814],[110.160599,29.753569],[110.113788,29.789932],[110.02386,29.769674],[109.941325,29.774349],[109.908064,29.763959],[109.869876,29.774869],[109.779333,29.757725],[109.755311,29.733304],[109.760238,29.689122],[109.714659,29.673524],[109.701108,29.636078],[109.717739,29.615269],[109.664768,29.599659],[109.651833,29.625674],[109.578536,29.629836],[109.558826,29.606944],[109.516326,29.626194],[109.488609,29.553336],[109.467051,29.560104],[109.458428,29.513242],[109.433791,29.530948],[109.436254,29.488761],[109.415928,29.497617],[109.418392,29.453332],[109.368501,29.413719],[109.391291,29.372005],[109.343863,29.369398],[109.352487,29.284872],[109.312451,29.25146],[109.257632,29.222738],[109.275494,29.202366],[109.261328,29.161089],[109.274262,29.121885],[109.232378,29.119271],[109.240386,29.086328],[109.312451,29.066453],[109.319842,29.042388],[109.294588,29.015177],[109.292741,28.987436],[109.261328,28.952356],[109.235458,28.882161],[109.246545,28.80143],[109.241002,28.776779],[109.2989,28.7474],[109.294588,28.722211],[109.252704,28.691767],[109.271183,28.671816],[109.192958,28.636104],[109.201581,28.597753],[109.235458,28.61982],[109.252089,28.606685],[109.306907,28.62087],[109.319842,28.579886],[109.273646,28.53836],[109.274262,28.494714],[109.260712,28.46473],[109.264407,28.392628],[109.289045,28.373673],[109.268719,28.33786],[109.275494,28.313101],[109.317994,28.277795],[109.33524,28.293605],[109.388211,28.268307],[109.367885,28.254602],[109.340168,28.19027],[109.33832,28.141731],[109.314298,28.103729],[109.298284,28.036136],[109.335856,28.063073],[109.378972,28.034551],[109.362342,28.007608],[109.319842,27.988585],[109.30198,27.956343],[109.32169,27.868027],[109.346943,27.838396],[109.332777,27.782815],[109.37774,27.736741],[109.366653,27.721909],[109.414081,27.725087],[109.470747,27.680049],[109.45658,27.673689],[109.470131,27.62863],[109.451037,27.586204],[109.461508,27.567637],[109.404841,27.55066],[109.303211,27.47582],[109.300132,27.423774],[109.245313,27.41793],[109.202197,27.450331],[109.167089,27.41793],[109.141835,27.448207],[109.142451,27.418461],[109.103647,27.336621],[109.044517,27.331304],[109.053756,27.293551],[108.983539,27.26802],[108.963213,27.235565],[108.907778,27.204699],[108.926873,27.160512],[108.878829,27.106187],[108.79075,27.084343],[108.877597,27.01612],[108.942887,27.017186],[108.942887,27.017186],[108.940423,27.044907],[109.007561,27.08008],[109.032814,27.104056],[109.128901,27.122701],[109.101183,27.06889],[109.165857,27.066758],[109.21698,27.114711],[109.239154,27.14933],[109.264407,27.131755],[109.33524,27.139212],[109.358646,27.153058],[109.415312,27.154123],[109.441182,27.117907],[109.472595,27.134951],[109.454733,27.069423],[109.486761,27.053968],[109.497848,27.079548],[109.520022,27.058764],[109.555131,26.946788],[109.436254,26.892359],[109.452885,26.861932],[109.486761,26.895562],[109.509551,26.877947],[109.513247,26.84004],[109.497232,26.815474],[109.522486,26.749226],[109.528645,26.743881],[109.554515,26.73533],[109.597015,26.756173],[109.568065,26.726243],[109.528645,26.743881],[109.52187,26.749226],[109.486761,26.759913],[109.447957,26.759913],[109.407305,26.719829],[109.35495,26.693098],[109.283501,26.698445],[109.306291,26.661012],[109.334008,26.646036],[109.35495,26.658873],[109.390675,26.598955],[109.407305,26.533116],[109.381436,26.518659],[109.385747,26.493487],[109.362342,26.472061],[109.38082,26.454381],[109.319842,26.418477],[109.29582,26.350389],[109.271183,26.327863],[109.285965,26.295676],[109.325385,26.29031],[109.351255,26.264016],[109.369733,26.277432],[109.442414,26.289774],[109.467051,26.313917],[109.439334,26.238789],[109.47629,26.148035],[109.513863,26.128157],[109.502776,26.096451],[109.449805,26.101826],[109.452885,26.055598],[109.48245,26.029788],[109.513247,25.998056],[109.560058,26.021184],[109.588391,26.019571],[109.635203,26.047533],[109.649369,26.016882],[109.730057,25.989988],[109.710963,25.954478],[109.693717,25.959321],[109.67955,25.921649],[109.685094,25.880197],[109.768246,25.890427],[109.779333,25.866196],[109.811361,25.877504],[109.826144,25.911422],[109.806434,25.973848],[109.782412,25.996981],[109.814441,26.041081],[109.864332,26.027637],[109.898825,26.095377],[109.904368,26.135679],[109.970274,26.195301],[110.03002,26.166299],[110.099005,26.168985],[110.100853,26.132455],[110.065128,26.050221],[110.100853,26.020108],[110.168606,26.028713],[110.181541,26.060437],[110.24991,26.010965],[110.257301,25.961473],[110.325671,25.975462],[110.373098,26.088927],[110.437772,26.153945],[110.477808,26.179727],[110.495054,26.166299],[110.546793,26.233421],[110.552952,26.283335],[110.584365,26.296749],[110.612083,26.333764],[110.643495,26.308552],[110.673676,26.317135],[110.721104,26.294066],[110.742046,26.313917],[110.73527,26.270993],[110.759292,26.248451],[110.836284,26.255966],[110.939762,26.286554],[110.926212,26.320354],[110.944074,26.326791],[110.94469,26.373447],[110.974255,26.385778],[111.008747,26.35897],[111.008132,26.336982],[111.090667,26.308016],[111.208928,26.30426],[111.204616,26.276359],[111.228022,26.261333],[111.277913,26.272066],[111.293311,26.222148],[111.271754,26.217316],[111.274833,26.183486],[111.258203,26.151796],[111.26621,26.095914],[111.244652,26.078177],[111.267442,26.058824],[111.235413,26.048071],[111.189834,25.953402],[111.230486,25.916267],[111.251428,25.864581],[111.29208,25.854349],[111.297007,25.874274],[111.346282,25.906577],[111.376463,25.906039],[111.383239,25.881812],[111.460231,25.885042],[111.4861,25.859196],[111.43313,25.84627],[111.442369,25.77192],[111.399869,25.744431],[111.30871,25.720171],[111.309942,25.645203],[111.343202,25.602574],[111.324724,25.564249],[111.32842,25.521592],[111.279145,25.42326],[111.210776,25.363248],[111.184906,25.367034],[111.138711,25.303748],[111.103602,25.285351],[111.112841,25.21715],[110.998892,25.161371],[110.98411,25.101772],[110.951465,25.04377],[110.968711,24.975434],[111.009363,24.921172],[111.100522,24.945593],[111.101754,25.035095],[111.139943,25.042144],[111.200921,25.074672],[111.221862,25.106649],[111.274833,25.151078],[111.321645,25.105023],[111.36784,25.108817],[111.375231,25.128324],[111.435593,25.093642],[111.416499,25.047566],[111.467622,25.02208],[111.460231,24.992793],[111.43313,24.979774],[111.434977,24.951562],[111.470086,24.92877],[111.447296,24.892947],[111.449144,24.857113],[111.479325,24.797366],[111.461463,24.728894],[111.431282,24.687574],[111.451608,24.665822],[111.499035,24.667997],[111.526752,24.637538],[111.570484,24.64461],[111.588962,24.690837],[111.641933,24.684856],[111.637621,24.715303],[111.666571,24.760961],[111.708455,24.788673],[111.783599,24.785957],[111.814396,24.770199],[111.868599,24.771829],[111.875374,24.756613],[111.929577,24.75607],[111.951135,24.769655],[112.024431,24.740308]]],[[[109.528645,26.743881],[109.522486,26.749226],[109.52187,26.749226],[109.528645,26.743881]]]]}},
    {"type":"Feature","properties":{"adcode":440000,"name":"广东省","center":[113.280637,23.125178],"centroid":[113.429919,23.334643],"childrenNum":21,"level":"province","parent":{"adcode":100000},"subFeatureIndex":18,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[113.558736,22.212244],[113.594461,22.228864],[113.595693,22.304186],[113.617866,22.315259],[113.604932,22.339617],[113.627721,22.349027],[113.669605,22.416539],[113.66591,22.438667],[113.624642,22.443092],[113.608627,22.408793],[113.573519,22.41156],[113.631417,22.475723],[113.668373,22.4807],[113.691779,22.514981],[113.740438,22.534329],[113.717033,22.645391],[113.678228,22.726007],[113.733663,22.736494],[113.758301,22.683496],[113.765692,22.665825],[113.803264,22.593463],[113.856851,22.539857],[113.869786,22.459685],[113.893807,22.442539],[113.952937,22.486783],[113.954785,22.491206],[113.976343,22.510558],[114.031778,22.503923],[114.082285,22.512216],[114.095219,22.534329],[114.156813,22.543726],[114.166052,22.559201],[114.222719,22.553122],[114.232574,22.539857],[114.294784,22.563623],[114.321885,22.587385],[114.381631,22.60175],[114.427211,22.589042],[114.472174,22.522168],[114.476486,22.459132],[114.506667,22.438667],[114.549167,22.465769],[114.611377,22.481806],[114.628623,22.513875],[114.614456,22.545384],[114.568261,22.560859],[114.559022,22.583517],[114.603369,22.638763],[114.579964,22.661407],[114.51529,22.655332],[114.567029,22.685705],[114.591666,22.690122],[114.601521,22.730975],[114.689601,22.7674],[114.709927,22.787817],[114.749963,22.764089],[114.73518,22.724351],[114.728405,22.651466],[114.743803,22.632687],[114.746267,22.581859],[114.866375,22.591805],[114.88547,22.538751],[114.922426,22.549253],[114.927969,22.621639],[114.945216,22.645391],[115.039454,22.713862],[115.02344,22.726007],[115.053621,22.747533],[115.076411,22.788368],[115.154635,22.80161],[115.190975,22.77347],[115.190359,22.818711],[115.236555,22.82533],[115.230396,22.776781],[115.319091,22.783402],[115.338185,22.776781],[115.349272,22.712206],[115.381301,22.684048],[115.430576,22.684048],[115.471844,22.697852],[115.575322,22.650914],[115.565467,22.684048],[115.609198,22.753052],[115.541445,22.755259],[115.570394,22.786713],[115.583945,22.82864],[115.654162,22.865591],[115.696046,22.84298],[115.760103,22.834707],[115.788437,22.809885],[115.796444,22.739254],[115.829089,22.734838],[115.883291,22.78561],[115.931334,22.802713],[115.965211,22.800506],[115.99724,22.826985],[116.05637,22.844635],[116.104413,22.816505],[116.14137,22.835259],[116.239304,22.921275],[116.259014,22.932298],[116.302746,22.951588],[116.382818,22.91907],[116.449955,22.936707],[116.50539,22.930645],[116.544194,22.996769],[116.576839,23.014397],[116.557129,23.056253],[116.566368,23.088738],[116.550969,23.109656],[116.566368,23.134424],[116.665534,23.158086],[116.701259,23.198248],[116.74499,23.215299],[116.806584,23.200998],[116.821367,23.240597],[116.798577,23.244996],[116.782563,23.313714],[116.871874,23.4159],[116.871258,23.416449],[116.874338,23.447199],[116.874953,23.447748],[116.895895,23.476295],[116.888504,23.501543],[116.92854,23.530079],[116.963649,23.507031],[117.01046,23.502641],[117.044953,23.539955],[117.085605,23.536663],[117.192778,23.5619],[117.192778,23.629356],[117.147199,23.654027],[117.123793,23.647448],[117.055424,23.694038],[117.048032,23.758687],[117.019083,23.801952],[117.012308,23.855054],[116.981511,23.855602],[116.955642,23.922359],[116.976583,23.931659],[116.981511,23.999471],[116.953178,24.008218],[116.930388,24.064514],[116.9347,24.126794],[116.998757,24.179217],[116.956257,24.216883],[116.933468,24.220157],[116.938395,24.28127],[116.914374,24.287817],[116.919301,24.321087],[116.895895,24.350533],[116.903903,24.369614],[116.839229,24.442097],[116.860787,24.460075],[116.83307,24.496568],[116.796729,24.502014],[116.759157,24.545572],[116.761005,24.583128],[116.815207,24.654944],[116.777635,24.679418],[116.667382,24.658752],[116.623034,24.64189],[116.600861,24.654401],[116.570679,24.621762],[116.530027,24.604895],[116.506622,24.621218],[116.517709,24.652225],[116.485064,24.720196],[116.44626,24.714216],[116.416079,24.744113],[116.419158,24.767482],[116.375427,24.803885],[116.381586,24.82507],[116.417927,24.840821],[116.395137,24.877746],[116.363724,24.87123],[116.345862,24.828872],[116.297202,24.801712],[116.244232,24.793563],[116.251007,24.82507],[116.221442,24.829959],[116.191877,24.877203],[116.153073,24.846795],[116.068073,24.850053],[116.015102,24.905975],[115.985537,24.899461],[115.907929,24.923343],[115.89253,24.936911],[115.885139,24.898918],[115.907313,24.879917],[115.861733,24.863629],[115.863581,24.891318],[115.824161,24.909232],[115.807531,24.862543],[115.790284,24.856027],[115.764415,24.791933],[115.776734,24.774546],[115.756408,24.749004],[115.769342,24.708236],[115.801371,24.705517],[115.780429,24.663103],[115.797676,24.628834],[115.840791,24.584217],[115.843871,24.562446],[115.785357,24.567345],[115.752712,24.546116],[115.68927,24.545027],[115.671408,24.604895],[115.605503,24.62557],[115.569778,24.622306],[115.555611,24.683768],[115.522967,24.702799],[115.476771,24.762591],[115.412714,24.79302],[115.372678,24.774546],[115.358511,24.735416],[115.306772,24.758787],[115.269816,24.749548],[115.258729,24.728894],[115.1842,24.711498],[115.104744,24.667997],[115.083802,24.699537],[115.057317,24.703343],[115.024672,24.669085],[115.00373,24.679418],[114.940288,24.650049],[114.909491,24.661471],[114.893477,24.582584],[114.868839,24.562446],[114.846665,24.602719],[114.827571,24.588026],[114.781376,24.613057],[114.729637,24.608704],[114.73826,24.565168],[114.704999,24.525973],[114.664963,24.583673],[114.627391,24.576598],[114.589819,24.537406],[114.534384,24.559181],[114.429058,24.48622],[114.403189,24.497657],[114.391486,24.563535],[114.363769,24.582584],[114.300943,24.578775],[114.289856,24.619042],[114.258443,24.641346],[114.19069,24.656576],[114.169132,24.689749],[114.27261,24.700624],[114.281849,24.724001],[114.336052,24.749004],[114.342211,24.807145],[114.378551,24.861457],[114.403189,24.877746],[114.395798,24.951019],[114.454928,24.977062],[114.45616,24.99659],[114.506051,24.999844],[114.532536,25.022623],[114.561485,25.077382],[114.604601,25.083886],[114.640326,25.074129],[114.664963,25.10123],[114.735796,25.121822],[114.73518,25.155954],[114.685905,25.173287],[114.693912,25.213902],[114.73518,25.225813],[114.743188,25.274528],[114.714238,25.315651],[114.63663,25.324306],[114.599674,25.385959],[114.541159,25.416773],[114.477718,25.37136],[114.438914,25.376226],[114.43029,25.343779],[114.382863,25.317274],[114.31511,25.33837],[114.2954,25.299961],[114.260291,25.291845],[114.204857,25.29942],[114.190074,25.316733],[114.115545,25.302125],[114.083517,25.275611],[114.055799,25.277775],[114.039785,25.250714],[114.017611,25.273987],[114.029314,25.328093],[114.050256,25.36433],[113.983118,25.415152],[114.003444,25.442716],[113.94493,25.441635],[113.887032,25.436772],[113.877177,25.380552],[113.839605,25.363248],[113.814967,25.328634],[113.76446,25.333502],[113.753373,25.362707],[113.686852,25.351891],[113.680076,25.334584],[113.611707,25.327552],[113.584606,25.306453],[113.579062,25.34432],[113.535946,25.368656],[113.479896,25.375145],[113.449715,25.359463],[113.407215,25.401637],[113.373338,25.402719],[113.341926,25.448661],[113.314208,25.442716],[113.311129,25.490264],[113.248919,25.514031],[113.226129,25.50971],[113.176854,25.471355],[113.11834,25.445418],[113.131274,25.414611],[113.096782,25.412449],[113.078304,25.382174],[113.013014,25.352432],[112.969898,25.350269],[112.93479,25.325929],[112.924319,25.296714],[112.891058,25.339993],[112.854718,25.337829],[112.867036,25.249632],[112.897833,25.238264],[112.958195,25.254503],[112.992688,25.247467],[113.034572,25.198199],[112.97421,25.168412],[112.96805,25.141869],[113.004391,25.089306],[112.979137,25.03401],[113.009934,24.977604],[112.994536,24.927142],[112.941565,24.915745],[112.904609,24.921715],[112.873812,24.896747],[112.780805,24.896747],[112.778341,24.947764],[112.743233,24.959701],[112.742001,24.99876],[112.714899,25.025876],[112.712436,25.083344],[112.660081,25.132658],[112.628052,25.140785],[112.562762,25.124531],[112.458053,25.152162],[112.44327,25.185744],[112.414937,25.14241],[112.365046,25.191701],[112.315771,25.175453],[112.302836,25.157037],[112.256025,25.159204],[112.246785,25.185202],[112.187039,25.182494],[112.177184,25.106649],[112.151931,25.055698],[112.155626,25.026419],[112.12175,24.989538],[112.119902,24.963499],[112.175337,24.927685],[112.167329,24.859828],[112.149467,24.837019],[112.124214,24.841364],[112.03367,24.771286],[112.024431,24.740308],[111.961606,24.721283],[111.939432,24.686487],[111.953598,24.64733],[111.927729,24.629378],[111.936968,24.595645],[111.972077,24.578775],[112.007185,24.534684],[112.009649,24.503103],[111.985011,24.467701],[112.025047,24.438828],[112.057692,24.387057],[112.05954,24.339628],[112.026279,24.294908],[111.990555,24.279634],[111.986243,24.25672],[111.958526,24.263813],[111.912946,24.221795],[111.877222,24.227252],[111.871062,24.176487],[111.886461,24.163929],[111.878454,24.109862],[111.92157,24.012045],[111.940664,23.987989],[111.911714,23.943693],[111.854432,23.947521],[111.845809,23.904305],[111.812548,23.887343],[111.824867,23.832612],[111.8107,23.80688],[111.722621,23.823305],[111.683201,23.822758],[111.683201,23.822758],[111.654868,23.833159],[111.627766,23.78881],[111.621607,23.725819],[111.666571,23.718696],[111.614832,23.65896],[111.615448,23.639225],[111.555702,23.64087],[111.487332,23.626615],[111.479941,23.532822],[111.428818,23.466414],[111.399869,23.469159],[111.383239,23.399423],[111.389398,23.375804],[111.363528,23.340641],[111.376463,23.30437],[111.353058,23.284582],[111.36476,23.240047],[111.388782,23.210349],[111.38447,23.16744],[111.365992,23.14488],[111.377695,23.082132],[111.402333,23.066165],[111.43313,23.073322],[111.433746,23.036428],[111.389398,23.005583],[111.403565,22.99126],[111.362913,22.967568],[111.374615,22.938361],[111.358601,22.889301],[111.218167,22.748085],[111.185522,22.735942],[111.118385,22.744773],[111.058023,22.729871],[111.089435,22.695643],[111.055559,22.648705],[110.997045,22.631582],[110.958856,22.636553],[110.950233,22.61059],[110.896031,22.613352],[110.897878,22.591805],[110.812263,22.576333],[110.778386,22.585174],[110.749437,22.556991],[110.762988,22.518298],[110.740198,22.498947],[110.74143,22.464109],[110.688459,22.477935],[110.712481,22.440879],[110.711249,22.369506],[110.74143,22.361757],[110.749437,22.329653],[110.787009,22.28259],[110.759292,22.274837],[110.725415,22.29588],[110.687843,22.249914],[110.646575,22.220554],[110.678604,22.172901],[110.629329,22.149068],[110.598532,22.162924],[110.602843,22.18343],[110.55788,22.196175],[110.505525,22.14297],[110.456866,22.189526],[110.414366,22.208365],[110.378026,22.164587],[110.34846,22.195621],[110.326287,22.152393],[110.364475,22.125785],[110.35154,22.097508],[110.359547,22.015973],[110.352772,21.97602],[110.374946,21.967695],[110.374946,21.967695],[110.378642,21.939942],[110.378642,21.939942],[110.391576,21.89386],[110.337374,21.887751],[110.290562,21.917736],[110.283787,21.892194],[110.224041,21.882198],[110.224041,21.882198],[110.212338,21.886085],[110.212338,21.886085],[110.196323,21.899968],[110.12857,21.902744],[110.101469,21.86998],[110.050962,21.857205],[109.999839,21.881643],[109.94502,21.84443],[109.940093,21.769419],[109.916071,21.668787],[109.888354,21.652101],[109.888354,21.652101],[109.839695,21.636525],[109.786108,21.637638],[109.778101,21.670455],[109.742992,21.616497],[109.754695,21.556396],[109.788572,21.490702],[109.785492,21.45673],[109.819369,21.445033],[109.894513,21.442248],[109.904368,21.429992],[109.868644,21.365913],[109.770709,21.359783],[109.757775,21.346963],[109.763934,21.226514],[109.674623,21.136671],[109.674007,21.067997],[109.655529,20.929435],[109.664768,20.862343],[109.711579,20.774519],[109.730057,20.719673],[109.74484,20.621124],[109.793499,20.615522],[109.813825,20.574627],[109.811977,20.541566],[109.839695,20.489439],[109.888354,20.475423],[109.895745,20.42776],[109.864948,20.40196],[109.861252,20.376717],[109.916071,20.316677],[109.909296,20.236961],[109.929006,20.211691],[109.993679,20.254368],[110.082375,20.258859],[110.118099,20.219553],[110.168606,20.219553],[110.220345,20.25156],[110.296722,20.249314],[110.349076,20.258859],[110.384185,20.293103],[110.425453,20.291419],[110.452554,20.311064],[110.491358,20.373912],[110.54125,20.42047],[110.550489,20.47262],[110.499982,20.572386],[110.487047,20.640167],[110.466105,20.680485],[110.411286,20.670966],[110.392192,20.682724],[110.407591,20.731987],[110.393424,20.816479],[110.350924,20.84165],[110.327519,20.847802],[110.269004,20.839972],[110.209874,20.860106],[110.184005,20.891979],[110.180925,20.98197],[110.204947,21.003202],[110.208642,21.050684],[110.241903,21.016051],[110.24991,21.045098],[110.296722,21.093684],[110.39096,21.124949],[110.422373,21.190807],[110.451322,21.186343],[110.501213,21.217588],[110.534474,21.204198],[110.626249,21.215915],[110.65951,21.239902],[110.713097,21.3124],[110.768531,21.364799],[110.796248,21.37483],[110.888639,21.367585],[110.929291,21.375945],[111.034617,21.438906],[111.103602,21.455616],[111.171355,21.458401],[111.28284,21.485691],[111.276065,21.443362],[111.250196,21.45116],[111.257587,21.41495],[111.28592,21.41885],[111.353058,21.464528],[111.382623,21.495714],[111.444217,21.514088],[111.494724,21.501282],[111.521825,21.517429],[111.560629,21.50518],[111.609904,21.530234],[111.650556,21.512418],[111.677658,21.529677],[111.693672,21.590345],[111.736788,21.609821],[111.794686,21.61149],[111.832258,21.578659],[111.810084,21.555283],[111.887693,21.578659],[111.941896,21.607039],[111.972692,21.603144],[112.026895,21.633744],[111.997946,21.657107],[111.954214,21.667674],[111.956062,21.710494],[112.036134,21.761637],[112.136532,21.793871],[112.192583,21.789425],[112.196894,21.736624],[112.236315,21.727173],[112.238778,21.702153],[112.353343,21.707157],[112.415553,21.734956],[112.427256,21.789981],[112.445734,21.803317],[112.497473,21.785535],[112.535661,21.753856],[112.647146,21.758302],[112.68595,21.810541],[112.792508,21.921067],[112.841167,21.920512],[112.893522,21.84443],[112.929862,21.838875],[112.989608,21.869424],[113.047507,21.956595],[113.053666,22.012089],[113.032108,22.04593],[113.045659,22.088636],[113.086927,22.12634],[113.091854,22.065344],[113.142977,22.012089],[113.1516,21.979905],[113.235368,21.887751],[113.266781,21.871646],[113.319752,21.909407],[113.330223,21.96159],[113.442324,22.009315],[113.45957,22.043711],[113.527939,22.073663],[113.567359,22.075327],[113.554425,22.107489],[113.554425,22.142416],[113.534715,22.174009],[113.53841,22.209473],[113.558736,22.212244]]],[[[117.024627,23.437865],[116.982743,23.460924],[116.944555,23.440061],[116.951946,23.419744],[117.027091,23.41535],[117.050496,23.400522],[117.081909,23.409309],[117.124409,23.389537],[117.142887,23.400522],[117.142887,23.459826],[117.129336,23.483431],[117.093612,23.459277],[117.058503,23.47355],[117.029554,23.443356],[117.024627,23.437865]]],[[[112.853486,21.740515],[112.876275,21.772753],[112.840551,21.776644],[112.782653,21.739959],[112.724138,21.719945],[112.70566,21.679354],[112.734609,21.666562],[112.780189,21.671568],[112.730914,21.613715],[112.775261,21.564189],[112.817145,21.590345],[112.798667,21.610933],[112.821457,21.655994],[112.804826,21.686583],[112.83316,21.736624],[112.853486,21.740515]]],[[[112.530733,21.583667],[112.563378,21.591458],[112.571385,21.619835],[112.621277,21.606482],[112.665624,21.642644],[112.639139,21.67268],[112.66624,21.683803],[112.663776,21.714386],[112.592327,21.693256],[112.560299,21.666562],[112.57077,21.645982],[112.535045,21.628737],[112.530733,21.583667]]],[[[114.231342,22.016528],[114.311414,22.041493],[114.302791,22.050368],[114.239965,22.03539],[114.231342,22.016528]]],[[[110.43346,21.171276],[110.489511,21.138904],[110.508605,21.140579],[110.544945,21.083633],[110.582517,21.094801],[110.632409,21.210893],[110.589293,21.194713],[110.525235,21.190249],[110.499366,21.213125],[110.445163,21.184669],[110.431612,21.180763],[110.43346,21.171276]]],[[[112.435263,21.663781],[112.456205,21.648763],[112.458669,21.68992],[112.435263,21.663781]]],[[[110.517844,21.079166],[110.459946,21.062971],[110.398352,21.096476],[110.352772,21.079724],[110.305961,21.0881],[110.27578,21.033369],[110.211106,20.986999],[110.201251,20.938378],[110.309656,20.963529],[110.347845,20.984763],[110.407591,20.990351],[110.47288,20.983087],[110.511684,20.916578],[110.535706,20.922727],[110.539402,20.987557],[110.560344,21.061295],[110.517844,21.079166]]],[[[113.765076,21.962145],[113.774315,21.998218],[113.74167,21.991559],[113.765076,21.962145]]],[[[113.723192,21.922177],[113.742902,21.950489],[113.71888,21.951599],[113.723192,21.922177]]],[[[113.142977,21.831653],[113.162071,21.853873],[113.203955,21.861093],[113.167615,21.876644],[113.136818,21.868869],[113.142977,21.831653]]],[[[113.819894,22.396068],[113.813735,22.419858],[113.786634,22.413773],[113.819894,22.396068]]],[[[114.190074,21.986564],[114.229494,21.995443],[114.180835,22.00987],[114.190074,21.986564]]],[[[114.153734,21.97491],[114.171596,22.000437],[114.124169,21.985455],[114.153734,21.97491]]],[[[116.769628,20.771721],[116.761005,20.750456],[116.87249,20.738143],[116.889736,20.683284],[116.849084,20.628405],[116.749302,20.600958],[116.796113,20.582471],[116.862635,20.588633],[116.905135,20.619443],[116.934084,20.676565],[116.925461,20.726949],[116.88604,20.775638],[116.820135,20.780674],[116.769628,20.771721]]],[[[113.025333,21.847762],[113.045659,21.882753],[113.007471,21.869424],[113.025333,21.847762]]],[[[110.405127,20.678245],[110.437772,20.677685],[110.414366,20.710157],[110.405127,20.678245]]],[[[110.644727,20.935584],[110.584365,20.948998],[110.548641,20.908752],[110.562807,20.861224],[110.611467,20.860106],[110.646575,20.917137],[110.644727,20.935584]]],[[[110.556648,20.32734],[110.593604,20.360447],[110.586213,20.381205],[110.556648,20.32734]]],[[[115.943037,21.097592],[115.953508,21.064088],[115.989233,21.035603],[116.040356,21.02052],[116.067457,21.04063],[116.044051,21.110434],[116.024341,21.12439],[115.965211,21.123832],[115.943037,21.097592]]],[[[115.926407,20.981411],[115.939342,20.945644],[115.970139,20.919373],[115.999088,20.922727],[116.000936,20.948439],[115.954124,20.99985],[115.926407,20.981411]]],[[[115.834632,22.722695],[115.834632,22.722143],[115.835248,22.722695],[115.834632,22.722695]]],[[[115.834632,22.723247],[115.834632,22.722695],[115.835248,22.722695],[115.834632,22.723247]]]]}},
    {"type":"Feature","properties":{"adcode":450000,"name":"广西壮族自治区","center":[108.320004,22.82402],"centroid":[108.7944,23.833381],"childrenNum":14,"level":"province","parent":{"adcode":100000},"subFeatureIndex":19,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[109.48245,26.029788],[109.473211,26.006663],[109.408537,25.967392],[109.435022,25.93349],[109.396834,25.900117],[109.359262,25.836036],[109.339552,25.83442],[109.327849,25.76168],[109.340168,25.731493],[109.296436,25.71424],[109.207125,25.740119],[109.206509,25.788087],[109.147995,25.741736],[109.13198,25.762758],[109.143683,25.795092],[109.095024,25.80533],[109.077778,25.776771],[109.048213,25.790781],[108.989698,25.778926],[108.999553,25.765453],[108.963829,25.732572],[108.940423,25.740119],[108.896076,25.71424],[108.900387,25.682423],[108.953974,25.686738],[108.953974,25.686738],[109.007561,25.734728],[109.043285,25.738502],[109.07901,25.72071],[109.075314,25.693749],[109.030966,25.629556],[109.051908,25.566949],[109.088249,25.550752],[109.024807,25.51241],[108.949046,25.557231],[108.8893,25.543193],[108.890532,25.556151],[108.826474,25.550212],[108.814772,25.526992],[108.781511,25.554531],[108.799989,25.576666],[108.783975,25.628477],[108.724844,25.634952],[108.68912,25.623081],[108.68604,25.587462],[108.660787,25.584763],[108.658323,25.550212],[108.68912,25.533473],[108.634917,25.520512],[108.6072,25.491885],[108.600425,25.432448],[108.62999,25.335666],[108.625062,25.308076],[108.589338,25.335125],[108.585642,25.365952],[108.471693,25.458928],[108.418723,25.443257],[108.400244,25.491344],[108.359592,25.513491],[108.348506,25.536173],[108.308469,25.525912],[108.280752,25.48],[108.241332,25.46217],[108.251803,25.430286],[108.192673,25.458928],[108.162492,25.444878],[108.193289,25.405421],[108.142782,25.390825],[108.152021,25.324306],[108.143398,25.269658],[108.115065,25.210112],[108.080572,25.193867],[108.001732,25.196574],[107.928435,25.155954],[107.872384,25.141327],[107.839124,25.115861],[107.762747,25.125073],[107.789233,25.15487],[107.760283,25.188451],[107.762131,25.229061],[107.741805,25.24043],[107.700537,25.194408],[107.696226,25.219858],[107.661733,25.258833],[107.659885,25.316192],[107.632168,25.310241],[107.599523,25.250714],[107.576734,25.256668],[107.512676,25.209029],[107.472024,25.213902],[107.489886,25.276693],[107.481263,25.299961],[107.432604,25.289139],[107.409198,25.347024],[107.420901,25.392987],[107.375937,25.411908],[107.358691,25.393528],[107.318039,25.401637],[107.308184,25.432988],[107.336517,25.461089],[107.263836,25.543193],[107.232423,25.556691],[107.228728,25.604733],[107.205322,25.607971],[107.185612,25.578825],[107.064272,25.559391],[107.066736,25.50917],[107.015613,25.495666],[106.996519,25.442716],[106.963874,25.437852],[106.987896,25.358922],[107.012533,25.352973],[107.013765,25.275611],[106.975577,25.232851],[106.933077,25.250714],[106.904128,25.231768],[106.888113,25.181953],[106.853005,25.186827],[106.787715,25.17112],[106.764926,25.183036],[106.732281,25.162454],[106.691013,25.179245],[106.644817,25.164621],[106.63989,25.132658],[106.590615,25.08768],[106.551195,25.082802],[106.519782,25.054072],[106.450181,25.033468],[106.442173,25.019369],[106.332536,24.988454],[106.304819,24.973807],[106.253696,24.971094],[106.215508,24.981944],[106.191486,24.95319],[106.145291,24.954275],[106.197645,24.885889],[106.206269,24.851139],[106.173008,24.760417],[106.150218,24.762591],[106.113878,24.714216],[106.047356,24.684312],[106.024566,24.633186],[105.961741,24.677786],[105.942031,24.725088],[105.863806,24.729437],[105.827466,24.702799],[105.767104,24.719109],[105.70551,24.768569],[105.617431,24.78161],[105.607576,24.803885],[105.573083,24.797366],[105.497322,24.809318],[105.493011,24.833217],[105.457286,24.87123],[105.428337,24.930941],[105.365511,24.943423],[105.334099,24.9266],[105.267577,24.929313],[105.251563,24.967296],[105.212758,24.995505],[105.178266,24.985199],[105.157324,24.958616],[105.131454,24.959701],[105.09573,24.92877],[105.096346,24.928228],[105.082179,24.915745],[105.077868,24.918459],[105.039064,24.872859],[105.026745,24.815836],[105.03352,24.787586],[104.899245,24.752809],[104.865985,24.730524],[104.841963,24.676155],[104.771746,24.659839],[104.729246,24.617953],[104.703377,24.645698],[104.628848,24.660927],[104.595587,24.709323],[104.529682,24.731611],[104.489646,24.653313],[104.520443,24.535228],[104.550008,24.518894],[104.575877,24.424661],[104.616529,24.421937],[104.63008,24.397958],[104.610986,24.377246],[104.641783,24.367979],[104.70892,24.321087],[104.721239,24.340173],[104.703377,24.419757],[104.715695,24.441552],[104.74834,24.435559],[104.765587,24.45953],[104.784681,24.443732],[104.83642,24.446456],[104.914028,24.426296],[104.930042,24.411038],[104.979933,24.412673],[105.042759,24.442097],[105.106817,24.414853],[105.111744,24.37234],[105.138846,24.376701],[105.188121,24.347261],[105.196744,24.326541],[105.164715,24.288362],[105.215222,24.214699],[105.24294,24.208695],[105.229389,24.165567],[105.182577,24.167205],[105.20044,24.105491],[105.260186,24.061236],[105.292831,24.074896],[105.273121,24.092927],[105.320548,24.116416],[105.334099,24.094566],[105.395692,24.065607],[105.406163,24.043748],[105.493011,24.016965],[105.533663,24.130071],[105.594641,24.137718],[105.628518,24.126794],[105.649459,24.032816],[105.704278,24.0667],[105.739387,24.059596],[105.765256,24.073804],[105.802212,24.051945],[105.796669,24.023524],[105.841633,24.03063],[105.859495,24.056864],[105.89214,24.040468],[105.908154,24.069432],[105.901995,24.099482],[105.919241,24.122425],[105.963589,24.110954],[105.998081,24.120786],[106.011632,24.099482],[106.04982,24.089649],[106.053516,24.051399],[106.096631,24.018058],[106.091088,23.998924],[106.128044,23.956819],[106.157609,23.891174],[106.192718,23.879135],[106.173008,23.861622],[106.192102,23.824947],[106.136667,23.795381],[106.157609,23.724175],[106.149602,23.665538],[106.120653,23.605229],[106.141595,23.569579],[106.08616,23.524043],[106.071994,23.495506],[106.039965,23.484529],[105.999929,23.447748],[105.986378,23.489469],[105.935871,23.508678],[105.913081,23.499348],[105.89214,23.52514],[105.852103,23.526786],[105.815763,23.507031],[105.805908,23.467512],[105.758481,23.459826],[105.699966,23.40162],[105.637757,23.404366],[105.694423,23.363168],[105.699966,23.327453],[105.649459,23.346136],[105.593409,23.312614],[105.560148,23.257093],[105.526272,23.234548],[105.542902,23.184495],[105.558916,23.177893],[105.574931,23.066165],[105.625438,23.064513],[105.648844,23.078828],[105.724604,23.06231],[105.74185,23.030921],[105.780039,23.022659],[105.805908,22.994565],[105.839169,22.987403],[105.879205,22.916865],[105.893987,22.936707],[105.959277,22.948832],[105.994385,22.93781],[106.019639,22.990709],[106.08616,22.996218],[106.106486,22.980792],[106.153914,22.988505],[106.206885,22.978588],[106.270326,22.907494],[106.258007,22.889852],[106.286957,22.867245],[106.366413,22.857871],[106.37134,22.878273],[106.41384,22.877171],[106.504383,22.91025],[106.525941,22.946628],[106.562282,22.923479],[106.606013,22.925684],[106.631267,22.88103],[106.657136,22.863385],[106.674998,22.891506],[106.716882,22.881582],[106.709491,22.866142],[106.774781,22.812643],[106.776012,22.813746],[106.778476,22.814298],[106.779092,22.813746],[106.779708,22.813195],[106.78094,22.813195],[106.784636,22.812643],[106.796338,22.812091],[106.801882,22.815401],[106.804346,22.816505],[106.808657,22.817608],[106.813585,22.817608],[106.838838,22.803265],[106.820976,22.768504],[106.768621,22.739254],[106.780324,22.708894],[106.756302,22.68957],[106.711955,22.575228],[106.650361,22.575228],[106.61402,22.602303],[106.585071,22.517192],[106.588151,22.472958],[106.560434,22.455813],[106.588767,22.374486],[106.562897,22.345706],[106.663296,22.33076],[106.670071,22.283144],[106.688549,22.260438],[106.7021,22.207257],[106.673151,22.182322],[106.706411,22.160707],[106.691629,22.13521],[106.71565,22.089745],[106.706411,22.021521],[106.683006,21.999882],[106.698404,21.959925],[106.73844,22.008205],[106.790179,22.004876],[106.802498,21.98157],[106.859164,21.986009],[106.926302,21.967695],[106.935541,21.933836],[106.974345,21.923288],[106.999598,21.947714],[107.05996,21.914959],[107.058729,21.887196],[107.018693,21.859427],[107.018077,21.81943],[107.093837,21.803317],[107.148656,21.758858],[107.194851,21.736624],[107.199163,21.718833],[107.242279,21.703265],[107.271844,21.727173],[107.310648,21.733844],[107.356843,21.667674],[107.363619,21.602031],[107.388256,21.594241],[107.431372,21.642088],[107.477567,21.659888],[107.500973,21.613715],[107.486806,21.59591],[107.547168,21.58645],[107.584741,21.614828],[107.603219,21.597579],[107.712856,21.616497],[107.807711,21.655438],[107.837892,21.640419],[107.863761,21.650988],[107.892095,21.622617],[107.893942,21.596466],[107.929051,21.585893],[107.958,21.534131],[108.034376,21.545821],[108.108289,21.508521],[108.193905,21.519656],[108.156332,21.55083],[108.205608,21.597579],[108.241332,21.599805],[108.249955,21.561406],[108.210535,21.505737],[108.230245,21.491259],[108.330027,21.540254],[108.397781,21.533017],[108.492635,21.554727],[108.591802,21.677129],[108.626294,21.67991],[108.658939,21.643757],[108.678033,21.659331],[108.735931,21.628181],[108.734084,21.626512],[108.745786,21.602587],[108.801837,21.626512],[108.83325,21.610933],[108.881293,21.627068],[108.937959,21.589789],[109.093792,21.579215],[109.09872,21.571424],[109.110422,21.568085],[109.138756,21.567528],[109.142451,21.511861],[109.074698,21.489589],[109.039589,21.457844],[109.046365,21.424421],[109.095024,21.419407],[109.138756,21.388762],[109.186183,21.390991],[109.245929,21.425536],[109.41716,21.438906],[109.484914,21.453388],[109.529877,21.437234],[109.540964,21.466199],[109.576689,21.493487],[109.604406,21.523553],[109.612413,21.556953],[109.654913,21.493487],[109.704188,21.462857],[109.785492,21.45673],[109.788572,21.490702],[109.754695,21.556396],[109.742992,21.616497],[109.778101,21.670455],[109.786108,21.637638],[109.839695,21.636525],[109.888354,21.652101],[109.888354,21.652101],[109.916071,21.668787],[109.940093,21.769419],[109.94502,21.84443],[109.999839,21.881643],[110.050962,21.857205],[110.101469,21.86998],[110.12857,21.902744],[110.196323,21.899968],[110.212338,21.886085],[110.212338,21.886085],[110.224041,21.882198],[110.224041,21.882198],[110.283787,21.892194],[110.290562,21.917736],[110.337374,21.887751],[110.391576,21.89386],[110.378642,21.939942],[110.378642,21.939942],[110.374946,21.967695],[110.374946,21.967695],[110.352772,21.97602],[110.359547,22.015973],[110.35154,22.097508],[110.364475,22.125785],[110.326287,22.152393],[110.34846,22.195621],[110.378026,22.164587],[110.414366,22.208365],[110.456866,22.189526],[110.505525,22.14297],[110.55788,22.196175],[110.602843,22.18343],[110.598532,22.162924],[110.629329,22.149068],[110.678604,22.172901],[110.646575,22.220554],[110.687843,22.249914],[110.725415,22.29588],[110.759292,22.274837],[110.787009,22.28259],[110.749437,22.329653],[110.74143,22.361757],[110.711249,22.369506],[110.712481,22.440879],[110.688459,22.477935],[110.74143,22.464109],[110.740198,22.498947],[110.762988,22.518298],[110.749437,22.556991],[110.778386,22.585174],[110.812263,22.576333],[110.897878,22.591805],[110.896031,22.613352],[110.950233,22.61059],[110.958856,22.636553],[110.997045,22.631582],[111.055559,22.648705],[111.089435,22.695643],[111.058023,22.729871],[111.118385,22.744773],[111.185522,22.735942],[111.218167,22.748085],[111.358601,22.889301],[111.374615,22.938361],[111.362913,22.967568],[111.403565,22.99126],[111.389398,23.005583],[111.433746,23.036428],[111.43313,23.073322],[111.402333,23.066165],[111.377695,23.082132],[111.365992,23.14488],[111.38447,23.16744],[111.388782,23.210349],[111.36476,23.240047],[111.353058,23.284582],[111.376463,23.30437],[111.363528,23.340641],[111.389398,23.375804],[111.383239,23.399423],[111.399869,23.469159],[111.428818,23.466414],[111.479941,23.532822],[111.487332,23.626615],[111.555702,23.64087],[111.615448,23.639225],[111.614832,23.65896],[111.666571,23.718696],[111.621607,23.725819],[111.627766,23.78881],[111.654868,23.833159],[111.683201,23.822758],[111.683201,23.822758],[111.722621,23.823305],[111.8107,23.80688],[111.824867,23.832612],[111.812548,23.887343],[111.845809,23.904305],[111.854432,23.947521],[111.911714,23.943693],[111.940664,23.987989],[111.92157,24.012045],[111.878454,24.109862],[111.886461,24.163929],[111.871062,24.176487],[111.877222,24.227252],[111.912946,24.221795],[111.958526,24.263813],[111.986243,24.25672],[111.990555,24.279634],[112.026279,24.294908],[112.05954,24.339628],[112.057692,24.387057],[112.025047,24.438828],[111.985011,24.467701],[112.009649,24.503103],[112.007185,24.534684],[111.972077,24.578775],[111.936968,24.595645],[111.927729,24.629378],[111.953598,24.64733],[111.939432,24.686487],[111.961606,24.721283],[112.024431,24.740308],[111.951135,24.769655],[111.929577,24.75607],[111.875374,24.756613],[111.868599,24.771829],[111.814396,24.770199],[111.783599,24.785957],[111.708455,24.788673],[111.666571,24.760961],[111.637621,24.715303],[111.641933,24.684856],[111.588962,24.690837],[111.570484,24.64461],[111.526752,24.637538],[111.499035,24.667997],[111.451608,24.665822],[111.431282,24.687574],[111.461463,24.728894],[111.479325,24.797366],[111.449144,24.857113],[111.447296,24.892947],[111.470086,24.92877],[111.434977,24.951562],[111.43313,24.979774],[111.460231,24.992793],[111.467622,25.02208],[111.416499,25.047566],[111.435593,25.093642],[111.375231,25.128324],[111.36784,25.108817],[111.321645,25.105023],[111.274833,25.151078],[111.221862,25.106649],[111.200921,25.074672],[111.139943,25.042144],[111.101754,25.035095],[111.100522,24.945593],[111.009363,24.921172],[110.968711,24.975434],[110.951465,25.04377],[110.98411,25.101772],[110.998892,25.161371],[111.112841,25.21715],[111.103602,25.285351],[111.138711,25.303748],[111.184906,25.367034],[111.210776,25.363248],[111.279145,25.42326],[111.32842,25.521592],[111.324724,25.564249],[111.343202,25.602574],[111.309942,25.645203],[111.30871,25.720171],[111.399869,25.744431],[111.442369,25.77192],[111.43313,25.84627],[111.4861,25.859196],[111.460231,25.885042],[111.383239,25.881812],[111.376463,25.906039],[111.346282,25.906577],[111.297007,25.874274],[111.29208,25.854349],[111.251428,25.864581],[111.230486,25.916267],[111.189834,25.953402],[111.235413,26.048071],[111.267442,26.058824],[111.244652,26.078177],[111.26621,26.095914],[111.258203,26.151796],[111.274833,26.183486],[111.271754,26.217316],[111.293311,26.222148],[111.277913,26.272066],[111.228022,26.261333],[111.204616,26.276359],[111.208928,26.30426],[111.090667,26.308016],[111.008132,26.336982],[111.008747,26.35897],[110.974255,26.385778],[110.94469,26.373447],[110.944074,26.326791],[110.926212,26.320354],[110.939762,26.286554],[110.836284,26.255966],[110.759292,26.248451],[110.73527,26.270993],[110.742046,26.313917],[110.721104,26.294066],[110.673676,26.317135],[110.643495,26.308552],[110.612083,26.333764],[110.584365,26.296749],[110.552952,26.283335],[110.546793,26.233421],[110.495054,26.166299],[110.477808,26.179727],[110.437772,26.153945],[110.373098,26.088927],[110.325671,25.975462],[110.257301,25.961473],[110.24991,26.010965],[110.181541,26.060437],[110.168606,26.028713],[110.100853,26.020108],[110.065128,26.050221],[110.100853,26.132455],[110.099005,26.168985],[110.03002,26.166299],[109.970274,26.195301],[109.904368,26.135679],[109.898825,26.095377],[109.864332,26.027637],[109.814441,26.041081],[109.782412,25.996981],[109.806434,25.973848],[109.826144,25.911422],[109.811361,25.877504],[109.779333,25.866196],[109.768246,25.890427],[109.685094,25.880197],[109.67955,25.921649],[109.693717,25.959321],[109.710963,25.954478],[109.730057,25.989988],[109.649369,26.016882],[109.635203,26.047533],[109.588391,26.019571],[109.560058,26.021184],[109.513247,25.998056],[109.48245,26.029788]]],[[[105.096346,24.928228],[105.09573,24.92877],[105.077868,24.918459],[105.082179,24.915745],[105.096346,24.928228]]],[[[109.088249,21.014934],[109.11227,21.02499],[109.117814,21.017727],[109.144299,21.041189],[109.138756,21.067439],[109.09256,21.057386],[109.088865,21.031134],[109.088249,21.014934]]]]}},
    {"type":"Feature","properties":{"adcode":460000,"name":"海南省","center":[110.33119,20.031971],"centroid":[109.754859,19.189767],"childrenNum":19,"level":"province","parent":{"adcode":100000},"subFeatureIndex":20,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[110.106396,20.026812],[110.042339,19.991384],[109.997375,19.980136],[109.965346,19.993634],[109.898825,19.994196],[109.855093,19.984073],[109.814441,19.993072],[109.76147,19.981261],[109.712195,20.017253],[109.657993,20.01163],[109.585312,19.98801],[109.526797,19.943573],[109.498464,19.873236],[109.411001,19.895184],[109.349407,19.898561],[109.300748,19.917693],[109.25948,19.898561],[109.255784,19.867045],[109.231147,19.863105],[109.159082,19.79048],[109.169553,19.736411],[109.147379,19.704863],[109.093792,19.68965],[109.048829,19.619764],[108.993394,19.587065],[108.92872,19.524468],[108.855424,19.469182],[108.806148,19.450561],[108.765496,19.400894],[108.694047,19.387346],[108.644772,19.349518],[108.609048,19.276661],[108.591186,19.141592],[108.598577,19.055633],[108.630606,19.003017],[108.637997,18.924346],[108.595497,18.872256],[108.593033,18.809386],[108.65278,18.740258],[108.663866,18.67337],[108.641077,18.565614],[108.644772,18.486738],[108.68912,18.447571],[108.776583,18.441894],[108.881293,18.416344],[108.905315,18.389087],[108.944735,18.314107],[109.006329,18.323198],[109.108575,18.323766],[109.138756,18.268081],[109.17448,18.260125],[109.287813,18.264671],[109.355566,18.215221],[109.441182,18.199303],[109.467051,18.173718],[109.527413,18.169169],[109.584696,18.143579],[109.661688,18.175424],[109.726362,18.177698],[109.749767,18.193618],[109.785492,18.339672],[109.919767,18.375457],[110.022629,18.360121],[110.070672,18.376025],[110.090382,18.399309],[110.116867,18.506602],[110.214186,18.578662],[110.246215,18.609859],[110.329366,18.642185],[110.367555,18.631977],[110.499366,18.651824],[110.499366,18.751592],[110.578206,18.784458],[110.590525,18.838841],[110.585597,18.88075],[110.619474,19.152334],[110.676756,19.286264],[110.706321,19.320153],[110.729727,19.378878],[110.787009,19.399765],[110.844292,19.449996],[110.888023,19.518827],[110.920668,19.552668],[111.008747,19.60398],[111.061718,19.612436],[111.071573,19.628784],[111.043856,19.763448],[111.013675,19.850159],[110.966248,20.018377],[110.940994,20.028499],[110.871393,20.01163],[110.808567,20.035808],[110.778386,20.068415],[110.744509,20.074036],[110.717408,20.148778],[110.687843,20.163947],[110.655814,20.134169],[110.562191,20.110006],[110.526467,20.07516],[110.495054,20.077408],[110.387265,20.113378],[110.318279,20.108882],[110.28933,20.056047],[110.243135,20.077408],[110.144585,20.074598],[110.106396,20.026812]]],[[[112.208597,3.876129],[112.241858,3.845677],[112.280046,3.86777],[112.260336,3.917925],[112.219068,3.908969],[112.208597,3.876129]]],[[[113.266165,8.125929],[113.311129,8.177469],[113.343157,8.193463],[113.288955,8.119412],[113.349933,8.172137],[113.386273,8.238479],[113.386273,8.289412],[113.354244,8.304217],[113.353628,8.237887],[113.293882,8.176284],[113.266165,8.125929]]],[[[111.99733,3.848065],[112.015192,3.823583],[112.064467,3.830152],[112.073707,3.865979],[112.03367,3.892251],[111.99733,3.848065]]],[[[111.463311,17.077491],[111.536607,17.104949],[111.4861,17.058039],[111.559397,17.087788],[111.542151,17.11982],[111.452224,17.092936],[111.463311,17.077491]]],[[[117.708319,15.182712],[117.712631,15.118592],[117.726798,15.105303],[117.827812,15.111659],[117.72495,15.131302],[117.720638,15.195418],[117.74466,15.217941],[117.784696,15.16885],[117.838899,15.15903],[117.782848,15.187333],[117.748355,15.230068],[117.715095,15.222561],[117.708319,15.182712]]],[[[112.241858,3.942404],[112.292365,3.946583],[112.288053,3.97345],[112.254177,3.97942],[112.241858,3.942404]]],[[[111.734324,16.19732],[111.779903,16.19732],[111.81686,16.224329],[111.813164,16.261676],[111.782367,16.273741],[111.716462,16.249036],[111.789758,16.250186],[111.790374,16.220307],[111.734324,16.19732]]],[[[111.649324,16.255931],[111.577875,16.208239],[111.56802,16.162834],[111.611136,16.156511],[111.690592,16.211112],[111.606825,16.177779],[111.598817,16.198469],[111.681353,16.262251],[111.649324,16.255931]]],[[[113.896887,7.607204],[113.919677,7.566865],[113.98743,7.536014],[114.058879,7.537794],[114.157429,7.561525],[114.289856,7.617288],[114.368696,7.638642],[114.407501,7.683126],[114.419819,7.765557],[114.464167,7.814771],[114.540543,7.862201],[114.555326,7.891249],[114.540543,7.945783],[114.511594,7.966527],[114.47279,7.968898],[114.414892,7.952895],[114.268298,7.870501],[114.211632,7.786904],[114.095219,7.721082],[114.029314,7.670078],[113.921524,7.639235],[113.896887,7.607204]]],[[[113.976959,8.872888],[114.013299,8.836817],[114.035473,8.783591],[114.060111,8.816119],[114.041017,8.843913],[113.989894,8.878801],[113.976959,8.872888]]],[[[113.956017,8.840365],[113.975111,8.793054],[114.012068,8.798376],[113.977575,8.841548],[113.956017,8.840365]]],[[[111.97454,16.323715],[112.002258,16.306484],[112.07617,16.323715],[112.074938,16.349558],[112.047221,16.360469],[112.002874,16.350707],[111.97454,16.323715]]],[[[111.739251,16.452898],[111.766969,16.470116],[111.786679,16.520039],[111.759577,16.545857],[111.765737,16.495366],[111.739251,16.452898]]],[[[112.216604,8.866383],[112.206133,8.88767],[112.180264,8.862244],[112.216604,8.866383]]],[[[113.792177,7.373422],[113.828518,7.362145],[113.829134,7.383511],[113.792177,7.373422]]],[[[114.194386,8.764664],[114.201161,8.727991],[114.248588,8.724442],[114.222103,8.784773],[114.194386,8.764664]]],[[[112.232619,16.996239],[112.207981,16.987081],[112.222764,16.960751],[112.292981,16.96762],[112.266496,16.993949],[112.232619,16.996239]]],[[[114.689601,10.345648],[114.702536,10.312677],[114.725941,10.319154],[114.747499,10.37214],[114.717318,10.380381],[114.689601,10.345648]]],[[[115.837712,9.709775],[115.861117,9.694438],[115.867277,9.650191],[115.901153,9.67084],[115.925791,9.781734],[115.901153,9.795888],[115.870972,9.778785],[115.837712,9.709775]]],[[[114.617536,9.965688],[114.642173,9.917351],[114.672355,9.927963],[114.685905,9.979245],[114.617536,9.965688]]],[[[113.769387,7.636862],[113.773699,7.601865],[113.814967,7.603051],[113.831597,7.644573],[113.769387,7.636862]]],[[[109.463972,7.344339],[109.463972,7.315254],[109.513247,7.320002],[109.571761,7.373422],[109.654297,7.479648],[109.709115,7.511095],[109.791651,7.524742],[109.938861,7.504569],[109.948716,7.522962],[109.904984,7.55144],[109.816289,7.572797],[109.72205,7.575763],[109.653065,7.559745],[109.536037,7.448792],[109.463972,7.344339]]],[[[116.273181,8.879392],[116.294123,8.858105],[116.332311,8.901269],[116.305826,8.917233],[116.273181,8.879392]]],[[[112.476531,16.001247],[112.570154,16.011027],[112.612037,16.039212],[112.588016,16.070844],[112.462364,16.043813],[112.448814,16.005274],[112.476531,16.001247]]],[[[112.537509,8.846278],[112.57077,8.815527],[112.639755,8.818484],[112.598487,8.859288],[112.537509,8.846278]]],[[[114.469095,10.836261],[114.475254,10.814512],[114.513442,10.848605],[114.565181,10.836261],[114.593514,10.856245],[114.587355,10.909138],[114.55471,10.900911],[114.469095,10.836261]]],[[[112.409393,16.294996],[112.383524,16.265698],[112.411241,16.2634],[112.475915,16.288677],[112.531349,16.285805],[112.536893,16.312228],[112.509176,16.317397],[112.409393,16.294996]]],[[[116.48876,10.395686],[116.461658,10.34918],[116.467202,10.309144],[116.511549,10.297957],[116.566368,10.304434],[116.644592,10.335051],[116.637817,10.365076],[116.514629,10.34918],[116.542346,10.41982],[116.526332,10.426883],[116.48876,10.395686]]],[[[112.349031,16.912088],[112.360734,16.925257],[112.334249,16.962469],[112.30222,16.963041],[112.349031,16.912088]]],[[[111.500267,16.45175],[111.49534,16.4374],[111.545847,16.43453],[111.538455,16.461507],[111.500267,16.45175]]],[[[115.500177,9.897897],[115.54822,9.869007],[115.585177,9.896128],[115.581481,9.917351],[115.518039,9.933857],[115.500177,9.897897]]],[[[114.669891,8.210048],[114.691449,8.18517],[114.74134,8.189316],[114.726557,8.21064],[114.669891,8.210048]]],[[[114.507899,8.120004],[114.530073,8.103415],[114.595978,8.120596],[114.624311,8.149626],[114.595978,8.15792],[114.507899,8.120004]]],[[[115.16757,8.386523],[115.18112,8.345668],[115.235939,8.321982],[115.285214,8.314876],[115.315395,8.356326],[115.299381,8.370537],[115.202678,8.395403],[115.16757,8.386523]]],[[[113.895039,8.00505],[113.904894,7.963564],[113.9708,7.944597],[113.969568,7.974825],[113.940003,8.018088],[113.895039,8.00505]]],[[[115.436119,9.393447],[115.450286,9.345028],[115.469996,9.3592],[115.456445,9.417064],[115.436119,9.393447]]],[[[116.457347,9.174326],[116.477057,9.137103],[116.500462,9.164282],[116.457347,9.174326]]],[[[113.638192,8.976942],[113.654823,8.962163],[113.730583,9.004133],[113.719496,9.020092],[113.644968,8.989355],[113.638192,8.976942]]],[[[114.696992,11.004322],[114.710543,11.001972],[114.793079,11.07657],[114.799854,11.10476],[114.766593,11.110045],[114.710543,11.039567],[114.696992,11.004322]]],[[[114.62,11.432264],[114.652644,11.436957],[114.661884,11.522584],[114.621232,11.518479],[114.62,11.432264]]],[[[114.910723,10.863298],[114.931049,10.841551],[114.959998,10.902087],[114.934129,10.902674],[114.910723,10.863298]]],[[[111.572948,16.470116],[111.578491,16.447158],[111.614216,16.44027],[111.592658,16.490775],[111.572948,16.470116]]],[[[113.939387,8.875253],[113.912285,8.888853],[113.893807,8.862836],[113.916597,8.837999],[113.939387,8.875253]]],[[[109.936397,7.848566],[109.936397,7.823665],[109.988136,7.8124],[110.050346,7.846194],[110.082991,7.896584],[110.078063,7.949339],[110.0331,7.944597],[109.953027,7.888878],[109.936397,7.848566]]],[[[116.727128,11.501473],[116.765316,11.430504],[116.772092,11.445755],[116.738215,11.514961],[116.727128,11.501473]]],[[[111.690592,16.587731],[111.724469,16.560198],[111.717078,16.59404],[111.690592,16.587731]]],[[[112.507328,16.466098],[112.586784,16.525777],[112.575081,16.537251],[112.499321,16.493645],[112.507328,16.466098]]],[[[111.761425,16.061642],[111.791606,16.028859],[111.828563,16.049565],[111.829795,16.070844],[111.761425,16.061642]]],[[[113.845764,10.018733],[113.865474,10.00341],[113.872249,10.123029],[113.856851,10.12185],[113.845764,10.018733]]],[[[114.791847,8.160882],[114.777064,8.114079],[114.812173,8.110524],[114.818332,8.141332],[114.791847,8.160882]]],[[[116.557129,9.745167],[116.566368,9.718623],[116.593469,9.723932],[116.557129,9.745167]]],[[[115.28275,10.191951],[115.288294,10.172513],[115.333257,10.200198],[115.28891,10.211388],[115.28275,10.191951]]],[[[116.832454,10.476908],[116.855243,10.468669],[116.868794,10.495739],[116.832454,10.476908]]],[[[114.703151,16.170307],[114.816484,16.198469],[114.802934,16.215135],[114.704383,16.199044],[114.703151,16.170307]]],[[[115.97753,9.321997],[115.926407,9.311366],[115.943037,9.269433],[115.976298,9.268252],[115.999088,9.293649],[115.97753,9.321997]]],[[[113.660366,9.231039],[113.676997,9.202683],[113.697323,9.225722],[113.660366,9.231039]]],[[[114.665579,7.590001],[114.671739,7.563898],[114.72163,7.59178],[114.703767,7.614915],[114.665579,7.590001]]],[[[114.493116,10.717504],[114.513442,10.722208],[114.562717,10.778064],[114.539312,10.793349],[114.493116,10.717504]]],[[[117.770529,10.773361],[117.798862,10.753371],[117.835819,10.803931],[117.831507,10.838612],[117.801942,10.839788],[117.775457,10.809222],[117.770529,10.773361]]],[[[114.242429,10.242014],[114.263371,10.239658],[114.326197,10.284414],[114.312646,10.300901],[114.265219,10.275581],[114.242429,10.242014]]],[[[114.688985,11.469217],[114.722246,11.429331],[114.737644,11.463938],[114.720398,11.49209],[114.688985,11.469217]]],[[[116.638433,10.503977],[116.653215,10.491031],[116.70865,10.492797],[116.699411,10.517511],[116.638433,10.503977]]],[[[110.459946,8.116449],[110.471032,8.072012],[110.554184,8.093935],[110.599764,8.156735],[110.568351,8.17273],[110.461793,8.128298],[110.459946,8.116449]]],[[[111.463311,8.52504],[111.497187,8.523857],[111.509506,8.550489],[111.463311,8.52504]]],[[[115.258113,8.509652],[115.271048,8.477098],[115.296301,8.510836],[115.258113,8.509652]]],[[[113.221817,8.073789],[113.235984,8.068456],[113.283411,8.111117],[113.269861,8.120004],[113.221817,8.073789]]],[[[114.074893,10.929118],[114.064422,10.904437],[114.110002,10.918541],[114.096451,10.947921],[114.074893,10.929118]]],[[[117.258068,10.320331],[117.299336,10.313855],[117.299952,10.343293],[117.274698,10.358011],[117.258068,10.320331]]],[[[114.212864,16.040937],[114.31203,16.034611],[114.306487,16.057616],[114.268914,16.059342],[114.212864,16.040937]]],[[[110.609003,8.010976],[110.642879,7.989049],[110.641648,8.031125],[110.622553,8.041199],[110.609003,8.010976]]],[[[115.509416,8.490712],[115.521735,8.460523],[115.55438,8.461115],[115.569162,8.49012],[115.558691,8.523265],[115.514344,8.519122],[115.509416,8.490712]]],[[[111.539071,7.54432],[111.542767,7.524742],[111.583419,7.543134],[111.612368,7.592374],[111.566788,7.606017],[111.539071,7.54432]]],[[[111.657947,8.672974],[111.665955,8.622683],[111.717694,8.6499],[111.697368,8.67889],[111.657947,8.672974]]],[[[110.460561,7.799948],[110.487663,7.783346],[110.511684,7.805878],[110.485199,7.827815],[110.460561,7.799948]]],[[[112.345952,8.926101],[112.392763,8.919598],[112.384756,8.946793],[112.345952,8.926101]]],[[[116.469665,9.810041],[116.47952,9.785272],[116.50847,9.79117],[116.490607,9.821246],[116.469665,9.810041]]],[[[111.925265,8.070827],[111.949287,8.05068],[111.994866,8.047125],[112.018888,8.065494],[112.013344,8.093342],[111.95483,8.106377],[111.925265,8.070827]]],[[[114.457392,15.599305],[114.466631,15.576823],[114.491884,15.59354],[114.457392,15.599305]]],[[[114.985252,11.078332],[115.013585,11.063062],[115.021592,11.085967],[114.985252,11.078332]]],[[[114.10569,16.004124],[114.110618,15.978235],[114.132176,16.007575],[114.10569,16.004124]]],[[[116.045283,10.095338],[116.067457,10.065876],[116.09579,10.09357],[116.070537,10.12892],[116.045283,10.095338]]],[[[117.266691,10.69163],[117.348611,10.672811],[117.404661,10.671047],[117.418212,10.702803],[117.369553,10.7422],[117.293176,10.735144],[117.266691,10.69163]]],[[[114.854057,7.244611],[114.819564,7.192957],[114.869455,7.198895],[114.854057,7.244611]]],[[[112.823305,8.910729],[112.859645,8.889444],[112.873196,8.908364],[112.823305,8.910729]]],[[[111.670266,7.651098],[111.707223,7.648725],[111.749722,7.703884],[111.726317,7.729977],[111.691208,7.711593],[111.670266,7.651098]]],[[[112.207981,8.835634],[112.235699,8.827355],[112.241242,8.852783],[112.207981,8.835634]]],[[[112.527654,5.79444],[112.531965,5.766455],[112.562762,5.75931],[112.562146,5.820637],[112.527654,5.79444]]],[[[114.599058,8.846278],[114.645869,8.844504],[114.68221,8.881166],[114.665579,8.900087],[114.61692,8.881166],[114.599058,8.846278]]],[[[114.868223,7.983715],[114.907643,7.951117],[114.914419,8.00742],[114.883006,8.011569],[114.868223,7.983715]]],[[[112.945261,8.410204],[112.985297,8.429149],[112.949572,8.432701],[112.945261,8.410204]]],[[[113.600004,6.961929],[113.580294,6.920344],[113.62341,6.942325],[113.600004,6.961929]]],[[[116.695099,16.345538],[116.708034,16.299591],[116.738831,16.303612],[116.747454,16.360469],[116.717889,16.373676],[116.695099,16.345538]]],[[[117.347995,10.090624],[117.354154,10.06293],[117.385567,10.063519],[117.373864,10.106532],[117.347995,10.090624]]],[[[112.993304,19.472003],[113.038883,19.480466],[113.048123,19.506417],[113.029028,19.52898],[112.993304,19.52616],[112.980369,19.496263],[112.993304,19.472003]]],[[[114.448153,16.034035],[114.485109,16.034611],[114.521449,16.056466],[114.465399,16.067393],[114.448153,16.034035]]],[[[113.832213,19.158552],[113.874097,19.151203],[113.914749,19.172119],[113.920293,19.223551],[113.875945,19.237113],[113.80696,19.222986],[113.799568,19.19925],[113.832213,19.158552]]],[[[112.650842,5.106941],[112.655769,5.055676],[112.682871,5.048522],[112.719211,5.075944],[112.678559,5.121247],[112.650842,5.106941]]],[[[111.638853,7.907254],[111.665339,7.887099],[111.712766,7.887099],[111.713382,7.927408],[111.651788,7.932743],[111.638853,7.907254]]],[[[112.244322,8.874662],[112.281278,8.855148],[112.288669,8.885896],[112.244322,8.874662]]],[[[112.89229,7.844416],[112.929862,7.827815],[112.93171,7.867537],[112.89229,7.844416]]],[[[112.583088,5.56159],[112.606494,5.51751],[112.614501,5.465683],[112.642834,5.489512],[112.616349,5.568737],[112.583088,5.56159]]],[[[112.523342,5.656289],[112.5449,5.616386],[112.565842,5.63068],[112.56153,5.677133],[112.528886,5.687257],[112.523342,5.656289]]],[[[115.361591,13.948985],[115.397315,13.92517],[115.438583,13.943757],[115.423185,13.977443],[115.377605,13.968732],[115.361591,13.948985]]],[[[113.596924,10.240836],[113.617866,10.22199],[113.638192,10.243192],[113.596924,10.240836]]],[[[113.860546,15.477068],[113.893807,15.463802],[113.890112,15.490909],[113.860546,15.477068]]],[[[112.907072,4.993079],[112.943413,4.991887],[112.952652,5.047926],[112.910768,5.038388],[112.907072,4.993079]]],[[[112.557219,5.109326],[112.568922,5.071771],[112.610806,5.091443],[112.601567,5.120055],[112.557219,5.109326]]],[[[112.350263,5.621747],[112.385988,5.615791],[112.385372,5.643187],[112.350263,5.621747]]],[[[112.226459,16.759147],[112.254177,16.751698],[112.262184,16.778057],[112.211061,16.795819],[112.226459,16.759147]]],[[[112.233851,15.69612],[112.25972,15.734718],[112.240626,15.741055],[112.20367,15.71398],[112.233851,15.69612]]],[[[112.612037,5.367973],[112.640371,5.347715],[112.685334,5.371548],[112.690878,5.406702],[112.62374,5.401935],[112.612037,5.367973]]],[[[112.472219,5.73966],[112.496857,5.736683],[112.498089,5.775387],[112.472219,5.73966]]],[[[113.217506,6.306249],[113.230441,6.285429],[113.243991,6.325878],[113.217506,6.306249]]],[[[116.152457,9.579384],[116.189413,9.565221],[116.187565,9.595317],[116.152457,9.579384]]],[[[114.948911,7.508722],[114.960614,7.484988],[115.012353,7.484988],[115.013585,7.525928],[114.948911,7.508722]]],[[[111.553854,7.807656],[111.585267,7.771487],[111.619759,7.840265],[111.603745,7.861608],[111.553854,7.807656]]],[[[113.938771,15.8355],[113.973263,15.805558],[113.9708,15.83953],[113.938771,15.8355]]],[[[114.926122,16.036911],[114.895325,16.036336],[114.910723,16.001823],[114.926122,16.036911]]],[[[116.749302,9.056736],[116.699411,9.049053],[116.70865,9.024229],[116.740679,9.028367],[116.749302,9.056736]]],[[[112.64653,16.385733],[112.681639,16.400661],[112.660081,16.426494],[112.64653,16.385733]]],[[[111.203384,19.92557],[111.203384,19.925007],[111.204,19.92557],[111.204,19.926132],[111.203384,19.92557]]],[[[115.758256,10.461018],[115.776118,10.434534],[115.801987,10.463372],[115.758256,10.461018]]],[[[117.21372,10.735144],[117.187235,10.741612],[117.206945,10.707507],[117.21372,10.735144]]],[[[112.671784,16.331755],[112.701349,16.331755],[112.677943,16.35932],[112.671784,16.331755]]],[[[115.782277,10.541046],[115.795212,10.499858],[115.805067,10.524571],[115.782277,10.541046]]],[[[112.512255,9.544566],[112.50856,9.525679],[112.568922,9.516826],[112.567074,9.554008],[112.512255,9.544566]]],[[[114.610145,15.649447],[114.581195,15.625242],[114.610761,15.615444],[114.610145,15.649447]]],[[[117.299336,11.077745],[117.264227,11.063062],[117.284553,11.02547],[117.304263,11.027232],[117.299336,11.077745]]],[[[117.691073,11.048965],[117.653501,11.046029],[117.655965,11.024882],[117.690457,11.016658],[117.691073,11.048965]]],[[[114.166668,9.38459],[114.175291,9.342075],[114.195617,9.350933],[114.194386,9.391676],[114.166668,9.38459]]],[[[114.714854,9.736909],[114.693296,9.741038],[114.680978,9.707416],[114.704999,9.700337],[114.714854,9.736909]]],[[[112.554139,5.97839],[112.553523,5.942676],[112.575697,5.971247],[112.554139,5.97839]]]]}},
    {"type":"Feature","properties":{"adcode":500000,"name":"重庆市","center":[106.504962,29.533155],"centroid":[107.8839,30.067297],"childrenNum":38,"level":"province","parent":{"adcode":100000},"subFeatureIndex":21,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[106.37442,28.525742],[106.403369,28.569901],[106.477282,28.530474],[106.504999,28.544669],[106.466811,28.586193],[106.49268,28.591448],[106.502535,28.661313],[106.528405,28.677591],[106.492064,28.742153],[106.461883,28.761041],[106.45326,28.817162],[106.474202,28.832891],[106.561666,28.756319],[106.56105,28.719062],[106.587535,28.691767],[106.6171,28.691242],[106.617716,28.66709],[106.651593,28.649235],[106.618332,28.645033],[106.63681,28.622972],[106.606629,28.593024],[106.615252,28.549401],[106.567825,28.523638],[106.564745,28.485247],[106.632499,28.503655],[106.697788,28.47683],[106.708259,28.450524],[106.747063,28.467361],[106.726121,28.51838],[106.73844,28.554657],[106.77786,28.563068],[106.756918,28.607211],[106.784636,28.626649],[106.807425,28.589346],[106.830831,28.623497],[106.866556,28.624548],[106.889345,28.695966],[106.86594,28.690192],[106.824056,28.756319],[106.845614,28.780975],[106.872099,28.777304],[106.923222,28.809821],[106.951555,28.766812],[106.988512,28.776254],[106.983584,28.851239],[107.019308,28.861722],[107.016229,28.882685],[107.14188,28.887925],[107.206554,28.868535],[107.194851,28.838134],[107.227496,28.836037],[107.210866,28.817686],[107.219489,28.772582],[107.24659,28.76209],[107.261373,28.792514],[107.327894,28.810869],[107.339597,28.845997],[107.383945,28.848618],[107.41351,28.911502],[107.441227,28.943977],[107.412894,28.960211],[107.396879,28.993718],[107.364235,29.00942],[107.395647,29.041341],[107.369778,29.091558],[107.412278,29.094696],[107.427676,29.128682],[107.408582,29.138091],[107.401807,29.184603],[107.441227,29.203934],[107.486806,29.174153],[107.570574,29.218037],[107.589052,29.150113],[107.605683,29.164747],[107.659885,29.162656],[107.700537,29.141228],[107.749197,29.199754],[107.810791,29.139137],[107.784921,29.048143],[107.823725,29.034016],[107.810175,28.984295],[107.867457,28.960211],[107.882855,29.00628],[107.908725,29.007327],[107.925971,29.032446],[108.026369,29.039772],[108.070717,29.086328],[108.150173,29.053375],[108.193289,29.072207],[108.256115,29.040295],[108.277673,29.091558],[108.306622,29.079006],[108.297999,29.045527],[108.319556,28.961258],[108.345426,28.943453],[108.357745,28.893165],[108.346658,28.859625],[108.352817,28.815589],[108.386078,28.803003],[108.385462,28.772058],[108.347274,28.736381],[108.332491,28.679166],[108.439049,28.634003],[108.501258,28.626649],[108.50249,28.63768],[108.575787,28.659738],[108.636149,28.621396],[108.604736,28.590922],[108.610896,28.539412],[108.573939,28.531],[108.586874,28.463678],[108.609664,28.43579],[108.609048,28.407368],[108.576403,28.38631],[108.580099,28.343128],[108.611512,28.324691],[108.667562,28.334173],[108.656475,28.359981],[108.697127,28.401051],[108.688504,28.422106],[108.640461,28.456838],[108.657091,28.47683],[108.700207,28.48209],[108.709446,28.501026],[108.746402,28.45105],[108.780279,28.42579],[108.759953,28.389995],[108.783359,28.380518],[108.761801,28.304143],[108.726692,28.282011],[108.738395,28.228241],[108.772888,28.212949],[108.821547,28.245113],[108.855424,28.199764],[108.89546,28.219804],[108.923793,28.217167],[108.929952,28.19027],[109.005713,28.162837],[109.026655,28.220331],[109.086401,28.184467],[109.101799,28.202401],[109.081473,28.247749],[109.117198,28.277795],[109.152306,28.349975],[109.153538,28.417369],[109.191726,28.471043],[109.23361,28.474726],[109.274262,28.494714],[109.273646,28.53836],[109.319842,28.579886],[109.306907,28.62087],[109.252089,28.606685],[109.235458,28.61982],[109.201581,28.597753],[109.192958,28.636104],[109.271183,28.671816],[109.252704,28.691767],[109.294588,28.722211],[109.2989,28.7474],[109.241002,28.776779],[109.246545,28.80143],[109.235458,28.882161],[109.261328,28.952356],[109.292741,28.987436],[109.294588,29.015177],[109.319842,29.042388],[109.312451,29.066453],[109.240386,29.086328],[109.232378,29.119271],[109.215748,29.145409],[109.162777,29.180946],[109.139372,29.168927],[109.110422,29.21647],[109.141835,29.270256],[109.106727,29.288526],[109.11227,29.361053],[109.060531,29.403292],[109.034662,29.360531],[108.999553,29.36366],[108.983539,29.332883],[108.919481,29.3261],[108.934264,29.399643],[108.927488,29.435612],[108.884373,29.440824],[108.866511,29.470527],[108.888684,29.502305],[108.878213,29.539279],[108.913322,29.574679],[108.901003,29.604863],[108.870206,29.596537],[108.888068,29.628795],[108.844337,29.658443],[108.781511,29.635558],[108.797525,29.660003],[108.786438,29.691721],[108.752562,29.649082],[108.690968,29.689642],[108.676801,29.749412],[108.680497,29.800319],[108.658939,29.854833],[108.601041,29.863656],[108.556077,29.818493],[108.52528,29.770713],[108.548686,29.749412],[108.504954,29.728626],[108.504338,29.707836],[108.460606,29.741098],[108.437201,29.741098],[108.442744,29.778505],[108.422418,29.772791],[108.424266,29.815897],[108.371295,29.841337],[108.433505,29.880262],[108.467998,29.864175],[108.516041,29.885451],[108.517889,29.9394],[108.536367,29.983472],[108.532055,30.051873],[108.513577,30.057571],[108.546222,30.104178],[108.56778,30.157491],[108.551766,30.1637],[108.581947,30.255759],[108.54499,30.269716],[108.524048,30.309506],[108.501258,30.314673],[108.460606,30.35961],[108.431041,30.354446],[108.402092,30.376649],[108.430425,30.416397],[108.411331,30.438586],[108.42673,30.492233],[108.472925,30.487076],[108.512961,30.501515],[108.556077,30.487592],[108.56778,30.468508],[108.6497,30.53915],[108.642925,30.578831],[108.688504,30.58759],[108.698975,30.54482],[108.743939,30.494812],[108.789518,30.513374],[108.808612,30.491202],[108.838793,30.503062],[108.893612,30.565434],[108.971836,30.627766],[109.006329,30.626736],[109.042669,30.655571],[109.071002,30.640125],[109.111654,30.646303],[109.106111,30.61077],[109.105495,30.585529],[109.102415,30.580377],[109.101183,30.579346],[109.106111,30.570587],[109.103647,30.565949],[109.143683,30.521108],[109.191726,30.545851],[109.191726,30.545851],[109.245313,30.580892],[109.299516,30.630341],[109.314298,30.599953],[109.36111,30.551004],[109.337088,30.521623],[109.35495,30.487076],[109.418392,30.559766],[109.435638,30.595832],[109.535421,30.664837],[109.543428,30.63961],[109.574225,30.646818],[109.590855,30.69366],[109.625348,30.702923],[109.661072,30.738936],[109.656761,30.760538],[109.701724,30.783677],[109.780564,30.848437],[109.828608,30.864364],[109.894513,30.899803],[109.943788,30.878746],[110.008462,30.883369],[110.019549,30.829425],[110.048498,30.800642],[110.082375,30.799614],[110.151976,30.911613],[110.153824,30.953708],[110.172918,30.978853],[110.140889,30.987062],[110.140273,31.030661],[110.120563,31.0322],[110.119947,31.088592],[110.147048,31.116776],[110.180309,31.121899],[110.200019,31.158779],[110.180309,31.179774],[110.155671,31.279564],[110.161831,31.314338],[110.118715,31.409899],[110.054042,31.410921],[110.036795,31.436966],[109.98752,31.474744],[109.94502,31.47066],[109.969658,31.508935],[109.894513,31.519139],[109.837847,31.555354],[109.727594,31.548214],[109.745456,31.598182],[109.76455,31.602769],[109.737449,31.628761],[109.731289,31.700582],[109.683246,31.719929],[109.622268,31.711783],[109.585928,31.726546],[109.549587,31.73011],[109.502776,31.716365],[109.446109,31.722983],[109.381436,31.705165],[109.281654,31.716874],[109.282885,31.743343],[109.253936,31.759628],[109.279806,31.776418],[109.27611,31.79931],[109.195422,31.817618],[109.191111,31.85575],[109.123357,31.892851],[109.085785,31.929428],[108.986619,31.980205],[108.902235,31.984774],[108.837561,32.039072],[108.78767,32.04871],[108.75133,32.076098],[108.734084,32.106519],[108.676801,32.10297],[108.585026,32.17189],[108.543758,32.177969],[108.509882,32.201266],[108.480317,32.182527],[108.399013,32.194176],[108.370063,32.172397],[108.379918,32.154158],[108.379918,32.154158],[108.379303,32.153652],[108.379303,32.153652],[108.399628,32.147065],[108.452599,32.090296],[108.42981,32.061391],[108.372527,32.077112],[108.344194,32.067477],[108.362056,32.035521],[108.329411,32.020299],[108.370063,31.988835],[108.351585,31.971575],[108.307238,31.997463],[108.259194,31.967006],[108.343578,31.860834],[108.386078,31.854226],[108.391005,31.829822],[108.429194,31.809482],[108.455063,31.814059],[108.462454,31.780488],[108.535135,31.757592],[108.50557,31.734182],[108.514809,31.693963],[108.546838,31.665442],[108.519121,31.665952],[108.468614,31.636404],[108.442744,31.633856],[108.390389,31.591555],[108.386078,31.544134],[108.339266,31.539033],[108.344194,31.512506],[108.254883,31.49873],[108.233941,31.506894],[108.191441,31.492096],[108.193289,31.467598],[108.224086,31.464024],[108.216079,31.41041],[108.153869,31.371073],[108.185898,31.336831],[108.095354,31.268311],[108.038688,31.252964],[108.031297,31.217144],[108.07626,31.231985],[108.089811,31.204859],[108.025753,31.116263],[108.009123,31.109602],[108.026985,31.061938],[108.060246,31.052197],[108.00358,31.025533],[107.983254,30.983983],[107.942602,30.989114],[107.948145,30.918802],[107.994956,30.908533],[107.956152,30.882855],[107.851443,30.792931],[107.788001,30.81966],[107.763979,30.817091],[107.760899,30.862823],[107.739957,30.884396],[107.693146,30.875665],[107.645103,30.821202],[107.57735,30.847924],[107.515756,30.854603],[107.483111,30.838675],[107.498509,30.809381],[107.454162,30.771851],[107.454162,30.771851],[107.424597,30.74048],[107.458473,30.704981],[107.477567,30.664837],[107.516987,30.644759],[107.485575,30.598408],[107.427676,30.547397],[107.443075,30.53348],[107.408582,30.521623],[107.368546,30.468508],[107.338981,30.386459],[107.288474,30.337402],[107.257677,30.267131],[107.221337,30.213878],[107.103076,30.090198],[107.080286,30.094341],[107.084598,30.063786],[107.058113,30.043066],[107.055649,30.040476],[107.054417,30.040994],[107.053801,30.043584],[107.02054,30.036849],[106.981736,30.08502],[106.976193,30.083467],[106.94478,30.037367],[106.913367,30.025451],[106.862244,30.033223],[106.83699,30.049801],[106.825904,30.03115],[106.825904,30.03115],[106.785252,30.01716],[106.732281,30.027005],[106.724274,30.058607],[106.699636,30.074145],[106.700252,30.111944],[106.672535,30.122297],[106.677462,30.156974],[106.631883,30.186464],[106.611557,30.235596],[106.612173,30.235596],[106.611557,30.235596],[106.612173,30.235596],[106.612173,30.235596],[106.612789,30.235596],[106.612789,30.235596],[106.642354,30.246454],[106.611557,30.292455],[106.560434,30.31519],[106.545035,30.296589],[106.49884,30.295556],[106.43971,30.308473],[106.428623,30.254725],[106.401521,30.242318],[106.349167,30.24542],[106.334384,30.225772],[106.306667,30.238182],[106.296196,30.205603],[106.264167,30.20974],[106.260471,30.19681],[106.232754,30.185947],[106.180399,30.233011],[106.168696,30.303823],[106.132356,30.323972],[106.132972,30.30279],[106.07261,30.333786],[106.031958,30.373551],[105.943263,30.372002],[105.900763,30.405042],[105.84656,30.410203],[105.825618,30.436006],[105.792357,30.427234],[105.760329,30.384393],[105.754785,30.342567],[105.714749,30.322939],[105.720292,30.252657],[105.720292,30.252657],[105.670401,30.254208],[105.624822,30.275918],[105.619894,30.234045],[105.662394,30.210258],[105.642684,30.186464],[105.56138,30.183878],[105.550909,30.179222],[105.536127,30.152834],[105.596489,30.159043],[105.574315,30.130579],[105.580474,30.129544],[105.582938,30.127474],[105.582938,30.12385],[105.642068,30.101072],[105.638988,30.076216],[105.676561,30.06793],[105.687032,30.038922],[105.719677,30.042548],[105.753553,30.018196],[105.723372,29.975177],[105.730763,29.95755],[105.70243,29.924879],[105.717213,29.893753],[105.738771,29.891159],[105.707974,29.840818],[105.610655,29.837184],[105.582938,29.819013],[105.574931,29.744216],[105.529351,29.707836],[105.481924,29.718232],[105.476996,29.674564],[105.419714,29.688082],[105.38091,29.628275],[105.347649,29.621512],[105.332867,29.592374],[105.296526,29.571035],[105.305149,29.53199],[105.337794,29.459064],[105.334099,29.441345],[105.387069,29.455416],[105.387069,29.455416],[105.399388,29.43874],[105.372903,29.421018],[105.426489,29.419454],[105.441888,29.400686],[105.418482,29.352185],[105.42033,29.31149],[105.465294,29.322969],[105.459134,29.288526],[105.513337,29.283306],[105.521344,29.264513],[105.557684,29.278608],[105.631597,29.280174],[105.647612,29.253027],[105.695039,29.287482],[105.712285,29.219082],[105.703662,29.176766],[105.728916,29.134432],[105.752321,29.129727],[105.728916,29.1062],[105.757865,29.069068],[105.74185,29.039249],[105.766488,29.013607],[105.762176,28.9911],[105.801596,28.958116],[105.797285,28.936121],[105.830546,28.944501],[105.852719,28.927217],[105.910002,28.920407],[105.969132,28.965971],[106.001161,28.973824],[106.040581,28.955498],[106.049204,28.906263],[106.070762,28.919884],[106.101559,28.898928],[106.14837,28.901548],[106.173008,28.920407],[106.206885,28.904691],[106.264783,28.845997],[106.245689,28.817686],[106.267863,28.779402],[106.274022,28.739004],[106.305435,28.704365],[106.304203,28.64976],[106.346703,28.583565],[106.33192,28.55308],[106.37442,28.525742]]],[[[109.105495,30.585529],[109.106111,30.61077],[109.09256,30.578831],[109.09872,30.579346],[109.101183,30.579346],[109.102415,30.580377],[109.105495,30.585529]]],[[[105.582938,30.12385],[105.582938,30.127474],[105.580474,30.129544],[105.574315,30.130579],[105.582938,30.12385]]],[[[109.09872,30.579346],[109.09256,30.578831],[109.103647,30.565949],[109.106111,30.570587],[109.09872,30.579346]]],[[[107.058113,30.043066],[107.053801,30.043584],[107.054417,30.040994],[107.055649,30.040476],[107.058113,30.043066]]]]}},
    {"type":"Feature","properties":{"adcode":510000,"name":"四川省","center":[104.065735,30.659462],"centroid":[102.693453,30.674545],"childrenNum":21,"level":"province","parent":{"adcode":100000},"subFeatureIndex":22,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[101.167885,27.198311],[101.170349,27.175421],[101.145095,27.103523],[101.157414,27.094999],[101.136472,27.023584],[101.228863,26.981992],[101.227015,26.959057],[101.264587,26.955323],[101.267667,26.903034],[101.311399,26.903034],[101.365602,26.883819],[101.399478,26.841642],[101.358826,26.771669],[101.387159,26.753501],[101.389623,26.723036],[101.435819,26.740675],[101.458608,26.731054],[101.445674,26.77434],[101.466,26.786629],[101.513427,26.768463],[101.453065,26.692563],[101.481398,26.673313],[101.461072,26.640687],[101.461688,26.606447],[101.402558,26.604841],[101.395783,26.591998],[101.422884,26.53151],[101.458608,26.49563],[101.506652,26.499915],[101.530057,26.467239],[101.565782,26.454381],[101.637847,26.388995],[101.635383,26.357361],[101.660636,26.346635],[101.64031,26.318745],[101.597195,26.303187],[101.586108,26.279579],[101.630455,26.224832],[101.690202,26.241473],[101.737013,26.219463],[101.773353,26.168448],[101.807846,26.156093],[101.796759,26.114723],[101.839875,26.082477],[101.835563,26.04592],[101.857737,26.049146],[101.899621,26.099139],[101.929186,26.105588],[101.954439,26.084627],[102.020961,26.096451],[102.080091,26.065275],[102.107808,26.068501],[102.152156,26.10935],[102.174946,26.146961],[102.242699,26.190468],[102.245163,26.212483],[102.349257,26.244694],[102.392372,26.296749],[102.440416,26.300505],[102.542046,26.338591],[102.570995,26.362723],[102.629509,26.336982],[102.638748,26.307479],[102.60056,26.250598],[102.659074,26.221611],[102.709581,26.210336],[102.739762,26.268846],[102.785342,26.298895],[102.833385,26.306406],[102.878964,26.364332],[102.893131,26.338591],[102.975667,26.340736],[102.998457,26.371839],[102.988602,26.413117],[102.989833,26.482775],[103.030485,26.485989],[103.052659,26.514374],[103.052659,26.555602],[103.035413,26.556673],[103.026174,26.664221],[103.005232,26.679195],[103.008312,26.710741],[102.983674,26.76686],[102.991681,26.775409],[102.966428,26.837904],[102.949181,26.843244],[102.896211,26.91264],[102.894979,27.001724],[102.870957,27.026782],[102.913457,27.133886],[102.904218,27.227584],[102.883276,27.258444],[102.883892,27.299401],[102.899906,27.317481],[102.941174,27.405711],[102.989833,27.367983],[103.055739,27.40943],[103.080992,27.396679],[103.141355,27.420586],[103.144434,27.450331],[103.19063,27.523596],[103.232514,27.56976],[103.2861,27.561802],[103.29226,27.632872],[103.349542,27.678459],[103.369868,27.708664],[103.393274,27.709194],[103.461027,27.779638],[103.487512,27.794992],[103.509686,27.843687],[103.502295,27.910343],[103.55465,27.978543],[103.515846,27.965329],[103.486281,28.033495],[103.459179,28.021345],[103.430846,28.044587],[103.470266,28.122204],[103.533092,28.168641],[103.573128,28.230877],[103.643961,28.260401],[103.692004,28.232459],[103.701859,28.198709],[103.740048,28.23615],[103.770845,28.233514],[103.828743,28.285173],[103.877402,28.316262],[103.85338,28.356822],[103.860156,28.383677],[103.828743,28.44],[103.829975,28.459995],[103.781931,28.525216],[103.802873,28.563068],[103.838598,28.587244],[103.833054,28.605109],[103.850917,28.66709],[103.887873,28.61982],[103.910047,28.631377],[103.953779,28.600906],[104.05972,28.6277],[104.09606,28.603533],[104.117618,28.634003],[104.170589,28.642932],[104.230951,28.635579],[104.252509,28.660788],[104.277147,28.631902],[104.314719,28.615617],[104.372617,28.649235],[104.425588,28.626649],[104.417581,28.598279],[104.375697,28.5946],[104.355987,28.555183],[104.323342,28.540989],[104.260516,28.536257],[104.267908,28.499448],[104.254357,28.403683],[104.282074,28.343128],[104.314103,28.306778],[104.343052,28.334173],[104.384936,28.329959],[104.392943,28.291497],[104.420045,28.269889],[104.44961,28.269889],[104.462544,28.241422],[104.442834,28.211366],[104.402182,28.202928],[104.406494,28.173389],[104.444682,28.16231],[104.448994,28.113758],[104.40095,28.091586],[104.373233,28.051454],[104.304248,28.050926],[104.30856,28.036136],[104.362762,28.012891],[104.40095,27.952114],[104.44961,27.927794],[104.508124,27.878078],[104.52537,27.889187],[104.573413,27.840512],[104.607906,27.857974],[104.63316,27.850567],[104.676275,27.880723],[104.743413,27.901881],[104.761891,27.884426],[104.796999,27.901352],[104.842579,27.900294],[104.888158,27.914574],[104.918339,27.938897],[104.903557,27.962158],[104.975006,28.020816],[104.980549,28.063073],[105.002107,28.064129],[105.061853,28.096866],[105.119752,28.07205],[105.168411,28.071522],[105.186889,28.054623],[105.167795,28.021345],[105.186273,27.995454],[105.218302,27.990698],[105.247867,28.009193],[105.270657,27.99704],[105.284823,27.935725],[105.233084,27.895534],[105.25957,27.827811],[105.313157,27.810874],[105.273736,27.794992],[105.293447,27.770637],[105.290367,27.712373],[105.308229,27.704955],[105.353809,27.748924],[105.44004,27.775402],[105.508409,27.769048],[105.560148,27.71979],[105.605112,27.715552],[105.62359,27.666269],[105.664242,27.683759],[105.720292,27.683759],[105.722756,27.706015],[105.76772,27.7182],[105.848408,27.707074],[105.868118,27.732504],[105.922937,27.746805],[105.92848,27.729855],[105.985146,27.749983],[106.023335,27.746805],[106.063987,27.776991],[106.120653,27.779638],[106.193334,27.75422],[106.242609,27.767459],[106.306667,27.808756],[106.337464,27.859033],[106.325145,27.898708],[106.304819,27.899237],[106.307899,27.936782],[106.328225,27.952643],[106.286341,28.007079],[106.246305,28.011835],[106.266631,28.066769],[106.206885,28.134343],[106.145291,28.162837],[106.093552,28.162837],[105.975907,28.107952],[105.943878,28.143314],[105.895219,28.119565],[105.860727,28.159672],[105.889676,28.237732],[105.848408,28.255656],[105.824386,28.306251],[105.78743,28.335753],[105.76464,28.308359],[105.76464,28.308359],[105.737539,28.30309],[105.730147,28.271997],[105.68888,28.284119],[105.639604,28.324164],[105.655003,28.362615],[105.643916,28.431053],[105.612503,28.438947],[105.62359,28.517854],[105.68272,28.534154],[105.693191,28.58882],[105.712901,28.586718],[105.74493,28.616668],[105.757249,28.590397],[105.78435,28.610889],[105.808372,28.599855],[105.884748,28.595126],[105.889676,28.670765],[105.937719,28.686517],[105.966668,28.761041],[106.001161,28.743727],[106.030726,28.694917],[106.085544,28.681792],[106.103407,28.636104],[106.14837,28.642932],[106.17116,28.629275],[106.184711,28.58882],[106.254928,28.539412],[106.2925,28.537309],[106.304819,28.505233],[106.349167,28.473674],[106.379348,28.479986],[106.37442,28.525742],[106.33192,28.55308],[106.346703,28.583565],[106.304203,28.64976],[106.305435,28.704365],[106.274022,28.739004],[106.267863,28.779402],[106.245689,28.817686],[106.264783,28.845997],[106.206885,28.904691],[106.173008,28.920407],[106.14837,28.901548],[106.101559,28.898928],[106.070762,28.919884],[106.049204,28.906263],[106.040581,28.955498],[106.001161,28.973824],[105.969132,28.965971],[105.910002,28.920407],[105.852719,28.927217],[105.830546,28.944501],[105.797285,28.936121],[105.801596,28.958116],[105.762176,28.9911],[105.766488,29.013607],[105.74185,29.039249],[105.757865,29.069068],[105.728916,29.1062],[105.752321,29.129727],[105.728916,29.134432],[105.703662,29.176766],[105.712285,29.219082],[105.695039,29.287482],[105.647612,29.253027],[105.631597,29.280174],[105.557684,29.278608],[105.521344,29.264513],[105.513337,29.283306],[105.459134,29.288526],[105.465294,29.322969],[105.42033,29.31149],[105.418482,29.352185],[105.441888,29.400686],[105.426489,29.419454],[105.372903,29.421018],[105.399388,29.43874],[105.387069,29.455416],[105.387069,29.455416],[105.334099,29.441345],[105.337794,29.459064],[105.305149,29.53199],[105.296526,29.571035],[105.332867,29.592374],[105.347649,29.621512],[105.38091,29.628275],[105.419714,29.688082],[105.476996,29.674564],[105.481924,29.718232],[105.529351,29.707836],[105.574931,29.744216],[105.582938,29.819013],[105.610655,29.837184],[105.707974,29.840818],[105.738771,29.891159],[105.717213,29.893753],[105.70243,29.924879],[105.730763,29.95755],[105.723372,29.975177],[105.753553,30.018196],[105.719677,30.042548],[105.687032,30.038922],[105.676561,30.06793],[105.638988,30.076216],[105.642068,30.101072],[105.582938,30.12385],[105.574315,30.130579],[105.596489,30.159043],[105.536127,30.152834],[105.550909,30.179222],[105.556453,30.187499],[105.558916,30.18543],[105.56138,30.183878],[105.642684,30.186464],[105.662394,30.210258],[105.619894,30.234045],[105.624822,30.275918],[105.670401,30.254208],[105.720292,30.252657],[105.720292,30.252657],[105.714749,30.322939],[105.754785,30.342567],[105.760329,30.384393],[105.792357,30.427234],[105.825618,30.436006],[105.84656,30.410203],[105.900763,30.405042],[105.943263,30.372002],[106.031958,30.373551],[106.07261,30.333786],[106.132972,30.30279],[106.132356,30.323972],[106.168696,30.303823],[106.180399,30.233011],[106.232754,30.185947],[106.260471,30.19681],[106.260471,30.204051],[106.260471,30.207672],[106.264167,30.20974],[106.296196,30.205603],[106.306667,30.238182],[106.334384,30.225772],[106.349167,30.24542],[106.401521,30.242318],[106.428623,30.254725],[106.43971,30.308473],[106.49884,30.295556],[106.545035,30.296589],[106.560434,30.31519],[106.611557,30.292455],[106.642354,30.246454],[106.612789,30.235596],[106.612789,30.235596],[106.612173,30.235596],[106.612173,30.235596],[106.611557,30.235596],[106.612173,30.235596],[106.611557,30.235596],[106.631883,30.186464],[106.677462,30.156974],[106.672535,30.122297],[106.700252,30.111944],[106.699636,30.074145],[106.724274,30.058607],[106.732281,30.027005],[106.785252,30.01716],[106.825904,30.03115],[106.825904,30.03115],[106.83699,30.049801],[106.862244,30.033223],[106.913367,30.025451],[106.94478,30.037367],[106.976193,30.083467],[106.975577,30.088127],[106.976809,30.088127],[106.977425,30.087609],[106.978656,30.087609],[106.979888,30.088127],[106.980504,30.087609],[106.981736,30.08502],[107.02054,30.036849],[107.053801,30.043584],[107.058113,30.043066],[107.084598,30.063786],[107.080286,30.094341],[107.103076,30.090198],[107.221337,30.213878],[107.257677,30.267131],[107.288474,30.337402],[107.338981,30.386459],[107.368546,30.468508],[107.408582,30.521623],[107.443075,30.53348],[107.427676,30.547397],[107.485575,30.598408],[107.516987,30.644759],[107.477567,30.664837],[107.458473,30.704981],[107.424597,30.74048],[107.454162,30.771851],[107.454162,30.771851],[107.498509,30.809381],[107.483111,30.838675],[107.515756,30.854603],[107.57735,30.847924],[107.645103,30.821202],[107.693146,30.875665],[107.739957,30.884396],[107.760899,30.862823],[107.763979,30.817091],[107.788001,30.81966],[107.851443,30.792931],[107.956152,30.882855],[107.994956,30.908533],[107.948145,30.918802],[107.942602,30.989114],[107.983254,30.983983],[108.00358,31.025533],[108.060246,31.052197],[108.026985,31.061938],[108.009123,31.109602],[108.025753,31.116263],[108.089811,31.204859],[108.07626,31.231985],[108.031297,31.217144],[108.038688,31.252964],[108.095354,31.268311],[108.185898,31.336831],[108.153869,31.371073],[108.216079,31.41041],[108.224086,31.464024],[108.193289,31.467598],[108.191441,31.492096],[108.233941,31.506894],[108.254883,31.49873],[108.344194,31.512506],[108.339266,31.539033],[108.386078,31.544134],[108.390389,31.591555],[108.442744,31.633856],[108.468614,31.636404],[108.519121,31.665952],[108.546838,31.665442],[108.514809,31.693963],[108.50557,31.734182],[108.535135,31.757592],[108.462454,31.780488],[108.455063,31.814059],[108.429194,31.809482],[108.391005,31.829822],[108.386078,31.854226],[108.343578,31.860834],[108.259194,31.967006],[108.307238,31.997463],[108.351585,31.971575],[108.370063,31.988835],[108.329411,32.020299],[108.362056,32.035521],[108.344194,32.067477],[108.372527,32.077112],[108.42981,32.061391],[108.452599,32.090296],[108.399628,32.147065],[108.379303,32.153652],[108.379303,32.153652],[108.379918,32.154158],[108.379918,32.154158],[108.370063,32.172397],[108.399013,32.194176],[108.480317,32.182527],[108.509882,32.201266],[108.507418,32.245819],[108.469846,32.270618],[108.414411,32.252399],[108.389773,32.263533],[108.310933,32.232152],[108.240716,32.274666],[108.179738,32.221521],[108.156948,32.239239],[108.143398,32.219495],[108.086731,32.233165],[108.018362,32.2119],[108.024521,32.177462],[107.979558,32.146051],[107.924739,32.197215],[107.890247,32.214432],[107.864377,32.201266],[107.812022,32.247844],[107.753508,32.338399],[107.707929,32.331826],[107.680827,32.397035],[107.648183,32.413709],[107.598291,32.411688],[107.527458,32.38238],[107.489886,32.425328],[107.456625,32.41775],[107.460937,32.453612],[107.438763,32.465732],[107.436299,32.529835],[107.382097,32.54043],[107.356843,32.506622],[107.313727,32.489965],[107.287858,32.457147],[107.263836,32.403099],[107.212097,32.428864],[107.189924,32.468256],[107.127098,32.482393],[107.080286,32.542448],[107.108004,32.600951],[107.098765,32.649338],[107.05996,32.686115],[107.066736,32.708779],[107.012533,32.721367],[106.912751,32.704247],[106.903512,32.721367],[106.854853,32.724388],[106.82344,32.705254],[106.793259,32.712807],[106.783404,32.735967],[106.733513,32.739491],[106.670071,32.694678],[106.626955,32.682086],[106.585687,32.68813],[106.517934,32.668485],[106.498224,32.649338],[106.451412,32.65992],[106.421231,32.616579],[106.389203,32.62666],[106.347935,32.671003],[106.301123,32.680071],[106.267863,32.673522],[106.254928,32.693671],[106.17424,32.6977],[106.120037,32.719856],[106.071378,32.758114],[106.07261,32.76365],[106.093552,32.82402],[106.071378,32.828546],[106.044277,32.864747],[106.011632,32.829552],[105.969132,32.849162],[105.93156,32.826032],[105.893371,32.838603],[105.849024,32.817985],[105.825002,32.824523],[105.822538,32.770192],[105.779423,32.750061],[105.768952,32.767676],[105.719061,32.759624],[105.677793,32.726402],[105.596489,32.69921],[105.585402,32.728919],[105.563844,32.724891],[105.555221,32.794343],[105.534279,32.790822],[105.524424,32.847654],[105.495475,32.873292],[105.49917,32.911986],[105.467757,32.930071],[105.414171,32.922034],[105.408011,32.885857],[105.38091,32.876307],[105.396308,32.85067],[105.396308,32.85067],[105.427721,32.784281],[105.454207,32.767173],[105.448663,32.732946],[105.368591,32.712807],[105.347033,32.68259],[105.297758,32.656897],[105.263265,32.652362],[105.219534,32.666469],[105.215222,32.63674],[105.185041,32.617587],[105.111128,32.593893],[105.0791,32.637244],[105.026745,32.650346],[104.925115,32.607505],[104.881999,32.600951],[104.845659,32.653873],[104.820405,32.662943],[104.795768,32.643292],[104.739717,32.635228],[104.696601,32.673522],[104.643015,32.661935],[104.592508,32.695685],[104.582653,32.722374],[104.526602,32.728416],[104.51182,32.753585],[104.458849,32.748551],[104.363994,32.822511],[104.294393,32.835586],[104.277147,32.90244],[104.288234,32.942628],[104.345516,32.940117],[104.378161,32.953174],[104.383704,32.994343],[104.426204,33.010906],[104.391711,33.035493],[104.337509,33.038002],[104.378161,33.109214],[104.351059,33.158828],[104.32827,33.223934],[104.323958,33.26898],[104.303632,33.304499],[104.333813,33.315502],[104.386168,33.298497],[104.420045,33.327004],[104.373849,33.345004],[104.292545,33.336505],[104.272219,33.391486],[104.22048,33.404477],[104.213089,33.446932],[104.180444,33.472895],[104.155191,33.542755],[104.176749,33.5996],[104.103452,33.663381],[104.046169,33.686291],[103.980264,33.670852],[103.861388,33.682307],[103.778236,33.658898],[103.690772,33.69376],[103.667983,33.685793],[103.645809,33.708697],[103.593454,33.716164],[103.563889,33.699735],[103.552186,33.671351],[103.520157,33.678323],[103.545411,33.719649],[103.518309,33.807213],[103.464723,33.80224],[103.434542,33.752993],[103.35447,33.743539],[103.278709,33.774387],[103.284868,33.80224],[103.24976,33.814175],[103.228202,33.79478],[103.165376,33.805721],[103.153673,33.819147],[103.181391,33.900649],[103.16476,33.929454],[103.1315,33.931937],[103.120413,33.953286],[103.157369,33.998944],[103.147514,34.036644],[103.119797,34.03466],[103.129652,34.065899],[103.178927,34.079779],[103.121644,34.112487],[103.124108,34.162022],[103.100087,34.181828],[103.052043,34.195194],[103.005848,34.184798],[102.973203,34.205588],[102.977515,34.252595],[102.949181,34.292159],[102.911609,34.312923],[102.85987,34.301058],[102.856791,34.270895],[102.798276,34.272874],[102.779798,34.236764],[102.728675,34.235774],[102.694799,34.198659],[102.664002,34.192719],[102.651067,34.165983],[102.598712,34.14766],[102.655994,34.113478],[102.649219,34.080275],[102.615958,34.099604],[102.511865,34.086222],[102.471213,34.072839],[102.437336,34.087214],[102.406539,34.033172],[102.392372,33.971651],[102.345561,33.969666],[102.315996,33.993983],[102.287047,33.977607],[102.248858,33.98654],[102.226069,33.963214],[102.16817,33.983066],[102.136142,33.965199],[102.25317,33.861399],[102.261177,33.821136],[102.243315,33.786823],[102.296286,33.783838],[102.324619,33.754486],[102.284583,33.719151],[102.342481,33.725622],[102.31538,33.665374],[102.346793,33.605582],[102.440416,33.574673],[102.477988,33.543254],[102.446575,33.53228],[102.461358,33.501345],[102.462589,33.449429],[102.447807,33.454922],[102.392988,33.404477],[102.368967,33.41247],[102.310452,33.397982],[102.296286,33.413969],[102.258098,33.409472],[102.218062,33.349503],[102.192192,33.337005],[102.217446,33.247961],[102.200815,33.223434],[102.160163,33.242956],[102.144765,33.273983],[102.117047,33.288492],[102.08933,33.227439],[102.08933,33.204908],[102.054838,33.189884],[101.99386,33.1999],[101.935345,33.186879],[101.921795,33.153817],[101.887302,33.135778],[101.865744,33.103198],[101.825708,33.119239],[101.841723,33.184876],[101.83002,33.213921],[101.770274,33.248962],[101.769658,33.26898],[101.877447,33.314502],[101.887302,33.383991],[101.915635,33.425957],[101.946432,33.442937],[101.906396,33.48188],[101.907012,33.539264],[101.884222,33.578163],[101.844186,33.602591],[101.831252,33.554726],[101.783208,33.556721],[101.769042,33.538765],[101.777665,33.533776],[101.769042,33.45592],[101.695745,33.433948],[101.663716,33.383991],[101.64955,33.323004],[101.677883,33.297497],[101.735781,33.279987],[101.709912,33.21292],[101.653861,33.162835],[101.661252,33.135778],[101.633535,33.101193],[101.557775,33.167344],[101.515275,33.192889],[101.487557,33.226938],[101.403174,33.225436],[101.386543,33.207412],[101.393935,33.157826],[101.381616,33.153316],[101.297232,33.262475],[101.217776,33.256469],[101.182668,33.26948],[101.156798,33.236449],[101.124769,33.221431],[101.11553,33.194893],[101.169733,33.10019],[101.143863,33.086151],[101.146327,33.056563],[101.184515,33.041514],[101.171581,33.009902],[101.183899,32.984304],[101.129081,32.989324],[101.134624,32.95217],[101.124153,32.909976],[101.178356,32.892892],[101.223935,32.855698],[101.237486,32.825026],[101.22332,32.725898],[101.157414,32.661431],[101.124769,32.658408],[101.077342,32.68259],[101.030531,32.660424],[100.99727,32.627668],[100.956618,32.621116],[100.93198,32.600447],[100.887633,32.632708],[100.834046,32.648835],[100.77122,32.643795],[100.690532,32.678056],[100.71209,32.645307],[100.710242,32.610026],[100.673286,32.628172],[100.661583,32.616075],[100.657887,32.546484],[100.645568,32.526303],[100.603069,32.553547],[100.54517,32.569687],[100.516837,32.632204],[100.470026,32.694678],[100.450932,32.694678],[100.420135,32.73194],[100.378251,32.698707],[100.399193,32.756101],[100.339447,32.719353],[100.258759,32.742511],[100.231041,32.696189],[100.229809,32.650346],[100.208252,32.606497],[100.189773,32.630692],[100.109701,32.640268],[100.088143,32.668988],[100.139266,32.724388],[100.117093,32.802392],[100.123252,32.837095],[100.064738,32.895907],[100.029629,32.895907],[100.038252,32.929066],[99.956332,32.948152],[99.947709,32.986814],[99.877492,33.045527],[99.877492,32.993339],[99.851007,32.941623],[99.805427,32.940619],[99.788181,32.956689],[99.764159,32.924545],[99.791877,32.883344],[99.766623,32.826032],[99.760464,32.769689],[99.717964,32.732443],[99.700718,32.76667],[99.646515,32.774721],[99.640355,32.790822],[99.589233,32.789312],[99.558436,32.839106],[99.45311,32.862233],[99.376118,32.899927],[99.353944,32.885354],[99.268944,32.878318],[99.24677,32.924043],[99.235067,32.982296],[99.214741,32.991332],[99.196263,33.035493],[99.124814,33.046028],[99.090322,33.079131],[99.024416,33.094675],[99.014561,33.081137],[98.971445,33.098185],[98.967134,33.115229],[98.92217,33.118738],[98.858728,33.150811],[98.804526,33.219428],[98.802062,33.270481],[98.759562,33.276985],[98.779888,33.370497],[98.736157,33.406975],[98.742316,33.477887],[98.725686,33.503341],[98.678258,33.522801],[98.648077,33.548741],[98.652389,33.595114],[98.622824,33.610067],[98.61728,33.637476],[98.6567,33.64744],[98.610505,33.682805],[98.582788,33.731595],[98.539672,33.746525],[98.51873,33.77389],[98.494092,33.768915],[98.492861,33.796272],[98.463295,33.848477],[98.434962,33.843009],[98.407245,33.867362],[98.425723,33.913066],[98.415252,33.956761],[98.440506,33.981577],[98.428187,34.029204],[98.396774,34.053008],[98.399854,34.085231],[98.344419,34.094648],[98.258188,34.083249],[98.206449,34.08424],[98.158405,34.107037],[98.098043,34.122892],[98.028442,34.122892],[97.95453,34.190739],[97.898479,34.209548],[97.8104,34.207568],[97.796849,34.199154],[97.796849,34.199154],[97.789458,34.182818],[97.789458,34.182818],[97.766668,34.158555],[97.665654,34.126855],[97.70261,34.036644],[97.652719,33.998448],[97.660111,33.956264],[97.629314,33.919523],[97.601596,33.929951],[97.52214,33.903133],[97.503662,33.912073],[97.460546,33.887236],[97.395257,33.889224],[97.398336,33.848477],[97.371851,33.842015],[97.373083,33.817655],[97.406344,33.795278],[97.422974,33.754984],[97.418046,33.728608],[97.435293,33.682307],[97.415583,33.605582],[97.450075,33.582152],[97.523372,33.577166],[97.511669,33.520805],[97.552321,33.465906],[97.625618,33.461412],[97.674893,33.432949],[97.754349,33.409972],[97.676125,33.341004],[97.622538,33.337005],[97.607756,33.263976],[97.548626,33.203907],[97.487648,33.168346],[97.498119,33.137783],[97.487032,33.107209],[97.517213,33.097683],[97.542466,33.035995],[97.499966,33.011408],[97.523988,32.988822],[97.438372,32.976271],[97.375547,32.956689],[97.347829,32.895907],[97.376163,32.886359],[97.392793,32.828546],[97.386018,32.77925],[97.429133,32.714318],[97.42359,32.70475],[97.48272,32.654377],[97.535075,32.638252],[97.543698,32.62162],[97.607756,32.614059],[97.616995,32.586329],[97.700763,32.53488],[97.730944,32.527312],[97.795617,32.521257],[97.80732,32.50006],[97.863986,32.499051],[97.880001,32.486431],[97.940363,32.482393],[98.079565,32.415224],[98.107283,32.391476],[98.125145,32.401077],[98.218768,32.342444],[98.208913,32.318171],[98.23047,32.262521],[98.218768,32.234683],[98.260035,32.208862],[98.303151,32.121726],[98.357354,32.087253],[98.404781,32.045159],[98.402933,32.026896],[98.434962,32.007613],[98.432498,31.922825],[98.399238,31.895899],[98.426339,31.856767],[98.414636,31.832365],[98.461448,31.800327],[98.508875,31.751995],[98.516882,31.717383],[98.545831,31.717383],[98.553839,31.660349],[98.619128,31.591555],[98.651157,31.57881],[98.696736,31.538523],[98.714599,31.508935],[98.844562,31.429817],[98.84333,31.416028],[98.887062,31.37465],[98.810685,31.306668],[98.805758,31.279052],[98.773113,31.249382],[98.691809,31.333253],[98.643766,31.338876],[98.616048,31.3036],[98.60373,31.257568],[98.62344,31.221238],[98.602498,31.192062],[98.675179,31.15417],[98.710287,31.1178],[98.712135,31.082954],[98.736772,31.049121],[98.774961,31.031174],[98.806374,30.995783],[98.797135,30.948575],[98.774345,30.908019],[98.797135,30.87926],[98.850105,30.849465],[98.904924,30.782649],[98.957895,30.765166],[98.963438,30.728134],[98.907388,30.698292],[98.92217,30.609225],[98.939417,30.598923],[98.926482,30.569556],[98.932025,30.521623],[98.965286,30.449937],[98.967134,30.33482],[98.986844,30.280569],[98.970829,30.260928],[98.993003,30.215429],[98.9813,30.182843],[98.989308,30.151799],[99.044742,30.079842],[99.036735,30.053945],[99.055213,29.958587],[99.068148,29.931621],[99.0238,29.846009],[99.018873,29.792009],[98.992387,29.677163],[99.014561,29.607464],[99.052133,29.563748],[99.044742,29.520013],[99.066916,29.421018],[99.058909,29.417368],[99.075539,29.316186],[99.114343,29.243628],[99.113727,29.221171],[99.105104,29.162656],[99.118039,29.100971],[99.113727,29.07273],[99.132206,28.94869],[99.123582,28.890021],[99.103872,28.841803],[99.114343,28.765763],[99.134053,28.734806],[99.126662,28.698066],[99.147604,28.640831],[99.183944,28.58882],[99.170394,28.566221],[99.191952,28.494714],[99.187024,28.44],[99.16485,28.425264],[99.200575,28.365774],[99.229524,28.350502],[99.237531,28.317842],[99.28927,28.286227],[99.306516,28.227714],[99.374886,28.18183],[99.412458,28.295186],[99.392748,28.318369],[99.437095,28.398419],[99.404451,28.44421],[99.426625,28.454207],[99.396444,28.491032],[99.403219,28.546246],[99.463581,28.549401],[99.466045,28.579886],[99.504233,28.619294],[99.540573,28.623497],[99.53195,28.677591],[99.553508,28.710664],[99.614486,28.740054],[99.609559,28.784122],[99.625573,28.81454],[99.676696,28.810345],[99.717964,28.846521],[99.722275,28.757369],[99.755536,28.701216],[99.79434,28.699116],[99.834992,28.660788],[99.834376,28.628225],[99.873181,28.631902],[99.875644,28.611939],[99.91876,28.599329],[99.985281,28.529422],[99.990209,28.47683],[100.073977,28.426317],[100.057346,28.368934],[100.136803,28.349975],[100.176223,28.325218],[100.147274,28.288862],[100.188541,28.252493],[100.153433,28.208202],[100.102926,28.201873],[100.091223,28.181302],[100.062274,28.193962],[100.033325,28.184467],[100.021006,28.147008],[100.05673,28.097922],[100.088759,28.029269],[100.120788,28.018703],[100.196549,27.936254],[100.170063,27.907699],[100.210715,27.87702],[100.30865,27.861149],[100.30865,27.830457],[100.28586,27.80611],[100.304954,27.788639],[100.311729,27.724028],[100.327744,27.72032],[100.350534,27.755809],[100.412127,27.816167],[100.442924,27.86644],[100.504518,27.852154],[100.511294,27.827811],[100.54517,27.809286],[100.609228,27.859033],[100.634482,27.915631],[100.681293,27.923035],[100.719481,27.858503],[100.707162,27.800816],[100.757053,27.770107],[100.775532,27.743098],[100.782307,27.691708],[100.848212,27.672099],[100.827886,27.615904],[100.854988,27.623858],[100.91227,27.521473],[100.901183,27.453517],[100.936908,27.469448],[100.95169,27.426961],[101.021907,27.332899],[101.026219,27.270679],[101.042233,27.22173],[101.071798,27.194585],[101.119226,27.208957],[101.167885,27.198311],[101.167885,27.198311]]],[[[106.264167,30.20974],[106.260471,30.207672],[106.260471,30.204051],[106.260471,30.19681],[106.264167,30.20974]]],[[[106.976809,30.088127],[106.975577,30.088127],[106.976193,30.083467],[106.981736,30.08502],[106.980504,30.087609],[106.979888,30.088127],[106.978656,30.087609],[106.977425,30.087609],[106.976809,30.088127]]],[[[105.558916,30.18543],[105.556453,30.187499],[105.550909,30.179222],[105.56138,30.183878],[105.558916,30.18543]]]]}},
    {"type":"Feature","properties":{"adcode":520000,"name":"贵州省","center":[106.713478,26.578343],"centroid":[106.880455,26.826368],"childrenNum":9,"level":"province","parent":{"adcode":100000},"subFeatureIndex":23,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[109.274262,28.494714],[109.23361,28.474726],[109.191726,28.471043],[109.153538,28.417369],[109.152306,28.349975],[109.117198,28.277795],[109.081473,28.247749],[109.101799,28.202401],[109.086401,28.184467],[109.026655,28.220331],[109.005713,28.162837],[108.929952,28.19027],[108.923793,28.217167],[108.89546,28.219804],[108.855424,28.199764],[108.821547,28.245113],[108.772888,28.212949],[108.738395,28.228241],[108.726692,28.282011],[108.761801,28.304143],[108.783359,28.380518],[108.759953,28.389995],[108.780279,28.42579],[108.746402,28.45105],[108.709446,28.501026],[108.700207,28.48209],[108.657091,28.47683],[108.640461,28.456838],[108.688504,28.422106],[108.697127,28.401051],[108.656475,28.359981],[108.667562,28.334173],[108.611512,28.324691],[108.580099,28.343128],[108.576403,28.38631],[108.609048,28.407368],[108.609664,28.43579],[108.586874,28.463678],[108.573939,28.531],[108.610896,28.539412],[108.604736,28.590922],[108.636149,28.621396],[108.575787,28.659738],[108.50249,28.63768],[108.501258,28.626649],[108.439049,28.634003],[108.332491,28.679166],[108.347274,28.736381],[108.385462,28.772058],[108.386078,28.803003],[108.352817,28.815589],[108.346658,28.859625],[108.357745,28.893165],[108.345426,28.943453],[108.319556,28.961258],[108.297999,29.045527],[108.306622,29.079006],[108.277673,29.091558],[108.256115,29.040295],[108.193289,29.072207],[108.150173,29.053375],[108.070717,29.086328],[108.026369,29.039772],[107.925971,29.032446],[107.908725,29.007327],[107.882855,29.00628],[107.867457,28.960211],[107.810175,28.984295],[107.823725,29.034016],[107.784921,29.048143],[107.810791,29.139137],[107.749197,29.199754],[107.700537,29.141228],[107.659885,29.162656],[107.605683,29.164747],[107.589052,29.150113],[107.570574,29.218037],[107.486806,29.174153],[107.441227,29.203934],[107.401807,29.184603],[107.408582,29.138091],[107.427676,29.128682],[107.412278,29.094696],[107.369778,29.091558],[107.395647,29.041341],[107.364235,29.00942],[107.396879,28.993718],[107.412894,28.960211],[107.441227,28.943977],[107.41351,28.911502],[107.383945,28.848618],[107.339597,28.845997],[107.327894,28.810869],[107.261373,28.792514],[107.24659,28.76209],[107.219489,28.772582],[107.210866,28.817686],[107.227496,28.836037],[107.194851,28.838134],[107.206554,28.868535],[107.14188,28.887925],[107.016229,28.882685],[107.019308,28.861722],[106.983584,28.851239],[106.988512,28.776254],[106.951555,28.766812],[106.923222,28.809821],[106.872099,28.777304],[106.845614,28.780975],[106.824056,28.756319],[106.86594,28.690192],[106.889345,28.695966],[106.866556,28.624548],[106.830831,28.623497],[106.807425,28.589346],[106.784636,28.626649],[106.756918,28.607211],[106.77786,28.563068],[106.73844,28.554657],[106.726121,28.51838],[106.747063,28.467361],[106.708259,28.450524],[106.697788,28.47683],[106.632499,28.503655],[106.564745,28.485247],[106.567825,28.523638],[106.615252,28.549401],[106.606629,28.593024],[106.63681,28.622972],[106.618332,28.645033],[106.651593,28.649235],[106.617716,28.66709],[106.6171,28.691242],[106.587535,28.691767],[106.56105,28.719062],[106.561666,28.756319],[106.474202,28.832891],[106.45326,28.817162],[106.461883,28.761041],[106.492064,28.742153],[106.528405,28.677591],[106.502535,28.661313],[106.49268,28.591448],[106.466811,28.586193],[106.504999,28.544669],[106.477282,28.530474],[106.403369,28.569901],[106.37442,28.525742],[106.379348,28.479986],[106.349167,28.473674],[106.304819,28.505233],[106.2925,28.537309],[106.254928,28.539412],[106.184711,28.58882],[106.17116,28.629275],[106.14837,28.642932],[106.103407,28.636104],[106.085544,28.681792],[106.030726,28.694917],[106.001161,28.743727],[105.966668,28.761041],[105.937719,28.686517],[105.889676,28.670765],[105.884748,28.595126],[105.808372,28.599855],[105.78435,28.610889],[105.757249,28.590397],[105.74493,28.616668],[105.712901,28.586718],[105.693191,28.58882],[105.68272,28.534154],[105.62359,28.517854],[105.612503,28.438947],[105.643916,28.431053],[105.655003,28.362615],[105.639604,28.324164],[105.68888,28.284119],[105.730147,28.271997],[105.737539,28.30309],[105.76464,28.308359],[105.76464,28.308359],[105.78743,28.335753],[105.824386,28.306251],[105.848408,28.255656],[105.889676,28.237732],[105.860727,28.159672],[105.895219,28.119565],[105.943878,28.143314],[105.975907,28.107952],[106.093552,28.162837],[106.145291,28.162837],[106.206885,28.134343],[106.266631,28.066769],[106.246305,28.011835],[106.286341,28.007079],[106.328225,27.952643],[106.307899,27.936782],[106.304819,27.899237],[106.325145,27.898708],[106.337464,27.859033],[106.306667,27.808756],[106.242609,27.767459],[106.193334,27.75422],[106.120653,27.779638],[106.063987,27.776991],[106.023335,27.746805],[105.985146,27.749983],[105.92848,27.729855],[105.922937,27.746805],[105.868118,27.732504],[105.848408,27.707074],[105.76772,27.7182],[105.722756,27.706015],[105.720292,27.683759],[105.664242,27.683759],[105.62359,27.666269],[105.605112,27.715552],[105.560148,27.71979],[105.508409,27.769048],[105.44004,27.775402],[105.353809,27.748924],[105.308229,27.704955],[105.29591,27.631811],[105.304533,27.611661],[105.25649,27.582491],[105.232469,27.546945],[105.260186,27.514573],[105.234316,27.489093],[105.233084,27.436522],[105.182577,27.367451],[105.184425,27.392959],[105.120984,27.418461],[105.068013,27.418461],[105.01073,27.379143],[104.913412,27.327051],[104.871528,27.290891],[104.851818,27.299401],[104.856746,27.332368],[104.824717,27.3531],[104.77113,27.317481],[104.7545,27.345658],[104.611602,27.306846],[104.570334,27.331836],[104.539537,27.327583],[104.497037,27.414743],[104.467472,27.414211],[104.363378,27.467855],[104.30856,27.407305],[104.295625,27.37436],[104.247582,27.336621],[104.248813,27.291955],[104.210625,27.297273],[104.173053,27.263232],[104.113923,27.338216],[104.084358,27.330773],[104.01722,27.383926],[104.015372,27.429086],[103.956242,27.425367],[103.932221,27.443958],[103.905119,27.38552],[103.903271,27.347785],[103.874322,27.331304],[103.865699,27.28185],[103.80041,27.26536],[103.801641,27.250464],[103.748671,27.210021],[103.696316,27.126429],[103.63349,27.12057],[103.620555,27.096598],[103.652584,27.092868],[103.659975,27.065692],[103.614396,27.079548],[103.601461,27.061962],[103.623635,27.035312],[103.623019,27.007056],[103.675374,27.051836],[103.704939,27.049171],[103.73204,27.018785],[103.753598,26.963858],[103.775156,26.951056],[103.763453,26.905702],[103.779468,26.87421],[103.722185,26.851253],[103.705555,26.794642],[103.725265,26.742812],[103.773308,26.716621],[103.759142,26.689355],[103.748671,26.623568],[103.763453,26.585041],[103.815808,26.55239],[103.819504,26.529903],[103.865699,26.512232],[103.953163,26.521336],[104.008597,26.511697],[104.067727,26.51491],[104.068343,26.573266],[104.121314,26.638012],[104.160734,26.646571],[104.222328,26.620358],[104.268524,26.617683],[104.274683,26.633733],[104.313487,26.612867],[104.353523,26.620893],[104.398487,26.686147],[104.424356,26.709137],[104.468088,26.644431],[104.459465,26.602701],[104.488414,26.579689],[104.556783,26.590393],[104.579573,26.568449],[104.57095,26.524549],[104.598667,26.520801],[104.638703,26.477954],[104.631928,26.451702],[104.665804,26.434019],[104.664572,26.397572],[104.684283,26.3772],[104.659645,26.335373],[104.592508,26.317672],[104.542616,26.253282],[104.548776,26.226979],[104.518595,26.165762],[104.52845,26.114186],[104.499501,26.070651],[104.460081,26.085702],[104.470552,26.009352],[104.438523,25.92757],[104.414501,25.909807],[104.441602,25.868889],[104.42374,25.841961],[104.397871,25.76168],[104.370769,25.730415],[104.328886,25.760602],[104.310407,25.647901],[104.332581,25.598796],[104.389248,25.595558],[104.428668,25.576126],[104.436059,25.520512],[104.418813,25.499447],[104.434827,25.472436],[104.44961,25.495126],[104.483486,25.494585],[104.524138,25.526992],[104.556783,25.524832],[104.543232,25.400556],[104.566638,25.402719],[104.615913,25.364871],[104.646094,25.356759],[104.639935,25.295632],[104.689826,25.296173],[104.736021,25.268034],[104.816094,25.262622],[104.826565,25.235558],[104.806854,25.224189],[104.822869,25.170037],[104.801927,25.163537],[104.753884,25.214443],[104.724319,25.195491],[104.732326,25.167871],[104.695369,25.122364],[104.685514,25.078466],[104.619609,25.060577],[104.684898,25.054072],[104.713232,24.996048],[104.663957,24.964584],[104.635623,24.903803],[104.586964,24.872859],[104.539537,24.813663],[104.542616,24.75607],[104.529682,24.731611],[104.595587,24.709323],[104.628848,24.660927],[104.703377,24.645698],[104.729246,24.617953],[104.771746,24.659839],[104.841963,24.676155],[104.865985,24.730524],[104.899245,24.752809],[105.03352,24.787586],[105.026745,24.815836],[105.039064,24.872859],[105.077868,24.918459],[105.09573,24.92877],[105.131454,24.959701],[105.157324,24.958616],[105.178266,24.985199],[105.212758,24.995505],[105.251563,24.967296],[105.267577,24.929313],[105.334099,24.9266],[105.365511,24.943423],[105.428337,24.930941],[105.457286,24.87123],[105.493011,24.833217],[105.497322,24.809318],[105.573083,24.797366],[105.607576,24.803885],[105.617431,24.78161],[105.70551,24.768569],[105.767104,24.719109],[105.827466,24.702799],[105.863806,24.729437],[105.942031,24.725088],[105.961741,24.677786],[106.024566,24.633186],[106.047356,24.684312],[106.113878,24.714216],[106.150218,24.762591],[106.173008,24.760417],[106.206269,24.851139],[106.197645,24.885889],[106.145291,24.954275],[106.191486,24.95319],[106.215508,24.981944],[106.253696,24.971094],[106.304819,24.973807],[106.332536,24.988454],[106.442173,25.019369],[106.450181,25.033468],[106.519782,25.054072],[106.551195,25.082802],[106.590615,25.08768],[106.63989,25.132658],[106.644817,25.164621],[106.691013,25.179245],[106.732281,25.162454],[106.764926,25.183036],[106.787715,25.17112],[106.853005,25.186827],[106.888113,25.181953],[106.904128,25.231768],[106.933077,25.250714],[106.975577,25.232851],[107.013765,25.275611],[107.012533,25.352973],[106.987896,25.358922],[106.963874,25.437852],[106.996519,25.442716],[107.015613,25.495666],[107.066736,25.50917],[107.064272,25.559391],[107.185612,25.578825],[107.205322,25.607971],[107.228728,25.604733],[107.232423,25.556691],[107.263836,25.543193],[107.336517,25.461089],[107.308184,25.432988],[107.318039,25.401637],[107.358691,25.393528],[107.375937,25.411908],[107.420901,25.392987],[107.409198,25.347024],[107.432604,25.289139],[107.481263,25.299961],[107.489886,25.276693],[107.472024,25.213902],[107.512676,25.209029],[107.576734,25.256668],[107.599523,25.250714],[107.632168,25.310241],[107.659885,25.316192],[107.661733,25.258833],[107.696226,25.219858],[107.700537,25.194408],[107.741805,25.24043],[107.762131,25.229061],[107.760283,25.188451],[107.789233,25.15487],[107.762747,25.125073],[107.839124,25.115861],[107.872384,25.141327],[107.928435,25.155954],[108.001732,25.196574],[108.080572,25.193867],[108.115065,25.210112],[108.143398,25.269658],[108.152021,25.324306],[108.142782,25.390825],[108.193289,25.405421],[108.162492,25.444878],[108.192673,25.458928],[108.251803,25.430286],[108.241332,25.46217],[108.280752,25.48],[108.308469,25.525912],[108.348506,25.536173],[108.359592,25.513491],[108.400244,25.491344],[108.418723,25.443257],[108.471693,25.458928],[108.585642,25.365952],[108.589338,25.335125],[108.625062,25.308076],[108.62999,25.335666],[108.600425,25.432448],[108.6072,25.491885],[108.634917,25.520512],[108.68912,25.533473],[108.658323,25.550212],[108.660787,25.584763],[108.68604,25.587462],[108.68912,25.623081],[108.724844,25.634952],[108.783975,25.628477],[108.799989,25.576666],[108.781511,25.554531],[108.814772,25.526992],[108.826474,25.550212],[108.890532,25.556151],[108.8893,25.543193],[108.949046,25.557231],[109.024807,25.51241],[109.088249,25.550752],[109.051908,25.566949],[109.030966,25.629556],[109.075314,25.693749],[109.07901,25.72071],[109.043285,25.738502],[109.007561,25.734728],[108.953974,25.686738],[108.953974,25.686738],[108.900387,25.682423],[108.896076,25.71424],[108.940423,25.740119],[108.963829,25.732572],[108.999553,25.765453],[108.989698,25.778926],[109.048213,25.790781],[109.077778,25.776771],[109.095024,25.80533],[109.143683,25.795092],[109.13198,25.762758],[109.147995,25.741736],[109.206509,25.788087],[109.207125,25.740119],[109.296436,25.71424],[109.340168,25.731493],[109.327849,25.76168],[109.339552,25.83442],[109.359262,25.836036],[109.396834,25.900117],[109.435022,25.93349],[109.408537,25.967392],[109.473211,26.006663],[109.48245,26.029788],[109.452885,26.055598],[109.449805,26.101826],[109.502776,26.096451],[109.513863,26.128157],[109.47629,26.148035],[109.439334,26.238789],[109.467051,26.313917],[109.442414,26.289774],[109.369733,26.277432],[109.351255,26.264016],[109.325385,26.29031],[109.285965,26.295676],[109.271183,26.327863],[109.29582,26.350389],[109.319842,26.418477],[109.38082,26.454381],[109.362342,26.472061],[109.385747,26.493487],[109.381436,26.518659],[109.407305,26.533116],[109.390675,26.598955],[109.35495,26.658873],[109.334008,26.646036],[109.306291,26.661012],[109.283501,26.698445],[109.35495,26.693098],[109.407305,26.719829],[109.447957,26.759913],[109.486761,26.759913],[109.47629,26.829894],[109.467051,26.83203],[109.452885,26.861932],[109.436254,26.892359],[109.555131,26.946788],[109.520022,27.058764],[109.497848,27.079548],[109.486761,27.053968],[109.454733,27.069423],[109.472595,27.134951],[109.441182,27.117907],[109.415312,27.154123],[109.358646,27.153058],[109.33524,27.139212],[109.264407,27.131755],[109.239154,27.14933],[109.21698,27.114711],[109.165857,27.066758],[109.101183,27.06889],[109.128901,27.122701],[109.032814,27.104056],[109.007561,27.08008],[108.940423,27.044907],[108.942887,27.017186],[108.942887,27.017186],[108.877597,27.01612],[108.79075,27.084343],[108.878829,27.106187],[108.926873,27.160512],[108.907778,27.204699],[108.963213,27.235565],[108.983539,27.26802],[109.053756,27.293551],[109.044517,27.331304],[109.103647,27.336621],[109.142451,27.418461],[109.141835,27.448207],[109.167089,27.41793],[109.202197,27.450331],[109.245313,27.41793],[109.300132,27.423774],[109.303211,27.47582],[109.404841,27.55066],[109.461508,27.567637],[109.451037,27.586204],[109.470131,27.62863],[109.45658,27.673689],[109.470747,27.680049],[109.414081,27.725087],[109.366653,27.721909],[109.37774,27.736741],[109.332777,27.782815],[109.346943,27.838396],[109.32169,27.868027],[109.30198,27.956343],[109.319842,27.988585],[109.362342,28.007608],[109.378972,28.034551],[109.335856,28.063073],[109.298284,28.036136],[109.314298,28.103729],[109.33832,28.141731],[109.340168,28.19027],[109.367885,28.254602],[109.388211,28.268307],[109.33524,28.293605],[109.317994,28.277795],[109.275494,28.313101],[109.268719,28.33786],[109.289045,28.373673],[109.264407,28.392628],[109.260712,28.46473],[109.274262,28.494714]]],[[[109.47629,26.829894],[109.486761,26.759913],[109.52187,26.749226],[109.522486,26.749226],[109.497232,26.815474],[109.513247,26.84004],[109.509551,26.877947],[109.486761,26.895562],[109.452885,26.861932],[109.467051,26.83203],[109.47629,26.829894]]],[[[109.528645,26.743881],[109.568065,26.726243],[109.597015,26.756173],[109.554515,26.73533],[109.528645,26.743881]]]]}},
    {"type":"Feature","properties":{"adcode":530000,"name":"云南省","center":[102.712251,25.040609],"centroid":[101.485106,25.008643],"childrenNum":16,"level":"province","parent":{"adcode":100000},"subFeatureIndex":24,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[105.308229,27.704955],[105.290367,27.712373],[105.293447,27.770637],[105.273736,27.794992],[105.313157,27.810874],[105.25957,27.827811],[105.233084,27.895534],[105.284823,27.935725],[105.270657,27.99704],[105.247867,28.009193],[105.218302,27.990698],[105.186273,27.995454],[105.167795,28.021345],[105.186889,28.054623],[105.168411,28.071522],[105.119752,28.07205],[105.061853,28.096866],[105.002107,28.064129],[104.980549,28.063073],[104.975006,28.020816],[104.903557,27.962158],[104.918339,27.938897],[104.888158,27.914574],[104.842579,27.900294],[104.796999,27.901352],[104.761891,27.884426],[104.743413,27.901881],[104.676275,27.880723],[104.63316,27.850567],[104.607906,27.857974],[104.573413,27.840512],[104.52537,27.889187],[104.508124,27.878078],[104.44961,27.927794],[104.40095,27.952114],[104.362762,28.012891],[104.30856,28.036136],[104.304248,28.050926],[104.373233,28.051454],[104.40095,28.091586],[104.448994,28.113758],[104.444682,28.16231],[104.406494,28.173389],[104.402182,28.202928],[104.442834,28.211366],[104.462544,28.241422],[104.44961,28.269889],[104.420045,28.269889],[104.392943,28.291497],[104.384936,28.329959],[104.343052,28.334173],[104.314103,28.306778],[104.282074,28.343128],[104.254357,28.403683],[104.267908,28.499448],[104.260516,28.536257],[104.323342,28.540989],[104.355987,28.555183],[104.375697,28.5946],[104.417581,28.598279],[104.425588,28.626649],[104.372617,28.649235],[104.314719,28.615617],[104.277147,28.631902],[104.252509,28.660788],[104.230951,28.635579],[104.170589,28.642932],[104.117618,28.634003],[104.09606,28.603533],[104.05972,28.6277],[103.953779,28.600906],[103.910047,28.631377],[103.887873,28.61982],[103.850917,28.66709],[103.833054,28.605109],[103.838598,28.587244],[103.802873,28.563068],[103.781931,28.525216],[103.829975,28.459995],[103.828743,28.44],[103.860156,28.383677],[103.85338,28.356822],[103.877402,28.316262],[103.828743,28.285173],[103.770845,28.233514],[103.740048,28.23615],[103.701859,28.198709],[103.692004,28.232459],[103.643961,28.260401],[103.573128,28.230877],[103.533092,28.168641],[103.470266,28.122204],[103.430846,28.044587],[103.459179,28.021345],[103.486281,28.033495],[103.515846,27.965329],[103.55465,27.978543],[103.502295,27.910343],[103.509686,27.843687],[103.487512,27.794992],[103.461027,27.779638],[103.393274,27.709194],[103.369868,27.708664],[103.349542,27.678459],[103.29226,27.632872],[103.2861,27.561802],[103.232514,27.56976],[103.19063,27.523596],[103.144434,27.450331],[103.141355,27.420586],[103.080992,27.396679],[103.055739,27.40943],[102.989833,27.367983],[102.941174,27.405711],[102.899906,27.317481],[102.883892,27.299401],[102.883276,27.258444],[102.904218,27.227584],[102.913457,27.133886],[102.870957,27.026782],[102.894979,27.001724],[102.896211,26.91264],[102.949181,26.843244],[102.966428,26.837904],[102.991681,26.775409],[102.983674,26.76686],[103.008312,26.710741],[103.005232,26.679195],[103.026174,26.664221],[103.035413,26.556673],[103.052659,26.555602],[103.052659,26.514374],[103.030485,26.485989],[102.989833,26.482775],[102.988602,26.413117],[102.998457,26.371839],[102.975667,26.340736],[102.893131,26.338591],[102.878964,26.364332],[102.833385,26.306406],[102.785342,26.298895],[102.739762,26.268846],[102.709581,26.210336],[102.659074,26.221611],[102.60056,26.250598],[102.638748,26.307479],[102.629509,26.336982],[102.570995,26.362723],[102.542046,26.338591],[102.440416,26.300505],[102.392372,26.296749],[102.349257,26.244694],[102.245163,26.212483],[102.242699,26.190468],[102.174946,26.146961],[102.152156,26.10935],[102.107808,26.068501],[102.080091,26.065275],[102.020961,26.096451],[101.954439,26.084627],[101.929186,26.105588],[101.899621,26.099139],[101.857737,26.049146],[101.835563,26.04592],[101.839875,26.082477],[101.796759,26.114723],[101.807846,26.156093],[101.773353,26.168448],[101.737013,26.219463],[101.690202,26.241473],[101.630455,26.224832],[101.586108,26.279579],[101.597195,26.303187],[101.64031,26.318745],[101.660636,26.346635],[101.635383,26.357361],[101.637847,26.388995],[101.565782,26.454381],[101.530057,26.467239],[101.506652,26.499915],[101.458608,26.49563],[101.422884,26.53151],[101.395783,26.591998],[101.402558,26.604841],[101.461688,26.606447],[101.461072,26.640687],[101.481398,26.673313],[101.453065,26.692563],[101.513427,26.768463],[101.466,26.786629],[101.445674,26.77434],[101.458608,26.731054],[101.435819,26.740675],[101.389623,26.723036],[101.387159,26.753501],[101.358826,26.771669],[101.399478,26.841642],[101.365602,26.883819],[101.311399,26.903034],[101.267667,26.903034],[101.264587,26.955323],[101.227015,26.959057],[101.228863,26.981992],[101.136472,27.023584],[101.157414,27.094999],[101.145095,27.103523],[101.170349,27.175421],[101.167885,27.198311],[101.167885,27.198311],[101.119226,27.208957],[101.071798,27.194585],[101.042233,27.22173],[101.026219,27.270679],[101.021907,27.332899],[100.95169,27.426961],[100.936908,27.469448],[100.901183,27.453517],[100.91227,27.521473],[100.854988,27.623858],[100.827886,27.615904],[100.848212,27.672099],[100.782307,27.691708],[100.775532,27.743098],[100.757053,27.770107],[100.707162,27.800816],[100.719481,27.858503],[100.681293,27.923035],[100.634482,27.915631],[100.609228,27.859033],[100.54517,27.809286],[100.511294,27.827811],[100.504518,27.852154],[100.442924,27.86644],[100.412127,27.816167],[100.350534,27.755809],[100.327744,27.72032],[100.311729,27.724028],[100.304954,27.788639],[100.28586,27.80611],[100.30865,27.830457],[100.30865,27.861149],[100.210715,27.87702],[100.170063,27.907699],[100.196549,27.936254],[100.120788,28.018703],[100.088759,28.029269],[100.05673,28.097922],[100.021006,28.147008],[100.033325,28.184467],[100.062274,28.193962],[100.091223,28.181302],[100.102926,28.201873],[100.153433,28.208202],[100.188541,28.252493],[100.147274,28.288862],[100.176223,28.325218],[100.136803,28.349975],[100.057346,28.368934],[100.073977,28.426317],[99.990209,28.47683],[99.985281,28.529422],[99.91876,28.599329],[99.875644,28.611939],[99.873181,28.631902],[99.834376,28.628225],[99.834992,28.660788],[99.79434,28.699116],[99.755536,28.701216],[99.722275,28.757369],[99.717964,28.846521],[99.676696,28.810345],[99.625573,28.81454],[99.609559,28.784122],[99.614486,28.740054],[99.553508,28.710664],[99.53195,28.677591],[99.540573,28.623497],[99.504233,28.619294],[99.466045,28.579886],[99.463581,28.549401],[99.403219,28.546246],[99.396444,28.491032],[99.426625,28.454207],[99.404451,28.44421],[99.437095,28.398419],[99.392748,28.318369],[99.412458,28.295186],[99.374886,28.18183],[99.306516,28.227714],[99.28927,28.286227],[99.237531,28.317842],[99.229524,28.350502],[99.200575,28.365774],[99.16485,28.425264],[99.187024,28.44],[99.191952,28.494714],[99.170394,28.566221],[99.183944,28.58882],[99.147604,28.640831],[99.126662,28.698066],[99.134053,28.734806],[99.114343,28.765763],[99.103872,28.841803],[99.123582,28.890021],[99.132206,28.94869],[99.113727,29.07273],[99.118039,29.100971],[99.105104,29.162656],[99.113727,29.221171],[99.037351,29.20759],[99.024416,29.188783],[98.9813,29.204978],[98.960974,29.165792],[98.967134,29.128159],[98.991771,29.105677],[99.013329,29.036632],[98.925866,28.978536],[98.917859,28.886877],[98.973909,28.864867],[98.972677,28.832367],[98.922786,28.823978],[98.912931,28.800906],[98.852569,28.798283],[98.827932,28.821356],[98.821772,28.920931],[98.786048,28.998952],[98.757714,29.004186],[98.70228,28.9644],[98.655469,28.976966],[98.624056,28.95864],[98.6567,28.910454],[98.643766,28.895261],[98.668403,28.843376],[98.652389,28.817162],[98.683802,28.740054],[98.666555,28.712239],[98.594491,28.667615],[98.637606,28.552029],[98.619128,28.50944],[98.625903,28.489455],[98.673947,28.478934],[98.693041,28.43158],[98.740468,28.348395],[98.746628,28.321003],[98.710287,28.288862],[98.712135,28.229296],[98.649925,28.200291],[98.625903,28.165475],[98.559382,28.182885],[98.494092,28.141203],[98.464527,28.151229],[98.428803,28.104785],[98.389383,28.114814],[98.389999,28.16442],[98.370289,28.18394],[98.37768,28.246167],[98.353042,28.293078],[98.317934,28.324691],[98.301303,28.384204],[98.208913,28.358401],[98.207681,28.330486],[98.231702,28.314681],[98.266811,28.242477],[98.21692,28.212949],[98.169492,28.206093],[98.17442,28.163365],[98.139311,28.142259],[98.160253,28.101089],[98.133152,27.990698],[98.143007,27.948942],[98.187355,27.939426],[98.205217,27.889716],[98.169492,27.851096],[98.215688,27.810874],[98.234166,27.690648],[98.283441,27.654608],[98.310542,27.583552],[98.317318,27.51935],[98.337644,27.508734],[98.388767,27.515104],[98.429419,27.549068],[98.430035,27.653547],[98.444201,27.665209],[98.474998,27.634462],[98.53536,27.620676],[98.554454,27.646126],[98.587099,27.587265],[98.583404,27.571351],[98.650541,27.567637],[98.662244,27.586734],[98.706591,27.553313],[98.685034,27.484315],[98.704744,27.462014],[98.686881,27.425367],[98.702896,27.412618],[98.706591,27.362136],[98.741084,27.330241],[98.734925,27.287168],[98.717062,27.271211],[98.723222,27.221198],[98.696121,27.211086],[98.713983,27.139744],[98.712751,27.075817],[98.765722,27.05077],[98.762642,27.018252],[98.732461,27.002257],[98.757098,26.877947],[98.730613,26.851253],[98.762026,26.798916],[98.746012,26.696841],[98.770033,26.690424],[98.762642,26.660478],[98.781736,26.620893],[98.773113,26.578083],[98.753403,26.559349],[98.757098,26.491881],[98.741084,26.432947],[98.750323,26.424372],[98.733693,26.350926],[98.681338,26.308016],[98.672715,26.239863],[98.713367,26.231274],[98.735541,26.185097],[98.712751,26.156093],[98.720142,26.127082],[98.661012,26.087852],[98.656084,26.139977],[98.632679,26.145887],[98.575396,26.118485],[98.602498,26.054523],[98.614201,25.968468],[98.637606,25.971696],[98.686881,25.925955],[98.705976,25.855426],[98.677642,25.816105],[98.640686,25.798864],[98.553839,25.845731],[98.529201,25.840884],[98.476846,25.77731],[98.461448,25.735267],[98.457752,25.682963],[98.409709,25.664084],[98.402317,25.593939],[98.326557,25.566409],[98.314854,25.543193],[98.247717,25.607971],[98.170724,25.620383],[98.189818,25.569108],[98.163949,25.524292],[98.131304,25.51025],[98.15779,25.457307],[98.137464,25.381633],[98.101123,25.388662],[98.099891,25.354055],[98.06971,25.311864],[98.006884,25.298338],[98.0075,25.279399],[97.940363,25.214985],[97.904023,25.216609],[97.875689,25.25721],[97.839349,25.27074],[97.796233,25.155954],[97.743262,25.078466],[97.719857,25.080634],[97.727864,25.04377],[97.716777,24.978147],[97.729712,24.908689],[97.785762,24.876117],[97.797465,24.845709],[97.765436,24.823984],[97.680437,24.827243],[97.652103,24.790846],[97.569567,24.765852],[97.547394,24.739221],[97.569567,24.708236],[97.570799,24.602719],[97.554785,24.490577],[97.530147,24.443187],[97.588662,24.435559],[97.669966,24.452993],[97.679821,24.401228],[97.716161,24.358711],[97.662574,24.339083],[97.665038,24.296544],[97.721089,24.295999],[97.767284,24.258357],[97.729712,24.227252],[97.72848,24.183585],[97.754349,24.163929],[97.748806,24.160653],[97.743262,24.159561],[97.730944,24.113685],[97.700763,24.093473],[97.697067,24.092927],[97.637321,24.04812],[97.628698,24.004938],[97.572647,23.983068],[97.529531,23.943146],[97.5283,23.926736],[97.618227,23.888438],[97.640401,23.866001],[97.647176,23.840823],[97.684132,23.876946],[97.718009,23.867643],[97.72848,23.895551],[97.763588,23.907041],[97.795617,23.951897],[97.8104,23.943146],[97.863371,23.978693],[97.896015,23.974319],[97.902175,24.014231],[97.984095,24.031177],[97.995182,24.04648],[98.091268,24.085824],[98.096196,24.08637],[98.123297,24.092927],[98.125761,24.092927],[98.132536,24.09238],[98.19721,24.09839],[98.219999,24.113685],[98.343187,24.098936],[98.37768,24.114232],[98.48239,24.122425],[98.487933,24.123517],[98.547063,24.128433],[98.593875,24.08036],[98.646229,24.106038],[98.681954,24.100029],[98.71891,24.127887],[98.818692,24.133348],[98.841482,24.126794],[98.876591,24.15137],[98.895069,24.098936],[98.807606,24.025164],[98.773729,24.022431],[98.727533,23.970491],[98.701048,23.981427],[98.673331,23.960647],[98.701048,23.946427],[98.68565,23.90157],[98.701664,23.834254],[98.669019,23.800857],[98.696121,23.784429],[98.784816,23.781691],[98.824236,23.727462],[98.811917,23.703354],[98.835939,23.683625],[98.847026,23.632097],[98.882134,23.620035],[98.882134,23.595358],[98.844562,23.578904],[98.80391,23.540504],[98.826084,23.470257],[98.874743,23.483431],[98.912315,23.426333],[98.920938,23.360971],[98.872895,23.329651],[98.906772,23.331849],[98.936953,23.309866],[98.928946,23.26589],[98.889525,23.209249],[98.906772,23.185595],[99.002242,23.160287],[99.057677,23.164689],[99.048438,23.11461],[99.106336,23.086536],[99.187024,23.100299],[99.255393,23.077727],[99.281879,23.101399],[99.3484,23.12892],[99.380429,23.099748],[99.440791,23.079379],[99.477747,23.083233],[99.528255,23.065614],[99.517168,23.006685],[99.533798,22.961507],[99.563363,22.925684],[99.531334,22.897019],[99.446951,22.934503],[99.43648,22.913557],[99.462965,22.844635],[99.401371,22.826434],[99.385357,22.761882],[99.326842,22.751396],[99.31514,22.737598],[99.339777,22.708894],[99.385973,22.57136],[99.359487,22.535435],[99.382277,22.493418],[99.297277,22.41156],[99.251698,22.393301],[99.278183,22.34626],[99.233836,22.296434],[99.235683,22.250468],[99.207966,22.232188],[99.175321,22.185647],[99.188256,22.162924],[99.156227,22.159599],[99.219669,22.110816],[99.294814,22.109152],[99.35456,22.095845],[99.400139,22.100281],[99.486987,22.128557],[99.516552,22.099726],[99.562747,22.113034],[99.578762,22.098617],[99.581841,22.103053],[99.648979,22.100835],[99.696406,22.067562],[99.762927,22.068117],[99.870101,22.029288],[99.871333,22.067007],[99.972347,22.053141],[99.965571,22.014309],[100.000064,21.973245],[99.982202,21.919401],[99.960028,21.907186],[99.944014,21.821097],[99.991441,21.703821],[100.049339,21.669899],[100.094303,21.702709],[100.131875,21.699929],[100.169447,21.663225],[100.107853,21.585337],[100.123252,21.565302],[100.131259,21.504066],[100.168831,21.482906],[100.184846,21.516315],[100.206404,21.509634],[100.235353,21.466756],[100.298795,21.477894],[100.349302,21.528564],[100.437381,21.533017],[100.48296,21.458958],[100.526692,21.471211],[100.579047,21.451717],[100.691764,21.510748],[100.730568,21.518542],[100.753358,21.555283],[100.789082,21.570867],[100.804481,21.609821],[100.847597,21.634856],[100.870386,21.67268],[100.896872,21.68269],[100.899335,21.684915],[100.936292,21.694368],[100.937524,21.693812],[101.015132,21.707157],[101.089661,21.773865],[101.123537,21.771642],[101.111835,21.746074],[101.116762,21.691032],[101.153102,21.669343],[101.169117,21.590345],[101.146943,21.560293],[101.209153,21.55751],[101.210385,21.509077],[101.225167,21.499055],[101.193138,21.473996],[101.194986,21.424979],[101.142631,21.409379],[101.183899,21.334699],[101.244877,21.302364],[101.246725,21.275598],[101.222088,21.234324],[101.290457,21.17853],[101.387775,21.225956],[101.439514,21.227072],[101.532521,21.252174],[101.601506,21.233208],[101.588572,21.191365],[101.605818,21.172392],[101.672339,21.194713],[101.703136,21.14616],[101.76473,21.147835],[101.794911,21.208104],[101.834331,21.204756],[101.833715,21.252731],[101.791832,21.285636],[101.745636,21.297345],[101.730238,21.336929],[101.749948,21.409379],[101.741324,21.482906],[101.772737,21.512975],[101.755491,21.538027],[101.754875,21.58478],[101.804766,21.577546],[101.828788,21.617054],[101.807846,21.644313],[101.780129,21.640975],[101.76781,21.716054],[101.747484,21.729953],[101.771506,21.833319],[101.740093,21.845541],[101.735165,21.875534],[101.700057,21.897191],[101.701288,21.938832],[101.666796,21.934391],[101.606434,21.967695],[101.626144,22.005986],[101.573789,22.115251],[101.602738,22.131883],[101.596579,22.161262],[101.547304,22.238282],[101.56455,22.269299],[101.625528,22.28259],[101.671723,22.372826],[101.648318,22.400494],[101.672339,22.47517],[101.715455,22.477935],[101.774585,22.506135],[101.824476,22.45692],[101.823244,22.42705],[101.862665,22.389427],[101.901469,22.384447],[101.907628,22.437007],[101.978461,22.427603],[102.046214,22.458026],[102.131214,22.430922],[102.145381,22.397727],[102.179257,22.430369],[102.270416,22.419858],[102.25625,22.457473],[102.322771,22.554227],[102.356648,22.563623],[102.404691,22.629925],[102.384365,22.679631],[102.43672,22.699508],[102.45951,22.762986],[102.510633,22.774574],[102.551285,22.743669],[102.569763,22.701164],[102.607335,22.730975],[102.657226,22.687913],[102.688639,22.70006],[102.80074,22.620534],[102.82353,22.623296],[102.880196,22.586832],[102.892515,22.533223],[102.930703,22.482359],[102.986754,22.477935],[103.030485,22.441432],[103.081608,22.454154],[103.071753,22.488441],[103.183238,22.558649],[103.161065,22.590147],[103.195557,22.648153],[103.220195,22.643734],[103.283021,22.678526],[103.288564,22.732078],[103.321209,22.777885],[103.323057,22.807678],[103.375411,22.794989],[103.441317,22.753052],[103.436389,22.6973],[103.457947,22.658646],[103.50907,22.601198],[103.529396,22.59291],[103.580519,22.66693],[103.567585,22.701164],[103.642113,22.794989],[103.740048,22.709446],[103.743127,22.697852],[103.766533,22.688465],[103.825047,22.615562],[103.863851,22.584069],[103.875554,22.565833],[103.894032,22.564728],[103.964865,22.502265],[104.009213,22.517745],[104.009213,22.575228],[104.022148,22.593463],[104.04309,22.67687],[104.045553,22.728215],[104.089901,22.768504],[104.117618,22.808781],[104.224176,22.826434],[104.261748,22.841877],[104.274067,22.828088],[104.256821,22.77347],[104.272835,22.73815],[104.323342,22.728767],[104.375697,22.690122],[104.422508,22.734838],[104.498885,22.774574],[104.527834,22.814298],[104.596203,22.846289],[104.674428,22.817056],[104.737869,22.825882],[104.732942,22.852356],[104.760659,22.862282],[104.772362,22.893711],[104.846275,22.926235],[104.860441,22.970874],[104.821021,23.032022],[104.804391,23.110207],[104.874608,23.123417],[104.882615,23.163589],[104.912796,23.175693],[104.949136,23.152033],[104.958991,23.188896],[105.093266,23.260942],[105.122215,23.247745],[105.181962,23.279084],[105.238012,23.26424],[105.260186,23.31811],[105.325475,23.390086],[105.353809,23.362069],[105.372903,23.317561],[105.416018,23.283482],[105.445584,23.292827],[105.50225,23.202648],[105.542902,23.184495],[105.526272,23.234548],[105.560148,23.257093],[105.593409,23.312614],[105.649459,23.346136],[105.699966,23.327453],[105.694423,23.363168],[105.637757,23.404366],[105.699966,23.40162],[105.758481,23.459826],[105.805908,23.467512],[105.815763,23.507031],[105.852103,23.526786],[105.89214,23.52514],[105.913081,23.499348],[105.935871,23.508678],[105.986378,23.489469],[105.999929,23.447748],[106.039965,23.484529],[106.071994,23.495506],[106.08616,23.524043],[106.141595,23.569579],[106.120653,23.605229],[106.149602,23.665538],[106.157609,23.724175],[106.136667,23.795381],[106.192102,23.824947],[106.173008,23.861622],[106.192718,23.879135],[106.157609,23.891174],[106.128044,23.956819],[106.091088,23.998924],[106.096631,24.018058],[106.053516,24.051399],[106.04982,24.089649],[106.011632,24.099482],[105.998081,24.120786],[105.963589,24.110954],[105.919241,24.122425],[105.901995,24.099482],[105.908154,24.069432],[105.89214,24.040468],[105.859495,24.056864],[105.841633,24.03063],[105.796669,24.023524],[105.802212,24.051945],[105.765256,24.073804],[105.739387,24.059596],[105.704278,24.0667],[105.649459,24.032816],[105.628518,24.126794],[105.594641,24.137718],[105.533663,24.130071],[105.493011,24.016965],[105.406163,24.043748],[105.395692,24.065607],[105.334099,24.094566],[105.320548,24.116416],[105.273121,24.092927],[105.292831,24.074896],[105.260186,24.061236],[105.20044,24.105491],[105.182577,24.167205],[105.229389,24.165567],[105.24294,24.208695],[105.215222,24.214699],[105.164715,24.288362],[105.196744,24.326541],[105.188121,24.347261],[105.138846,24.376701],[105.111744,24.37234],[105.106817,24.414853],[105.042759,24.442097],[104.979933,24.412673],[104.930042,24.411038],[104.914028,24.426296],[104.83642,24.446456],[104.784681,24.443732],[104.765587,24.45953],[104.74834,24.435559],[104.715695,24.441552],[104.703377,24.419757],[104.721239,24.340173],[104.70892,24.321087],[104.641783,24.367979],[104.610986,24.377246],[104.63008,24.397958],[104.616529,24.421937],[104.575877,24.424661],[104.550008,24.518894],[104.520443,24.535228],[104.489646,24.653313],[104.529682,24.731611],[104.542616,24.75607],[104.539537,24.813663],[104.586964,24.872859],[104.635623,24.903803],[104.663957,24.964584],[104.713232,24.996048],[104.684898,25.054072],[104.619609,25.060577],[104.685514,25.078466],[104.695369,25.122364],[104.732326,25.167871],[104.724319,25.195491],[104.753884,25.214443],[104.801927,25.163537],[104.822869,25.170037],[104.806854,25.224189],[104.826565,25.235558],[104.816094,25.262622],[104.736021,25.268034],[104.689826,25.296173],[104.639935,25.295632],[104.646094,25.356759],[104.615913,25.364871],[104.566638,25.402719],[104.543232,25.400556],[104.556783,25.524832],[104.524138,25.526992],[104.483486,25.494585],[104.44961,25.495126],[104.434827,25.472436],[104.418813,25.499447],[104.436059,25.520512],[104.428668,25.576126],[104.389248,25.595558],[104.332581,25.598796],[104.310407,25.647901],[104.328886,25.760602],[104.370769,25.730415],[104.397871,25.76168],[104.42374,25.841961],[104.441602,25.868889],[104.414501,25.909807],[104.438523,25.92757],[104.470552,26.009352],[104.460081,26.085702],[104.499501,26.070651],[104.52845,26.114186],[104.518595,26.165762],[104.548776,26.226979],[104.542616,26.253282],[104.592508,26.317672],[104.659645,26.335373],[104.684283,26.3772],[104.664572,26.397572],[104.665804,26.434019],[104.631928,26.451702],[104.638703,26.477954],[104.598667,26.520801],[104.57095,26.524549],[104.579573,26.568449],[104.556783,26.590393],[104.488414,26.579689],[104.459465,26.602701],[104.468088,26.644431],[104.424356,26.709137],[104.398487,26.686147],[104.353523,26.620893],[104.313487,26.612867],[104.274683,26.633733],[104.268524,26.617683],[104.222328,26.620358],[104.160734,26.646571],[104.121314,26.638012],[104.068343,26.573266],[104.067727,26.51491],[104.008597,26.511697],[103.953163,26.521336],[103.865699,26.512232],[103.819504,26.529903],[103.815808,26.55239],[103.763453,26.585041],[103.748671,26.623568],[103.759142,26.689355],[103.773308,26.716621],[103.725265,26.742812],[103.705555,26.794642],[103.722185,26.851253],[103.779468,26.87421],[103.763453,26.905702],[103.775156,26.951056],[103.753598,26.963858],[103.73204,27.018785],[103.704939,27.049171],[103.675374,27.051836],[103.623019,27.007056],[103.623635,27.035312],[103.601461,27.061962],[103.614396,27.079548],[103.659975,27.065692],[103.652584,27.092868],[103.620555,27.096598],[103.63349,27.12057],[103.696316,27.126429],[103.748671,27.210021],[103.801641,27.250464],[103.80041,27.26536],[103.865699,27.28185],[103.874322,27.331304],[103.903271,27.347785],[103.905119,27.38552],[103.932221,27.443958],[103.956242,27.425367],[104.015372,27.429086],[104.01722,27.383926],[104.084358,27.330773],[104.113923,27.338216],[104.173053,27.263232],[104.210625,27.297273],[104.248813,27.291955],[104.247582,27.336621],[104.295625,27.37436],[104.30856,27.407305],[104.363378,27.467855],[104.467472,27.414211],[104.497037,27.414743],[104.539537,27.327583],[104.570334,27.331836],[104.611602,27.306846],[104.7545,27.345658],[104.77113,27.317481],[104.824717,27.3531],[104.856746,27.332368],[104.851818,27.299401],[104.871528,27.290891],[104.913412,27.327051],[105.01073,27.379143],[105.068013,27.418461],[105.120984,27.418461],[105.184425,27.392959],[105.182577,27.367451],[105.233084,27.436522],[105.234316,27.489093],[105.260186,27.514573],[105.232469,27.546945],[105.25649,27.582491],[105.304533,27.611661],[105.29591,27.631811],[105.308229,27.704955]]]]}},
    {"type":"Feature","properties":{"adcode":540000,"name":"西藏自治区","center":[91.132212,29.660361],"centroid":[88.388277,31.56375],"childrenNum":7,"level":"province","parent":{"adcode":100000},"subFeatureIndex":25,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[89.711414,36.093272],[89.614711,36.109712],[89.594385,36.126632],[89.490291,36.151281],[89.375727,36.228078],[89.335075,36.23725],[89.292575,36.231457],[89.232213,36.295636],[89.198952,36.260417],[89.126887,36.254626],[89.10225,36.281164],[89.054822,36.291777],[89.013554,36.315409],[88.964279,36.318785],[88.926091,36.36458],[88.870657,36.348193],[88.838628,36.353496],[88.802903,36.33807],[88.783809,36.291777],[88.766563,36.292259],[88.690186,36.367954],[88.623665,36.389636],[88.618121,36.428168],[88.573158,36.461386],[88.498629,36.446463],[88.470912,36.48208],[88.41055,36.473418],[88.356963,36.477268],[88.366202,36.458016],[88.282434,36.470049],[88.241782,36.468605],[88.222688,36.447426],[88.182652,36.452721],[88.134609,36.427205],[88.092109,36.43539],[88.006494,36.430575],[87.983088,36.437797],[87.95845,36.408423],[87.919646,36.39349],[87.838342,36.383855],[87.826023,36.391563],[87.767509,36.3747],[87.731785,36.384818],[87.6203,36.360243],[87.570409,36.342409],[87.470626,36.354459],[87.460155,36.409868],[87.426895,36.42576],[87.386859,36.412757],[87.363453,36.420463],[87.348055,36.393008],[87.292004,36.358797],[87.193454,36.349158],[87.161425,36.325535],[87.149106,36.297565],[87.08628,36.310587],[87.051788,36.2966],[86.996353,36.308658],[86.943998,36.284058],[86.931064,36.265242],[86.887332,36.262829],[86.86331,36.299977],[86.836209,36.291294],[86.746282,36.291777],[86.69947,36.24449],[86.599072,36.222285],[86.531935,36.227113],[86.515305,36.205385],[86.454943,36.221319],[86.392733,36.206834],[86.35824,36.168676],[86.2794,36.170608],[86.248603,36.141616],[86.187625,36.130983],[86.182081,36.064734],[86.199944,36.047801],[86.173458,36.008113],[86.150668,36.00424],[86.129111,35.941761],[86.093386,35.906868],[86.090306,35.876809],[86.05335,35.842857],[86.035488,35.846738],[85.949256,35.778794],[85.903677,35.78462],[85.835308,35.771996],[85.811286,35.778794],[85.691178,35.751114],[85.65299,35.731199],[85.612953,35.651486],[85.566142,35.6403],[85.518715,35.680658],[85.373969,35.700101],[85.341324,35.753543],[85.271107,35.788989],[85.146071,35.742371],[85.053065,35.752086],[84.99455,35.737028],[84.973608,35.709334],[84.920022,35.696213],[84.798066,35.647595],[84.729081,35.613546],[84.704443,35.616951],[84.628067,35.595055],[84.570168,35.588242],[84.513502,35.564391],[84.448828,35.550272],[84.475929,35.516181],[84.45314,35.473303],[84.424191,35.466479],[84.333032,35.413821],[84.274517,35.404065],[84.200605,35.381135],[84.160569,35.359663],[84.140859,35.379184],[84.095895,35.362592],[84.077417,35.400163],[84.005968,35.422599],[83.906186,35.40309],[83.885244,35.367472],[83.79778,35.354783],[83.785462,35.36308],[83.677672,35.361128],[83.622238,35.335256],[83.599448,35.351366],[83.54155,35.341603],[83.540318,35.364056],[83.502745,35.360639],[83.449159,35.382111],[83.405427,35.380648],[83.333978,35.397236],[83.280391,35.401138],[83.251442,35.417722],[83.178145,35.38943],[83.127022,35.398699],[83.088834,35.425526],[83.067892,35.46258],[82.998907,35.484512],[82.971806,35.548324],[82.981661,35.599922],[82.956407,35.636409],[82.967494,35.667532],[82.894813,35.673852],[82.873871,35.688922],[82.795031,35.688436],[82.780249,35.666073],[82.731589,35.637868],[82.652133,35.67288],[82.628727,35.692324],[82.546192,35.708362],[82.501844,35.701073],[82.468583,35.717595],[82.424852,35.712736],[82.392823,35.656349],[82.336156,35.651486],[82.350323,35.611113],[82.328149,35.559523],[82.2992,35.544916],[82.263475,35.547837],[82.234526,35.520565],[82.189563,35.513258],[82.164925,35.495719],[82.086701,35.467454],[82.071302,35.450393],[82.034346,35.451855],[82.029419,35.426013],[82.05344,35.35039],[82.030034,35.321585],[81.99123,35.30547],[81.955506,35.307423],[81.927789,35.271275],[81.853876,35.25857],[81.804601,35.270786],[81.736847,35.26248],[81.68634,35.235599],[81.513261,35.23511],[81.504638,35.279092],[81.447972,35.318167],[81.441196,35.333303],[81.385762,35.335256],[81.363588,35.354783],[81.314313,35.337209],[81.285364,35.345508],[81.26627,35.322562],[81.219458,35.319144],[81.191741,35.36552],[81.142466,35.365032],[81.103662,35.386015],[81.09935,35.40748],[81.054387,35.402602],[81.031597,35.380648],[81.030981,35.337209],[81.002648,35.334768],[81.026053,35.31133],[80.963844,35.310842],[80.924423,35.330862],[80.894242,35.324027],[80.844351,35.345508],[80.759968,35.334768],[80.689135,35.339162],[80.690982,35.364544],[80.65649,35.393821],[80.599823,35.409431],[80.56841,35.391381],[80.532686,35.404553],[80.514824,35.391869],[80.444607,35.417235],[80.432904,35.449418],[80.375006,35.387966],[80.321419,35.38699],[80.286926,35.35283],[80.267832,35.295701],[80.362687,35.20871],[80.257977,35.203331],[80.223484,35.177409],[80.23026,35.147565],[80.118159,35.066293],[80.078123,35.076578],[80.031311,35.034447],[80.04363,35.022196],[80.02392,34.971209],[80.041782,34.943252],[80.034391,34.902033],[80.003594,34.895162],[79.996819,34.856375],[79.961094,34.862759],[79.926602,34.849499],[79.947544,34.821008],[79.898268,34.732035],[79.906892,34.683821],[79.866856,34.671517],[79.88595,34.642965],[79.84345,34.55725],[79.861312,34.528166],[79.801566,34.478847],[79.735661,34.471447],[79.699936,34.477861],[79.675914,34.451216],[79.58106,34.456151],[79.545335,34.476381],[79.504683,34.45467],[79.435082,34.447761],[79.363017,34.428018],[79.326677,34.44332],[79.274322,34.435916],[79.241677,34.415183],[79.179467,34.422588],[79.161605,34.441345],[79.072294,34.412714],[79.039033,34.421601],[79.0107,34.399877],[79.048888,34.348506],[79.039649,34.33467],[79.019939,34.313417],[78.981751,34.31836],[78.958345,34.230827],[78.941099,34.212022],[78.9257,34.155584],[78.910302,34.143202],[78.878273,34.163012],[78.828998,34.125369],[78.801897,34.137258],[78.737223,34.089692],[78.661462,34.086718],[78.656535,34.030196],[78.736607,33.999937],[78.744614,33.980585],[78.734143,33.918529],[78.762476,33.90959],[78.756317,33.8773],[78.766172,33.823124],[78.758165,33.790802],[78.779723,33.73259],[78.692259,33.676331],[78.684868,33.654415],[78.713201,33.623025],[78.755085,33.623025],[78.74215,33.55323],[78.816679,33.480882],[78.84994,33.419963],[78.896751,33.41247],[78.949722,33.376495],[78.9682,33.334505],[79.022403,33.323504],[79.041497,33.268479],[79.083997,33.245459],[79.072294,33.22844],[79.10925,33.200401],[79.152366,33.184375],[79.162221,33.165841],[79.139431,33.117735],[79.162837,33.01191],[79.204721,32.964724],[79.255844,32.942628],[79.227511,32.89038],[79.237982,32.846145],[79.225047,32.784281],[79.275554,32.778746],[79.301423,32.728919],[79.27309,32.678056],[79.299575,32.637244],[79.308199,32.596918],[79.272474,32.561113],[79.252148,32.516715],[79.190554,32.511669],[79.180083,32.492994],[79.135736,32.472295],[79.124649,32.416235],[79.103091,32.369744],[79.067982,32.380863],[79.005772,32.375304],[78.970664,32.331826],[78.904142,32.374798],[78.87273,32.40512],[78.81052,32.436441],[78.782186,32.480373],[78.760629,32.563635],[78.781571,32.608009],[78.74215,32.654881],[78.741534,32.703743],[78.6861,32.680071],[78.675013,32.658408],[78.628202,32.630188],[78.588782,32.637748],[78.577695,32.615067],[78.518564,32.605993],[78.500086,32.580782],[78.424942,32.565652],[78.395377,32.530339],[78.426174,32.502584],[78.472985,32.435431],[78.458818,32.379853],[78.483456,32.357106],[78.480992,32.329297],[78.508709,32.297939],[78.475449,32.236708],[78.430485,32.212407],[78.429869,32.194683],[78.469905,32.127808],[78.509941,32.147065],[78.527188,32.11463],[78.609107,32.052768],[78.60726,32.023851],[78.705194,31.988835],[78.762476,31.947203],[78.768636,31.92638],[78.739687,31.885228],[78.665158,31.851684],[78.654687,31.819144],[78.706426,31.778453],[78.763092,31.668499],[78.798817,31.675629],[78.806824,31.64099],[78.845628,31.609905],[78.833925,31.584927],[78.779723,31.545154],[78.740303,31.532912],[78.729832,31.478316],[78.755701,31.478316],[78.792041,31.435944],[78.760013,31.392531],[78.755085,31.355742],[78.795121,31.301043],[78.859179,31.289281],[78.865338,31.312804],[78.884432,31.277006],[78.923852,31.246824],[78.930628,31.220726],[78.997765,31.158779],[78.97436,31.115751],[79.010084,31.043994],[79.059359,31.028097],[79.096931,30.992192],[79.181931,31.015788],[79.205953,31.0004],[79.227511,30.949088],[79.33222,30.969103],[79.316206,31.01784],[79.35809,31.031174],[79.404901,31.071678],[79.424611,31.061425],[79.427075,31.018353],[79.505915,31.027584],[79.550879,30.957813],[79.59769,30.925989],[79.660516,30.956787],[79.668523,30.980392],[79.729501,30.941389],[79.75845,30.936769],[79.835443,30.851006],[79.890877,30.855116],[79.913051,30.833022],[79.900732,30.7991],[79.961094,30.771337],[79.955551,30.738422],[79.970333,30.685941],[80.014065,30.661748],[80.04363,30.603559],[80.143412,30.55822],[80.214245,30.586044],[80.261673,30.566465],[80.322035,30.564403],[80.357759,30.520592],[80.43044,30.515952],[80.446454,30.495327],[80.504969,30.483466],[80.549316,30.448905],[80.585041,30.463866],[80.633084,30.458707],[80.692214,30.416913],[80.719316,30.414848],[80.81725,30.321389],[80.910873,30.30279],[80.933662,30.266614],[80.996488,30.267648],[81.034677,30.246971],[81.038372,30.205086],[81.082104,30.151281],[81.085799,30.100554],[81.110437,30.085538],[81.09627,30.052909],[81.131995,30.016124],[81.225618,30.005759],[81.256415,30.011978],[81.247792,30.032705],[81.2829,30.061197],[81.293371,30.094859],[81.269349,30.153351],[81.335871,30.149729],[81.393769,30.199396],[81.397465,30.240767],[81.419023,30.270232],[81.406088,30.291938],[81.427646,30.305373],[81.399929,30.319323],[81.406088,30.369421],[81.432573,30.379231],[81.406704,30.40401],[81.418407,30.420525],[81.454131,30.412268],[81.494783,30.381296],[81.555761,30.369421],[81.566232,30.428782],[81.613044,30.412784],[81.63029,30.446842],[81.723913,30.407623],[81.759021,30.385426],[81.872354,30.373035],[81.939491,30.344633],[81.954274,30.355995],[81.99123,30.322939],[82.022027,30.339468],[82.060215,30.332237],[82.104563,30.346182],[82.132896,30.30434],[82.11873,30.279019],[82.114418,30.226806],[82.142135,30.200948],[82.188947,30.18543],[82.207425,30.143519],[82.183403,30.12178],[82.17786,30.06793],[82.246845,30.071555],[82.311519,30.035813],[82.333693,30.045138],[82.368185,30.014051],[82.412533,30.011978],[82.431011,29.989692],[82.474743,29.973622],[82.498148,29.947698],[82.560974,29.955476],[82.609017,29.886489],[82.64351,29.868846],[82.6238,29.834588],[82.703872,29.847566],[82.737749,29.80655],[82.691553,29.766037],[82.757459,29.761881],[82.774089,29.726548],[82.816589,29.717192],[82.830756,29.687562],[82.885574,29.689122],[82.9484,29.704718],[82.966878,29.658963],[83.011226,29.667804],[83.088834,29.604863],[83.12887,29.623593],[83.159667,29.61735],[83.164595,29.595496],[83.217565,29.60018],[83.266841,29.571035],[83.27608,29.505951],[83.325355,29.502826],[83.383253,29.42206],[83.415898,29.420496],[83.423289,29.361053],[83.450391,29.332883],[83.463941,29.285916],[83.492274,29.280174],[83.548941,29.201322],[83.57789,29.203934],[83.596368,29.174153],[83.656114,29.16736],[83.667201,29.200277],[83.727563,29.244672],[83.800244,29.249372],[83.82057,29.294267],[83.851367,29.294789],[83.911729,29.323491],[83.949301,29.312533],[83.986874,29.325057],[84.002272,29.291658],[84.052163,29.296877],[84.116837,29.286438],[84.130388,29.239972],[84.203068,29.239972],[84.197525,29.210202],[84.17104,29.19453],[84.176583,29.133909],[84.20738,29.118749],[84.192597,29.084236],[84.194445,29.045004],[84.224626,29.049189],[84.248648,29.030353],[84.228322,28.949738],[84.234481,28.889497],[84.268358,28.895261],[84.330568,28.859101],[84.340423,28.866963],[84.408176,28.85386],[84.404481,28.828173],[84.434046,28.823978],[84.445133,28.764189],[84.483321,28.735331],[84.557233,28.74635],[84.620059,28.732182],[84.650856,28.714338],[84.669334,28.680742],[84.699515,28.671816],[84.698284,28.633478],[84.773428,28.610363],[84.857196,28.567798],[84.896616,28.587244],[84.981616,28.586193],[84.995782,28.611414],[85.05676,28.674441],[85.126361,28.676016],[85.155926,28.643983],[85.195963,28.624022],[85.18426,28.587244],[85.189803,28.544669],[85.160238,28.49261],[85.108499,28.461047],[85.129441,28.377885],[85.113427,28.344708],[85.179948,28.324164],[85.209513,28.338914],[85.272339,28.282538],[85.349947,28.298347],[85.379512,28.274105],[85.415853,28.321003],[85.458969,28.332593],[85.520563,28.326798],[85.602483,28.295712],[85.601251,28.254075],[85.650526,28.283592],[85.682555,28.375779],[85.720743,28.372093],[85.753388,28.227714],[85.791576,28.195544],[85.854402,28.172334],[85.871648,28.124843],[85.898749,28.101617],[85.901213,28.053566],[85.980053,27.984357],[85.949256,27.937311],[86.002227,27.90717],[86.053966,27.900823],[86.125415,27.923035],[86.082915,28.018175],[86.086611,28.090002],[86.128495,28.086835],[86.140198,28.114814],[86.19132,28.167058],[86.223965,28.092642],[86.206103,28.084195],[86.231972,27.974315],[86.27324,27.976958],[86.308965,27.950528],[86.393349,27.926736],[86.414906,27.904526],[86.450015,27.908757],[86.475884,27.944713],[86.514689,27.954757],[86.513457,27.996511],[86.537478,28.044587],[86.55842,28.047757],[86.568891,28.103201],[86.60092,28.097922],[86.611391,28.069938],[86.647732,28.06941],[86.662514,28.092114],[86.700086,28.101617],[86.74813,28.089474],[86.768456,28.06941],[86.756753,28.032967],[86.827586,28.012363],[86.864542,28.022401],[86.885484,27.995983],[86.926752,27.985942],[86.935375,27.955286],[87.035157,27.946299],[87.080737,27.910872],[87.118309,27.840512],[87.173744,27.818284],[87.227946,27.812991],[87.249504,27.839454],[87.280917,27.845275],[87.317258,27.826753],[87.364069,27.824106],[87.421967,27.856916],[87.418272,27.825694],[87.45954,27.820931],[87.58088,27.859562],[87.598126,27.814579],[87.670191,27.832045],[87.668343,27.809815],[87.727473,27.802933],[87.77798,27.860091],[87.782292,27.890774],[87.826639,27.927794],[87.930733,27.909285],[87.982472,27.884426],[88.037291,27.901881],[88.090877,27.885484],[88.111819,27.864852],[88.137689,27.878607],[88.120442,27.915103],[88.156783,27.957929],[88.203594,27.943127],[88.242398,27.967444],[88.254101,27.939426],[88.357579,27.986471],[88.401311,27.976958],[88.43334,28.002852],[88.469064,28.009721],[88.498013,28.04089],[88.554064,28.027684],[88.565151,28.083139],[88.620585,28.091586],[88.645223,28.111119],[88.67602,28.068353],[88.764099,28.068353],[88.812142,28.018175],[88.842939,28.006023],[88.846635,27.921448],[88.864497,27.921448],[88.888519,27.846863],[88.863265,27.811932],[88.870657,27.743098],[88.850331,27.710783],[88.852178,27.671039],[88.816454,27.641354],[88.813374,27.606889],[88.770874,27.563924],[88.797976,27.521473],[88.783193,27.467324],[88.809063,27.405711],[88.838012,27.37808],[88.867577,27.3818],[88.901453,27.327583],[88.920548,27.325456],[88.911924,27.272807],[88.942105,27.261636],[88.984605,27.208957],[89.067757,27.240354],[89.077612,27.287168],[89.152757,27.319076],[89.182938,27.373829],[89.132431,27.441302],[89.095474,27.471572],[89.109025,27.537925],[89.163228,27.574534],[89.128735,27.611131],[89.131815,27.633402],[89.184786,27.673689],[89.238988,27.796581],[89.295655,27.84845],[89.375727,27.875962],[89.44348,27.968501],[89.461958,28.03191],[89.511233,28.086307],[89.541414,28.088418],[89.605472,28.161782],[89.720037,28.170224],[89.779167,28.197127],[89.789638,28.240895],[89.869094,28.221386],[89.901739,28.18183],[89.976268,28.189215],[90.017536,28.162837],[90.03355,28.136981],[90.07297,28.155451],[90.103151,28.141731],[90.124709,28.190797],[90.166593,28.187632],[90.189999,28.161782],[90.231882,28.144897],[90.297172,28.153868],[90.367389,28.088946],[90.384019,28.06096],[90.43699,28.063073],[90.47949,28.044587],[90.513983,28.062016],[90.569417,28.044059],[90.591591,28.021345],[90.701844,28.076274],[90.741264,28.053038],[90.802242,28.040362],[90.806554,28.015005],[90.853365,27.969029],[90.896481,27.946299],[90.96177,27.9537],[90.976553,27.935725],[90.96485,27.900294],[91.025828,27.857445],[91.113292,27.846333],[91.155175,27.894476],[91.147784,27.927794],[91.162567,27.968501],[91.216153,27.989113],[91.251878,27.970615],[91.309776,28.057791],[91.464993,28.002852],[91.490246,27.971672],[91.486551,27.937311],[91.552456,27.90717],[91.611586,27.891303],[91.618978,27.856916],[91.561079,27.855329],[91.544449,27.820401],[91.610355,27.819343],[91.642383,27.7664],[91.622673,27.692238],[91.570934,27.650897],[91.562311,27.627569],[91.582637,27.598933],[91.564775,27.58196],[91.585101,27.540578],[91.626985,27.509265],[91.663325,27.507142],[91.71876,27.467324],[91.753868,27.462545],[91.839484,27.489624],[91.946657,27.464138],[92.010715,27.474758],[92.021802,27.444489],[92.064918,27.391365],[92.125896,27.273339],[92.091403,27.264296],[92.071077,27.237694],[92.061222,27.190327],[92.032273,27.167967],[92.02673,27.108318],[92.043976,27.052902],[92.076005,27.041175],[92.124664,26.960124],[92.109265,26.854991],[92.197961,26.86994],[92.28604,26.892359],[92.404916,26.9025],[92.496691,26.921711],[92.549046,26.941453],[92.64698,26.952656],[92.682089,26.947855],[92.802813,26.895028],[92.909371,26.914241],[93.050421,26.883819],[93.111399,26.880082],[93.232739,26.906769],[93.56781,26.938252],[93.625092,26.955323],[93.747048,27.015587],[93.817265,27.025183],[93.841903,27.045973],[93.849294,27.168499],[93.970634,27.30525],[94.056866,27.375423],[94.147409,27.458297],[94.220705,27.536333],[94.277372,27.58143],[94.353132,27.578778],[94.399944,27.589386],[94.443675,27.585143],[94.478168,27.602116],[94.524979,27.596282],[94.660486,27.650367],[94.722696,27.683759],[94.78121,27.699127],[94.836645,27.728796],[94.88592,27.743098],[94.947514,27.792345],[95.015267,27.82887],[95.067006,27.840512],[95.28628,27.939955],[95.32878,28.017646],[95.352802,28.04089],[95.371896,28.110063],[95.39715,28.142259],[95.437802,28.161782],[95.528345,28.182885],[95.674322,28.254075],[95.740228,28.275159],[95.787655,28.270416],[95.832003,28.295186],[95.874502,28.29782],[95.899756,28.278322],[95.907763,28.241422],[95.936096,28.240368],[95.989067,28.198181],[96.074683,28.193434],[96.098088,28.212421],[96.194175,28.212949],[96.275479,28.228241],[96.298269,28.140148],[96.367254,28.118509],[96.398667,28.118509],[96.395587,28.143842],[96.426384,28.161782],[96.46334,28.143314],[96.499681,28.067297],[96.538485,28.075218],[96.623485,28.024514],[96.635188,27.994926],[96.690622,27.948942],[96.711564,27.9574],[96.784245,27.931495],[96.810114,27.890245],[96.849534,27.874375],[96.908049,27.884426],[96.972722,27.861149],[97.008447,27.807698],[97.049099,27.81405],[97.062649,27.742568],[97.097758,27.740979],[97.103301,27.780697],[97.167975,27.811932],[97.253591,27.891832],[97.303482,27.913516],[97.324424,27.880723],[97.386634,27.882839],[97.372467,27.907699],[97.379242,27.970087],[97.413119,28.01342],[97.378626,28.031382],[97.375547,28.062545],[97.320728,28.054095],[97.305945,28.071522],[97.340438,28.104785],[97.326887,28.132759],[97.352757,28.149646],[97.362612,28.199236],[97.349677,28.235623],[97.398336,28.238786],[97.402032,28.279903],[97.422358,28.297293],[97.461162,28.26778],[97.469169,28.30309],[97.518445,28.327852],[97.488879,28.347341],[97.485184,28.38631],[97.499966,28.428948],[97.521524,28.444736],[97.507974,28.46473],[97.521524,28.495766],[97.569567,28.541515],[97.60406,28.515225],[97.634857,28.532051],[97.68598,28.519958],[97.737103,28.465782],[97.738335,28.396313],[97.769748,28.3742],[97.801161,28.326798],[97.842429,28.326798],[97.871378,28.361561],[97.907718,28.363141],[98.020435,28.253548],[98.008116,28.214003],[98.03337,28.187105],[98.056775,28.202401],[98.090036,28.195544],[98.097427,28.166531],[98.139311,28.142259],[98.17442,28.163365],[98.169492,28.206093],[98.21692,28.212949],[98.266811,28.242477],[98.231702,28.314681],[98.207681,28.330486],[98.208913,28.358401],[98.301303,28.384204],[98.317934,28.324691],[98.353042,28.293078],[98.37768,28.246167],[98.370289,28.18394],[98.389999,28.16442],[98.389383,28.114814],[98.428803,28.104785],[98.464527,28.151229],[98.494092,28.141203],[98.559382,28.182885],[98.625903,28.165475],[98.649925,28.200291],[98.712135,28.229296],[98.710287,28.288862],[98.746628,28.321003],[98.740468,28.348395],[98.693041,28.43158],[98.673947,28.478934],[98.625903,28.489455],[98.619128,28.50944],[98.637606,28.552029],[98.594491,28.667615],[98.666555,28.712239],[98.683802,28.740054],[98.652389,28.817162],[98.668403,28.843376],[98.643766,28.895261],[98.6567,28.910454],[98.624056,28.95864],[98.655469,28.976966],[98.70228,28.9644],[98.757714,29.004186],[98.786048,28.998952],[98.821772,28.920931],[98.827932,28.821356],[98.852569,28.798283],[98.912931,28.800906],[98.922786,28.823978],[98.972677,28.832367],[98.973909,28.864867],[98.917859,28.886877],[98.925866,28.978536],[99.013329,29.036632],[98.991771,29.105677],[98.967134,29.128159],[98.960974,29.165792],[98.9813,29.204978],[99.024416,29.188783],[99.037351,29.20759],[99.113727,29.221171],[99.114343,29.243628],[99.075539,29.316186],[99.058909,29.417368],[99.066916,29.421018],[99.044742,29.520013],[99.052133,29.563748],[99.014561,29.607464],[98.992387,29.677163],[99.018873,29.792009],[99.0238,29.846009],[99.068148,29.931621],[99.055213,29.958587],[99.036735,30.053945],[99.044742,30.079842],[98.989308,30.151799],[98.9813,30.182843],[98.993003,30.215429],[98.970829,30.260928],[98.986844,30.280569],[98.967134,30.33482],[98.965286,30.449937],[98.932025,30.521623],[98.926482,30.569556],[98.939417,30.598923],[98.92217,30.609225],[98.907388,30.698292],[98.963438,30.728134],[98.957895,30.765166],[98.904924,30.782649],[98.850105,30.849465],[98.797135,30.87926],[98.774345,30.908019],[98.797135,30.948575],[98.806374,30.995783],[98.774961,31.031174],[98.736772,31.049121],[98.712135,31.082954],[98.710287,31.1178],[98.675179,31.15417],[98.602498,31.192062],[98.62344,31.221238],[98.60373,31.257568],[98.616048,31.3036],[98.643766,31.338876],[98.691809,31.333253],[98.773113,31.249382],[98.805758,31.279052],[98.810685,31.306668],[98.887062,31.37465],[98.84333,31.416028],[98.844562,31.429817],[98.714599,31.508935],[98.696736,31.538523],[98.651157,31.57881],[98.619128,31.591555],[98.553839,31.660349],[98.545831,31.717383],[98.516882,31.717383],[98.508875,31.751995],[98.461448,31.800327],[98.414636,31.832365],[98.426339,31.856767],[98.399238,31.895899],[98.432498,31.922825],[98.434962,32.007613],[98.402933,32.026896],[98.404781,32.045159],[98.357354,32.087253],[98.303151,32.121726],[98.260035,32.208862],[98.218768,32.234683],[98.23047,32.262521],[98.208913,32.318171],[98.218768,32.342444],[98.125145,32.401077],[98.107283,32.391476],[98.079565,32.415224],[97.940363,32.482393],[97.880001,32.486431],[97.863986,32.499051],[97.80732,32.50006],[97.795617,32.521257],[97.730944,32.527312],[97.684132,32.530339],[97.670582,32.51722],[97.540618,32.536899],[97.50243,32.530844],[97.463626,32.55506],[97.448843,32.586833],[97.411887,32.575235],[97.374315,32.546484],[97.3583,32.563635],[97.332431,32.542448],[97.334895,32.514192],[97.388481,32.501575],[97.341054,32.440987],[97.387865,32.427349],[97.424822,32.322723],[97.415583,32.296421],[97.371235,32.273148],[97.32196,32.303503],[97.299786,32.294904],[97.264062,32.182527],[97.271453,32.139971],[97.313953,32.130342],[97.293011,32.096887],[97.308409,32.076605],[97.258518,32.072041],[97.219714,32.109054],[97.201852,32.090296],[97.233881,32.063927],[97.214786,32.042623],[97.188301,32.055304],[97.169823,32.032984],[97.127323,32.044145],[97.028773,32.04871],[97.006599,32.067984],[96.935766,32.048203],[96.965947,32.008628],[96.941925,31.986297],[96.894498,32.013703],[96.863085,31.996448],[96.868629,31.964975],[96.824281,32.007613],[96.722651,32.013195],[96.742977,32.001016],[96.753448,31.944156],[96.776238,31.935015],[96.81073,31.894375],[96.794716,31.869474],[96.760223,31.860325],[96.765767,31.819144],[96.799027,31.792188],[96.840295,31.720438],[96.790404,31.698545],[96.778701,31.675629],[96.722651,31.686833],[96.691854,31.722474],[96.661057,31.705674],[96.615477,31.737236],[96.56805,31.711783],[96.519391,31.74945],[96.468884,31.769804],[96.435623,31.796258],[96.407906,31.845583],[96.389428,31.919777],[96.288414,31.919777],[96.253305,31.929936],[96.220044,31.905553],[96.188632,31.904028],[96.214501,31.876589],[96.202798,31.841008],[96.183088,31.835924],[96.178161,31.775401],[96.231131,31.749959],[96.222508,31.733164],[96.252073,31.697527],[96.245298,31.657802],[96.221892,31.647613],[96.207726,31.598691],[96.156603,31.602769],[96.148595,31.686324],[96.135661,31.70211],[96.064828,31.720438],[95.989067,31.78761],[95.983524,31.816601],[95.89914,31.81711],[95.846169,31.736218],[95.853561,31.714329],[95.823995,31.68225],[95.779648,31.748941],[95.634286,31.782523],[95.580083,31.76726],[95.546823,31.73978],[95.511714,31.750468],[95.480301,31.795749],[95.456896,31.801853],[95.406389,31.896915],[95.408852,31.918761],[95.3682,31.92892],[95.360809,31.95939],[95.395918,32.001523],[95.454432,32.007613],[95.421171,32.033999],[95.454432,32.061898],[95.440265,32.157705],[95.406389,32.182021],[95.367584,32.178982],[95.366968,32.151118],[95.31523,32.148585],[95.270266,32.194683],[95.270266,32.194683],[95.239469,32.287315],[95.241317,32.3207],[95.214216,32.321712],[95.20744,32.297433],[95.10581,32.258979],[95.079325,32.279726],[95.096571,32.322217],[95.193274,32.332331],[95.261643,32.348006],[95.228382,32.363678],[95.218527,32.397035],[95.153853,32.386423],[95.081789,32.384907],[95.075013,32.376315],[95.075013,32.376315],[95.057151,32.395014],[94.988166,32.422802],[94.944434,32.404109],[94.912405,32.41573],[94.889616,32.472295],[94.852043,32.463712],[94.80708,32.486431],[94.78737,32.522266],[94.762116,32.526303],[94.737479,32.587338],[94.638312,32.645307],[94.614291,32.673522],[94.591501,32.640772],[94.522516,32.595909],[94.459074,32.599439],[94.463386,32.572209],[94.435052,32.562626],[94.395016,32.594397],[94.371611,32.524789],[94.350053,32.533871],[94.294002,32.519743],[94.292154,32.502584],[94.250886,32.51722],[94.196684,32.51621],[94.176974,32.454117],[94.137554,32.433915],[94.091974,32.463207],[94.049474,32.469771],[94.03038,32.448057],[93.978641,32.459672],[93.960163,32.484917],[93.90904,32.463207],[93.861613,32.466237],[93.851142,32.50965],[93.820345,32.549511],[93.75136,32.56313],[93.721795,32.578261],[93.651577,32.571705],[93.618933,32.522771],[93.516687,32.47583],[93.501904,32.503593],[93.476651,32.504603],[93.4631,32.556069],[93.411977,32.558086],[93.385492,32.525294],[93.33868,32.5712],[93.308499,32.580278],[93.300492,32.619604],[93.260456,32.62666],[93.239514,32.662439],[93.210565,32.655385],[93.176688,32.6705],[93.159442,32.644803],[93.087993,32.63674],[93.069515,32.626156],[93.023935,32.703239],[93.019624,32.737477],[93.00053,32.741001],[92.964189,32.714821],[92.933392,32.719353],[92.866871,32.698203],[92.822523,32.729926],[92.789262,32.719856],[92.756618,32.743014],[92.686401,32.76516],[92.667922,32.73194],[92.634662,32.720863],[92.574916,32.741001],[92.56814,32.73194],[92.484372,32.745028],[92.459119,32.76365],[92.411076,32.748048],[92.355641,32.764657],[92.343938,32.738484],[92.310062,32.751571],[92.255243,32.720863],[92.198577,32.754591],[92.211511,32.788306],[92.193649,32.801889],[92.227526,32.821003],[92.205352,32.866255],[92.145606,32.885857],[92.101874,32.860222],[92.038432,32.860725],[92.018722,32.829552],[91.955897,32.8205],[91.896766,32.907967],[91.857962,32.90244],[91.839484,32.948152],[91.799448,32.942126],[91.752637,32.969242],[91.685499,32.989324],[91.664557,33.012913],[91.583253,33.0375],[91.55492,33.060074],[91.535826,33.10019],[91.49579,33.109214],[91.436044,33.066092],[91.370138,33.100691],[91.311624,33.108211],[91.261733,33.141291],[91.226624,33.141792],[91.18782,33.106206],[91.161335,33.108712],[91.147784,33.07211],[91.072024,33.113224],[91.037531,33.098686],[91.001807,33.11573],[90.927894,33.120241],[90.902024,33.083143],[90.88293,33.120241],[90.803474,33.114227],[90.740032,33.142293],[90.704308,33.135778],[90.627315,33.180368],[90.562642,33.229441],[90.490577,33.264977],[90.405577,33.260473],[90.363077,33.279487],[90.332896,33.310501],[90.246665,33.423959],[90.22018,33.437943],[90.107463,33.460913],[90.088984,33.478885],[90.083441,33.525295],[90.01076,33.553728],[89.984275,33.612061],[90.008296,33.687785],[89.981195,33.70322],[89.983659,33.725622],[89.907282,33.741051],[89.902355,33.758467],[89.942391,33.801246],[89.899891,33.80771],[89.837065,33.868853],[89.795181,33.865374],[89.73174,33.921509],[89.718805,33.946832],[89.688008,33.959739],[89.684928,33.990013],[89.635037,34.049537],[89.656595,34.057966],[89.655979,34.097126],[89.71203,34.131809],[89.756993,34.124874],[89.760073,34.152613],[89.789638,34.150632],[89.816739,34.16945],[89.838297,34.263477],[89.825362,34.293642],[89.86663,34.324785],[89.858623,34.359375],[89.820435,34.369255],[89.799493,34.39642],[89.819819,34.420614],[89.823515,34.455657],[89.814891,34.548871],[89.777935,34.574499],[89.798877,34.628686],[89.74837,34.641981],[89.72558,34.660689],[89.732356,34.732035],[89.799493,34.743838],[89.825978,34.796931],[89.867862,34.81069],[89.838913,34.865705],[89.814891,34.86816],[89.821051,34.902033],[89.78779,34.921664],[89.747138,34.903506],[89.707102,34.919701],[89.670146,34.887798],[89.578987,34.895162],[89.560509,34.938836],[89.59069,35.057965],[89.593153,35.104491],[89.579603,35.118688],[89.519241,35.133862],[89.46935,35.214577],[89.450255,35.223867],[89.48598,35.256616],[89.531559,35.276161],[89.494603,35.298632],[89.516161,35.330862],[89.497067,35.361128],[89.58761,35.383575],[89.619639,35.412357],[89.658443,35.425526],[89.685544,35.416259],[89.739131,35.468429],[89.765,35.482563],[89.740979,35.507412],[89.720037,35.501566],[89.699711,35.544916],[89.71203,35.581915],[89.75145,35.580942],[89.765616,35.599922],[89.726196,35.648082],[89.748986,35.66267],[89.747138,35.7516],[89.782863,35.773453],[89.767464,35.799183],[89.801957,35.848193],[89.778551,35.861775],[89.707718,35.849163],[89.654747,35.848193],[89.62395,35.859349],[89.550654,35.856924],[89.554965,35.873414],[89.489676,35.903475],[89.428082,35.917531],[89.434857,35.992136],[89.404676,36.016827],[89.417611,36.044897],[89.474893,36.022151],[89.605472,36.038123],[89.688624,36.091337],[89.711414,36.093272]]]]}},
    {"type":"Feature","properties":{"adcode":610000,"name":"陕西省","center":[108.948024,34.263161],"centroid":[108.887114,35.263661],"childrenNum":10,"level":"province","parent":{"adcode":100000},"subFeatureIndex":26,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[110.379257,34.600612],[110.29549,34.610956],[110.269004,34.629671],[110.229584,34.692679],[110.243135,34.725641],[110.246831,34.789068],[110.230816,34.880925],[110.262229,34.944233],[110.320743,35.00504],[110.373714,35.134351],[110.364475,35.197952],[110.378642,35.210666],[110.374946,35.251728],[110.45009,35.327933],[110.477808,35.413821],[110.531394,35.511309],[110.567735,35.539559],[110.589293,35.602355],[110.609619,35.632031],[110.57759,35.701559],[110.571431,35.800639],[110.550489,35.838005],[110.549257,35.877778],[110.511684,35.879718],[110.516612,35.918501],[110.502445,35.947575],[110.516612,35.971796],[110.49259,35.994073],[110.491974,36.034735],[110.467953,36.074893],[110.447011,36.164328],[110.45625,36.22663],[110.474112,36.248352],[110.474112,36.306729],[110.459946,36.327946],[110.487047,36.393972],[110.489511,36.430094],[110.47288,36.453203],[110.503677,36.488335],[110.488895,36.556628],[110.496902,36.582102],[110.447627,36.621018],[110.426685,36.657514],[110.394656,36.676716],[110.402663,36.697352],[110.438388,36.685835],[110.447011,36.737649],[110.407591,36.776007],[110.423605,36.818179],[110.406975,36.824886],[110.424221,36.855539],[110.376178,36.882351],[110.408823,36.892403],[110.424221,36.963685],[110.381721,37.002408],[110.382953,37.022001],[110.426685,37.008621],[110.417446,37.027257],[110.460561,37.044932],[110.49567,37.086956],[110.535706,37.115118],[110.53509,37.138021],[110.590525,37.187145],[110.651503,37.256722],[110.661974,37.281963],[110.690307,37.287201],[110.678604,37.317668],[110.695234,37.34955],[110.641648,37.360015],[110.630561,37.372858],[110.644111,37.435135],[110.740198,37.44939],[110.759292,37.474567],[110.770995,37.538184],[110.795017,37.558586],[110.771611,37.594634],[110.763604,37.639668],[110.793169,37.650567],[110.775306,37.680886],[110.706321,37.705511],[110.716792,37.728708],[110.750669,37.736281],[110.735886,37.77035],[110.680452,37.790216],[110.59422,37.922049],[110.522771,37.955088],[110.528315,37.990471],[110.507989,38.013107],[110.501829,38.097929],[110.519692,38.130889],[110.509221,38.192061],[110.528315,38.211814],[110.565887,38.215105],[110.57759,38.297345],[110.601612,38.308147],[110.661358,38.308617],[110.701394,38.353215],[110.746973,38.366355],[110.77777,38.440924],[110.796864,38.453579],[110.840596,38.439986],[110.874473,38.453579],[110.870777,38.510265],[110.907733,38.521035],[110.920052,38.581878],[110.898494,38.587024],[110.880632,38.626776],[110.916357,38.673981],[110.915125,38.704345],[110.965016,38.755699],[111.009363,38.847579],[110.995813,38.868084],[111.016755,38.889981],[111.009979,38.932823],[110.980414,38.970056],[110.998276,38.998433],[111.038313,39.020289],[111.094363,39.030053],[111.138095,39.064447],[111.147334,39.100681],[111.173819,39.135041],[111.163348,39.152678],[111.219399,39.244044],[111.213239,39.257021],[111.247732,39.302419],[111.202152,39.305197],[111.179363,39.326959],[111.186138,39.35149],[111.155341,39.338531],[111.159037,39.362596],[111.125776,39.366297],[111.087588,39.376013],[111.098059,39.401914],[111.064182,39.400989],[111.058639,39.447681],[111.10545,39.472631],[111.10545,39.497573],[111.148566,39.531277],[111.154725,39.569116],[111.136863,39.587106],[111.101138,39.559428],[111.017371,39.552045],[110.958856,39.519275],[110.891103,39.509118],[110.869545,39.494341],[110.782698,39.38804],[110.73835,39.348713],[110.731575,39.30705],[110.702626,39.273701],[110.626249,39.266751],[110.596684,39.282966],[110.566503,39.320014],[110.559728,39.351027],[110.524003,39.382952],[110.482735,39.360745],[110.434692,39.381101],[110.429764,39.341308],[110.385417,39.310291],[110.257917,39.407001],[110.243751,39.423645],[110.152592,39.45415],[110.12549,39.432891],[110.136577,39.39174],[110.161831,39.387115],[110.184005,39.355192],[110.217881,39.281113],[110.109476,39.249606],[110.041107,39.21623],[109.962267,39.212056],[109.90252,39.271848],[109.871723,39.243581],[109.961035,39.191651],[109.893897,39.141075],[109.92223,39.107183],[109.890818,39.103932],[109.851397,39.122971],[109.793499,39.074204],[109.762086,39.057476],[109.72513,39.018429],[109.665384,38.981687],[109.685094,38.968195],[109.672159,38.928167],[109.624116,38.85457],[109.549587,38.805618],[109.511399,38.833595],[109.444262,38.782763],[109.404226,38.720689],[109.338936,38.701542],[109.329081,38.66043],[109.367269,38.627711],[109.331545,38.597783],[109.276726,38.623035],[109.196654,38.552867],[109.175712,38.518694],[109.128901,38.480288],[109.054372,38.433892],[109.051292,38.385122],[109.007561,38.359316],[108.961981,38.26493],[108.976148,38.245192],[108.938575,38.207582],[108.964445,38.154894],[109.069155,38.091336],[109.050676,38.055059],[109.06977,38.023008],[109.037742,38.021593],[109.018648,37.971602],[108.982923,37.964053],[108.9743,37.931962],[108.93488,37.922521],[108.893612,37.978207],[108.883141,38.01405],[108.830786,38.049875],[108.797525,38.04799],[108.82709,37.989056],[108.798141,37.93385],[108.791982,37.872934],[108.799989,37.784068],[108.784591,37.764673],[108.791982,37.700303],[108.777815,37.683728],[108.720533,37.683728],[108.699591,37.669518],[108.628142,37.651988],[108.532671,37.690832],[108.485244,37.678044],[108.422418,37.648672],[108.301078,37.640616],[108.293071,37.656726],[108.24626,37.665728],[108.205608,37.655779],[108.193905,37.638246],[108.134159,37.622131],[108.055318,37.652462],[108.025137,37.649619],[108.012819,37.66857],[108.025753,37.696041],[107.993109,37.735335],[107.982022,37.787378],[107.884703,37.808186],[107.842819,37.828987],[107.732566,37.84931],[107.684523,37.888522],[107.65003,37.86443],[107.659269,37.844112],[107.646335,37.805349],[107.620465,37.776026],[107.599523,37.791162],[107.57119,37.776499],[107.499125,37.765619],[107.484959,37.706458],[107.425828,37.684201],[107.387024,37.691305],[107.389488,37.671413],[107.422133,37.665254],[107.361155,37.613125],[107.311264,37.609806],[107.330358,37.584201],[107.369162,37.58752],[107.345756,37.518725],[107.284162,37.481691],[107.282931,37.437036],[107.257677,37.337179],[107.273075,37.29101],[107.309416,37.239095],[107.270612,37.229089],[107.317423,37.200017],[107.336517,37.165687],[107.334669,37.138975],[107.306952,37.100799],[107.281083,37.127047],[107.268764,37.099367],[107.28601,37.054963],[107.288474,37.008143],[107.288474,37.008143],[107.291554,36.979463],[107.291554,36.979463],[107.310032,36.912502],[107.336517,36.925899],[107.365466,36.905324],[107.478183,36.908196],[107.533618,36.867031],[107.540393,36.828718],[107.5909,36.836382],[107.642023,36.819137],[107.670356,36.83303],[107.722095,36.802367],[107.742421,36.811951],[107.768291,36.792783],[107.866841,36.766899],[107.907493,36.750118],[107.914268,36.720861],[107.940754,36.694953],[107.938906,36.655594],[108.006659,36.683435],[108.02329,36.647912],[108.001732,36.639269],[108.060862,36.592194],[108.079956,36.614294],[108.092891,36.587388],[108.163724,36.563839],[108.1976,36.630144],[108.222854,36.631105],[108.204992,36.606607],[108.204992,36.606607],[108.210535,36.577296],[108.245644,36.571048],[108.262274,36.549417],[108.340498,36.559032],[108.365136,36.519603],[108.391621,36.505654],[108.408252,36.45946],[108.460606,36.422871],[108.495099,36.422389],[108.514809,36.445501],[108.510498,36.47438],[108.562852,36.43876],[108.618903,36.433946],[108.651548,36.384818],[108.641693,36.359279],[108.646004,36.254143],[108.712526,36.138716],[108.682345,36.062316],[108.688504,36.021183],[108.659555,35.990683],[108.652164,35.94806],[108.593649,35.950967],[108.562852,35.921409],[108.518505,35.905414],[108.499411,35.872444],[108.527744,35.82442],[108.533903,35.746257],[108.517889,35.699615],[108.539447,35.605761],[108.618287,35.557088],[108.625678,35.537124],[108.605968,35.503028],[108.631222,35.418698],[108.61028,35.355271],[108.614591,35.328909],[108.583178,35.294724],[108.547454,35.304981],[108.48894,35.275184],[108.36144,35.279581],[108.345426,35.300586],[108.296767,35.267855],[108.239484,35.256127],[108.221622,35.296678],[108.174811,35.304981],[108.094739,35.280069],[108.049159,35.253683],[107.949993,35.245375],[107.960464,35.263457],[107.867457,35.256127],[107.841587,35.276649],[107.745501,35.311819],[107.737494,35.267366],[107.667277,35.257104],[107.652494,35.244886],[107.686371,35.218],[107.715936,35.168114],[107.727639,35.120157],[107.769523,35.064333],[107.769523,35.064333],[107.773218,35.060904],[107.773218,35.060904],[107.814486,35.024646],[107.846515,35.024646],[107.863145,34.999158],[107.842203,34.979056],[107.741805,34.953553],[107.675284,34.9511],[107.638943,34.935402],[107.619849,34.964834],[107.564415,34.968757],[107.523763,34.909886],[107.455394,34.916757],[107.400575,34.932949],[107.369162,34.917738],[107.350068,34.93393],[107.286626,34.931968],[107.252749,34.880925],[107.189308,34.893198],[107.162206,34.944233],[107.119707,34.950119],[107.089526,34.976604],[107.08275,35.024156],[107.012533,35.029547],[106.990975,35.068252],[106.950323,35.066782],[106.901664,35.094698],[106.838222,35.080007],[106.710723,35.100574],[106.706411,35.081966],[106.615252,35.071191],[106.577064,35.089312],[106.541956,35.083925],[106.52163,35.027587],[106.494528,35.006021],[106.494528,35.006021],[106.484673,34.983959],[106.493296,34.941289],[106.527789,34.876507],[106.556122,34.861285],[106.550579,34.82936],[106.575216,34.769897],[106.539492,34.745805],[106.505615,34.746789],[106.487137,34.715311],[106.456956,34.703996],[106.442173,34.675455],[106.471122,34.634102],[106.419384,34.643458],[106.314058,34.578934],[106.341159,34.568093],[106.334384,34.517811],[106.455108,34.531617],[106.514238,34.511894],[106.513622,34.498085],[106.558586,34.48822],[106.610941,34.454177],[106.638042,34.391481],[106.717498,34.369255],[106.691013,34.337635],[106.705179,34.299575],[106.68239,34.256057],[106.652825,34.24369],[106.63373,34.260014],[106.589383,34.253584],[106.577064,34.280786],[106.526557,34.292159],[106.496376,34.238248],[106.5321,34.254079],[106.55797,34.229837],[106.585071,34.149641],[106.560434,34.109514],[106.501919,34.105055],[106.505615,34.056479],[106.471738,34.024244],[106.474202,33.970659],[106.41076,33.909093],[106.428007,33.866368],[106.475434,33.875809],[106.491448,33.834559],[106.461883,33.789807],[106.488369,33.757969],[106.482825,33.707203],[106.534564,33.695254],[106.575832,33.631497],[106.58076,33.576169],[106.540108,33.512822],[106.456956,33.532779],[106.447101,33.613058],[106.384891,33.612061],[106.35163,33.587137],[106.303587,33.604585],[106.237681,33.564201],[106.187174,33.546746],[106.108334,33.569686],[106.117573,33.602591],[106.086776,33.617045],[106.047356,33.610067],[105.971596,33.613058],[105.940183,33.570684],[105.902611,33.556222],[105.871198,33.511325],[105.842248,33.489866],[105.831162,33.451926],[105.837937,33.410971],[105.827466,33.379993],[105.709822,33.382991],[105.755401,33.329004],[105.752937,33.291994],[105.791741,33.278486],[105.799133,33.258471],[105.862574,33.234447],[105.917393,33.237951],[105.965436,33.204407],[105.968516,33.154318],[105.93156,33.178365],[105.897067,33.146803],[105.923552,33.147805],[105.934639,33.112221],[105.914929,33.066092],[105.926632,33.042517],[105.917393,32.993841],[105.861959,32.939112],[105.82685,32.950663],[105.735691,32.905454],[105.656851,32.895405],[105.638373,32.879323],[105.590329,32.87681],[105.565692,32.906962],[105.528119,32.919019],[105.49917,32.911986],[105.495475,32.873292],[105.524424,32.847654],[105.534279,32.790822],[105.555221,32.794343],[105.563844,32.724891],[105.585402,32.728919],[105.596489,32.69921],[105.677793,32.726402],[105.719061,32.759624],[105.768952,32.767676],[105.779423,32.750061],[105.822538,32.770192],[105.825002,32.824523],[105.849024,32.817985],[105.893371,32.838603],[105.93156,32.826032],[105.969132,32.849162],[106.011632,32.829552],[106.044277,32.864747],[106.071378,32.828546],[106.093552,32.82402],[106.07261,32.76365],[106.076921,32.76365],[106.076305,32.759121],[106.071378,32.758114],[106.120037,32.719856],[106.17424,32.6977],[106.254928,32.693671],[106.267863,32.673522],[106.301123,32.680071],[106.347935,32.671003],[106.389203,32.62666],[106.421231,32.616579],[106.451412,32.65992],[106.498224,32.649338],[106.517934,32.668485],[106.585687,32.68813],[106.626955,32.682086],[106.670071,32.694678],[106.733513,32.739491],[106.783404,32.735967],[106.793259,32.712807],[106.82344,32.705254],[106.854853,32.724388],[106.903512,32.721367],[106.912751,32.704247],[107.012533,32.721367],[107.066736,32.708779],[107.05996,32.686115],[107.098765,32.649338],[107.108004,32.600951],[107.080286,32.542448],[107.127098,32.482393],[107.189924,32.468256],[107.212097,32.428864],[107.263836,32.403099],[107.287858,32.457147],[107.313727,32.489965],[107.356843,32.506622],[107.382097,32.54043],[107.436299,32.529835],[107.438763,32.465732],[107.460937,32.453612],[107.456625,32.41775],[107.489886,32.425328],[107.527458,32.38238],[107.598291,32.411688],[107.648183,32.413709],[107.680827,32.397035],[107.707929,32.331826],[107.753508,32.338399],[107.812022,32.247844],[107.864377,32.201266],[107.890247,32.214432],[107.924739,32.197215],[107.979558,32.146051],[108.024521,32.177462],[108.018362,32.2119],[108.086731,32.233165],[108.143398,32.219495],[108.156948,32.239239],[108.179738,32.221521],[108.240716,32.274666],[108.310933,32.232152],[108.389773,32.263533],[108.414411,32.252399],[108.469846,32.270618],[108.507418,32.245819],[108.509882,32.201266],[108.543758,32.177969],[108.585026,32.17189],[108.676801,32.10297],[108.734084,32.106519],[108.75133,32.076098],[108.78767,32.04871],[108.837561,32.039072],[108.902235,31.984774],[108.986619,31.980205],[109.085785,31.929428],[109.123357,31.892851],[109.191111,31.85575],[109.195422,31.817618],[109.27611,31.79931],[109.279806,31.776418],[109.253936,31.759628],[109.282885,31.743343],[109.281654,31.716874],[109.381436,31.705165],[109.446109,31.722983],[109.502776,31.716365],[109.549587,31.73011],[109.585928,31.726546],[109.592087,31.789136],[109.633971,31.804396],[109.633971,31.824738],[109.60379,31.885737],[109.584696,31.900472],[109.62042,31.928412],[109.631507,31.962436],[109.590855,32.012688],[109.590855,32.047696],[109.621652,32.106519],[109.58716,32.161251],[109.604406,32.199241],[109.592703,32.219495],[109.550203,32.225065],[109.528645,32.270112],[109.495385,32.300468],[109.513247,32.342444],[109.502776,32.38895],[109.529877,32.405625],[109.526797,32.43341],[109.575457,32.506622],[109.637051,32.540935],[109.619804,32.56767],[109.631507,32.599943],[109.726978,32.608513],[109.746072,32.594901],[109.816905,32.577252],[109.910528,32.592884],[109.97089,32.577756],[110.017701,32.546989],[110.084223,32.580782],[110.090382,32.617083],[110.124259,32.616579],[110.153824,32.593388],[110.206179,32.633212],[110.156903,32.683093],[110.159367,32.767173],[110.127338,32.77774],[110.142121,32.802895],[110.105164,32.832569],[110.051578,32.851676],[109.988752,32.886359],[109.927158,32.887364],[109.907448,32.903947],[109.856941,32.910479],[109.847702,32.893395],[109.789804,32.882339],[109.76455,32.909474],[109.785492,32.987316],[109.794731,33.067095],[109.704188,33.101694],[109.688174,33.116733],[109.576073,33.110216],[109.522486,33.138785],[109.468283,33.140288],[109.438718,33.152314],[109.498464,33.207412],[109.514479,33.237951],[109.60687,33.235949],[109.619804,33.275484],[109.649985,33.251465],[109.693101,33.254468],[109.732521,33.231443],[109.813209,33.236449],[109.852013,33.247961],[109.916687,33.229942],[109.973353,33.203907],[109.999223,33.212419],[110.031252,33.191888],[110.164911,33.209415],[110.218497,33.163336],[110.285635,33.171352],[110.33799,33.160331],[110.372482,33.186379],[110.398352,33.176862],[110.398352,33.176862],[110.471032,33.171352],[110.54125,33.255469],[110.57759,33.250464],[110.59422,33.168346],[110.623785,33.143796],[110.650887,33.157324],[110.702626,33.097182],[110.753133,33.15031],[110.824582,33.158327],[110.828893,33.201403],[110.865234,33.213921],[110.9219,33.203907],[110.960704,33.253967],[110.984726,33.255469],[111.025994,33.330504],[111.025994,33.375495],[110.996429,33.435946],[111.02661,33.467903],[111.021066,33.471397],[111.021682,33.476389],[111.02661,33.478386],[111.002588,33.535772],[111.00382,33.578662],[110.966864,33.609071],[110.878784,33.634486],[110.823966,33.685793],[110.831973,33.713675],[110.81719,33.751003],[110.782082,33.796272],[110.74143,33.798759],[110.712481,33.833564],[110.66259,33.85295],[110.612083,33.852453],[110.587445,33.887733],[110.628713,33.910086],[110.627481,33.925482],[110.665669,33.937895],[110.671213,33.966192],[110.620706,34.035652],[110.587445,34.023252],[110.591757,34.101586],[110.61393,34.113478],[110.642264,34.161032],[110.621938,34.177372],[110.55788,34.193214],[110.55172,34.213012],[110.507989,34.217466],[110.43962,34.243196],[110.428533,34.288203],[110.451938,34.292653],[110.503677,34.33714],[110.473496,34.393457],[110.403279,34.433448],[110.403279,34.433448],[110.360779,34.516825],[110.372482,34.544435],[110.404511,34.557743],[110.366939,34.566614],[110.379257,34.600612]]],[[[111.02661,33.478386],[111.021682,33.476389],[111.021066,33.471397],[111.02661,33.467903],[111.02661,33.478386]]],[[[106.076921,32.76365],[106.07261,32.76365],[106.071378,32.758114],[106.076305,32.759121],[106.076921,32.76365]]]]}},
    {"type":"Feature","properties":{"adcode":620000,"name":"甘肃省","center":[103.823557,36.058039],"childrenNum":14,"level":"province","parent":{"adcode":100000},"subFeatureIndex":27,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[106.506231,35.737514],[106.504383,35.736057],[106.498224,35.732656],[106.49268,35.732656],[106.434782,35.688436],[106.460036,35.643705],[106.47913,35.575101],[106.460036,35.578995],[106.440941,35.52641],[106.465579,35.481101],[106.490217,35.480613],[106.483441,35.450393],[106.503767,35.415284],[106.501304,35.364056],[106.472354,35.310842],[106.415688,35.276161],[106.368261,35.273718],[106.363333,35.238532],[106.319601,35.265411],[106.241377,35.358687],[106.237681,35.409431],[106.196414,35.409919],[106.173008,35.437716],[106.129892,35.393333],[106.113262,35.361616],[106.083081,35.421624],[106.073226,35.420649],[106.067682,35.436254],[106.073226,35.447468],[106.071378,35.449418],[106.06953,35.458193],[106.071994,35.463555],[106.054132,35.45478],[106.034422,35.469404],[106.002393,35.438692],[105.894603,35.413821],[105.897683,35.451368],[106.048588,35.488898],[106.047356,35.498155],[106.023335,35.49377],[106.017175,35.519103],[105.900147,35.54735],[105.868734,35.540046],[105.847176,35.490359],[105.816379,35.575101],[105.800365,35.564878],[105.762176,35.602841],[105.759097,35.634464],[105.713517,35.650513],[105.722756,35.673366],[105.690727,35.698643],[105.723988,35.725854],[105.740618,35.698643],[105.759097,35.724883],[105.70243,35.733142],[105.667322,35.749657],[105.595873,35.715651],[105.481924,35.727312],[105.457286,35.771511],[105.432033,35.787533],[105.428953,35.819082],[105.408627,35.822479],[105.38091,35.792873],[105.371055,35.844312],[105.39754,35.857409],[105.350113,35.875839],[105.324859,35.941761],[105.343954,36.033767],[105.406163,36.074409],[105.430801,36.10391],[105.491163,36.101009],[105.515185,36.147415],[105.478844,36.213111],[105.460366,36.223733],[105.45975,36.268137],[105.476381,36.293224],[105.455439,36.321678],[105.425873,36.330357],[105.401236,36.369881],[105.398156,36.430575],[105.363048,36.443093],[105.362432,36.496514],[105.322396,36.535954],[105.281744,36.522489],[105.252179,36.553263],[105.2762,36.563358],[105.261418,36.602764],[105.22015,36.631105],[105.225693,36.664716],[105.201056,36.700711],[105.218302,36.730455],[105.272505,36.739567],[105.275584,36.752515],[105.319932,36.742924],[105.340874,36.764502],[105.334714,36.80093],[105.303302,36.820575],[105.279896,36.86751],[105.244787,36.894796],[105.178882,36.892403],[105.185657,36.942164],[105.165331,36.99476],[105.128991,36.996194],[105.05939,37.022956],[105.03968,37.007187],[105.004571,37.035378],[104.95468,37.040156],[104.954064,37.077407],[104.914644,37.097935],[104.888158,37.15901],[104.864753,37.17284],[104.85613,37.211933],[104.776673,37.246718],[104.717543,37.208597],[104.638087,37.201923],[104.600515,37.242907],[104.624536,37.298627],[104.651022,37.290534],[104.673812,37.317668],[104.713848,37.329566],[104.662109,37.367626],[104.679971,37.408044],[104.521059,37.43466],[104.499501,37.421353],[104.448994,37.42468],[104.437907,37.445589],[104.365226,37.418026],[104.298705,37.414223],[104.287002,37.428007],[104.237727,37.411847],[104.183524,37.406618],[104.089285,37.465067],[103.935916,37.572818],[103.874938,37.604117],[103.841062,37.64725],[103.683381,37.777919],[103.627947,37.797783],[103.40744,37.860651],[103.362477,38.037621],[103.368636,38.08898],[103.53494,38.156776],[103.507838,38.280905],[103.465339,38.353215],[103.416063,38.404821],[103.85954,38.64454],[104.011677,38.85923],[104.044322,38.895105],[104.173053,38.94446],[104.196459,38.9882],[104.190915,39.042139],[104.207546,39.083495],[104.171205,39.160567],[104.047401,39.297788],[104.073271,39.351953],[104.089901,39.419947],[103.955626,39.456923],[103.85338,39.461543],[103.728961,39.430117],[103.595302,39.386652],[103.428998,39.353341],[103.344615,39.331588],[103.259615,39.263971],[103.188166,39.215302],[103.133347,39.192579],[103.007696,39.099753],[102.883892,39.120649],[102.616574,39.171703],[102.579002,39.183301],[102.45335,39.255167],[102.3548,39.231993],[102.276576,39.188868],[102.050526,39.141075],[102.012338,39.127149],[101.902701,39.111827],[101.833715,39.08907],[101.926106,39.000758],[101.955055,38.985874],[102.045599,38.904885],[102.075164,38.891378],[101.941505,38.808883],[101.873751,38.733761],[101.777049,38.66043],[101.672955,38.6908],[101.601506,38.65529],[101.562702,38.713218],[101.412413,38.764099],[101.331109,38.777164],[101.307087,38.80282],[101.34158,38.822406],[101.33542,38.847113],[101.24303,38.860628],[101.237486,38.907214],[101.198682,38.943064],[101.228863,39.020754],[101.117378,38.975174],[100.969553,38.946788],[100.961545,39.005874],[100.901799,39.030053],[100.875314,39.002619],[100.835278,39.025869],[100.829118,39.075133],[100.864227,39.106719],[100.842669,39.199999],[100.842053,39.405614],[100.707778,39.404689],[100.606764,39.387577],[100.498975,39.400527],[100.500823,39.481408],[100.44354,39.485565],[100.326512,39.509118],[100.301258,39.572345],[100.314193,39.606935],[100.250135,39.685274],[100.128179,39.702312],[100.040716,39.757083],[99.958796,39.769504],[99.904593,39.785601],[99.822058,39.860063],[99.672384,39.888079],[99.469124,39.875221],[99.440791,39.885783],[99.459885,39.898181],[99.491298,39.884406],[99.533182,39.891753],[99.714268,39.972061],[99.751225,40.006909],[99.841152,40.013326],[99.927383,40.063727],[99.955716,40.150695],[100.007455,40.20008],[100.169447,40.277743],[100.169447,40.541131],[100.242744,40.618855],[100.237201,40.716905],[100.224882,40.727337],[100.107853,40.875475],[100.057346,40.908049],[99.985897,40.909858],[99.673,40.93292],[99.565827,40.846961],[99.174705,40.858278],[99.172858,40.747289],[99.12543,40.715091],[99.102025,40.676522],[99.041662,40.693767],[98.984996,40.782644],[98.790975,40.705564],[98.80699,40.660181],[98.802678,40.607043],[98.762642,40.639748],[98.72199,40.657911],[98.689345,40.691952],[98.668403,40.773128],[98.569853,40.746836],[98.627751,40.677884],[98.344419,40.568413],[98.333332,40.918903],[98.25018,40.93925],[98.184891,40.988056],[98.142391,41.001607],[97.971776,41.09774],[97.903407,41.168057],[97.629314,41.440498],[97.613915,41.477276],[97.84674,41.656379],[97.653335,41.986856],[97.500582,42.243894],[97.371235,42.457076],[97.172903,42.795257],[96.968411,42.756161],[96.742361,42.75704],[96.386348,42.727592],[96.166458,42.623314],[96.103632,42.604375],[96.072219,42.569566],[96.02356,42.542675],[96.0174,42.482239],[95.978596,42.436762],[96.06606,42.414674],[96.042038,42.352787],[96.040806,42.326688],[96.178161,42.21775],[96.077147,42.149457],[96.13874,42.05399],[96.137509,42.019765],[96.117183,41.985966],[96.054973,41.936124],[95.998306,41.906289],[95.855408,41.849699],[95.801206,41.848361],[95.759322,41.835878],[95.65646,41.826067],[95.57146,41.796181],[95.445193,41.719841],[95.39407,41.693481],[95.335556,41.644305],[95.299831,41.565994],[95.247476,41.61344],[95.194505,41.694821],[95.199433,41.719395],[95.16494,41.735474],[95.135991,41.772976],[95.110738,41.768513],[95.011572,41.726541],[94.969072,41.718948],[94.861898,41.668451],[94.809543,41.619256],[94.750413,41.538227],[94.534219,41.505966],[94.184365,41.268444],[94.01067,41.114875],[93.908424,40.983539],[93.809874,40.879548],[93.820961,40.793519],[93.760599,40.664721],[93.506216,40.648376],[92.928465,40.572504],[92.920458,40.391792],[92.906907,40.310609],[92.796654,40.153897],[92.745531,39.868331],[92.687632,39.657174],[92.639589,39.514196],[92.52564,39.368611],[92.378431,39.258411],[92.339011,39.236628],[92.343938,39.146181],[92.366112,39.096037],[92.366728,39.059335],[92.41046,39.03842],[92.459119,39.042604],[92.459119,39.063982],[92.489916,39.099753],[92.545966,39.111362],[92.659299,39.109969],[92.765857,39.136898],[92.866871,39.138754],[92.889045,39.160103],[92.938936,39.169848],[92.978356,39.143396],[93.043029,39.146645],[93.115094,39.17959],[93.142196,39.160567],[93.131725,39.108112],[93.165601,39.090928],[93.198246,39.045857],[93.179152,38.923977],[93.237666,38.916062],[93.274007,38.896036],[93.453245,38.915596],[93.729186,38.924443],[93.834511,38.867618],[93.884403,38.867618],[93.884403,38.826136],[93.769838,38.821007],[93.756287,38.807484],[93.773533,38.771099],[93.800019,38.750566],[93.885018,38.720689],[93.95154,38.715086],[93.973098,38.724891],[94.281067,38.7599],[94.370379,38.7627],[94.511429,38.445142],[94.527443,38.425922],[94.527443,38.365416],[94.56132,38.351807],[94.582878,38.36917],[94.672805,38.386998],[94.812623,38.385591],[94.861282,38.393565],[94.884072,38.414669],[94.973999,38.430142],[95.045448,38.418889],[95.072549,38.402476],[95.122441,38.417014],[95.140919,38.392158],[95.185266,38.379492],[95.209904,38.327868],[95.229614,38.330685],[95.259179,38.302981],[95.315846,38.318947],[95.408236,38.300163],[95.440881,38.310965],[95.455664,38.291709],[95.487693,38.314721],[95.51849,38.294997],[95.585011,38.343359],[95.608417,38.339134],[95.671858,38.388405],[95.703887,38.400131],[95.723597,38.378554],[95.775952,38.356031],[95.83693,38.344298],[95.852945,38.287481],[95.89606,38.2903],[95.932401,38.259291],[95.93856,38.237202],[96.006929,38.207582],[96.06606,38.173245],[96.109175,38.187358],[96.221892,38.149246],[96.252689,38.167599],[96.264392,38.145952],[96.313051,38.161952],[96.301964,38.183124],[96.335841,38.246132],[96.378341,38.277146],[96.46334,38.277616],[96.665369,38.23015],[96.655514,38.295936],[96.638883,38.307208],[96.626564,38.356031],[96.698013,38.422172],[96.707868,38.459203],[96.6666,38.483567],[96.706637,38.505582],[96.780549,38.504177],[96.800259,38.52759],[96.767614,38.552399],[96.808882,38.582346],[96.7941,38.608072],[96.847071,38.599186],[96.876636,38.580475],[96.961019,38.558015],[97.055874,38.594508],[97.047251,38.653888],[97.057722,38.67258],[97.009063,38.702477],[97.023229,38.755699],[97.00044,38.7613],[96.987505,38.793025],[96.993664,38.834993],[96.983809,38.869016],[96.940693,38.90768],[96.938846,38.95563],[96.965331,39.017034],[96.95794,39.041674],[96.969643,39.097895],[97.012142,39.142004],[96.962251,39.198144],[97.017686,39.208347],[97.060186,39.19768],[97.14149,39.199999],[97.220946,39.193042],[97.315185,39.164744],[97.347213,39.167528],[97.371235,39.140611],[97.401416,39.146645],[97.458698,39.117863],[97.504894,39.076527],[97.58127,39.052364],[97.679205,39.010524],[97.701379,38.963076],[97.828878,38.93003],[97.875689,38.898365],[98.009348,38.85923],[98.029058,38.834061],[98.068478,38.816344],[98.091884,38.786495],[98.167645,38.840121],[98.242173,38.880664],[98.235398,38.918855],[98.276666,38.963541],[98.287753,38.992386],[98.280977,39.027263],[98.316702,39.040744],[98.383839,39.029588],[98.401086,39.001688],[98.432498,38.996107],[98.428187,38.976104],[98.457752,38.952838],[98.526737,38.95563],[98.584635,38.93003],[98.624056,38.959353],[98.612353,38.977035],[98.661628,38.993782],[98.70536,39.043533],[98.730613,39.057011],[98.743548,39.086747],[98.816845,39.085818],[98.818076,39.064911],[98.886446,39.040744],[98.903076,39.012384],[98.951735,38.987735],[99.054597,38.97657],[99.107568,38.951907],[99.071843,38.921184],[99.068764,38.896968],[99.141445,38.852706],[99.222133,38.788827],[99.291118,38.765966],[99.361951,38.718354],[99.375502,38.684727],[99.412458,38.665571],[99.450646,38.60433],[99.501769,38.612281],[99.52887,38.546314],[99.585537,38.498556],[99.63974,38.474666],[99.65945,38.449361],[99.727203,38.415607],[99.758,38.410449],[99.826985,38.370109],[99.960028,38.320825],[100.001912,38.315191],[100.049955,38.283254],[100.071513,38.284663],[100.117093,38.253652],[100.126332,38.231561],[100.182998,38.222158],[100.159592,38.291239],[100.163904,38.328337],[100.136803,38.33444],[100.093071,38.407166],[100.022238,38.432017],[100.001296,38.467169],[100.025933,38.507923],[100.064122,38.518694],[100.086911,38.492936],[100.113397,38.497151],[100.163288,38.461546],[100.24028,38.441861],[100.259374,38.366355],[100.301874,38.388405],[100.331439,38.337257],[100.318505,38.329276],[100.396729,38.293118],[100.424446,38.307208],[100.432453,38.275267],[100.459555,38.2654],[100.474953,38.288891],[100.516837,38.272448],[100.545786,38.247072],[100.595061,38.242372],[100.619083,38.26587],[100.71517,38.253652],[100.752126,38.238612],[100.825423,38.158658],[100.860531,38.148305],[100.913502,38.17889],[100.93814,38.16007],[100.91843,38.129006],[100.922125,38.084741],[100.888864,38.056001],[100.895024,38.013107],[100.91843,37.999432],[100.964009,38.011221],[101.077342,37.941874],[101.103211,37.946593],[101.114298,37.92016],[101.152486,37.891356],[101.159262,37.86821],[101.202994,37.84742],[101.276906,37.83655],[101.362522,37.791162],[101.382848,37.822369],[101.459224,37.86632],[101.551615,37.835604],[101.598427,37.827569],[101.670491,37.754264],[101.659405,37.733441],[101.791832,37.696041],[101.815853,37.654357],[101.854657,37.664781],[101.873135,37.686569],[101.946432,37.728235],[101.998787,37.724921],[102.036359,37.685149],[102.048678,37.651515],[102.035128,37.627819],[102.102265,37.582304],[102.131214,37.54625],[102.103497,37.482641],[102.125055,37.48549],[102.176794,37.458892],[102.19712,37.420403],[102.299981,37.391404],[102.29875,37.370004],[102.368351,37.327662],[102.428097,37.308624],[102.419474,37.294343],[102.45335,37.271487],[102.457662,37.248147],[102.490307,37.223371],[102.533422,37.217176],[102.578386,37.17284],[102.599944,37.174748],[102.642444,37.099845],[102.583314,37.104618],[102.488459,37.078362],[102.506321,37.019134],[102.450271,36.968467],[102.499546,36.954599],[102.526031,36.928291],[102.56114,36.91968],[102.587009,36.869904],[102.639364,36.852666],[102.720052,36.767858],[102.692335,36.775528],[102.639364,36.732853],[102.612879,36.738129],[102.601176,36.710307],[102.630741,36.650793],[102.684328,36.619097],[102.724364,36.613813],[102.714509,36.599401],[102.761936,36.568645],[102.734219,36.562396],[102.753313,36.525855],[102.793349,36.497957],[102.771791,36.47438],[102.829689,36.365544],[102.831537,36.365544],[102.838928,36.345783],[102.836465,36.344819],[102.845704,36.331803],[102.896827,36.331803],[102.922696,36.298047],[103.024942,36.256556],[103.021246,36.232906],[103.066826,36.216974],[103.048964,36.199107],[102.986754,36.193312],[102.965812,36.151765],[102.948566,36.150798],[102.941174,36.104877],[102.882044,36.082632],[102.932551,36.048285],[102.968276,36.044414],[102.951645,36.021667],[102.971971,35.995525],[102.942406,35.92674],[102.954725,35.858864],[102.94487,35.829757],[102.914073,35.845282],[102.81737,35.850133],[102.787189,35.862745],[102.739146,35.821023],[102.715125,35.815685],[102.686175,35.771996],[102.707733,35.70496],[102.744074,35.657807],[102.7644,35.653431],[102.763168,35.612086],[102.808747,35.560496],[102.746537,35.545403],[102.729291,35.523487],[102.782878,35.527871],[102.743458,35.494745],[102.695414,35.528358],[102.570995,35.548324],[102.531575,35.580455],[102.503241,35.585322],[102.49893,35.545403],[102.437952,35.455268],[102.447807,35.437229],[102.408387,35.409431],[102.314764,35.434303],[102.293822,35.424063],[102.287663,35.36552],[102.317844,35.343067],[102.311684,35.31426],[102.280887,35.303028],[102.3123,35.282512],[102.370199,35.263946],[102.365887,35.235599],[102.404075,35.179366],[102.346793,35.164201],[102.310452,35.128967],[102.29567,35.071681],[102.252554,35.048657],[102.218062,35.057475],[102.211286,35.034937],[102.176178,35.032977],[102.157699,35.010923],[102.133678,35.014844],[102.094874,34.986901],[102.048062,34.910868],[102.068388,34.887798],[101.985852,34.90007],[101.916867,34.873561],[101.923027,34.835746],[101.917483,34.705964],[101.919947,34.621791],[101.934729,34.58731],[101.956287,34.582876],[101.97415,34.548871],[102.001867,34.538519],[102.093026,34.536547],[102.139837,34.50351],[102.155852,34.507456],[102.169402,34.457631],[102.205743,34.407777],[102.259329,34.355917],[102.237156,34.34307],[102.237156,34.34307],[102.186649,34.352952],[102.149692,34.271885],[102.067772,34.293642],[102.062229,34.227858],[102.01357,34.218456],[102.030816,34.190739],[102.003099,34.162022],[101.965526,34.167469],[101.955055,34.109514],[101.897773,34.133791],[101.874367,34.130323],[101.851578,34.153108],[101.836795,34.124378],[101.788136,34.131809],[101.764114,34.122892],[101.736397,34.080275],[101.718535,34.083249],[101.703136,34.119424],[101.674187,34.110506],[101.6206,34.178857],[101.53868,34.212022],[101.492485,34.195689],[101.482014,34.218951],[101.417956,34.227858],[101.369913,34.248143],[101.327413,34.24468],[101.325565,34.268423],[101.268899,34.278808],[101.228863,34.298586],[101.235022,34.325279],[101.193754,34.336646],[101.178356,34.320831],[101.098284,34.329233],[101.054552,34.322808],[100.986799,34.374689],[100.951074,34.38358],[100.895024,34.375183],[100.868538,34.332693],[100.821727,34.317371],[100.798321,34.260014],[100.809408,34.247153],[100.764445,34.178857],[100.806329,34.155584],[100.848828,34.089692],[100.870386,34.083744],[100.880857,34.036644],[100.93506,33.990013],[100.927669,33.975126],[100.965857,33.946832],[100.994806,33.891707],[101.023139,33.896178],[101.054552,33.863386],[101.153718,33.8445],[101.153102,33.823124],[101.190675,33.791796],[101.186363,33.741051],[101.162957,33.719649],[101.177124,33.685295],[101.166653,33.659894],[101.217776,33.669856],[101.23687,33.685793],[101.302776,33.657902],[101.385312,33.644949],[101.424732,33.655411],[101.428427,33.680315],[101.501724,33.702723],[101.58426,33.674339],[101.585492,33.645448],[101.616905,33.598603],[101.611977,33.565199],[101.622448,33.502343],[101.718535,33.494857],[101.748716,33.505337],[101.769042,33.538765],[101.783208,33.556721],[101.831252,33.554726],[101.844186,33.602591],[101.884222,33.578163],[101.907012,33.539264],[101.906396,33.48188],[101.946432,33.442937],[101.915635,33.425957],[101.887302,33.383991],[101.877447,33.314502],[101.769658,33.26898],[101.770274,33.248962],[101.83002,33.213921],[101.841723,33.184876],[101.825708,33.119239],[101.865744,33.103198],[101.887302,33.135778],[101.921795,33.153817],[101.935345,33.186879],[101.99386,33.1999],[102.054838,33.189884],[102.08933,33.204908],[102.08933,33.227439],[102.117047,33.288492],[102.144765,33.273983],[102.160163,33.242956],[102.200815,33.223434],[102.217446,33.247961],[102.192192,33.337005],[102.218062,33.349503],[102.258098,33.409472],[102.296286,33.413969],[102.310452,33.397982],[102.368967,33.41247],[102.392988,33.404477],[102.447807,33.454922],[102.462589,33.449429],[102.461358,33.501345],[102.446575,33.53228],[102.477988,33.543254],[102.440416,33.574673],[102.346793,33.605582],[102.31538,33.665374],[102.342481,33.725622],[102.284583,33.719151],[102.324619,33.754486],[102.296286,33.783838],[102.243315,33.786823],[102.261177,33.821136],[102.25317,33.861399],[102.136142,33.965199],[102.16817,33.983066],[102.226069,33.963214],[102.248858,33.98654],[102.287047,33.977607],[102.315996,33.993983],[102.345561,33.969666],[102.392372,33.971651],[102.406539,34.033172],[102.437336,34.087214],[102.471213,34.072839],[102.511865,34.086222],[102.615958,34.099604],[102.649219,34.080275],[102.655994,34.113478],[102.598712,34.14766],[102.651067,34.165983],[102.664002,34.192719],[102.694799,34.198659],[102.728675,34.235774],[102.779798,34.236764],[102.798276,34.272874],[102.856791,34.270895],[102.85987,34.301058],[102.911609,34.312923],[102.949181,34.292159],[102.977515,34.252595],[102.973203,34.205588],[103.005848,34.184798],[103.052043,34.195194],[103.100087,34.181828],[103.124108,34.162022],[103.121644,34.112487],[103.178927,34.079779],[103.129652,34.065899],[103.119797,34.03466],[103.147514,34.036644],[103.157369,33.998944],[103.120413,33.953286],[103.1315,33.931937],[103.16476,33.929454],[103.181391,33.900649],[103.153673,33.819147],[103.165376,33.805721],[103.228202,33.79478],[103.24976,33.814175],[103.284868,33.80224],[103.278709,33.774387],[103.35447,33.743539],[103.434542,33.752993],[103.464723,33.80224],[103.518309,33.807213],[103.545411,33.719649],[103.520157,33.678323],[103.552186,33.671351],[103.563889,33.699735],[103.593454,33.716164],[103.645809,33.708697],[103.667983,33.685793],[103.690772,33.69376],[103.778236,33.658898],[103.861388,33.682307],[103.980264,33.670852],[104.046169,33.686291],[104.103452,33.663381],[104.176749,33.5996],[104.155191,33.542755],[104.180444,33.472895],[104.213089,33.446932],[104.22048,33.404477],[104.272219,33.391486],[104.292545,33.336505],[104.373849,33.345004],[104.420045,33.327004],[104.386168,33.298497],[104.333813,33.315502],[104.303632,33.304499],[104.323958,33.26898],[104.32827,33.223934],[104.351059,33.158828],[104.378161,33.109214],[104.337509,33.038002],[104.391711,33.035493],[104.426204,33.010906],[104.383704,32.994343],[104.378161,32.953174],[104.345516,32.940117],[104.288234,32.942628],[104.277147,32.90244],[104.294393,32.835586],[104.363994,32.822511],[104.458849,32.748551],[104.51182,32.753585],[104.526602,32.728416],[104.582653,32.722374],[104.592508,32.695685],[104.643015,32.661935],[104.696601,32.673522],[104.739717,32.635228],[104.795768,32.643292],[104.820405,32.662943],[104.845659,32.653873],[104.881999,32.600951],[104.925115,32.607505],[105.026745,32.650346],[105.0791,32.637244],[105.111128,32.593893],[105.185041,32.617587],[105.215222,32.63674],[105.219534,32.666469],[105.263265,32.652362],[105.297758,32.656897],[105.347033,32.68259],[105.368591,32.712807],[105.448663,32.732946],[105.454207,32.767173],[105.427721,32.784281],[105.396308,32.85067],[105.396308,32.85067],[105.38091,32.876307],[105.408011,32.885857],[105.414171,32.922034],[105.467757,32.930071],[105.49917,32.911986],[105.528119,32.919019],[105.565692,32.906962],[105.590329,32.87681],[105.638373,32.879323],[105.656851,32.895405],[105.735691,32.905454],[105.82685,32.950663],[105.861959,32.939112],[105.917393,32.993841],[105.926632,33.042517],[105.914929,33.066092],[105.934639,33.112221],[105.923552,33.147805],[105.897067,33.146803],[105.93156,33.178365],[105.968516,33.154318],[105.965436,33.204407],[105.917393,33.237951],[105.862574,33.234447],[105.799133,33.258471],[105.791741,33.278486],[105.752937,33.291994],[105.755401,33.329004],[105.709822,33.382991],[105.827466,33.379993],[105.837937,33.410971],[105.831162,33.451926],[105.842248,33.489866],[105.871198,33.511325],[105.902611,33.556222],[105.940183,33.570684],[105.971596,33.613058],[106.047356,33.610067],[106.086776,33.617045],[106.117573,33.602591],[106.108334,33.569686],[106.187174,33.546746],[106.237681,33.564201],[106.303587,33.604585],[106.35163,33.587137],[106.384891,33.612061],[106.447101,33.613058],[106.456956,33.532779],[106.540108,33.512822],[106.58076,33.576169],[106.575832,33.631497],[106.534564,33.695254],[106.482825,33.707203],[106.488369,33.757969],[106.461883,33.789807],[106.491448,33.834559],[106.475434,33.875809],[106.428007,33.866368],[106.41076,33.909093],[106.474202,33.970659],[106.471738,34.024244],[106.505615,34.056479],[106.501919,34.105055],[106.560434,34.109514],[106.585071,34.149641],[106.55797,34.229837],[106.5321,34.254079],[106.496376,34.238248],[106.526557,34.292159],[106.577064,34.280786],[106.589383,34.253584],[106.63373,34.260014],[106.652825,34.24369],[106.68239,34.256057],[106.705179,34.299575],[106.691013,34.337635],[106.717498,34.369255],[106.638042,34.391481],[106.610941,34.454177],[106.558586,34.48822],[106.513622,34.498085],[106.514238,34.511894],[106.455108,34.531617],[106.334384,34.517811],[106.341159,34.568093],[106.314058,34.578934],[106.419384,34.643458],[106.471122,34.634102],[106.442173,34.675455],[106.456956,34.703996],[106.487137,34.715311],[106.505615,34.746789],[106.539492,34.745805],[106.575216,34.769897],[106.550579,34.82936],[106.556122,34.861285],[106.527789,34.876507],[106.493296,34.941289],[106.484673,34.983959],[106.494528,35.006021],[106.494528,35.006021],[106.52163,35.027587],[106.541956,35.083925],[106.577064,35.089312],[106.615252,35.071191],[106.706411,35.081966],[106.710723,35.100574],[106.838222,35.080007],[106.901664,35.094698],[106.950323,35.066782],[106.990975,35.068252],[107.012533,35.029547],[107.08275,35.024156],[107.089526,34.976604],[107.119707,34.950119],[107.162206,34.944233],[107.189308,34.893198],[107.252749,34.880925],[107.286626,34.931968],[107.350068,34.93393],[107.369162,34.917738],[107.400575,34.932949],[107.455394,34.916757],[107.523763,34.909886],[107.564415,34.968757],[107.619849,34.964834],[107.638943,34.935402],[107.675284,34.9511],[107.741805,34.953553],[107.842203,34.979056],[107.863145,34.999158],[107.846515,35.024646],[107.814486,35.024646],[107.773218,35.060904],[107.773218,35.060904],[107.769523,35.064333],[107.769523,35.064333],[107.727639,35.120157],[107.715936,35.168114],[107.686371,35.218],[107.652494,35.244886],[107.667277,35.257104],[107.737494,35.267366],[107.745501,35.311819],[107.841587,35.276649],[107.867457,35.256127],[107.960464,35.263457],[107.949993,35.245375],[108.049159,35.253683],[108.094739,35.280069],[108.174811,35.304981],[108.221622,35.296678],[108.239484,35.256127],[108.296767,35.267855],[108.345426,35.300586],[108.36144,35.279581],[108.48894,35.275184],[108.547454,35.304981],[108.583178,35.294724],[108.614591,35.328909],[108.61028,35.355271],[108.631222,35.418698],[108.605968,35.503028],[108.625678,35.537124],[108.618287,35.557088],[108.539447,35.605761],[108.517889,35.699615],[108.533903,35.746257],[108.527744,35.82442],[108.499411,35.872444],[108.518505,35.905414],[108.562852,35.921409],[108.593649,35.950967],[108.652164,35.94806],[108.659555,35.990683],[108.688504,36.021183],[108.682345,36.062316],[108.712526,36.138716],[108.646004,36.254143],[108.641693,36.359279],[108.651548,36.384818],[108.618903,36.433946],[108.562852,36.43876],[108.510498,36.47438],[108.514809,36.445501],[108.495099,36.422389],[108.460606,36.422871],[108.408252,36.45946],[108.391621,36.505654],[108.365136,36.519603],[108.340498,36.559032],[108.262274,36.549417],[108.245644,36.571048],[108.210535,36.577296],[108.204992,36.606607],[108.204992,36.606607],[108.222854,36.631105],[108.1976,36.630144],[108.163724,36.563839],[108.092891,36.587388],[108.079956,36.614294],[108.060862,36.592194],[108.001732,36.639269],[108.02329,36.647912],[108.006659,36.683435],[107.938906,36.655594],[107.940754,36.694953],[107.914268,36.720861],[107.907493,36.750118],[107.866841,36.766899],[107.768291,36.792783],[107.742421,36.811951],[107.722095,36.802367],[107.670356,36.83303],[107.642023,36.819137],[107.5909,36.836382],[107.540393,36.828718],[107.533618,36.867031],[107.478183,36.908196],[107.365466,36.905324],[107.336517,36.925899],[107.310032,36.912502],[107.291554,36.979463],[107.291554,36.979463],[107.288474,37.008143],[107.288474,37.008143],[107.28601,37.054963],[107.268764,37.099367],[107.234887,37.096503],[107.181916,37.143269],[107.133873,37.134681],[107.095685,37.115595],[107.030395,37.140883],[107.031011,37.108436],[106.998367,37.106527],[106.905976,37.151378],[106.912135,37.110345],[106.891193,37.098413],[106.818512,37.141838],[106.776012,37.158056],[106.772933,37.120367],[106.750143,37.09889],[106.728585,37.121321],[106.687933,37.12991],[106.673151,37.1113],[106.6171,37.135158],[106.605397,37.127524],[106.645433,37.064992],[106.666991,37.016745],[106.646665,37.000496],[106.64297,36.962729],[106.594926,36.967988],[106.595542,36.94025],[106.540108,36.984244],[106.549347,36.941685],[106.601702,36.918244],[106.609709,36.878521],[106.609709,36.878521],[106.626955,36.892403],[106.637426,36.867031],[106.637426,36.867031],[106.657752,36.820575],[106.627571,36.752995],[106.644817,36.72278],[106.59431,36.750118],[106.514238,36.715584],[106.519782,36.708868],[106.519782,36.708868],[106.530869,36.690154],[106.490833,36.685835],[106.491448,36.628703],[106.444637,36.624861],[106.465579,36.583063],[106.444637,36.557109],[106.397826,36.576816],[106.392282,36.556628],[106.363949,36.577296],[106.37134,36.549417],[106.39721,36.548455],[106.455724,36.496995],[106.494528,36.494589],[106.523477,36.468605],[106.492064,36.422389],[106.510543,36.379037],[106.497608,36.31348],[106.470507,36.306246],[106.504383,36.266207],[106.54134,36.25366],[106.559202,36.292259],[106.647897,36.259451],[106.685469,36.273445],[106.698404,36.244008],[106.735976,36.23725],[106.772933,36.212628],[106.808657,36.21118],[106.833295,36.229044],[106.858548,36.206834],[106.858548,36.206834],[106.873947,36.178338],[106.873947,36.178338],[106.930613,36.138716],[106.925686,36.115997],[106.957715,36.091337],[106.940468,36.064734],[106.928149,36.011502],[106.94786,35.988262],[106.90228,35.943699],[106.93862,35.952905],[106.940468,35.931101],[106.912751,35.93207],[106.849925,35.887476],[106.927534,35.810346],[106.897353,35.759856],[106.868403,35.771996],[106.867171,35.738485],[106.819128,35.7448],[106.806193,35.70982],[106.750759,35.725369],[106.750759,35.689408],[106.674998,35.728284],[106.66268,35.70739],[106.633115,35.714679],[106.620796,35.743829],[106.595542,35.727312],[106.566593,35.738971],[106.506231,35.737514]]],[[[106.047356,35.498155],[106.048588,35.488898],[106.054132,35.45478],[106.071994,35.463555],[106.078769,35.509848],[106.047356,35.498155]]],[[[102.831537,36.365544],[102.829689,36.365544],[102.836465,36.344819],[102.838928,36.345783],[102.831537,36.365544]]],[[[106.073226,35.447468],[106.067682,35.436254],[106.073226,35.420649],[106.083081,35.421624],[106.073226,35.447468]]],[[[106.504383,35.736057],[106.506231,35.737514],[106.49268,35.732656],[106.498224,35.732656],[106.504383,35.736057]]]]}},
    {"type":"Feature","properties":{"adcode":630000,"name":"青海省","center":[101.778916,36.623178],"centroid":[96.043533,35.726403],"childrenNum":8,"level":"province","parent":{"adcode":100000},"subFeatureIndex":28,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[102.829689,36.365544],[102.771791,36.47438],[102.793349,36.497957],[102.753313,36.525855],[102.734219,36.562396],[102.761936,36.568645],[102.714509,36.599401],[102.724364,36.613813],[102.684328,36.619097],[102.630741,36.650793],[102.601176,36.710307],[102.612879,36.738129],[102.639364,36.732853],[102.692335,36.775528],[102.720052,36.767858],[102.639364,36.852666],[102.587009,36.869904],[102.56114,36.91968],[102.526031,36.928291],[102.499546,36.954599],[102.450271,36.968467],[102.506321,37.019134],[102.488459,37.078362],[102.583314,37.104618],[102.642444,37.099845],[102.599944,37.174748],[102.578386,37.17284],[102.533422,37.217176],[102.490307,37.223371],[102.457662,37.248147],[102.45335,37.271487],[102.419474,37.294343],[102.428097,37.308624],[102.368351,37.327662],[102.29875,37.370004],[102.299981,37.391404],[102.19712,37.420403],[102.176794,37.458892],[102.125055,37.48549],[102.103497,37.482641],[102.131214,37.54625],[102.102265,37.582304],[102.035128,37.627819],[102.048678,37.651515],[102.036359,37.685149],[101.998787,37.724921],[101.946432,37.728235],[101.873135,37.686569],[101.854657,37.664781],[101.815853,37.654357],[101.791832,37.696041],[101.659405,37.733441],[101.670491,37.754264],[101.598427,37.827569],[101.551615,37.835604],[101.459224,37.86632],[101.382848,37.822369],[101.362522,37.791162],[101.276906,37.83655],[101.202994,37.84742],[101.159262,37.86821],[101.152486,37.891356],[101.114298,37.92016],[101.103211,37.946593],[101.077342,37.941874],[100.964009,38.011221],[100.91843,37.999432],[100.895024,38.013107],[100.888864,38.056001],[100.922125,38.084741],[100.91843,38.129006],[100.93814,38.16007],[100.913502,38.17889],[100.860531,38.148305],[100.825423,38.158658],[100.752126,38.238612],[100.71517,38.253652],[100.619083,38.26587],[100.595061,38.242372],[100.545786,38.247072],[100.516837,38.272448],[100.474953,38.288891],[100.459555,38.2654],[100.432453,38.275267],[100.424446,38.307208],[100.396729,38.293118],[100.318505,38.329276],[100.331439,38.337257],[100.301874,38.388405],[100.259374,38.366355],[100.24028,38.441861],[100.163288,38.461546],[100.113397,38.497151],[100.086911,38.492936],[100.064122,38.518694],[100.025933,38.507923],[100.001296,38.467169],[100.022238,38.432017],[100.093071,38.407166],[100.136803,38.33444],[100.163904,38.328337],[100.159592,38.291239],[100.182998,38.222158],[100.126332,38.231561],[100.117093,38.253652],[100.071513,38.284663],[100.049955,38.283254],[100.001912,38.315191],[99.960028,38.320825],[99.826985,38.370109],[99.758,38.410449],[99.727203,38.415607],[99.65945,38.449361],[99.63974,38.474666],[99.585537,38.498556],[99.52887,38.546314],[99.501769,38.612281],[99.450646,38.60433],[99.412458,38.665571],[99.375502,38.684727],[99.361951,38.718354],[99.291118,38.765966],[99.222133,38.788827],[99.141445,38.852706],[99.068764,38.896968],[99.071843,38.921184],[99.107568,38.951907],[99.054597,38.97657],[98.951735,38.987735],[98.903076,39.012384],[98.886446,39.040744],[98.818076,39.064911],[98.816845,39.085818],[98.743548,39.086747],[98.730613,39.057011],[98.70536,39.043533],[98.661628,38.993782],[98.612353,38.977035],[98.624056,38.959353],[98.584635,38.93003],[98.526737,38.95563],[98.457752,38.952838],[98.428187,38.976104],[98.432498,38.996107],[98.401086,39.001688],[98.383839,39.029588],[98.316702,39.040744],[98.280977,39.027263],[98.287753,38.992386],[98.276666,38.963541],[98.235398,38.918855],[98.242173,38.880664],[98.167645,38.840121],[98.091884,38.786495],[98.068478,38.816344],[98.029058,38.834061],[98.009348,38.85923],[97.875689,38.898365],[97.828878,38.93003],[97.701379,38.963076],[97.679205,39.010524],[97.58127,39.052364],[97.504894,39.076527],[97.458698,39.117863],[97.401416,39.146645],[97.371235,39.140611],[97.347213,39.167528],[97.315185,39.164744],[97.220946,39.193042],[97.14149,39.199999],[97.060186,39.19768],[97.017686,39.208347],[96.962251,39.198144],[97.012142,39.142004],[96.969643,39.097895],[96.95794,39.041674],[96.965331,39.017034],[96.938846,38.95563],[96.940693,38.90768],[96.983809,38.869016],[96.993664,38.834993],[96.987505,38.793025],[97.00044,38.7613],[97.023229,38.755699],[97.009063,38.702477],[97.057722,38.67258],[97.047251,38.653888],[97.055874,38.594508],[96.961019,38.558015],[96.876636,38.580475],[96.847071,38.599186],[96.7941,38.608072],[96.808882,38.582346],[96.767614,38.552399],[96.800259,38.52759],[96.780549,38.504177],[96.706637,38.505582],[96.6666,38.483567],[96.707868,38.459203],[96.698013,38.422172],[96.626564,38.356031],[96.638883,38.307208],[96.655514,38.295936],[96.665369,38.23015],[96.46334,38.277616],[96.378341,38.277146],[96.335841,38.246132],[96.301964,38.183124],[96.313051,38.161952],[96.264392,38.145952],[96.252689,38.167599],[96.221892,38.149246],[96.109175,38.187358],[96.06606,38.173245],[96.006929,38.207582],[95.93856,38.237202],[95.932401,38.259291],[95.89606,38.2903],[95.852945,38.287481],[95.83693,38.344298],[95.775952,38.356031],[95.723597,38.378554],[95.703887,38.400131],[95.671858,38.388405],[95.608417,38.339134],[95.585011,38.343359],[95.51849,38.294997],[95.487693,38.314721],[95.455664,38.291709],[95.440881,38.310965],[95.408236,38.300163],[95.315846,38.318947],[95.259179,38.302981],[95.229614,38.330685],[95.209904,38.327868],[95.185266,38.379492],[95.140919,38.392158],[95.122441,38.417014],[95.072549,38.402476],[95.045448,38.418889],[94.973999,38.430142],[94.884072,38.414669],[94.861282,38.393565],[94.812623,38.385591],[94.672805,38.386998],[94.582878,38.36917],[94.56132,38.351807],[94.527443,38.365416],[94.527443,38.425922],[94.511429,38.445142],[94.370379,38.7627],[94.281067,38.7599],[93.973098,38.724891],[93.95154,38.715086],[93.885018,38.720689],[93.800019,38.750566],[93.773533,38.771099],[93.756287,38.807484],[93.769838,38.821007],[93.884403,38.826136],[93.884403,38.867618],[93.834511,38.867618],[93.729186,38.924443],[93.453245,38.915596],[93.274007,38.896036],[93.237666,38.916062],[93.179152,38.923977],[93.198246,39.045857],[93.165601,39.090928],[93.131725,39.108112],[93.142196,39.160567],[93.115094,39.17959],[93.043029,39.146645],[92.978356,39.143396],[92.938936,39.169848],[92.889045,39.160103],[92.866871,39.138754],[92.765857,39.136898],[92.659299,39.109969],[92.545966,39.111362],[92.489916,39.099753],[92.459119,39.063982],[92.459119,39.042604],[92.41046,39.03842],[92.416003,39.010524],[92.380279,38.999828],[92.263866,39.002153],[92.197961,38.983548],[92.173323,38.960749],[92.10865,38.963541],[91.966368,38.930961],[91.880752,38.899297],[91.87952,38.884391],[91.806223,38.872744],[91.694738,38.86622],[91.681188,38.852706],[91.501333,38.815411],[91.446515,38.813546],[91.298689,38.746365],[91.242639,38.752433],[91.188436,38.73096],[90.992567,38.695003],[90.970394,38.697806],[90.899561,38.679588],[90.724634,38.658094],[90.65996,38.674449],[90.619308,38.664636],[90.645794,38.635191],[90.606374,38.610878],[90.608837,38.594508],[90.560794,38.593573],[90.525685,38.561291],[90.463476,38.556611],[90.465323,38.521971],[90.427135,38.493873],[90.353222,38.482162],[90.315034,38.501835],[90.248513,38.491531],[90.130868,38.494341],[90.111774,38.477945],[90.111774,38.418889],[90.129636,38.400131],[90.179528,38.396848],[90.137644,38.340543],[90.280542,38.238142],[90.352607,38.233441],[90.361846,38.300163],[90.401882,38.311434],[90.531229,38.319886],[90.516446,38.207111],[90.519526,37.730601],[90.579272,37.720661],[90.586663,37.703144],[90.643946,37.696988],[90.777605,37.648672],[90.820104,37.613599],[90.854597,37.604117],[90.882314,37.575664],[90.865684,37.53059],[90.911879,37.519674],[90.958075,37.477891],[91.019669,37.493088],[91.073256,37.475992],[91.099741,37.447965],[91.113292,37.387124],[91.136081,37.355734],[91.134849,37.324331],[91.194596,37.273868],[91.1909,37.205737],[91.280211,37.163779],[91.286371,37.105095],[91.303617,37.083136],[91.291298,37.042544],[91.303617,37.012444],[91.216153,37.010054],[91.181045,37.025345],[91.133618,37.007665],[91.126842,36.978507],[91.051698,36.96751],[91.036915,36.929727],[90.983944,36.913459],[90.924198,36.921115],[90.853981,36.915373],[90.758511,36.825844],[90.732025,36.825844],[90.727098,36.755872],[90.754815,36.721341],[90.720938,36.708868],[90.706156,36.658955],[90.730793,36.655594],[90.72217,36.620058],[90.741264,36.585947],[90.810865,36.585466],[90.831191,36.55807],[90.905104,36.560474],[91.011662,36.539801],[91.035683,36.529703],[91.039995,36.474861],[91.028292,36.443093],[91.051698,36.433946],[91.026444,36.323607],[91.07264,36.299012],[91.051698,36.238215],[91.096045,36.219871],[91.09235,36.163844],[91.124994,36.115514],[91.081263,36.088436],[90.979017,36.106811],[90.922966,36.028927],[90.850285,36.016827],[90.815793,36.035703],[90.776373,36.086501],[90.659344,36.13485],[90.613149,36.126632],[90.534925,36.147899],[90.478258,36.13195],[90.424055,36.133883],[90.325505,36.159496],[90.23681,36.160462],[90.198006,36.187516],[90.130252,36.2078],[90.145651,36.239181],[90.058188,36.255591],[90.043405,36.276822],[90.003369,36.278752],[90.028006,36.258486],[90.019999,36.213594],[89.997825,36.168193],[89.944855,36.140649],[89.941159,36.067637],[89.914058,36.079246],[89.819819,36.080697],[89.766848,36.073925],[89.711414,36.093272],[89.688624,36.091337],[89.605472,36.038123],[89.474893,36.022151],[89.417611,36.044897],[89.404676,36.016827],[89.434857,35.992136],[89.428082,35.917531],[89.489676,35.903475],[89.554965,35.873414],[89.550654,35.856924],[89.62395,35.859349],[89.654747,35.848193],[89.707718,35.849163],[89.778551,35.861775],[89.801957,35.848193],[89.767464,35.799183],[89.782863,35.773453],[89.747138,35.7516],[89.748986,35.66267],[89.726196,35.648082],[89.765616,35.599922],[89.75145,35.580942],[89.71203,35.581915],[89.699711,35.544916],[89.720037,35.501566],[89.740979,35.507412],[89.765,35.482563],[89.739131,35.468429],[89.685544,35.416259],[89.658443,35.425526],[89.619639,35.412357],[89.58761,35.383575],[89.497067,35.361128],[89.516161,35.330862],[89.494603,35.298632],[89.531559,35.276161],[89.48598,35.256616],[89.450255,35.223867],[89.46935,35.214577],[89.519241,35.133862],[89.579603,35.118688],[89.593153,35.104491],[89.59069,35.057965],[89.560509,34.938836],[89.578987,34.895162],[89.670146,34.887798],[89.707102,34.919701],[89.747138,34.903506],[89.78779,34.921664],[89.821051,34.902033],[89.814891,34.86816],[89.838913,34.865705],[89.867862,34.81069],[89.825978,34.796931],[89.799493,34.743838],[89.732356,34.732035],[89.72558,34.660689],[89.74837,34.641981],[89.798877,34.628686],[89.777935,34.574499],[89.814891,34.548871],[89.823515,34.455657],[89.819819,34.420614],[89.799493,34.39642],[89.820435,34.369255],[89.858623,34.359375],[89.86663,34.324785],[89.825362,34.293642],[89.838297,34.263477],[89.816739,34.16945],[89.789638,34.150632],[89.760073,34.152613],[89.756993,34.124874],[89.71203,34.131809],[89.655979,34.097126],[89.656595,34.057966],[89.635037,34.049537],[89.684928,33.990013],[89.688008,33.959739],[89.718805,33.946832],[89.73174,33.921509],[89.795181,33.865374],[89.837065,33.868853],[89.899891,33.80771],[89.942391,33.801246],[89.902355,33.758467],[89.907282,33.741051],[89.983659,33.725622],[89.981195,33.70322],[90.008296,33.687785],[89.984275,33.612061],[90.01076,33.553728],[90.083441,33.525295],[90.088984,33.478885],[90.107463,33.460913],[90.22018,33.437943],[90.246665,33.423959],[90.332896,33.310501],[90.363077,33.279487],[90.405577,33.260473],[90.490577,33.264977],[90.562642,33.229441],[90.627315,33.180368],[90.704308,33.135778],[90.740032,33.142293],[90.803474,33.114227],[90.88293,33.120241],[90.902024,33.083143],[90.927894,33.120241],[91.001807,33.11573],[91.037531,33.098686],[91.072024,33.113224],[91.147784,33.07211],[91.161335,33.108712],[91.18782,33.106206],[91.226624,33.141792],[91.261733,33.141291],[91.311624,33.108211],[91.370138,33.100691],[91.436044,33.066092],[91.49579,33.109214],[91.535826,33.10019],[91.55492,33.060074],[91.583253,33.0375],[91.664557,33.012913],[91.685499,32.989324],[91.752637,32.969242],[91.799448,32.942126],[91.839484,32.948152],[91.857962,32.90244],[91.896766,32.907967],[91.955897,32.8205],[92.018722,32.829552],[92.038432,32.860725],[92.101874,32.860222],[92.145606,32.885857],[92.205352,32.866255],[92.227526,32.821003],[92.193649,32.801889],[92.211511,32.788306],[92.198577,32.754591],[92.255243,32.720863],[92.310062,32.751571],[92.343938,32.738484],[92.355641,32.764657],[92.411076,32.748048],[92.459119,32.76365],[92.484372,32.745028],[92.56814,32.73194],[92.574916,32.741001],[92.634662,32.720863],[92.667922,32.73194],[92.686401,32.76516],[92.756618,32.743014],[92.789262,32.719856],[92.822523,32.729926],[92.866871,32.698203],[92.933392,32.719353],[92.964189,32.714821],[93.00053,32.741001],[93.019624,32.737477],[93.023935,32.703239],[93.069515,32.626156],[93.087993,32.63674],[93.159442,32.644803],[93.176688,32.6705],[93.210565,32.655385],[93.239514,32.662439],[93.260456,32.62666],[93.300492,32.619604],[93.308499,32.580278],[93.33868,32.5712],[93.385492,32.525294],[93.411977,32.558086],[93.4631,32.556069],[93.476651,32.504603],[93.501904,32.503593],[93.516687,32.47583],[93.618933,32.522771],[93.651577,32.571705],[93.721795,32.578261],[93.75136,32.56313],[93.820345,32.549511],[93.851142,32.50965],[93.861613,32.466237],[93.90904,32.463207],[93.960163,32.484917],[93.978641,32.459672],[94.03038,32.448057],[94.049474,32.469771],[94.091974,32.463207],[94.137554,32.433915],[94.176974,32.454117],[94.196684,32.51621],[94.250886,32.51722],[94.292154,32.502584],[94.294002,32.519743],[94.350053,32.533871],[94.371611,32.524789],[94.395016,32.594397],[94.435052,32.562626],[94.463386,32.572209],[94.459074,32.599439],[94.522516,32.595909],[94.591501,32.640772],[94.614291,32.673522],[94.638312,32.645307],[94.737479,32.587338],[94.762116,32.526303],[94.78737,32.522266],[94.80708,32.486431],[94.852043,32.463712],[94.889616,32.472295],[94.912405,32.41573],[94.944434,32.404109],[94.988166,32.422802],[95.057151,32.395014],[95.075013,32.376315],[95.075013,32.376315],[95.081789,32.384907],[95.153853,32.386423],[95.218527,32.397035],[95.228382,32.363678],[95.261643,32.348006],[95.193274,32.332331],[95.096571,32.322217],[95.079325,32.279726],[95.10581,32.258979],[95.20744,32.297433],[95.214216,32.321712],[95.241317,32.3207],[95.239469,32.287315],[95.270266,32.194683],[95.270266,32.194683],[95.31523,32.148585],[95.366968,32.151118],[95.367584,32.178982],[95.406389,32.182021],[95.440265,32.157705],[95.454432,32.061898],[95.421171,32.033999],[95.454432,32.007613],[95.395918,32.001523],[95.360809,31.95939],[95.3682,31.92892],[95.408852,31.918761],[95.406389,31.896915],[95.456896,31.801853],[95.480301,31.795749],[95.511714,31.750468],[95.546823,31.73978],[95.580083,31.76726],[95.634286,31.782523],[95.779648,31.748941],[95.823995,31.68225],[95.853561,31.714329],[95.846169,31.736218],[95.89914,31.81711],[95.983524,31.816601],[95.989067,31.78761],[96.064828,31.720438],[96.135661,31.70211],[96.148595,31.686324],[96.156603,31.602769],[96.207726,31.598691],[96.221892,31.647613],[96.245298,31.657802],[96.252073,31.697527],[96.222508,31.733164],[96.231131,31.749959],[96.178161,31.775401],[96.183088,31.835924],[96.202798,31.841008],[96.214501,31.876589],[96.188632,31.904028],[96.220044,31.905553],[96.253305,31.929936],[96.288414,31.919777],[96.389428,31.919777],[96.407906,31.845583],[96.435623,31.796258],[96.468884,31.769804],[96.519391,31.74945],[96.56805,31.711783],[96.615477,31.737236],[96.661057,31.705674],[96.691854,31.722474],[96.722651,31.686833],[96.778701,31.675629],[96.790404,31.698545],[96.840295,31.720438],[96.799027,31.792188],[96.765767,31.819144],[96.760223,31.860325],[96.794716,31.869474],[96.81073,31.894375],[96.776238,31.935015],[96.753448,31.944156],[96.742977,32.001016],[96.722651,32.013195],[96.824281,32.007613],[96.868629,31.964975],[96.863085,31.996448],[96.894498,32.013703],[96.941925,31.986297],[96.965947,32.008628],[96.935766,32.048203],[97.006599,32.067984],[97.028773,32.04871],[97.127323,32.044145],[97.169823,32.032984],[97.188301,32.055304],[97.214786,32.042623],[97.233881,32.063927],[97.201852,32.090296],[97.219714,32.109054],[97.258518,32.072041],[97.308409,32.076605],[97.293011,32.096887],[97.313953,32.130342],[97.271453,32.139971],[97.264062,32.182527],[97.299786,32.294904],[97.32196,32.303503],[97.371235,32.273148],[97.415583,32.296421],[97.424822,32.322723],[97.387865,32.427349],[97.341054,32.440987],[97.388481,32.501575],[97.334895,32.514192],[97.332431,32.542448],[97.3583,32.563635],[97.374315,32.546484],[97.411887,32.575235],[97.448843,32.586833],[97.463626,32.55506],[97.50243,32.530844],[97.540618,32.536899],[97.670582,32.51722],[97.684132,32.530339],[97.730944,32.527312],[97.700763,32.53488],[97.616995,32.586329],[97.607756,32.614059],[97.543698,32.62162],[97.535075,32.638252],[97.48272,32.654377],[97.42359,32.70475],[97.429133,32.714318],[97.386018,32.77925],[97.392793,32.828546],[97.376163,32.886359],[97.347829,32.895907],[97.375547,32.956689],[97.438372,32.976271],[97.523988,32.988822],[97.499966,33.011408],[97.542466,33.035995],[97.517213,33.097683],[97.487032,33.107209],[97.498119,33.137783],[97.487648,33.168346],[97.548626,33.203907],[97.607756,33.263976],[97.622538,33.337005],[97.676125,33.341004],[97.754349,33.409972],[97.674893,33.432949],[97.625618,33.461412],[97.552321,33.465906],[97.511669,33.520805],[97.523372,33.577166],[97.450075,33.582152],[97.415583,33.605582],[97.435293,33.682307],[97.418046,33.728608],[97.422974,33.754984],[97.406344,33.795278],[97.373083,33.817655],[97.371851,33.842015],[97.398336,33.848477],[97.395257,33.889224],[97.460546,33.887236],[97.503662,33.912073],[97.52214,33.903133],[97.601596,33.929951],[97.629314,33.919523],[97.660111,33.956264],[97.652719,33.998448],[97.70261,34.036644],[97.665654,34.126855],[97.766668,34.158555],[97.789458,34.182818],[97.789458,34.182818],[97.796849,34.199154],[97.796849,34.199154],[97.8104,34.207568],[97.898479,34.209548],[97.95453,34.190739],[98.028442,34.122892],[98.098043,34.122892],[98.158405,34.107037],[98.206449,34.08424],[98.258188,34.083249],[98.344419,34.094648],[98.399854,34.085231],[98.396774,34.053008],[98.428187,34.029204],[98.440506,33.981577],[98.415252,33.956761],[98.425723,33.913066],[98.407245,33.867362],[98.434962,33.843009],[98.463295,33.848477],[98.492861,33.796272],[98.494092,33.768915],[98.51873,33.77389],[98.539672,33.746525],[98.582788,33.731595],[98.610505,33.682805],[98.6567,33.64744],[98.61728,33.637476],[98.622824,33.610067],[98.652389,33.595114],[98.648077,33.548741],[98.678258,33.522801],[98.725686,33.503341],[98.742316,33.477887],[98.736157,33.406975],[98.779888,33.370497],[98.759562,33.276985],[98.802062,33.270481],[98.804526,33.219428],[98.858728,33.150811],[98.92217,33.118738],[98.967134,33.115229],[98.971445,33.098185],[99.014561,33.081137],[99.024416,33.094675],[99.090322,33.079131],[99.124814,33.046028],[99.196263,33.035493],[99.214741,32.991332],[99.235067,32.982296],[99.24677,32.924043],[99.268944,32.878318],[99.353944,32.885354],[99.376118,32.899927],[99.45311,32.862233],[99.558436,32.839106],[99.589233,32.789312],[99.640355,32.790822],[99.646515,32.774721],[99.700718,32.76667],[99.717964,32.732443],[99.760464,32.769689],[99.766623,32.826032],[99.791877,32.883344],[99.764159,32.924545],[99.788181,32.956689],[99.805427,32.940619],[99.851007,32.941623],[99.877492,32.993339],[99.877492,33.045527],[99.947709,32.986814],[99.956332,32.948152],[100.038252,32.929066],[100.029629,32.895907],[100.064738,32.895907],[100.123252,32.837095],[100.117093,32.802392],[100.139266,32.724388],[100.088143,32.668988],[100.109701,32.640268],[100.189773,32.630692],[100.208252,32.606497],[100.229809,32.650346],[100.231041,32.696189],[100.258759,32.742511],[100.339447,32.719353],[100.399193,32.756101],[100.378251,32.698707],[100.420135,32.73194],[100.450932,32.694678],[100.470026,32.694678],[100.516837,32.632204],[100.54517,32.569687],[100.603069,32.553547],[100.645568,32.526303],[100.657887,32.546484],[100.661583,32.616075],[100.673286,32.628172],[100.710242,32.610026],[100.71209,32.645307],[100.690532,32.678056],[100.77122,32.643795],[100.834046,32.648835],[100.887633,32.632708],[100.93198,32.600447],[100.956618,32.621116],[100.99727,32.627668],[101.030531,32.660424],[101.077342,32.68259],[101.124769,32.658408],[101.157414,32.661431],[101.22332,32.725898],[101.237486,32.825026],[101.223935,32.855698],[101.178356,32.892892],[101.124153,32.909976],[101.134624,32.95217],[101.129081,32.989324],[101.183899,32.984304],[101.171581,33.009902],[101.184515,33.041514],[101.146327,33.056563],[101.143863,33.086151],[101.169733,33.10019],[101.11553,33.194893],[101.124769,33.221431],[101.156798,33.236449],[101.182668,33.26948],[101.217776,33.256469],[101.297232,33.262475],[101.381616,33.153316],[101.393935,33.157826],[101.386543,33.207412],[101.403174,33.225436],[101.487557,33.226938],[101.515275,33.192889],[101.557775,33.167344],[101.633535,33.101193],[101.661252,33.135778],[101.653861,33.162835],[101.709912,33.21292],[101.735781,33.279987],[101.677883,33.297497],[101.64955,33.323004],[101.663716,33.383991],[101.695745,33.433948],[101.769042,33.45592],[101.777665,33.533776],[101.769042,33.538765],[101.748716,33.505337],[101.718535,33.494857],[101.622448,33.502343],[101.611977,33.565199],[101.616905,33.598603],[101.585492,33.645448],[101.58426,33.674339],[101.501724,33.702723],[101.428427,33.680315],[101.424732,33.655411],[101.385312,33.644949],[101.302776,33.657902],[101.23687,33.685793],[101.217776,33.669856],[101.166653,33.659894],[101.177124,33.685295],[101.162957,33.719649],[101.186363,33.741051],[101.190675,33.791796],[101.153102,33.823124],[101.153718,33.8445],[101.054552,33.863386],[101.023139,33.896178],[100.994806,33.891707],[100.965857,33.946832],[100.927669,33.975126],[100.93506,33.990013],[100.880857,34.036644],[100.870386,34.083744],[100.848828,34.089692],[100.806329,34.155584],[100.764445,34.178857],[100.809408,34.247153],[100.798321,34.260014],[100.821727,34.317371],[100.868538,34.332693],[100.895024,34.375183],[100.951074,34.38358],[100.986799,34.374689],[101.054552,34.322808],[101.098284,34.329233],[101.178356,34.320831],[101.193754,34.336646],[101.235022,34.325279],[101.228863,34.298586],[101.268899,34.278808],[101.325565,34.268423],[101.327413,34.24468],[101.369913,34.248143],[101.417956,34.227858],[101.482014,34.218951],[101.492485,34.195689],[101.53868,34.212022],[101.6206,34.178857],[101.674187,34.110506],[101.703136,34.119424],[101.718535,34.083249],[101.736397,34.080275],[101.764114,34.122892],[101.788136,34.131809],[101.836795,34.124378],[101.851578,34.153108],[101.874367,34.130323],[101.897773,34.133791],[101.955055,34.109514],[101.965526,34.167469],[102.003099,34.162022],[102.030816,34.190739],[102.01357,34.218456],[102.062229,34.227858],[102.067772,34.293642],[102.149692,34.271885],[102.186649,34.352952],[102.237156,34.34307],[102.237156,34.34307],[102.259329,34.355917],[102.205743,34.407777],[102.169402,34.457631],[102.155852,34.507456],[102.139837,34.50351],[102.093026,34.536547],[102.001867,34.538519],[101.97415,34.548871],[101.956287,34.582876],[101.934729,34.58731],[101.919947,34.621791],[101.917483,34.705964],[101.923027,34.835746],[101.916867,34.873561],[101.985852,34.90007],[102.068388,34.887798],[102.048062,34.910868],[102.094874,34.986901],[102.133678,35.014844],[102.157699,35.010923],[102.176178,35.032977],[102.211286,35.034937],[102.218062,35.057475],[102.252554,35.048657],[102.29567,35.071681],[102.310452,35.128967],[102.346793,35.164201],[102.404075,35.179366],[102.365887,35.235599],[102.370199,35.263946],[102.3123,35.282512],[102.280887,35.303028],[102.311684,35.31426],[102.317844,35.343067],[102.287663,35.36552],[102.293822,35.424063],[102.314764,35.434303],[102.408387,35.409431],[102.447807,35.437229],[102.437952,35.455268],[102.49893,35.545403],[102.503241,35.585322],[102.531575,35.580455],[102.570995,35.548324],[102.695414,35.528358],[102.743458,35.494745],[102.782878,35.527871],[102.729291,35.523487],[102.746537,35.545403],[102.808747,35.560496],[102.763168,35.612086],[102.7644,35.653431],[102.744074,35.657807],[102.707733,35.70496],[102.686175,35.771996],[102.715125,35.815685],[102.739146,35.821023],[102.787189,35.862745],[102.81737,35.850133],[102.914073,35.845282],[102.94487,35.829757],[102.954725,35.858864],[102.942406,35.92674],[102.971971,35.995525],[102.951645,36.021667],[102.968276,36.044414],[102.932551,36.048285],[102.882044,36.082632],[102.941174,36.104877],[102.948566,36.150798],[102.965812,36.151765],[102.986754,36.193312],[103.048964,36.199107],[103.066826,36.216974],[103.021246,36.232906],[103.024942,36.256556],[102.922696,36.298047],[102.896827,36.331803],[102.845704,36.331803],[102.836465,36.344819],[102.829689,36.365544]]]]}},
    {"type":"Feature","properties":{"adcode":640000,"name":"宁夏回族自治区","center":[106.278179,38.46637],"centroid":[106.169866,37.291332],"childrenNum":5,"level":"province","parent":{"adcode":100000},"subFeatureIndex":29,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[107.268764,37.099367],[107.281083,37.127047],[107.306952,37.100799],[107.334669,37.138975],[107.336517,37.165687],[107.317423,37.200017],[107.270612,37.229089],[107.309416,37.239095],[107.273075,37.29101],[107.257677,37.337179],[107.282931,37.437036],[107.284162,37.481691],[107.345756,37.518725],[107.369162,37.58752],[107.330358,37.584201],[107.311264,37.609806],[107.361155,37.613125],[107.422133,37.665254],[107.389488,37.671413],[107.387024,37.691305],[107.425828,37.684201],[107.484959,37.706458],[107.499125,37.765619],[107.57119,37.776499],[107.599523,37.791162],[107.620465,37.776026],[107.646335,37.805349],[107.659269,37.844112],[107.65003,37.86443],[107.560719,37.893717],[107.49235,37.944706],[107.448618,37.933378],[107.411662,37.948009],[107.440611,37.995659],[107.3938,38.014993],[107.33159,38.086625],[107.240431,38.111586],[107.19054,38.153953],[107.138801,38.161011],[107.119091,38.134185],[107.071047,38.138892],[107.051337,38.122886],[107.010069,38.120532],[106.942316,38.132302],[106.858548,38.156306],[106.779092,38.171833],[106.737824,38.197706],[106.654672,38.22921],[106.627571,38.232501],[106.555506,38.263521],[106.482209,38.319417],[106.599854,38.389812],[106.647897,38.470917],[106.66268,38.601524],[106.709491,38.718821],[106.756302,38.748699],[106.837606,38.847579],[106.954019,38.941202],[106.971881,39.026333],[106.96757,39.054688],[106.933693,39.076527],[106.878874,39.091392],[106.859164,39.107648],[106.825288,39.19397],[106.795723,39.214375],[106.790795,39.241263],[106.806193,39.277407],[106.806809,39.318625],[106.781556,39.371849],[106.751375,39.381564],[106.683622,39.357506],[106.643586,39.357969],[106.602318,39.37555],[106.556122,39.322329],[106.525325,39.308439],[106.511774,39.272311],[106.402753,39.291767],[106.280181,39.262118],[106.29558,39.167992],[106.285109,39.146181],[106.251232,39.131327],[106.192718,39.142932],[106.170544,39.163352],[106.145907,39.153142],[106.096631,39.084889],[106.078153,39.026333],[106.087392,39.006339],[106.060907,38.96866],[106.021487,38.953769],[105.97098,38.909077],[105.992538,38.857366],[105.909386,38.791159],[105.908154,38.737496],[105.88598,38.716953],[105.894603,38.696405],[105.852719,38.641735],[105.874277,38.593105],[105.856415,38.569714],[105.863806,38.53508],[105.836705,38.476071],[105.850872,38.443736],[105.827466,38.432486],[105.835473,38.387467],[105.821307,38.366824],[105.86627,38.296406],[105.842248,38.240962],[105.802828,38.220277],[105.775111,38.186887],[105.76772,38.121474],[105.780655,38.084741],[105.840401,38.004147],[105.799749,37.939986],[105.80406,37.862068],[105.760944,37.799674],[105.677177,37.771769],[105.622358,37.777919],[105.616199,37.722555],[105.598952,37.699356],[105.467141,37.695094],[105.4037,37.710246],[105.315004,37.702197],[105.221998,37.677097],[105.187505,37.657674],[105.111128,37.633981],[105.027977,37.580881],[104.866601,37.566651],[104.805007,37.539133],[104.623305,37.522522],[104.433595,37.515402],[104.419429,37.511604],[104.407726,37.464592],[104.322726,37.44844],[104.287002,37.428007],[104.298705,37.414223],[104.365226,37.418026],[104.437907,37.445589],[104.448994,37.42468],[104.499501,37.421353],[104.521059,37.43466],[104.679971,37.408044],[104.662109,37.367626],[104.713848,37.329566],[104.673812,37.317668],[104.651022,37.290534],[104.624536,37.298627],[104.600515,37.242907],[104.638087,37.201923],[104.717543,37.208597],[104.776673,37.246718],[104.85613,37.211933],[104.864753,37.17284],[104.888158,37.15901],[104.914644,37.097935],[104.954064,37.077407],[104.95468,37.040156],[105.004571,37.035378],[105.03968,37.007187],[105.05939,37.022956],[105.128991,36.996194],[105.165331,36.99476],[105.185657,36.942164],[105.178882,36.892403],[105.244787,36.894796],[105.279896,36.86751],[105.303302,36.820575],[105.334714,36.80093],[105.340874,36.764502],[105.319932,36.742924],[105.275584,36.752515],[105.272505,36.739567],[105.218302,36.730455],[105.201056,36.700711],[105.225693,36.664716],[105.22015,36.631105],[105.261418,36.602764],[105.2762,36.563358],[105.252179,36.553263],[105.281744,36.522489],[105.322396,36.535954],[105.362432,36.496514],[105.363048,36.443093],[105.398156,36.430575],[105.401236,36.369881],[105.425873,36.330357],[105.455439,36.321678],[105.476381,36.293224],[105.45975,36.268137],[105.460366,36.223733],[105.478844,36.213111],[105.515185,36.147415],[105.491163,36.101009],[105.430801,36.10391],[105.406163,36.074409],[105.343954,36.033767],[105.324859,35.941761],[105.350113,35.875839],[105.39754,35.857409],[105.371055,35.844312],[105.38091,35.792873],[105.408627,35.822479],[105.428953,35.819082],[105.432033,35.787533],[105.457286,35.771511],[105.481924,35.727312],[105.595873,35.715651],[105.667322,35.749657],[105.70243,35.733142],[105.759097,35.724883],[105.740618,35.698643],[105.723988,35.725854],[105.690727,35.698643],[105.722756,35.673366],[105.713517,35.650513],[105.759097,35.634464],[105.762176,35.602841],[105.800365,35.564878],[105.816379,35.575101],[105.847176,35.490359],[105.868734,35.540046],[105.900147,35.54735],[106.017175,35.519103],[106.023335,35.49377],[106.047356,35.498155],[106.078769,35.509848],[106.071994,35.463555],[106.06953,35.458193],[106.073842,35.45478],[106.073226,35.450393],[106.071378,35.449418],[106.073226,35.447468],[106.083081,35.421624],[106.113262,35.361616],[106.129892,35.393333],[106.173008,35.437716],[106.196414,35.409919],[106.237681,35.409431],[106.241377,35.358687],[106.319601,35.265411],[106.363333,35.238532],[106.368261,35.273718],[106.415688,35.276161],[106.472354,35.310842],[106.501304,35.364056],[106.503767,35.415284],[106.483441,35.450393],[106.490217,35.480613],[106.465579,35.481101],[106.440941,35.52641],[106.460036,35.578995],[106.47913,35.575101],[106.460036,35.643705],[106.434782,35.688436],[106.49268,35.732656],[106.506231,35.737514],[106.566593,35.738971],[106.595542,35.727312],[106.620796,35.743829],[106.633115,35.714679],[106.66268,35.70739],[106.674998,35.728284],[106.750759,35.689408],[106.750759,35.725369],[106.806193,35.70982],[106.819128,35.7448],[106.867171,35.738485],[106.868403,35.771996],[106.897353,35.759856],[106.927534,35.810346],[106.849925,35.887476],[106.912751,35.93207],[106.940468,35.931101],[106.93862,35.952905],[106.90228,35.943699],[106.94786,35.988262],[106.928149,36.011502],[106.940468,36.064734],[106.957715,36.091337],[106.925686,36.115997],[106.930613,36.138716],[106.873947,36.178338],[106.873947,36.178338],[106.858548,36.206834],[106.858548,36.206834],[106.833295,36.229044],[106.808657,36.21118],[106.772933,36.212628],[106.735976,36.23725],[106.698404,36.244008],[106.685469,36.273445],[106.647897,36.259451],[106.559202,36.292259],[106.54134,36.25366],[106.504383,36.266207],[106.470507,36.306246],[106.497608,36.31348],[106.510543,36.379037],[106.492064,36.422389],[106.523477,36.468605],[106.494528,36.494589],[106.455724,36.496995],[106.39721,36.548455],[106.37134,36.549417],[106.363949,36.577296],[106.392282,36.556628],[106.397826,36.576816],[106.444637,36.557109],[106.465579,36.583063],[106.444637,36.624861],[106.491448,36.628703],[106.490833,36.685835],[106.530869,36.690154],[106.519782,36.708868],[106.519782,36.708868],[106.514238,36.715584],[106.59431,36.750118],[106.644817,36.72278],[106.627571,36.752995],[106.657752,36.820575],[106.637426,36.867031],[106.637426,36.867031],[106.626955,36.892403],[106.609709,36.878521],[106.609709,36.878521],[106.601702,36.918244],[106.549347,36.941685],[106.540108,36.984244],[106.595542,36.94025],[106.594926,36.967988],[106.64297,36.962729],[106.646665,37.000496],[106.666991,37.016745],[106.645433,37.064992],[106.605397,37.127524],[106.6171,37.135158],[106.673151,37.1113],[106.687933,37.12991],[106.728585,37.121321],[106.750143,37.09889],[106.772933,37.120367],[106.776012,37.158056],[106.818512,37.141838],[106.891193,37.098413],[106.912135,37.110345],[106.905976,37.151378],[106.998367,37.106527],[107.031011,37.108436],[107.030395,37.140883],[107.095685,37.115595],[107.133873,37.134681],[107.181916,37.143269],[107.234887,37.096503],[107.268764,37.099367]]],[[[106.048588,35.488898],[105.897683,35.451368],[105.894603,35.413821],[106.002393,35.438692],[106.034422,35.469404],[106.054132,35.45478],[106.048588,35.488898]]],[[[106.073842,35.45478],[106.06953,35.458193],[106.071378,35.449418],[106.073226,35.450393],[106.073842,35.45478]]]]}},
    {"type":"Feature","properties":{"adcode":650000,"name":"新疆维吾尔自治区","center":[87.617733,43.792818],"centroid":[85.294711,41.371801],"childrenNum":24,"level":"province","parent":{"adcode":100000},"subFeatureIndex":30,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[96.386348,42.727592],[96.363558,42.900562],[95.921314,43.229789],[95.880046,43.28035],[95.857872,43.417436],[95.735916,43.597569],[95.705735,43.67077],[95.645373,43.787966],[95.623199,43.855756],[95.527113,44.007466],[95.426099,44.009618],[95.377439,44.025972],[95.326932,44.028554],[95.35157,44.090054],[95.355882,44.166087],[95.376208,44.227444],[95.4107,44.245024],[95.43041,44.281882],[95.41378,44.298589],[95.238853,44.277169],[95.1286,44.269884],[94.998637,44.253169],[94.945666,44.292592],[94.826174,44.320001],[94.768275,44.34055],[94.722696,44.34055],[94.673421,44.397021],[94.606283,44.448311],[94.557008,44.462408],[94.470777,44.509373],[94.390705,44.521749],[94.359292,44.515775],[94.329727,44.582734],[94.279836,44.603617],[94.227481,44.645785],[94.215162,44.667921],[94.152336,44.684944],[94.066105,44.732154],[93.723642,44.865498],[93.716251,44.894334],[93.613389,44.926546],[93.509296,44.968055],[93.434767,44.955351],[93.376869,44.985412],[93.314659,44.995147],[93.314043,44.980333],[93.252449,44.991761],[93.174225,45.015458],[93.100312,45.007419],[93.062124,45.018419],[93.002377,45.009958],[92.932776,45.017573],[92.922921,45.03703],[92.884117,45.046756],[92.847777,45.038721],[92.779407,45.050561],[92.683937,45.02561],[92.547814,45.018419],[92.501003,45.001072],[92.414155,45.018419],[92.348866,45.014188],[92.315605,45.028994],[92.240461,45.015881],[92.100026,45.081417],[92.056911,45.086911],[91.885679,45.078882],[91.803144,45.082685],[91.694738,45.065357],[91.561695,45.075501],[91.500101,45.103809],[91.448978,45.156586],[91.429268,45.156586],[91.37753,45.11099],[91.33503,45.129571],[91.242023,45.13717],[91.230936,45.153632],[91.195827,45.159118],[91.17119,45.199616],[91.129922,45.21606],[91.050466,45.208892],[91.007966,45.218589],[90.96177,45.201303],[90.881698,45.192025],[90.866916,45.209314],[90.897713,45.249776],[90.877387,45.280946],[90.831807,45.300313],[90.804706,45.29484],[90.813329,45.32851],[90.773909,45.405874],[90.772677,45.432338],[90.723402,45.464667],[90.671047,45.487747],[90.676591,45.582488],[90.714779,45.728895],[90.799778,45.834905],[90.890937,45.921566],[91.028292,46.023054],[91.014741,46.06667],[91.021517,46.121038],[90.98456,46.160431],[90.94822,46.219262],[90.955611,46.233752],[90.900177,46.31235],[90.983328,46.374734],[90.996263,46.419309],[91.025828,46.444057],[91.038147,46.500936],[91.060937,46.516999],[91.079415,46.558989],[91.068328,46.579149],[91.017821,46.58244],[91.036299,46.670393],[91.054161,46.717598],[91.019053,46.766402],[90.992567,46.769682],[90.992567,46.790583],[90.942676,46.82581],[90.958075,46.879425],[90.929742,46.893331],[90.92235,46.938707],[90.901408,46.960768],[90.830575,46.995883],[90.767134,46.992617],[90.691989,47.080717],[90.653801,47.111681],[90.579888,47.198364],[90.56141,47.206903],[90.521374,47.2845],[90.488113,47.317374],[90.526301,47.379007],[90.507823,47.400076],[90.468403,47.404937],[90.459164,47.43895],[90.474562,47.462422],[90.468403,47.497611],[90.398186,47.547724],[90.376012,47.603036],[90.346447,47.637324],[90.384635,47.644179],[90.331665,47.681663],[90.216484,47.70543],[90.180144,47.72516],[90.13518,47.723147],[90.07605,47.777469],[90.070506,47.820483],[90.086521,47.86547],[90.066195,47.883534],[90.040941,47.874704],[89.960253,47.885942],[89.957789,47.842982],[89.86971,47.834144],[89.761921,47.835751],[89.735435,47.89758],[89.651052,47.913627],[89.645508,47.947711],[89.595617,47.973359],[89.599313,48.015811],[89.569132,48.037825],[89.498299,48.02822],[89.38127,48.046227],[89.359712,48.026219],[89.308589,48.021816],[89.282104,47.994189],[89.231597,47.98017],[89.156452,47.996992],[89.078228,47.98698],[89.044967,48.009806],[89.027105,48.051028],[88.953808,48.090618],[88.939026,48.115396],[88.824461,48.107005],[88.79736,48.133772],[88.721599,48.160526],[88.700657,48.180881],[88.668628,48.171303],[88.638447,48.183674],[88.601491,48.221567],[88.594716,48.259831],[88.575006,48.277757],[88.605803,48.337863],[88.573774,48.351785],[88.573158,48.369679],[88.535586,48.368884],[88.523267,48.403461],[88.503557,48.412996],[88.462289,48.392335],[88.438267,48.393528],[88.360659,48.433251],[88.363123,48.460641],[88.318159,48.478497],[88.229464,48.498329],[88.196819,48.493967],[88.151855,48.526478],[88.130297,48.521721],[88.10874,48.545895],[88.041602,48.548272],[87.973233,48.575997],[87.96153,48.599353],[88.010805,48.618742],[88.02682,48.65315],[88.089645,48.69504],[88.090877,48.71992],[88.064392,48.712813],[88.029283,48.750313],[87.96153,48.773588],[87.93874,48.757809],[87.872219,48.799612],[87.826639,48.800795],[87.803234,48.824835],[87.829103,48.825623],[87.792147,48.849258],[87.78106,48.872094],[87.742256,48.881146],[87.760118,48.925992],[87.793995,48.927565],[87.814321,48.945256],[87.87653,48.949186],[87.871603,48.963726],[87.911639,48.979833],[87.883922,48.993971],[87.883306,49.023806],[87.835263,49.054406],[87.858052,49.07362],[87.844502,49.090084],[87.867291,49.108892],[87.845733,49.146096],[87.82048,49.148445],[87.821096,49.173883],[87.793379,49.18249],[87.762582,49.172709],[87.700372,49.175839],[87.67635,49.15549],[87.602437,49.152359],[87.563017,49.142572],[87.517438,49.145704],[87.49588,49.132001],[87.511894,49.10184],[87.43675,49.075188],[87.388707,49.097921],[87.304939,49.112418],[87.239033,49.114376],[87.211932,49.140615],[87.112766,49.15549],[87.088128,49.133567],[87.000049,49.142572],[86.953853,49.131218],[86.887948,49.132001],[86.854071,49.109284],[86.84976,49.066563],[86.836209,49.051269],[86.772151,49.02773],[86.732115,48.994757],[86.730267,48.959797],[86.757985,48.894919],[86.782006,48.887049],[86.821426,48.850439],[86.818963,48.831139],[86.770303,48.810255],[86.754289,48.78463],[86.780774,48.731369],[86.771535,48.717156],[86.70255,48.666195],[86.693311,48.64366],[86.640956,48.629027],[86.635413,48.612016],[86.594761,48.576789],[86.579978,48.538763],[86.416138,48.481671],[86.38103,48.49357],[86.305269,48.491984],[86.270161,48.452307],[86.225813,48.432456],[86.053966,48.441192],[85.916612,48.438015],[85.791576,48.418954],[85.758315,48.403064],[85.695489,48.335078],[85.695489,48.302445],[85.678243,48.266205],[85.633895,48.232731],[85.622193,48.202824],[85.587084,48.191654],[85.576613,48.15853],[85.55136,48.127781],[85.551975,48.081423],[85.531649,48.046227],[85.547048,48.008205],[85.617881,47.550552],[85.614801,47.498015],[85.685018,47.428829],[85.701649,47.384275],[85.675779,47.321837],[85.701033,47.28856],[85.682555,47.249982],[85.682555,47.222757],[85.641903,47.18413],[85.582772,47.142626],[85.547048,47.096609],[85.545816,47.057891],[85.441106,47.063191],[85.355491,47.054629],[85.325926,47.044842],[85.276651,47.068898],[85.213825,47.041172],[85.175637,46.997924],[85.102956,46.968936],[85.082014,46.939933],[84.987159,46.918272],[84.979768,46.883106],[84.95513,46.861013],[84.934188,46.863878],[84.867051,46.927673],[84.849189,46.957092],[84.781435,46.979962],[84.748175,47.009759],[84.699515,47.008535],[84.668718,46.995067],[84.563393,46.991801],[84.506726,46.97302],[84.425422,47.008943],[84.37122,46.993434],[84.336727,47.00527],[84.2893,46.994658],[84.195061,47.003638],[84.150098,46.977512],[84.086656,46.965261],[84.038613,46.973428],[84.002888,46.990576],[83.951765,46.98731],[83.932671,46.970161],[83.88586,46.982003],[83.766367,47.026896],[83.69923,47.015472],[83.700462,47.032199],[83.576042,47.059114],[83.566803,47.080717],[83.53847,47.083977],[83.463325,47.132042],[83.418978,47.119012],[83.370318,47.178436],[83.324739,47.167858],[83.306261,47.179656],[83.257602,47.173147],[83.221877,47.186977],[83.207094,47.213814],[83.17445,47.218286],[83.15474,47.236168],[83.108544,47.221944],[83.02724,47.21544],[83.031552,47.168265],[82.993364,47.065229],[82.937929,47.014248],[82.923762,46.932169],[82.876335,46.823762],[82.878183,46.797138],[82.829524,46.772551],[82.788872,46.677784],[82.774089,46.600124],[82.726662,46.494756],[82.609017,46.294985],[82.518474,46.153798],[82.461808,45.97982],[82.401446,45.972333],[82.342932,45.935303],[82.336156,45.882418],[82.349707,45.822811],[82.340468,45.772742],[82.289961,45.71636],[82.288729,45.655321],[82.266555,45.620172],[82.281954,45.53891],[82.448257,45.461309],[82.546808,45.426038],[82.60101,45.346178],[82.58746,45.224069],[82.562822,45.204676],[82.487061,45.181058],[82.344779,45.219011],[82.294272,45.247669],[82.206809,45.236713],[82.109491,45.211422],[82.091012,45.222383],[82.09594,45.249776],[82.052824,45.255674],[81.993078,45.237978],[81.921013,45.233342],[81.879745,45.284314],[81.832318,45.319673],[81.78797,45.3836],[81.677101,45.35459],[81.645072,45.359216],[81.582863,45.336503],[81.575471,45.30789],[81.536667,45.304101],[81.52866,45.285999],[81.462754,45.264099],[81.437501,45.28263],[81.398697,45.275471],[81.382066,45.257781],[81.327864,45.260729],[81.284748,45.23882],[81.236705,45.247248],[81.175111,45.227863],[81.170183,45.211001],[81.111669,45.218168],[81.080872,45.182745],[81.024821,45.162916],[80.966307,45.168402],[80.93551,45.160384],[80.897938,45.127459],[80.862214,45.127037],[80.816634,45.152788],[80.731634,45.156164],[80.686055,45.129148],[80.599207,45.105921],[80.519135,45.108878],[80.493882,45.127037],[80.445839,45.097895],[80.443991,45.077614],[80.404571,45.049293],[80.358375,45.040836],[80.328194,45.070007],[80.291854,45.06578],[80.24381,45.031532],[80.195767,45.030686],[80.144644,45.059017],[80.136021,45.041259],[80.111999,45.052675],[80.060876,45.026033],[80.056565,45.011227],[79.98142,44.964244],[79.951855,44.957892],[79.944464,44.937985],[79.887798,44.90917],[79.969102,44.877797],[79.953703,44.849377],[79.991891,44.830281],[79.999283,44.793768],[80.087978,44.817122],[80.115695,44.815424],[80.169898,44.84471],[80.18776,44.825612],[80.178521,44.796741],[80.200695,44.756808],[80.238883,44.7228],[80.313412,44.704938],[80.400259,44.628751],[80.411962,44.605321],[80.350368,44.484615],[80.383013,44.401297],[80.399027,44.30587],[80.413194,44.264741],[80.400875,44.198704],[80.407034,44.149772],[80.3941,44.127009],[80.449534,44.078017],[80.458773,44.047054],[80.457541,43.981203],[80.485259,43.95579],[80.475404,43.938124],[80.511128,43.906657],[80.522215,43.816473],[80.75504,43.494329],[80.761199,43.446554],[80.746417,43.439167],[80.735946,43.389609],[80.686055,43.333916],[80.69283,43.32042],[80.777214,43.308227],[80.769207,43.265535],[80.788917,43.242433],[80.789533,43.201876],[80.804315,43.178314],[80.79446,43.137277],[80.752576,43.148194],[80.73225,43.131163],[80.706997,43.143828],[80.650946,43.147321],[80.593048,43.133347],[80.556092,43.104515],[80.482795,43.06955],[80.416889,43.05687],[80.378701,43.031502],[80.397795,42.996933],[80.487106,42.948766],[80.5912,42.923354],[80.602903,42.894424],[80.503737,42.882146],[80.450766,42.861971],[80.407034,42.834767],[80.338049,42.831695],[80.280151,42.838278],[80.262289,42.828623],[80.259209,42.790865],[80.225948,42.713083],[80.228412,42.692852],[80.179753,42.670415],[80.163738,42.629919],[80.180985,42.590718],[80.221637,42.533415],[80.265368,42.502097],[80.225948,42.485769],[80.206238,42.431462],[80.239499,42.389927],[80.229028,42.358536],[80.283847,42.320493],[80.272144,42.281984],[80.29247,42.259842],[80.28631,42.233261],[80.233339,42.210215],[80.168666,42.200462],[80.163738,42.152563],[80.139717,42.151232],[80.16805,42.096635],[80.193303,42.081535],[80.14218,42.03488],[80.089826,42.047325],[79.923522,42.042436],[79.852689,42.015319],[79.854537,41.984186],[79.822508,41.963275],[79.776313,41.89248],[79.724574,41.896935],[79.640806,41.884907],[79.616784,41.856385],[79.550879,41.834094],[79.500988,41.835432],[79.457256,41.847915],[79.415372,41.836769],[79.356242,41.795735],[79.326061,41.809565],[79.276786,41.78101],[79.271858,41.767174],[79.21704,41.725648],[79.138199,41.722968],[79.10925,41.697503],[79.043345,41.681414],[79.021787,41.657273],[78.99407,41.664427],[78.957729,41.65146],[78.891824,41.597777],[78.86657,41.593749],[78.825302,41.560173],[78.739071,41.555695],[78.696571,41.54181],[78.707042,41.522098],[78.675629,41.50238],[78.650375,41.467411],[78.580774,41.481759],[78.527188,41.440947],[78.454507,41.412228],[78.391681,41.408189],[78.385522,41.394721],[78.338094,41.397415],[78.324544,41.384395],[78.235232,41.399211],[78.163783,41.383497],[78.149617,41.368228],[78.165015,41.340825],[78.136682,41.279239],[78.129291,41.228398],[78.094798,41.224347],[77.972842,41.173013],[77.905089,41.185174],[77.836104,41.153189],[77.814546,41.13426],[77.807155,41.091876],[77.829328,41.059394],[77.796068,41.049014],[77.780669,41.022832],[77.737553,41.032313],[77.684583,41.00793],[77.654402,41.016059],[77.597119,41.005221],[77.591576,40.992122],[77.540453,41.006575],[77.476395,40.999349],[77.473931,41.022832],[77.415417,41.038633],[77.363062,41.04089],[77.296541,41.004769],[77.236795,41.027798],[77.169041,41.009285],[77.108063,41.038181],[77.091433,41.062553],[77.023064,41.059394],[77.002122,41.073381],[76.940528,41.028701],[76.885709,41.027347],[76.85368,40.97631],[76.817956,40.975406],[76.761905,40.954167],[76.741579,40.912119],[76.731724,40.818887],[76.693536,40.779472],[76.646725,40.759983],[76.646725,40.73686],[76.676906,40.696036],[76.654732,40.652917],[76.657196,40.620218],[76.611,40.601591],[76.601145,40.578868],[76.556798,40.542495],[76.543247,40.513837],[76.539551,40.464226],[76.508754,40.429613],[76.470566,40.422779],[76.442233,40.391336],[76.390494,40.37766],[76.381871,40.39088],[76.333212,40.343459],[76.327668,40.391336],[76.283321,40.415034],[76.279625,40.439179],[76.22419,40.401819],[76.176147,40.381307],[76.144118,40.393615],[76.081293,40.39635],[76.048648,40.388601],[76.048648,40.357141],[76.026474,40.355317],[75.986438,40.381763],[75.932235,40.339353],[75.921764,40.291439],[75.890351,40.30924],[75.84046,40.312434],[75.831221,40.327492],[75.785642,40.301025],[75.739446,40.299199],[75.709265,40.280939],[75.688323,40.343915],[75.669845,40.363982],[75.686475,40.418223],[75.717272,40.443278],[75.733287,40.474242],[75.646439,40.516567],[75.631041,40.548862],[75.627345,40.605226],[75.636584,40.624306],[75.599628,40.659727],[75.550353,40.64883],[75.467817,40.599773],[75.432093,40.563412],[75.355716,40.537947],[75.292274,40.483802],[75.268869,40.483802],[75.242383,40.448743],[75.206659,40.447833],[75.13521,40.463315],[75.102565,40.44009],[75.051442,40.449654],[75.021877,40.466958],[74.995392,40.455119],[74.963363,40.464681],[74.891914,40.507467],[74.844486,40.521117],[74.819233,40.505647],[74.814921,40.461039],[74.795211,40.443278],[74.908544,40.338897],[74.862965,40.32658],[74.824776,40.344371],[74.700357,40.346195],[74.697893,40.310153],[74.673255,40.278656],[74.618437,40.27957],[74.577169,40.260391],[74.534669,40.207851],[74.485394,40.182251],[74.433039,40.13148],[74.356662,40.089371],[74.316626,40.106767],[74.280902,40.09807],[74.26304,40.125074],[74.126301,40.104479],[74.113366,40.086624],[74.023439,40.085251],[74.008041,40.050901],[73.943367,40.016076],[73.980324,40.004617],[73.910722,39.934443],[73.907027,39.873843],[73.845433,39.831115],[73.841737,39.756163],[73.905795,39.741899],[73.924273,39.722108],[73.953838,39.600018],[73.916266,39.586644],[73.914418,39.564041],[73.883621,39.540969],[73.893476,39.528046],[73.868223,39.482794],[73.836194,39.472169],[73.745651,39.462005],[73.6471,39.474479],[73.61076,39.465702],[73.592898,39.412087],[73.502355,39.383877],[73.554094,39.350102],[73.554709,39.295935],[73.542391,39.269531],[73.564564,39.266288],[73.580579,39.237555],[73.623079,39.235237],[73.639709,39.220402],[73.657571,39.166136],[73.688368,39.154999],[73.719781,39.108112],[73.720397,39.071881],[73.743187,39.029588],[73.780143,39.026798],[73.820179,39.041674],[73.839889,39.008199],[73.846665,38.962145],[73.826339,38.916993],[73.767824,38.941202],[73.742571,38.933754],[73.70931,38.893241],[73.699455,38.857832],[73.729636,38.837324],[73.769056,38.775765],[73.757353,38.719755],[73.809092,38.634256],[73.799237,38.610878],[73.852208,38.584217],[73.89902,38.579071],[73.926121,38.536016],[74.011736,38.52478],[74.034526,38.541634],[74.090577,38.542102],[74.068403,38.585621],[74.088113,38.610878],[74.11275,38.611345],[74.147859,38.676785],[74.229779,38.656224],[74.353583,38.655757],[74.421952,38.647812],[74.455829,38.632853],[74.506336,38.637528],[74.546988,38.607604],[74.613509,38.593105],[74.639995,38.599653],[74.717603,38.542102],[74.78474,38.538357],[74.821697,38.491062],[74.862965,38.484035],[74.868508,38.403883],[74.834015,38.361193],[74.789668,38.324581],[74.806914,38.285602],[74.793363,38.271039],[74.816769,38.215576],[74.80445,38.167128],[74.821697,38.10311],[74.879595,38.021122],[74.92579,38.01735],[74.911008,37.966884],[74.919015,37.908357],[74.936877,37.876241],[74.917167,37.845057],[74.989848,37.797783],[75.006478,37.770823],[74.949196,37.725395],[74.923327,37.717347],[74.920863,37.684675],[74.891914,37.668097],[74.940573,37.559061],[75.000935,37.53059],[75.002167,37.511604],[75.035428,37.500685],[75.078543,37.511129],[75.090862,37.486915],[75.129666,37.459367],[75.153072,37.414223],[75.125971,37.388075],[75.140137,37.355258],[75.125971,37.322427],[75.078543,37.318144],[75.018181,37.293867],[74.927022,37.277678],[74.911008,37.233378],[74.816153,37.216699],[74.800139,37.248147],[74.753943,37.281011],[74.727458,37.282916],[74.665864,37.23576],[74.642458,37.261485],[74.598727,37.258151],[74.578401,37.231472],[74.54514,37.2491],[74.511263,37.240048],[74.477387,37.19954],[74.487858,37.161871],[74.465068,37.147085],[74.496481,37.116072],[74.498944,37.072155],[74.530357,37.082182],[74.56793,37.032512],[74.617205,37.043499],[74.632603,37.066425],[74.70898,37.084569],[74.739161,37.028212],[74.792747,37.027257],[74.806914,37.054485],[74.84695,37.056873],[74.84387,37.0134],[74.86974,36.990458],[74.893762,36.939772],[74.938725,36.94312],[74.927638,36.978029],[75.005862,36.99476],[75.032348,37.016745],[75.063145,37.006231],[75.172166,37.013877],[75.16847,36.991892],[75.244847,36.963207],[75.288579,36.974682],[75.345861,36.960816],[75.413614,36.954599],[75.396368,36.904367],[75.430245,36.873255],[75.434556,36.83303],[75.425933,36.778883],[75.458578,36.720861],[75.504773,36.743404],[75.536802,36.729975],[75.537418,36.773131],[75.588541,36.762584],[75.634121,36.771693],[75.724048,36.750597],[75.8072,36.707908],[75.871257,36.666636],[75.947018,36.590752],[75.924228,36.566242],[75.991981,36.505654],[76.035097,36.409386],[75.991365,36.35205],[75.998757,36.312034],[76.055423,36.252695],[76.060967,36.225182],[76.011691,36.229044],[76.016619,36.165294],[75.96796,36.159013],[75.936547,36.13485],[75.949482,36.070056],[75.982742,36.031347],[76.028322,36.016827],[76.044336,36.026991],[76.097307,36.022635],[76.117017,35.975186],[76.16506,35.908807],[76.146582,35.839946],[76.160133,35.82442],[76.221727,35.823449],[76.228502,35.837035],[76.298719,35.841401],[76.365857,35.82442],[76.369552,35.86323],[76.431762,35.851589],[76.471798,35.886021],[76.51553,35.881173],[76.55803,35.923347],[76.59745,35.895718],[76.579587,35.866625],[76.587595,35.840431],[76.566037,35.819082],[76.593754,35.771996],[76.69292,35.747714],[76.769297,35.653917],[76.848753,35.668018],[76.906651,35.615005],[76.967013,35.591649],[76.99781,35.611113],[77.072339,35.591162],[77.093281,35.569746],[77.195527,35.519103],[77.307628,35.540533],[77.331649,35.530793],[77.355055,35.494257],[77.396939,35.467942],[77.451758,35.46063],[77.518895,35.482075],[77.578025,35.47574],[77.590344,35.460143],[77.639619,35.45478],[77.657481,35.477689],[77.690742,35.448443],[77.735706,35.461605],[77.757879,35.497181],[77.797299,35.491334],[77.816394,35.518616],[77.85643,35.487436],[77.870596,35.495232],[77.914944,35.465017],[77.917408,35.490847],[77.951284,35.478664],[78.009799,35.491821],[78.029509,35.469404],[78.048603,35.491334],[78.140378,35.494745],[78.113892,35.466967],[78.107117,35.437229],[78.046755,35.384063],[78.013494,35.366008],[78.020885,35.315237],[78.01719,35.228267],[78.060306,35.180344],[78.062769,35.114772],[78.078784,35.100084],[78.124979,35.108407],[78.150849,35.069721],[78.123131,35.036897],[78.160704,34.990823],[78.201972,34.974642],[78.182262,34.936874],[78.206283,34.891726],[78.237696,34.882398],[78.230921,34.776288],[78.21429,34.760556],[78.213059,34.717771],[78.267261,34.705472],[78.265413,34.651335],[78.280812,34.623269],[78.346101,34.60406],[78.397224,34.605538],[78.427405,34.594207],[78.436029,34.543942],[78.492695,34.578441],[78.542586,34.574499],[78.559832,34.55725],[78.562912,34.51288],[78.58139,34.505483],[78.634977,34.538026],[78.708274,34.522249],[78.715049,34.502031],[78.758781,34.481807],[78.742766,34.45467],[78.809288,34.432955],[78.878273,34.391481],[78.899831,34.354929],[78.958961,34.386049],[78.973128,34.362833],[79.039649,34.33467],[79.048888,34.348506],[79.0107,34.399877],[79.039033,34.421601],[79.072294,34.412714],[79.161605,34.441345],[79.179467,34.422588],[79.241677,34.415183],[79.274322,34.435916],[79.326677,34.44332],[79.363017,34.428018],[79.435082,34.447761],[79.504683,34.45467],[79.545335,34.476381],[79.58106,34.456151],[79.675914,34.451216],[79.699936,34.477861],[79.735661,34.471447],[79.801566,34.478847],[79.861312,34.528166],[79.84345,34.55725],[79.88595,34.642965],[79.866856,34.671517],[79.906892,34.683821],[79.898268,34.732035],[79.947544,34.821008],[79.926602,34.849499],[79.961094,34.862759],[79.996819,34.856375],[80.003594,34.895162],[80.034391,34.902033],[80.041782,34.943252],[80.02392,34.971209],[80.04363,35.022196],[80.031311,35.034447],[80.078123,35.076578],[80.118159,35.066293],[80.23026,35.147565],[80.223484,35.177409],[80.257977,35.203331],[80.362687,35.20871],[80.267832,35.295701],[80.286926,35.35283],[80.321419,35.38699],[80.375006,35.387966],[80.432904,35.449418],[80.444607,35.417235],[80.514824,35.391869],[80.532686,35.404553],[80.56841,35.391381],[80.599823,35.409431],[80.65649,35.393821],[80.690982,35.364544],[80.689135,35.339162],[80.759968,35.334768],[80.844351,35.345508],[80.894242,35.324027],[80.924423,35.330862],[80.963844,35.310842],[81.026053,35.31133],[81.002648,35.334768],[81.030981,35.337209],[81.031597,35.380648],[81.054387,35.402602],[81.09935,35.40748],[81.103662,35.386015],[81.142466,35.365032],[81.191741,35.36552],[81.219458,35.319144],[81.26627,35.322562],[81.285364,35.345508],[81.314313,35.337209],[81.363588,35.354783],[81.385762,35.335256],[81.441196,35.333303],[81.447972,35.318167],[81.504638,35.279092],[81.513261,35.23511],[81.68634,35.235599],[81.736847,35.26248],[81.804601,35.270786],[81.853876,35.25857],[81.927789,35.271275],[81.955506,35.307423],[81.99123,35.30547],[82.030034,35.321585],[82.05344,35.35039],[82.029419,35.426013],[82.034346,35.451855],[82.071302,35.450393],[82.086701,35.467454],[82.164925,35.495719],[82.189563,35.513258],[82.234526,35.520565],[82.263475,35.547837],[82.2992,35.544916],[82.328149,35.559523],[82.350323,35.611113],[82.336156,35.651486],[82.392823,35.656349],[82.424852,35.712736],[82.468583,35.717595],[82.501844,35.701073],[82.546192,35.708362],[82.628727,35.692324],[82.652133,35.67288],[82.731589,35.637868],[82.780249,35.666073],[82.795031,35.688436],[82.873871,35.688922],[82.894813,35.673852],[82.967494,35.667532],[82.956407,35.636409],[82.981661,35.599922],[82.971806,35.548324],[82.998907,35.484512],[83.067892,35.46258],[83.088834,35.425526],[83.127022,35.398699],[83.178145,35.38943],[83.251442,35.417722],[83.280391,35.401138],[83.333978,35.397236],[83.405427,35.380648],[83.449159,35.382111],[83.502745,35.360639],[83.540318,35.364056],[83.54155,35.341603],[83.599448,35.351366],[83.622238,35.335256],[83.677672,35.361128],[83.785462,35.36308],[83.79778,35.354783],[83.885244,35.367472],[83.906186,35.40309],[84.005968,35.422599],[84.077417,35.400163],[84.095895,35.362592],[84.140859,35.379184],[84.160569,35.359663],[84.200605,35.381135],[84.274517,35.404065],[84.333032,35.413821],[84.424191,35.466479],[84.45314,35.473303],[84.475929,35.516181],[84.448828,35.550272],[84.513502,35.564391],[84.570168,35.588242],[84.628067,35.595055],[84.704443,35.616951],[84.729081,35.613546],[84.798066,35.647595],[84.920022,35.696213],[84.973608,35.709334],[84.99455,35.737028],[85.053065,35.752086],[85.146071,35.742371],[85.271107,35.788989],[85.341324,35.753543],[85.373969,35.700101],[85.518715,35.680658],[85.566142,35.6403],[85.612953,35.651486],[85.65299,35.731199],[85.691178,35.751114],[85.811286,35.778794],[85.835308,35.771996],[85.903677,35.78462],[85.949256,35.778794],[86.035488,35.846738],[86.05335,35.842857],[86.090306,35.876809],[86.093386,35.906868],[86.129111,35.941761],[86.150668,36.00424],[86.173458,36.008113],[86.199944,36.047801],[86.182081,36.064734],[86.187625,36.130983],[86.248603,36.141616],[86.2794,36.170608],[86.35824,36.168676],[86.392733,36.206834],[86.454943,36.221319],[86.515305,36.205385],[86.531935,36.227113],[86.599072,36.222285],[86.69947,36.24449],[86.746282,36.291777],[86.836209,36.291294],[86.86331,36.299977],[86.887332,36.262829],[86.931064,36.265242],[86.943998,36.284058],[86.996353,36.308658],[87.051788,36.2966],[87.08628,36.310587],[87.149106,36.297565],[87.161425,36.325535],[87.193454,36.349158],[87.292004,36.358797],[87.348055,36.393008],[87.363453,36.420463],[87.386859,36.412757],[87.426895,36.42576],[87.460155,36.409868],[87.470626,36.354459],[87.570409,36.342409],[87.6203,36.360243],[87.731785,36.384818],[87.767509,36.3747],[87.826023,36.391563],[87.838342,36.383855],[87.919646,36.39349],[87.95845,36.408423],[87.983088,36.437797],[88.006494,36.430575],[88.092109,36.43539],[88.134609,36.427205],[88.182652,36.452721],[88.222688,36.447426],[88.241782,36.468605],[88.282434,36.470049],[88.366202,36.458016],[88.356963,36.477268],[88.41055,36.473418],[88.470912,36.48208],[88.498629,36.446463],[88.573158,36.461386],[88.618121,36.428168],[88.623665,36.389636],[88.690186,36.367954],[88.766563,36.292259],[88.783809,36.291777],[88.802903,36.33807],[88.838628,36.353496],[88.870657,36.348193],[88.926091,36.36458],[88.964279,36.318785],[89.013554,36.315409],[89.054822,36.291777],[89.10225,36.281164],[89.126887,36.254626],[89.198952,36.260417],[89.232213,36.295636],[89.292575,36.231457],[89.335075,36.23725],[89.375727,36.228078],[89.490291,36.151281],[89.594385,36.126632],[89.614711,36.109712],[89.711414,36.093272],[89.766848,36.073925],[89.819819,36.080697],[89.914058,36.079246],[89.941159,36.067637],[89.944855,36.140649],[89.997825,36.168193],[90.019999,36.213594],[90.028006,36.258486],[90.003369,36.278752],[90.043405,36.276822],[90.058188,36.255591],[90.145651,36.239181],[90.130252,36.2078],[90.198006,36.187516],[90.23681,36.160462],[90.325505,36.159496],[90.424055,36.133883],[90.478258,36.13195],[90.534925,36.147899],[90.613149,36.126632],[90.659344,36.13485],[90.776373,36.086501],[90.815793,36.035703],[90.850285,36.016827],[90.922966,36.028927],[90.979017,36.106811],[91.081263,36.088436],[91.124994,36.115514],[91.09235,36.163844],[91.096045,36.219871],[91.051698,36.238215],[91.07264,36.299012],[91.026444,36.323607],[91.051698,36.433946],[91.028292,36.443093],[91.039995,36.474861],[91.035683,36.529703],[91.011662,36.539801],[90.905104,36.560474],[90.831191,36.55807],[90.810865,36.585466],[90.741264,36.585947],[90.72217,36.620058],[90.730793,36.655594],[90.706156,36.658955],[90.720938,36.708868],[90.754815,36.721341],[90.727098,36.755872],[90.732025,36.825844],[90.758511,36.825844],[90.853981,36.915373],[90.924198,36.921115],[90.983944,36.913459],[91.036915,36.929727],[91.051698,36.96751],[91.126842,36.978507],[91.133618,37.007665],[91.181045,37.025345],[91.216153,37.010054],[91.303617,37.012444],[91.291298,37.042544],[91.303617,37.083136],[91.286371,37.105095],[91.280211,37.163779],[91.1909,37.205737],[91.194596,37.273868],[91.134849,37.324331],[91.136081,37.355734],[91.113292,37.387124],[91.099741,37.447965],[91.073256,37.475992],[91.019669,37.493088],[90.958075,37.477891],[90.911879,37.519674],[90.865684,37.53059],[90.882314,37.575664],[90.854597,37.604117],[90.820104,37.613599],[90.777605,37.648672],[90.643946,37.696988],[90.586663,37.703144],[90.579272,37.720661],[90.519526,37.730601],[90.516446,38.207111],[90.531229,38.319886],[90.401882,38.311434],[90.361846,38.300163],[90.352607,38.233441],[90.280542,38.238142],[90.137644,38.340543],[90.179528,38.396848],[90.129636,38.400131],[90.111774,38.418889],[90.111774,38.477945],[90.130868,38.494341],[90.248513,38.491531],[90.315034,38.501835],[90.353222,38.482162],[90.427135,38.493873],[90.465323,38.521971],[90.463476,38.556611],[90.525685,38.561291],[90.560794,38.593573],[90.608837,38.594508],[90.606374,38.610878],[90.645794,38.635191],[90.619308,38.664636],[90.65996,38.674449],[90.724634,38.658094],[90.899561,38.679588],[90.970394,38.697806],[90.992567,38.695003],[91.188436,38.73096],[91.242639,38.752433],[91.298689,38.746365],[91.446515,38.813546],[91.501333,38.815411],[91.681188,38.852706],[91.694738,38.86622],[91.806223,38.872744],[91.87952,38.884391],[91.880752,38.899297],[91.966368,38.930961],[92.10865,38.963541],[92.173323,38.960749],[92.197961,38.983548],[92.263866,39.002153],[92.380279,38.999828],[92.416003,39.010524],[92.41046,39.03842],[92.366728,39.059335],[92.366112,39.096037],[92.343938,39.146181],[92.339011,39.236628],[92.378431,39.258411],[92.52564,39.368611],[92.639589,39.514196],[92.687632,39.657174],[92.745531,39.868331],[92.796654,40.153897],[92.906907,40.310609],[92.920458,40.391792],[92.928465,40.572504],[93.506216,40.648376],[93.760599,40.664721],[93.820961,40.793519],[93.809874,40.879548],[93.908424,40.983539],[94.01067,41.114875],[94.184365,41.268444],[94.534219,41.505966],[94.750413,41.538227],[94.809543,41.619256],[94.861898,41.668451],[94.969072,41.718948],[95.011572,41.726541],[95.110738,41.768513],[95.135991,41.772976],[95.16494,41.735474],[95.199433,41.719395],[95.194505,41.694821],[95.247476,41.61344],[95.299831,41.565994],[95.335556,41.644305],[95.39407,41.693481],[95.445193,41.719841],[95.57146,41.796181],[95.65646,41.826067],[95.759322,41.835878],[95.801206,41.848361],[95.855408,41.849699],[95.998306,41.906289],[96.054973,41.936124],[96.117183,41.985966],[96.137509,42.019765],[96.13874,42.05399],[96.077147,42.149457],[96.178161,42.21775],[96.040806,42.326688],[96.042038,42.352787],[96.06606,42.414674],[95.978596,42.436762],[96.0174,42.482239],[96.02356,42.542675],[96.072219,42.569566],[96.103632,42.604375],[96.166458,42.623314],[96.386348,42.727592]]]]}},
    {"type":"Feature","properties":{"adcode":710000,"name":"台湾省","center":[121.509062,25.044332],"centroid":[120.971485,23.749452],"childrenNum":0,"level":"province","parent":{"adcode":100000},"subFeatureIndex":31,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[120.443706,22.441432],[120.517619,22.408793],[120.569973,22.361757],[120.640806,22.241605],[120.659285,22.154056],[120.661748,22.067007],[120.651277,22.033171],[120.667908,21.983235],[120.701784,21.927174],[120.743052,21.915515],[120.781857,21.923843],[120.854537,21.883309],[120.873016,21.897191],[120.86624,21.984345],[120.907508,22.033171],[120.912436,22.086418],[120.903197,22.12634],[120.914899,22.302525],[120.981421,22.528248],[121.014682,22.584069],[121.03316,22.650914],[121.078739,22.669691],[121.170514,22.723247],[121.21055,22.770711],[121.237652,22.836362],[121.276456,22.877171],[121.324499,22.945526],[121.35468,23.00999],[121.370695,23.084334],[121.409499,23.1025],[121.430441,23.137175],[121.415042,23.196047],[121.440296,23.271937],[121.479716,23.322507],[121.497578,23.419744],[121.5216,23.483431],[121.522832,23.538858],[121.587505,23.760878],[121.621382,23.920718],[121.65957,24.007125],[121.63986,24.064514],[121.643556,24.097843],[121.678048,24.133895],[121.689135,24.174303],[121.809243,24.339083],[121.82649,24.423572],[121.867758,24.47914],[121.88562,24.529784],[121.892395,24.617953],[121.86283,24.671261],[121.841272,24.734329],[121.844968,24.836476],[121.933047,24.938539],[122.012503,25.001471],[121.98109,25.030757],[121.947214,25.031841],[121.917033,25.138076],[121.841888,25.135367],[121.782142,25.160287],[121.745186,25.161912],[121.707613,25.191701],[121.700222,25.226896],[121.655259,25.242054],[121.62323,25.29455],[121.585041,25.309159],[121.53515,25.307535],[121.444607,25.27074],[121.413194,25.238806],[121.371926,25.159746],[121.319572,25.140785],[121.209318,25.12724],[121.132942,25.078466],[121.102145,25.075214],[121.024537,25.040517],[121.009754,24.993878],[120.961095,24.940167],[120.914899,24.864715],[120.89211,24.767482],[120.82374,24.688118],[120.762147,24.658208],[120.68885,24.600542],[120.642654,24.490033],[120.589068,24.43229],[120.546568,24.370159],[120.520698,24.311816],[120.470807,24.242533],[120.451713,24.182493],[120.391967,24.118055],[120.316206,23.984708],[120.278018,23.92783],[120.245989,23.840276],[120.175156,23.807427],[120.102476,23.701162],[120.095084,23.58768],[120.12157,23.504836],[120.108019,23.341191],[120.081534,23.291728],[120.018708,23.073322],[120.029795,23.048544],[120.133272,23.000625],[120.149287,22.896468],[120.20041,22.721039],[120.274323,22.560307],[120.297112,22.531565],[120.443706,22.441432]]],[[[124.542782,25.903886],[124.584666,25.908731],[124.566804,25.941563],[124.542782,25.903886]]],[[[123.445178,25.726102],[123.469816,25.712623],[123.50862,25.722867],[123.512316,25.755212],[123.479055,25.768687],[123.445794,25.749822],[123.445178,25.726102]]],[[[119.646064,23.550928],[119.691028,23.547087],[119.678093,23.600294],[119.61034,23.604132],[119.601717,23.575613],[119.566608,23.584937],[119.562297,23.530627],[119.578927,23.502641],[119.609108,23.503738],[119.646064,23.550928]]],[[[123.666916,25.914114],[123.706952,25.91519],[123.689706,25.939949],[123.666916,25.914114]]],[[[119.506246,23.625518],[119.506246,23.577259],[119.47237,23.556962],[119.519181,23.559705],[119.52534,23.62497],[119.506246,23.625518]]],[[[119.497623,23.38679],[119.495159,23.349982],[119.516717,23.349982],[119.497623,23.38679]]],[[[119.557369,23.666634],[119.608492,23.620035],[119.615268,23.661153],[119.586318,23.675952],[119.557369,23.666634]]],[[[122.066706,25.6247],[122.087032,25.61067],[122.092575,25.639268],[122.066706,25.6247]]],[[[121.468013,22.67687],[121.474788,22.643734],[121.513592,22.631582],[121.514824,22.676318],[121.468013,22.67687]]],[[[121.510513,22.086972],[121.507433,22.048704],[121.533918,22.022076],[121.594281,21.995443],[121.604752,22.022631],[121.575186,22.037055],[121.575802,22.0842],[121.510513,22.086972]]],[[[122.097503,25.499987],[122.110438,25.465952],[122.122141,25.495666],[122.097503,25.499987]]],[[[119.421247,23.216949],[119.436029,23.186146],[119.453275,23.216399],[119.421247,23.216949]]],[[[120.355011,22.327439],[120.395663,22.342385],[120.383344,22.355669],[120.355011,22.327439]]]]}},
    {"type":"Feature","properties":{"adcode":810000,"name":"香港特别行政区","center":[114.173355,22.320048],"centroid":[114.134357,22.377366],"childrenNum":18,"level":"province","parent":{"adcode":100000},"subFeatureIndex":32,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[114.031778,22.503923],[114.000981,22.491206],[113.977575,22.45692],[113.918445,22.418199],[113.920293,22.367845],[113.951706,22.355116],[113.956633,22.359543],[113.980039,22.366185],[114.026234,22.34792],[113.955401,22.298649],[113.969568,22.321349],[113.898119,22.308615],[113.889496,22.271514],[113.8433,22.229418],[113.84946,22.191188],[113.899351,22.215568],[113.935691,22.205041],[113.981271,22.229972],[113.996669,22.206149],[114.026234,22.229418],[114.004676,22.239389],[114.02993,22.263207],[114.034857,22.300864],[114.069966,22.326885],[114.121089,22.320795],[114.145726,22.300864],[114.120473,22.272068],[114.164821,22.226648],[114.200545,22.232188],[114.203009,22.206703],[114.265835,22.200608],[114.248588,22.274837],[114.262139,22.294773],[114.284929,22.263761],[114.313262,22.264315],[114.315726,22.299203],[114.315726,22.299756],[114.278153,22.328546],[114.283081,22.386661],[114.322501,22.385554],[114.323117,22.385554],[114.323733,22.385001],[114.323733,22.384447],[114.356994,22.340171],[114.394566,22.361757],[114.385327,22.41156],[114.406269,22.432582],[114.406269,22.433688],[114.376088,22.436454],[114.325581,22.479041],[114.278769,22.435901],[114.220255,22.427603],[114.205473,22.449729],[114.23319,22.466875],[114.2529,22.445304],[114.340979,22.50337],[114.309566,22.497288],[114.28924,22.52272],[114.263987,22.541515],[114.263371,22.541515],[114.260291,22.547595],[114.232574,22.528801],[114.232574,22.539857],[114.222719,22.553122],[114.166052,22.559201],[114.156813,22.543726],[114.095219,22.534329],[114.082285,22.512216],[114.031778,22.503923]]],[[[114.142647,22.213906],[114.123553,22.238836],[114.120473,22.177888],[114.154965,22.177888],[114.166668,22.205041],[114.142647,22.213906]]],[[[114.305871,22.372273],[114.313878,22.340724],[114.332972,22.353455],[114.305255,22.372826],[114.305871,22.372273]]],[[[114.320037,22.381127],[114.323733,22.384447],[114.323733,22.385001],[114.323117,22.385554],[114.322501,22.385554],[114.319421,22.382234],[114.320037,22.38168],[114.320037,22.381127]]],[[[114.305871,22.369506],[114.305871,22.372273],[114.305255,22.372826],[114.305871,22.369506]]],[[[114.315726,22.299203],[114.316958,22.298649],[114.316342,22.30031],[114.315726,22.299756],[114.315726,22.299203]]],[[[114.319421,22.382234],[114.320037,22.381127],[114.320037,22.38168],[114.319421,22.382234]]],[[[114.372392,22.32301],[114.373008,22.323564],[114.372392,22.323564],[114.372392,22.32301]]],[[[114.323733,22.297541],[114.324349,22.297541],[114.323733,22.298095],[114.323733,22.297541]]]]}},
    {"type":"Feature","properties":{"adcode":820000,"name":"澳门特别行政区","center":[113.54909,22.198951],"centroid":[113.566988,22.159307],"childrenNum":8,"level":"province","parent":{"adcode":100000},"subFeatureIndex":33,"acroutes":[100000]},"geometry":{"type":"MultiPolygon","coordinates":[[[[113.554425,22.107489],[113.6037,22.132438],[113.575983,22.194513],[113.558736,22.212244],[113.53841,22.209473],[113.534715,22.174009],[113.554425,22.142416],[113.554425,22.107489]]],[[[113.586453,22.201162],[113.575983,22.201162],[113.575983,22.194513],[113.586453,22.201162]]]]}},
    {"type":"Feature","properties":{"name":"","adchar":"JD","adcode":"100000_JD"},"geometry":{"type":"MultiPolygon","coordinates":[[[[122.51865306,23.46078502],[122.79861399,24.57367379],[122.79889322,24.57678999],[122.79819583,24.57983997],[122.79659008,24.58252516],[122.79423315,24.58458272],[122.79135575,24.58581125],[122.78823955,24.58609049],[122.78518957,24.5853931],[122.78250438,24.58378734],[122.78044682,24.58143041],[122.77921829,24.57855302],[122.49925737,23.46566424],[122.49897813,23.46254804],[122.49967552,23.45949807],[122.50128127,23.45681287],[122.5036382,23.45475531],[122.5065156,23.45352678],[122.50963181,23.45324755],[122.51268178,23.45394494],[122.51536697,23.45555069],[122.51742454,23.45790762],[122.51865306,23.46078502]]],[[[121.17202617,20.8054593],[121.90938804,21.68743347],[121.9109946,21.69011818],[121.91169291,21.69316794],[121.91141462,21.69628423],[121.91018696,21.699162],[121.9081301,21.70151955],[121.9054454,21.70312611],[121.90239563,21.70382443],[121.89927934,21.70354613],[121.89640158,21.70231847],[121.89404403,21.70026162],[121.15668216,20.81828744],[121.1550756,20.81560273],[121.15437729,20.81255297],[121.15465558,20.80943668],[121.15588324,20.80655891],[121.1579401,20.80420136],[121.1606248,20.8025948],[121.16367457,20.80189649],[121.16679085,20.80217478],[121.16966862,20.80340244],[121.17202617,20.8054593]]],[[[119.47366172,18.00707291],[120.02569734,19.02403788],[120.02674143,19.02698721],[120.02682302,19.03011484],[120.02593412,19.0331146],[120.02416175,19.03569286],[120.02167941,19.03759723],[120.01873007,19.03864132],[120.01560245,19.03872291],[120.01260269,19.03783401],[120.01002443,19.03606165],[120.00812005,19.0335793],[119.45608443,18.01661433],[119.45504035,18.01366499],[119.45495876,18.01053737],[119.45584765,18.00753761],[119.45762002,18.00495935],[119.46010237,18.00305497],[119.4630517,18.00201089],[119.46617933,18.0019293],[119.46917909,18.0028182],[119.47175735,18.00459056],[119.47366172,18.00707291]]],[[[119.0726757,15.04098494],[119.0726757,16.04388528],[119.07218626,16.04697545],[119.07076587,16.04976313],[119.06855355,16.05197545],[119.06576587,16.05339584],[119.0626757,16.05388528],[119.05958553,16.05339584],[119.05679784,16.05197545],[119.05458553,16.04976313],[119.05316513,16.04697545],[119.0526757,16.04388528],[119.0526757,15.04105889],[119.0521839,15.00781004],[119.05262758,15.00471297],[119.05400659,15.00190458],[119.05618595,14.99965979],[119.05895232,14.99819832],[119.06203491,14.99766324],[119.06513198,14.99810691],[119.06794036,14.99948592],[119.07018516,15.00166528],[119.07164663,15.00443165],[119.07218171,15.00751424],[119.0726746,15.04083704],[119.0726757,15.04098494]]],[[[118.68646749,11.18959191],[118.52518702,10.91547751],[118.52404181,10.91256595],[118.52385237,10.909443],[118.52463726,10.90641436],[118.52631962,10.9037765],[118.5287348,10.90178762],[118.53164636,10.90064241],[118.53476931,10.90045298],[118.53779795,10.90123786],[118.54043581,10.90292022],[118.54242469,10.9053354],[118.70409227,11.18010771],[118.70476212,11.18147468],[118.87431591,11.606662],[118.87459939,11.60747236],[118.98894963,11.98573108],[118.98937534,11.98883067],[118.9888224,11.99191011],[118.98734492,11.99466796],[118.98508753,11.99683427],[118.98227119,11.99819697],[118.9791716,11.99862269],[118.97609216,11.99806975],[118.97333431,11.99659227],[118.97116801,11.99433487],[118.9698053,11.99151854],[118.85557939,11.6136711],[118.68646749,11.18959191]]],[[[115.54466883,7.14672265],[116.2504858,7.979279],[116.25211077,7.98195261],[116.25283001,7.9849975],[116.25257312,7.98811563],[116.25136525,7.99100176],[116.24932463,7.99337338],[116.24665102,7.99499834],[116.24360613,7.99571758],[116.240488,7.99546069],[116.23760187,7.99425282],[116.23523025,7.99221221],[115.52941328,7.15965587],[115.52778832,7.15698226],[115.52706908,7.15393736],[115.52732596,7.15081924],[115.52853383,7.1479331],[115.53057445,7.14556148],[115.53324806,7.14393652],[115.53629295,7.14321728],[115.53941108,7.14347417],[115.54229721,7.14468204],[115.54466883,7.14672265]]],[[[112.30705249,3.53487257],[111.78690114,3.41687263],[111.78399583,3.41571167],[111.78159146,3.41370973],[111.77992341,3.41106279],[111.77915495,3.40802995],[111.77936129,3.40490807],[111.78052226,3.40200275],[111.78252419,3.39959839],[111.78517113,3.39793033],[111.78820398,3.39716187],[111.79132585,3.39736822],[112.31181658,3.51544515],[112.31248917,3.51562254],[112.52147408,3.5785908],[112.52281386,3.57910186],[112.85206367,3.73256867],[112.85465776,3.7343178],[112.85658437,3.73678292],[112.85765492,3.73972276],[112.85776462,3.74284952],[112.85690272,3.74585715],[112.8551536,3.74845124],[112.85268847,3.75037785],[112.84974864,3.7514484],[112.84662187,3.75155809],[112.84361424,3.7506962],[112.51501594,3.59753306],[112.30705249,3.53487257]]],[[[108.26055972,6.08912451],[108.29013305,6.01266273],[108.29170425,6.00995718],[108.29403462,6.00786957],[108.29689603,6.00660426],[108.3000084,6.00628511],[108.30306706,6.00694335],[108.30577262,6.00851455],[108.30786022,6.01084492],[108.30912553,6.01370633],[108.30944469,6.0168187],[108.30878645,6.01987736],[108.279563,6.09543449],[108.25611734,6.22752625],[108.21679964,6.53816468],[108.21876335,6.94964057],[108.24419535,7.07390742],[108.24433543,7.07703297],[108.24350281,7.08004883],[108.24177899,7.0826598],[108.2393327,7.08461028],[108.23640341,7.08570936],[108.23327786,7.08584944],[108.230262,7.08501682],[108.22765103,7.083293],[108.22570055,7.08084671],[108.22460147,7.07791743],[108.19897125,6.95268198],[108.1987683,6.95072469],[108.19679674,6.53760583],[108.19687578,6.53630242],[108.23630689,6.22476797],[108.23638164,6.22427602],[108.26004031,6.09098419],[108.26055972,6.08912451]]],[[[110.12822847,11.36894451],[110.05553696,11.25335394],[110.05430621,11.25047749],[110.05402458,11.2473615],[110.05471962,11.24431099],[110.05632331,11.24162456],[110.05867865,11.23956519],[110.0615551,11.23833444],[110.06467109,11.23805281],[110.0677216,11.23874785],[110.07040803,11.24035153],[110.07246741,11.24270688],[110.14541497,11.35870461],[110.14588682,11.35954163],[110.20700505,11.48128846],[110.20728377,11.48189306],[110.25854422,11.60358735],[110.25901765,11.60499559],[110.30436343,11.7826124],[110.30456934,11.78364161],[110.32822801,11.94571326],[110.32832827,11.94685414],[110.33424294,12.14159753],[110.33424553,12.14210167],[110.33227398,12.24038351],[110.33172267,12.24346324],[110.33024665,12.24622187],[110.3279904,12.24838938],[110.32517479,12.24975358],[110.32207543,12.25018094],[110.3189957,12.24962962],[110.31623706,12.2481536],[110.31406956,12.24589736],[110.31270536,12.24308175],[110.312278,12.23998238],[110.3142445,12.14195265],[110.3083549,11.94803461],[110.28485499,11.78705054],[110.23982347,11.61066468],[110.18898148,11.48996382],[110.12822847,11.36894451]]],[[[109.82951587,15.22896754],[109.84522534,15.15316562],[109.84633168,15.15023907],[109.84828823,15.14779763],[109.85090347,15.14608029],[109.85392139,15.14525516],[109.85704658,15.145403],[109.85997314,15.14650935],[109.86241457,15.1484659],[109.86413191,15.15108113],[109.86495704,15.15409906],[109.8648092,15.15722425],[109.84903675,15.23333003],[109.84889209,15.23393326],[109.78974541,15.45068337],[109.7892391,15.45210582],[109.69066131,15.67432448],[109.6900529,15.67548445],[109.59147511,15.83677407],[109.59116145,15.8372556],[109.53201478,15.92259221],[109.53166592,15.92306523],[109.30888011,16.20725797],[109.30658844,16.20938798],[109.30375073,16.21070558],[109.30064474,16.21108179],[109.29757451,16.21047978],[109.29484059,16.20895848],[109.29271057,16.20666681],[109.29139298,16.2038291],[109.29101677,16.20072311],[109.29161878,16.19765288],[109.29314007,16.19491896],[109.51574449,15.91095759],[109.57455994,15.82609887],[109.67264555,15.66561455],[109.77065019,15.44468789],[109.82951587,15.22896754]]]]}}
]}
```

##### IV 中国省际测试代码

```js
myChart.showLoading();
echarts.registerMap('CHINA', china);
option = {
  title: {
    text: '中国省际碳排放时空分析图(2023)',
    subtext: 'Spatial and temporal analysis of China\'s interprovincial carbon emissions(2023)',
    left: 'right'
  },
  tooltip: {
    trigger: 'item',
    showDelay: 0,
    transitionDuration: 0.2
  },
  visualMap: {
    left: 'right',
    min: 500000,
    max: 38000000,
    inRange: {
      color: [
        '#313695',
        '#4575b4',
        '#74add1',
        '#abd9e9',
        '#e0f3f8',
        '#ffffbf',
        '#fee090',
        '#fdae61',
        '#f46d43',
        '#d73027',
        '#a50026'
      ]
    },
    text: ['High', 'Low'],
    calculable: true
  },
  // 左上角的：数据视图 还原 保存为图片
  toolbox: {
    show: true,
    //orient: 'vertical',
    left: 'left',
    top: 'top',
    feature: {
      dataView: { 
        readOnly: false,
        // optionToContent: (opt) => {
        //     // 自定义
        //     var axisData = opt.xAxis[0].data;
        //     var series = opt.series;
        //     var table = '<table style="width:100%;text-align:center"><tbody><tr>'
        //                 + '<td>时间</td>'
        //                 + '<td>' + series[0].name + '</td>'
        //                 + '<td>' + series[1].name + '</td>'
        //                 + '</tr>';
        //     for (var i = 0, l = axisData.length; i < l; i++) {
        //         table += '<tr>'
        //                 + '<td>' + axisData[i] + '</td>'
        //                 + '<td>' + series[0].data[i] + '</td>'
        //                 + '<td>' + series[1].data[i] + '</td>'
        //                 + '</tr>';
        //     }
        //     table += '</tbody></table>';
        //     return table;
        // }
      },
      restore: {},
      saveAsImage: {}
    }
  },
  series: [
    {
      name: '碳排放量',
      type: 'map',
      roam: true,
      map: 'CHINA',
      emphasis: {
        label: {
          show: true
        }
      },
      data: [
        { name: '北京市', value: 4822023 },
        { name: '天津市', value: 731449 },
        { name: '河北省', value: 6553255 },
        { name: '山西省', value: 2949131 },
        { name: '内蒙古自治区', value: 38041430 },
        { name: '辽宁省', value: 5187582 },
        { name: '吉林省', value: 3590347 },
        { name: '黑龙江省', value: 917092 },
        { name: '上海市', value: 632323 },
        { name: '江苏省', value: 19317568 },
        { name: '浙江省', value: 9919945 },
        { name: '安徽省', value: 1392313 },
        { name: '福建省', value: 1595728 },
        { name: '江西省', value: 12875255 },
        { name: '山东省', value: 6537334 },
        { name: '河南省', value: 3074186 },
        { name: '湖北省', value: 2885905 },
        { name: '湖南省', value: 4380415 },
        { name: '广东省', value: 4601893 },
        { name: '广西壮族自治区', value: 1329192 },
        { name: '海南省', value: 5884563 },
        { name: '重庆市', value: 6646144 },
        { name: '四川省', value: 9883360 },
        { name: '贵州省', value: 5379139 },
        { name: '云南省', value: 2984926 },
        { name: '西藏自治区', value: 6021988 },
        { name: '陕西省', value: 1005141 },
        { name: '甘肃省', value: 1855525 },
        { name: '青海省', value: 2758931 },
        { name: '宁夏回族自治区', value: 1320718 },
        { name: '新疆维吾尔自治区', value: 8864590 },
        { name: '台湾省', value: 2085538 },
        { name: '香港特别行政区', value: 19570261 },
        { name: '澳门特别行政区', value: 9752073 }
      ]
    }
  ]
};
myChart.setOption(option);
myChart.hideLoading();
```

##### V 中国省际测试示意图

![image-20230418025645019](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230418025645019.png)

### 3）引入 axios

> axios 官网：[起步 | Axios 中文文档 | Axios 中文网 (axios-http.cn)](https://www.axios-http.cn/docs/intro)

```
npm i axios -s
```

### 4）引入 Element-ui

> Element-ui 官网：[组件 | Element](https://element.eleme.cn/#/zh-CN/component/installation)

```
npm i element-ui -S
```

### 5）引入富文本框 WangEdit

> [wangEditor](https://www.wangeditor.com/)
>
> [用于 Vue React | wangEditor](https://www.wangeditor.com/v5/for-frame.html#使用)

```
npm install @wangeditor/editor-for-vue --save
```

```vue
<template>
    <div id="send-message">
        <div style="border: 1px solid #ccc;">
            <Toolbar
                    style="border-bottom: 1px solid #ccc"
                    :editor="editor"
                    :defaultConfig="toolbarConfig"
                    :mode="mode"
            />
            <Editor
                    style="height: 500px; overflow-y: hidden;"
                    v-model="html"
                    :defaultConfig="editorConfig"
                    :mode="mode"
                    @onCreated="onCreated"
            />
        </div>
        <div class="send-message-ops">
            <el-button>预览</el-button>
            <el-button>发送</el-button>
        </div>
    </div>
</template>

<script>
    import { Editor, Toolbar } from '@wangeditor/editor-for-vue';
    import '@wangeditor/editor/dist/css/style.css';

    export default {
        name: 'send-message',
        components: {
            Editor,
            Toolbar
        },
        data() {
            return {
                editor: null,
                html: '',
                toolbarConfig: { },
                editorConfig: { placeholder: '请输入内容...' },
                mode: 'default', // or 'simple'
            };
        },
        methods: {
            onCreated(editor) {
                this.editor = Object.seal(editor) // 一定要用 Object.seal() ，否则会报错
            },
        },
        // mounted() {
        //     // 模拟 ajax 请求，异步渲染编辑器
        //     setTimeout(() => {
        //         this.html = '<p>模拟 Ajax 异步设置内容 HTML</p>'
        //     }, 1500)
        // },
        beforeDestroy() {
            const editor = this.editor
            if (editor == null) return
            editor.destroy() // 组件销毁时，及时销毁编辑器
        }
    }
</script>

<style scoped>
    /*#send-message {*/
    /*    height: 600px;*/
    /*}*/

    .send-message-ops {
        margin-top: 10px;
        display: flex;
        justify-content: flex-end;
    }
</style>
```

给富文本框加上**上传图片**和**上传视频**的功能

> [菜单配置 | wangEditor](https://www.wangeditor.com/v5/menu-config.html#自定义功能)

## 七、API 设计与实现

### 1）绘制Excel表格(Java)

> 参考文献：[(1条消息) java实现导出数据到Excel表格_JAVA_AVA的博客-CSDN博客](https://blog.csdn.net/javaDX/article/details/120747090)

```
SpringBoot 默认提供了四种静态资源目录:

classpath:/public/
classpath:/static/
classpath:/resources/
classpath:/META-INF/resources/

优先级依次升高。 即，如果在四个目录下都放置相同的文件名称的静态文件，访问时，会优先展示 /META-INF/resources下的文件内容。
```

​	**由于我们的项目并不希望用户可以直接用过网络 url 下载对应的 excel 表格，所以在本项目中不会将文件放入以上项目！**

**CESService**

```java
/**
 * ${year}_ces 表的数据 => 装换成 excel
 * @param year 年份 => "2019", "2018"
 */
public void export(String year) {
    // 判断该年份的碳排放数据 excel 表是否存在，如果存在则直接返回
    if (ExportExcel.exit("/ces", year + "年碳排放数据")) return;

    List<CES> list = getAllProvince(year); // 获取该年份的碳排放数据

    final List<CESResp> list1 = CopyUtil.copyList(list, CESResp.class);

    ExportExcel.exportPlus("/ces", year + "年碳排放数据", list1.size(),
            (row)-> {
                for (int i = 0; i < title.size(); i++) {
                    row.createCell(i).setCellValue(title.get(i));
                }
            },
            (row, i)->{
                CESResp resp = list1.get(i);
                row.createCell(0).setCellValue(resp.getReg());
                row.createCell(1).setCellValue(resp.getValue());
            });
}
```

**ExportExcel**

```java
package cn.cetasas.db.util;

import cn.cetasas.db.exception.BusinessException;
import cn.cetasas.db.exception.BusinessExceptionCode;
import cn.cetasas.db.resp.CESResp;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.net.URLDecoder;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 将 Map 对象转换成 Excel 文件
 *
 * SpringBoot 默认提供了四种静态资源目录:
 * classpath:/public/
 * classpath:/static/
 * classpath:/resources/
 * classpath:/META-INF/resources/
 *
 * 我们的项目不希望用户可以直接用过网络 url 下载对应的 excel 表格，所以在本项目中会将对应的 excel 表格放入 /excel 目录下
 *
 * 实例代码：
 * ExportExcel.export(list1, title, "/ces", "2019年碳排放数据");
 * ExportExcel.exportPlus("/ces", "2019年碳排放数据", list1.size(),
 *         (row)-> {
 *             for (int i = 0; i < title.size(); i++) {
 *                 row.createCell(i).setCellValue(title.get(i));
 *             }
 *         },
 *         (row, i)->{
 *             CESResp resp = list1.get(i);
 *             row.createCell(0).setCellValue(resp.getReg());
 *             row.createCell(1).setCellValue(resp.getValue());
 *         });
 */
public class ExportExcel {

    private final static Logger LOG = LoggerFactory.getLogger(ExportExcel.class);

//    /E:/docs/college/%e6%af%95%e4%b8%9a%e8%ae%be%e8%ae%a1/%e4%bb%a3%e7%a0%81/carbon-emission-system/db-service/target/test-classes/
//    private static String basePath;
//
//    static {
//        try {
//            basePath = ResourceUtils.getURL("classpath:").getPath();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

    private static String basePath;

    static {
        try {
            basePath = URLDecoder.decode(ResourceUtils.getURL("classpath:").getPath().substring(1), "utf-8") + "excel";
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 缺点：只能导出碳排放数据表 => 不够通用
     * 改造方法：将第 2 步和第 3 步交由调用者去实现
     * @param list 要导入的数据列表
     * @param title 表头，传入形式如: {props: value}
     * @param path 需要存放的路径, 传入形式如: /ces
     * @param fileName 保存的文件名
     */
    @Deprecated
    public static void export(List<CESResp> list, List<String> title, String path, String fileName) {
        File file = createFile(path, fileName);
        try(// 1）创建一个工作簿
            Workbook wb = new HSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
            // 2）在工作簿中创建一个 sheet => 表
            Sheet sheet = wb.createSheet(fileName);
            // 3）设置表头
            Row tableTile = sheet.createRow(0);
            for (int i = 0; i < title.size(); i++) {
                Cell cell = tableTile.createCell(i);
                cell.setCellValue(title.get(i));
            }
            // 4）设置表的内容
            for (CESResp obj : list) {
                Row row = sheet.createRow(sheet.getLastRowNum() + 1);
                row.createCell(0).setCellValue(obj.getReg());
                row.createCell(1).setCellValue(obj.getValue());
            }
            // 5）导出
            wb.write(fos);
        } catch (IOException e) {
            throw new BusinessException(BusinessExceptionCode.EXPORT_FAILED);
        }
    }

    /**
     *
     * @param path 存放路径, 传入形式如: /ces
     * @param fileName 文件名
     * @param size 导入数据长度
     * @param setTitle 自定义设置表头的方法
     * @param setContent 自定义设置表的数据的方法，接受参数 行和第几行元素
     * @return 返回导出的文件名
     */
    public static String exportPlus(String path, String fileName, int size, Consumer<Row> setTitle, BiConsumer<Row, Integer> setContent) {
        File file = createFile(path, fileName);
        try(// 1）创建一个工作簿
            Workbook wb = new HSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
            // 2）在工作簿中创建一个 sheet => 表
            Sheet sheet = wb.createSheet(fileName);
            // 3）设置表头
            setTitle.accept(sheet.createRow(0));
            // 4）设置表的内容
            for (int i = 0; i < size; i++) { // i 是第 i 行
                setContent.accept(sheet.createRow(sheet.getLastRowNum() + 1), i);
            }
            // 5）导出
            wb.write(fos);
        } catch (IOException e) {
            throw new BusinessException(BusinessExceptionCode.EXPORT_FAILED);
        }
        return file.getName();
    }

    private static File createFile(String path, String fileName) {
        String pathName = basePath + path;
        File file = new File(pathName);
        if (!file.exists()) { // 如果文件夹不存在、则新建
            LOG.info("创建目录 or 文件夹：{}", pathName);
            boolean newFile = file.mkdirs();
            LOG.info("创建结果：{}", newFile);
        }
        return new File(basePath + path + "/" + fileName + ".xls");
    }

    /**
     * 判断 basePath + path + "/" 路径下的文件【fileName.xls】是否存在
     * @param path 存放路径
     * @param fileName 文件名
     * @return
     */
    public static boolean exit(String path, String fileName) {
        File file = new File(getFullName(basePath + path + "/" + fileName));
        return file.exists();
    }

    public static String getBasePath() {
        return basePath;
    }

    public static String getFullName(String s) {
        return s + ".xls";
    }
}
```

ces-serice

```java
    /**
     * ${year}_ces 表的数据 => 装换成 excel
     * @param year 年份 => "2019", "2018"
     * @return
     */
    public String export(String year) {
        // 判断该年份的碳排放数据 excel 表是否存在，如果存在则直接返回
        if (!ExportExcel.exit(downloadPath, year + downloadSuffix)) {
            List<CES> list = getAllProvince(year); // 获取该年份的碳排放数据

//            final List<CESResp> list1 = CopyUtil.copyList(list, CESResp.class);

            return ExportExcel.exportPlus(downloadPath, year + downloadSuffix, list.size(),
                    (row)-> {
                        for (int i = 0; i < title.size(); i++) {
                            row.createCell(i).setCellValue(title.get(i));
                        }
                    },
                    (row, i)->{
                        CES resp = list.get(i);
                        row.createCell(0).setCellValue(resp.getReg());
                        row.createCell(1).setCellValue(resp.getValue());
                    });
        } else return ExportExcel.getFullName(year + downloadSuffix);
    }
```

ees-service

```java
    /**
     * ${year}_ees 表的数据 => 装换成 excel
     * @param year 年份 => "2019", "2018"
     * @return
     */
    public String export(String year) {
        // 判断该年份的碳排放数据 excel 表是否存在，如果存在则直接返回
        if (!ExportExcel.exit(downloadPath, year + downloadSuffix)) {
            List<EES> list = getAllProvince(year); // 获取该年份的碳排放数据

            final Map<String, Map<String, Double>> map = cast(list); // [{r_code e_code reg zb value cef}] => {reg:{zb:value}}}
            final List<String> column = new ArrayList<>(map.keySet()); // ["北京市","天津市"...]
//        final List<String> title = new ArrayList<>(map.get("北京市").keySet());
            final List<String> title = new ArrayList<>(map.get(column.get(0)).keySet());
            return ExportExcel.exportPlus(downloadPath, year + downloadSuffix, column.size(),
                    (row)-> {
                        row.createCell(0).setCellValue("省份\\能源(万吨/亿立方米/亿千瓦小时)");
                        for (int i = 0; i < title.size(); i++) {
                            row.createCell(i + 1).setCellValue(title.get(i));
                        }
                    },
                    (row, i)->{
                        String reg = column.get(i);
                        Map<String, Double> zbs = map.get(reg);
                        row.createCell(0).setCellValue(reg);
                        for (int i1 = 0; i1 < title.size(); i1++) {
                            String s = title.get(i1);
                            Double value = zbs.get(s);
                            row.createCell(i1 + 1).setCellValue(value);
                        }
                    });
        } else return ExportExcel.getFullName(year + downloadSuffix);
    }
```

total-service



### 2）下载数据到本地

#### I 后端

```java
public void download(String fileName, HttpServletResponse response) {
        String absolutePath = ExportExcel.getBasePath() + downloadPath + "/" + fileName;
        InputStream in = null;
        OutputStream out = null;
        try {
            // 1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
            response.setContentType("multipart/form-data");
            // 2.设置文件头：最后一个参数是设置下载文件名
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            // 3.将 Content-Disposition 放出让前端可以访问
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            // 4.传输数据
            in = new FileInputStream(absolutePath);
            out = response.getOutputStream();
            int bytes = IOUtils.copy(in, out);
            LOG.info("File Written with {} bytes", bytes);
            out.flush();
        } catch (IOException e) {
            throw new BusinessException(BusinessExceptionCode.DOWNLOAD_FILE_FAILED);
//            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
```

#### II 前端

```js
download(val) {
    const year = val.substring(0, 4);
    const url = `/db/${this.db}/download?year=${year}`;
    // console.log(url);
    // 在axios请求中加入responseType：'blob'参数，表示接收的数据为二进制文件流
    this.$message.info("正在下载中，请客官稍等片刻ヾ(◍°∇°◍)ﾉﾞ...");
    axios.get(url, {responseType: 'blob'}).then(resp => {
        const filename = resp.headers['content-disposition'].split("=")[1];
        // 将数据流转换为对应格式的文件，并创建a标签，模拟点击下载，实现文件下载功能
        let fileReader = new FileReader();
        fileReader.readAsDataURL(resp.data);
        fileReader.onload = e => {
            let a = document.createElement('a');
            a.download = decodeURI(filename);
            a.href = e.target.result;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
        }
    }).catch(error => {
        this.$message.error("系统错误，请联系管理员！");
    })
}
```

### 3）发送邮件（用于注册用户和忘记密码）

```java
package cn.cetasas.user.util;

import cn.cetasas.user.exception.BusinessException;
import cn.cetasas.user.exception.BusinessExceptionCode;
import cn.cetasas.user.pojo.Mail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.Date;

@Component
public class MailUtil {

    private final static Logger LOG = LoggerFactory.getLogger(MailUtil.class);

    @Resource
    private JavaMailSenderImpl javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    // 主题
    private final static String subject = "中国省际碳排放时空分析系统: 注册验证码 ?";

    // 内容
    private final static String text = "<body>\n" +
            "<h3>亲爱的用户！</h3>\n" +
            "<span>这是您的注册验证码 <font color=\"red\">?</font>，请在 60 秒内使用。</span></br>\n" +
            "——来自中国省际碳排放时空分析系统\n" +
            "</body>";

    /**
     * 检测邮件信息类
     * @param to
     * @param subject
     * @param text
     */
    private void checkMail(String to,String subject,String text) {
        if (StringUtils.isEmpty(to)) {
            throw new RuntimeException("邮件收信人不能为空");
        }
        if (StringUtils.isEmpty(subject)) {
            throw new RuntimeException("邮件主题不能为空");
        }
        if (StringUtils.isEmpty(text)) {
            throw new RuntimeException("邮件内容不能为空");
        }
    }

    // 给该邮箱发送 "验证码"
    @Async
    public void sendVerification(Mail mail, String verification) {
        try {
            //true 代表支持复杂的类型
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(javaMailSender.createMimeMessage(),true);
            //邮件发信人
            mimeMessageHelper.setFrom(sender);
            //邮件收信人  1或多个
//            mimeMessageHelper.setTo(to.split(","));
            mimeMessageHelper.setTo(mail.getMail());
            //邮件主题
            mimeMessageHelper.setSubject(subject.replace("?", verification));
            //邮件内容
            mimeMessageHelper.setText(text.replace("?", verification));
            //邮件发送时间 => 马上发送
            mimeMessageHelper.setSentDate(new Date());

            //发送邮件
            javaMailSender.send(mimeMessageHelper.getMimeMessage());
            LOG.info("发送邮件成功：{} -> {}", sender, mail.getMail());
        } catch (MessagingException e) {
            throw new BusinessException(BusinessExceptionCode.SEND_MAIL_ERROR);
        }
    }
}
```

### 4）登录校验（redis + token）

##### I 后端获取 token，并设置进redis（24h后过期）

```java
package cn.cetasas.user.controller;

import cn.cetasas.user.exception.BusinessException;
import cn.cetasas.user.exception.BusinessExceptionCode;
import cn.cetasas.user.req.UserLoginRequest;
import cn.cetasas.user.req.UserResetPasswordRequest;
import cn.cetasas.user.req.UserSaveRequest;
import cn.cetasas.user.resp.CommonResp;
import cn.cetasas.user.resp.UserLoginResponse;
import cn.cetasas.user.service.UserService;
import cn.cetasas.user.util.SnowFlake;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class UserController {

    private final static Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate; // 操作 redis 的工具

    @Resource
    private SnowFlake snowFlake; // 雪花算法

    // 登录
    @PostMapping("/login")
    // @RequestBody 这个注解对应的就是json方式的（Post）提交
    // 如果是 form 表单的提交，就不用写这个注解了@RequestBody
    public CommonResp<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest user) {
        LOG.info("用户【{}】正在尝试登录", user.getUsername());
        // 16 进制的 md5
        // 注册的时候，密码是经过两层加密的，所以登录的时候也要经过两层加密
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        CommonResp<UserLoginResponse> resp = new CommonResp<>();

        // 如果有验证码 => 则检验 => 配合的是 /user/verification/get
        if (!ObjectUtils.isEmpty(user.getVerification())) {
            verify(user.getUsername(), user.getVerification());
        }

        // 登录
        UserLoginResponse userLoginResponse = userService.login(user);

        // 登录成功之后，能够执行到这的，一定是登录成功的；因为登录失败的，都被异常处理了
        Long token = snowFlake.nextId(); // token 也要返回给前端的！！
                                         // 因为前端后面的请求，都要带上这个 token，好让我们后端可以验证这个 token 是不是有效的
                                         // 即给 UserLoginResp 也增加一个 token 属性
        LOG.info("生成单点登录token {}, 并放入redis中！", token);
        userLoginResponse.setToken(token);

        // 将登录信息保存进 redis 中
        redisTemplate.opsForValue().set(token, JSON.toJSON(userLoginResponse), 3600 * 24, TimeUnit.SECONDS); // 24 h 后登录信息消失

        resp.setContent(userLoginResponse);
        return resp;
    }

    // 注册
    @PostMapping
    public CommonResp<String> save(@Valid @RequestBody UserSaveRequest user) {
        LOG.info("正在注册用户：{}", user);
        CommonResp<String> resp = new CommonResp<>();

        // 检验验证码 => 配合的是 /user/verification/send
        verify(user.getMail(), user.getVerification());

        // 16 进制的 md5 => 加密
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        userService.save(user);
        resp.setMessage("注册成功");
        LOG.info("用户【{}】注册成功...", user.getName());
        return resp;
    }

    // 修改密码
    @PostMapping("/reset_password")
    public CommonResp<String> restPassword(@Valid @RequestBody UserResetPasswordRequest user) {
        CommonResp<String> resp = new CommonResp<>();

        // 16 进制的 md5 => 加密
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        userService.resetPassword(user); // 修改密码
        resp.setMessage("修改密码成功");
        return resp;
    }

    // 修改密码前检验是否是本人
    @GetMapping("/verify")
    public CommonResp<String> verifyBeforeRestPassword(@RequestParam("mail") String mail,
                                                       @RequestParam("verification") String verification) {
        CommonResp<String> resp = new CommonResp<>();
        verify(mail, verification);
        LOG.info("验证成功");
        return resp;
    }

    private void verify(String key, String verification) {
        Object o = redisTemplate.opsForValue().get(key);
        if (ObjectUtils.isEmpty(o)) {
            throw new BusinessException(BusinessExceptionCode.VERIFICATION_OUT_OF_TIME);
        } else if (o.toString().equals(verification)) {
            LOG.info("验证成功");
        } else {
            throw new BusinessException(BusinessExceptionCode.VERIFICATION_NOT_MATCH);
        }
    }
}
```

##### II 前端每次请求带上 token（在main.js里配置）



### 5）获取图片验证码

> [5分钟搞定 SpringBoot 图形验证码功能 - 腾讯云开发者社区-腾讯云 (tencent.com)](https://cloud.tencent.com/developer/article/1973787)

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="initial-scale=1.0,width=device-width, user-scalable=no"/>
  <script src="https://cdn.bootcss.com/jquery/3.4.1/jquery.min.js"></script>
  <title>Title</title>
</head>
<body>
<p>方式一</p>
<img alt="验证码1" id="code1" onclick="check1()"/>

<p>方式二</p>
<img alt="验证码2" id="code2" onclick="check2()"/>
</body>
</html>
<script>
  getCode1();
  getCode2();

  // 点击事件
  function check1() {
    getCode1();
  }

  // 点击事件
  function check2() {
    getCode2();
  }

  // 响应类型以blob的方式
  function getCode1() {
    var url = "http://127.0.0.1/verify/getcode";
    var xhr = new XMLHttpRequest();
    xhr.open('POST', url, true);
    xhr.responseType = "blob";
    xhr.onload = function () {
      if (this.status === 200) {
        var res = this.response;
        $("#code1").attr("src", window.URL.createObjectURL(res));
      }
    };
    xhr.send();
  }

  // 响应类型以arraybuffer的方式
  function getCode2() {
    var url = "http://127.0.0.1/verify/getcode";
    var xhr = new XMLHttpRequest();
    xhr.open('POST', url, true);
    xhr.responseType = "arraybuffer";
    xhr.onload = function () {
      if (this.status === 200) {
        var res = this.response;
        $("#code2").attr("src", "data:image/png;base64,"  
          btoa(
            new Uint8Array(res).reduce(function (data, byte) {
              console.log(data);
              return data   String.fromCharCode(byte)
            }, '')
          ));
      }
    };
    xhr.send();
  }
</script>
```

**修改后**

```js
console.log("click");
const url = "http://127.0.0.1:10010/user/verification/get";
const xhr = new XMLHttpRequest();
xhr.open('GET', url, true);
xhr.responseType = "blob";
xhr.onload = function () {
    if (this.status === 200) {
        const res = this.response;
        document.getElementById("code").setAttribute("src", window.URL.createObjectURL(res));
    }
};
xhr.send();
```



## 八、项目部署



## 九、服务端设计

### 1）Nacos

#### 1. Windows安装

开发阶段采用单机安装即可。

##### I 下载安装包

在Nacos的GitHub页面，提供有下载链接，可以下载编译好的Nacos服务端或者源代码：

GitHub主页：https://github.com/alibaba/nacos

GitHub的Release下载页：https://github.com/alibaba/nacos/releases

如图：

![image-20210402161102887](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20210402161102887.png)

本课程采用1.4.1.版本的Nacos，课前资料已经准备了安装包：

![image-20210402161130261](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20210402161130261.png)

windows版本使用`nacos-server-1.4.1.zip`包即可。

##### II 解压

将这个包解压到任意非中文目录下，如图：

![image-20210402161843337](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20210402161843337.png)

目录说明：

- bin：启动脚本
- conf：配置文件

##### III 端口配置

Nacos的默认端口是8848，如果你电脑上的其它进程占用了8848端口，请先尝试关闭该进程。

**如果无法关闭占用8848端口的进程**，也可以进入nacos的conf目录，修改配置文件中的端口：

![image-20210402162008280](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20210402162008280.png)

修改其中的内容：

![image-20210402162251093](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20210402162251093.png)

##### IV 启动

启动非常简单，进入bin目录，结构如下：

![image-20210402162350977](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20210402162350977.png)

然后执行命令即可：

- windows命令：

  ```
  startup.cmd -m standalone
  ```


执行后的效果如图：

![image-20210402162526774](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20210402162526774.png)

##### V 访问

在浏览器输入地址：http://127.0.0.1:8848/nacos即可：

![image-20210402162630427](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20210402162630427.png)

默认的账号和密码都是nacos，进入后：

![image-20210402162709515](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20210402162709515.png)

#### 2. Linux安装

Linux或者Mac安装方式与Windows类似。

##### I 安装JDK

Nacos依赖于JDK运行，索引Linux上也需要安装JDK才行。

上传jdk安装包：

![image-20210402172334810](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20210402172334810.png)

上传到某个目录，例如：`/usr/local/`

然后解压缩：

```sh
tar -xvf jdk-8u144-linux-x64.tar.gz
```

然后重命名为java

配置环境变量：

```sh
export JAVA_HOME=/usr/local/java
export PATH=$PATH:$JAVA_HOME/bin
```

设置环境变量：

```sh
source /etc/profile
```

##### II 上传安装包

如图：

![image-20210402161102887](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20210402161102187.png)

也可以直接使用课前资料中的tar.gz：

![image-20210402161130261](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20210402161131261.png)

上传到Linux服务器的某个目录，例如`/usr/local/src`目录下：

![image-20210402163715580](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20210402163715580.png)

##### III 解压

命令解压缩安装包：

```sh
tar -xvf nacos-server-1.4.1.tar.gz
```

然后删除安装包：

```sh
rm -rf nacos-server-1.4.1.tar.gz
```

目录中最终样式：

![image-20210402163858429](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20210402163858429.png)

目录内部：

![image-20210402164414827](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20210402164414827.png)



##### IV 端口配置

与windows中类似

##### V 启动

在nacos/bin目录中，输入命令启动Nacos：

```sh
sh startup.sh -m standalone
```

#### 3. Nacos的依赖

父工程：

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-dependencies</artifactId>
    <version>2.2.5.RELEASE</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

客户端：

```xml
<!-- nacos客户端依赖包 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>

```

### 2）user-service

> 顾名思义：提供用户服务

#### 1. 引入 mybatis-generator

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
        "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">
<generatorConfiguration>
    <context id="Mysql" targetRuntime="MyBatis3"  defaultModelType="flat">

        <!-- 自动检查关键字，为关键字增加反引号 -->
        <property name="autoDelimitKeywords" value="true"/>
        <property name="beginningDelimiter" value="`"/>
        <property name="endingDelimiter" value="`"/>

        <!-- 覆盖生成XML文件-->
        <plugin type="org.mybatis.generator.plugins.UnmergeableXmlMappersPlugin" />
        <!-- 生成的实体类添加toString()方法 -->
        <plugin type="org.mybatis.generator.plugins.ToStringPlugin" />

        <!-- 不生成注释 -->
        <commentGenerator>
<!--            <property name="suppressDate" value="true" />-->
            <!-- 是否去除自动生成的注释 true：是 ： false:否 -->
            <property name="suppressAllComments" value="true" />
        </commentGenerator>

        <!-- 数据库链接配置 -->
        <jdbcConnection driverClass="com.mysql.cj.jdbc.Driver"
                        connectionURL="jdbc:mysql://localhost:3306/bysj_user"
                        userId="bysj" password="bysj">
        </jdbcConnection>

        <!-- domain类的位置 -->
        <javaModelGenerator targetPackage="cn.cetasas.user.pojo"
                            targetProject="src\main\java"/>

        <!-- mapper xml 的位置 -->
        <sqlMapGenerator targetPackage="mapper"
                         targetProject="src\main\resources"/>

        <!-- mapper 类的位置 -->
        <javaClientGenerator type="XMLMAPPER"
                             targetPackage="cn.cetasas.user.mapper"
                             targetProject="src\main\java"/>

        <!-- 需要生成对应pojo、mapper等实体类对应的表 -->
         <table tableName="user"/>
    </context>
</generatorConfiguration>
```

#### 2. 初始化用户表

```sql
-- 创建用户表 => 储存用户的用户名、密码和邮箱 => 邮箱为 id
DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `id` INT AUTO_INCREMENT COMMENT '主键',
  `mail` VARCHAR(50) NOT NULL UNIQUE COMMENT '邮箱',
  `name` VARCHAR(20) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(20) NOT NULL COMMENT '密码',
  PRIMARY KEY (`id`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '用户表';

INSERT INTO `user`(`mail`, `name`, `password`)
VALUES('827543964@qq.com', 'admin', 'admin');
```

提供验证码

```java
package cn.cetasas.user.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Random;

@Service
public class MailService {
    private final static char[] ch =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
             'A', 'B', 'C', 'D', 'E', 'F', 'G',
             'H', 'I', 'J', 'K', 'L', 'M', 'N',
             'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
             'a', 'b', 'c', 'd', 'e', 'f', 'g',
             'h', 'i', 'j', 'k', 'l', 'm', 'n',
             'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    private final static Random random = new Random();

    @GetMapping
    public String getVerification() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i <6; i++){
            char num = ch[random.nextInt(ch.length)];
            str.append(num);
        }
        return str.toString();
    }
}
```

#### 3. 发送邮件

<font color='crimson'>给指定邮箱发送验证码！！！！</font>

> Java 实现发送邮件：[(6条消息) Java实现邮件发送_java发送邮件_Marvellous丶的博客-CSDN博客](https://blog.csdn.net/baolingye/article/details/96598222)
>
> SpringBoot 实现发送邮件：[(6条消息) Springboot实现发送邮件功能_12程序猿的博客-CSDN博客](https://blog.csdn.net/qq_26383975/article/details/121957917)

##### I 获取QQ邮箱的授权码

![image-20230504205133423](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230504205133423.png)

![image-20230504205233252](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230504205233252.png)

![image-20230504210010290](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230504210010290.png)

![image-20230504210039970](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230504210039970.png)

##### II 引入依赖

```xml
<dependency>  
        <groupId>org.springframework.boot</groupId>  
        <artifactId>spring-boot-starter-mail</artifactId>  
</dependency>
```

##### III 有关配置

```yaml
spring:
  #邮箱基本配置
  mail:
    #配置smtp服务主机地址
    # qq邮箱为smtp.qq.com          端口号465或587
    # sina    smtp.sina.cn
    # aliyun  smtp.aliyun.com
    # 163     smtp.163.com       端口号465或994
    host: smtp.qq.com
    #发送者邮箱
    username: 2371584307@qq.com
    #配置密码,注意不是真正的密码，而是刚刚申请到的授权码
    password: xlvpnfffcyxdecib
    #端口号465或587
    port: 587
    #默认的邮件编码为UTF-8
    default-encoding: UTF-8
    #其他参数
    properties:
     mail:
        #配置SSL 加密工厂
      smtp:
        ssl:
          #本地测试，先放开ssl
          enable: false
          required: false
        #开启debug模式，这样邮件发送过程的日志会在控制台打印出来，方便排查错误
      debug: true
```

##### IV 发送

```java
package cn.cetasas.user.util;

import cn.cetasas.user.exception.BusinessException;
import cn.cetasas.user.exception.BusinessExceptionCode;
import cn.cetasas.user.pojo.Mail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.Date;

@Component
public class MailUtil {

    private final static Logger LOG = LoggerFactory.getLogger(MailUtil.class);

    @Resource
    private JavaMailSenderImpl javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    // 主题
    private final static String subject = "中国省际碳排放时空分析系统: 注册验证码 ?";

    // 内容
    private final static String text = "<body>\n" +
            "<h3>亲爱的用户！</h3>\n" +
            "<span>这是您的注册验证码 <font color=\"red\">?</font>，请在 60 秒内使用。</span></br>\n" +
            "——来自中国省际碳排放时空分析系统\n" +
            "</body>";

    /**
     * 检测邮件信息类
     * @param to
     * @param subject
     * @param text
     */
    private void checkMail(String to,String subject,String text) {
        if (StringUtils.isEmpty(to)) {
            throw new RuntimeException("邮件收信人不能为空");
        }
        if (StringUtils.isEmpty(subject)) {
            throw new RuntimeException("邮件主题不能为空");
        }
        if (StringUtils.isEmpty(text)) {
            throw new RuntimeException("邮件内容不能为空");
        }
    }

    // 给该邮箱发送 "验证码"
    @Async
    public void sendVerification(Mail mail, String verification) {
        try {
            //true 代表支持复杂的类型
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(javaMailSender.createMimeMessage(),true);
            //邮件发信人
            mimeMessageHelper.setFrom(sender);
            //邮件收信人  1或多个
//            mimeMessageHelper.setTo(to.split(","));
            mimeMessageHelper.setTo(mail.getMail());
            //邮件主题
            mimeMessageHelper.setSubject(subject.replace("?", verification));
            //邮件内容
            mimeMessageHelper.setText(text.replace("?", verification));
            //邮件发送时间 => 马上发送
            mimeMessageHelper.setSentDate(new Date());

            //发送邮件
            javaMailSender.send(mimeMessageHelper.getMimeMessage());
            LOG.info("发送邮件成功：{} -> {}", sender, mail.getMail());
        } catch (MessagingException e) {
            throw new BusinessException(BusinessExceptionCode.SEND_MAIL_ERROR);
        }
    }
}
```

### 3）db-service

> 顾名思义：提供数据库服务

### 4）feign

> 提供跨域请求

### 5）gateway服务网关

> 顾名思义：提供网关服务

Spring Cloud Gateway 是 Spring Cloud 的一个全新项目，该项目是基于 Spring 5.0，Spring Boot 2.0 和 Project Reactor 等响应式编程和事件流技术开发的网关，它旨在为微服务架构提供一种简单有效的统一的 API 路由管理方式。

```yml
spring:
    gateway:
      routes:
        - id: web-service # 路由唯一标识
          uri: lb://webservice # lb 表示 LoadBalance，表示路由的目标地址
          predicates: # 路由断言，判断请求是否符合规则
            - Path=/web/** # 路径断言，判断路径是否以 /web 开头，如果是则符合
        - id: user-service
          uri: lb://userservice
          predicates:
            - Path=/user/**
        - id: db-service
          uri: lb://dbservice
          predicates:
            - Path=/db/**
        - id: message-service
          uri: lb://messageservice
          predicates:
            - Path=/message/**
      # Gateway 解决跨域问题
      globalcors: # 全局的跨域处理
        add-to-simple-url-handler-mapping: true # 解决options请求被拦截问题
        corsConfigurations:
          '[/**]':
            allowedOrigins: # 允许哪些网站的跨域请求
              - "*" # 允许所有网站
            allowedMethods: # 允许的跨域ajax的请求方式
              - "GET"
              - "POST"
              - "DELETE"
              - "PUT"
              - "OPTIONS"
            allowedHeaders: "*" # 允许在请求中携带的头信息
            allowCredentials: true # 是否允许携带cookie
            maxAge: 360000 # 这次跨域检测的有效期
```

```java
package cn.cetasas.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

// 除去 Gateway 所有的启动类都需要加上
// @EnableRedisHttpSession(flushMode = FlushMode.IMMEDIATE)
@SpringBootApplication
public class GatewayApplication {
    private static final Logger LOG = LoggerFactory.getLogger(GatewayApplication.class);

    public static void main(String[] args) {
//        SpringApplication.run(GatewayApplication.class, args);
        SpringApplication app = new SpringApplication(GatewayApplication.class);
        Environment env = app.run(args).getEnvironment();
        LOG.info("ヾ(◍°∇°◍)ﾉﾞ\t启动成功!!");
        LOG.info("启动地址: http://127.0.0.1:{}", env.getProperty("server.port"));
    }
}
```

#### 1. 为什么需要网关

Gateway网关是我们服务的守门神，所有微服务的统一入口。

网关的**核心功能特性**：

- 请求路由
- 权限控制
- 限流

架构图：

![image-20210714210131152](E:\video\微服务开发框架SpringCloud+RabbitMQ+Docker+Redis+搜索+分布式史上最全面的微服务全技术栈课程\实用篇\学习资料\day02-SpringCloud02\讲义\assets\image-20210714210131152.png)

**权限控制**：网关作为微服务入口，需要校验用户是是否有请求资格，如果没有则进行拦截。

**路由和负载均衡**：一切请求都必须先经过gateway，但网关不处理业务，而是根据某种规则，把请求转发到某个微服务，这个过程叫做路由。当然路由的目标服务有多个时，还需要做负载均衡。

**限流**：当请求流量过高时，在网关中按照下流的微服务能够接受的速度来放行请求，避免服务压力过大。

在SpringCloud中网关的实现包括两种：

- gateway
- zuul

Zuul是基于Servlet的实现，属于阻塞式编程。而SpringCloudGateway则是基于Spring5中提供的WebFlux，属于响应式编程的实现，具备更好的性能。

#### 2. gateway快速入门

下面，我们就演示下网关的基本路由功能。基本步骤如下：

1. 创建SpringBoot工程gateway，引入网关依赖
2. 编写启动类
3. 编写基础配置和路由规则
4. 启动网关服务进行测试

##### I 创建gateway服务，引入依赖

创建服务：

![image-20210714210919458](E:\video\微服务开发框架SpringCloud+RabbitMQ+Docker+Redis+搜索+分布式史上最全面的微服务全技术栈课程\实用篇\学习资料\day02-SpringCloud02\讲义\assets\image-20210714210919458.png)

引入依赖：

```xml
<!--网关-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<!--nacos服务发现依赖-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

##### II 编写启动类

```java
package cn.itcast.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}
}
```

##### III 编写基础配置和路由规则

创建application.yml文件，内容如下：

```yaml
server:
  port: 10010 # 网关端口
spring:
  application:
    name: gateway # 服务名称
  cloud:
    nacos:
      server-addr: localhost:8848 # nacos地址
    gateway:
      routes: # 网关路由配置
        - id: user-service # 路由id，自定义，只要唯一即可
          # uri: http://127.0.0.1:8081 # 路由的目标地址 http就是固定地址
          uri: lb://userservice # 路由的目标地址 lb就是负载均衡，后面跟服务名称
          predicates: # 路由断言，也就是判断请求是否符合路由规则的条件
            - Path=/user/** # 这个是按照路径匹配，只要以/user/开头就符合要求
```

我们将符合`Path` 规则的一切请求，都代理到 `uri`参数指定的地址。

本例中，我们将 `/user/**`开头的请求，代理到`lb://userservice`，lb是负载均衡，根据服务名拉取服务列表，实现负载均衡。

##### IV 重启测试

重启网关，访问http://localhost:10010/user/1时，符合`/user/**`规则，请求转发到uri：http://userservice/user/1，得到了结果：

![image-20210714211908341](E:\video\微服务开发框架SpringCloud+RabbitMQ+Docker+Redis+搜索+分布式史上最全面的微服务全技术栈课程\实用篇\学习资料\day02-SpringCloud02\讲义\assets\image-20210714211908341.png)

##### V 网关路由的流程图

整个访问的流程如下：

![image-20210714211742956](E:\video\微服务开发框架SpringCloud+RabbitMQ+Docker+Redis+搜索+分布式史上最全面的微服务全技术栈课程\实用篇\学习资料\day02-SpringCloud02\讲义\assets\image-20210714211742956.png)

总结：

网关搭建步骤：

1. 创建项目，引入nacos服务发现和gateway依赖

2. 配置application.yml，包括服务基本信息、nacos地址、路由

路由配置包括：

1. 路由id：路由的唯一标示

2. 路由目标（uri）：路由的目标地址，http代表固定地址，lb代表根据服务名负载均衡

3. 路由断言（predicates）：判断路由的规则，

4. 路由过滤器（filters）：对请求或响应做处理

接下来，就重点来学习路由断言和路由过滤器的详细知识

#### 3. 断言工厂

我们在配置文件中写的断言规则只是字符串，这些字符串会被Predicate Factory读取并处理，转变为路由判断的条件

例如Path=/user/**是按照路径匹配，这个规则是由

`org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory`类来

处理的，像这样的断言工厂在SpringCloudGateway还有十几个:

| **名称**   | **说明**                       | **示例**                                                     |
| ---------- | ------------------------------ | ------------------------------------------------------------ |
| After      | 是某个时间点后的请求           | -  After=2037-01-20T17:42:47.789-07:00[America/Denver]       |
| Before     | 是某个时间点之前的请求         | -  Before=2031-04-13T15:14:47.433+08:00[Asia/Shanghai]       |
| Between    | 是某两个时间点之前的请求       | -  Between=2037-01-20T17:42:47.789-07:00[America/Denver],  2037-01-21T17:42:47.789-07:00[America/Denver] |
| Cookie     | 请求必须包含某些cookie         | - Cookie=chocolate, ch.p                                     |
| Header     | 请求必须包含某些header         | - Header=X-Request-Id, \d+                                   |
| Host       | 请求必须是访问某个host（域名） | -  Host=**.somehost.org,**.anotherhost.org                   |
| Method     | 请求方式必须是指定方式         | - Method=GET,POST                                            |
| Path       | 请求路径必须符合指定规则       | - Path=/red/{segment},/blue/**                               |
| Query      | 请求参数必须包含指定参数       | - Query=name, Jack或者-  Query=name                          |
| RemoteAddr | 请求者的ip必须是指定范围       | - RemoteAddr=192.168.1.1/24                                  |
| Weight     | 权重处理                       |                                                              |

我们只需要掌握Path这种路由工程就可以了。

#### 4. 过滤器工厂

GatewayFilter是网关中提供的一种过滤器，可以对进入网关的请求和微服务返回的响应做处理：

![image-20210714212312871](E:\video\微服务开发框架SpringCloud+RabbitMQ+Docker+Redis+搜索+分布式史上最全面的微服务全技术栈课程\实用篇\学习资料\day02-SpringCloud02\讲义\assets\image-20210714212312871.png)

##### I 路由过滤器的种类

Spring提供了31种不同的路由过滤器工厂。例如：

| **名称**             | **说明**                     |
| -------------------- | ---------------------------- |
| AddRequestHeader     | 给当前请求添加一个请求头     |
| RemoveRequestHeader  | 移除请求中的一个请求头       |
| AddResponseHeader    | 给响应结果中添加一个响应头   |
| RemoveResponseHeader | 从响应结果中移除有一个响应头 |
| RequestRateLimiter   | 限制请求的流量               |

##### II 请求头过滤器

下面我们以AddRequestHeader 为例来讲解。

> **需求**：给所有进入userservice的请求添加一个请求头：Truth=itcast is freaking awesome!

只需要修改gateway服务的application.yml文件，添加路由过滤即可：

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: user-service 
        uri: lb://userservice 
        predicates: 
        - Path=/user/** 
        filters: # 过滤器
        - AddRequestHeader=Truth, Itcast is freaking awesome! # 添加请求头
```

当前过滤器写在userservice路由下，因此仅仅对访问userservice的请求有效。

##### III 默认过滤器

如果要对所有的路由都生效，则可以将过滤器工厂写到default下。格式如下：

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: user-service 
        uri: lb://userservice 
        predicates: 
        - Path=/user/**
      default-filters: # 默认过滤项
      - AddRequestHeader=Truth, Itcast is freaking awesome! 
```

##### IV 总结

过滤器的作用是什么？

① 对路由的请求或响应做加工处理，比如添加请求头

② 配置在路由下的过滤器只对当前路由的请求生效

defaultFilters的作用是什么？

① 对所有路由都生效的过滤器

#### 5. 全局过滤器

上一节学习的过滤器，网关提供了31种，但每一种过滤器的作用都是固定的。如果我们希望拦截请求，做自己的业务逻辑则没办法实现。

##### I 全局过滤器作用

全局过滤器的作用也是处理一切进入网关的请求和微服务响应，与GatewayFilter的作用一样。区别在于GatewayFilter通过配置定义，处理逻辑是固定的；而GlobalFilter的逻辑需要自己写代码实现。

定义方式是实现GlobalFilter接口。

```java
public interface GlobalFilter {
    /**
     *  处理当前请求，有必要的话通过{@link GatewayFilterChain}将请求交给下一个过滤器处理
     *
     * @param exchange 请求上下文，里面可以获取Request、Response等信息
     * @param chain 用来把请求委托给下一个过滤器 
     * @return {@code Mono<Void>} 返回标示当前过滤器业务结束
     */
    Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain);
}
```

在filter中编写自定义逻辑，可以实现下列功能：

- 登录状态判断
- 权限校验
- 请求限流等

##### II 自定义全局过滤器

需求：定义全局过滤器，拦截请求，判断请求的参数是否满足下面条件：

- 参数中是否有authorization，

- authorization参数值是否为admin

如果同时满足则放行，否则拦截

实现：

在gateway中定义一个过滤器：

```java
package cn.itcast.gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Order(-1)
@Component
public class AuthorizeFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.获取请求参数
        MultiValueMap<String, String> params = exchange.getRequest().getQueryParams();
        // 2.获取authorization参数
        String auth = params.getFirst("authorization");
        // 3.校验
        if ("admin".equals(auth)) {
            // 放行
            return chain.filter(exchange);
        }
        // 4.拦截
        // 4.1.禁止访问，设置状态码
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        // 4.2.结束处理
        return exchange.getResponse().setComplete();
    }
}
```

##### III 过滤器执行顺序

请求进入网关会碰到三类过滤器：当前路由的过滤器、DefaultFilter、GlobalFilter

请求路由后，会将当前路由过滤器和DefaultFilter、GlobalFilter，合并到一个过滤器链（集合）中，排序后依次执行每个过滤器：

![image-20210714214228409](E:\video\微服务开发框架SpringCloud+RabbitMQ+Docker+Redis+搜索+分布式史上最全面的微服务全技术栈课程\实用篇\学习资料\day02-SpringCloud02\讲义\assets\image-20210714214228409.png)



排序的规则是什么呢？

- 每一个过滤器都必须指定一个int类型的order值，**order值越小，优先级越高，执行顺序越靠前**。
- GlobalFilter通过实现Ordered接口，或者添加@Order注解来指定order值，由我们自己指定
- 路由过滤器和defaultFilter的order由Spring指定，默认是按照声明顺序从1递增。
- 当过滤器的order值一样时，会按照 defaultFilter > 路由过滤器 > GlobalFilter的顺序执行。

详细内容，可以查看源码：

`org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator#getFilters()`方法是先加载defaultFilters，然后再加载某个route的filters，然后合并。

`org.springframework.cloud.gateway.handler.FilteringWebHandler#handle()`方法会加载全局过滤器，与前面的过滤器合并后根据order排序，组织过滤器链

#### 6. 跨域问题

##### I 什么是跨域问题

跨域：域名不一致就是跨域，主要包括：

- 域名不同： www.taobao.com 和 www.taobao.org 和 www.jd.com 和 miaosha.jd.com

- 域名相同，端口不同：localhost:8080和localhost8081

跨域问题：浏览器禁止请求的发起者与服务端发生跨域ajax请求，请求被浏览器拦截的问题

解决方案：CORS，这个以前应该学习过，这里不再赘述了。不知道的小伙伴可以查看https://www.ruanyifeng.com/blog/2016/04/cors.html

##### II 跨域问题

找到课前资料的页面文件：

![image-20210714215713563](E:\video\微服务开发框架SpringCloud+RabbitMQ+Docker+Redis+搜索+分布式史上最全面的微服务全技术栈课程\实用篇\学习资料\day02-SpringCloud02\讲义\assets\image-20210714215713563.png)

放入tomcat或者nginx这样的web服务器中，启动并访问。

可以在浏览器控制台看到下面的错误：

![image-20210714215832675](E:\video\微服务开发框架SpringCloud+RabbitMQ+Docker+Redis+搜索+分布式史上最全面的微服务全技术栈课程\实用篇\学习资料\day02-SpringCloud02\讲义\assets\image-20210714215832675.png)

从localhost:8090访问localhost:10010，端口不同，显然是跨域的请求。

##### III 解决跨域问题

在gateway服务的application.yml文件中，添加下面的配置：

```yaml
spring:
  cloud:
    gateway:
      # 。。。
      globalcors: # 全局的跨域处理
        add-to-simple-url-handler-mapping: true # 解决options请求被拦截问题
        corsConfigurations:
          '[/**]':
            allowedOrigins: # 允许哪些网站的跨域请求 
              #- “*” # 允许所有的网站
              - "http://localhost:8090"
            allowedMethods: # 允许的跨域ajax的请求方式
              - "GET"
              - "POST"
              - "DELETE"
              - "PUT"
              - "OPTIONS"
            allowedHeaders: "*" # 允许在请求中携带的头信息
            allowCredentials: true # 是否允许携带cookie
            maxAge: 360000 # 这次跨域检测的有效期
```

### 6）Redis

```xml
<!-- 整合 redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### 7）MySQL

创建用户 bysj，将 bysj_user、bysj_db、bysj_message 这三张表的权限给他

### 8）message-service

#### 1. 引入 Spring Security

> 通过自带的登录界面，完成登录功能

```xml
<!-- SpringSecurity -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<!-- redis -->
<dependency>
	<groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<!-- jwt 依赖 -->
<dependency>
	<groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.0</version>
</dependency>
<!-- fastjson 依赖 -->
<dependency>
	<groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.33</version>
</dependency>
```

##### I 数据库校验用户

​	从之前的分析，我们可以知道：可以自定义一个 UserDetailService 让 SpringSecurity 使用我们的 UserDetailService，去数据库中查询用户名和密码。

```java
package cn.cetasas.message.service.impl;

import cn.cetasas.message.exception.BusinessException;
import cn.cetasas.message.exception.BusinessExceptionCode;
import cn.cetasas.message.mapper.UserMapper;
import cn.cetasas.message.pojo.User;
import cn.cetasas.message.pojo.UserExample;
import cn.cetasas.message.pojo.impl.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final static Logger LOG = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Resource
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LOG.info("正在向数据库中查询用户【{}】", username);
        // 查询用户信息
        UserExample example = new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andUsernameEqualTo(username);
        List<User> users = userMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(users)) {
            throw new BusinessException(BusinessExceptionCode.USERNAME_NOT_EXIST);
        }
        User user = users.get(0);
        //TODO 查询对应的权限信息

        // 把数据封装成 UserDetails 返回
        return new LoginUser(user);
    }
}
```

```java
package cn.cetasas.message.pojo.impl;

import cn.cetasas.message.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
@NoArgsConstructor  // 空参构造
@AllArgsConstructor // 有参构造
public class LoginUser implements UserDetails {

    private User user;

    // 获取权限信息
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

##### II 用户密码加密存储

​	实际项目中，我们不会把密码明文存储在数据库中。

​	默认使用的PasswordEncoder要求数据库中的密码格式为：{id}password。它会根据id去判断密码的加密方式。但是我们一般不会采用这种方式。所以就需要替换PasswordEncoder。

​	我们一般使用SpringSecurity为我们提供的BCryptPasswordEncoder。

​	我们只需要使用把BCryptPasswordEncoder对象注入到Spring容器中，SpringSecurity就会使用该PasswordEncoder来进行密码校验。

​	我们可以定义一个SpringSecurity的配置类，SpringSecurity要求这个配置类要继承WebSecurityConfigurerAdapter

```java
package cn.cetasas.message.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    // 创建BCryptPasswordEncoder注入容器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

> 这个加密后的密文实在是太长了，所以，本项目中不采用该加密方法 => 使用默认的加密形

​	所以数据库中 admin 的密码应该为明文

![image-20230508220249319](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230508220249319.png)

#### 2. 引入之后，默认的登陆界面

![image-20230508033246333](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230508033246333.png)

> 太麻烦了，选择自定义登录界面，放弃 SpringSecurity => 采用默认的系统配置

#### 3. 消息管理表设计

```sql
-- 消息表
--  添加消息时，给所有在线的用户发送消息
DROP TABLE IF EXISTS `message`;
CREATE TABLE IF NOT EXISTS `message` (
  `id` BIGINT NOT NULL COMMENT '消息ID',
  `title` VARCHAR(50) NOT NULL COMMENT '标题',
  `content` MEDIUMTEXT NOT NULL COMMENT '内容', -- 富文本 -> html, 媒体文本
  `createtime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '消息表';

-- 是否已读 => 记录表 ==> 存在 用户ID 则代表已读取，没有则代表该用户未读
DROP TABLE IF EXISTS `record`;
CREATE TABLE IF NOT EXISTS `record` (
  `id` BIGINT NOT NULL COMMENT '消息ID',
  `userid` INT NOT NULL COMMENT '用户ID', -- 来自 bysj_user 库的 user 表，由于两张表不在同一个库中，所以这里不能用外键索引
  FOREIGN KEY (`id`) REFERENCES `message`(`id`),
  PRIMARY KEY (`id`, `userid`) -- 联合主键索引
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '记录表';

-- 用户表 => 这里就专指管理员
--  本系统不会提供有关该表的相关操作 => 只有一个管理员 admin, admin
DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `username` VARCHAR(10) NOT NULL COMMENT '用户名',
  `password` VARCHAR(20) NOT NULL COMMENT '密码',
  PRIMARY KEY (`username`)
) ENGINE = Innodb
  DEFAULT
  CHARSET = utf8mb4 COMMENT = '管理员表';

INSERT INTO `user`(`username`, `password`)
VALUES ('admin', 'admin');
```

#### 4. 引入 Thymeleaf

> [(6条消息) SpringBoot引入Thymeleaf_柠檬茶ViTa的博客-CSDN博客](https://blog.csdn.net/youyuc/article/details/88999733)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

##### I 发布消息页面



##### II 管理消息



#### 5. 引入WebSocket发送消息

##### I 依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

##### II 与服务端建立链接





## 十、遇到的bug

### 1）父子项目之间依赖问题

dependencyManagement标签里面的依赖，子模块是不会自动使用父模块的jar包，子模块要使用的话，就要给出groupid和artifactid，无需给出version

dependencies，子模块会自动使用父模块中的jar包

### 2）@Autowired、@Qualifier和@Resource的区别

1. @Autowired 根据类型 type 注入
2. @Qualifier("beanName") 一般作为 @Autowired 修饰使用
3. @Resource 默认根据名字 name 注入，其次按照类型搜索，也可以通过 name 和 type 属性进行选择性注入
4. 一般 @Autowired 和 @Qualifier 一起用，@Resource 单独用

### 3）@RequestBody、@RequestParam、@PathVariable和@PathParam

#### 1. @RequestBody

@RequestBody 主要用来接收前端传给后端的 json 字符串中的数据（<font color='crimson'>请求体</font>中的数据）

<font color='crimson'>而 Get 方式无请求体，所以使用 @RequestBody 接受收据时，前端不能使用 Get 方式提交数据，而是使用 Post 方式提交的。</font>在后端的同一个接受方法里，@RequestBody 与 @RequestParam 可以同时使用，而 @RequestBody 最多只能有一个，@RequestParam 可以有多个

#### 2. @RequestParam

@RequestParam 用于将请求参数区域的数据映射到控制层能处理方法的参数上

```
@RequestParam(value="参数名",required="true/false",defaultValue="")
```

defaultValue：如果本次请求没有携带这个参数，或者参数为空，那么就会启用默认值

name：绑定本次参数的名称，要跟 URL 上面的一样

required：这个参数是不是必须得

value：跟 name 一样的作用，是 name 属性的一个别名

#### 3. @PathVariable

> 接收请求路径中占位符的值

@PathVariable 注解可以将 URL 中占位符参数绑定到控制器处理方法的入参中；URL 中的 {XXX} 占位符可以通过 @PathVariable("XXX") 绑定到操作方法的入参中

#### 4. @PathParam

作用和@RequestParam一样

### 4）IDEA中的HTTPClient有关Post请求

```http
### Send POST request with json body
POST https://httpbin.org/post
Content-Type: application/json

{
  "id": 999,
  "value": "content"
}

### Send POST request with body as parameters
POST https://httpbin.org/post
Content-Type: application/x-www-form-urlencoded

id=999&value=content

### Send a form with the text and file fields
POST https://httpbin.org/post
Content-Type: multipart/form-data; boundary=WebAppBoundary

--WebAppBoundary
Content-Disposition: form-data; name="element-name"
Content-Type: text/plain

Name
--WebAppBoundary
Content-Disposition: form-data; name="data"; filename="data.json"
Content-Type: application/json

< ./request-form-data.json
--WebAppBoundary--

### Send request with dynamic variables in request's body
POST https://httpbin.org/post
Content-Type: application/json

{
  "id": {{$uuid}},
  "price": {{$randomInt}},
  "ts": {{$timestamp}},
  "value": "content"
}

###
```

### 5）MyBatis 中的 #{} 和 ${} 的区别

先看一段代码：

> <font color='crimson'>注：这段代码如果不是如下的搭配，就会报错</font>

```java
package cn.cetasas.user.mapper;

public interface AllMapper {
    int createTable(String tableName);
}
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="cn.cetasas.user.mapper.AllMapper">
    <update id="createTable" parameterType="java.lang.String" >
        CREATE TABLE IF NOT EXISTS `${tableName}` (
             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
             `name` varchar(50) COMMENT '名称',
             primary key (`id`)
        ) ENGINE = Innodb
          default
          CHARSET = utf8mb4 COMMENT = #{tableName};
    </update>
</mapper>
```

#### 1. #{}

#{} 解析为 SQL 时，会将形参的值取出，并自动给其添加引号。例如：当实参 username="Amy" 时，传入下 Mapper 映射文件后

```xml
<select id="findByName" parameterType="String" resultMap="studentResultMap">
	select * from user where username=#{value}
</select>
```

SQL将解析为

```sql
select * from user where username="Amy"
```

#### 2. ${}

${} 解析为 SQL 时，将形参变量的指直接取出，直接拼接显示在 SQL 中

例如：当实参 username="Amy" 时，传入下 Mapper 映射文件后

```xml
<select id="findByName" parameterType="String" resultMap="studentResultMap">
	select * from user where username=${value}
</select>
```

SQL将解析为

```sql
select * from user where username=Amy
```

显而该 SQL 无法正常执行，故需要在 mapper 映射文件中的 ${value} 前后手动添加引号，如下所示：

```xml
<select id="findByName" parameterType="String" resultMap="studentResultMap">
	select * from user where username='${value}'
</select>
```

SQL 将解析为

```sql
select * from user where username='Amy'
```

#### 3. SQL 注入

${} 方式是将形参和 SQL 语句直接拼接形成完整的 SQL 命令后，再进行编译，所以可以通过精心设计的形参变量的值，来改变原 SQL 语句的使用意图从而产生安全隐患，即为 SQL 注入攻击。现举例说明：

现有Mapper映射文件如下：

```xml
<select id="findByName" parameterType="String" resultMap="studentResultMap">
    SELECT * FROM user WHERE username='${value}'
</select>
```

当 username = "' OR 1=1 OR '" 传入后，${}将变量内容直接和SQL语句进行拼接，结果如下:

```sql
SELECT * FROM user WHERE username='' OR 1=1 OR ''
```

显而易见，上述语句将把整个数据库内容直接暴露出来了

\#{} 方式则是先用占位符代替参数将 SQL 语句先进行预编译，然后再将参数中的内容替换进来。由于 SQL 语句已经被预编译过，其 SQL 意图将无法通过非法的参数内容实现更改，其参数中的内容，无法变为 SQL 命令的一部分。故，**#{} 可以防止 SQL 注入而 ${} 却不行**

### 6）MySQL 中 update、delete和create的小细节

<font color='crimson'>update和delete会返回影响的行数，即返回值为int</font>

同时update和delete的返回值也是可以为boolean，当返回值为0时对应的boolean类型就是false, 如果不为零就是返回true

create无返回值

### 7）引入websocket导致test单元报错

```
org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'serverEndpointExporter' defined in class path resource [cn/cetasas/user/config/WebSocketConfig.class]: Invocation of init method failed; nested exception is java.lang.IllegalStateException: javax.websocket.server.ServerContainer not available
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1799) ~[spring-beans-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:594) ~[spring-beans-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:516) ~[spring-beans-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:324) ~[spring-beans-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[spring-beans-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:322) ~[spring-beans-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:202) ~[spring-beans-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:897) ~[spring-beans-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:879) ~[spring-context-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:551) ~[spring-context-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:758) ~[spring-boot-2.3.9.RELEASE.jar:2.3.9.RELEASE]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:750) ~[spring-boot-2.3.9.RELEASE.jar:2.3.9.RELEASE]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:405) ~[spring-boot-2.3.9.RELEASE.jar:2.3.9.RELEASE]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:315) ~[spring-boot-2.3.9.RELEASE.jar:2.3.9.RELEASE]
	at org.springframework.boot.test.context.SpringBootContextLoader.loadContext(SpringBootContextLoader.java:127) [spring-boot-test-2.1.4.RELEASE.jar:2.1.4.RELEASE]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContextInternal(DefaultCacheAwareContextLoaderDelegate.java:99) [spring-test-5.2.8.RELEASE.jar:5.2.8.RELEASE]
	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:124) [spring-test-5.2.8.RELEASE.jar:5.2.8.RELEASE]
	at org.springframework.test.context.support.DefaultTestContext.getApplicationContext(DefaultTestContext.java:123) [spring-test-5.2.8.RELEASE.jar:5.2.8.RELEASE]
	at org.springframework.test.context.web.ServletTestExecutionListener.setUpRequestContextIfNecessary(ServletTestExecutionListener.java:190) [spring-test-5.2.8.RELEASE.jar:5.2.8.RELEASE]
	at org.springframework.test.context.web.ServletTestExecutionListener.prepareTestInstance(ServletTestExecutionListener.java:132) [spring-test-5.2.8.RELEASE.jar:5.2.8.RELEASE]
	at org.springframework.test.context.TestContextManager.prepareTestInstance(TestContextManager.java:244) [spring-test-5.2.8.RELEASE.jar:5.2.8.RELEASE]
	at org.springframework.test.context.junit4.SpringJUnit4ClassRunner.createTest(SpringJUnit4ClassRunner.java:227) [spring-test-5.2.8.RELEASE.jar:5.2.8.RELEASE]
	at org.springframework.test.context.junit4.SpringJUnit4ClassRunner$1.runReflectiveCall(SpringJUnit4ClassRunner.java:289) [spring-test-5.2.8.RELEASE.jar:5.2.8.RELEASE]
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12) [junit-4.13.2.jar:4.13.2]
	at org.springframework.test.context.junit4.SpringJUnit4ClassRunner.methodBlock(SpringJUnit4ClassRunner.java:291) [spring-test-5.2.8.RELEASE.jar:5.2.8.RELEASE]
	at org.springframework.test.context.junit4.SpringJUnit4ClassRunner.runChild(SpringJUnit4ClassRunner.java:246) [spring-test-5.2.8.RELEASE.jar:5.2.8.RELEASE]
	at org.springframework.test.context.junit4.SpringJUnit4ClassRunner.runChild(SpringJUnit4ClassRunner.java:97) [spring-test-5.2.8.RELEASE.jar:5.2.8.RELEASE]
	at org.junit.runners.ParentRunner$4.run(ParentRunner.java:331) [junit-4.13.2.jar:4.13.2]
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:79) [junit-4.13.2.jar:4.13.2]
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:329) [junit-4.13.2.jar:4.13.2]
	at org.junit.runners.ParentRunner.access$100(ParentRunner.java:66) [junit-4.13.2.jar:4.13.2]
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:293) [junit-4.13.2.jar:4.13.2]
	at org.springframework.test.context.junit4.statements.RunBeforeTestClassCallbacks.evaluate(RunBeforeTestClassCallbacks.java:61) [spring-test-5.2.8.RELEASE.jar:5.2.8.RELEASE]
	at org.springframework.test.context.junit4.statements.RunAfterTestClassCallbacks.evaluate(RunAfterTestClassCallbacks.java:70) [spring-test-5.2.8.RELEASE.jar:5.2.8.RELEASE]
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306) [junit-4.13.2.jar:4.13.2]
	at org.junit.runners.ParentRunner.run(ParentRunner.java:413) [junit-4.13.2.jar:4.13.2]
	at org.springframework.test.context.junit4.SpringJUnit4ClassRunner.run(SpringJUnit4ClassRunner.java:190) [spring-test-5.2.8.RELEASE.jar:5.2.8.RELEASE]
	at org.junit.runner.JUnitCore.run(JUnitCore.java:137) [junit-4.13.2.jar:4.13.2]
	at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:69) [junit-rt.jar:na]
	at com.intellij.rt.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:33) [junit-rt.jar:na]
	at com.intellij.rt.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:221) [junit-rt.jar:na]
	at com.intellij.rt.junit.JUnitStarter.main(JUnitStarter.java:54) [junit-rt.jar:na]
```

原因就是这个注解：

![image-20230330182655814](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230330182655814.png)

解决方法一：将@RunWith(SpringRunner.class)去掉即可，但这种方式会有局限，比如下方你要@Autowired一个类就会报错

![image-20230330182819643](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230330182819643.png)

解决方法二：在SpringBootTest后面加上

```
(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
```

即可，原因是：websocket是需要依赖tomcat等容器的启动，所以在测试过程中我们要真正的启动一个tomcat作为容器

![image-20230330183053785](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230330183053785.png)

### 8）启用Nacos后，RestTemplate报错

> 原因是：启用Nacos后，原本的 ip + url 的形式就不能使用了，resttemplate 会将服务进行负载均衡 => 通过 服务名 +  url 的形式
>
> [(1条消息) Spring Cloud RestTemplate @LoadBalanced 支持ip、域名、服务名 调用_loadbalanced+支持ip调用_baozhutang的博客-CSDN博客](https://blog.csdn.net/baozhutang/article/details/90236887)

```
java.lang.IllegalStateException: No instances available for data.stats.gov.cn
	at org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient.execute(RibbonLoadBalancerClient.java:119) ~[spring-cloud-netflix-ribbon-2.2.7.RELEASE.jar:2.2.7.RELEASE]
	at org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient.execute(RibbonLoadBalancerClient.java:99) ~[spring-cloud-netflix-ribbon-2.2.7.RELEASE.jar:2.2.7.RELEASE]
	at org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor.intercept(LoadBalancerInterceptor.java:58) ~[spring-cloud-commons-2.2.7.RELEASE.jar:2.2.7.RELEASE]
	at org.springframework.http.client.InterceptingClientHttpRequest$InterceptingRequestExecution.execute(InterceptingClientHttpRequest.java:93) ~[spring-web-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.http.client.InterceptingClientHttpRequest.executeInternal(InterceptingClientHttpRequest.java:77) ~[spring-web-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.http.client.AbstractBufferingClientHttpRequest.executeInternal(AbstractBufferingClientHttpRequest.java:48) ~[spring-web-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.http.client.AbstractClientHttpRequest.execute(AbstractClientHttpRequest.java:53) ~[spring-web-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:737) ~[spring-web-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.web.client.RestTemplate.execute(RestTemplate.java:693) ~[spring-web-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.web.client.RestTemplate.getForObject(RestTemplate.java:322) ~[spring-web-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at cn.cetasas.db.util.CarbonEmissionStatistics.getOriginalCarbonEmissionStatistics(CarbonEmissionStatistics.java:79) ~[classes/:na]
	at cn.cetasas.db.util.CarbonEmissionStatistics.getTotalCarbonEmissionStatistics(CarbonEmissionStatistics.java:170) ~[classes/:na]
	at cn.cetasas.db.web.TestController.getTotal(TestController.java:84) ~[classes/:na]
	at cn.cetasas.db.web.TestController$$FastClassBySpringCGLIB$$cae0903d.invoke(<generated>) ~[classes/:na]
	at org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:218) ~[spring-core-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:771) ~[spring-aop-5.2.12.RELEASE.jar:5.2.12.RELEASE]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-5.2.12.RELEASE.jar:5.2.12.RELEASE]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:749) ~[spring-aop-5.2.12.RELEASE.jar:5.2.12.RELEASE]
	at org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:56) ~[spring-aop-5.2.12.RELEASE.jar:5.2.12.RELEASE]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.2.12.RELEASE.jar:5.2.12.RELEASE]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:749) ~[spring-aop-5.2.12.RELEASE.jar:5.2.12.RELEASE]
	at org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint.proceed(MethodInvocationProceedingJoinPoint.java:88) ~[spring-aop-5.2.12.RELEASE.jar:5.2.12.RELEASE]
	at cn.cetasas.db.aspect.LogAspect.doAround(LogAspect.java:85) ~[classes/:na]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0_131]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0_131]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0_131]
	at java.lang.reflect.Method.invoke(Method.java:498) ~[na:1.8.0_131]
	at org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethodWithGivenArgs(AbstractAspectJAdvice.java:644) ~[spring-aop-5.2.12.RELEASE.jar:5.2.12.RELEASE]
	at org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethod(AbstractAspectJAdvice.java:633) ~[spring-aop-5.2.12.RELEASE.jar:5.2.12.RELEASE]
	at org.springframework.aop.aspectj.AspectJAroundAdvice.invoke(AspectJAroundAdvice.java:70) ~[spring-aop-5.2.12.RELEASE.jar:5.2.12.RELEASE]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.2.12.RELEASE.jar:5.2.12.RELEASE]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:749) ~[spring-aop-5.2.12.RELEASE.jar:5.2.12.RELEASE]
	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:95) ~[spring-aop-5.2.12.RELEASE.jar:5.2.12.RELEASE]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.2.12.RELEASE.jar:5.2.12.RELEASE]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:749) ~[spring-aop-5.2.12.RELEASE.jar:5.2.12.RELEASE]
	at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:691) ~[spring-aop-5.2.12.RELEASE.jar:5.2.12.RELEASE]
	at cn.cetasas.db.web.TestController$$EnhancerBySpringCGLIB$$70c73c0e.getTotal(<generated>) ~[classes/:na]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0_131]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0_131]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0_131]
	at java.lang.reflect.Method.invoke(Method.java:498) ~[na:1.8.0_131]
	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:190) ~[spring-web-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:138) ~[spring-web-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:105) ~[spring-webmvc-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:878) ~[spring-webmvc-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:792) ~[spring-webmvc-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87) ~[spring-webmvc-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1040) ~[spring-webmvc-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:943) ~[spring-webmvc-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1006) ~[spring-webmvc-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:898) ~[spring-webmvc-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:626) ~[tomcat-embed-core-9.0.43.jar:4.0.FR]
	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:883) ~[spring-webmvc-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:733) ~[tomcat-embed-core-9.0.43.jar:4.0.FR]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:227) ~[tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162) ~[tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53) [tomcat-embed-websocket-9.0.43.jar:9.0.43]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189) ~[tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162) ~[tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100) [spring-web-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119) [spring-web-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189) ~[tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162) ~[tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93) [spring-web-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119) [spring-web-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189) ~[tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162) ~[tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201) [spring-web-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119) [spring-web-5.2.13.RELEASE.jar:5.2.13.RELEASE]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189) ~[tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162) ~[tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:202) [tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:97) [tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:542) [tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:143) [tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:92) [tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:78) [tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:346) [tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:374) [tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:65) [tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:887) [tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1684) [tomcat-embed-core-9.0.43.jar:9.0.43]
	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49) [tomcat-embed-core-9.0.43.jar:9.0.43]
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142) [na:1.8.0_131]
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617) [na:1.8.0_131]
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61) [tomcat-embed-core-9.0.43.jar:9.0.43]
	at java.lang.Thread.run(Thread.java:748) [na:1.8.0_131]

04-02 23:14:35:732  INFO 16220 --- [nio-8080-exec-1] cn.cetasas.db.aspect.LogAspect           : 返回结果: {"message":"系统出现异常，请联系管理员","success":false}
04-02 23:14:35:732  INFO 16220 --- [nio-8080-exec-1] cn.cetasas.db.aspect.LogAspect           : ------------- 结束 耗时：14 ms -------------
```

解决方法：

![image-20230402232231962](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230402232231962.png)

### 9）前端npm install出现bug

![image-20230418001615756](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230418001615756.png)

**解决办法：**

> [(3条消息) error An unexpected error occurred: “https://registry.nlark.com/date-fns/download/date-fns-2.23.0.tg_date-fns 下载不下来_一个专注写代码的程序媛的博客-CSDN博客](https://blog.csdn.net/linanran1027/article/details/120346208)

### 10）IDEA 小技巧 —— 代码格式化

```
ctrl + a // 全选
ctrl + alt + l // 代码格式化
```

### 11）父子组件初始化顺序

``` 
父beforeCreate->父created->父beforeMount->子beforeCreate->子created->子beforeMount->子mounted->父mounted
```

​	所以，在父组件的mounted事件中去初始化的值，不会同步到子组件去，导致子组件对应属性为null

### 12）验证码注册到 redis 的 bug

> [解决vue项目axios每次请求session不一致的问题_vue.js (ay1.cc)](http://www.ay1.cc/article/27800.html)
>
> [(6条消息) 解决Axios跨域请求时session不一致_axios请求 request cookies 和 response cookies中的sessio_hezebin的博客-CSDN博客](https://blog.csdn.net/MrKorbin/article/details/104069236)
>
> [(6条消息) 解决SpringCloud+gateway的session不一致问题。_gateway session不一致_卑微的新手的博客-CSDN博客](https://blog.csdn.net/qq_46060404/article/details/122413989)

​	在本项目中，采用向用户邮箱发送验证码实现注册、修改密码等功能

​	同时，在保存用户此次的验证码是通过将验证码信息放入 session 中，而前端的 axios 框架默认每次请求都是不同的 session，所以导致我们的项目在验证验证码的时候拿不到此次验证码，导致用户始终无法登录成功

​	所以我们需要在前端给 axios 添加一些配置，让其保证每次请求都是同一个 session（在 main.js 中添加如下配置）

```js
axios.defaults.withCredentials = true; //意思是携带cookie信息,保持session的一致性
```

----------有问题---------

```java
package cn.cetasas.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 解决跨域请求问题
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
                // 映射的请求地址，/** 表示针对所有的请求地址
        registry.addMapping(("/**"))
                .allowedOrigins("*")
                .allowedHeaders(CorsConfiguration.ALL)
                // GET、POST、DELETE...
                .allowedMethods(CorsConfiguration.ALL)
                // 允许前端带上它的凭证，如 cookie，作用 session 校验
                .allowCredentials(true)
                // 在调用接口之前，（前端会偷偷地）发一个OPTIONS请求
                // 检查这个地址是否存在，再发送GET请求
                .maxAge(3600);  // 1小时内不需要再预见（发OPTIONS请求）
    }
}
```

### 13）将 key 存入 redis 中的乱码问题

> [(6条消息) 解决 redis 的 key 出现的 \xac\xed\x00\x05t\x00 乱码问题_林志鹏JAVA的博客-CSDN博客](https://blog.csdn.net/m4330187/article/details/108091447)

![image-20230505161931496](https://raw.githubusercontent.com/KingKingLin/carbon-emission-system/master/img/image-20230505161931496.png)
