package cn.cetasas.user.controller;

import cn.cetasas.user.exception.BusinessException;
import cn.cetasas.user.exception.BusinessExceptionCode;
import cn.cetasas.user.pojo.Mail;
import cn.cetasas.user.resp.CommonResp;
import cn.cetasas.user.service.UserService;
import cn.cetasas.user.service.VerifyService;
import cn.cetasas.user.util.MailUtil;
import cn.cetasas.user.util.SnowFlake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user/verification")
public class VerifyController {

    private final static Logger LOG = LoggerFactory.getLogger(VerifyController.class);

//    @Resource
//    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private VerifyService verifyService;

    @Resource
    private UserService userService;

    @Resource
    private MailUtil mailUtil;

    @Resource
    private SnowFlake snowFlake;

//    @GetMapping("/send")
//    // 注册、修改密码 => 文字验证码
//    public CommonResp<String> sendVerification(@Valid Mail mail,
//                                               HttpServletRequest request) {
//        CommonResp<String> resp = new CommonResp<>();
//        // 获取到session
//        HttpSession session = request.getSession();
//        // 取到sessionid
//        String id = session.getId();
//
//        // 1) 获取验证码
//        // String verification = verifyService.getVerification();
//        Object[] objs = verifyService.getVerificationIMG();
//        resp.setContent(objs[0].toString()); // 验证码
//        // 将验证码存入Session
//        session.setAttribute("SESSION_VERIFY_CODE_" + id, objs[0]);
//        // 打印验证码
//        LOG.info("获取验证码 {}", objs[0]);
//
//        // 2) 向该邮箱地址发送验证码
//        LOG.info("向邮箱【{}】发送验证码 {}", mail.getMail(), objs[0]);
//        mailUtil.sendVerification(mail, objs[0].toString());
//
//        // 2) 在 redis 注册验证码 => 60s 过期
//        // 设置redis值的序列化方式
//        // redisTemplate.setValueSerializer(new StringRedisSerializer());
//        // 在redis中保存一个验证码最多尝试次数
//        // 这里采用的是先预设一个上限次数，再以reidis decrement(递减)的方式来进行验证
//        // 这样有个缺点，就是用户只申请验证码，不验证就走了的话，这里就会白白占用1分钟的空间，造成浪费了
//        // 为了避免以上的缺点，也可以采用redis的increment（自增）方法，只有用户开始在做验证的时候设置值，
//        //    超过多少次错误，就失效；避免空间浪费
//        stringRedisTemplate.opsForValue().set("VERIFY_CODE_" + id, "3", 60, TimeUnit.SECONDS);
//        LOG.info("redis 生成 key({})", "VERIFY_CODE_" + id);
//
//        return resp;
//    }
//
//    // 登录的时候，不需要发送验证码邮件！！！
//    // 登录是获取图片验证码
//    @GetMapping("/get")
//    public void getVerificationIMG(HttpServletResponse response, HttpServletRequest request) throws IOException {
//        // 获取到session
//        HttpSession session = request.getSession();
//        // 取到sessionid
//        String id = session.getId();
//
//        // 1) 获取验证码 objs[0]：验证码; objs[1]：图片
//        Object[] objs = verifyService.getVerificationIMG();
//        // 将验证码存入Session
//        session.setAttribute("SESSION_VERIFY_CODE_" + id, objs[0]);
//        // 打印验证码
//        LOG.info("获取验证码 {}", objs[0]);
//        // 2) 在 redis 注册验证码 => 60s 过期
//        // 设置redis值的序列化方式
//        // redisTemplate.setValueSerializer(new StringRedisSerializer());
//        // 在redis中保存一个验证码最多尝试次数
//        // 这里采用的是先预设一个上限次数，再以reidis decrement(递减)的方式来进行验证
//        // 这样有个缺点，就是用户只申请验证码，不验证就走了的话，这里就会白白占用1分钟的空间，造成浪费了
//        // 为了避免以上的缺点，也可以采用redis的increment（自增）方法，只有用户开始在做验证的时候设置值，
//        //    超过多少次错误，就失效；避免空间浪费
//        stringRedisTemplate.opsForValue().set("VERIFY_CODE_" + id, "3", 60, TimeUnit.SECONDS);
//        LOG.info("redis 生成 key({})", "VERIFY_CODE_" + id);
//
//
//        // 将图片输出给浏览器
//        BufferedImage image = (BufferedImage) objs[1];
//        response.setContentType("image/png");
//        OutputStream os = response.getOutputStream();
//        ImageIO.write(image, "png", os);
//    }

