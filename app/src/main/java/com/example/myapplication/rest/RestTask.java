package com.example.myapplication.rest;

import android.os.AsyncTask;

import com.example.myapplication.MyAppException;
import com.example.myapplication.bouncer.InputReader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class RestTask extends AsyncTask<Void, Void, Void> {
    private RestCallback callback;

    public RestTask(RestCallback callback) {
        this.callback = callback;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL url = new URL("https://gorest.co.in/public/v2/users");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() != 200) {
                throw new MyAppException("response code " + connection.getResponseCode());
            }

            List<String> users = new LinkedList<>();
            JSONArray usersJson = new JSONArray(InputReader.readToString(connection.getInputStream()));
            for (int i = 0; i < usersJson.length(); i++) {
                JSONObject user = usersJson.getJSONObject(i);
                String name = user.getString("name");
                users.add(name);
            }
            connection.disconnect();
            this.callback.handleResponse(users.toString());
        } catch (Exception e) {
            throw new MyAppException(e);
        }
        return null;
    }
}
