package me.kagura.crawler.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignatureHelper {

//    public static void main(String[] args) throws UnsupportedEncodingException {
//        //pubkey = Crytor.encodePassword(pubkey, str);
//        String x = "password=oG1ebcLkuTXYJZBzuxxDFgA4PHM0DeBl0tzNZ68iEiLq%2BrN8w3esjKHGeexupArq1IlkVW%2FM1%2B5%2BXFq59tUlb9gVz7Fzd7ueCbEB5BcQolun6bYuA%2BGqSmrz%2FG4C9%2FHHOnyOrTjV9%2FODqemZ6H008L3DUEjpO6dcYBVR2vd%2F%2F44%3D";
//        System.err.println(URLDecoder.decode(x,"utf-8"));
//    }


    public static String getParamSig(String str) {
        try {
            return a("didiwuxiankejiyouxian2013q" + str);
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    private static String a(String str) throws NoSuchAlgorithmException {
        if (str == null || str.length() == 0) {
            return "";
        }
        MessageDigest instance = MessageDigest.getInstance("SHA-1");
        instance.update(str.getBytes());
        return a(instance.digest());
    }

    private static String a(byte[] bArr) {
        String str = "";
        str = "";
        for (byte b : bArr) {
            String toHexString = Integer.toHexString(b & 255);
            if (toHexString.length() == 1) {
                str = str + "0" + toHexString;
            } else {
                str = str + toHexString;
            }
        }
        return str;
    }
}
