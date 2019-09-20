package com.dai.mail.utils;

import com.dai.mail.entity.MailVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeUtility;
import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

/**
 * 邮件发送工具类
 *
 * @author: daihao
 * @date: 2019/9/19 17:35
 */
@Slf4j
public class EmailUtils {

    private static volatile JavaMailSenderImpl sender = null;
    private static String EMAIL_HOST = "";
    private static String EMAIL_USERNAME = "";
    private static String EMAIL_PASSWORD = "";
    private static String EMAIL_TIMEOUT = "";
    private static String EMAIL_FROM = "";

    /**
     * 获取 JavaMailSenderImpl
     * @return
     */
    private static JavaMailSenderImpl getMailSender() {
        if ( null == sender ) {
            synchronized ( EmailUtils.class ) {
                if ( null == sender ) {
                    sender = new JavaMailSenderImpl();
                    sender.setHost(EMAIL_HOST);
                    sender.setUsername(EMAIL_USERNAME);
                    sender.setPassword(EMAIL_PASSWORD);
                    sender.setDefaultEncoding("utf-8");
                    Properties p = new Properties();
                    p.setProperty("mail.smtp.timeout", EMAIL_TIMEOUT);
                    p.setProperty("mail.smtp.auth", "false");
                    p.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    sender.setJavaMailProperties(p);
                }
            }
        }
        return sender;
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
     * 检测带附件邮件信息内容
     * @param mailVo
     */
    private static void checkStreamMail(MailVo mailVo, String attachmentName) throws Exception {
        checkMail(mailVo);
        if (StringUtils.isBlank(attachmentName)) {
            throw new RuntimeException("附件名称不能为空");
        }
    }

    /**
     * 发送邮件
     *
     * @param mailVo
     */
    private static void sendMail(MailVo mailVo, InputStream inputStream, String attachmentName, File file) throws Exception {
        try {
            checkMail(mailVo);
            JavaMailSenderImpl mailSender = getMailSender();
            /**
             * true表示支持复杂类型
             */
            MimeMessageHelper messageHelper = new MimeMessageHelper(mailSender.createMimeMessage(), true, "utf-8");
            //邮件发信人
            if (StringUtils.isBlank(mailVo.getFrom())) {
                mailVo.setFrom(EMAIL_FROM);
            } else {
                mailVo.setFrom(mailVo.getFrom());
            }
            //邮件收信人
            messageHelper.setTo(mailVo.getTo().split(","));
            //邮件主题
            messageHelper.setSubject(mailVo.getSubject());
            //邮件内容
            messageHelper.setText(mailVo.getText(), mailVo.getIsHtml());
            //抄送
            if (!org.springframework.util.StringUtils.isEmpty(mailVo.getCc())) {
                messageHelper.setCc(mailVo.getCc().split(","));
            }
            //密送
            if (!org.springframework.util.StringUtils.isEmpty(mailVo.getBcc())) {
                messageHelper.setCc(mailVo.getBcc().split(","));
            }
            //添加邮件附件
            if (null != inputStream) {
                /**
                 * 对于在内存中生成的文件，可以使用 ByteArrayResource
                 * 只需使用 Apache Commons 的 IOUtils 转换 InputStream 对象
                 *
                 * ------导入commons-io包
                 */
                messageHelper.addAttachment(MimeUtility.encodeWord(attachmentName), new ByteArrayResource(IOUtils.toByteArray(inputStream)));
            }
            //添加邮件附件
            if (null != file) {
                messageHelper.addAttachment(MimeUtility.encodeWord(attachmentName), file);
            }
            //发送时间
            if (org.springframework.util.StringUtils.isEmpty(mailVo.getSentDate())) {
                mailVo.setSentDate(new Date());
                messageHelper.setSentDate(mailVo.getSentDate());
            }
            //正式发送邮件
            mailSender.send(messageHelper.getMimeMessage());
            log.info("发送邮件成功：{}->{}", mailVo.getFrom(), mailVo.getTo());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送普通文本邮件, 不带附件
     *
     * @param mailVo
     */
    public static void sendMail(MailVo mailVo) throws Exception {
        checkMail(mailVo);
        sendMail(mailVo, null, null, null);
    }

    /**
     * 发送带具体文件的附件邮件 (默认file的名称)
     *
     * @param mailVo
     */
    public static void sendMail(MailVo mailVo, File file) throws Exception {
        checkMail(mailVo);
        sendMail(mailVo, null, file.getName(), file);
    }

    /**
     * 发送带具体文件的附件邮件
     *
     * @param mailVo
     */
    public static void sendMail(MailVo mailVo, File file, String attachmentName) throws Exception {
        checkMail(mailVo);
        sendMail(mailVo, null, attachmentName, file);
    }

    /**
     * 发送流式附件的邮件
     *
     * @param mailVo
     */
    public static void sendMail(MailVo mailVo, InputStream inputStream, String attachmentName) throws Exception {
        checkStreamMail(mailVo, attachmentName);
        sendMail(mailVo, inputStream, attachmentName, null);
    }

}
