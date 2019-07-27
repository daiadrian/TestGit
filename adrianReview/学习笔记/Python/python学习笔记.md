python学习笔记

# 基础语法

## 数据类型

- 整型 (`int`)

- 浮点型（`float`）

- 布尔型（`bool`） 

  - 真 `True` `非 0 数` —— **非零即真**
  - 假 `False` `0`

- 复数型 (`complex`)

  - 主要用于科学计算，例如：平面场问题、波动问题、电感电容等问题

- 字符串

  > ````python
  > string = "Hello Python"
  > # 遍历字符
  > for c in string:
  >     print(c)
  > 
  > # 常用方法
  > string.isspace() 如果 string 中只包含空格，则返回 True
  > string.startswith(str)检查字符串是否是以 str 开头，是则返回 True
  > string.endswith(str)检查字符串是否是以 str 结束，是则返回 True
  > string.strip()截掉 string 左右两边的空白字符
  > 
  > #string.find(str, start=0, end=len(string))检测 str 是否包含在 string 中，
  > #如果start和end指定范围，则检查是否包含在指定范围内，如果是返回开始的索引值，否则返回 `-1`
  > 
  > ````

  字符串切片：

  ````python
  字符串[开始索引:结束索引:步长]
  num_str = "0123456789"
  # 1. 截取从 2 ~ 5 位置 的字符串
  print(num_str[2:6])
  
  # 2. 截取从 2 ~ `末尾` 的字符串
  print(num_str[2:])
  
  # 3. 截取从 `开始` ~ 5 位置 的字符串
  print(num_str[:6])
  
  # 4. 截取完整的字符串
  print(num_str[:])
  
  # 5. 从开始位置，每隔一个字符截取字符串
  print(num_str[::2])
  
  # 倒序切片
  # -1 表示倒数第一个字符
  print(num_str[-1])
  
  # 6. 截取从 2 ~ `末尾 - 1` 的字符串
  print(num_str[2:-1])
  
  # 7. 截取字符串末尾两个字符
  print(num_str[-2:])
  
  # 8. 字符串的逆序（面试题）
  print(num_str[::-1])
  ````

  

### 列表，字典和元组

1. 列表

   ````python
   name_list = [1, 2, 3]
   #常用方法：
   # 索引从 `0` 开始
   my_list[index]
   # 输出列表的长度
   len(name_list)
   # reverse默认是False，True是倒序排列
   name_list.sort(reverse=True)
   # 删除一个元素
   name_list.remove(3)
   # 在指定索引位置插入元素
   name_list.insert(index, element)
   # 在列表尾插入一个元素
   name_list.append(5)
   # 将列表2的数据追加到指定列表后面
   name_list.extend(列表2)
   
   # del 列表[索引]     | 删除指定索引的数据  
   # 注意：del本质上是用来将一个变量从内存中删除的
   
   # 列表.remove[数据]  | 删除第一个出现的指定数据 
   # 列表.pop          | 删除末尾数据 
   # 列表.pop(索引) 	 | 删除指定索引数据 
   # 列表.clear 		  | 清空列表 
   
   # for 循环内部使用的变量 in 列表
   for name in name_list:
       循环内部针对列表元素进行操作
       print(name)
   ````

2. 元组

   ````python
   # 元组的元素不能修改；索引从 `0` 开始
   info_tuple = ("zhangsan", 18, 1.75)
   # 获得数据第一次出现的索引
   info_tuple.index(18)
   # 遍历操作和列表一致
   ````

元组和列表的相互转换：

````python
list(元组)  # 元组转成列表
tuple(列表) # 列表转成元组
````

3. 字典

   ````python
   xiaoming = {"name": "小明",
               "age": 18,
               "gender": True,
               "height": 1.75}
   # 获得key的列表
   xiaoming.keys()
   # 获取value的列表
   xiaoming.values()
   # 获得(key, value)的元组列表
   xiaoming.items()
   # 根据key取值，如果key不存在则报错
   xiaoming[key]
   xiaoming.get(key)
   # 删除指定键值对，key不存在报错
   xiaoming.pop(key)
   del xiaoming[key]
   # 清空字典
   xiaoming.clear()
   # 将字典2的数据合并到字典1
   xiaoming.update(字典2)
   ````

### 公共常用方法

| 函数      | 描述                 | 备注                        |
| --------- | -------------------- | --------------------------- |
| len(item) | 计算容器中元素个数   |                             |
| del(item) | 删除变量             | del 有两种方式              |
| max(item) | 返回容器中元素最大值 | 如果是字典，只针对 key 比较 |
| min(item) | 返回容器中元素最小值 | 如果是字典，只针对 key 比较 |

