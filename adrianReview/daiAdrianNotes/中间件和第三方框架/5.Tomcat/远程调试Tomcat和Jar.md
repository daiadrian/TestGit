# 远程调试Tomcat和Springboot项目

## 远程调试Tomcat

1. 修改 `startup.sh` 文件

   ```sh
   exec "$PRGDIR"/"$EXECUTABLE" jpda start "$@"
   ```

   - 在文件最后一行，start 之前增加 `jpda` 参数项

2. 修改 `catalina.sh` 文件

   ```sh
   if [ "$1" = "jpda" ] ; then
     if [ -z "$JPDA_TRANSPORT" ]; then
       JPDA_TRANSPORT="dt_socket"
     fi
     if [ -z "$JPDA_ADDRESS" ]; then
       JPDA_ADDRESS="55555"
     fi
     if [ -z "$JPDA_SUSPEND" ]; then
       JPDA_SUSPEND="n"
     fi
     if [ -z "$JPDA_OPTS" ]; then
       JPDA_OPTS="-agentlib:jdwp=transport=$JPDA_TRANSPORT,address=$JPDA_ADDRESS,server=y,suspend=$JPDA_SUSPEND"
     fi
     CATALINA_OPTS="$JPDA_OPTS $CATALINA_OPTS"
     shift
   fi
   ```

   - 修改 `JPDA_ADDRESS` 的端口为任意端口

3. 在IDEA中创建 Remote 去连接远程机器的 `JPDA_ADDRESS` 端口即可进行调试





## 远程调试Springboot项目

```shell
java -Xdebug -Xrunjdwp:transport=dt_socket,address=55555,server=y,suspend=y -jar test.jar
```





# Tomcat监控

## 1.tomcat-manager

## 2.psi-probe监控