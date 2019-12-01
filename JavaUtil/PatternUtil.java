package com.example.controller.util;

import java.util.regex.Pattern;

/**
 * 常用的正则表达式
 */
public class PatternUtil {

    public static void main(String[] args) {
        /**
         * 匹配中文
         */
        Pattern.compile("\\u4e00-\\u9fa5");

        /**
         * 匹配英文和数字
         */
        Pattern.compile("0-9a-zA-Z");

        /**
         * 匹配一个字符串中是否包含:  中文，英文字母和数字及下划线
         */
        Pattern.compile("^[\\u4e00-\\u9fa5_a-zA-Z0-9]+$");

        /**
         * 匹配中英文,数字和字符(/ , -)
         */
        Pattern.compile("^[\\u4e00-\\u9fa5\\-/,a-zA-Z0-9]+$");

        /**
         * 匹配手机号码
         */
        Pattern.compile("^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\\d{8}$");
        Pattern.compile("^1[0-9]\\d{10}$");

        /**
         * 匹配身份证
         */
        Pattern.compile("(^\\d{15}$)|(^\\d{17}([0-9]|X|x)$)");

        Pattern.compile("d{15}|d{18}");
    }

}
