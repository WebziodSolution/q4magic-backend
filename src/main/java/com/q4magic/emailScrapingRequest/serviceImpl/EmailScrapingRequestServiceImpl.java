package com.q4magic.emailScrapingRequest.serviceImpl;

import com.q4magic.common.dto.EmailFetchRequestDto;
import com.q4magic.common.dto.EmailScrapingRequestsDto;
import com.q4magic.common.models.Customers;
import com.q4magic.common.models.EmailScrapingRequests;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.repository.EmailScrapingRequestsRepository;
import com.q4magic.emailScrapingRequest.service.EmailScrapingRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service(value = "EmailScrapingRequestService")
public class EmailScrapingRequestServiceImpl implements EmailScrapingRequestService {

    @Autowired
    private EmailScrapingRequestsRepository emailScrapingRequestsRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Override
    public List<EmailScrapingRequestsDto> getAllEmailScrapingRequests(Integer customerId) {
        try {
            List<EmailScrapingRequests> emailScrapingRequestsList = this.emailScrapingRequestsRepository.findMailsByCustomerId(customerId);
            List<EmailScrapingRequestsDto> emailScrapingRequestsDtoList = new ArrayList<>();
            if (!emailScrapingRequestsList.isEmpty()) {
                for (EmailScrapingRequests emailScrapingRequests : emailScrapingRequestsList) {
                    emailScrapingRequestsDtoList.add(this.getEmailScrapingRequests(emailScrapingRequests.getId()));
                }
            }
            return emailScrapingRequestsDtoList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public EmailScrapingRequestsDto getEmailScrapingRequests(Integer id) {
        try {
            EmailScrapingRequests emailScrapingRequests = this.emailScrapingRequestsRepository.findById(id).orElseThrow(() -> new RuntimeException("Email Scraping Request not found"));
            EmailScrapingRequestsDto emailScrapingRequestsDto = new EmailScrapingRequestsDto();
            emailScrapingRequestsDto.setId(emailScrapingRequests.getId());
            emailScrapingRequestsDto.setEmail(emailScrapingRequests.getEmail());
            emailScrapingRequestsDto.setPassword(emailScrapingRequests.getPassword());
            emailScrapingRequestsDto.setProtocol(emailScrapingRequests.getProtocol());
            emailScrapingRequestsDto.setImapHost(emailScrapingRequests.getImapHost());
            emailScrapingRequestsDto.setImapPort(emailScrapingRequests.getImapPort());
            emailScrapingRequestsDto.setMaxMessages(emailScrapingRequests.getMaxMessages());
            emailScrapingRequestsDto.setStatus(emailScrapingRequests.getStatus());
            emailScrapingRequestsDto.setCreatedBy(emailScrapingRequests.getCustomers().getId());
            return emailScrapingRequestsDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createEmailScrapingRequest(EmailScrapingRequestsDto emailFetchRequestDto) {
        try {
            Customers customers = this.customersRepository.findById(emailFetchRequestDto.getCreatedBy()).orElseThrow(() -> new RuntimeException("Customer not found"));
            EmailScrapingRequests emailScrapingRequests = new EmailScrapingRequests();
            emailScrapingRequests.setEmail(emailFetchRequestDto.getEmail());
            emailScrapingRequests.setPassword(emailFetchRequestDto.getPassword());
            emailScrapingRequests.setProtocol(emailFetchRequestDto.getProtocol());
            emailScrapingRequests.setImapHost(emailFetchRequestDto.getImapHost());
            emailScrapingRequests.setImapPort(emailFetchRequestDto.getImapPort());
            emailScrapingRequests.setMaxMessages(emailFetchRequestDto.getMaxMessages());
            emailScrapingRequests.setStatus(0);
            emailScrapingRequests.setCustomers(customers);
            emailScrapingRequests.setCreatedAt(new Date());
            this.emailScrapingRequestsRepository.save(emailScrapingRequests);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void changeScrapingRequestStatus(Integer id, Integer status) {
        try {
            EmailScrapingRequests emailScrapingRequests = this.emailScrapingRequestsRepository.findById(id).orElseThrow(() -> new RuntimeException("Email Scraping Request not found"));
            emailScrapingRequests.setStatus(status);
            this.emailScrapingRequestsRepository.save(emailScrapingRequests);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void changeScrapingRequestMessageCount(Integer id, Integer count) {
        try {
            EmailScrapingRequests emailScrapingRequests = this.emailScrapingRequestsRepository.findById(id).orElseThrow(() -> new RuntimeException("Email Scraping Request not found"));
            emailScrapingRequests.setMaxMessages(count);
            this.emailScrapingRequestsRepository.save(emailScrapingRequests);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
