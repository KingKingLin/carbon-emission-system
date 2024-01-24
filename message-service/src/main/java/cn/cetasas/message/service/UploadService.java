package cn.cetasas.message.service;

import cn.cetasas.message.exception.BusinessException;
import cn.cetasas.message.exception.BusinessExceptionCode;
import cn.cetasas.message.mapper.MImageMapper;
import cn.cetasas.message.mapper.MVideoMapper;
import cn.cetasas.message.req.MImageInsertRequest;
import cn.cetasas.message.req.MVideoInsertRequest;
import cn.cetasas.message.utils.NacosUtils;
import cn.cetasas.message.utils.SnowFlake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@Service
public class UploadService {

    private final static Logger LOG = LoggerFactory.getLogger(UploadService.class);

    @Resource
    private SnowFlake snowFlake;

    @Resource
    private MImageMapper mFileMapper;

    @Resource
    private MVideoMapper mVideoMapper;

    @Resource
    private NacosUtils nacosUtils;

    @Value("${gateway_domain}")
    private String gateway_domain;

    public String upload_image(MultipartFile file) {
        try {
            String suffix = getSuffix(file);
            LOG.info("文件的后缀名为: {}", suffix);
            long id = snowFlake.nextId();
            LOG.info("生成文件名的id为：{}", id);

            MImageInsertRequest mfile = new MImageInsertRequest();
            mfile.setId(id);
            mfile.setContent(file.getBytes());
            mfile.setFilename(id + suffix);
            mfile.setSuffix(suffix);
            mFileMapper.insert(mfile);

            String gateway = nacosUtils.getGatewayAddress("gateway");
            LOG.info("获取到 gatewaty 的 ip 地址：{}", gateway);
//            return gateway + id;
//            return "http://" + gateway + "/message/api/image/" + id + suffix; // 部署前
//            return gateway_domain + "/message/api/image/" + id + suffix;      // 部署后
            return "/message/api/image/" + id + suffix;                         // 通用
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(BusinessExceptionCode.UPLOAD_IMAGE_FAILED);
        }
    }

    private String getSuffix(MultipartFile file) {
        return file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
    }

    public String upload_video(MultipartFile file) {
        try {
            String suffix = getSuffix(file);
            LOG.info("文件的后缀名为: {}", suffix);
            long id = snowFlake.nextId();
            LOG.info("生成文件名的id为：{}", id);

            MVideoInsertRequest mfile = new MVideoInsertRequest();
            mfile.setId(id);
            mfile.setContent(file.getBytes());
            mfile.setFilename(id + suffix);
            mfile.setSuffix(suffix);
            mVideoMapper.insert(mfile);

            String gateway = nacosUtils.getGatewayAddress("gateway");
            LOG.info("获取到 gatewaty 的 ip 地址：{}", gateway);
//            return gateway + id;
//            return "http://" + gateway + "/message/api/video/" + id + suffix; // 部署前
//            return gateway_domain + "/message/api/video/" + id + suffix;      // 部署后
            return "/message/api/video/" + id + suffix;                         // 通用
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(BusinessExceptionCode.UPLOAD_VIDEO_FAILED);
        }
    }
}
