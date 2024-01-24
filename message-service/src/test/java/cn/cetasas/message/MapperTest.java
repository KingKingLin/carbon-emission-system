package cn.cetasas.message;

import cn.cetasas.message.mapper.UserMapper;
import cn.cetasas.message.pojo.User;
import cn.cetasas.message.resp.UserMessageResponse;
import cn.cetasas.message.service.ApiService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MapperTest {

    @Resource
    private UserMapper userMapper;

    @Test
    public void testUserMapper() {
        List<User> users = userMapper.selectByExample(null);
        System.out.println(users);
    }

    @Test
    public void testPasswordEncoder() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        $2a$10$AGlhx1HUMNNnSD/4bJGtb.s0x2VlL/XSQpxrJvCxnoC58tt/.Av8C
//        $2a$10$00X.oko6P5pLQGiXUSHiauklwhFOJZ1RCmarniKxHp0WHMdI.UZKq
//        String encode1 = passwordEncoder.encode("admin");
//        String encode2 = passwordEncoder.encode("admin");
//        System.out.println(encode1);
//        System.out.println(encode2);
        System.out.println(passwordEncoder.matches("admin",
                "$2a$10$AGlhx1HUMNNnSD/4bJGtb.s0x2VlL/XSQpxrJvCxnoC58tt/.Av8C"));


    }

    @Test
    public void testDate() {
        Date date = new Date();
        System.out.println(date);
    }

    @Resource
    private ApiService apiService;

    @Test
    public void testIsread() {
        List<UserMessageResponse> list = apiService.listByUser("1");
        System.out.println(list);
    }

    @Test
    public void testBytes() {
        byte[] arr = {0, 1, 4, 5};
        System.out.println(arr.toString());
        System.out.println(Arrays.toString(arr));

        String s = new String(arr);
        System.out.println(s.getBytes());
    }
}
