package com.q4magic.customerQuota.serviceImpl;

import com.q4magic.common.dto.CustomerQuotaDto;
import com.q4magic.common.models.CustomerQuota;
import com.q4magic.common.models.Customers;
import com.q4magic.common.repository.CustomerQuotaRepository;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.customerQuota.service.CustomerQuotaService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service(value = "CustomerQuotaService")
public class CustomerQuotaServiceImpl implements CustomerQuotaService {

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private CustomerQuotaRepository customerQuotaRepository;

    @Override
    public List<CustomerQuotaDto> getAllCustomerQuotas(Integer customerId) {
        try {
            List<CustomerQuota> customerQuotas = this.customerQuotaRepository.getALlCustomerQuota(customerId);
            List<CustomerQuotaDto> customerQuotaDtoList = new ArrayList<>();

            if (!customerQuotas.isEmpty()) {
                for (CustomerQuota customerQuota : customerQuotas){
                    customerQuotaDtoList.add(this.getCustomerQuotaById(customerQuota.getId()));
                }
            }
            return customerQuotaDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CustomerQuotaDto getCustomerQuotaById(Integer id) {
        try {
            CustomerQuota customerQuota = this.customerQuotaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("CustomerQuota not found with id: " + id));
            CustomerQuotaDto customerQuotaDto = new CustomerQuotaDto();
            if (customerQuota != null) {
                customerQuotaDto.setCustomerId(customerQuota.getCustomers().getId());
                BeanUtils.copyProperties(customerQuota, customerQuotaDto);
            }
            return customerQuotaDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CustomerQuotaDto createCustomerQuota(CustomerQuotaDto customerQuotaDto) {
        try {
            CustomerQuota customerQuota = new CustomerQuota();
            Customers customers = customersRepository.findById(customerQuotaDto.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerQuotaDto.getCustomerId()));
            customerQuota.setCustomers(customers);
            BeanUtils.copyProperties(customerQuotaDto, customerQuota, "id");
            this.customerQuotaRepository.save(customerQuota);
            return customerQuotaDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CustomerQuotaDto updateCustomerQuota(Integer id, CustomerQuotaDto customerQuotaDto) {
        try {
            CustomerQuota customerQuota = this.customerQuotaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("CustomerQuota not found with id: " + id));
//            Customers customers = customersRepository.findById(customerQuotaDto.getCustomerId())
//                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerQuotaDto.getCustomerId()));
//            customerQuota.setCustomers(customers);
            BeanUtils.copyProperties(customerQuotaDto, customerQuota, "id", "customers");
            this.customerQuotaRepository.save(customerQuota);
            return customerQuotaDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteCustomerQuota(Integer id) {
        try {
            CustomerQuota customerQuota = this.customerQuotaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("CustomerQuota not found with id: " + id));
            this.customerQuotaRepository.delete(customerQuota);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
