package cn.dai.jdk8;

import org.junit.Test;

import java.util.ArrayList;
import java.util.function.*;

/**
 * @ClassName: MyLambda
 * @Description: TODO
 * @Author: DaiAdrian
 * @Date: 2019/3/17 14:35
 * @Version: 1.0
 **/
public class MyLambda {

    /**
     * 函数式接口：接口中只有一个抽象方法的接口，称为函数式接口。
     *      可以使用注解 @FunctionalInterface 修饰可以检查是否是函数式接口
     *
     * 语法格式一：无参数，无返回值
     * 		() -> System.out.println("Hello Lambda!");
     *
     * 语法格式二：有一个参数，并且无返回值
     * 		(x) -> System.out.println(x)
     *
     * 语法格式三：若只有一个参数，小括号可以省略不写
     * 		x -> System.out.println(x)
     *
     * 语法格式四：有两个以上的参数，有返回值，并且 Lambda 体中有多条语句
     *		Comparator<Integer> com = (x, y) -> {
     *			System.out.println("函数式接口");
     *			return Integer.compare(x, y);
     *		};
     *
     * 语法格式五：若 Lambda 体中只有一条语句， return 和 大括号都可以省略不写
     * 		Comparator<Integer> com = (x, y) -> Integer.compare(x, y);
     *
     * 语法格式六：Lambda 表达式的参数列表的数据类型可以省略不写，因为JVM编译器通过上下文推断出，数据类型，即“类型推断”
     * 		(Integer x, Integer y) -> Integer.compare(x, y);
     */
    /**
     * Java8 内置的四大核心函数式接口
     *
     * Consumer<T> : 消费型接口
     * 		void accept(T t);
     *
     * Supplier<T> : 供给型接口
     * 		T get();
     *
     * Function<T, R> : 函数型接口
     * 		R apply(T t);
     *
     * Predicate<T> : 断言型接口
     * 		boolean test(T t);
     */
    @Test
    public void test1() {
        String string = "hello jdk8!";
        //消费型接口
        Consumer<String> consumer = (str) -> System.out.println(str);
        consumer.accept(string);

        //供给型接口
        Supplier<String> supplier = () -> {
            return string;
        };
        System.out.println(supplier.get());

        //函数型接口
        Function<Integer, String> function = (x) -> {
            return x > 0 ? string : "get out!";
        };
        System.out.println(function.apply(1));

        //断言型接口
        Predicate<Integer> predicate = (x) -> {
            return x > 0 ? true : false;
        };
        if (predicate.test(1)) {
            System.out.println(string);
        }
    }

    /**
     * 方法引用
     *
     * 当要传递给Lambda体的操作，已经有实现的方法了，可以使用方法引用！
     * （实现抽象方法的参数列表，必须与方法引用方法的参数列表保持一致！）
     * 1. 对象的引用 :: 实例方法名
     * 2. 类名 :: 静态方法名
     * 3. 类名 :: 实例方法名
     *
     *
     * 构造器引用
     *
     * 构造器的参数列表，需要与函数式接口中参数列表保持一致
     * 1. 类名 :: new
     *
     *
     * 数组引用
     * 1. 类型[] :: new
     */
    @Test
    public void test2() {
        //方法引用
        Consumer<String> consumer = System.out::println;
        consumer.accept("hello world!");
        //构造器引用
        consumer = String::new;


        //数组引用
        Function<Integer, Integer[]> function = Integer[] :: new;
        Integer[] apply = function.apply(20);
        //等同于
        Function<Integer, Integer[]> function1 = (x) -> new Integer[x];
        Integer[] apply1 = function1.apply(20);
    }


}
