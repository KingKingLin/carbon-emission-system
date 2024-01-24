package cn.cetasas.message.req;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class UserRequest {
    @NotEmpty(message = "【用户名】不能为空")
    private String username;
    @NotEmpty(message = "【密码】不能为空")
    private String password;
}
