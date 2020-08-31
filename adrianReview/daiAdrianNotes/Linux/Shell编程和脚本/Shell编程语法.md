

## Shell变量

### 变量命名规则

```shell
#!/bin/bash
java_name="Hello World!"
```

变量名和等号之间不能有空格。同时，变量名的命名须遵循如下规则：

- 命名只能使用英文字母，数字和下划线，首个字符不能以数字开头
- 中间不能有空格，可以使用下划线（_）
- 不能使用标点符号
- 不能使用bash里的关键字（可用help命令查看保留关键字）



### 变量类型

1. **局部变量** 
   - 局部变量在脚本或命令中定义
   - 仅在当前shell实例中有效，其他shell启动的程序不能访问局部变量

2. **环境变量** 
   - 所有的程序，包括shell启动的程序，都能访问环境变量
   - 有些程序需要环境变量来保证其正常运行
   - 必要的时候shell脚本也可以定义环境变量

3. **shell变量** 
   - shell变量是由shell程序设置的特殊变量
   - shell变量中有一部分是环境变量，有一部分是局部变量
   - 这些变量保证了shell的正常运行



### 变量使用规则

```powershell
java_name="Hello World!"
echo $your_name
echo ${your_name}
```

两种方式都可以，但建议带上中括号，帮助解析器识别变量边界



**<font color=orange>只读变量</font>**

```shell
#!/bin/bash
java_name="Hello World!"
readonly java_name
java_name="JDK8"  
#输出：This variable is read only
```

使用 `readonly` 命令可以将变量定义为只读变量，只读变量的值不能被改变



**<font color=orange>删除变量</font>**

使用 unset 命令可以删除变量，unset 命令不能删除只读变量



### 字符串

<font color=orange>**单引号**</font>

- 单引号里的任何字符都会原样输出，单引号字符串中的变量是无效的
- 单引号字串中不能出现单独一个的单引号（对单引号使用转义符后也不行），但可成对出现，作为字符串拼接使用

```shell
str='this is a string'
```



<font color=orange>**双引号**</font>

- 双引号里可以有变量
- 双引号里可以出现转义字符

```shell
java_name="world"
# 使用双引号拼接
greeting="hello, "$java_name" !"
greeting_1="hello, ${java_name} !"
echo $greeting  $greeting_1
# 使用单引号拼接
greeting_2='hello, '$java_name' !'
greeting_3='hello, ${java_name} !'
echo $greeting_2  $greeting_3

#输出
hello, world ! hello, world !
hello, world ! hello, ${java_name} !
```



#### 字符串操作

```shell
java_name="world"
#获取字符串长度
echo ${#java_name}

#截取字符串, 下标从 0 开始
echo ${java_name:1:4} #输出 ava_

```



### 数组

```shell
# 数组名=(值1 值2 ... 值n)
array_name=(
    value0
    value1
    value2
    value3
)
#或者是
array_name[0]=value0
array_name[1]=value1
array_name[n]=valuen


#输出所有元素
echo ${array_name[@]}
echo ${array_name[*]}
#输出某个元素,下标从 0 开始
echo ${array_name[1]}
# 取得数组单个元素的长度
lengthn=${#array_name[n]}
```



### 传递参数

​		可以在执行 Shell 脚本时，向脚本传递参数，脚本内获取参数的格式为：**$n**

​		**n** 代表一个数字，1 为执行脚本的第一个参数，2 为执行脚本的第二个参数，以此类推

​		**$0** 为执行的文件名（包含文件路径）

```shell
$ ./test.sh 1 2 3

#内容
echo "执行的文件名：$0";
echo "第一个参数为：$1";
echo "第二个参数为：$2";
echo "第三个参数为：$3";
#内容

#输出
执行的文件名：./test.sh
第一个参数为：1
第二个参数为：2
第三个参数为：3
```



| 参数 | 说明                                                        |
| ---- | ----------------------------------------------------------- |
| $#   | 传递到脚本的参数个数                                        |
| $*   | 以一个单字符串显示所有向脚本传递的参数                      |
| $$   | 脚本运行的当前进程ID号                                      |
| $@   | 与$*相同，但是使用时加引号，并在引号中返回每个参数          |
| $?   | 显示最后命令的退出状态。0表示没有错误，其他任何值表明有错误 |



**`$*` 与 `$@` 区别**

- 相同点：都是引用所有参数

- 不同点：只有在双引号中体现出来

  > ​		假设在脚本运行时写了三个参数 1、2、3，，则 " * " 等价于 "1 2 3"（传递了一个参数），而 "@" 等价于 "1" "2" "3"（传递了三个参数）

```shell
echo "-- \$* 演示 ---"
for i in "$*"; do
    echo $i
done

echo "-- \$@ 演示 ---"
for i in "$@"; do
    echo $i
done

####输出
-- $* 演示 ---
1 2 3
-- $@ 演示 ---
1
2
3
```



