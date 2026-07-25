package com.q4magic.authId.Proofy;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Proofy {
    public static Map<String, Object> validate(String proofyBaseUrl, String proofyApiKey, String proofyAId, String email) {
        Map<String, Object> resBody = new HashMap<>();
        resBody.put("status", false);
        try {
            HttpResponse<String> response = Unirest.get(proofyBaseUrl + "verifyaddr?aid=" + proofyAId + "&key=" + proofyApiKey + "&email=" + email)
                    .asString();
            if(response.getStatus() == 200) {
                if(response.getBody() == null) {
                    resBody.put("status", false);
                } else {
                    JSONObject data = new JSONObject(response.getBody());
                    resBody = getResult(proofyBaseUrl, proofyApiKey, proofyAId, data.getString("cid"));
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

    public static Map<String, Object> getResult(String proofyBaseUrl, String proofyApiKey, String proofyAId, String cid) {
        Map<String, Object> resBody = new HashMap<>();
        resBody.put("status", false);
        try {
            HttpResponse<String> response = Unirest.get(proofyBaseUrl + "getresult?aid=" + proofyAId + "&key=" + proofyApiKey + "&cid=" + cid)
                    .asString();
            if(response.getStatus() == 200) {
                if(response.getBody() == null) {
                    resBody.put("status", false);
                } else {
                    JSONObject data = new JSONObject(response.getBody());
                    JSONArray jsonArray = data.getJSONArray("result");
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    String emailStatus = jsonObject.getString("statusName");
                    if(emailStatus.equalsIgnoreCase("deliverable")
                            || emailStatus.equalsIgnoreCase("risky")) {
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
