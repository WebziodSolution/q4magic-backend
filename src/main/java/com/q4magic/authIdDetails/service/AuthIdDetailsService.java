package com.q4magic.authIdDetails.service;

import com.q4magic.common.dto.AuthIdDetailsDto;

import java.util.Map;

public interface AuthIdDetailsService {
    Map<String, Object> getAuthDetails(String email);

    Map<String, Object> createAuthDetails(AuthIdDetailsDto authIdDetailsDto);

    Map<String, Object> updateAuthDetails(AuthIdDetailsDto authIdDetailsDto);

    void deleteAuthDetails(Integer authId);

    Map<String, Object> login(String email);
}
