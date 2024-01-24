package cn.cetasas.db.pojo;

import java.util.HashMap;

public class StandardData<T> {

    private HashMap<String, ECode> zb;

    private CESData<T> data;

    public HashMap<String, ECode> getZb() {
        return zb;
    }

    public void setZb(HashMap<String, ECode> zb) {
        this.zb = zb;
    }

    public CESData<T> getData() {
        return data;
    }

    public void setData(CESData<T> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "StandardData{" +
                "zb=" + zb +
                ", data=" + data +
                '}';
    }
}
