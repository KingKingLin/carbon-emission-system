package cn.cetasas.db.util;

import cn.cetasas.db.exception.BusinessException;
import cn.cetasas.db.exception.BusinessExceptionCode;
import org.springframework.util.StringUtils;

import java.util.Calendar;

public class DateRange {

//    public static final int min = 1990;
    public static final int min = 1995;

    public static final int max = 2019; // 国家统计局里暂时只有到 2019 年的数据

    public static boolean validate(String year) {
        if (StringUtils.isEmpty(year)) {
            throw new BusinessException(BusinessExceptionCode.NULL_POINTER);
        }

        int now = Calendar.getInstance().get(Calendar.YEAR); // 现在的日期
        int y = Integer.parseInt(year);

        if (y > max && y <= now) {
            throw new BusinessException(BusinessExceptionCode.NO_RELEVANT_DATA);
        }

        return y < min || y > max; // true 的范围
    }
}
