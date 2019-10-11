### maven的构建过程

1. clean 清理：删除以前编译的结果
2. validate 验证：验证项目是否正确且所有必须信息是可用的
3. compile 编译：源代码编译
4. test 测试：使用适当的单元测试框架（例如JUnit）运行测试
5. package 打包：创建 JAR/WAR包，如在 pom.xml 中定义提及的包
6. verify 检查：对集成测试的结果进行检查，以保证质量达标
7. install 安装：安装打包的项目到本地仓库
8. deploy 部署：拷贝最终的工程包到远程仓库中，以共享给其他开发人员和工程





### 依赖

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



### 打包成可运行jar包

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