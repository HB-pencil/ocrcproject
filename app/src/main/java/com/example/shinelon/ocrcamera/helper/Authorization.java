package com.example.shinelon.ocrcamera.helper;

import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Shinelon on 2017/12/27.
 */


public class Authorization{

    public static String generateKey() throws Exception{
        String current = String.valueOf(System.currentTimeMillis()/1000);
        String expired = String.valueOf(System.currentTimeMillis()/1000+ 3600);
        String original = "a=1253939683&" + "b=hardblack&" + "k=AKIDGSUM4cU98xa2KRBOabUFtQmwviKlN4w0&"
                + "t=" + current + "&" + "e=" + expired;
        byte [] result = new byte[getResult(original).length + original.getBytes().length];
        System.arraycopy(getResult(original),0,result,0,getResult(original).length);
        System.arraycopy(original.getBytes(),0,result,getResult(original).length,original.getBytes().length);
        byte [] b = org.apache.commons.codec.binary.Base64.encodeBase64(result);
        return new String(b);
    }

    public static byte [] getResult(String original) throws Exception{
        String key = "2ua5OWaUCi3CAxZ0ZxAD5mIdmkW6Vknf";
        byte [] ori = original.getBytes();
        byte [] k = key.getBytes();
        SecretKey secretKey = new SecretKeySpec(k,"HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(secretKey);
        return mac.doFinal(ori);
    }


}