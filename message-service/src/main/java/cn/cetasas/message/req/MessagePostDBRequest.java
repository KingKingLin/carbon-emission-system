package cn.cetasas.message.req;

import lombok.Data;

@Data
public class MessagePostDBRequest {
    private Long id;

    private String title;

    private String content;
}