package com.q4magic.account.service;

import com.q4magic.common.dto.AccountDto;

import java.util.List;
import java.util.Map;

public interface AccountService {
    List<AccountDto> getAllAccounts(Integer accountId,String fetchType);

    AccountDto getAccountBySalesforceId(String salesforceAccountId);

    AccountDto getAccountById(Integer accountId);

    Map<String, Object> getAccountByName(Integer createdBy);

    AccountDto createAccount(AccountDto accountDto, Boolean syncToSalesforce, Boolean checkValidation);

    AccountDto updateAccount(Integer accountId, AccountDto accountDto, Boolean syncToSalesforce, Boolean checkValidation);

    void deleteAccount(Integer accountId, Boolean syncToSalesforce);
}
