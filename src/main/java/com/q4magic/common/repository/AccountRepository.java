package com.q4magic.common.repository;

import com.q4magic.common.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    @Query("SELECT a FROM Account a WHERE a.salesforceAccountId=:salesforceAccountId")
    Account findBySalesforceAccountId(String salesforceAccountId);

    @Query("SELECT a FROM Account a WHERE a.isDeleted = false AND a.customers.id = :id AND a.salesforceAccountId IS NOT NULL ORDER BY a.id DESC")
    List<Account> findActiveAccounts(@Param("id") Integer id);

    @Query("SELECT MAX(a.id) FROM Account a")
    int lastAccountId();

    @Query("SELECT COUNT(a) FROM Account a WHERE a.isDeleted=false AND a.customers.id=:customerId")
    int countAccountByCustomerId(Integer customerId);

    @Query("SELECT a FROM Account a " +
            "WHERE a.id <> :accId " +
            "AND a.isDeleted = false " +
            "AND a.customers.id = :customerId " +
            "AND a.accountName = :name")
    Account findAccountByCustomerIdAndNameAndIdNot(Integer accId, Integer customerId, String name);

    @Query("SELECT a FROM Account a WHERE a.isDeleted=false AND a.customers.id=:customerId")
    Account findAccountByCustomerIdAndName(Integer customerId);

}