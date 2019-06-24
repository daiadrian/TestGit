package com.dai.sort;

/**
 * Created by dh on 2018/9/11.
 * <p>
 * 快速排序：
 * <p>
 * 选取基准值(一般左边第一个值)，
 * 然后先从右边(先从右边，先从右边，先从右边)开始找到小于基准值的值，记录位置j
 * 再然后从左边开始找到大于基准值的值，记录位置为i。然后交换两值的位置(i，j指向位置不变)
 * 继续从右开始上面的动作直到i==j为止，然后基准值与i的值交换位置
 * 然后再将基准值此时位置的左边和右边的数组递归上面的动作直到整个数组都是有序的
 */
public class QuickSort {

    /**
     * @param arrays
     * @param left
     * @param right
     */
    public static void quickSort(Integer[] arrays, int left, int right) {
        if (arrays == null || arrays.length <= 0) {
            return;
        }
        //判断是否有下标越界的可能
        //判断是否有下标越界的可能
        //判断是否有下标越界的可能
        if (left > right){
            return;
        }
        //选取基准值
        int temp = arrays[left];
        int i = left;
        int j = right;

        //开始比较直到i==j
        while (i != j) {
            //先从右边开始比较
            while (arrays[j] > temp && i < j) {
                //往左边找到比基准值小的值为止
                j--;
            }
            //右边比较结束后，再左边开始比较
            while (arrays[i] <= temp && i < j) {
                //往右边找到比基准大的值为止
                i++;
            }
            if (i < j) {
                int t = arrays[i];
                arrays[i] = arrays[j];
                arrays[j] = t;
            }
        }
        arrays[left] = arrays[i];
        arrays[i] = temp;

        //递归处理基准值左边和基准值右边的数组
        quickSort(arrays, left, i - 1);
        quickSort(arrays, i + 1, right);
    }


}
