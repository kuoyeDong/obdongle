package com.example.obdongle.util;


import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 算法工具
 * Created by adolf_dong on 2016/5/23.
 */
public class MathUtil {
    /**
     * 获得数组中的有效数据
     *
     * @param arry 源数组
     * @return 去掉无用位置的有效数组
     */
    public static byte[] validArray(byte[] arry) {
        String cache = null;
        try {
            cache = new String(arry, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] goal = null;
        try {
            if (cache != null) {
                goal = cache.trim().getBytes("utf-8");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return goal;
    }

    /**
     * 获取字节数据的去符号位值
     */
    public static int validByte(byte src) {
        return src & 0xff;
    }

    /**
     * 获取字节中的特定一个位值
     *
     * @param src   byte
     * @param index 0-7 取第几个位
     * @return 第index位的值  0 1
     */
    public static int byteIndexValid(byte src, int index) {
        return ((src & 0xff) >> index) & 0x01;
    }

    /**
     * 从直接的startPos开始取len长度的bit的值
     *
     * @param src      字节
     * @param startPos 开始位置，注意bit7-bit0的bit顺序
     * @param len      取的长度
     */
    public static int byteIndexValid(byte src, int startPos, int len) {
        if (startPos + len > 8) {
            throw new IndexOutOfBoundsException();
        }
        int srcInt = src & 0xff;
        int sum = 0;
        for (int i = 0; i < len; i++) {
            sum += ((srcInt >> (startPos + i)) & 0x01) << i;
        }
        return sum;
    }

    /**
     * 通过组索引获得组成员
     *
     * @param bytes    组索引
     * @param integers 组成员装载容器
     */
    public static void index2List(byte[] bytes, List<Integer> integers) {
        for (int index = 0; index < 32; index++) {
            if (bytes[index] == 0) {
                continue;
            }
            for (int i = 0; i < 8; i++) {
                if (((bytes[index] >> i) & 0x01) != 0) {
                    int indexVal = index * 8 + i + 1;
                    integers.add(indexVal);
                }
            }
        }
    }

    /**
     * 判断字节数组值是否为0
     */
    public static boolean byteArrayIsZero(byte[] src) {
        for (byte aSrc : src) {
            if (aSrc != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字节数组值是否全为ff
     */
    public static boolean byteArrayIsff(byte[] src, int start, int len) {
        for (int i = 0; i < len; i++) {
            byte aSrc = src[i + start];
            if ((aSrc & 0xff) != 0xff) {
                return false;
            }
        }
        return true;
    }



    /**
     * 改变某个字节的某个位
     *
     * @param src    目标字节
     * @param dst    要改变的位 0-7
     * @param isTrue 1为true
     * @return 目标位置
     */
    public static byte changeByteIndexvalue(byte src, int dst, boolean isTrue) {
        byte goal;
        if (dst == 0) {
            goal = (byte) ((MathUtil.byteIndexValid(src, 1, 7) << 1) + (isTrue ? 1 : 0));
        } else if (dst == 7) {
            goal = (byte) (MathUtil.byteIndexValid(src, 0, 7) + (isTrue ? 0x80 : 0));
        } else {
            goal = (byte) ((MathUtil.byteIndexValid(src, dst + 1, 7 - dst) << (dst + 1))
                    + (isTrue ? 1 << dst : 0) + MathUtil.byteIndexValid(src, 0, dst));
        }
        return goal;
    }

    /**
     * 改变某个字节的某个位
     *
     * @param src    目标字节
     * @param dst    要改变的位 0-7
     * @param isTure 1为true
     * @return 目标位置
     */
    public static byte setBitIndex(byte src, int dst, boolean isTure) {
        if (isTure) {
            src |= (1 << dst);
        } else {
            src &= ~(1 << dst);
        }
        return src;
    }

    /**
     * 反转字节的某个bit
     *
     * @param src 字节
     * @param dst 目标位置
     */
    public static byte toggleBitIndex(byte src, int dst) {
        src ^= (1 << dst);
        return src;
    }


}

