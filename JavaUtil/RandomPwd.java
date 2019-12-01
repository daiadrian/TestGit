package com.example.controller;


import java.util.regex.Pattern;

/**
 * 生成随机8位数密码
 *
 * (char) (rand * ('9' - '0') + '0') 相当于 (char) (rand * '9' - rand * '0' + '0')
 *
 * 可以理解成:
 *      如果要获取 40-30 中的随机数，就可以是：
 *          (40 - 30) * rand + 30
 *
 */
public class RandomPwd {

    public static void main(String[] args) {
        Pattern upperCase = Pattern.compile(".*[A-Z]{1,6}.*");
        Pattern lowerCase = Pattern.compile(".*[a-z]{1,6}.*");
        Pattern numCase = Pattern.compile(".*[0-9]{1,6}.*");
        StringBuffer pwd = null;
        while (true) {
            pwd = new StringBuffer();
            for (int j = 0; j < 8; j++) {
                double rand = Math.random();
                double randTri = Math.random() * 3;
                if (randTri >= 0 && randTri < 1) {
                    pwd.append((char) (rand * ('9' - '0') + '0'));
                } else if (randTri >= 1 && randTri < 2) {
                    pwd.append((char) (rand * ('Z' - 'A') + 'A'));
                } else {
                    pwd.append((char) (rand * ('z' - 'a') + 'a'));
                }
            }
            String password = pwd.toString();
            if (upperCase.matcher(password).matches() && lowerCase.matcher(password).matches()
                    && numCase.matcher(password).matches()) {
                break;
            }
        }
        System.out.println(pwd.toString());
    }

}