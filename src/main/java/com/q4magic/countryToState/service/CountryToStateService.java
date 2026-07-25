package com.q4magic.countryToState.service;


import com.q4magic.common.dto.countryToState.CountryToStateDto;

import java.util.List;

public interface CountryToStateService {
    List<CountryToStateDto> getAllState();
    List<CountryToStateDto> getAllStateByCountry(int id);
}
