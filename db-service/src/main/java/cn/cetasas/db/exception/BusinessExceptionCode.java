package cn.cetasas.db.exception;

public enum BusinessExceptionCode {

    CRAWLER_FALSE("获取数据失败[1]，请联系管理员！"),
    REFLECT_FIELD_ERROR("获取数据失败[2]，请联系管理员！"),
    TABLE_IS_EXIT("该表已存在！"),
    OUT_OF_DATE("请求的年份超出限额！"),
    NO_RELEVANT_DATA("暂无当前年份的相关数据！"),
    NULL_POINTER("传入空值！"),
    CREATE_FILE_FAILED("创建文件失败！"),
    EXPORT_FAILED("导出excel失败！"),
    DOWNLOAD_FILE_FAILED("下载文件失败！")
    ;

    private String desc;

    BusinessExceptionCode(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
