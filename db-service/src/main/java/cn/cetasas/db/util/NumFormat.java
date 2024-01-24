package cn.cetasas.db.util;

import java.text.NumberFormat;

// double保留两位小数
public class NumFormat {
    // 方法一
//    double f = 2.356;
//    BigDecimal bg = new BigDecimal(f);
//    double f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    // 方法二
    private final static NumberFormat nf = NumberFormat.getInstance();

    static {
        nf.setMaximumFractionDigits(2);
    }

    /**
     * 保留两位小数
     * @param d 数据
     * @return 四舍五入
     */
    public static double twoDecimal(double d) {
        return Double.parseDouble(nf.format(d));
    }
}
