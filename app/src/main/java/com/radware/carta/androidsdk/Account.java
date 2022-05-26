package com.radware.carta.androidsdk;

import android.content.Context;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Account {
    private final UrlAccess urlAccess;
    private final EncryptionKey accountKey;
    private final String guid;
    private boolean accessGranted;
    private JSONObject admissionResponse;


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
            BouncerLogger.debug("send original url " + method + " " + this.urlAccess.getUrl());
            URL url = new URL(this.urlAccess.getUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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
                throw new BouncerException("response code " + connection.getResponseCode());
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
        HashMap<String, String> headers = new HashMap<>();
        if (urlAccess.getHeaders() != null) {
            headers.putAll(urlAccess.getHeaders());
        }
        long epoch = System.currentTimeMillis() / 1000;
        String signText = this.accountKey.getAddress() + this.guid + epoch;
        String signature = this.accountKey.sign(signText);
        headers.put("bouncerAccount", this.accountKey.getAddress());
        headers.put("bouncerGuid", this.guid);
        headers.put("bouncerTime", Long.toString(epoch));
        headers.put("bouncerSignature", signature);
        headers.put("bouncerInst", "true");
        return headers;
    }

    private void sendCheck() {
        HashMap<String, Object> body = new HashMap<>();
        this.sendToAdmissionWrapper("check", body);
    }

    private void sendToAdmissionWrapper(String urlSuffix, HashMap<String, Object> body) {
        try {
            body.put("guid", this.guid);
            body.put("host", this.urlAccess.getOriginalHost());
            body.put("url", this.urlAccess.getOriginalPath());
            body.put("account", this.accountKey.getAddress());

            String bouncerUrl = Bouncer.getInstance().getAdmissionUrl() + "/bouncer/admission/" + urlSuffix;
            this.admissionResponse = this.sendToAdmission(bouncerUrl, body);
            if (this.admissionResponse.has("directAccess") &&
                    this.admissionResponse.getBoolean("directAccess")) {
                this.accessGranted = true;
                BouncerLogger.debug("access granted");
            }
        } catch (Exception e) {
            throw new BouncerException(e);
        }
    }

    protected JSONObject sendToAdmission(String accessUrl, HashMap<String, Object> bodyMap) {
        BouncerLogger.debug("send to url " + accessUrl + " body " + bodyMap);
        try {
            URL url = new URL(accessUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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
                throw new BouncerException("admission error " +
                        connection.getResponseCode() + ":" +
                        InputReader.readToString(connection.getErrorStream()));
            }

            String responseJson = InputReader.readToString(connection.getInputStream());
            BouncerLogger.debug("response from admission: " + responseJson);
            JSONObject jsonObject = new JSONObject(responseJson);
            connection.disconnect();
            return jsonObject;
        } catch (Exception e) {
            throw new BouncerException(e);
        }
    }

    private void sendNextAction() {
        try {
            String nextAction = this.admissionResponse.getString("method");
            BouncerLogger.debug("next action: " + nextAction);
            switch (nextAction) {
                case "AddAccount":
                    this.actionWithChallenge("add-account");
                    break;
                case "AddToken":
                    this.actionWithChallenge("add-token");
                    break;
                case "AddEther":
                    this.actionWithChallenge("add-ether");
                    break;
                case "PayToken":
                    this.actionPayToken();
                    break;
                default:
                    throw new BouncerException("unknown next action method " + nextAction);
            }
        } catch (Exception e) {
            throw new BouncerException(e);
        }
    }

    private void actionPayToken() {
        try {
            if (this.payDebt()) {
                return;
            }


            this.payTokenOnce(this.guid, this.admissionResponse.getInt("urlTokens"), this.urlAccess.getOriginalPath(), false);
            BouncerLogger.debug("access granted after token payment");
            this.accessGranted = true;
        } catch (Exception e) {
            throw new BouncerException(e);
        }
    }

    private boolean payDebt() {
        try {
            if (!this.admissionResponse.has("debt")) {
                return false;
            }

            JSONObject debt = this.admissionResponse.getJSONObject("debt");
            Iterator<String> debtIterator = debt.keys();
            while (debtIterator.hasNext()) {
                String debtGuid = debtIterator.next();
                JSONObject debtDetails = debt.getJSONObject(debtGuid);
                int price = debtDetails.getInt("price");
                String url = debtDetails.getString("url");
                this.payTokenOnce(debtGuid, price, url, true);
            }

            return false;
        } catch (Exception e) {
            throw new BouncerException(e);
        }
    }

    private void payTokenOnce(String guid, int urlTokens, String url, boolean isDebt) {
        BouncerLogger.debug("pay token debt=" + isDebt);
        String signText = guid + Boolean.toString(isDebt).toLowerCase();
        String signature = this.accountKey.sign(signText);
        HashMap<String, Object> body = new HashMap<>();
        body.put("tokens", urlTokens);
        body.put("url", url);
        body.put("guid", guid);
        body.put("debt", isDebt);
        body.put("signature", signature);
        this.sendToAdmissionWrapper("pay-token", body);
    }

    private void actionWithChallenge(String method) {
        try {
            String challenge = this.admissionResponse.getString("challenge");
            int difficulty = this.admissionResponse.getInt("difficulty");
            ChallengeSolver solver = new ChallengeSolver(challenge, difficulty);
            HashMap<String, Object> body = solver.solve();
            this.sendToAdmissionWrapper(method, body);
        } catch (Exception e) {
            throw new BouncerException(e);
        }
    }
}
