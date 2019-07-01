package com.dai.sort.mySort;

import java.util.Arrays;
import java.util.List;

/**
 * 堆排序：(指利用 堆 这种数据结构所设计的一种排序算法, 它是选择排序的一种)
 *
 *      堆分为大根堆和小根堆, 是完全二叉树 (除了最后一层之外的其他每一层都被完全填充, 并且所有结点都保持向左对齐)
 *
 *  堆是具有以下性质的完全二叉树：
 *          1. 每个结点的值都 大于或等于 其左右孩子结点的值, 称为大顶堆
 *          2. 每个结点的值都 小于或等于 其左右孩子结点的值, 称为小顶堆
 *
 */
public class MyHeapifySort {

    public static void main(String[] args) {
        List<Integer> integers = Arrays.asList(1, 3, 50, 20, 15, 12, 33, 31, 25);
        sort(integers);
        integers.stream().forEach(System.out::println);
    }

    /**
     * 堆排序
     *
     * @param heapifyList
     */
    public static void sort(List<Integer> heapifyList) {
        if (null != heapifyList && heapifyList.size() > 0) {
            //先进行一次建堆的操作
            maxHeapifySort(heapifyList, heapifyList.size());
            /**
             * 堆排序需要建立在 数组/列表 已经是 大/小 根堆的情况下
             */
            for (int i = heapifyList.size() - 1;i > 0;i--) {
                /**
                 * 因为已经成功的建堆了, 数组/列表第一位已经是最大值了
                 * 那么将第一位与最后一位进行交换即可
                 */
                int temp = heapifyList.get(i);
                heapifyList.set(i, heapifyList.get(0));
                heapifyList.set(0, temp);
                /**
                 * 交换完位置之后, 数组/列表 的最后一位是最大值了
                 *
                 * 所以继续调整一下处最后一位以为的数组/列表, 将其调整成为大根堆
                 * 再重复的将其交换到最后一位, 递归得到有序的从小到大的数组/列表
                 */
                heapifySort(heapifyList, 0, i);
            }
        }
    }

    /**
     * 对数组/列表进行一次大根堆排序 (数组/列表 第一位是最大值)
     *
     * @param heapifyList
     * @param size
     */
    public static void maxHeapifySort(List<Integer> heapifyList, int size) {
        /**
         * 从第一个非叶子节点开始找最大值得到一个大根堆
         */
        for (int currentNode = size / 2 - 1;currentNode >= 0;currentNode--) {
            heapifySort(heapifyList, currentNode, size);
        }
    }

    /**
     *
     * @param heapifyList
     * @param currentNode
     * @param size
     */
    public static void heapifySort(List<Integer> heapifyList, int currentNode, int size) {
        /**
         * 防止下标越界了
         */
        if (currentNode < size) {
            /**
             * 获取当前节点的左右子节点的下标
             */
            int leftNode = 2 * currentNode + 1;
            int rightNode = 2 * currentNode + 2;
            //用于记录值较大的节点下标
            int max = currentNode;
            /**
             *  判断下标是否越界
             *      如果没有越界, 那么判断左节点是否比当前节点的值大
             *      如果比当前节点的值大, 那么记录左节点的值为max
             */
            if (leftNode < size) {
                if (heapifyList.get(max) < heapifyList.get(leftNode)) {
                    max = leftNode;
                }
            }
            /**
             *  判断下标是否越界
             *      如果没有越界, 那么判断右节点是否比当前纪录值要大
             *      如果比当前纪录的值大, 那么将右节点作为纪录值
             */
            if (rightNode < size) {
                if (heapifyList.get(max) < heapifyList.get(rightNode)) {
                    max = rightNode;
                }
            }

            /**
             * 如果最大值的下标不是传入的当前节点, 那么交换当前节点和纪录值的位置
             */
            if (max != currentNode) {
                int temp = heapifyList.get(currentNode);
                heapifyList.set(currentNode, heapifyList.get(max));
                heapifyList.set(max, temp);
                /**
                 * 交换位置后, 继续往下判断 交换后的节点(就是之前的父节点) 是否是交换前节点所在的子树的最大值
                 * 如果不是的话继续往下, 需要继续比较直到所有的子树都满足大顶堆的要求
                 */
                heapifySort(heapifyList, max, size);
            }
        }
    }

}
