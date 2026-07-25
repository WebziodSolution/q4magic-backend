package com.q4magic.common.repository;

import com.q4magic.common.models.countryToState.CountryToState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryToStateRepository extends JpaRepository<CountryToState, Integer> {
    @Query(value = "SELECT s FROM CountryToState s WHERE s.country.id = :id")
    List<CountryToState> findByCountryId(@Param("id") int id);
}
