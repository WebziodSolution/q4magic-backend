package com.q4magic.emailScrapingRequest.service;

import com.q4magic.common.dto.EmailScrapingRequestsDto;

import java.util.List;

public interface EmailScrapingRequestService {
    List<EmailScrapingRequestsDto> getAllEmailScrapingRequests(Integer customerId);

    EmailScrapingRequestsDto getEmailScrapingRequests(Integer id);

    void createEmailScrapingRequest(EmailScrapingRequestsDto emailFetchRequestDto);

    void changeScrapingRequestStatus(Integer id, Integer status);

    void changeScrapingRequestMessageCount(Integer id, Integer count);

}
