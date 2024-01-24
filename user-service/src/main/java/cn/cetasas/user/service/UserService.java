package cn.cetasas.user.service;

import cn.cetasas.user.exception.BusinessException;
import cn.cetasas.user.exception.BusinessExceptionCode;
import cn.cetasas.user.mapper.UserMapper;
import cn.cetasas.user.pojo.Mail;
import cn.cetasas.user.pojo.User;
import cn.cetasas.user.pojo.UserExample;
import cn.cetasas.user.req.UserLoginRequest;
import cn.cetasas.user.req.UserResetPasswordRequest;
import cn.cetasas.user.req.UserSaveRequest;
import cn.cetasas.user.resp.UserLoginResponse;
import cn.cetasas.user.util.CopyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Service
public class UserService {

    private final static Logger LOG = LoggerFactory.getLogger(UserService.class);

    @Resource
    private UserMapper userMapper;

    /**
     * 登录
     * 如果登录失败，不能给用户返回具体的信息，为了防止黑客来攻击
     * 举个例子：如果黑客拿了一堆用户名/密码来测试，如果你返回用户名不存在，知道出现密码错误，那么他就知道改用户名存在，只是密码错了
     * 那么他就会尝试去破解这个密码
     *
     * 所以我们统一返回 用户名不存在或密码不对
     *
     * 即，我们返回给前端时模糊的；但是我们自己查日志应该是具体的；
     * 即，日志打印越详细越好，方便生产运维，不能坑了自己；
     * @param user
     * @return
     */
    public UserLoginResponse login(@Valid UserLoginRequest user) {
        // 支持 用户名+密码 和 邮箱+密码 两种登录方式
        // 先通过邮箱或用户名从库中查询出用户信息，再比较密码是否正确
        User userDB = selectUserByMailOrName(user);
        if (ObjectUtils.isEmpty(userDB)) {
            // 用户名不存在，不能给用户具体的提示，防止黑客来攻击
            // 通过异常中断我们的业务
            LOG.info("邮箱或用户名不存在, {}", user.getUsername());
            // 通过这个日志，如果我们发现了很多用户名不存在，即代表着，有人在探测我们的用户名
            throw new BusinessException(BusinessExceptionCode.LOGIN_USER_ERROR);
        } else {
            // 比对密码是否正确
            if(user.getPassword().equals(userDB.getPassword())) {
                // 登录成功
                return CopyUtil.copy(userDB, UserLoginResponse.class);
            } else {
                // 密码不对
                // 小提示：这里可以加个功能：密码输错5次就锁定用户
                LOG.info("密码不对, 输入密码: {}, 数据库密码: {}", user.getPassword(), userDB.getPassword());
                throw new BusinessException(BusinessExceptionCode.LOGIN_USER_ERROR);
            }
        }
    }

    /**
     * 存在漏洞：
     *  如果有如下两个用户：
     *      user1: 82@qq.com  user1     password
     *      user2: 32@163.com 82@qq.com password
     *  那么我们的查询 sql => select * from user where mail = ? or name = ?
     *  将会查出两个数据 => 在我们项目中，这种情况是不允许存在的
     *
     * 为了避免这个问题需要后端程序员在"注册用户"时就避免这种情况！！
     * @param user
     * @return
     */
    private User selectUserByMailOrName(UserLoginRequest user) {
        // 设置查询条件
        UserExample example = new UserExample();
        // 1) `mail` = #{username}
        UserExample.Criteria criteria1 = example.createCriteria();
        criteria1.andMailEqualTo(user.getUsername());
        // 2) `name` = #{username}
        UserExample.Criteria criteria2 = example.createCriteria();
        criteria2.andNameEqualTo(user.getUsername());
        // 3) `mail` = #{username} or `name` = #{username}
        example.or(criteria2);

        List<User> users = userMapper.selectByExample(example); // 查询

        return CollectionUtils.isEmpty(users) ? null : users.get(0);
    }

    /**
     * 保存用户
     * @param user
     */
    public void save(UserSaveRequest user) {
        User user1 = CopyUtil.copy(user, User.class);
        if (ObjectUtils.isEmpty(user1.getId())) { // 保存
            if (isExitMailAndName(user1)) {
                userMapper.insert(user1); // 新增
            }
        }
    }

    /**
     * 判断邮箱和用户名是否存在
     * @param user
     * @return
     */
    private boolean isExitMailAndName(User user) {
        User user1 = selectUserByName(user.getName());
        if (!ObjectUtils.isEmpty(user1)) {
            throw new BusinessException(BusinessExceptionCode.USER_NAME_EXIST);
        }

        User user2 = selectUserByMail(user.getMail());
        if (!ObjectUtils.isEmpty(user2)) {
            throw new BusinessException(BusinessExceptionCode.USER_MAIL_EXIST);
        }

        // 防止用户采用 bug 注册用户 => 即拿邮箱作为用户名
        User user3 = selectUserByMail(user.getName());
        if (!ObjectUtils.isEmpty(user3)) { // 有值 => 代表该用户名 正好撞上了已经被注册过的邮箱
            throw new BusinessException(BusinessExceptionCode.MAIL_CAN_NOT_BE_NAME);
        }

        return true;
    }

    private User selectUserByName(String name) {
        // 设置查询条件
        UserExample example = new UserExample();
        // 1) where name = #{name}
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(name);
        // 2) 查询
        List<User> users = userMapper.selectByExample(example);
        return CollectionUtils.isEmpty(users) ? null : users.get(0);
    }

    private User selectUserByMail(String mail) {
        // 设置查询条件
        UserExample example = new UserExample();
        // 1) where mail = #{mail}
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andMailEqualTo(mail);
        // 2) 查询
        List<User> users = userMapper.selectByExample(example);
        return CollectionUtils.isEmpty(users) ? null : users.get(0);
    }

    public void resetPassword(UserResetPasswordRequest user) {
        User user1 = CopyUtil.copy(user, User.class);
        userMapper.updateByPrimaryKeySelective(user1);
    }

    public Integer forgetPassword(Mail mail) {
        // 创建查询条件
        UserExample example = new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andMailEqualTo(mail.getMail());

        // 查询
        List<User> users = userMapper.selectByExample(example);

        // 如果 users 为空，则代表该邮箱尚未被注册
        return CollectionUtils.isEmpty(users) ? null : users.get(0).getId();
    }

    public Integer verifyMail(String mail) {
        User user = selectUserByMail(mail);
        // 如果 user 为 null 代表，该用户还未注册
        // 如果 user 不为 null 代表，该用户已注册
        if (!ObjectUtils.isEmpty(user)) {
            return user.getId();
        } else return null;
    }
}
