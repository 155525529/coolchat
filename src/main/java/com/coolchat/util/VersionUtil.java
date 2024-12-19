package com.coolchat.util;

public class VersionUtil {

    public static int compareVersions(String version1, String version2) {
        String[] v1 = version1.split("\\.");
        String[] v2 = version2.split("\\.");

        int length = Math.max(v1.length, v2.length);
        for (int i = 0; i < length; i++) {
            int v1Part = i < v1.length ? Integer.parseInt(v1[i]) : 0;
            int v2Part = i < v2.length ? Integer.parseInt(v2[i]) : 0;
            
            if (v1Part < v2Part) return -1;
            if (v1Part > v2Part) return 1;
        }
        return 0;
    }
}