| 运算符 | Python 表达式      | 结果                         | 描述           | 支持的数据类型           |
| :----: | ------------------ | ---------------------------- | -------------- | ------------------------ |
|   +    | [1, 2] + [3, 4]    | [1, 2, 3, 4]                 | 合并           | 字符串、列表、元组       |
|   *    | ["Hi!"] * 4        | ['Hi!', 'Hi!', 'Hi!', 'Hi!'] | 重复           | 字符串、列表、元组       |
|   in   | 3 in (1, 2, 3)     | True                         | 元素是否存在   | 字符串、列表、元组、字典 |
| not in | 4 not in (1, 2, 3) | True                         | 元素是否不存在 | 字符串、列表、元组、字典 |

## 函数定义

````python
def measure():
    """返回当前的温度"""
    print("开始测量...")
    temp = 39
    print("测量结束...")
    return temp


result = measure()
print(result)


def print_info(name, title="", gender=True):
    """
    :param title: 职位
    :param name: 班上同学的姓名
    :param gender: True 男生 False 女生
    """
    gender_text = "男生"
    if not gender:
        gender_text = "女生"
    print("%s%s 是 %s" % (title, name, gender_text))
    
    
# *args —— 存放 元组 参数，前面有一个 `*`
# **kwargs —— 存放 字典 参数，前面有两个 `*`   
def demo(num, *args, **kwargs):
    print(num)
    print(args)
    print(kwargs)
    
    
demo(1, 2, 3, 4, 5, name="小明", age=18, gender=True)    
````

## 类定义

````python
class 类名:

    def 方法1(self, 参数列表):
        pass
    
    def 方法2(self, 参数列表):
        pass
    
# 单例模式
class Singleton(object):
    instance = None
    initFlag = False

    def __new__(cls, *args, **kwargs):
        if cls.instance is None:
            # 重写 __new__ 方法一定要 return super().__new__(cls)
            cls.instance = super().__new__(cls)
		
        return cls.instance

    def __init__(self):
        if Singleton.initFlag is False:
            super().__init__()
            Singleton.initFlag = True
            
            
# 定义属性或方法时，在属性名或者方法名前 增加 两个下划线，定义的就是 私有 属性或方法
class Women:

    def __init__(self, name):
        self.name = name
		#私有属性
        self.__age = 18
	#私有方法
    def __secret(self):
        print("我的年龄是 %d" % self.__age)
````

| 序号 | 方法名     | 类型 | 作用                                                         |
| ---- | ---------- | ---- | ------------------------------------------------------------ |
| 01   | `__new__`  | 方法 | **创建对象**时，会被 **自动** 调用                           |
| 02   | `__init__` | 方法 | **对象被初始化**时，会被 **自动** 调用                       |
| 03   | `__del__`  | 方法 | **对象被从内存中销毁**前，会被 **自动** 调用                 |
| 04   | `__str__`  | 方法 | 返回**对象的描述信息**，`print` 函数输出使用(相当于toString) |

创建对象的动作有两步：

- 在内存中为对象 **分配空间**
- 调用初始化方法 `__init__` 为 **对象初始化**

### 继承和多态

````python
class 类名(父类名):
    pass
# 多继承(ps:尽量少用多继承，不然有同名方法时容易混淆)
class 子类名(父类名1, 父类名2...)
    pass
````



### 类属性、类方法和静态方法

````python
class Singleton(object):
    # 类属性
    instance = None
    initFlag = False
    
    # 类方法需要用修饰器 @classmethod 来标识
    # 调用类方法： "类名.类方法名" (cls参数不用传递)
    @classmethod
    def 类方法名(cls):
        # 可以通过 cls. 访问类的属性
		# 也可以通过 cls. 调用其他的类方法
        pass
    
    
    #静态方法 需要用 修饰器 @staticmethod 来标识
#既不需要访问实例属性或者调用实例方法,也不需要访问类属性或者调用类方法的时候可以定义为静态方法
    @staticmethod
    def 静态方法名():
        pass
````



## 异常

​	在开发中，可以在主函数中增加 **异常捕获**；在主函数中调用的其他函数，只要出现异常，都会传递到主函数的 **异常捕获** 中；这样就不需要在代码中，增加大量的**异常捕获**，能够保证代码的整洁

````python
try:
    # 提示用户输入一个数字
    num = int(input("请输入数字："))
except:
    print("请输入正确的数字")
    
   
#捕获错误类型
try:
    # 尝试执行的代码
    pass
except 错误类型1:
    # 针对错误类型1，对应的代码处理
    pass
except 错误类型2:
    # 针对错误类型2，对应的代码处理
    pass
except (错误类型3, 错误类型4):
    # 针对错误类型3 和 4，对应的代码处理
    pass
except Exception as result:
    # 这个是捕获未知的错误，打印错误信息
    print("未知错误 %s" % result)
else:
    # 没有异常才会执行的代码
    pass
finally:
    # 无论是否有异常，都会执行的代码
    print("无论是否有异常，都会执行的代码")    
    

#例子：判断输入的数   
try:
    num = int(input("请输入整数："))
    result = 8 / num
    print(result)
