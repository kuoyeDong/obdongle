package com.example.obdongle.util;

/**
 * 数据类型转换工具
 * Created by adolf_dong on 2016/1/20.
 */
public class Transformation {


    /**
     * byte数组转为十六进制字符串
     *
     * @return 十六进制字符串
     */
    public static String byteArryToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte aSrc : src) {
            int v = aSrc & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


    /**
     * 字节转16进制
     *
     * @param src
     * @return
     */
    public static String byte2HexString(byte src) {
        StringBuilder stringBuilder = new StringBuilder("");
        int v = src & 0xFF;
        String hv = Integer.toHexString(v);
        // FIXME: 2016/7/21 不补0
        if (hv.length() < 2) {
            stringBuilder.append(0);
        }
        stringBuilder.append(hv);
        return stringBuilder.toString();
    }

    /**
     * 取目标字节后4位转换成16进制String
     *
     * @param src 引用的字节资源
     * @return
     */
    public static String halfByte2HexString(byte src) {
        StringBuilder stringBuilder = new StringBuilder("");
        int v = src & 0x0F;
        String hv = Integer.toHexString(v);
        if (hv.length() < 2) {
            stringBuilder.append(0);
        }
        stringBuilder.append(hv);
        return stringBuilder.toString();
    }

    /**
     * 用于设置edit输入内容
     *
     * @return
     */
    public static char[] setType() {
        return new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
                'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
                'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D',
                'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
                'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
                'Y', 'Z'};
    }


    /**
     * 返回十六进制字符串的字节数组
     *
     * @param hexStr
     * @return
     */

    public static byte[] hexString2Bytes(String hexStr) {
        int l = hexStr.length() / 2 + hexStr.length() % 2;
        byte[] bytes = new byte[l];
        for (int i = 0; i < bytes.length; i++) {
            int value;
            if (2 * (i + 1) < hexStr.length()) {
                value = Integer.valueOf(hexStr.substring(2 * i, 2 * (i + 1)), 16);
            } else {
                value = Integer.valueOf(hexStr.substring(2 * i), 16);
            }
            bytes[i] = (byte) value;
        }
        return bytes;
    }


}