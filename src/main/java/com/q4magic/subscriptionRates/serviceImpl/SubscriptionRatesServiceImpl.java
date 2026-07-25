package com.q4magic.subscriptionRates.serviceImpl;

import com.q4magic.common.dto.SubscriptionRatesDto;
import com.q4magic.common.models.SubscriptionRates;
import com.q4magic.common.repository.SubscriptionRatesRepository;
import com.q4magic.subscriptionRates.service.SubscriptionRatesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service(value = "SubscriptionRatesService")
public class SubscriptionRatesServiceImpl implements SubscriptionRatesService {

    @Autowired
    private SubscriptionRatesRepository subscriptionRatesRepository;

    @Override
    public List<SubscriptionRatesDto> getAllSubscriptionRates() {
        try {
            List<SubscriptionRates> subscriptionRatesList = this.subscriptionRatesRepository.findAll();
            List<SubscriptionRatesDto> subscriptionRatesDtoList = new ArrayList<>();
            if(!subscriptionRatesList.isEmpty()){
                for (SubscriptionRates subscriptionRates : subscriptionRatesList) {
                    SubscriptionRatesDto subscriptionRatesDto = new SubscriptionRatesDto();
                    subscriptionRatesDto.setId(subscriptionRates.getId());
                    subscriptionRatesDto.setAmount(subscriptionRates.getAmount());
                    subscriptionRatesDto.setLicenseType(subscriptionRates.getLicenseType());
                    subscriptionRatesDto.setBeginDate(subscriptionRates.getBeginDate());
                    subscriptionRatesDto.setEndDate(subscriptionRates.getEndDate());
                    subscriptionRatesDto.setSubscriptionRatesCol(subscriptionRates.getSubscriptionRatesCol());
                    subscriptionRatesDtoList.add(subscriptionRatesDto);
                }
            }
            return subscriptionRatesDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
