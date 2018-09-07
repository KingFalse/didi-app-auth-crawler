package me.kagura.crawler.utils;

import javax.crypto.Cipher;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSACryptography {

    public static String data = "hello world";
    public static String publicKeyString = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUlHZk1BMEdDU3FHU0liM0RRRUJBUVVBQTRHTkFEQ0JpUUtCZ1FDM3RvclRjaFYzRmc3Q25VZnI1ck5TR05UOQo3eElVcG9KcTZSczF0dGo2YXNXa3ZsSUlHY1l3SC9GSlF0QSswLzlVbCs3dnJqcTh0dnlMbzBFU3duczMreHAwCmV4bUxGZ09IVGdjNzgxZzJScHc0SVJYVVc1dXdmZTZzYUQzNUJvbHE3Y1llMk1kV3Fzd0tvR0RJcXN5Y0RzdXUKZWZqdGtTSHE3d0dJM0tKOHlRSURBUUFCCi0tLS0tRU5EIFBVQkxJQyBLRVktLS0tLQo=";

    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        String pk = new String(Base64.getDecoder().decode(publicKeyString)).replaceAll("-----[A-Z]{3,5} PUBLIC KEY-----", "").replaceAll("\\s", "");
        //获取公钥
        PublicKey publicKey = getPublicKey(pk);
        //公钥加密
        byte[] encryptedBytes = encrypt(data.getBytes(), publicKey);
        String base64 = Base64.getEncoder().encodeToString(encryptedBytes);
        System.err.println(URLEncoder.encode(base64, "UTF-8"));
    }

    public static String encrypt(String pk, String pass) throws Exception {
        pk = new String(Base64.getDecoder().decode(pk)).replaceAll("-----[A-Z]{3,5} PUBLIC KEY-----", "").replaceAll("\\s", "");
        PublicKey publicKey = getPublicKey(pk);
        byte[] encryptedBytes = encrypt(data.getBytes(), publicKey);
        String base64 = Base64.getEncoder().encodeToString(encryptedBytes);
        System.err.println(URLEncoder.encode(base64, "UTF-8"));
        return URLEncoder.encode(base64, "UTF-8");
    }

    //将base64编码后的公钥字符串转成PublicKey实例  
    public static PublicKey getPublicKey(String publicKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKey.getBytes());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    //公钥加密
    public static byte[] encrypt(byte[] content, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");//java默认"RSA"="RSA/ECB/PKCS1Padding"
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(content);
    }

}