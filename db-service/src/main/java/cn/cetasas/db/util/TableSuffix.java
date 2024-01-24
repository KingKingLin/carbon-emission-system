package cn.cetasas.db.util;

import java.util.HashMap;
import java.util.Map;

public class TableSuffix {

    public final static String CES = "_ces";

    public final static String EES = "_ees";

    public final static String TOTAL = "_total";

    public static Map<String, String> tableMap;

    static {
        tableMap = new HashMap<>();
        tableMap.put("_ces", "省际碳排放数据");
        tableMap.put("_ees", "省际能源排放数据");
        tableMap.put("_total", "年度总能源排放数据");
    }
}
