package cn.cetasas.message.resp;

import lombok.Data;

import java.util.Date;

@Data
public class MessageWithoutContentResponse {
    private Long id;

    private String title;

    private Date createtime;

    private Date modifytime;
}