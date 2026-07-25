package com.q4magic.common.repository;

import com.q4magic.common.models.SubscriptionRates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRatesRepository extends JpaRepository<SubscriptionRates, Integer> {

}