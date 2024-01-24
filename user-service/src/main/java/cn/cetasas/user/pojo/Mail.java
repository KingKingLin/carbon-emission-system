package cn.cetasas.user.pojo;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

public class Mail {
    @NotEmpty(message = "【邮箱】不能为空")
    @Pattern(regexp = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$", message = "【邮箱】格式不正确")
    private String mail;

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    @Override
    public String toString() {
        return "Mail{" +
                "mail='" + mail + '\'' +
                '}';
    }
}
