package cn.cetasas.message.exception;

public enum BusinessExceptionCode {

    TEST("test"),
    LOGIN_ERROR("用户名或密码错误"),
    NOT_SUCH_SERVER("没有这个服务"),
    IMAGE_NOT_EXIST("该图片不存在"),
    VIDEO_NOT_EXIST("该视频不存在"),
    DOWNLOAD_IMAGE_FAILED("下载图片失败"),
    DOWNLOAD_VIDEO_FAILED("下载视频失败"),
    UPLOAD_IMAGE_FAILED("上传图片失败"),
    UPLOAD_VIDEO_FAILED("上传视频失败")
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
