package cn.cetasas.db.pojo;

import lombok.Data;

@Data
public class EES {

    private String rCode;

    private String eCode;

    private String reg;

    private String zb;

    private double value; // 碳排放量

    private double cef; // 碳排放系数
}
