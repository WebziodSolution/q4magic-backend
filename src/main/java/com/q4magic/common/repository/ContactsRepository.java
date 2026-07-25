package com.q4magic.common.repository;

import com.q4magic.common.models.Contacts;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface ContactsRepository extends JpaRepository<Contacts, Integer> {
    @Query("""
                SELECT c
                FROM Contacts c
                WHERE c.customers.id=:customerId AND LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(c.lastName)  LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    List<Contacts> searchByFirstOrLastName(Integer customerId, @Param("search") String search);

    @Query("""
    SELECT c
    FROM Contacts c
    WHERE c.customers.id = :customerId 
      AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
       OR LOWER(c.lastName)  LIKE LOWER(CONCAT('%', :search, '%')))
""")
    List<Contacts> getByFirstOrLastName(Integer customerId, @Param("search") String search);

    // New: lets us ask Spring Data to order for us
    List<Contacts> findByIsDeletedFalseAndCustomersId(Integer userId, Sort sort);

    @Query("SELECT c FROM Contacts c WHERE c.salesforceContactId=:salesforceContactId")
    Contacts findBySalesforceContactId(String salesforceContactId);

    @Query("SELECT COUNT(c) FROM Contacts c WHERE c.customers.id=:customerId AND c.isDeleted=false AND c.fromMailScraping IS TRUE")
    int countNewContactByCustomerId(Integer customerId);

    @Query("""
                SELECT c
                FROM Contacts c
                LEFT JOIN FETCH c.contacts m
                WHERE c.isDeleted = false
                  AND c.customers.id = :customersId
                  AND c.contacts.id = :contactsId
            """)
    List<Contacts> findByIsDeletedFalseAndCustomersIdAndContactsId(
            @Param("customersId") Integer customersId,
            @Param("contactsId") Integer contactsId
    );

    @Query("""
        SELECT COUNT(c) FROM Contacts c
        WHERE c.customers.id = :customerId
          AND c.createdAt BETWEEN :start AND :end AND c.fromMailScraping IS TRUE
    """)
    Integer countContactsByCustomerAndDateRange(
            @Param("customerId") Integer customerId,
            @Param("start") Timestamp start,
            @Param("end") Timestamp end
    );

}
