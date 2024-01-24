package cn.cetasas.user.req;

import javax.validation.constraints.NotNull;

public class InfoUpdateRequest {
    @NotNull
    private Long id;

    private String sex;

//    private Integer userid; // 绑定的 userid 也不允许修改

    private String city;

    private String info;

//    @Length(max = 50, message = "【地址】最多50个字符")
    private String address;

//    private Date createtime; // 创建时间不允许修改

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", sex=").append(sex);
        sb.append(", city=").append(city);
        sb.append(", info=").append(info);
        sb.append(", address=").append(address);
        sb.append("]");
        return sb.toString();
    }
}