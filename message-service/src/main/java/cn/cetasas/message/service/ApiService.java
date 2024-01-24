package cn.cetasas.message.service;

import cn.cetasas.message.exception.BusinessException;
import cn.cetasas.message.exception.BusinessExceptionCode;
import cn.cetasas.message.mapper.MessageMapper;
import cn.cetasas.message.mapper.RecordMapper;
import cn.cetasas.message.mapper.UserMapper;
import cn.cetasas.message.pojo.Record;
import cn.cetasas.message.pojo.RecordExample;
import cn.cetasas.message.pojo.User;
import cn.cetasas.message.pojo.UserExample;
import cn.cetasas.message.req.*;
import cn.cetasas.message.resp.MessageResponse;
import cn.cetasas.message.resp.MessageWithoutContentResponse;
import cn.cetasas.message.resp.UserMessageResponse;
import cn.cetasas.message.resp.UserResponse;
import cn.cetasas.message.utils.CopyUtil;
import cn.cetasas.message.utils.SnowFlake;
import com.google.protobuf.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

@Service
public class ApiService {
    private final static Logger LOG = LoggerFactory.getLogger(ApiService.class);

    @Resource
    private UserMapper userMapper;

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private SnowFlake snowFlake; // 雪花算法

    @Resource
    private RecordMapper recordMapper;

    @Resource
    private WsService wsService; // 推送消息

    public UserResponse login(@Valid UserRequest user) {
        // 查询
        User user1 = selectByUserName(user); // 数据里查询出来的用户信息

        if (ObjectUtils.isEmpty(user1) || !user1.getPassword().equals(user.getPassword())) {
            LOG.info("用户名或者密码错误");
            throw new BusinessException(BusinessExceptionCode.LOGIN_ERROR);
        }

        return CopyUtil.copy(user1, UserResponse.class);
    }

    private User selectByUserName(UserRequest user) {
        // 创建查询条件
        UserExample example = new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andUsernameEqualTo(user.getUsername());
        List<User> users = userMapper.selectByExample(example);
        return CollectionUtils.isEmpty(users) ? null : users.get(0);
    }

    // 1) 存入数据库中
    // 2) 异步发送消息
    public long publish(MessagePostRequest message) {
        MessagePostDBRequest message1 = CopyUtil.copy(message, MessagePostDBRequest.class);
        long id = snowFlake.nextId();
        LOG.info("生成消息【{}】", id);
        // 1) 存入数据库中
        message1.setId(id);
        messageMapper.publish(message1);

        // 2) 推送消息 => 推送 title
        wsService.sendInfo("您收到一条消息：【" + message1.getTitle() + "】，请注意查收！");
        return id;
    }

    public List<MessageWithoutContentResponse> list() {
        return messageMapper.list();
    }

    public void revise(MessageReviseRequest message) {
        MessageReviseDBRequest message1 = CopyUtil.copy(message, MessageReviseDBRequest.class);
        // 修改 message1 的更新时间
//        Date date = new Date();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        message1.setModifytime(timestamp.toString());
        // 修改
        messageMapper.revise(message1);
    }

    public MessageResponse selectByPrimaryKey(Long id) {
        return messageMapper.selectByPrimaryKey(id);
    }

    public void delete(Long id) {
        messageMapper.delete(id);
    }

    public Integer getNotReadCount(String userid) {
        return messageMapper.getNotReadCount(userid);
    }

    public List<UserMessageResponse> listByUser(String userid) {
        // 用户已经阅读的文章
        List<Long> readList = messageMapper.listByUser(userid);
        // 得到所有的文章
        List<MessageWithoutContentResponse> list = messageMapper.list();
        // 创建返回值
        List<UserMessageResponse> list1 = CopyUtil.copyList(list, UserMessageResponse.class);
        // 给阅读过的文章赋值: isread = true
        progress(readList, list1);
        return list1;
    }

    private void progress(List<Long> list1, List<UserMessageResponse> list2) {
        HashMap<Long, UserMessageResponse> map = new HashMap<>();
        list2.forEach(i -> map.put(i.getId(), i));

        for (Long id : list1) {
            UserMessageResponse message = map.get(id);
            message.setIsread(true);
        }
    }

    public List<MessageWithoutContentResponse> limitList(Integer num) {
        return messageMapper.limitList(num);
    }

    public Integer read(RecordRequest recode) {
        // 通过 id 查询 recode
        RecordExample example = new RecordExample();
        RecordExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(recode.getId()).andUseridEqualTo(recode.getUserid());
        List<Record> records = recordMapper.selectByExample(example);
        // 复制
        Record record1 = CopyUtil.copy(recode, Record.class);
        // 判断 records 是否为空
        if (CollectionUtils.isEmpty(records)) { // 该用户还未读过该文章
            recordMapper.insert(record1);
            return 1;
        } else { // 该用户是读过该文章的 => 直接返回
            return 0;
        }
    }
}
