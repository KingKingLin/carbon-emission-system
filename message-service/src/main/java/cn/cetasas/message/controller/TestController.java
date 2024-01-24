//package cn.cetasas.message.controller;
//
//import cn.cetasas.message.service.WsService;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.annotation.Resource;
//
//@RestController
//@RequestMapping("/message/test")
//public class TestController {
//
////    @Resource
////    private DBClient dbClient;
////
////    @GetMapping
////    public CommonResp<List<CESResp>> testFeign(@PathParam("year") String year) {
////        return dbClient.getAllProvince(year);
////    }
//
//    @GetMapping("/hello")
//    public String hello() {
//        return "hello";
//    }
//
//    @Resource
//    private WsService wsService;
//
//    @GetMapping("/sendMessage")
//    public void sendMessage() {
//        wsService.sendInfo("发送消息");
//    }
//}
