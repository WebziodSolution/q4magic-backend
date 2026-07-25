package com.q4magic.businessInfo.serviceImpl;

import com.q4magic.businessInfo.service.BusinessInfoService;
import com.q4magic.common.dto.BusinessInfoDto;
import com.q4magic.common.models.BusinessInfo;
import com.q4magic.common.models.Customers;
import com.q4magic.common.repository.BusinessInfoRepository;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.service.CommonService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service(value = "BusinessInfoService")
public class BusinessInfoServiceImpl implements BusinessInfoService {

    @Value("${360pipeDrive}")
    String FILE_DIRECTORY;

    @Autowired
    private BusinessInfoRepository businessInfoRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private CustomersRepository customersRepository;

    @Override
    public BusinessInfoDto getBusinessInfoById(Integer id) {
        try {
            BusinessInfo businessInfo = this.businessInfoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Business info not found"));
            BusinessInfoDto businessInfoDto = new BusinessInfoDto();
            BeanUtils.copyProperties(businessInfoDto, businessInfo, "cusId");
            businessInfoDto.setCusId(businessInfo.getCustomers().getId());
            return businessInfoDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BusinessInfoDto createBusinessInfo(BusinessInfoDto businessInfoDto) {
        try {
            BusinessInfo businessInfo = new BusinessInfo();
            Customers customers = this.customersRepository.findById(businessInfoDto.getCusId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            businessInfo.setCustomers(customers);
            BeanUtils.copyProperties(businessInfoDto, businessInfo, "id", "cusId");
            this.businessInfoRepository.save(businessInfo);
            businessInfoDto.setId(businessInfo.getId());
            return businessInfoDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BusinessInfoDto updateBusinessInfo(Integer id, BusinessInfoDto businessInfoDto) {
        try {
            BusinessInfo businessInfo = this.businessInfoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Business info not found"));
            Customers customers = this.customersRepository.findById(businessInfoDto.getCusId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            businessInfo.setCustomers(customers);
            BeanUtils.copyProperties(businessInfoDto, businessInfo, "id", "cusId");
            this.businessInfoRepository.save(businessInfo);
            return businessInfoDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteBusinessInfo(Integer id) {
        try {
            BusinessInfo businessInfo = this.businessInfoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Business info not found"));
            this.deleteBrandLogo(id);
            this.businessInfoRepository.delete(businessInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String uploadBrandLogo(Integer brandId, String imagePath) {
        try {
            this.deleteBrandLogo(brandId);
            BusinessInfo businessInfo = this.businessInfoRepository.findById(brandId)
                    .orElseThrow(() -> new RuntimeException("Business info not found"));
            String updatedPath = this.commonService.updateFileLocation(imagePath, brandId, "brandLogo", "brandLogo");
            if (updatedPath.equals("Error")) {
                return "Error";
            } else {
                businessInfo.setBrandLogo(updatedPath);
                this.businessInfoRepository.save(businessInfo);
                return updatedPath;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error uploadBrandLogo: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteBrandLogo(Integer brandId) {
        try {
            BusinessInfo businessInfo = this.businessInfoRepository.findById(brandId)
                    .orElseThrow(() -> new RuntimeException("Business info not found"));
            if (businessInfo.getBrandLogo() != null && businessInfo.getBrandLogo().startsWith("https://cdn")) {
                businessInfo.setBrandLogo(null);
                this.businessInfoRepository.save(businessInfo);
                return true;
            } else {
                File existingImagePath = new File(FILE_DIRECTORY + brandId + "/brandLogo/");
                if (existingImagePath.exists()) {
                    this.commonService.deleteDirectoryRecursively(existingImagePath);
                    businessInfo.setBrandLogo("");
                    this.businessInfoRepository.save(businessInfo);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error deleteBrandLogo: " + e.getMessage(), e);
        }
    }
}
