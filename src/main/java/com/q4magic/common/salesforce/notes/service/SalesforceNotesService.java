package com.q4magic.common.salesforce.notes.service;

import java.util.Map;

public interface SalesforceNotesService {
    Map<String, Object> getAllNotesAndAttachments(String accessToken, String instanceUrl, String parentId);
}
