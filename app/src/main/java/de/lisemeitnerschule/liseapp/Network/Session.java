package de.lisemeitnerschule.liseapp.Network;

import android.util.Base64;

import com.google.gdata.util.common.base.PercentEscaper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import de.lisemeitnerschule.liseapp.LiseApp;


public class Session {
    private static HashMap<String,Session> sessions = new HashMap<String,Session>();
    public static Session instance(String username,String SecretHash) throws Exception {
        if(sessions.containsKey(SecretHash)){
            return sessions.get(username);
        }else {
            Session res = new Session(username,SecretHash);
            sessions.put(username,res);
            return res;
        }
    }
    public static Session instance(String username)  {
        try {
            return sessions.get(username);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static final String baseUrl = LiseApp.URL+"/rest/LiseApp/";

        public final String  SecretHash;
        public final String  username  ;
    private static final PercentEscaper percentEscaper = new PercentEscaper("-._~", false);
    public static String login(String username,String password) throws Exception {
        HttpURLConnection connection ;
        try {
            //prepare the Login Data
            JSONObject data = new JSONObject();
            data.put("username",username);
            data.put("password",password);
            data.put("nonce",generateNonce());
            data.put("timestamp",System.currentTimeMillis() / 1000L);
            String dataString = data.toString();

            connection = (HttpURLConnection) new URL(baseUrl+"login").openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "utf-8");
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Length", "" + Integer.toString(dataString.getBytes().length));
            connection.connect();
            DataOutputStream out  = new DataOutputStream(connection.getOutputStream ());
            out.writeBytes(dataString);
            out.flush();
            out.close();
            if(connection.getResponseCode()!=200){
                byte[] buff = new byte[connection.getContentLength()];
                connection.getErrorStream().read(buff);
                throw new Exception("Wrong username/password combination: "+new String(buff));
            }
            byte[] buff = new byte[connection.getContentLength()];
            connection.getInputStream().read(buff);
            return percentEscaper.escape(new String(buff));
        } catch (Exception e) {
            throw new Exception("failed to Login as: "+username,e);
        }
    }
    public Session(String username,String SecretHash) throws Exception {

        try {
            this.username = username;
            this.SecretHash = SecretHash;
            apiRequest("test");
        }catch (Exception e){
            throw new Exception("Failed to authenticate as "+username,e);
        }
    }





    private static final HashMap<String,ApiFuction> knownFunctions = new HashMap<String,ApiFuction>();
        static{
            knownFunctions.put("test",new ApiFuction("test")); //Just to test the Autoauthentication. This is a GET request which returns 'Hello World' when the Authentication Header is correct.
            knownFunctions.put("news",new ApiPostFuction("news",new String[]{"timestamp"})); //Returns an JSON string with all relevant News for the specified user

        }



    public JSONObject apiRequest(ApiFuction function,JSONObject data) throws Exception {
        //POST API call;
        ((ApiPostFuction)function).setData(data);
        return function.execute(this);
    }
    public JSONObject apiRequest(String function,JSONObject data) throws Exception {
        //POST API call;
        return apiRequest(knownFunctions.get(function), data);
    }
    public JSONObject apiRequest(ApiFuction function,JSONArray data) throws Exception {
        //POST API call;
        ((ApiPostFuction)function).setData(data);
        return function.execute(this);
    }
    public JSONObject apiRequest(String function,JSONArray data) throws Exception {
        //POST API call;
        return apiRequest(knownFunctions.get(function), data);
    }
    public JSONObject apiRequest(ApiFuction function,String[] data) throws Exception {
        //POST API call;
        ((ApiPostFuction)function).setData(data);
        return function.execute(this);
    }
    public JSONObject apiRequest(String function,String[] data) throws Exception {
        //POST API call;
        return apiRequest(knownFunctions.get(function), data);
    }
    public JSONObject apiRequest(String function) throws Exception {
        //GET API call;
        return apiRequest(knownFunctions.get(function));
    }
    public JSONObject apiRequest(ApiFuction function) throws Exception {
        //GET API call;
        return function.execute(this);
    }







        private static String generateNonce() {
        try {

            // Generate a random seed
            byte[] seed;

                SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");

                // Get 1024 random bits
                byte[] bytes = new byte[1024/8];
                sr.nextBytes(bytes);

                // Create two secure number generators with the same seed
                int seedByteCount = 10;
                seed = sr.generateSeed(seedByteCount);


            sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(seed);
            return percentEscaper.escape(new String(MessageDigest.getInstance("MD5").digest((""+sr.nextInt()+System.currentTimeMillis()).getBytes())).trim());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


            //shouldn't ever occur
        return null;
    }


    public HttpURLConnection createConnection(String path) throws IOException, URISyntaxException {
        HttpURLConnection connection = (HttpURLConnection) new URL(path).openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("charset", "utf-8");

        return connection;
    }
    public static final String publicHash = "public";
    public String generateOAuthHeader(HttpURLConnection connection) throws IOException, URISyntaxException {
        System.err.println("SecretHash: "+this.SecretHash);
        if(this.SecretHash.isEmpty()||this.SecretHash.equals(publicHash)){
            System.err.println("public Account detected!");
            return "public";
        }


        String res = "realm=\""+connection.getURL().toString()+"\","+
                "oauth_consumer_key=\""+username+"\","+
                "oauth_token=\"\","+
                "oauth_signature_method=\"HMAC-SHA1\","+
                "oauth_signature=\"\","+
                "oauth_timestamp=\""+System.currentTimeMillis()/1000L+"\","+
                "oauth_nonce=\""+generateNonce()+"\","+
                "oauth_version=\"1.0\"";
        connection.setRequestProperty("Authorization",res);
        String signatur = generateOAuthSignatur(connection);
        res = res.replace("oauth_signature=\"\",","oauth_signature=\""+signatur+"\",");

        connection.setRequestProperty("Authorization",res);
        return res;
    }
    public String generateOAuthSignatur(HttpURLConnection connection) throws IOException, URISyntaxException {
        byte[] keyBytes;
        try {
            String key = ((SecretHash)+'&');
            keyBytes = key.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            //Should never happen
            e.printStackTrace();
            return null;
        }
        SecretKeySpec key = new SecretKeySpec(keyBytes,"HmacSHA1");
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            //Sould never happen
            e.printStackTrace();
            return null;
        }
        try {
            mac.init(key);
        } catch (InvalidKeyException e) {
            //key is invalid: not the Useres fault so we just catch it;
            new Exception("Secret: "+SecretHash+" invalid!",e).printStackTrace();
            return  null;
        }

        String sbs = new SignatureBaseString(connection).generate();
        byte[] text;
        try {
            text = sbs.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            //shoudl never happen
            e.printStackTrace();
            return null;
        }
        byte[] HmacSHA1encoded = mac.doFinal(text);
        String signatur =  percentEscaper.escape(Base64.encodeToString(HmacSHA1encoded, Base64.DEFAULT).trim());
        System.err.println(signatur);
        return signatur;
    }
//Copyright for SignaturBaseString class(I made some modifications to replace some of the functions normally provided by some of his other classes. All credit goes to atthias Kaeppler):
/*
 * Copyright (c) 2009 Matthias Kaeppler Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */


    private class SignatureBaseString {

        private HttpURLConnection request;

        private Map<String,List<String>> requestParameters;

        /**
         * Constructs a new SBS instance that will operate on the given request
         * object and parameter set.
         *
         * @param request
         *        the HTTP request
         */
        public SignatureBaseString(HttpURLConnection request) {
            this.request = request;
            this.requestParameters = request.getRequestProperties();
        }

        /**
         * Builds the signature base string from the data this instance was
         * configured with.
         *
         * @return the signature base string
         */
        public String generate() throws IOException, URISyntaxException {

                String normalizedUrl = normalizeRequestUrl();
                String normalizedParams = normalizeRequestParameters();

                return request.getRequestMethod() + '&' + percentEscaper.escape(normalizedUrl) + '&'
                        + percentEscaper.escape(normalizedParams);

        }

        public String normalizeRequestUrl() throws URISyntaxException {
            URI uri = request.getURL().toURI();
            String scheme = uri.getScheme().toLowerCase();
            String authority = uri.getAuthority().toLowerCase();
            boolean dropPort = (scheme.equals("http") && uri.getPort() == 80)
                    || (scheme.equals("https") && uri.getPort() == 443);
            if (dropPort) {
                // find the last : in the authority
                int index = authority.lastIndexOf(":");
                if (index >= 0) {
                    authority = authority.substring(0, index);
                }
            }
            String path = uri.getRawPath();
            if (path == null || path.length() <= 0) {
                path = "/"; // conforms to RFC 2616 section 3.2.2
            }
            // we know that there is no query and no fragment here.
            return scheme + "://" + authority + path;
        }

        /**
         * Normalizes the set of request parameters this instance was configured
         * with, as per OAuth spec section 9.1.1.
         *
         * @return the normalized params string
         * @throws IOException
         */
        public String normalizeRequestParameters() throws IOException {
            if (requestParameters == null) {
                return "";
            }

            List<String> res = new ArrayList<String>();


            StringBuilder sb = new StringBuilder();
            String[] values = requestParameters.get("Authorization").get(0).replace("\"", "").split(",");
            Arrays.sort(values);
            for(String current:values){
                if(current.startsWith("oauth_signature=")||current.startsWith("realm"))continue;
                if(sb.length()>0)sb.append("&");
                sb.append(current);
            }

            return sb.toString();
        }
    }
}

class ApiFuction{
    protected final String name;
    public ApiFuction(String name){
        this.name = name;
    }
    public JSONObject execute(Session session) throws Exception {
        HttpURLConnection connection = session.createConnection(Session.baseUrl+name);
        configureConnection(connection);
        session.generateOAuthHeader(connection);
        connect(connection);
        byte[] buff;
        if(connection.getResponseCode()!=200){
            buff = new byte[connection.getContentLength()];
            connection.getErrorStream().read(buff);
            throw new Exception("An error occurred while executing the APIRequest: "+name+" Error Code: "+connection.getResponseCode()+" Error: "+new String(buff));
        }
        buff = new byte[connection.getContentLength()];
        connection.getInputStream().read(buff);
        String rawData = new String(buff);
        JSONObject data = null;
        try {
            String json = rawData.substring(rawData.indexOf('{'));
            try {
                data = new JSONObject(json);
            }catch(Exception e){
                throw new Exception("Failed to parse json form message: "+json);
            }
        }catch (StringIndexOutOfBoundsException e){
            new Exception("Failed to parse message "+rawData+", because it is to short (message is"+rawData.length()+" min Length is 3)",e).printStackTrace();
        }
        connection.disconnect();
        return data;
    }
    protected void configureConnection(HttpURLConnection connection) throws IOException {
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
    }
    protected void connect(HttpURLConnection connection) throws IOException {
        connection.connect();
    }
}
class ApiPostFuction extends  ApiFuction{
    protected final String[]  names;
    public ApiPostFuction(String ApiFunction,String[] names) {
        super(ApiFunction);
        this.names = names;
    }
    private String data;
    protected void configureConnection(HttpURLConnection connection) throws IOException {
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestProperty("Content-Length", "" + Integer.toString(data.getBytes().length));
    }
    protected void connect(HttpURLConnection connection) throws IOException {
        DataOutputStream out  = new DataOutputStream(connection.getOutputStream ());
        out.writeBytes(data);
        out.flush();
        out.close();
    }
    public void setData(JSONObject data){
        this.data = data.toString();

    }
    public void setData(JSONArray data) throws JSONException {
        JSONArray array = new JSONArray();
        array.put(Arrays.copyOf(names,data.length()));
        if(data.length()!= names.length){
            this.data = data.toJSONObject(array).toString();
        }
        array.put(names);
        this.data = data.toJSONObject(array).toString();
    }
    public void setData(String[] data) throws JSONException {
        JSONObject res = new JSONObject();
        for(int i = 0;data.length<i;i++){
            if(data[i]==null||data[i].isEmpty())continue;
            res. put(names[i], data[i]);
        }
        this.data = res.toString();
    }
}