except ValueError:
    print("请输入正确的整数")
except ZeroDivisionError:
    print("除 0 错误")
````

### 抛出异常

````python
def input_password():
    pwd = input("请输入密码：")
    if len(pwd) >= 8:
        return pwd
    # 创建异常对象 - 使用异常的错误信息字符串作为参数
    ex = Exception("密码长度不够")
    # 抛出异常对象
    raise ex
    
try:
    user_pwd = input_password()
    print(user_pwd)
except Exception as result:
    print("发现错误：%s" % result)
````



## 模块

1. 每一个以扩展名 `py` 结尾的 `Python` 源代码文件都是一个 **模块**

2. **模块名** 同样也是一个 **标识符**，需要符合标识符的命名规则

3. 在模块中定义的 **全局变量** 、**函数**、**类** 都是提供给外界直接使用的 **工具**

4. **模块** 就好比是 **工具包**，要想使用这个工具包中的工具，就需要先 **导入** 这个模块
5. 类似Java中的工具类，导入即可用
6. 可以通过 `模块名.` 使用 模块提供的工具 —— 全局变量、函数（有别名可以用 `别名.`）

````python
import 模块名1
import 模块名2 

#导入的时候可以使用别名导入
# 模块别名 应该符合 大驼峰命名法
import 模块名1 as 模块别名

# 从某一个模块中，导入部分工具；可以直接使用模块提供的工具 —— 全局变量、函数、类
from 模块名1 import 工具名

#实例
----------------------
import random
# 生成一个 0～10 的数字
rand = random.randint(0, 10)
print(rand)
----------------------
````



## 文件操作

| 函数/方法 | 说明                           |
| :-------: | :----------------------------- |
|   open    | 打开文件，并且返回文件操作对象 |
|   read    | 将文件内容读取到内存           |
|   write   | 将指定内容写入文件             |
|   close   | 关闭文件                       |

- `open`函数的第一个参数是要打开的文件名（文件名区分大小写）

  - 如果文件 **存在**，返回 **文件操作对象**
  - 如果文件 **不存在**，会 **抛出异常**
  - open函数的访问方式：

  | 访问方式 | 说明                                                         |
  | -------- | ------------------------------------------------------------ |
  | r        | 以**只读**方式打开文件。文件的指针将会放在文件的开头，这是**默认模式**。如果文件不存在，抛出异常 |
  | w        | 以**只写**方式打开文件。如果文件存在会被覆盖。如果文件不存在，创建新文件 |
  | a        | 以**追加**方式打开文件。如果该文件已存在，文件指针将会放在文件的结尾。如果文件不存在，创建新文件进行写入 |
  | r+       | 以**读写**方式打开文件。文件的指针将会放在文件的开头。如果文件不存在，抛出异常 |
  | w+       | 以**读写**方式打开文件。如果文件存在会被覆盖。如果文件不存在，创建新文件 |
  | a+       | 以**读写**方式打开文件。如果该文件已存在，文件指针将会放在文件的结尾。如果文件不存在，创建新文件进行写入 |

- `read` 方法可以一次性 **读入** 并 **返回** 文件的 **所有内容**

- `close` 方法负责 **关闭文件**

````python
##复制文件作为例子
# 1. 打开文件
file_read = open("README")
file_write = open("README[复件]", "w")
# 2. 读取并写入文件
while True:
    # 每次读取一行
    text = file_read.readline()
    # 判断是否读取到内容
    if not text:
        break
    file_write.write(text)
# 3. 关闭文件
file_read.close()
file_write.close()
````

### os模块

| 方法名     | 说明           | 示例                              |
| ---------- | -------------- | --------------------------------- |
| rename     | 重命名文件     | `os.rename(源文件名, 目标文件名)` |
| remove     | 删除文件       | `os.remove(文件名)`               |
| listdir    | 目录列表       | `os.listdir(目录名)`              |
| mkdir      | 创建目录       | `os.mkdir(目录名)`                |
| rmdir      | 删除目录       | `os.rmdir(目录名)`                |
| getcwd     | 获取当前目录   | `os.getcwd()`                     |
| chdir      | 修改工作目录   | `os.chdir(目标目录)`              |
| path.isdir | 判断是否是文件 | `os.path.isdir(文件路径)`         |



## eval函数

````python
# 基本的数学计算
In [1]: eval("1 + 1")
Out[1]: 2

# 字符串重复
In [2]: eval("'*' * 10")
Out[2]: '**********'

# 将字符串转换成列表
In [3]: type(eval("[1, 2, 3, 4, 5]"))
Out[3]: list

# 将字符串转换成字典
In [4]: type(eval("{'name': 'xiaoming', 'age': 18}"))
Out[4]: dict
    
    
##勿滥用eval
eval("__import__('os').system('ls')")
#等价于
import os
os.system("ls")
````



