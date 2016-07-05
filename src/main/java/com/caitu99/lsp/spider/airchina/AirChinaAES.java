package com.caitu99.lsp.spider.airchina;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 国航AES加密算法
 *
 * @author lawrence
 * @ClassName: AirChinaAES
 * @date 2015年11月11日 下午2:45:36
 * @Copyright (c) 2015-2020 by caitu99
 */
public class AirChinaAES {

    public static String encrypt(String src, String sKey) throws Exception {
        String ivs = sKey;
        byte[] raw = sKey.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//"算法/模式/补码方式"  
        IvParameterSpec iv = new IvParameterSpec(ivs.getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度  
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(src.getBytes());

        return org.apache.commons.codec.binary.Base64.encodeBase64String(encrypted);//此处使用BAES64做转码功能，同时能起到2次加密的作用。  
    }

    public static String decrypt(String src, String sKey) throws Exception {
        try {
            String ivs = sKey;
            byte[] raw = sKey.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(ivs
                    .getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = org.apache.commons.codec.binary.Base64.decodeBase64(src);//先用bAES64解密  
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original);
                return originalString;
            } catch (Exception e) {
                System.out.println(e.toString());
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        //
        String data = "147258";
        String key = "7093938041116273";
        String en = encrypt(data, key);
        System.out.println(en);

        System.out.println(decrypt(en, key));

    }

} 