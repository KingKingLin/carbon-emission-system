package cn.cetasas.message.req;

import lombok.Data;

@Data
public class MImageInsertRequest {
    private Long id;

    private String filename;

    private String suffix;

    private byte[] content;
}