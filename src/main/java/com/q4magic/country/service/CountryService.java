package com.q4magic.country.service;


import com.q4magic.common.dto.country.CountryDto;

import java.util.List;

public interface CountryService {
    List<CountryDto> getAllCountry();
    CountryDto getCountry(int id);
}
