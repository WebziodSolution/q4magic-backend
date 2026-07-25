package com.q4magic.country.serviceImpl;

import com.q4magic.common.dto.country.CountryDto;
import com.q4magic.common.models.country.Country;
import com.q4magic.common.repository.CountryRepository;
import com.q4magic.country.service.CountryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service(value = "countryService")
public class CountryServiceImpl implements CountryService {
    @Autowired
    private CountryRepository countryRepository;

    @Override
    public List<CountryDto> getAllCountry() {
        try {
            List<Country> countries = this.countryRepository.findAll();
            List<CountryDto> countryDtos = new ArrayList<>();
            for (Country country : countries) {
                countryDtos.add(this.getCountry(country.getId()));
            }
            return countryDtos;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CountryDto getCountry(int id) {
        try {
            Country country = this.countryRepository.findById(id).orElseThrow(() -> new RuntimeException("Country not found"));
            CountryDto conCountryDto = new CountryDto();
            BeanUtils.copyProperties(country, conCountryDto);
            return conCountryDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