    @GetMapping("/send")
    // 注册、修改密码 => 文字验证码
    public CommonResp<String> sendVerification(@Valid Mail mail,
                                               HttpServletResponse response) {
        CommonResp<String> resp = new CommonResp<>();

        // 1) 获取验证码
        String verification = verifyService.getVerification();
        resp.setContent(verification); // 验证码
        // 打印验证码
        LOG.info("获取验证码 {}", verification);

        // 2) 向该邮箱地址发送验证码
        LOG.info("向邮箱【{}】发送验证码 {}", mail.getMail(), verification);
        mailUtil.sendVerification(mail, verification);

        // 3) 在 redis 注册验证码 => 60s 过期
        // 在redis中保存一个验证码最多尝试次数
        // 这里采用的是先预设一个上限次数，再以reidis decrement(递减)的方式来进行验证
        // 这样有个缺点，就是用户只申请验证码，不验证就走了的话，这里就会白白占用1分钟的空间，造成浪费了
        // 为了避免以上的缺点，也可以采用redis的increment（自增）方法，只有用户开始在做验证的时候设置值，
        //      超过多少次错误，就失效；避免空间浪费
        long id = snowFlake.nextId();
        String VERIFY_KEY = "VERIFY_KEY_" + id;
        stringRedisTemplate.opsForValue().set(VERIFY_KEY, "3", 60, TimeUnit.SECONDS);
        LOG.info("生成 key({}) 并设置进 redis 中", VERIFY_KEY);
        String VERIFY_CODE = "VERIFY_CODE_" + id; // 验证码
        stringRedisTemplate.opsForValue().set(VERIFY_CODE, verification, 60, TimeUnit.SECONDS);
        LOG.info("生成验证码的 key({}) 并设置进 redis 中", VERIFY_CODE);

        // 4) 将 验证码 设置进 header 中
        response.setHeader("Access-Control-Expose-Headers", "VERIFY_KEY,VERIFY_CODE");
        response.setHeader("VERIFY_KEY", VERIFY_KEY);
        response.setHeader("VERIFY_CODE", VERIFY_CODE);

        return resp;
    }

    // 登录的时候，不需要发送验证码邮件！！！
    // 登录是获取图片验证码
    @GetMapping("/get")
    public void getVerificationIMG(HttpServletResponse response) throws IOException {
        // 1) 获取验证码 objs[0]：验证码; objs[1]：图片
        Object[] objs = verifyService.getVerificationIMG();
        // 打印验证码
        LOG.info("获取验证码 {}", objs[0]);

        // 2) 在 redis 注册验证码 => 60s 过期
        // 在redis中保存一个验证码最多尝试次数
        // 这里采用的是先预设一个上限次数，再以reidis decrement(递减)的方式来进行验证
        // 这样有个缺点，就是用户只申请验证码，不验证就走了的话，这里就会白白占用1分钟的空间，造成浪费了
        // 为了避免以上的缺点，也可以采用redis的increment（自增）方法，只有用户开始在做验证的时候设置值，
        //    超过多少次错误，就失效；避免空间浪费
        long id = snowFlake.nextId();
        String VERIFY_KEY = "VERIFY_KEY_" + id;
        stringRedisTemplate.opsForValue().set(VERIFY_KEY, "3", 60, TimeUnit.SECONDS);
        LOG.info("生成 key({}) 并设置进 redis 中", VERIFY_KEY);
        String VERIFY_CODE = "VERIFY_CODE_" + id; // 验证码
        stringRedisTemplate.opsForValue().set(VERIFY_CODE, objs[0].toString(), 60, TimeUnit.SECONDS);
        LOG.info("生成验证码的 key({}) 并设置进 redis 中", VERIFY_CODE);

        // 4) 将 验证码 设置进 header 中
        response.setHeader("Access-Control-Expose-Headers", "VERIFY_KEY,VERIFY_CODE");
//        response.setHeader("Access-Control-Expose-Headers", "VERIFY_KEY");
        response.setHeader("VERIFY_KEY", VERIFY_KEY);
        response.setHeader("VERIFY_CODE", VERIFY_CODE);

        // 5) 将图片输出给浏览器
        BufferedImage image = (BufferedImage) objs[1];
        response.setContentType("image/png");
        OutputStream os = response.getOutputStream();
        ImageIO.write(image, "png", os);
    }

    // "修改密码"前检验是否是本人
    // 1) 验证该邮箱是否注册用户过
    // 2) 向邮箱发送验证码
    // 3) 返回用户 id
    // =======================> 不用担心如果有黑客拿到 id 后就可以修改用户密码之类的
    // 因为在 修改密码 必须带上验证码，而验证码是发送到用户邮箱
    // 因此就算黑客或其他人通过浏览器请求该接口，拿到 id 也无济于事
    @GetMapping("/verify")
    public CommonResp<Integer> verifyBeforeRestPassword(@RequestParam("mail") String mail,
                                                       @RequestParam("verification") String verification,
                                                       HttpServletRequest request) {
        CommonResp<Integer> resp = new CommonResp<>();
        // 验证邮箱 => 验证成功返回用户 id
        Integer id = userService.verifyMail(mail);
        if (ObjectUtils.isEmpty(id)) { // 用户还未注册，应该去注册
            LOG.info("邮箱【{}】还未注册", mail);
            throw new BusinessException(BusinessExceptionCode.MAIL_NOT_ENROLL);
        }
        resp.setContent(id);
        // 验证验证码
        verifyService.verify2(verification, request);
        resp.setMessage("验证成功，可以修改密码！");
        LOG.info("验证成功");
        return resp;
    }
}
