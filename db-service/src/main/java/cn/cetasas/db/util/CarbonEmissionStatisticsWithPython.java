//package cn.cetasas.db.util;
//
//import com.alibaba.fastjson.JSON;
//import org.springframework.util.ResourceUtils;
//
//import java.io.BufferedReader;
//import java.io.FileNotFoundException;
//import java.io.InputStreamReader;
//import java.io.UnsupportedEncodingException;
//import java.net.URLDecoder;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//public class CarbonEmissionStatisticsWithPython {
//    private static String realPath;
//
//    static {
//        try {
//            realPath = URLDecoder.decode(ResourceUtils.getURL("classpath:").getPath().substring(1), "utf-8");
//        } catch (FileNotFoundException | UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static Map<?, ?> getRegCode(String zb) {
//        Map<?, ?> res = null;
//        try {
//            System.out.println("start");
//            String[] args1=new String[]{"python", realPath + "python/reg_code.py", zb};
//            Process pr = Runtime.getRuntime().exec(args1);
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream(), "gbk"/*"GB2312"*/));
//            // String line;
//            // while ((line = in.readLine()) != null) {
//            //    System.out.println(line);
//            // }
//            // String json = IOUtils.toString(in); //当然, 请注意, 使用此机制, 可能会通过发送填满服务器内存的永无止境的数据流来进行拒绝服务攻击
//            String json = in.lines().collect(Collectors.joining());
//            // 处理json字符串
//            // json = json.replaceAll("(?:'| )", "\""); // 适用于一次性替换不同字符
//            json = json.replaceAll("'", "\"").replaceAll(" ", "").replaceAll("True", "true").replaceAll("False", "false");
//            res = JSON.parseObject(json, Map.class);
//            in.close();
//            int i = pr.waitFor();
//            System.out.println(i == 0 ? "调用python脚本成功!" : "调用python脚本失败!");
//            System.out.println("end");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return res;
//    }
//}
