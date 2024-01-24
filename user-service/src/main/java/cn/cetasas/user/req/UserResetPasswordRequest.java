package cn.cetasas.user.req;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class UserResetPasswordRequest {
    @NotNull
    private Integer id; // primary key

    /**
     * 邮箱和密码是不允许修改的
     */
//    @NotNull(message = "【邮箱】不能为空")
//    @Pattern(regexp = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$", message = "【邮箱】格式不正确")
//    private String mail;
//    private String name;

    @NotEmpty(message = "【密码】不能为空")
    private String password;

//    @NotEmpty(message = "【验证码】不能为空")
//    private String verification;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", password=").append(password);
        sb.append("]");
        return sb.toString();
    }
}