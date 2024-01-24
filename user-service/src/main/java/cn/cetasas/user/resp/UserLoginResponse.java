package cn.cetasas.user.resp;

/**
 * 把该实体放入 redis 需要注意的点：
 * 1. 实现序列化接口
 * 2. 把该对象转成 Json 对象
 */
//public class UserLoginResponse implements Serializable {
public class UserLoginResponse {
    private Integer id;

    private String token; // 本项目通过雪花算法设计 token

    private String mail;

    private String name;

    private String password;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", token=").append(token);
        sb.append(", mail=").append(mail);
        sb.append(", name=").append(name);
        sb.append(", password=").append(password);
        sb.append("]");
        return sb.toString();
    }
}