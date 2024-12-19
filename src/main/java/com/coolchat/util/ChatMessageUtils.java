package com.coolchat.util;

import cn.hutool.core.util.StrUtil;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ChatMessageUtils {
    public static String getChatSessionId4User(String[] userIds) {
        Arrays.sort(userIds);
        try {
            return Md5Utils.encodeMd5(Arrays.stream(userIds).collect(Collectors.joining("")));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("加密算法异常");
        }
    }

    public static String getChatSessionId4Group(String groupId){
        try {
            return Md5Utils.encodeMd5(groupId);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("加密算法异常");
        }
    }

    public static String cleanHtmlTag(String content){
        if (StrUtil.isEmpty(content)){
            return content;
        }
        content = content.replace("<", "&lt;");
        content = content.replace("\r\n", "<br>");
        content = content.replace("\n", "<br>");
        return content;
    }

    public static String getFileSuffix(String fileName){
        if (StrUtil.isEmpty(fileName)){
            return null;
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
