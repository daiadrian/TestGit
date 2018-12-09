package com.dai.sort;

/**
 * Created by dh on 2018/9/6.
 *
 * 归并排序(分成小分的，再合并成完整有序的)
 *
 * 采用分治策略。
 *
 * 分：将这个数组递归拆分成序列长度为1的序列(类似二分一样的拆分方法，按照mid中间位置拆分序列)
 *
 * 治：将拆分好的两个有序序列进行排序合并：为两个有序序列的头设置指针
 * 然后比较两个指针指向的元素大小(由小到大排序)，然后小的元素放入到temp临时数组中，然后移动指针到下一位
 * 直到其中一边的序列全部放入temp数组中，另外一序列的元素就可以直接放到temp数组中，此时的temp数组即为排好序的数组
 * 然后再递归实现，让temp为最终排好序的数组，就可以放回原数组中
 */
public class MergeSort {

    //策略 : 分

    /**
     * temp参数为临时数组，最好第一次调用的时候传入new Integer[arrays.length]，以免递归时多次创建影响性能
     *
     * 类二分一样的递归拆分方式，然后再将拆分好序列进行merge合并操作
     *
     */
    public static void sort(Integer[] arrays, int left, int right,Integer[] temp) {
        if (arrays == null || arrays.length <= 0) {
            return;
        }
        if (left < right) {
            int mid = (left + right) / 2;
            sort(arrays, left, mid, temp);
            sort(arrays, mid + 1, right, temp);
            merge(arrays, left, mid, right, temp);
        }
    }

    //策略 : 治
    public static void merge(Integer[] arrays, int left,int mid, int right,Integer[] temp) {
        //i作为记录temp数组的下标
        int i = 0;
        //作为左边拆分好的有序数组的指针
        int tempLeft = left;
        //作为右边拆分好的有序数组的指针
        int tempRight = mid + 1;
        //两边的序列进行比较，较小的数放入到temp数组中，并且将指针往下移动一位
        while (tempLeft <= mid && right >= tempRight) {
            if (arrays[tempLeft] < arrays[tempRight]) {
                temp[i++] = arrays[tempLeft++];
            } else {
                temp[i++] = arrays[tempRight++];
            }
        }
        //如果左边有序数组剩余的元素直接放入temp数组中
        while (tempLeft <= mid) {
            temp[i++] = arrays[tempLeft++];
        }
        //如果右边有序数组剩余的元素直接放入temp数组中
        while (right >= tempRight) {
            temp[i++] = arrays[tempRight++];
        }
        //将temp数组的元素拷贝到原数组中
        i = 0;
        while(right >= left){
            arrays[left++] = temp[i++];
        }
    }

}
