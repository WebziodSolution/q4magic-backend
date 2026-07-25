package com.q4magic.common.salesforce.contacts.service;

import java.util.Map;

public interface SalesforceContactsService {
    Map<String, Object> getAllContacts(String accessToken, String instanceUrl);

    Map<String, Object> getContactDetails(String contactId, String accessToken, String instanceUrl);

    Map<String, Object> createContact(Map<String, Object> contactData, String accessToken, String instanceUrl);

    Map<String, Object> updateContact(String contactId, Map<String, Object> contactData, String accessToken, String instanceUrl);

    Map<String, Object> deleteContact(String contactId, String accessToken, String instanceUrl);
}
