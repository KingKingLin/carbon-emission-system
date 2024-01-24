package cn.cetasas.db.pojo;

public class ZB {
    private double value;

    public ZB() {}

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public ZB(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "{" +
                "value=" + value +
                '}';
    }
}
