package com.example.elsysandroid;

import android.util.Base64;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

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
 * @author ITMO students Bazarova Anna, Denisenko Kirill, Ryabov Sergey, Chernyshev Nikita
 * @version 1.0
 */
public final class Protocol{
    /** Поле с URL */
    public static final String URL = "/xmlapi/std";

    /** Поле - формат серверных даты-времени для конвертации в строку */
    public static final SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    /** Поле - формат локальных даты-времени для конвертации в строку */
    public static final SimpleDateFormat LocalDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    /** Задаем Time Zone */
    static {
        DateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Функция для конвертации xml-элемента в строку
     * @param element xml-элемент
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
     * @param aCID ID клиента
     * @param aSIDResp ID ответа сервера
     * @return возвращает xml-элемент, готовый к отправке
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
     * @param aCID ID клиента
     * @param aSIDResp ID ответа сервера
     * @param date скорректированное время
     * @return возвращает xml-элемент, готовый к отправке
     * @see Protocol#GetXContent(int, int, Date)
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
     * Функция для формирования аргуметнов для запроса в виде xml
     * @param aCID ID клиента
     * @param aSIDResp ID ответа сервера
     * @param aInitData xml-элемент, добавляемый в запрос
     * @return возвращает xml-элемент, готовый к отправке
     * @see Protocol#GetXContent(int, int, Element)
     */
    public static Element GetXContent(int aCID, int aSIDResp, Element aInitData)
    {
        Element root = GetXContent(aCID, aSIDResp);
        Document document = root.getOwnerDocument();

        Element body = (Element) root.getElementsByTagName("Body").item(0);
        body.appendChild(document.adoptNode(aInitData.cloneNode(true)));

        return root;
    }

    /**
     * Функция для формирования команды контроллеру виде xml
     * @param aID ID элемента
     * @param aDevType тип контроллера
     * @param aCommand тип команды
     * @param aCommandID номер команды
     * @return возвращает xml-элемент, готовый к отправке
     * @see Protocol#GetCommand(int, int, int, int)
     */
    public static Element GetCommand(int aID, int aDevType, int aCommand, int aCommandID)
    {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element root = document.createElement("ControlCmd");
            document.appendChild(root);

            Element devId = document.createElement("DevID");
            devId.appendChild(document.createTextNode(Integer.toString(aID)));
            root.appendChild(devId);

            Element devType = document.createElement("DevType");
            devType.appendChild(document.createTextNode(Integer.toString(aDevType)));
            root.appendChild(devType);

            Element action = document.createElement("Action");
            action.appendChild(document.createTextNode(Integer.toString(aCommand)));
            root.appendChild(action);

            Element id = document.createElement("ID");
            id.appendChild(document.createTextNode(Integer.toString(aCommandID)));
            root.appendChild(id);

            return root;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Функция для формирования команды контроллеру виде xml
     * @param aID ID элемента
     * @param aDevType тип контроллера
     * @param aCommand тип команды
     * @param aCommandID номер команды
     * @param aDate время создания команды
     * @return возвращает xml-элемент, готовый к отправке
     * @see Protocol#GetCommand(int, int, int, int, Date)
     */
    public static Element GetCommand(int aID, int aDevType, int aCommand, int aCommandID, Date aDate) {
        Element root = GetCommand(aID, aDevType, aCommand, aCommandID);
        Document document = root.getOwnerDocument();

        Element date = document.createElement("DateTime");
        date.appendChild(document.createTextNode(DateFormat.format(aDate)));
        root.appendChild(date);

        return root;
    }

    /**
     * Функция для формирования случайной последовательности из 20 байт
     * @return возвращает случайную последовательность из 20 байт
     */
    public static String GetNonce(){
        byte[] nonce = new byte[20];
        Random random = new Random();
        random.nextBytes(nonce);
        return Base64.encodeToString(nonce, Base64.NO_WRAP);
    }

    /**
     * Функция, формирующая зашифрованную строку алгоритмом HMAC-SHA-1
     * @param aNonce случайная последовательность из 20 байт
     * @param aPassword ключ (пароль для шифрования)
     * @param aContent последовательность байтов, которую необходимо зашифровать
     * @param aCreationTime время начала формирования запроса
     * @return возвращает зашифрованную строку
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