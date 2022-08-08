package edu.byu.cs240.familymap;


import java.io.*;
import java.net.*;
import java.util.Map;

import request.*;
import result.*;
import com.google.gson.Gson;

public class ServerProxy {
    private final String host;
    private final String port;

    public ServerProxy(String host, String port) {
        this.host = host;
        this.port = port;
    }

    public LoginResult Login(LoginRequest request) {
        try {
            Gson gson = new Gson();
            URL url = new URL("http://" + host + ":" + port + "/user/login");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();

            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.addRequestProperty("Accept", "application/json");
            http.connect();

            writeString(gson.toJson(request), http.getOutputStream());
            http.getOutputStream().close();

            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String respData = readString(http.getInputStream());
                return gson.fromJson(respData, LoginResult.class);

            } else {
                System.out.println("ERROR: " + http.getResponseMessage());
                System.out.println(readString(http.getErrorStream()));
                return new LoginResult(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new LoginResult(false);
        }
    }
    public RegisterResult Register(RegisterRequest request) {
        try {
            Gson gson = new Gson();
            URL url = new URL("http://" + host + ":" + port + "/user/register");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();

            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.addRequestProperty("Accept", "application/json");
            http.connect();

            // this is the right way to send in the request to the server
            writeString(gson.toJson(request), http.getOutputStream());
            http.getOutputStream().close();

            // response data should be a LoginResult object
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String respData = readString(http.getInputStream());
                return gson.fromJson(respData, RegisterResult.class);

            } else {
                System.out.println("ERROR: " + http.getResponseMessage());
                System.out.println(readString(http.getErrorStream()));
                return new RegisterResult(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new RegisterResult(false);
        }
    }
    public PersonResult Person(String personId) {
        try {
            Gson gson = new Gson();
            URL url = new URL("http://" + host + ":" + port + "/person/" + personId);
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("GET");
            http.setDoOutput(false);
            http.addRequestProperty("Authorization", DataCache.getInstance().auth);
            http.addRequestProperty("Accept", "application/json");
            http.connect();
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream respBody = http.getInputStream();
                String respData = readString(respBody);
                return gson.fromJson(respData, PersonResult.class);
            } else {
                System.out.println("ERROR: " + http.getResponseMessage());
                return new PersonResult(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new PersonResult(false);
        }
    }
    public PersonsResult Persons() {
        try {
            Gson gson = new Gson();
            URL url = new URL("http://" + host + ":" + port + "/person");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();

            http.setRequestMethod("GET");
            http.setDoOutput(false);
            http.addRequestProperty("Authorization", DataCache.getInstance().auth);
            http.addRequestProperty("Accept", "application/json");
            http.connect();

            // response data is a persons object
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream respBody = http.getInputStream();
                String respData = readString(respBody);

                return gson.fromJson(respData, PersonsResult.class);
            } else {
                InputStream respBody = http.getErrorStream();
                return new PersonsResult(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new PersonsResult(false);
        }
    }
    public EventsResult Events() {
        try {
            Gson gson = new Gson();
            URL url = new URL("http://" + host + ":" + port + "/event");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();

            http.setRequestMethod("GET");
            http.setDoOutput(false);
            http.addRequestProperty("Authorization", DataCache.getInstance().auth);
            http.addRequestProperty("Accept", "application/json");
            http.connect();

            // response data is an events object
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream respBody = http.getInputStream();
                String respData = readString(respBody);

                return gson.fromJson(respData, EventsResult.class);
            } else {
                System.out.println("ERROR: " + http.getResponseMessage());
                return new EventsResult();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new EventsResult();
        }
    }

    private static String readString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader sr = new InputStreamReader(is);
        char[] buf = new char[1024];
        int len;
        while ((len = sr.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        return sb.toString();
    }
    private static void writeString(String str, OutputStream os) throws IOException {
        OutputStreamWriter sw = new OutputStreamWriter(os);
        sw.write(str);
        sw.flush();
    }
}
