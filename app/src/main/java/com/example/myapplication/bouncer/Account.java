package com.example.myapplication.bouncer;

import android.content.Context;

import com.example.myapplication.MyAppException;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class Account {
    private final UrlAccess urlAccess;
    private final EncryptionKey accountKey;
    private final String guid;
    private boolean accessGranted;
    private HashMap<String, String> admissionResponse;


    public Account(UrlAccess urlAccess) {
        this.urlAccess = urlAccess;
        this.accountKey = this.getAccountKey();
        this.guid = this.generateGuid();
    }

    private String generateGuid() {
        String data = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        data = data.replaceAll("-", "");
        return data.substring(0, 64);
    }

    private EncryptionKey getAccountKey() {
        try {

            Context context = Bouncer.getInstance().getContext();
            File file = new File(context.getFilesDir(), "bouncer_account");
            if (file.exists()) {
                FileInputStream in = new FileInputStream(file);
                String exported = InputReader.readToString(in);
                return new EncryptionKey(exported);
            }

            EncryptionKey key = new EncryptionKey();
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write(key.exportKeyPair().getBytes(StandardCharsets.UTF_8));
            }
            return key;
        } catch (Exception e) {
            throw new BouncerException(e);
        }
    }


    public ServerResponse sendGet() {
        this.authorize();
        return this.sendRequest("GET", null);
    }


    public ServerResponse sendPost(String body) {
        this.authorize();
        return this.sendRequest("POST", body);
    }

    private ServerResponse sendRequest(String method, String body) {
        try {
            URL url = new URL(this.urlAccess.getUrl());
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            for (Map.Entry<String, String> entry : this.getUpdatedHeaders().entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            if (body != null) {
                connection.setDoOutput(true);
                try (OutputStream out = connection.getOutputStream()) {
                    out.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }
            if (connection.getResponseCode() != 200) {
                throw new MyAppException("response code " + connection.getResponseCode());
            }

            HashMap<String, String> responseHeaders = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
                responseHeaders.put(entry.getKey(), entry.getValue().get(0));
            }
            String response = InputReader.readToString(connection.getInputStream());
            connection.disconnect();
            return new ServerResponse(response, responseHeaders);
        } catch (Exception e) {
            throw new BouncerException(e);
        }
    }

    private void authorize() {
        this.sendCheck();
        while (!this.accessGranted) {
            this.sendNextAction();
        }
    }

    private HashMap<String, String> getUpdatedHeaders() {
        HashMap<String, String> headers = new HashMap<>(urlAccess.getHeaders());
        long epoch = System.currentTimeMillis() / 1000;
        String signText = this.accountKey.getAddress() + this.guid + epoch;
        String signature = this.accountKey.signAsHexString(signText);
        headers.put("bouncerAccount", this.accountKey.getAddress());
        headers.put("bouncerGuid", this.guid);
        headers.put("bouncerTime", Long.toString(epoch));
        headers.put("bouncerSignature", signature);
        headers.put("bouncerInst", "true");
        return headers;
    }

    private void sendCheck() {
        HashMap<String, String> body = new HashMap<>();
        this.postToAdmission("check", body);
    }

    private void postToAdmission(String urlSuffix, HashMap<String, String> body) {
        body.put("guid", this.guid);
        body.put("host", this.urlAccess.getOriginalHost());
        body.put("url", this.urlAccess.getOriginalPath());
        body.put("'account'", this.accountKey.getAddress());

        String bouncerUrl = Bouncer.getInstance().getAdmissionUrl() + "/bouncer/admission/" + urlSuffix;
        this.admissionResponse = this.sendRequestToAdmission(bouncerUrl, body);
        String directAccess = this.admissionResponse.get("directAccess");
        if (Objects.equals(directAccess, "True")) {
            this.accessGranted = true;
        }
    }

    protected HashMap<String, String> sendRequestToAdmission(String accessUrl, HashMap<String, String> bodyMap) {
        try {
            URL url = new URL(accessUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            JSONObject bodyJson = new JSONObject(bodyMap);
            String bodyString = bodyJson.toString();
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = bodyString.getBytes(StandardCharsets.UTF_8);
                outputStream.write(input);
            }
            if (connection.getResponseCode() != 200) {
                throw new MyAppException("admission response code " + connection.getResponseCode());
            }

            JSONObject jsonObject = new JSONObject(InputReader.readToString(connection.getInputStream()));
            connection.disconnect();
            HashMap<String, String> responseMap = new HashMap<>();
            Iterator<String> keysIterator = jsonObject.keys();
            while (keysIterator.hasNext()) {
                String key = keysIterator.next();
                responseMap.put(key, jsonObject.getString(key));
            }
            return responseMap;
        } catch (Exception e) {
            throw new MyAppException(e);
        }
    }

    private void sendNextAction() {
    }
}
