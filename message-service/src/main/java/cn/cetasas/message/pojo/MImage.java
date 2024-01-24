package cn.cetasas.message.pojo;

import java.util.Date;

public class MImage {
    private Long id;

    private String filename;

    private Date createtime;

    private String suffix;

    private byte[] content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", filename=").append(filename);
        sb.append(", createtime=").append(createtime);
        sb.append(", suffix=").append(suffix);
        sb.append(", content=").append(content);
        sb.append("]");
        return sb.toString();
    }
}