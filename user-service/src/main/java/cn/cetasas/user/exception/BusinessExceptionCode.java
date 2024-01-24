package cn.cetasas.user.exception;

public enum BusinessExceptionCode {

    TEST("test"),
    LOGIN_USER_ERROR("用户名不存在或密码错误"),
    USER_NAME_EXIST("该用户名已经存在"),
    USER_MAIL_EXIST("该邮箱已被注册"),
    MAIL_CAN_NOT_BE_NAME("该昵称无法用作用户名"),
    VERIFICATION_OUT_OF_TIME("验证码已失效"),
    VERIFICATION_NOT_MATCH("验证码错误"),
    SEND_MAIL_ERROR("发送邮件失败"),
    MAIL_NOT_ENROLL("该邮箱还未注册")
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
