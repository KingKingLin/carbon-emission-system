package cn.cetasas.db.req;

import java.util.HashMap;

public class QueryReq extends HashMap<String, String> {
    public QueryReq() {
        this.put("rowcode", "zb");
        this.put("colcode", "sj");
        this.put("k1", "" + System.nanoTime());
    }
}