package com.coolchat.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Utils {

    /**
     * MD5 加密
     *
     * @param input 输入的字符串
     * @return MD5 哈希值（32位十六进制字符串）
     * @throws NoSuchAlgorithmException 如果不支持 MD5 算法
     */
    public static String encodeMd5(String input) throws NoSuchAlgorithmException {
        // 创建 MessageDigest 实例，指定使用 MD5 算法
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");

        // 将输入字符串转换成字节数组
        byte[] inputBytes = input.getBytes();

        // 使用 MD5 算法计算哈希值
        byte[] hashBytes = messageDigest.digest(inputBytes);

        // 将字节数组转换为十六进制字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            // 将每个字节转换为 2 位十六进制数字并附加到结果字符串中
            hexString.append(String.format("%02x", b));
        }

        // 返回 32 位的十六进制字符串
        return hexString.toString();
    }
}