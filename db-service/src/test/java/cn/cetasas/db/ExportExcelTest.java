package cn.cetasas.db;

import cn.cetasas.db.pojo.CES;
import cn.cetasas.db.pojo.EES;
import cn.cetasas.db.resp.CESResp;
import cn.cetasas.db.service.CESService;
import cn.cetasas.db.service.EESService;
import cn.cetasas.db.util.CopyUtil;
import cn.cetasas.db.util.ExportExcel;
import org.apache.poi.ss.usermodel.Row;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest()
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExportExcelTest {

    @Resource
    private CESService cesService;

    @Test
    public void testCES() {
        List<CES> list = cesService.getAllProvince("2019"); // 获取该年份的碳排放数据

        List<CESResp> list1 = CopyUtil.copyList(list, CESResp.class);

        System.out.println(list1);

        List<String> title = new ArrayList<>();
        title.add("省份");
        title.add("碳排放量");

//        ExportExcel.export(list1, title, "/ces", "2019年碳排放数据");
        String result = ExportExcel.exportPlus("/ces", "2019年碳排放数据", list1.size(),
                (row) -> {
                    for (int i = 0; i < title.size(); i++) {
                        row.createCell(i).setCellValue(title.get(i));
                    }
                },
                (row, i) -> {
                    CESResp resp = list1.get(i);
                    row.createCell(0).setCellValue(resp.getReg());
                    row.createCell(1).setCellValue(resp.getValue());
                });
        System.out.println(result);
    }

    @Resource
    private EESService eesService;

    @Test
    public void testEES() {
        // 判断该年份的碳排放数据 excel 表是否存在，如果存在则直接返回
        if (ExportExcel.exit("/ees", "20119年能源排放数据")) return;

        List<EES> list = eesService.getAllProvince("2019"); // 获取该年份的碳排放数据

        final Map<String, Map<String, Double>> map = cast(list); // [{r_code e_code reg zb value cef}] => {reg:{zb:value}}}
        final List<String> column = new ArrayList<>(map.keySet()); // ["北京市","天津市"...]
//        final List<String> title = new ArrayList<>(map.get("北京市").keySet());
        final List<String> title = new ArrayList<>(map.get(column.get(0)).keySet());
        String result = ExportExcel.exportPlus("/ees", "2019年能源排放数据", column.size(),
                (row) -> {
                    row.createCell(0).setCellValue("省份\\能源");
                    for (int i = 0; i < title.size(); i++) {
                        row.createCell(i + 1).setCellValue(title.get(i));
                    }
                },
                (row, i) -> {
                    String reg = column.get(i);
                    Map<String, Double> zbs = map.get(reg);
                    row.createCell(0).setCellValue(reg);
                    for (int i1 = 0; i1 < title.size(); i1++) {
                        String s = title.get(i1);
                        Double value = zbs.get(s);
                        row.createCell(i1 + 1).setCellValue(value);
                    }
                });
        System.out.println(result);
    }

    // 字符串开销大
    private Map<String, Map<String, Double>> cast(List<EES> list) {
        Map<String, Map<String, Double>> map = new HashMap<>();
        for (EES ees : list) {
            String reg = ees.getReg();
            String zb = ees.getZb();
            double value = ees.getValue();

            if (!map.containsKey(reg)) {
                map.put(reg, new HashMap<>());
            }
            map.get(reg).put(zb, value);
        }
        return map; // {reg:{zb:value}}
    }
}
