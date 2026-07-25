package com.q4magic.common.repository;

import com.q4magic.common.models.Opportunities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Repository
public interface OpportunitiesRepository extends JpaRepository<Opportunities, Integer>, JpaSpecificationExecutor<Opportunities> {
    @Query("SELECT o FROM Opportunities o WHERE o.salesforceOpportunityId=:salesforceOpportunityId")
    Opportunities findBySalesforceOpportunityId(String salesforceOpportunityId);

    @Query("SELECT o FROM Opportunities o WHERE o.salesforceOpportunityId IS NOT NULL")
    List<Opportunities> findAllSalesforceOpportunity();

    @Query("SELECT o FROM Opportunities o WHERE o.isDeleted= false AND o.customers.id = :customerId ORDER BY o.id DESC")
    List<Opportunities> findActiveOpportunities(@Param("customerId") Integer customerId);

    @Query("SELECT MAX(o.id) FROM Opportunities o")
    int lastOpportunitiesId();

    @Query("SELECT COUNT(o) FROM Opportunities o WHERE o.isDeleted=false AND o.customers.id=:customerId")
    int countOpportunitiesByCustomerId(Integer customerId);

    @Query("SELECT o FROM Opportunities o WHERE o.isDeleted=false AND o.customers.id=:customerId AND o.status='Pipeline'")
    List<Opportunities> pipeLineOpportunitiesByCustomerId(Integer customerId);

    @Query("SELECT o FROM Opportunities o WHERE o.isDeleted=false AND o.customers.id=:customerId AND o.salesStage='Closed Won'")
    List<Opportunities> closeDealOpportunitiesByCustomerId(Integer customerId);

    @Query("""
                SELECT o
                FROM Opportunities o
                WHERE o.isDeleted = false
                  AND o.customers.id = :customerId
                  AND o.closeDate IS NOT NULL
                  AND o.closeDate >= :startDate
                  AND o.closeDate <= :endDate
                ORDER BY o.id DESC
            """)
    List<Opportunities> findActiveOpportunitiesByCloseDateBetween(
            @Param("customerId") Integer customerId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    @Query("""
                SELECT o FROM Opportunities o
                WHERE o.customers.id = :customerId
                  AND o.createdAt BETWEEN :start AND :end
            """)
    List<Opportunities> findByCustomerAndDateRange(
            @Param("customerId") Integer customerId,
            @Param("start") Timestamp start,
            @Param("end") Timestamp end
    );

    @Query("""
                SELECT o FROM Opportunities o
                WHERE o.customers.id = :customerId
                  AND o.status = 'Pipeline'
                  AND o.createdAt BETWEEN :start AND :end
            """)
    List<Opportunities> findActiveOpportunitiesByCustomerAndDateRange(
            @Param("customerId") Integer customerId,
            @Param("start") Timestamp start,
            @Param("end") Timestamp end
    );

    @Query("""
                SELECT o FROM Opportunities o
                WHERE o.customers.id = :customerId
                  AND o.status = 'Won'
                  AND o.createdAt BETWEEN :start AND :end
            """)
    List<Opportunities> findClosedOpportunitiesByCustomerAndDateRange(
            @Param("customerId") Integer customerId,
            @Param("start") Timestamp start,
            @Param("end") Timestamp end
    );

    @Query("""
                SELECT o FROM Opportunities o
                WHERE o.customers.id = :customerId
            """)
    List<Opportunities> findAllByCustomer(
            @Param("customerId") Integer customerId
    );

    @Query("""
                SELECT o FROM Opportunities o
                WHERE o.customers.id = :customerId
                  AND o.status = 'Pipeline'
            """)
    List<Opportunities> findActiveOpportunitiesByCustomer(
            @Param("customerId") Integer customerId
    );

    @Query("""
                SELECT o FROM Opportunities o
                WHERE o.customers.id = :customerId
                  AND o.status = 'Closed'
            """)
    List<Opportunities> findClosedOpportunitiesByCustomer(
            @Param("customerId") Integer customerId
    );

}
