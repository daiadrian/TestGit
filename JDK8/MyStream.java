package cn.dai.jdk8;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @ClassName: MyStream
 * @Description: TODO
 * @Author: DaiAdrian
 * @Date: 2019/3/17 14:53
 * @Version: 1.0
 **/
public class MyStream {

    /**
     * Stream 是 Java8 中处理集合的关键抽象概念，
     *      它可以指定你希望对集合进行的操作，可以执行非常复杂的查找、过滤和映射数据等
     *
     * 流是数据渠道，用于操作数据源（集合、数组等）所生成的元素序列
     *
     */
    @Test
    public void test1() {
        //创建流
        //  1. Collection 提供了两个方法  stream() 与 parallelStream()
        List<String> list = new ArrayList<>();
        Stream<String> stream = list.stream(); //获取一个顺序流
        Stream<String> parallelStream = list.parallelStream(); //获取一个并行流

        //  2. 通过 Arrays 中的 stream() 获取一个数组流
        Integer[] nums = new Integer[10];
        Stream<Integer> stream1 = Arrays.stream(nums);

        //  3. 通过 Stream 类中静态方法 of()
        Stream<Integer> stream2 = Stream.of(1,2,3,4,5,6);

        //  4. 创建无限流
        //迭代
        Stream<Integer> stream3 = Stream.iterate(0, (x) -> x + 2);
        //生成
        Stream<Double> stream4 = Stream.generate(Math::random);
    }

    /**
     *中间操作
     *
     *  筛选与切片
     * 		filter——接收 Lambda ， 从流中排除某些元素。
     * 		limit——截断流，使其元素不超过给定数量。
     * 		skip(n) —— 跳过元素，返回一个扔掉了前 n 个元素的流。若流中元素不足 n 个，则返回一个空流。与 limit(n) 互补
     * 		distinct——筛选，通过流所生成元素的 hashCode() 和 equals() 去除重复元素
     *
     * 	映射
     *      map——接收 Lambda ， 将元素转换成其他形式或提取信息。
     *      	接收一个函数作为参数，该函数会被应用到每个元素上，并将其映射成一个新的元素。
     *      flatMap——接收一个函数作为参数，将流中的每个值都换成另一个流，然后把所有流连接成一个流
     *
     *   排序
     *      sorted()——自然排序
     *      sorted(Comparator com)——定制排序
     */
    @Test
    public void test2() {
        //filter
        Stream<Integer> stream = Stream.of(1, 2, 3, 4, 5, 6);
        System.out.println("----------filter-------------");
        stream.filter((x) -> { return x > 1 ? true : false; })
              .forEach(System.out::println);

        //limit
        Stream<Integer> stream1 = Stream.of(1, 2, 3, 4, 5, 6);
        System.out.println("----------limit-------------");
        stream1.limit(2).skip(2).forEach(System.out::println);

        //map
        List<Student> list = Arrays.asList(new Student("yes"));
        list.stream().map((student) -> student.getName()).forEach(System.out::println);
    }
    class Student{
        private String name;
        public Student(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     *终止操作
     *      allMatch——检查是否匹配所有元素
     * 		anyMatch——检查是否至少匹配一个元素
     * 		noneMatch——检查是否没有匹配的元素
     * 		findFirst——返回第一个元素
     * 		findAny——返回当前流中的任意元素
     * 		count——返回流中元素的总个数
     * 		max——返回流中最大值
     * 		min——返回流中最小值
     * 	收集
     * 	    collect——将流转换为其他形式。接收一个 Collector接口的实现，用于给Stream中元素做汇总的方法
     * 	归约
     * 		reduce(T identity, BinaryOperator) / reduce(BinaryOperator) ——可以将流中元素反复结合起来，得到一个值
     *
     *
     * 注意：流进行了终止操作后，不能再次使用
     * (否则会抛出stream has already been operated upon or closed的异常)
     */
    @Test
    public void test3() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4, 5, 6);
        //collect
        //计算总数
        Long count = stream.collect(Collectors.counting());
        System.out.println("总数====>" + count);

        //归约
        Stream<Integer> stream1 = Stream.of(1, 2, 3, 4, 5, 6);
        //下面两种方式都是计算总值
        Integer reduce = stream1.reduce(0, (x, y) -> x + y);
        stream1 = Stream.of(1, 2, 3, 4, 5, 6);
        Optional<Integer> optional = stream1.reduce(Integer::sum);
        optional.get();

    }

}
