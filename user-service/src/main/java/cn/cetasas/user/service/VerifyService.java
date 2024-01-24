package cn.cetasas.user.service;

import cn.cetasas.user.exception.BusinessException;
import cn.cetasas.user.exception.BusinessExceptionCode;
import cn.cetasas.user.util.VerifyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.util.Random;

@Service
public class VerifyService {

    private final static Logger LOG = LoggerFactory.getLogger(VerifyService.class);

//    @Resource
//    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final static char[] ch =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
             'A', 'B', 'C', 'D', 'E', 'F', 'G',
             'H', 'I', 'J', 'K', 'L', 'M', 'N',
             'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
             'a', 'b', 'c', 'd', 'e', 'f', 'g',
             'h', 'i', 'j', 'k', 'l', 'm', 'n',
             'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    private final static Random random = new Random();

    // 文字验证码
    public String getVerification() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i <6; i++){
            char num = ch[random.nextInt(ch.length)];
            str.append(num);
        }
        return str.toString();
    }

    /**
     * // 返回的数组第一个参数是生成的验证码，第二个参数是生成的图片
     * Object[] objs = VerifyUtil.newBuilder().build().createImage();
     *
     * // 这个根据自己的需要设置对应的参数来实现个性化
     * // 返回的数组第一个参数是生成的验证码，第二个参数是生成的图片
     * Object[] objs = VerifyUtil.newBuilder()
     *         .setWidth(120)   //设置图片的宽度
     *         .setHeight(35)   //设置图片的高度
     *         .setSize(6)      //设置字符的个数
     *         .setLines(10)    //设置干扰线的条数
     *         .setFontSize(25) //设置字体的大小
     *         .setTilt(true)   //设置是否需要倾斜
     *         .setBackgroundColor(Color.WHITE) //设置验证码的背景颜色
     *         .build()         //构建VerifyUtil项目
     *         .createImage();  //生成图片
     */
    public Object[] getVerificationIMG() {
        // 利用图片工具生成图片
        // 返回的数组第一个参数是生成的验证码，第二个参数是生成的图片
        return VerifyUtil.newBuilder()
                .setWidth(120)   //设置图片的宽度
                .setHeight(35)   //设置图片的高度
                .setSize(6)      //设置字符的个数
                .setLines(5)     //设置干扰线的条数
                .setFontSize(25) //设置字体的大小
                .setTilt(true)   //设置是否需要倾斜
                .setBackgroundColor(Color.LIGHT_GRAY) //设置验证码的背景颜色
                .build()         //构建VerifyUtil项目
                .createImage();
    }

//    @Deprecated
//    private void verify(String key, String verification) {
//        String s = stringRedisTemplate.opsForValue().get(key);
//        if (ObjectUtils.isEmpty(s)) {
//            throw new BusinessException(BusinessExceptionCode.VERIFICATION_OUT_OF_TIME);
//        } else if (s.equalsIgnoreCase(verification)) {
//            LOG.info("验证成功");
//        } else {
//            throw new BusinessException(BusinessExceptionCode.VERIFICATION_NOT_MATCH);
//        }
//    }
//
//    @Deprecated
//    public void verify1(String verification, HttpServletRequest request) {
//        HttpSession session = request.getSession();
//        String id = session.getId();
//
//        // 将redis中的尝试次数减一
//        String verifyCodeKey = "VERIFY_CODE_" + id;
//        Long num = stringRedisTemplate.opsForValue().decrement(verifyCodeKey);
//
//        // 如果次数次数小于0 说明验证码已经失效
//        if (num < 0) {
//            stringRedisTemplate.delete(verifyCodeKey); // 手动删除
//            throw new BusinessException(BusinessExceptionCode.VERIFICATION_OUT_OF_TIME);
//        }
//
//        // 将session中的取出对应session id生成的验证码
//        String serverCode = (String) session.getAttribute("SESSION_VERIFY_CODE_" + id);
//        // 校验验证码
//        if (null == serverCode || !serverCode.equalsIgnoreCase(verification)) {
//            throw new BusinessException(BusinessExceptionCode.VERIFICATION_NOT_MATCH);
//        }
//
//        // 验证通过之后手动将验证码失效
//        stringRedisTemplate.delete(verifyCodeKey);
//    }

    public void verify2(String verification, HttpServletRequest request) {
        // 将redis中的尝试次数减一
        String VERIFY_KEY = request.getHeader("VERIFY_KEY");
        Long num = stringRedisTemplate.opsForValue().decrement(VERIFY_KEY);

        // 如果次数次数小于0 说明验证码已经失效
        if (num < 0) {
            LOG.info("验证码失效");
            throw new BusinessException(BusinessExceptionCode.VERIFICATION_OUT_OF_TIME);
        }

        // 将session中的取出对应session id生成的验证码
        String VERIFY_CODE = request.getHeader("VERIFY_CODE");
        String code = stringRedisTemplate.opsForValue().get(VERIFY_CODE);
        // 校验验证码
        if (null == code || !code.equalsIgnoreCase(verification)) {
            LOG.info("验证码校验不通过");
            throw new BusinessException(BusinessExceptionCode.VERIFICATION_NOT_MATCH);
        }

        LOG.info("验证成功！！");

        // 验证通过之后手动将验证码失效
        stringRedisTemplate.delete(VERIFY_KEY);
        stringRedisTemplate.delete(VERIFY_CODE);
    }
}
