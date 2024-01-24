package cn.cetasas.message.resp;

import lombok.Data;

@Data
public class UploadImage {
    private String url;     // 图片 src ，必须

    private String alt;     // 图片描述文字，非必须

    private String href;    // 图片的链接，非必须
}
