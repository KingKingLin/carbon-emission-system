package cn.cetasas.db.service;

import cn.cetasas.db.exception.BusinessException;
import cn.cetasas.db.exception.BusinessExceptionCode;
import cn.cetasas.db.mapper.TableMapper;
import cn.cetasas.db.util.TableSuffix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TableService {

    private final static Logger LOG = LoggerFactory.getLogger(TableService.class);

    @Value("${dbName}")
    private String dbName;

    @Resource
    private TableMapper tableMapper;

    /**
     * @param tableName 表名
     * @return true 存在； false 不存在
     */
    private boolean isExitTable(String tableName) {
        return tableMapper.isExitTable(dbName, tableName) == 1;
    }

    /**
     * 判断碳排放表是否存在
     * @param year 年份
     * @return true 存在；false 不存在
     */
    public boolean isExitCESTable(String year) {
        return isExitTable(year + TableSuffix.CES);
    }

    /**
     * 判断能源排放表是否存在
     * @param year 年份
     * @return true 存在；false 不存在
     */
    public boolean isExitEESTable(String year) {
        return isExitTable(year + TableSuffix.EES);
    }

    /**
     * 判断总表是否存在
     * @param year 年份
     * @return true 存在；false 不存在
     */
    public boolean isExitTotalTable(String year) {
        return isExitTable(year + TableSuffix.TOTAL);
    }

    /**
     * 判断碳排放指标代码表EES部分是否初始化
     * @return true 已经初始化；false 还未初始化
     */
    public boolean isInitECodeEES() {
        return tableMapper.isInit("e_code_ees");
    }

    /**
     * 判断碳排放指标代码表TOTAL部分是否初始化
     * @return true 已经初始化；false 还未初始化
     */
    public boolean isInitECodeTotal() {
        return tableMapper.isInit("e_code_total");
    }

    /**
     * 判断地区代码表是否初始化
     * @return true 已经初始化；false 还未初始化
     */
    public boolean isInitRCode() {
        return tableMapper.isInit("r_code");
    }

    /**
     * 判断碳排放系数表是否存在
     * @return true 存在；false 不存在
     */
    public boolean isExitCEFTable() {
        return isExitTable("cef");
    }

    public void createCESTable(String year) {
        createTable(year, TableSuffix.CES);
    }

    public void createEESTable(String year) {
        createTable(year, TableSuffix.EES);
    }

    public void createTotalTable(String year) {
        createTable(year, TableSuffix.TOTAL);
    }

    private void createTable(String year, String suffix) {
        String tableName = year + suffix;

        if (isExitTable(tableName)) {
            LOG.warn("表【{}】已存在", tableName);
            throw new BusinessException(BusinessExceptionCode.TABLE_IS_EXIT);
        }

        LOG.info("正在创建表【{}】...", tableName);
        switch (suffix) {
            case TableSuffix.CES:
                tableMapper.createCESTable(tableName);
                break;
            case TableSuffix.EES:
                tableMapper.createEESTable(tableName);
                break;
            case TableSuffix.TOTAL:
                tableMapper.createTotalTable(tableName);
                break;
        }
        LOG.info("表【{}】创建成功!!!", tableName);
    }

    public void updateInitRCode() {
        tableMapper.updateInit("r_code");
    }

    public void updateInitECodeEES() {
        tableMapper.updateInit("e_code_ees");
    }

    public void updateInitECodeTotal() {
        tableMapper.updateInit("e_code_total");
    }
}
