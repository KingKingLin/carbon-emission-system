package cn.cetasas.message.req;

import lombok.Data;

@Data
public class MessageReviseDBRequest {
    private Long id;

    private String title;

    private String content;

    private String modifytime;
}