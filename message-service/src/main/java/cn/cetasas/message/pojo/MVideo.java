package cn.cetasas.message.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class MVideo {
    private Long id;

    private String filename;

    private String poster;

    private Date createtime;

    private String suffix;

    private byte[] content;
}