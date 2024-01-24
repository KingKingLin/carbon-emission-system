package cn.cetasas.db.mapper;

public interface TableMapper {

    // 判断 e_code 和 r_code 是否初始化
    boolean isInit(String tableName);

    void updateInit(String tableName);

    int isExitTable(String dbName, String tableName);

    // 碳排放表
    void createCESTable(String tableName);

    // 能源排放表
    void createEESTable(String tableName);

    void createTotalTable(String tableName);
}