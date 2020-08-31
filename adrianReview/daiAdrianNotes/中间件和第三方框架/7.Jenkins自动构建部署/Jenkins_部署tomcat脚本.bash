#!/bin/sh
#kill tomcat pid
#这句尤为重要，解决jenkins杀死衍生进程
export BUILD_ID=tomcat_jwcz_build_id

# 1.关闭tomcat
pidlist=`ps -ef|grep tomcat-jwcz|grep -v "grep"|awk '{print $2}'`
if [ "$pidlist" == "" ]
    then
        echo "----tomcat已经关闭----"
else
    echo "tomcat进程号 :$pidlist"
    kill -9 $pidlist
    echo "kill $pidlist:"
fi

pidlist2=`ps -ef|grep tomcat-jwcz|grep -v "grep"|awk '{print $2}'`
if [ "$pidlist2" == "" ]
    then 
       echo "----关闭tomcat成功----"
else
    echo "----关闭tomcat失败----"
fi

# 2.移除原来tomcat中webapps中的项目文件夹
rm -rf /usr/local/appBase/jwcz
sleep 3s
echo "----删除旧文件完毕----"

# 3.解压war包
unzip /usr/local/appBase/jwcz.war -d /usr/local/appBase/jwcz
sleep 1s
echo "----解压war包完毕----"

# 4.移除war包
rm -rf /usr/local/appBase/jwcz.war
sleep 1s
echo "----删除war包完毕----"

# 5.启动tomcat
cd /usr/local/tomcat-jwcz/bin
./startup.sh
echo "----启动tomcat----"