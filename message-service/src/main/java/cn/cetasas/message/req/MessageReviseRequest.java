package cn.cetasas.message.req;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class MessageReviseRequest {
    @NotNull(message = "【ID】不能为空")
    private Long id;

    @NotEmpty(message = "【标题】不能为空")
    private String title;

    @NotEmpty(message = "【内容】不能为空")
    private String content;
}