package cn.cetasas.message.controller;

import cn.cetasas.message.exception.BusinessException;
import cn.cetasas.message.resp.CommonResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ExceptionController {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionController.class);

    /**
     * 拦截业务异常
     */
    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public CommonResp<Object> businessExceptionHandler(BusinessException e) {
        CommonResp<Object> commonResp = new CommonResp<>();
        LOG.warn("业务异常：{}", e.getCode().getDesc());
        commonResp.setSuccess(false);
        commonResp.setMessage(e.getCode().getDesc());
        return commonResp;
    }

    /**
     * validation 校验，出现异常都会被它捕获
     * 校验异常统一处理
     */
    @ExceptionHandler(value = BindException.class)
    @ResponseBody
    public CommonResp<Object> validExceptionHandler(BindException e) {
        CommonResp<Object> commonResp = new CommonResp<>();
        LOG.warn("参数校验失败：{}", e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        commonResp.setSuccess(false);
        commonResp.setMessage(e.getBindingResult().getAllErrors().get(0).getDefaultMessage()); // 拿到校验失败的信息
        return commonResp;
    }

    /**
     * 校验异常统一处理
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public CommonResp<Object> systemExceptionHandler(Exception e) {
        CommonResp<Object> commonResp = new CommonResp<>();
        LOG.error("系统异常：", e);
        commonResp.setSuccess(false);
        commonResp.setMessage("系统出现异常，请联系管理员");
        return commonResp;
    }
}
