package cn.cetasas.message.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RecordRequest {
    @NotNull(message = "【id】不能为空")
    private Long id;

    @NotNull(message = "【userid】不能为空")
    private Integer userid;
}