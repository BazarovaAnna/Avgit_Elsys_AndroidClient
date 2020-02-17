package com.example.elsysandroid;

import android.util.Base64;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
/**
 * Класс, реализующий шифрование и кодирование
 * @autor ITMO students Bazarova Anna, Denisenko Kirill, Ryabov Sergey
 * @version 1.0
 */
public final class Protocol{
    /** Поле с URL */
    public static final String URL = "/xmlapi/std";

    /** Поле - формат серверных даты-времени для конвертации в строку */
    public static final SimpleDateFormat DateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    /** Поле - формат локальных даты-времени для конвертации в строку */
    public static final SimpleDateFormat LocalDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

    /**
     * Функция для конвертации xml-элемента в строку
     * @param element - xml-элемент
     * @return возвращает полученную строку
     */
    public static String toString(Element element) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(element), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }

    /**
     * Функция для формирования аргуметнов для запроса в виде xml
     * @param aCID - ID клиента
     * @param aSIDResp - ID ответа сервера
     * @return - xml-элемент, готовый к отправке
     * @see Protocol#GetXContent(int, int, Date)
     */
    public static Element GetXContent(int aCID, int aSIDResp) {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element root = document.createElement("Envelope");
            document.appendChild(root);

            Element body = document.createElement("Body");
            root.appendChild(body);

            Element cid = document.createElement("CID");
            cid.appendChild(document.createTextNode(Integer.toString(aCID)));
            body.appendChild(cid);

            Element sid = document.createElement("SID");
            sid.appendChild(document.createTextNode(Integer.toString(aSIDResp)));
            body.appendChild(sid);

            return root;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Функция для формирования аргуметнов для запроса в виде xml
     * @param aCID - ID клиента
     * @param aSIDResp - ID ответа сервера
     * @param date - скорректированное время
     * @return - xml-элемент, готовый к отправке
     * @see Protocol#GetXContent(int, int)
     */
    public static Element GetXContent(int aCID, int aSIDResp, Date date) {
        Element root = GetXContent(aCID, aSIDResp);
        Element body = (Element) root.getElementsByTagName("Body").item(0);
        Document document = root.getOwnerDocument();

        Element setDateTime = document.createElement("SetDateTime");
        body.appendChild(setDateTime);

        Element localTime = document.createElement("LocalTime");
        localTime.appendChild(document.createTextNode(LocalDateFormat.format(date)));
        setDateTime.appendChild(localTime);

        Element utcTime = document.createElement("UTCTime");
        utcTime.appendChild(document.createTextNode(DateFormat.format(date)));
        setDateTime.appendChild(utcTime);

        return root;
    }

    /**
     * Функция для формирования случайной последовательности из 20 байт
     * @return - случайная последовательность из 20 байт
     */
    public static String GetNonce(){
        byte[] nonce = new byte[20];
        Random random = new Random();
        random.nextBytes(nonce);
        return Base64.encodeToString(nonce, Base64.NO_WRAP);
    }

    /**
     * Функция, формирующая зашифрованную строку алгоритмом HMAC-SHA-1
     * @param aNonce - случайная последовательность из 20 байт
     * @param aPassword - ключ (пароль для шифрования)
     * @param aContent - последовательность байтов, которую необходимо зашифровать
     * @param aCreationTime - время начала формирования запроса
     * @return - зашифрованная строка
     * @see PollTask
     */
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
