package cn.cetasas.user.controller;

import cn.cetasas.user.req.UserLoginRequest;
import cn.cetasas.user.req.UserResetPasswordRequest;
import cn.cetasas.user.req.UserSaveRequest;
import cn.cetasas.user.resp.CommonResp;
import cn.cetasas.user.resp.UserLoginResponse;
import cn.cetasas.user.service.UserService;
import cn.cetasas.user.service.VerifyService;
import cn.cetasas.user.util.SnowFlake;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class UserController {

    private final static Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Resource
    private UserService userService;

    @Resource
    private VerifyService verifyService;

//    @Resource
//    private RedisTemplate<String, String> redisTemplate; // 操作 redis 的工具

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SnowFlake snowFlake; // 雪花算法

    // 登录
    @PostMapping("/login")
    // @RequestBody 这个注解对应的就是json方式的（Post）提交
    // 如果是 form 表单的提交，就不用写这个注解了@RequestBody
    public CommonResp<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest user, HttpServletRequest request) {
        LOG.info("用户【{}】正在尝试登录", user.getUsername());
        // 16 进制的 md5
        // 注册的时候，密码是经过两层加密的，所以登录的时候也要经过两层加密
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        CommonResp<UserLoginResponse> resp = new CommonResp<>();

        // 如果有验证码 => 则检验 => 配合的是 /user/verification/get
        if (!ObjectUtils.isEmpty(user.getVerification())) {
            // verify(user.getUsername(), user.getVerification());
            // verifyService.verify1(user.getVerification(), request);
            verifyService.verify2(user.getVerification(), request);
            LOG.info("验证成功");
        }

        // 登录
        UserLoginResponse userLoginResponse = userService.login(user);

        // 登录成功之后，能够执行到这的，一定是登录成功的；因为登录失败的，都被异常处理了
        String token = "USER_LOGIN_" + snowFlake.nextId(); // token 也要返回给前端的！！
                                         // 因为前端后面的请求，都要带上这个 token，好让我们后端可以验证这个 token 是不是有效的
                                         // 即给 UserLoginResp 也增加一个 token 属性
        LOG.info("生成单点登录token {}, 并放入redis中！", token);
        userLoginResponse.setToken(token);

        // 将登录信息保存进 redis 中
        stringRedisTemplate.opsForValue().set(token, JSON.toJSONString(userLoginResponse), 3600 * 24, TimeUnit.SECONDS); // 24 h 后登录信息消失

        resp.setContent(userLoginResponse);
        return resp;
    }

    // 注册
    @PostMapping
    public CommonResp<String> save(@Valid @RequestBody UserSaveRequest user,
                                   HttpServletRequest request) {
        LOG.info("正在注册用户：{}", user);
        CommonResp<String> resp = new CommonResp<>();

        // 检验验证码 => 配合的是 /user/verification/send
        // verifyService.verify(user.getMail(), user.getVerification());
        // verifyService.verify1(user.getVerification(), request);
        verifyService.verify2(user.getVerification(), request);
        LOG.info("验证成功");

        // 16 进制的 md5 => 加密
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        userService.save(user);
        resp.setMessage("注册成功");
        LOG.info("用户【{}】注册成功...", user.getName());
        return resp;
    }

    // 修改密码
    @PostMapping("/reset_password")
    public CommonResp<String> restPassword(@Valid @RequestBody UserResetPasswordRequest user) {
        CommonResp<String> resp = new CommonResp<>();
        // 16 进制的 md5 => 加密
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        userService.resetPassword(user); // 修改密码
        resp.setMessage("修改密码成功");
        return resp;
    }

    // 忘记密码 => 有 bug, 换另一种方式解决了
    // 1) 验证该邮箱是否注册用户过
    // 2) 向邮箱发送验证码
    // 3) 返回用户 id
    // =======================> 不用担心如果有黑客拿到 id 后就可以修改用户密码之类的
    // 因为在 修改密码 必须带上验证码，而验证码是发送到用户邮箱
    // 因此就算黑客或其他人通过浏览器请求该接口，拿到 id 也无济于事
//    @GetMapping("/forget_password")
//    public CommonResp<Integer> forgetPassword(@Valid Mail mail,
//                                              HttpServletRequest request,
//                                              HttpServletResponse response) throws ServletException, IOException {
//        LOG.info("用户【{}】忘记密码，尝试修改密码", mail.getMail());
//        CommonResp<Integer> resp = new CommonResp<>();
//        Integer id = userService.forgetPassword(mail);
//        if (StringUtils.isEmpty(id)) { // 邮箱不存在
//            LOG.info("该邮箱【{}】尚未被注册", mail.getMail());
//            throw new BusinessException(BusinessExceptionCode.MAIL_NOT_ENROLL);
//        }
//        // 用户存在
//        LOG.info("查询到该用户【{}】", mail.getMail());
//        // 发送验证码邮件 => 分发请求去发送邮件
//        RequestDispatcher dispatcher = request.getRequestDispatcher("/user/verification/send?mail=" + mail.getMail());
//        dispatcher.forward(request, response);
////        resp.setMessage("返回用户 id");
//        resp.setContent(id);
//        return resp;
//    }

    @GetMapping("/logout")
    public void logout(HttpServletRequest request) {
        String token = request.getHeader("token");

        LOG.info("token: {} 登出", token);

        stringRedisTemplate.delete(token);
    }
}
