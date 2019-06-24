package com.dai.sort.mySort;

import java.util.Arrays;
import java.util.List;

/**
 * 选择排序: 选择最大的值放到最前面
 *
 *      1.首先在未排序序列中找到最小（大）元素, 存放到排序序列的起始位置
 *      2.再从剩余未排序元素中继续寻找最小（大）元素, 然后放到 "已排序序列" 的末尾
 *      3.重复第二步，直到所有元素均排序完毕
 */
public class MySelectionSort {

    public static void main(String[] args) {
        List<Integer> integers = Arrays.asList(10, 3, 50, 20, 15, 1, 33, 31, 25);
        selectionSort(integers);
        integers.stream().forEach(System.out::println);
    }

    public static void selectionSort(List<Integer> selectionList) {
        if (null != selectionList && selectionList.size() > 0) {
            /**
             * 外层 for 循环是选择的次数,最后一位不需要选择排序了
             */
            for (int i = 0;i < selectionList.size() - 1;i++) {
                //存放每轮比较的最大值
                int max = i;
                /**
                 * 这一次 for 循环是比较的次数
                 */
                for (int j = i + 1;j < selectionList.size();j++) {
                    if (selectionList.get(max) < selectionList.get(j)) {
                        max = j;
                    }
                }
                if (max != i) {
                    int temp = selectionList.get(i);
                    selectionList.set(i, selectionList.get(max));
                    selectionList.set(max, temp);
                }
            }
        }
    }

}
