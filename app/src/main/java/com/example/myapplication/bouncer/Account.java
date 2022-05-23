package com.example.myapplication.bouncer;

import com.example.myapplication.MyAppException;

import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class Account {
    private final UrlAccess urlAccess;
    private boolean accessGranted;

    public Account(UrlAccess urlAccess) {
        this.urlAccess = urlAccess;
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
        HashMap<String, String> headers = new HashMap<>();
        for (Map.Entry<String, String> header : urlAccess.getHeaders().entrySet()) {
            headers.put(header.getKey(), header.getValue());
        }

        long epoch = System.currentTimeMillis();
        return headers;
//        epoc = int(time.time())
//        signature = this.account.sign("{}{}{}".format(this.account.address, this.guid, epoc))
//        headers = this.url_access.original_headers
//        headers['bouncerAccount'] = this.account.address
//        headers['bouncerGuid'] = this.guid
//        headers['bouncerTime'] = str(epoc)
//        headers['bouncerSignature'] = signature
//        headers['bouncerInst'] = 'true'
//        return headers

    }

    private void sendNextAction() {
    }

    private void sendCheck() {
        HashMap<String, String> body = new HashMap<>();
        this.postToAdmission("check", body);
    }

    private void postToAdmission(String urlSuffix, HashMap<String, String> body) {
        body.put("guid", this.guid);
        body['host'] = this.url_access.original_host
        body['url'] = this.url_access.original_path
        body['account'] = this.account.address

        admission_host = this.url_access.original_host
        if 'ADMISSION_URL' in os.environ:
        admission_host = os.environ['ADMISSION_URL']

        bouncer_url = '{}/bouncer/admission/{}'.format(admission_host, url_suffix)
        debug('client post: {} with body {}'.format(bouncer_url, body))
        response = requests.post(bouncer_url, json=body, verify=False)
        if response.status_code != 200:
        raise Exception('post to {} failed: {}: {}'.format(bouncer_url, response.status_code, response.text))

        this.server_response = response.json()
        debug('server response: {}'.format(this.server_response))
        if 'directAccess' in this.server_response and this.server_response['directAccess'] == 'True':
        this.access_granted = True
    }
}
