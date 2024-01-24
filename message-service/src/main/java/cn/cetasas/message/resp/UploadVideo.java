package cn.cetasas.message.resp;

import lombok.Data;

@Data
public class UploadVideo {
    private String url;     // 视频 src ，必须

    private String poster;  // 视频封面图片 url，可选
}
