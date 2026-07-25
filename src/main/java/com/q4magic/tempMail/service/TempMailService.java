package com.q4magic.tempMail.service;

import com.q4magic.common.dto.TempMailDto;

import java.util.List;
import java.util.Map;

public interface TempMailService {
    void deleteByRequestId(Integer reqId);

    void deleteAllByIds(List<Integer> ids);

    void deleteIntoMailByRequestId(Integer reqId);

    void deleteIntoMailAllByIds(List<Integer> ids);

    Map<String, Object> getMailByGroup(Integer customerId);

    List<TempMailDto> getAllTempMails(Integer customerId);

    TempMailDto getTempMailById(Integer id);

    void createTempMail(List<TempMailDto> tempMailDto, Integer customerId);

    boolean updateTempMail(Integer id, TempMailDto tempMailDto);

    void deleteTempMail(Integer id);

    void deleteTempMailInbox(Integer id);
}
