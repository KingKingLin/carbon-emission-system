//package cn.cetasas.message.service.impl;
//
//import cn.cetasas.message.exception.BusinessException;
//import cn.cetasas.message.exception.BusinessExceptionCode;
//import cn.cetasas.message.mapper.UserMapper;
//import cn.cetasas.message.pojo.User;
//import cn.cetasas.message.pojo.UserExample;
//import cn.cetasas.message.pojo.impl.LoginUser;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import org.springframework.util.CollectionUtils;
//
//import javax.annotation.Resource;
//import java.util.List;
//
//@Service
//public class UserDetailsServiceImpl implements UserDetailsService {
//    private final static Logger LOG = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
//
//    @Resource
//    private UserMapper userMapper;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        LOG.info("正在向数据库中查询用户【{}】", username);
//        // 查询用户信息
//        UserExample example = new UserExample();
//        UserExample.Criteria criteria = example.createCriteria();
//        criteria.andUsernameEqualTo(username);
//        List<User> users = userMapper.selectByExample(example);
//        if (CollectionUtils.isEmpty(users)) {
//            throw new BusinessException(BusinessExceptionCode.USERNAME_NOT_EXIST);
//        }
//        User user = users.get(0);
//        //TODO 查询对应的权限信息
//
//        // 把数据封装成 UserDetails 返回
//        return new LoginUser(user);
//    }
//}
