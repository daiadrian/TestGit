package com.dai.sort.mySort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 归并排序(分成小分的，再合并成完整有序的)
 *
 * 采用分治策略。
 *
 * 分：将这个数组递归拆分成序列长度为1的序列(类似二分一样的拆分方法，按照mid中间位置拆分序列)
 *
 * 治：
 *  1.将拆分好的两个有序序列进行排序合并：为两个有序序列的头设置指针
 *  2.然后比较两个指针指向的元素大小(由小到大排序), 小的元素放入到temp临时数组中, 然后移动指针到下一位
 *  3.直到其中一边的序列全部放入temp数组中, 另外一序列的元素就可以直接放到temp数组中, 此时的temp数组即为排好序的数组
 *  4.然后再递归实现, 让temp为最终排好序的数组, 就可以放回原数组中
 */
public class MyMergeSort {

    public static void main(String[] args) {
        List<Integer> integers = Arrays.asList(10, 3, 50, 20, 15, 1, 33, 31, 25);
        List<Integer> temp = new ArrayList<>();
        mergeSort(integers, 0, integers.size() - 1, temp);
        integers.stream().forEach(System.out::println);
    }

    /**
     *  分策略, 将列表分成若干的小序列 (递归方式)
     *
     * @param mergeList 源数组
     * @param left      数组左下标值
     * @param right     数组右下标值
     * @param temp      临时数组
     */
    public static void mergeSort(List<Integer> mergeList, int left, int right, List<Integer> temp) {
        if (null != mergeList && mergeList.size() > 0) {
            // 判断序列是否还可再分
            if (left < right) {
                int middle = (left + right) / 2;
                mergeSort(mergeList, left, middle, temp);
                mergeSort(mergeList, middle + 1, right, temp);
                merge(mergeList, left, right, temp);
            }
        }
    }

    /**
     *  治策略, 将小序列进行排序然后合并成有序的完整序列
     *
     * @param mergeList 源数组
     * @param left      数组左下标值
     * @param right     数组右下标值
     * @param temp      临时数组
     */
    public static void merge(List<Integer> mergeList, int left, int right, List<Integer> temp) {
        //临时数组的下标
        int tempIndex = 0;
        int middle = (left + right) / 2;
        /**
         * 两边的序列从最左边的值开始进行比较, 数组较小放在临时数组左边
         *
         *      因为拆分出来的序列会从最小序列开始比较得到的是一个 有序的序列
         *      所以只需要从左往右开始比较即可
         */
        int tempLeft = left;
        int tempRight = middle + 1;
        /**
         * 两个序列从左往右开始比较, 直至其中一边的序列完全排到临时数组中
         */
        while(tempLeft <= middle && right >= tempRight) {
            if (mergeList.get(tempLeft) > mergeList.get(tempRight)) {
                //如果左边值较大, 将右边序列的值放到临时数组中
                temp.add(mergeList.get(tempRight++));
                //如果是数组形式, 那么是: temp[tempIndex++] = mergeList.get(tempRight++);
            } else {
                //否则将左边序列的值放到临时数组中
                temp.add(mergeList.get(tempLeft++));
                //如果是数组形式, 那么是: temp[tempIndex++] = mergeList.get(tempLeft++);
            }
        }

        /**
         * 如果左边的值还没放完, 即右边序列的值都放入到临时数组中
         * 那么直接将左边序列剩下的值放入临时数组中
         */
        while (tempLeft <= middle) {
            temp.add(mergeList.get(tempLeft++));
        }

        /**
         * 反之则将右边序列剩下的值放入临时数组中
         */
        while (right >= tempRight) {
            temp.add(mergeList.get(tempRight++));
        }

        /**
         * 将临时数组排好序的内容放回原数组中
         */
        tempIndex = 0;
        while (tempIndex < temp.size()) {
            mergeList.set(left++, temp.get(tempIndex++));
        }
        //因为使用了临时的列表, 需要清空列表
        temp.clear();
    }

    /**
     * 数组方式的治策略
     *
     *  治策略, 将小序列进行排序然后合并成有序的完整序列
     *
     * @param mergeList 源数组
     * @param left      数组左下标值
     * @param right     数组右下标值
     * @param temp      临时数组
     */
    public static void arrayMerge(Integer[] mergeList, int left, int right, Integer[] temp) {
        //临时数组的下标
        int tempIndex = 0;
        int middle = (left + right) / 2;
        /**
         * 两边的序列从最左边的值开始进行比较, 数组较小放在临时数组左边
         *
         *      因为拆分出来的序列会从最小序列开始比较得到的是一个 有序的序列
         *      所以只需要从左往右开始比较即可
         */
        int tempLeft = left;
        int tempRight = middle + 1;
        /**
         * 两个序列从左往右开始比较, 直至其中一边的序列完全排到临时数组中
         */
        while(tempLeft <= middle && right >= tempRight) {
            if (mergeList[tempLeft] > mergeList[tempRight]) {
                //如果左边值较大, 将右边序列的值放到临时数组中
                temp[tempIndex++] = mergeList[tempRight++];
            } else {
                //否则将左边序列的值放到临时数组中
                temp[tempIndex++] = mergeList[tempLeft++];
            }
        }

        /**
         * 如果左边的值还没放完, 即右边序列的值都放入到临时数组中
         * 那么直接将左边序列剩下的值放入临时数组中
         */
        while (tempLeft <= middle) {
            temp[tempIndex++] = mergeList[tempLeft++];
        }

        /**
         * 反之则将右边序列剩下的值放入临时数组中
         */
        while (right >= tempRight) {
            temp[tempIndex++] = mergeList[tempRight++];
        }

        /**
         * 将临时数组排好序的内容放回原数组中
         */
        tempIndex = 0;
        while (left <= right) {
            mergeList[left++] = temp[tempIndex++];
        }
    }

}
