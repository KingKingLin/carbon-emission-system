package cn.cetasas.message.mapper;

import cn.cetasas.message.req.MessagePostDBRequest;
import cn.cetasas.message.req.MessageReviseDBRequest;
import cn.cetasas.message.req.MessageReviseRequest;
import cn.cetasas.message.resp.MessageResponse;
import cn.cetasas.message.resp.MessageWithoutContentResponse;
import cn.cetasas.message.resp.UserMessageResponse;

import java.util.List;

public interface MessageMapper {
    void publish(MessagePostDBRequest message);

    List<MessageWithoutContentResponse> list();

    MessageResponse selectByPrimaryKey(Long id);

    void revise(MessageReviseDBRequest message);

    void delete(Long id);

    Integer getNotReadCount(String userid);

    List<Long> listByUser(String userid);

    List<MessageWithoutContentResponse> limitList(Integer num);
}