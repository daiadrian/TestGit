#!/bin/bash

function backup(){
	dateStr=`date "+%Y%m%d%H%M"`
	if [[ -e $runningJarPath ]];then
        backUpFileName=${backUpJarPath}${dateStr}
        cp $runningJarPath $backUpFileName
        gzip $backUpFileName
    fi
}

function innerStart(){
	port=8099
    profile=stage
    #jvm_opts="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=58877 -Xmx1024m"
    jvm_opts="-Xmx512m"
	spring_opts="--spring.profiles.active=$profile --server.port=$port"
	if [[ -e $runningJarPath ]];then 
        echo "nohup /opt/jdk1.8.0_131/bin/java  $jvm_opts -jar $runningJarPath $spring_opts > $logPath" 
        nohup java $jvm_opts -jar $runningJarPath  $spring_opts > $logPath &
    else
        echo "$runningJarPath not found"
    fi

}

function start(){
	
	#先备份正在运行的jar包
	backup
	
	#将上传文件替换为runningJar	
	if [[ -e $uploadJarPath ]];then
		cp $uploadJarPath $runningJarPath
	fi
	
	#执行jar启动runningJar包
	innerStart
}

function stop(){
	username=nsb-usercenter
	password=BPxU0rew
	runningPort=`cat nsb-usercenter.port`
	if [[ -n $runningPort ]];then
		echo "curl http://127.0.0.1:$runningPort/eureka/offline?username=$username&password=$password"
    	result=`curl  "http://127.0.0.1:$runningPort/eureka/offline?username=$username&password=$password"`
    	sleep 8s	
	else	
		echo "cannot findd application port"
	fi

	runningPid=`cat nsb-usercenter.pid`
	if [[ -n $runningPid ]];then
		echo "kill -9 $runningPid"
	    kill -9 $runningPid
	else
		echo "cannot find application  pid"
	fi
}

function rollback(){
	rollbackPath=$1
	if [[ -e $rollbackPath ]];then
		backupFileName=`echo $rollbackPath | cut -d "." -f 1-3`
		gunzip -c $rollbackPath > $backupFileName
		if [[ -e $backupFileName ]];then
			stop
			mv $backupFileName $runningJarPath
			innerStart
		else
			echo "cannot find $backupFileName"
		fi
	else
		echo "cannot find $rollbackPath"
	fi
}


applicationName=nsb-usercenter
runningJarPath=${applicationName}-running.jar
uploadJarPath=${applicationName}.jar
backUpJarPath=${applicationName}.jar.bak
logPath=${applicationName}.log


curr_user=`whoami`
if [ $curr_user != "root" ];then
echo "only root can run  application"
exit 1
fi
case $1 in
"start")
	start
	;;
"stop")
	stop
	;;
"rollback")
	rollback $2
	;;
*)
	echo "operation not found"
esac

