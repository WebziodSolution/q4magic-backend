package com.q4magic.customerQuota.service;

import com.q4magic.common.dto.CustomerQuotaDto;

import java.util.List;


public interface CustomerQuotaService {
    List<CustomerQuotaDto> getAllCustomerQuotas(Integer customerId);

    CustomerQuotaDto getCustomerQuotaById(Integer id);

    CustomerQuotaDto createCustomerQuota(CustomerQuotaDto customerQuotaDto);

    CustomerQuotaDto updateCustomerQuota(Integer id, CustomerQuotaDto customerQuotaDto);

    void deleteCustomerQuota(Integer id);
}
