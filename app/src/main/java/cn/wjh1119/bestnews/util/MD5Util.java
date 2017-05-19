package cn.wjh1119.bestnews.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5工具类
 * Created by Mr.King on 2017/5/12 0012.
 */

class MD5Util {

    static String getMD5String(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] e = md.digest(value.getBytes());
            return toHexString(e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return value;
        }
    }

//    public static String getMD5String(byte[] bytes) {
//        try {
//            MessageDigest md = MessageDigest.getInstance("md5");
//            byte[] e = md.digest(bytes);
//            return toHexString(e);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//            return "";
//        }
//    }

    private static String toHexString(byte bytes[]) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (byte aByte : bytes) {
            stmp = Integer.toHexString(aByte & 0xff);
            if (stmp.length() == 1)
                hs.append("0").append(stmp);
            else
                hs.append(stmp);
        }

        return hs.toString();
    }
}