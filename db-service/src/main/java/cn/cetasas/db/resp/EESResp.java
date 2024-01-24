package cn.cetasas.db.resp;

import lombok.Data;

@Data
public class EESResp {
    private String reg;

    private String zb;

    private double value; // 碳排放量
}
