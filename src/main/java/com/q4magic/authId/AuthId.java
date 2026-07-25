package com.q4magic.authId;

import com.q4magic.common.dto.AuthIdDetailsDto;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AuthId {
    public static Map<String, Object> authIdToken(String authIdExternalId, String authIdApiKeyValue) {
        Map<String, Object> resBody = new HashMap<>();
        resBody.put("error", "");
        resBody.put("accessToken", "");
        resBody.put("refreshToken", "");
        try {
            HttpResponse<String> response = Unirest.post("https://id.authid.ai/IDCompleteBackendEngine/IdentityService/v1/auth/token")
                    .basicAuth(authIdExternalId, authIdApiKeyValue)
                    .asString();
            if (response.getStatus() == 200) {
                JSONObject data = new JSONObject(response.getBody());
                resBody.put("accessToken", data.getString("AccessToken"));
                resBody.put("refreshToken", data.getString("RefreshToken"));
            } else {
                resBody.put("error", response.getStatusText());
            }
        } catch (Exception e) {
            e.printStackTrace();
            resBody.put("error", e.getMessage());
        }
        return resBody;
    }

    public static Map<String, Object> authIdCreateAccount(String authIdExternalId, String authIdApiKeyValue, AuthIdDetailsDto authIdDetailsDto, String accNo) {
        Map<String, Object> resBody = new HashMap<>();
        resBody = AuthId.authIdToken(authIdExternalId, authIdApiKeyValue);
        resBody.put("accountNumber", "");
        try {
            String email = authIdDetailsDto.getEmail();
            String phoneNumber = "";
            HttpResponse<String> response = Unirest.post("https://id.authid.ai/IDCompleteBackendEngine/Default/AdministrationServiceRest/v1/accounts")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + resBody.get("accessToken"))
                    .body("{" +
                            "\"AccountNumber\": \"" + accNo + "\"," +
                            "\"Version\": 0," +
                            "\"DisplayName\": \"" + email + "\"," +
                            "\"CustomDisplayName\": \"" + email + "\"," +
                            "\"Description\": \"\"," +
                            "\"Enabled\": true," +
                            "\"Custom\": true," +
                            "\"Email\": \"" + email + "\"," +
                            "\"PhoneNumber\": \"" + phoneNumber + "\"" +
                            "}")
                    .asString();

            if (response.getStatus() == 200) {
                JSONObject data = new JSONObject(response.getBody());
                resBody.put("accountNumber", data.getString("AccountNumber"));
            } else {
                resBody.put("error", response.getStatusText());
            }
        } catch (Exception e) {
            e.printStackTrace();
            resBody.put("error", e.getMessage());
        }
        return resBody;
    }

    public static Map<String, Object> authIdGetAccount(String authIdExternalId, String authIdApiKeyValue, String accountNumber) {
        Map<String, Object> resBody = new HashMap<>();
        resBody = AuthId.authIdToken(authIdExternalId, authIdApiKeyValue);
        resBody.put("accountNumber", "");
        try {
            HttpResponse<String> response = Unirest.get("https://id.authid.ai/IDCompleteBackendEngine/Default/AdministrationServiceRest/v1/accounts/" + accountNumber)
                    .header("Authorization", "Bearer " + resBody.get("accessToken"))
                    .asString();
            if (response.getStatus() == 200) {
                if (response.getBody() == null) {
                    resBody.put("accountNumber", "");
                } else {
                    JSONObject data = new JSONObject(response.getBody());
                    resBody.put("accountNumber", data.getString("AccountNumber"));
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

    public static Map<String, Object> authIdGetForeignIDDocument(String authIdExternalId, String authIdApiKeyValue, String accountNumber, String documentType) {
        Map<String, Object> resBody = new HashMap<>();
        resBody = AuthId.authIdToken(authIdExternalId, authIdApiKeyValue);
        try {
            HttpResponse<String> response = Unirest.post("https://id.authid.ai/IDCompleteBackendEngine/Default/AuthorizationServiceRest/v2/operations")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + resBody.get("accessToken"))
                    .body("{" +
                            " \"AccountNumber\": \"" + accountNumber + "\"," +
                            "\"Payload\": {" +
                            "\"DocumentTypes\": [" +
                            " \"" + documentType + "\"" +
                            "]" +
                            "}," +
                            "\"Name\": \"GetForeignIDDocument\"," +
                            "\"Timeout\": 3600," +
                            "\"TransportType\": 0" +
                            "}")
                    .asString();

            if (response.getStatus() == 200) {
                if (response.getBody() == null) {
                    resBody.put("operationId", "");
                    resBody.put("oneTimeSecret", "");
                } else {
                    JSONObject data = new JSONObject(response.getBody());
                    resBody.put("operationId", data.getString("OperationId"));
                    resBody.put("oneTimeSecret", data.getString("OneTimeSecret"));
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

    public static Map<String, Object> authIdEnrollBioCredential(String authIdExternalId, String authIdApiKeyValue, String accountNumber) {
        Map<String, Object> resBody = new HashMap<>();
        resBody = AuthId.authIdToken(authIdExternalId, authIdApiKeyValue);
        try {
            HttpResponse<String> response = Unirest.post("https://id.authid.ai/IDCompleteBackendEngine/Default/AuthorizationServiceRest/v2/operations")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + resBody.get("accessToken"))
                    .body("{" +
                            " \"AccountNumber\": \"" + accountNumber + "\"," +
                            "\"Codeword\": \"\"," +
                            "\"Tag\": \"\"," +
                            "\"Name\": \"EnrollBioCredential\"," +
                            "\"Timeout\": 3600," +
                            "\"TransportType\": 0" +
                            "}")
                    .asString();

            if (response.getStatus() == 200) {
                if (response.getBody() == null) {
                    resBody.put("operationId", "");
                    resBody.put("oneTimeSecret", "");
                } else {
                    JSONObject data = new JSONObject(response.getBody());
                    resBody.put("operationId", data.getString("OperationId"));
                    resBody.put("oneTimeSecret", data.getString("OneTimeSecret"));
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

    public static Map<String, Object> authIdVerifyIdentity(String authIdExternalId, String authIdApiKeyValue, String accountNumber) {
        Map<String, Object> resBody = new HashMap<>();
        resBody = AuthId.authIdToken(authIdExternalId, authIdApiKeyValue);
        try {
            HttpResponse<String> response = Unirest.post("https://id.authid.ai/IDCompleteBackendEngine/Default/AuthorizationServiceRest/v2/transactions")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + resBody.get("accessToken"))
                    .body("{" +
                            "\"AccountNumber\": \"" + accountNumber + "\"," +
                            "\"ConfirmationPolicy\": {" +
                            "\"TransportType\": 0," +
                            "\"CredentialType\": 1," +
                            "\"BioPolicy\": {" +
                            "\"CheckLiveness\": \"true\"" +
                            "}" +
                            "}," +
                            "\"Name\": \"Verify_Identity\"," +
                            "\"Timeout\": 3600" +
                            "}")
                    .asString();
            if (response.getStatus() == 200) {
                if (response.getBody() == null) {
                    resBody.put("transactionId", "");
                    resBody.put("oneTimeSecret", "");
                } else {
                    JSONObject data = new JSONObject(response.getBody());
                    resBody.put("transactionId", data.getString("TransactionId"));
                    resBody.put("oneTimeSecret", data.getString("OneTimeSecret"));
                }
            } else {
                if (response.getStatusText().equalsIgnoreCase("Conflict")) {
                    resBody.put("error", "Biometric not found for this Email Account");
                } else {
                    JSONObject data = new JSONObject(response.getBody());
                    resBody.put("error", data.getString("Message"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resBody.put("error", e.getMessage());
        }
        return resBody;
    }

    public static Map<String, Object> authIdGetProofResults(String authIdExternalId, String authIdApiKeyValue, String authidOperationId) {
        Map<String, Object> resBody = new HashMap<>();
        resBody = AuthId.authIdToken(authIdExternalId, authIdApiKeyValue);
        resBody.put("accountNumber", "");
        resBody.put("verified", "");
        try {
            HttpResponse<String> response = Unirest.get("https://id.authid.ai/IDCompleteBackendEngine/Default/AuthorizationServiceRest/v2/operations/" + authidOperationId + "/result")
                    .header("Authorization", "Bearer " + resBody.get("accessToken"))
                    .asString();
            if (response.getStatus() == 200) {
                if (response.getBody() == null) {
                    resBody.put("error", "error");
                } else {
                    JSONObject data = new JSONObject(response.getBody());
                    JSONObject payload = data.getJSONObject("Payload");
                    JSONObject metadata = payload.getJSONObject("Metadata");
                    JSONObject biometricVerificationResult = metadata.getJSONObject("BiometricVerificationResult");
                    resBody.put("verified", biometricVerificationResult.getBoolean("Verified"));
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

    public static Map<String, Object> authIdGetProofResultsAllData(String authIdExternalId, String authIdApiKeyValue, String authidOperationId) {
        Map<String, Object> resBody = new HashMap<>();
        Map<String, Object> innerResBody = new HashMap<>();
        resBody = AuthId.authIdToken(authIdExternalId, authIdApiKeyValue);
        try {
            HttpResponse<String> response = Unirest.get("https://id.authid.ai/IDCompleteBackendEngine/Default/AuthorizationServiceRest/v2/operations/" + authidOperationId + "/result")
                    .header("Authorization", "Bearer " + resBody.get("accessToken"))
                    .asString();
            if (response.getStatus() == 200) {
                if (response.getBody() == null) {
                    resBody.put("error", "error");
                } else {
                    JSONObject data = new JSONObject(response.getBody());
                    JSONObject payload = data.getJSONObject("Payload");
                    if (!payload.isNull("Data")){
                        JSONObject innerData = payload.getJSONObject("Data");
                        JSONObject innerDocument = innerData.getJSONObject("Document");
                        JSONArray innerDocumentDataArray = innerDocument.getJSONArray("Data");

                        innerResBody.put("documentType", innerDocument.getString("Type"));
                        for (int i = 0; i < innerDocumentDataArray.length(); i++) {
                            JSONObject finalData = (JSONObject) innerDocumentDataArray.get(i);
                            innerResBody.put(finalData.getString("Key"), finalData.getString("Value"));
                        }
                        resBody.put("userInfo", innerResBody);
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

    public static Map<String, Object> authIdDeleteAccount(String authIdExternalId, String authIdApiKeyValue, String accountNumber) {
        Map<String, Object> resBody = new HashMap<>();
        resBody.put("msg", "");
        resBody = AuthId.authIdToken(authIdExternalId, authIdApiKeyValue);
        try {
            HttpResponse<String> response = Unirest.delete("https://id.authid.ai/IDCompleteBackendEngine/Default/AdministrationServiceRest/v1/accounts/" + accountNumber)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + resBody.get("accessToken"))
                    .asString();
            if (response.getStatus() == 200) {
                resBody.put("msg", "done");
                resBody.put("error", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resBody.put("error", e.getMessage());
        }
        return resBody;
    }

    public static Map<String, Object> authIdGetProofTempId(String authIdExternalId, String authIdApiKeyValue, String authidAccountNumber, String authidOperationId) {
        Map<String, Object> resBody = new HashMap<>();
        resBody = AuthId.authIdToken(authIdExternalId, authIdApiKeyValue);
        resBody.put("accountNumber", "");
        resBody.put("tempId", "");
        try {
            HttpResponse<String> response = Unirest.get("https://id.authid.ai/IDCompleteBackendEngine/Default/AdministrationServiceRest/v1/foreignOperations/documents/" + authidOperationId)
                    .header("Authorization", "Bearer " + resBody.get("accessToken"))
                    .asString();
            if (response.getStatus() == 200) {
                if (response.getBody() == null) {
                    resBody.put("error", "error");
                } else {
                    JSONObject data = new JSONObject(response.getBody());
                    String tempId = data.getString("TempId");
                    resBody = authIdSetProofedBiometricCredential(authIdExternalId, authIdApiKeyValue, authidAccountNumber, tempId);
                    resBody.put("tempId", tempId);
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

    public static Map<String, Object> authIdSetProofedBiometricCredential(String authIdExternalId, String authIdApiKeyValue, String authidAccountNumber, String proofTempId) {
        Map<String, Object> resBody = new HashMap<>();
        resBody = AuthId.authIdToken(authIdExternalId, authIdApiKeyValue);
        try {
            HttpResponse<String> response = Unirest.post("https://id.authid.ai/IDCompleteBackendEngine/Default/AdministrationServiceRest/v1/accounts/" + authidAccountNumber + "/proofedBioCredential")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + resBody.get("accessToken"))
                    .body("{ \"TempId\": \"" + proofTempId + "\"}")
                    .asString();
            if (response.getStatus() != 200) {
                resBody.put("error", response.getStatusText());
            }
        } catch (Exception e) {
            e.printStackTrace();
            resBody.put("error", e.getMessage());
        }
        return resBody;
    }
}
