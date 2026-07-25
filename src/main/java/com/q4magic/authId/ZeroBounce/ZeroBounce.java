package com.q4magic.authId.ZeroBounce;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ZeroBounce {
    public static Map<String, Object> validate(String zeroBounceBaseUrl, String zeroBounceApiKey, String email) {
        Map<String, Object> resBody = new HashMap<>();
        resBody.put("status", false);
        try {
            HttpResponse<String> response = Unirest.get(zeroBounceBaseUrl + "validate?api_key=" + zeroBounceApiKey + "&email=" + email)
                    .asString();
            if(response.getStatus() == 200) {
                if(response.getBody() == null) {
                    resBody.put("status", false);
                } else {
                    JSONObject data = new JSONObject(response.getBody());
                    if(data.getString("status").equals("valid")) {
                        resBody.put("status", true);
                    } else {
                        resBody.put("status", false);
                    }
                }
            } else {
                resBody.put("error", response.getStatusText());
            }
        } catch (Exception e) {
            e.printStackTrace();
            resBody.put("error", e.getMessage());
        }
        return resBody;
    }
}
