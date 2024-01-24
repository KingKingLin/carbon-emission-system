package cn.cetasas.message.controller;

import cn.cetasas.message.req.MessagePostRequest;
import cn.cetasas.message.req.MessageReviseRequest;
import cn.cetasas.message.req.RecordRequest;
import cn.cetasas.message.req.UserRequest;
import cn.cetasas.message.resp.*;
import cn.cetasas.message.service.ApiService;
import cn.cetasas.message.service.MFileService;
import cn.cetasas.message.service.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/message/api")
public class ApiController {

    private final static Logger LOG = LoggerFactory.getLogger(ApiController.class);

    @Resource
    private ApiService apiService;

    @Resource
    private UploadService uploadService;

    @Resource
    private MFileService mFileService;

    @PostMapping("/login")
    public CommonResp<UserResponse> login(@Valid @RequestBody UserRequest user) {
        CommonResp<UserResponse> resp = new CommonResp<>();
        LOG.info("用户【{}】尝试登录", user.getUsername());
        UserResponse login = apiService.login(user);
        LOG.info("用户【{}】登录成功", user.getUsername());
        resp.setContent(login);
        resp.setMessage("登录成功");
        return resp;
    }

    // 1) 创建消息体存储 mysql
    // 2) 推送消息
    @PostMapping("/publish")
    public CommonResp<Long> publish(@Valid @RequestBody MessagePostRequest message) {
        CommonResp<Long> resp = new CommonResp<>();
        LOG.info("管理员向全体用户推送了一条消息");
        long id = apiService.publish(message);
        resp.setContent(id);
        resp.setMessage("已推送");
        return resp;
    }

    // 修改
    //    修改最后创建时间 内容 和 标题
    //    返回修改后的 message 信息
    @PostMapping("/revise")
    public CommonResp<Boolean> revise(@Valid @RequestBody MessageReviseRequest message) {
        CommonResp<Boolean> resp = new CommonResp<>();
        LOG.info("管理员正在修改消息【{}】", message.getId());
        apiService.revise(message);
        resp.setMessage("修改成功");
        resp.setContent(true);
        return resp;
    }

    // 获取库中所有的 message
    @GetMapping("/list")
    public CommonResp<List<MessageWithoutContentResponse>> list() {
        CommonResp<List<MessageWithoutContentResponse>> resp = new CommonResp<>();
        List<MessageWithoutContentResponse> content = apiService.list();
        resp.setContent(content);
        return resp;
    }

    // 返回最新的五条消息
    @GetMapping("/list/limit/{num}")
    public CommonResp<List<MessageWithoutContentResponse>> limitList(@PathVariable("num") Integer num) {
        CommonResp<List<MessageWithoutContentResponse>> resp = new CommonResp<>();
        List<MessageWithoutContentResponse> content = apiService.limitList(num);
        resp.setContent(content);
        return resp;
    }

    // 获取库中所有的 message，带上该用户是否已读过
    @GetMapping("/list/{userid}")
    public CommonResp<List<UserMessageResponse>> listByUser(@PathVariable("userid") String userid) {
        CommonResp<List<UserMessageResponse>> resp = new CommonResp<>();
        List<UserMessageResponse> content = apiService.listByUser(userid);
        resp.setContent(content);
        return resp;
    }

    // 根据 id 获取某一个 message
    @GetMapping("/get/{id}")
    public CommonResp<MessageResponse> get(@PathVariable("id") Long id) {
        CommonResp<MessageResponse> resp = new CommonResp<>();
        MessageResponse message = apiService.selectByPrimaryKey(id);
        resp.setContent(message);
        return resp;
    }

    // 删除 根据 id 山粗
    @DeleteMapping("/delete/{id}")
    public CommonResp<Boolean> delete(@PathVariable("id") Long id) {
        CommonResp<Boolean> resp = new CommonResp<>();
        apiService.delete(id);
        resp.setContent(true);
        resp.setMessage("删除成功");
        return resp;
    }

    // 获取该用户未读的消息
    @GetMapping("/not-read/{userid}")
    public CommonResp<Integer> getNotRead(@PathVariable("userid") String userid) {
        CommonResp<Integer> resp = new CommonResp<>();
        Integer content = apiService.getNotReadCount(userid);
        resp.setContent(content);
        return resp;
    }

    @PostMapping("/read")
    public CommonResp<Integer> read(@Valid @RequestBody RecordRequest recode) {
        CommonResp<Integer> resp = new CommonResp<>();
        Integer content = apiService.read(recode);
        resp.setContent(content);
        resp.setMessage("已经阅读");
        return resp;
    }

    @PostMapping("/upload-image")
    public CommonResp<UploadImage> upload_image(@RequestParam("file") MultipartFile file) {
        LOG.info("上传图片");
        CommonResp<UploadImage> resp = new CommonResp<>();
        String src = uploadService.upload_image(file);
        UploadImage data = new UploadImage();
        data.setUrl(src);
        resp.setContent(data);
        resp.setMessage("上传成功");
        LOG.info("上传成功");
        return resp;
    }

    @PostMapping("/upload-video")
    public CommonResp<UploadVideo> upload_video(@RequestParam("file") MultipartFile file) {
        LOG.info("上传视频");
        CommonResp<UploadVideo> resp = new CommonResp<>();
        String src = uploadService.upload_video(file);
        UploadVideo data = new UploadVideo();
        data.setUrl(src);
        resp.setContent(data);
        resp.setMessage("上传成功");
        LOG.info("上传成功");
        return resp;
    }

//    @GetMapping("/image/{filename}")
//    public ResponseEntity<byte[]> getImage(@PathVariable("filename") String filename) {
//        String[] split = filename.split("\\.");
//        long id = Long.parseLong(split[0]);
//        byte[] data = mFileService.getImage(id);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.IMAGE_JPEG);
//        return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
//    }

    @GetMapping("/image/{filename}")
    public void getImage(@PathVariable("filename") String filename, HttpServletResponse response) {
        String[] split = filename.split("\\.");
        long id = Long.parseLong(split[0]);
        mFileService.getImage(id, response);
    }

    @GetMapping("/video/{filename}")
    public void getVideo(@PathVariable("filename") String filename, HttpServletResponse response) {
        String[] split = filename.split("\\.");
        long id = Long.parseLong(split[0]);
        mFileService.getVideo(id, response);
    }

    @GetMapping("/download/image/{filename}")
    public void downloadImage(@PathVariable("filename") String filename, HttpServletResponse response) {
        String[] split = filename.split("\\.");
        long id = Long.parseLong(split[0]);
        mFileService.downloadImage(id, response);
    }

    @GetMapping("/download/video/{filename}")
    public void downloadVideo(@PathVariable("filename") String filename, HttpServletResponse response) {
        String[] split = filename.split("\\.");
        long id = Long.parseLong(split[0]);
        mFileService.downloadVideo(id, response);
    }
}
