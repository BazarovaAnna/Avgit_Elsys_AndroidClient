package com.example.elsysandroid;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;


import java.io.ByteArrayOutputStream;
import java.util.Random;

public final class Protocol{
    public static final String URL = "/xmlapi/std";

    public static String GetNonce(){
        byte[] nonce = new byte[20];
        Random random = new Random();
        random.nextBytes(nonce);
        return nonce.toString();
    }

    public static String GetDigest(String aNonce, String aPassword, byte[] aContent, String aCreationTime){
        try {
            // Get an hmac_sha1 key from the raw key bytes
            byte[] keyBytes = aPassword.getBytes();
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);


            // Compute the hmac on input data bytes
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            outStream.write(aNonce.getBytes());
            outStream.write(aCreationTime.getBytes());
            outStream.write("POST".getBytes());
            outStream.write(Protocol.URL.getBytes());
            outStream.write(aContent);

            byte[] rawHmac = mac.doFinal(outStream.toByteArray());


            // Convert raw bytes to Hex
            byte[] hexBytes = new Hex().encode(rawHmac);

            //  Covert array of Hex bytes to a String
            return new String(hexBytes, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
