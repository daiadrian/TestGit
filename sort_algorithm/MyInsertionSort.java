package com.dai.sort.mySort;

import java.util.Arrays;
import java.util.List;

/**
 * 插入排序:
 *          1.将第一待排序序列第一个元素看做一个有序序列, 把第二个元素到最后一个元素当成是未排序序列
 *          2.从头到尾依次扫描未排序序列, 将扫描到的每个元素插入有序序列的适当位置
 */
public class MyInsertionSort {

    public static void main(String[] args) {
        List<Integer> integers = Arrays.asList(10, 3, 50, 20, 15, 1, 33, 31, 25);
        insertionSort(integers);
        integers.stream().forEach(System.out::println);
    }

    public static void insertionSort(List<Integer> insertionList) {
        if (null != insertionList && insertionList.size() > 0) {
            /**
             * 外层 for 循环是从左到右的每个元素都需要往前进行比较,这是比较的轮数
             */
            for (int i = 1;i < insertionList.size();i++) {
                //存放本轮的元素值
                Integer integer = insertionList.get(i);
                int j = i;
                /**
                 * 从右往左开始进行比较, 只要值比本轮的值大, 那么就交换位置
                 * 因为轮数是从左边开始进行的, 那么在本轮比较的时候, 左边的数组已经是有序的了
                 */
                while(j > 0 && insertionList.get(j - 1) > integer) {
                    insertionList.set(j, insertionList.get(j - 1));
                    j--;
                }
                /**
                 * j 下标值就是本轮元素值的位置
                 */
                insertionList.set(j, integer);
            }
        }
    }

}
