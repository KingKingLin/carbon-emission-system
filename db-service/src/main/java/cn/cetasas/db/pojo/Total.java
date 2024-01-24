package cn.cetasas.db.pojo;

import lombok.Data;

@Data
public class Total {

    private String eCode;

    private String zb; // 指标的名称

    private double value;
}