package com.q4magic.businessInfo.service;

import com.q4magic.common.dto.BusinessInfoDto;

public interface BusinessInfoService {
    BusinessInfoDto getBusinessInfoById(Integer id);

    BusinessInfoDto createBusinessInfo(BusinessInfoDto businessInfoDto);

    BusinessInfoDto updateBusinessInfo(Integer id, BusinessInfoDto businessInfoDto);

    void deleteBusinessInfo(Integer id);

    String uploadBrandLogo(Integer brandId, String imagePath);

    boolean deleteBrandLogo(Integer brandId);
}
