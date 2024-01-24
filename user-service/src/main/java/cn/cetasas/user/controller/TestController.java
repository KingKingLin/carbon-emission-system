package cn.cetasas.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/test")
public class TestController {

    private final static Logger LOG = LoggerFactory.getLogger(TestController.class);


    @GetMapping("/login")
    public String login() {
        return "登录成功！";
    }
}
