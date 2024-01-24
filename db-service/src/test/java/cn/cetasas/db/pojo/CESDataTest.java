package cn.cetasas.db.pojo;

import java.util.HashMap;

public class CESDataTest extends HashMap<String, SJTest> {
    private CESDataTest() {}

    private static class Singleton {
        private static final CESDataTest treeDataTest = new CESDataTest();
    }

    public static CESDataTest getInstance() {
        return Singleton.treeDataTest;
    }
}
