## Maven的构建过程

1. clean 清理：删除以前编译的结果
2. validate 验证：验证项目是否正确且所有必须信息是可用的
3. compile 编译：源代码编译
4. test 测试：使用适当的单元测试框架（例如JUnit）运行测试
5. package 打包：创建 JAR/WAR包，如在 pom.xml 中定义提及的包
6. verify 检查：对集成测试的结果进行检查，以保证质量达标
7. install 安装：安装打包的项目到本地仓库
8. deploy 部署：拷贝最终的工程包到远程仓库中，以共享给其他开发人员和工程



## DependencyManagement

### 锁定版本

```xml
<!-- dependencyManagement的作用是：锁定版本，且子模块继承之后不需要再写 groupId 和 版本号 -->
<dependencyManagement>
    <dependencies>
        <!-- SpringBoot 版本选择：2.2.2.RELEASE -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-parent</artifactId>
            <version>2.2.2.RELEASE</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <!-- SpringCloud 版本选择：Hoxton.SR1 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>Hoxton.SR1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <!-- SpringCloud Alibaba 依赖 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-alibaba-dependencies</artifactId>
            <version>2.1.0.RELEASE</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```



1. `dependencyManagement`：<font color=red>锁定版本，且子模块继承之后不需要再写 groupId 和 版本号</font>
2. `dependencyManagement` 是 Maven 用来提供一种管理依赖版本号的方式。通常会在一个项目的最顶层的父Pom文件中使用该元素
3. 在Pom文件中使用了 `dependencyManagement`  元素能让所有在子项目中引用一个依赖而不用显示列出版本号。<font color=blue>Maven 会沿着父子层次向上走，**直到找到一个拥有 `dependencyManagement` 元素的项目**，然后它就会使用该元素指定的版本号</font>
4. <font color=red>使用了 `dependencyManagement` 就可以使多个子项目都引用同一样依赖，可以**避免在每个使用的子项目中都去声明版本号**</font>。这样当想要升级或者切换版本时，就只需要在父Pom中更新即可。如果子项目需要另外的版本号，则在自己的Pom文件中声明版本号即可
5. <font color=blue>`dependencyManagement` 只是声明了依赖，**并不会实现引入**，因此子项目中需要显示的声明需要使用到的依赖，才会引入相关Jar包</font>
6. 如果不在子项目中声明依赖，是不会从父项目中继承依赖的；只有当子项目中写入该依赖，并且没有指定版本号，才会从父项目中继承该依赖，并且version和scope都读取自父Pom文件
7. 如果子项目中指定其他版本号，那么就会使用子项目中指定版本号的jar



## 依赖

1. compile 依赖范围

   - 参与打包，参与部署。对主程序和测试程序均有用（默认的依赖）

   - ```xml
     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter</artifactId>
         <version>2.1.0.RELEASE</version>
     </dependency>
     ```

2. test 依赖范围

   - 对测试程序有用，不参与打包和部署（例子：JUnit 的依赖）

   - ```xml
     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-test</artifactId>
         <version>2.1.0.RELEASE</version>
         <scope>test</scope>
     </dependency>
     ```

3. provider 依赖范围

   - 对主程序和测试程序均有效，但是不参与打包和部署

   - ```xml
     <dependency>
         <groupId>org.apache.tomcat.embed</groupId>
         <artifactId>tomcat-embed-core</artifactId>
         <version>9.0.19</version>
         <scope>provided</scope>
     </dependency>
     ```



### 排除依赖

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>5.2.0.RELEASE</version>
    <!-- 排除依赖 -->
    <exclusions>
        <exclusion>
            <groupId>commons-logging</groupId>
            <artifactId>commons-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```









## Springboot打包插件

1. 添加 maven 的打包插件

   ```xml
   <!-- 普通项目的打包插件 -->
   <build>
       <plugins>
           <plugin>
               <groupId>org.apache.maven.plugins</groupId>
               <artifactId>maven-compiler-plugin</artifactId>
               <version>3.8.1</version>
               <configuration>
                   <source>1.8</source>
                   <target>1.8</target>
               </configuration>
           </plugin>
       </plugins>
   </build>
   
   <!-- springboot的打包插件 -->
   <!--
    SpringBoot的Maven插件, 能够以Maven的方式为应用提供SpringBoot的支持，可以将
    SpringBoot应用打包为可执行的jar或war文件, 然后以通常的方式运行SpringBoot应用
   -->
   <build>
       <plugins>
           <plugin>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-maven-plugin</artifactId>
               <!--解决SpringBoot打包成jar后运行提示没有主清单属性-->
               <executions>
                   <execution>
                       <goals>
                           <goal>repackage</goal>
                       </goals>
                   </execution>
               </executions>
           </plugin>
       </plugins>
   </build>
   ```

2. 然后执行 mvn package 命令进行打包操作





