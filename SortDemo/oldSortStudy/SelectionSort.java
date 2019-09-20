package com.dai.sort;

/**
 * Created by dh on 2018/10/17.
 *
 * 选择排序
 *
 * 从左至右遍历，找到最小(大)的元素，然后与第一个元素交换。
 * 从剩余未排序元素中继续寻找最小（大）元素，然后与第二个元素进行交换。
 * 以此类推，直到所有元素均排序完毕
 *
 *
 * 选择排序需要花费 (N – 1) + (N – 2) + ... + 1 + 0 = N(N- 1) / 2 ~ N2/2次比较 和 N-1次交换操作。
 * 对初始数据不敏感，不管初始的数据有没有排好序，都需要经历N2/2次比较，这对于一些原本排好序，或者近似排好序的序列来说并不具有优势。
 * 在最好的情况下，即所有的排好序，需要0次交换，最差的情况，倒序，需要N-1次交换。
 * 数据交换的次数较少，如果某个元素位于正确的最终位置上，则它不会被移动。
 * 在最差情况下也只需要进行N-1次数据交换，在所有的完全依靠交换去移动元素的排序方法中，选择排序属于比较好的一种。
 */
public class SelectionSort {

    public static void sort(Integer[] arr) {
        if (arr == null || arr.length < 1){
            return;
        }
        for (int i = 0,length = arr.length;i < length;i++){
            //记录最小值的下标
            int min = i;
            for (int j = i+1;j < length;j++){
                if (arr[min] > arr[j]){
                    min = j;
                }
            }
            //交换
            if (min != i){
                int temp = arr[i];
                arr[i] = arr[min];
                arr[min] = temp;
            }
        }
    }

}
