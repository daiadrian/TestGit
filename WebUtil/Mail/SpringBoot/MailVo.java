package com.dai.mail.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 邮件信息类
 *
 * 例子:
 *      MailVo.builder()
 *              .to(toName)
 *              .text(text)
 *              .subject(subject)
 *              .isHtml(false)
 *              .from(from)
 *              .build();
 *
 * @author: daihao
 * @date: 2019/9/19 17:35
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MailVo implements Serializable {

    /**
     * 邮件发送人
     */
    private String from;

    /**
     * 邮件接收人（多个邮箱则用逗号","隔开）
     */
    private String to;

    /**
     * 邮件主题
     */
    private String subject;

    /**
     * 邮件内容
     */
    private String text;

    /**
     * 是否是HTML文本
     */
    private Boolean isHtml;

    /**
     * 抄送（多个邮箱则用逗号","隔开）
     */
    private String cc;

    /**
     * 密送（多个邮箱则用逗号","隔开）
     */
    private String bcc;

    /**
     * 发送时间
     */
    private Date sentDate;

}