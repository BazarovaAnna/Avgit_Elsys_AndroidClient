package com.example.elsysandroid;

import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public final class Protocol{
    public static final String URL = "/xmlapi/std";
    public static final SimpleDateFormat DateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

    public static String GetNonce(){
        byte[] nonce = new byte[20];
        Random random = new Random();
        random.nextBytes(nonce);
        return Base64.encodeToString(nonce, Base64.NO_WRAP);
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
            outStream.write(Base64.decode(aNonce, Base64.NO_WRAP));
            outStream.write(aCreationTime.getBytes());
            outStream.write("POST".getBytes());
            outStream.write(Protocol.URL.getBytes());
            outStream.write(aContent);

            //  Covert array of Hex bytes to a String
            byte[] rawHmac = mac.doFinal(outStream.toByteArray());
            return Base64.encodeToString(rawHmac, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
