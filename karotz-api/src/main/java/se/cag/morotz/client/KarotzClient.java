/*
 * @author Daniel Marell
 *         Created 2012-01-11 14:37
 */
package se.cag.morotz.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class KarotzClient implements KarotzApi {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String KAROTZ_URL_START = "http://api.karotz.com/api/karotz/start";
    private static final String KAROTZ_URL_INTERACTIVE_MODE = "http://api.karotz.com/api/karotz/interactivemode";

    private Client client = Client.create();
    private String interactiveId;
    private String apiKey;
    private String secretKey;
    private String installId;

    /**
     * @param apiKey    application APIKey
     * @param secretKey application SecretKey
     * @param installId application Install ID
     */
    public KarotzClient(String apiKey, String secretKey, String installId) {
        this.installId = installId;
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    public String getInteractiveId() {
        return interactiveId;
    }

    public boolean isInteractive() {
        return interactiveId != null;
    }

    @Override
    public void setLedColor(LedColor color) throws KarotzException {
        log.trace("setLedColor,color=" + color);
        MultivaluedMap<String, String> parameters = new MultivaluedMapImpl();
        parameters.add("apikey", apiKey);
        parameters.add("interactiveid", getInteractiveId());
        parameters.add("action", "light");
        parameters.add("color", color.getCode());
        WebResource webResource = client.resource("http://api.karotz.com/api/karotz/led");
        ClientResponse response = webResource.queryParams(parameters).accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
        String result = response.getEntity(String.class);
        String code = parseResponse(result, "code");
        if (!"OK".equalsIgnoreCase(code)) {
            throw new KarotzException("failed to do action: " + code);
        }
    }

    @Override
    public void setLedOff() {
        log.trace("setLedOff");
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void takePicture(String url) throws KarotzException {
        log.trace("takePicture,url=" + url);
        MultivaluedMap<String, String> parameters = new MultivaluedMapImpl();
        parameters.add("apikey", apiKey);
        parameters.add("interactiveid", getInteractiveId());
        parameters.add("action", "photo");
        parameters.add("url", url);
        WebResource webResource = client.resource("http://api.karotz.com/api/karotz/webcam");
        ClientResponse response = webResource.queryParams(parameters).get(ClientResponse.class);
        String result = response.getEntity(String.class);
        String code = parseResponse(result, "code");
        if (!"OK".equalsIgnoreCase(code)) {
            throw new KarotzException("failed to do action: " + code);
        }
    }

    public synchronized void startInteractiveMode() throws KarotzException {
        log.trace("startInteractiveMode");
        if (isInteractive()) {
            return;
        }
        Random random = new Random();
        MultivaluedMap<String, String> parameters = new MultivaluedMapImpl();
        parameters.add("apikey", apiKey);
        parameters.add("installid", installId);
        parameters.add("once", String.valueOf(random.nextInt(99999999)));
        parameters.add("timestamp", String.valueOf((int) (System.currentTimeMillis() / 1000L)));

        String sign = getSignature(parameters);
        parameters.add("signature", sign);

        WebResource webResource = client.resource(KAROTZ_URL_START);
        ClientResponse response = webResource.queryParams(parameters).accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
        log.trace("Response=" + response);
        if (response.getStatus() != 200) {
            throw new KarotzException("Failed : HTTP error code : " + response.getStatus());
        }
        String result = response.getEntity(String.class);
        log.trace("Result=" + result);

        interactiveId = parseResponse(result, "interactiveId");
        if (interactiveId == null) {
            String code = parseResponse(result, "code");
            throw new KarotzException("[code] " + code);
        }
    }

    public String getSignature(MultivaluedMap<String, String> params) {
        List<KeyValue> entries = new ArrayList<KeyValue>();
        for (Map.Entry<String, List<String>> e : params.entrySet()) {
            for (String value : e.getValue()) {
                entries.add(new KeyValue(e.getKey(), value));
            }
        }
        Collections.sort(entries, new Comparator<KeyValue>() {
            @Override
            public int compare(KeyValue key, KeyValue value) {
                return key.getKey().compareTo(value.getKey());
            }
        });

        StringBuilder paramString = new StringBuilder();
        for (KeyValue e : entries) {
            if (paramString.length() > 0) {
                paramString.append("&");
            }
            paramString.append(e.getKey());
            paramString.append("=");
            paramString.append(e.getValue());
        }

        try {
            return doHmacSha1(secretKey, paramString.toString());
        } catch (KarotzException e) {
            throw new RuntimeException();
        }
    }


    public synchronized void stopInteractiveMode() throws KarotzException {
        if (!isInteractive()) {
            return;
        }
        MultivaluedMap<String, String> parameters = new MultivaluedMapImpl();
        parameters.add("action", "stop");
        parameters.add("interactiveid", interactiveId);
        WebResource webResource = client.resource(KAROTZ_URL_INTERACTIVE_MODE);
        ClientResponse response = webResource.queryParams(parameters).accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new KarotzException("Failed : HTTP error code : " + response.getStatus());
        }
        String result = response.getEntity(String.class);
        String code = parseResponse(result, "code");
        if (!"OK".equalsIgnoreCase(code) && !"NOT_CONNECTED".equalsIgnoreCase(code)) {
            throw new KarotzException("[code] " + code);
        }

        interactiveId = null;
    }

    /**
     * Parses response from karotz.
     *
     * @param response response from karotz
     * @param tagName
     * @return tag value
     * @throws KarotzException illega response
     */
    public String parseResponse(String response, String tagName) throws KarotzException {
        if (response == null || tagName == null) {
            throw new IllegalArgumentException("params should not be null.");
        }

        String value;
        try {
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = parser.parse(new InputSource(new StringReader(response)));
            Element elt = (Element) document.getElementsByTagName(tagName).item(0);
            if (elt == null) {
                return null;
            }
            value = elt.getTextContent();
        } catch (SAXException e) {
            throw new KarotzException(e);
        } catch (ParserConfigurationException e) {
            throw new KarotzException(e);
        } catch (IOException e) {
            throw new KarotzException(e);
        }

        return value;
    }

    /**
     * Creates HmacSha1.
     *
     * @param secretKey SecretKey
     * @param data      target data
     * @return HmacSha1
     * @throws KarotzException Illegal encoding.
     */
    private static String doHmacSha1(String secretKey, String data) throws KarotzException {
//        String hmacSha1;
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
//            SecretKeySpec secret = new SecretKeySpec(secretKey.getBytes("ASCII"), "HmacSHA1");
            SecretKeySpec secret = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");
            mac.init(secret);
//            byte[] digest = mac.doFinal(data.getBytes("UTF-8"));
            byte[] digest = mac.doFinal(data.getBytes());

            byte[] hexBytes = new Base64().encode(digest);
            return URLEncoder.encode(new String(hexBytes), "UTF-8");
//            hmacSha1 = new String(Base64.encode(digest), "ASCII");
        } catch (IllegalStateException e) {
            throw new KarotzException(e);
        } catch (InvalidKeyException e) {
            throw new KarotzException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new KarotzException(e);
        } catch (UnsupportedEncodingException e) {
            throw new KarotzException(e);
        }

//        return hmacSha1;
    }

    private static class KeyValue {
        private String key;
        private String value;

        private KeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

}
