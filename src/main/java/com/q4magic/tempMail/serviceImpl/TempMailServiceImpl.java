package com.q4magic.tempMail.serviceImpl;

import com.q4magic.common.dto.TempMailDto;
import com.q4magic.common.models.Customers;
import com.q4magic.common.models.EmailScrapingRequests;
import com.q4magic.common.models.TempMail;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.repository.EmailScrapingRequestsRepository;
import com.q4magic.common.repository.TempMailRepository;
import com.q4magic.common.service.CommonService;
import com.q4magic.tempMail.service.TempMailService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service(value = "TempMailService")
public class TempMailServiceImpl implements TempMailService {

    @Autowired
    private TempMailRepository tempMailRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private EmailScrapingRequestsRepository emailScrapingRequestsRepository;

    @Override
    public Map<String, Object> getMailByGroup(Integer customerId) {
        try {
            Map<String, Object> res = new HashMap<>();
            List<EmailScrapingRequests> emailScrapingRequestsList =
                    this.emailScrapingRequestsRepository.findMailsByCustomerId(customerId);

            if (!emailScrapingRequestsList.isEmpty()) {
                List<Map<String, Object>> resultList = new ArrayList<>();

                for (EmailScrapingRequests emailScrapingRequests : emailScrapingRequestsList) {
                    Map<String, Object> domain = new HashMap<>();
                    domain.put("email", emailScrapingRequests.getEmail());
                    domain.put("totalMessages", emailScrapingRequests.getMaxMessages());
                    domain.put("requestId", emailScrapingRequests.getId());
                    // Only fetch emails if status == 1
                    if (emailScrapingRequests.getStatus().equals(1)) {
                        List<TempMail> tempMails =
                                this.tempMailRepository.findByRequestId(emailScrapingRequests.getId());
                        List<TempMailDto> tempMailDtoList = new ArrayList<>();

                        if (!tempMails.isEmpty()) {
                            for (TempMail tempMail : tempMails) {
                                tempMailDtoList.add(this.getTempMailById(tempMail.getId()));
                            }
                        }
                        domain.put("emails", tempMailDtoList);
                    } else {
                        domain.put("emails", new ArrayList<>()); // empty list if not status 1
                    }

                    resultList.add(domain);
                }

                res.put("result", resultList);
            } else {
                res.put("result", new ArrayList<>()); // return empty list if no records
            }

            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TempMailDto> getAllTempMails(Integer customerId) {
        try {
            List<TempMail> tempMails = this.tempMailRepository.findMailsByCustomerId(customerId);
            List<TempMailDto> tempMailDtoList = new ArrayList<>();
            if (!tempMails.isEmpty()) {
                for (TempMail tempMail : tempMails) {
                    tempMailDtoList.add(this.getTempMailById(tempMail.getId()));
                }
                return tempMailDtoList;
            } else {
                return tempMailDtoList;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TempMailDto getTempMailById(Integer id) {
        try {
            TempMail tempMail = this.tempMailRepository.findById(id).orElseThrow(() -> new RuntimeException("TempMail not found"));
            TempMailDto tempMailDto = new TempMailDto();
            tempMailDto.setFirstName(tempMail.getFirstName());
            tempMailDto.setLastName(tempMail.getLastName());
            tempMailDto.setRequestId(tempMail.getEmailScrapingRequests().getId());
            tempMailDto.setCreatedBy(tempMail.getCustomers().getId());
            tempMailDto.setCreatedDate(this.commonService.convertDateToString(tempMail.getCreatedDate()));
            BeanUtils.copyProperties(tempMail, tempMailDto);
            return tempMailDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createTempMail(List<TempMailDto> tempMailDto, Integer customerId) {
        try {
            for (TempMailDto mail : tempMailDto) {
                TempMail isExits = this.tempMailRepository.findByEmailAndRole(customerId, mail.getEmail(), mail.getJobTitle());
                if (isExits != null) {
                    this.updateTempMail(isExits.getId(), mail);
                } else {
                    Customers customers = this.customersRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found"));
                    TempMail tempMail = new TempMail();
                    tempMail.setCustomers(customers);
                    tempMail.setCreatedDate(new Date());
                    BeanUtils.copyProperties(mail, tempMail, "id", "createdBy", "createdDate");
                    this.tempMailRepository.save(tempMail);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean updateTempMail(Integer id, TempMailDto tempMailDto) {
        try {
            TempMail tempMail = this.tempMailRepository.findById(id).orElseThrow(() -> new RuntimeException("TempMail not found"));
            BeanUtils.copyProperties(tempMailDto, tempMail, "id", "createdBy", "createdDate");
            this.tempMailRepository.save(tempMail);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteTempMail(Integer id) {
        try {
            TempMail tempMail = this.tempMailRepository.findById(id).orElseThrow(() -> new RuntimeException("TempMail not found"));
            this.tempMailRepository.delete(tempMail);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteTempMailInbox(Integer id) {
        try {
            TempMail tempMail = this.tempMailRepository.findById(id).orElseThrow(() -> new RuntimeException("TempMail not found"));
            tempMail.setIsDeleted(true);
            this.tempMailRepository.save(tempMail);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteByRequestId(Integer reqId) {
        try {
            List<TempMail> tempMailList = this.tempMailRepository.findByRequestId(reqId);
            if (!tempMailList.isEmpty()) {
                for (TempMail tempMail : tempMailList) {
                    this.deleteTempMail(tempMail.getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAllByIds(List<Integer> ids) {
        try {
            for (Integer id : ids) {
                this.deleteTempMail(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteIntoMailByRequestId(Integer reqId) {
        try {
            List<TempMail> tempMailList = this.tempMailRepository.findByRequestId(reqId);
            if (!tempMailList.isEmpty()) {
                for (TempMail tempMail : tempMailList) {
                    tempMail.setIsDeleted(true);
                    this.tempMailRepository.save(tempMail);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteIntoMailAllByIds(List<Integer> ids) {
        try {
            for (Integer id : ids) {
                TempMail tempMail = this.tempMailRepository.findById(id).orElseThrow(() -> new RuntimeException("TempMail not found"));
                tempMail.setIsDeleted(true);
                this.tempMailRepository.save(tempMail);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}