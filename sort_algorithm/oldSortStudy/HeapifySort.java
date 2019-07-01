package com.dai.sort.sortExample;

import java.util.Arrays;

/**
 * Created by dh on 2018/9/6.
 *
 * 堆排序：
 *
 * 小根堆/大根堆(大/小顶堆)
 *
 * 堆是具有以下性质的完全二叉树：每个结点的值都大于或等于其左右孩子结点的值，称为大顶堆；
 *                              每个结点的值都小于或等于其左右孩子结点的值，称为小顶堆。
 */
//这个是大顶堆的实现：
public class HeapifySort {

    public static void heapify(Integer[] arrays, int currentNode, int size) {
        if (currentNode < size) {
            //获取当前节点位置上的 左、右节点
            int leftNode = 2 * currentNode + 1;
            int rightNode = 2 * currentNode + 2;
            //记录当前节点的下标作为当前最大值的节点
            int max = currentNode;
            //如果左节点没有超过下标，而且左节点的值要比当前节点的值大
            // 那么记录最大值的节点为左节点的下标
            if (leftNode < size) {
                if (arrays[max] < arrays[leftNode]) {
                    max = leftNode;
                }
            }
            //如果右节点没有超过下标，而且右节点的值要比当前最大值的节点的值大
            // 那么记录最大值的节点为右节点的下标
            if (rightNode < size) {
                if (arrays[max] < arrays[rightNode]) {
                    max = rightNode;
                }
            }
            //如果当前最大值的节点不是 currentNode(就是当前的父节点)
            //那么父节点就与当前最大值的节点交换位置
            if (max != currentNode) {
                int temp = arrays[max];
                arrays[max] = arrays[currentNode];
                arrays[currentNode] = temp;
                //交换位置后，继续往下判断 交换后的节点(就是之前的父节点) 是否是交换前节点所在的子树的最大值
                //如果不是的话继续往下，直到所有的子树都满足大顶堆的要求
                //备注：可以画图理解，会更容易理解这个递归做的事情
                heapify(arrays, max, size);
            }
        }
    }

    /**
     * 将arrays变成满足大根堆要求的数组
     */
    public static void maxHeapify(Integer[] arrays, int size) {
        //从第一个非叶子节点开始
        for (int i = size / 2 - 1; i >= 0; i--) {
            heapify(arrays, i, size);
        }
    }

    public static void sort(Integer[] arrays) {
        if (arrays == null || arrays.length <= 0) {
            return;
        }
        //建立一个大根堆
        maxHeapify(arrays, arrays.length);
		//堆排序要建立在arrays已经是大根堆的情况下,所以上面需要进行一次堆排得到大根堆
        //大根堆的第一个元素就是最大值，将最大值与最后位置上的值进行交换后，再进行堆排序
        for (int i = arrays.length - 1; i > 0; i--) {
            int temp = arrays[0];
            arrays[0] = arrays[i];
            arrays[i] = temp;

            heapify(arrays, 0, i);
        }

    }


    public static void main(String[] args) {
        Integer[] integers = {1, 3, 50, 20, 15, 12, 33, 31, 25, 70};
        sort(integers);
        Arrays.stream(integers).forEach(System.out::println);
    }

}
