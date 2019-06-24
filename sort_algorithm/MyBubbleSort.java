package com.dai.sort.mySort;

import java.util.Arrays;
import java.util.List;

/**
 * 冒泡排序:
 *      1.比较相邻的元素. 如果第一个比第二个大, 就交换他们两个
 *      2.对每一对相邻元素作同样的工作, 从开始第一对到结尾的最后一对. 这步做完后, 最后的元素会是最大的数
 *      3.针对所有的元素重复以上的步骤，除了最后一个
 */
public class MyBubbleSort {

    public static void main(String[] args) {
        List<Integer> integers = Arrays.asList(1, 3, 50, 20, 15, 12, 33, 31, 25);
        bubbleSort(integers);
        integers.stream().forEach(System.out::println);
    }

    public static void bubbleSort(List<Integer> insertionList) {
        /**
         * 此处加的 flag 标志是:
         *          如果一轮比较下来没有发生交换位置的情况, 那么整个数组就是有序的, 可不必再进行比较
         *          这种情况的时间复杂度就是 O(n)
         */
        boolean flag;
        if (null != insertionList && insertionList.size() > 0) {
            flag = true;
            /**
             * 外层 for 循环是比较的轮数
             */
            for (int i = 1;i < insertionList.size();i++) {
                /**
                 * 里层 for 循环是前后元素比较的次数
                 *  -i 是因为, 每一轮比较之后, 就确定一个数是排序正确的, 所以要减去轮数
                 */
                for (int j = 0;j < insertionList.size() - i;j++) {
                    //比较大小交换位置
                    if (insertionList.get(j) > insertionList.get(j + 1)) {
                        int temp = insertionList.get(j);
                        insertionList.set(j, insertionList.get(j + 1));
                        insertionList.set(j + 1, temp);
                        flag = false;
                    }
                }
                if(flag) break;
            }
        }
    }

}
