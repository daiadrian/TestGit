package com.dai.sort.mySort;

import java.util.Arrays;
import java.util.List;

/**
 * 快速排序：
 *      1.选取基准值(一般左边第一个值)
 *      2.先从右边(先从右边，先从右边，先从右边)开始找到小于基准值的值，记录位置j
 *      3.然后从左边开始找到大于基准值的值，记录位置为i
 *      4.交换两值的位置(i，j指向位置不变; 仅交换两个值的位置)
 *
 *      5.继续从右开始上面的动作直到 i==j 为止，然后基准值与i的值交换位置
 *      6.再将 基准值此时位置的 左边和右边的数组 递归 上面的动作直到整个数组都是有序的
 *
 */
public class MyQuickSort {

    public static void main(String[] args) {
        List<Integer> integers = Arrays.asList(1, 3, 50, 20, 15, 12, 33, 31, 25);
//        quickSort(integers, 0, integers.size() - 1);
        quickSort(integers, 0, integers.size() - 1);
        integers.stream().forEach(System.out::println);
    }

    /**
     *
     * @param quickList
     * @param left
     * @param right
     */
    public static void quickSort(List<Integer> quickList, int left, int right) {
        /**
         * 这里需要判断下标是否越界, left 必须要小于或等于 right
         */
        if (null != quickList && quickList.size() > 0 && left <= right) {
            //选取基准值
            int quickTemp = quickList.get(left);
            /**
             * 设置两个起始位置
             *      i 从左边开始, 即基准值的后一位
             *      j 从右边开始
             */
            int i = left;
            int j = right;
            /**
             * 循环直到 i==j, 因为i是小于或等于j
             */
            while (i < j) {
                /**
                 * 从右边开始往左边找到 比基准值 小的值 (i代表这个值的位置)
                 */
                while (quickList.get(j) > quickTemp && i < j) {
                    j--;
                }

                /**
                 * 从左边开始往右边找到 比基准值 大的值 (i代表这个值的位置)
                 */
                while (quickList.get(i) <= quickTemp && i < j) {
                    i++;
                }
                /**
                 * 如果 i 不等于 j 的情况下交换 i 和 j位置上的值
                 */
                if (i < j) {
                    int temp = quickList.get(i);
                    quickList.set(i, quickList.get(j));
                    quickList.set(j, temp);
                }
            }

            /**
             *  循环到了 i==j 的情况, 交换基准值和 i 位置上的值
             *      为什么要交换这个位置呢？
             *          1. i是从左往右直到找到大于基准值的值
             *          2. j是从右往左直到找到小于基准值的值
             *          3. 当i==j的情况是 i 的位置上的值肯定是小于/等于基准值的,
             *              因为i是从基准值位置开始的
             *
             */
            quickList.set(left, quickList.get(i));
            quickList.set(i, quickTemp);

            quickSort(quickList, left, i - 1);
            quickSort(quickList, i + 1, right);
        }
    }

}
