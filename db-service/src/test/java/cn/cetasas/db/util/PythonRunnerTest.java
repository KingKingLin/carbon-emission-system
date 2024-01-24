package cn.cetasas.db.util;

import org.junit.Test;
//import org.python.util.PythonInterpreter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

// 需要Jpython的依赖
public class PythonRunnerTest {

    private final String realPath = System.getProperty("user.dir");

//    @Test
//    public void pythonRunnerTest1() {
//        System.out.println(realPath);
//
//        // python 解释器
//        PythonInterpreter interpreter = new PythonInterpreter();
//        // 1）选择执行的Python语句
//        System.out.print("执行Python语句:\n\t");
//        interpreter.exec("a='hello world'; ");
//        interpreter.exec("print a;");
//        // 2）选择执行的Python文件
//        System.out.print("执行Python脚本:\n\t");
////        interpreter.execfile("E:\\docs\\college\\毕业设计\\代码\\carbon-emission-system\\user-service\\src\\main\\resources\\python\\test.py");
//        interpreter.execfile(realPath + "/src/main/resources/python/test.py");
//    }

    @Test
    public void pythonRunnerTest2() {
        try {
            System.out.println("start");
            // 运行环境 和 python script
//            String[] args1=new String[]{"D:/anaconda3/python", realPath + "/src/main/resources/python/test.py"};
//            String[] args1=new String[]{"python", realPath + "/src/main/resources/python/test.py"};
            String[] args1=new String[]{"python", realPath + "/src/main/resources/python/reg_code.py", "A0706"};
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
//            System.out.println(realPath + "/src/main/resources/python/reg_code.py");
            String[] args1=new String[]{"python", realPath + "/src/main/resources/python/reg_code.py", "A0706"};
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
