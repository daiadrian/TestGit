package com.dai.mail;

import com.dai.mail.entity.MailVo;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeUtility;
import java.io.File;
import java.io.InputStream;
import java.util.Date;

/**
 * @author: daihao
 * @date: 2019/9/20 16:23
 */
@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private JavaMailSenderImpl javaMailSender;

    /**
     * 发送邮件
     * @param mailVo
     */
    public void sendMail(MailVo mailVo) {
        try {
            checkMail(mailVo);
            sendMail(mailVo, null, null, null);
        } catch (Exception e) {
            logger.error("发送邮件失败:", e);
        }
    }

    /**
     * 检测邮件信息内容
     * @param mailVo
     */
    private static void checkMail(MailVo mailVo) throws Exception {
        if (StringUtils.isEmpty(mailVo.getTo())) {
            throw new RuntimeException("邮件收信人不能为空");
        }
        if (StringUtils.isEmpty(mailVo.getSubject())) {
            throw new RuntimeException("邮件主题不能为空");
        }
        if (StringUtils.isEmpty(mailVo.getText())) {
            throw new RuntimeException("邮件内容不能为空");
        }
        if (mailVo.getIsHtml() == null) {
            throw new RuntimeException("请先确认文本是否为HTML文本");
        }
    }

    /**
     * 发送邮件
     *
     * @param mailVo
     */
    private void sendMail(MailVo mailVo, InputStream inputStream, String attachmentName, File file) throws Exception {
        try {
            /**
             * true表示支持复杂类型
             */
            MimeMessageHelper messageHelper = new MimeMessageHelper(javaMailSender.createMimeMessage(), true, "utf-8");
            /**
             * 邮件发信人
             */
            mailVo.setFrom(mailVo.getFrom());
            /**
             * 邮件收信人
             */
            messageHelper.setTo(mailVo.getTo().split(","));
            /**
             * 邮件主题
             */
            messageHelper.setSubject(mailVo.getSubject());
            /**
             * 邮件内容
             */
            messageHelper.setText(mailVo.getText(), mailVo.getIsHtml());
            /**
             * 抄送
             */
            if (!org.springframework.util.StringUtils.isEmpty(mailVo.getCc())) {
                messageHelper.setCc(mailVo.getCc().split(","));
            }
            /**
             * 密送
             */
            if (!org.springframework.util.StringUtils.isEmpty(mailVo.getBcc())) {
                messageHelper.setCc(mailVo.getBcc().split(","));
            }
            /**
             * 添加邮件附件
             */
            if (null != inputStream) {
                /**
                 * 对于在内存中生成的文件，可以使用 ByteArrayResource
                 * 只需使用 Apache Commons 的 IOUtils 转换 InputStream 对象
                 *
                 * ------导入commons-io包
                 */
                messageHelper.addAttachment(MimeUtility.encodeWord(attachmentName), new ByteArrayResource(IOUtils.toByteArray(inputStream)));
            }
            /**
             * 添加邮件附件
             */
            if (null != file) {
                messageHelper.addAttachment(MimeUtility.encodeWord(attachmentName), file);
            }
            /**
             * 发送时间
             */
            if (org.springframework.util.StringUtils.isEmpty(mailVo.getSentDate())) {
                mailVo.setSentDate(new Date());
                messageHelper.setSentDate(mailVo.getSentDate());
            }
            //正式发送邮件
            javaMailSender.send(messageHelper.getMimeMessage());
            logger.info("发送邮件成功：{}->{}", mailVo.getFrom(), mailVo.getTo());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
