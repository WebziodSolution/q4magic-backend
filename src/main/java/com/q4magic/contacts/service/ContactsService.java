package com.q4magic.contacts.service;

import com.q4magic.common.dto.ContactsDto;

import java.util.List;
import java.util.Map;

public interface ContactsService {
    List<ContactsDto> searchContacts(Integer userId, String value);

    List<ContactsDto> getAllContacts(Integer userId, String fetchType);

    ContactsDto getContactById(Integer id);

    ContactsDto createContact(ContactsDto contactDto, Boolean syncToSalesforce);

    ContactsDto updateContact(Integer id, ContactsDto contactDto, Boolean syncToSalesforce);

    void deleteContact(Integer id, Boolean syncToSalesforce);

    void addContacts(Integer userId, List<Integer> tempMailIds);

    Map<String, Object> reportHierarch(Integer contactId);

    void addMultipleContacts(Integer userId, List<Map<String, Object>> contactList);
}
