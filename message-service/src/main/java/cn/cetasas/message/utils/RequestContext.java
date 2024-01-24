package cn.cetasas.message.utils;

import java.io.Serializable;

/**
 * 在以后线程执行的路径上，在不停的地方会去赋值，会去取值
 * 我们就可以用到线程本地变量
 *
 * 只在本个线程有效，线程之间互不干扰
 *
 * 往redis里面放值，都应该考虑过期时长
 */
public class RequestContext implements Serializable {
    // 远程地址
    public static ThreadLocal<String> remoteAddr = new ThreadLocal<>();

    public static String getRemoteAddr() {
        return remoteAddr.get();
    }

    public static void setRemoteAddr(String remoteAddr) {
        RequestContext.remoteAddr.set(remoteAddr);
    }
}
