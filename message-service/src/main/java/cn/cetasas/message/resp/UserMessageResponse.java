package cn.cetasas.message.resp;

import lombok.Data;

import java.util.Date;

@Data
public class UserMessageResponse {
    private Long id;

    private String title;

    private Date createtime;

    private Date modifytime;

    private Boolean isread = false; // 是否阅读 => 默认没有阅读
}