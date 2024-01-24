package cn.cetasas.message.req;

import lombok.Data;

@Data
public class MVideoInsertRequest {
    private Long id;

    private String filename;

    private String poster;

    private String suffix;

    private byte[] content;
}