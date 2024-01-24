package cn.cetasas.message.service;

import cn.cetasas.message.websocket.WebSocketServer;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class WsService {
    @Resource
    private WebSocketServer webSocketServer;

    @Async
    public void sendInfo(String message) {
        // 推送消息
        webSocketServer.sendInfo(message);
    }
}
