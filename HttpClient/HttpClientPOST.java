package com.dai;

import com.alibaba.fastjson.JSON;
import com.dai.aop.dao.User;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 * HttpClient POST请求
 */
public class HttpClientPOST {

    public static void main(String[] args) {
        /**
         * 得到HttpClient客户端
         */
        CloseableHttpClient client = HttpClientBuilder.create().build();

        CloseableHttpResponse response = null;
        try {
            /**
             * 创建POST请求,可以设置URI信息来装载参数
             */
            List<NameValuePair> params = new LinkedList<>();
            params.add(new BasicNameValuePair("java", "spring"));
            URI uri = new URIBuilder().setScheme("http")
                    .setHost("localhost")
                    .setPort(8080)
                    .setPath("/httpPost")
                    //设置参数的两种方式
                    .setParameter("java", "spring")
                    .setParameters(params)
                    .build();

            HttpPost httpPost = new HttpPost(uri);
            //设置content-type
            httpPost.setHeader("Content-Type", "application/json;charset=utf8");

            // 创建user对象
            User user = new User();
            user.setName("dh");
            user.setAge(22);
            /**
             * 将对象参数设置到httpPost
             */
            httpPost.setEntity(new StringEntity(JSON.toJSONString(user), "UTF-8"));

            /**
             * 设置额外的配置信息
             */
            RequestConfig requestConfig = RequestConfig.custom()
                                                    // 设置连接超时时间(单位毫秒)
                                                    .setConnectTimeout(5000)
                                                    // 设置请求超时时间(单位毫秒)
                                                    .setConnectionRequestTimeout(5000)
                                                    // socket读写超时时间(单位毫秒)
                                                    .setSocketTimeout(5000)
                                                    // 设置是否允许重定向(默认为true)
                                                    .setRedirectsEnabled(true)
                                                    .build();

            // 将上面的配置信息 运用到这个POST请求里
            httpPost.setConfig(requestConfig);

            // 由客户端执行(发送)GET请求
            response = client.execute(httpPost);
            // 从响应模型中获取响应实体
            HttpEntity entity = response.getEntity();

            System.out.println("响应状态为:" + response.getStatusLine());
            if (entity != null) {
                //可以利用EntityUtils来得到响应内容
                //乱码的情况下可以使用EntityUtils.toString(entity, "UTF-8")指定Charset
                System.out.println("响应内容长度为:" + entity.getContentLength());
                System.out.println("响应内容为:" + EntityUtils.toString(entity));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != client) client.close();
                if (null != response) response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
