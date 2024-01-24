package cn.cetasas.message.service;

import cn.cetasas.message.exception.BusinessException;
import cn.cetasas.message.exception.BusinessExceptionCode;
import cn.cetasas.message.mapper.MImageMapper;
import cn.cetasas.message.mapper.MVideoMapper;
import cn.cetasas.message.pojo.MImage;
import cn.cetasas.message.pojo.MVideo;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

@Service
public class MFileService {
    private final static Logger LOG = LoggerFactory.getLogger(MFileService.class);

    @Resource
    private MImageMapper mImageMapper;

    @Resource
    private MVideoMapper mVideoMapper;

    public void downloadImage(long id, HttpServletResponse response) {
        MImage mImage = mImageMapper.selectByPrimaryKey(id);

        if (ObjectUtils.isEmpty(mImage) || ObjectUtils.isEmpty(mImage.getContent())) {
            throw new BusinessException(BusinessExceptionCode.IMAGE_NOT_EXIST);
        }

        InputStream in = null;
        OutputStream out = null;
        try {
            // 1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
            response.setContentType("image/*");
            // 2.设置文件头：最后一个参数是设置下载文件名
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(mImage.getFilename(), "utf-8"));
            // 3.将 Content-Disposition 放出让前端可以访问
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            // 4.传输数据
            in = new ByteArrayInputStream(mImage.getContent());
            out = response.getOutputStream();
            int bytes = IOUtils.copy(in, out);
            LOG.info("File Written with {} bytes", bytes);
            out.flush();
        } catch (IOException e) {
            throw new BusinessException(BusinessExceptionCode.DOWNLOAD_IMAGE_FAILED);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void downloadVideo(long id, HttpServletResponse response) {
        MVideo mVideo = mVideoMapper.selectByPrimaryKey(id);

        if (ObjectUtils.isEmpty(mVideo) || ObjectUtils.isEmpty(mVideo.getContent())) {
            throw new BusinessException(BusinessExceptionCode.VIDEO_NOT_EXIST);
        }

        InputStream in = null;
        OutputStream out = null;
        try {
            // 1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
//            response.setContentType("video/mp4"); // 视频文件
            response.setContentType("video/*"); // 视频文件
            // 2.设置文件头：最后一个参数是设置下载文件名
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(mVideo.getFilename(), "utf-8"));
            // 3.将 Content-Disposition 放出让前端可以访问
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            // 4.传输数据
            in = new ByteArrayInputStream(mVideo.getContent());
            out = response.getOutputStream();
            int bytes = IOUtils.copy(in, out);
            LOG.info("File Written with {} bytes", bytes);
            out.flush();
        } catch (IOException e) {
            throw new BusinessException(BusinessExceptionCode.DOWNLOAD_VIDEO_FAILED);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] getImage(long id) {
        MImage mImage = mImageMapper.selectByPrimaryKey(id);

        return mImage.getContent();
    }

    public void getVideo(long id, HttpServletResponse response) {
        MVideo mVideo = mVideoMapper.selectByPrimaryKey(id);

        if (ObjectUtils.isEmpty(mVideo) || ObjectUtils.isEmpty(mVideo.getContent())) {
            throw new BusinessException(BusinessExceptionCode.VIDEO_NOT_EXIST);
        }

        InputStream in = null;
        OutputStream out = null;
        try {
            // 1. 设置 content-type
//            response.setContentType("video/mp4"); // 视频文件
            response.setContentType("video/*"); // 视频文件
            // 2. 传输数据
            in = new ByteArrayInputStream(mVideo.getContent());
            out = response.getOutputStream();
            int bytes = IOUtils.copy(in, out);
            LOG.info("File Written with {} bytes", bytes);
            out.flush();
        } catch (IOException e) {
            throw new BusinessException(BusinessExceptionCode.DOWNLOAD_VIDEO_FAILED);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getImage(long id, HttpServletResponse response) {
        MImage mImage = mImageMapper.selectByPrimaryKey(id);

        if (ObjectUtils.isEmpty(mImage) || ObjectUtils.isEmpty(mImage.getContent())) {
            throw new BusinessException(BusinessExceptionCode.IMAGE_NOT_EXIST);
        }

        InputStream in = null;
        OutputStream out = null;
        try {
            // 1. 设置 content-type
            response.setContentType("image/*"); // 视频文件
            // 2. 传输数据
            in = new ByteArrayInputStream(mImage.getContent());
            out = response.getOutputStream();
            int bytes = IOUtils.copy(in, out);
            LOG.info("File Written with {} bytes", bytes);
            out.flush();
        } catch (IOException e) {
            throw new BusinessException(BusinessExceptionCode.DOWNLOAD_IMAGE_FAILED);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
