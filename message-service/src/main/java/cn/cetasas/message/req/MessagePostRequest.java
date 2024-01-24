package cn.cetasas.message.req;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class MessagePostRequest {
    @NotEmpty(message = "【标题】不能为空")
    private String title;

    @NotEmpty(message = "【内容】不能为空")
    private String content;
}