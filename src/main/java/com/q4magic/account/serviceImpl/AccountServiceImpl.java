package com.q4magic.account.serviceImpl;

import com.q4magic.account.service.AccountService;
import com.q4magic.common.dto.AccountDto;
import com.q4magic.common.dto.SyncRecordsQueueDto;
import com.q4magic.common.models.*;
import com.q4magic.common.repository.AccountRepository;
import com.q4magic.common.repository.CRMRepository;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.repository.SyncRecordsQueueRepository;
import com.q4magic.syncRecordsQueue.service.SyncRecordsQueueService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service(value = "AccountService")
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CRMRepository crmRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private SyncRecordsQueueService syncRecordsQueueService;

    @Override
    public Map<String, Object> getAccountByName(Integer createdBy) {
        try {
            Map<String, Object> response = null;
            if (createdBy == null) {
                return Map.of(
                        "id", "null",
                        "success", false,
                        "message", "CustomerId is null"
                );
            }
            Account account = this.accountRepository.findAccountByCustomerIdAndName(createdBy);
            if (account != null) {
                response = Map.of(
                        "id", account.getId(),
                        "success", true,
                        "message", "Account already exists"
                );
                return response;
            } else {
                response = Map.of(
                        "id", "null",
                        "success", false
                );
                return response;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AccountDto> getAllAccounts(Integer accountId, String fetchType) {
        try {
            Customers customers = this.customersRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

            Set<Account> accountList = new LinkedHashSet<>();
            List<AccountDto> accountDtoList = new ArrayList<>();

            if (customers.getRole() != null &&
                    "SALES REPRESENTIVE".equals(customers.getRole().getRole())) {

                accountList.addAll(this.accountRepository.findActiveAccounts(accountId));

                List<Customers> subUsers = this.customersRepository.getAllSubUsers(accountId);
                if (subUsers != null && !subUsers.isEmpty()) {
                    for (Customers subUser : subUsers) {
                        accountList.addAll(this.accountRepository.findActiveAccounts(subUser.getId()));
                    }
                }
            } else {
                accountList.addAll(this.accountRepository.findActiveAccounts(accountId));

                if (customers.getCustomers() != null) {
                    accountList.addAll(this.accountRepository.findActiveAccounts(
                            customers.getCustomers().getId()));
                }
            }

            // Convert to list and explicitly sort DESC (last added first)
            List<Account> sortedAccounts = accountList.stream()
                    .sorted(Comparator.comparing(Account::getId).reversed())
                    .toList();

            return sortedAccounts.stream()
                    .map(account -> this.getAccountById(account.getId()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    @Override
    public AccountDto getAccountBySalesforceId(String salesforceAccountId) {
        try {
            Account account = this.accountRepository.findBySalesforceAccountId(salesforceAccountId);
            if (account != null) {
                AccountDto accountDto = new AccountDto();
                accountDto.setId(account.getId());
                accountDto.setCrmId(account.getCrm().getCrmId());
                accountDto.setCreatedBy(account.getCustomers().getId());
                BeanUtils.copyProperties(account, accountDto, "id", "crm");
                return accountDto;
            } else {
                throw new RuntimeException("Account not found with Salesforce ID: " + salesforceAccountId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public AccountDto getAccountById(Integer accountId) {
        try {
            Account account = this.accountRepository.findById(accountId).orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
            AccountDto accountDto = new AccountDto();
            accountDto.setId(account.getId());
            accountDto.setCrmId(account.getCrm().getCrmId());
            BeanUtils.copyProperties(account, accountDto, "id", "crm");
            return accountDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public AccountDto createAccount(AccountDto accountDto, Boolean isSyncToSalesforce, Boolean checkValidation) {
        try {
            Account isExites = this.accountRepository.findAccountByCustomerIdAndName(accountDto.getCreatedBy());
            if (isExites != null && checkValidation) {
                throw new RuntimeException("Account already exists");
            }
            Account account = new Account();
            CRM crm = this.crmRepository.findById(accountDto.getCrmId()).orElseThrow(() -> new RuntimeException("CRM not found with id: " + accountDto.getCrmId()));
            Customers customers = this.customersRepository.findById(accountDto.getCreatedBy()).orElseThrow(() -> new RuntimeException("User not found with id: " + accountDto.getCreatedBy()));
//            if (customers.getCustomers() != null) {
//                customers = this.customersRepository.findById(customers.getCustomers().getId())
//                        .orElseThrow(() -> new RuntimeException("User not found"));
//            }
            account.setCustomers(customers);
            account.setCrm(crm);
            account.setIsDeleted(false);
            BeanUtils.copyProperties(accountDto, account, "id", "crm", "isDeleted");
            this.accountRepository.save(account);
            if (isSyncToSalesforce) {
                SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                syncRecordsQueueDto.setSubjectId(account.getId());
                syncRecordsQueueDto.setSubject("Account");
                syncRecordsQueueDto.setOperationType("CREATE");
                syncRecordsQueueDto.setSyncType("PUSH");
                syncRecordsQueueDto.setDeleted(false);
                syncRecordsQueueDto.setCreatedBy(accountDto.getCreatedBy());
                this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
            }
            return accountDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public AccountDto updateAccount(Integer accountId, AccountDto accountDto, Boolean isSyncToSalesforce, Boolean checkValidation) {
        try {
            Account isExistingAccount = this.accountRepository.findAccountByCustomerIdAndNameAndIdNot(accountId, accountDto.getCreatedBy(), accountDto.getAccountName());
            if (isExistingAccount != null && checkValidation) {
                throw new RuntimeException("Account already exists with name: " + accountDto.getAccountName());
            }
            Account account = this.accountRepository.findById(accountId).orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
            CRM crm = this.crmRepository.findById(accountDto.getCrmId()).orElseThrow(() -> new RuntimeException("CRM not found with id: " + accountDto.getCrmId()));
            account.setCrm(crm);
            account.setIsDeleted(false);
            BeanUtils.copyProperties(accountDto, account, "id", "crm", "isDeleted");
            this.accountRepository.save(account);
            if (isSyncToSalesforce && account.getSalesforceAccountId() != null) {
                SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                syncRecordsQueueDto.setSubjectId(account.getId());
                syncRecordsQueueDto.setSubject("Account");
                syncRecordsQueueDto.setOperationType("UPDATE");
                syncRecordsQueueDto.setSyncType("PUSH");
                syncRecordsQueueDto.setDeleted(false);
                syncRecordsQueueDto.setCreatedBy(account.getCustomers().getId());
                this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
            }
            return accountDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAccount(Integer accountId, Boolean isSyncToSalesforce) {
        try {
            Account account = this.accountRepository.findById(accountId).orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
            if (!isSyncToSalesforce || account.getSalesforceAccountId() == null) {
                SyncRecordsQueueDto existingSyncRecord = this.syncRecordsQueueService.findBySubjectId(accountId, account.getCustomers().getId());
                if (existingSyncRecord != null) {
                    this.syncRecordsQueueService.deleteSyncRecord(existingSyncRecord.getId());
                }
                this.accountRepository.delete(account);
                return;
            }
            account.setIsDeleted(true);
            this.accountRepository.save(account);
            SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
            syncRecordsQueueDto.setSubjectId(account.getId());
            syncRecordsQueueDto.setSubject("Account");
            syncRecordsQueueDto.setOperationType("DELETE");
            syncRecordsQueueDto.setSyncType("PUSH");
            syncRecordsQueueDto.setDeleted(false);
            syncRecordsQueueDto.setCreatedBy(account.getCustomers().getId());
            this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
