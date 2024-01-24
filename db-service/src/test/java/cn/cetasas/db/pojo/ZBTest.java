package cn.cetasas.db.pojo;

public class ZBTest {
    public double value;

    public ZBTest() {}

    public ZBTest(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "{" +
                "value=" + value +
                '}';
    }
}
