package com.dai.sort.mySort;

import java.util.Arrays;
import java.util.List;

/**
 * 希尔排序: (希尔排序是高效版本的插入排序算法, 只是按照对应增量来划分区域提高插入排序的性能)
 *
 *      1.选择一个增量序列 t1，t2，……，tk，其中 ti > tj
 *      2.按增量序列个数 k，对序列进行 k 趟排序
 *      3.每趟排序根据对应的增量 ti, 将待排序列分割成若干长度为 m 的子序列,
 *      分别对各子表进行直接插入排序. 仅增量因子为 1 时, 整个序列作为一个表来处理, 表长度即为整个序列的长度
 */
public class MyShellSort {

    public static void main(String[] args) {
        List<Integer> integers = Arrays.asList(1, 3, 50, 20, 15, 12, 33, 31, 25);
        shellSort(integers);
        integers.stream().forEach(System.out::println);
    }

    public static void shellSort(List<Integer> shellList) {
        if (null != shellList && shellList.size() > 0) {
            int h = 1;
            //计算步长
            while (h < shellList.size()) {
                h = h * 3 + 1;
            }

            while (h > 0) {
                /**
                 * 按照 步长 来对元素进行插入排序的比较
                 */
                for (int i = h;i < shellList.size();i++) {
                    //因为步长是 h, 获取距离当前步长位置上的值
                    int j = i - h;
                    /**
                     *  按照步长来进行插入排序操作
                     */
                    Integer temp = shellList.get(i);
                    while (j >= 0 && shellList.get(j) > temp) {
                        shellList.set(j + h, shellList.get(j));
                        j = j - h;
                    }
                    shellList.set(j + h, temp);
                }
                h = h / 3;
            }
        }
    }

}
