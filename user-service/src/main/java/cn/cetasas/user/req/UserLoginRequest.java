package cn.cetasas.user.req;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class UserLoginRequest {
    @NotNull(message = "【邮箱或用户名】不能为空")
    private String username;

    /**
     * NotNull: 会检验null
     * NotEmpty: 会检验null和"'
     * 如果我们在登录框里，先写一个内容，再清空就是""
     *
     * 由于，本项目中为了保证用户的安全，采用前后端 MD5 加密，导致前端传过来的字符串必定是满足检测条件的 => 一串无规则的字符串
     * 采用双 MD5 加密后，就算数据库泄露，也可以在一定程度上保证用户密码不被破解，避免撞库攻击
     */
    @NotEmpty(message = "【密码】不能为空")
//    @Length(min = 6, max = 20, message = "【密码】6~20位")
//    @Pattern(regexp = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,32}$", message = "【密码】规则不正确")
    private String password;

    private String verification; // 验证码

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVerification() {
        return verification;
    }

    public void setVerification(String verification) {
        this.verification = verification;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", username=").append(username);
        sb.append(", password=").append(password);
        sb.append(", verification=").append(verification);
        sb.append("]");
        return sb.toString();
    }
}