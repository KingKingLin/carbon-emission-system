package cn.cetasas.db.resp;

import lombok.Data;

@Data
public class TotalResp {
    private String zb; // 指标的名称

    private double value;
}