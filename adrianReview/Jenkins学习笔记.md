Jenkins学习笔记

### Jenkins卸载

```shell
find / | grep 'jenkins'
# 将找到的内容全部删除才能彻底的删除Jenkins（war包安装的形式）

#rpm形式安装的卸载方法
rpm -e jenkins
find / -iname jenkins | xargs -n 1000 rm -rf
```



### Jenkins安装

```shell
wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io.key

yum -y install jenkins


#更新Jenkins版本
yum update jenkins

#系统配置文件
cat /etc/sysconfig/jenkins
#存放jenkins 配置及工作文件
JENKINS_HOME="/var/lib/jenkins" 
#jenkins默认8080端口
JENKINS_PORT="8080" 

#管理员admin的密码
cat /var/lib/jenkins/secrets/initialAdminPassword

```



### 问题汇总

1. 启动 Jenkins 发生的错误

   ```shell
   Starting jenkins (via systemctl):  Job for jenkins.service failed because the control process exited with error code. See "systemctl status jenkins.service" and "journalctl -xe" for details.
   
   
   #解释：因为JDK的配置位置出现问题
   [root@iZwz9j4160krdqf7yfhj4nZ ~]# systemctl status jenkins.service
   ● jenkins.service - LSB: Jenkins Automation Server
      Loaded: loaded (/etc/rc.d/init.d/jenkins; bad; vendor preset: disabled)
      Active: failed (Result: exit-code) since Sun 2019-11-03 21:11:43 CST; 29s ago
        Docs: man:systemd-sysv-generator(8)
     Process: 22487 ExecStart=/etc/rc.d/init.d/jenkins start (code=exited, status=1/FAILURE)
   
   Nov 03 21:11:43 iZwz9j4160krdqf7yfhj4nZ systemd[1]: Starting LSB: Jenkins Automation Server...
   Nov 03 21:11:43 iZwz9j4160krdqf7yfhj4nZ runuser[22492]: pam_unix(runuser:session): session opened for...=0)
   Nov 03 21:11:43 iZwz9j4160krdqf7yfhj4nZ jenkins[22487]: Starting Jenkins bash: /usr/bin/java: No such...ory
   Nov 03 21:11:43 iZwz9j4160krdqf7yfhj4nZ systemd[1]: jenkins.service: control process exited, code=exi...s=1
   Nov 03 21:11:43 iZwz9j4160krdqf7yfhj4nZ jenkins[22487]: [FAILED]
   Nov 03 21:11:43 iZwz9j4160krdqf7yfhj4nZ systemd[1]: Failed to start LSB: Jenkins Automation Server.
   Nov 03 21:11:43 iZwz9j4160krdqf7yfhj4nZ systemd[1]: Unit jenkins.service entered failed state.
   Nov 03 21:11:43 iZwz9j4160krdqf7yfhj4nZ systemd[1]: jenkins.service failed.
   
   #从这里可以看到 Jenkins 的配置文件位置为：ExecStart=/etc/rc.d/init.d/jenkins
   #修改该文件中java的路径即可
   ```

   