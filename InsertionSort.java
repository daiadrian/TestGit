package com.dai.sort;

import java.util.ArrayList;

/**
 * Created by dh on 2018/9/6.
 *
 *
 * 插入排序
 * 想象一下扑克牌的方法，抽出小的牌放到最左边
 *
 *
 */
public class InsertionSort {

    public static void sort(Integer[] arrays) {
        if (arrays == null || arrays.length <= 0) {
            return;
        }
        if (arrays.length > 1) {
            for (int i = 1; i < arrays.length; i++) {
                //记录拿出来的那个值
                int get = arrays[i];
                //往前开始比较然后插入
                int j = i - 1;
                //往前比较直到最左边位置上的值
                //并且只有当前一位的数值大于当前取出的数值，再进行交换
                //   否则不处理(因为从左边开始进行的比较，证明左边的其实已经是有序的了
                //                所以当前值如果大于前一个数，那么它肯定大于前一个数左边位置上的数)
                while (j >= 0 && arrays[j] > get) {
                    //将前一位的数放到当前位置上
                    arrays[j + 1] = arrays[j];
                    //继续往左前一位移动
                    j--;
                }
                //直到该值比前一位的值小(或二者相等)，
                //    将抓到的前一位值插入到该值右边(相等元素的相对次序未变，所以插入排序是稳定的)
                //j+1是因为上面的j进行了减一的处理，所以需要重新加一才能回到该位置上
                arrays[j + 1] = get;
            }
        }
    }

}
