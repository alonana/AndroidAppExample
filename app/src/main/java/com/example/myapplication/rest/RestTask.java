package com.example.myapplication.rest;

import android.os.AsyncTask;

import com.example.myapplication.MyAppException;
import com.example.myapplication.bouncer.Bouncer;
import com.example.myapplication.bouncer.ServerResponse;

import org.json.JSONObject;

public class RestTask extends AsyncTask<Void, Void, Void> {
    private RestCallback callback;

    public RestTask(RestCallback callback) {
        this.callback = callback;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            String url = "http://www.onepro.com/buy_nb";
            ServerResponse response = Bouncer.getInstance().sendPost(url, null, null);
            String responseBody = response.getBody();
            JSONObject responseJson = new JSONObject(responseBody);
            long price = responseJson.getLong("price");
            this.callback.handleResponse(Long.toString(price));
        } catch (Exception e) {
            throw new MyAppException(e);
        }
        return null;
    }
}
