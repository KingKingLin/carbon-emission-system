package cn.cetasas.user.util;

import cn.cetasas.user.exception.BusinessException;
import cn.cetasas.user.exception.BusinessExceptionCode;
import cn.cetasas.user.pojo.Mail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.Date;

@Component
public class MailUtil {

    private final static Logger LOG = LoggerFactory.getLogger(MailUtil.class);

    @Resource
    private JavaMailSenderImpl javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    // 主题
    private final static String subject = "中国省际碳排放时空分析系统: 注册验证码 ?";

    // 内容
    private final static String text = "<body>\n" +
            "<h3>亲爱的用户！</h3>\n" +
            "<span>这是您的验证码 <font color=\"red\">?</font>，请在 60 秒内使用。</span></br>\n" +
            "——来自中国省际碳排放时空分析系统\n" +
            "</body>";

    /**
     * 检测邮件信息类
     * @param to
     * @param subject
     * @param text
     */
    private void checkMail(String to,String subject,String text) {
        if (StringUtils.isEmpty(to)) {
            throw new RuntimeException("邮件收信人不能为空");
        }
        if (StringUtils.isEmpty(subject)) {
            throw new RuntimeException("邮件主题不能为空");
        }
        if (StringUtils.isEmpty(text)) {
            throw new RuntimeException("邮件内容不能为空");
        }
    }

    // 给该邮箱发送 "验证码"
    @Async
    public void sendVerification(Mail mail, String verification) {
        try {
            //true 代表支持复杂的类型
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(javaMailSender.createMimeMessage(),true);
            //邮件发信人
            mimeMessageHelper.setFrom(sender);
            //邮件收信人  1或多个
//            mimeMessageHelper.setTo(to.split(","));
            mimeMessageHelper.setTo(mail.getMail());
            //邮件主题
            mimeMessageHelper.setSubject(subject.replace("?", verification));
            //邮件内容
            //发送纯文本邮件
//            mimeMessageHelper.setText(text.replace("?", verification));
            //发送html邮件
            mimeMessageHelper.setText(text.replace("?", verification), true);
            //邮件发送时间 => 马上发送
            mimeMessageHelper.setSentDate(new Date());

            //发送邮件
            javaMailSender.send(mimeMessageHelper.getMimeMessage());
            LOG.info("发送邮件成功：{} -> {}", sender, mail.getMail());
        } catch (MessagingException e) {
            throw new BusinessException(BusinessExceptionCode.SEND_MAIL_ERROR);
        }
    }
}
