package com.q4magic.common.repository;

import com.q4magic.common.models.TimeZoneList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeZoneListRepository extends JpaRepository<TimeZoneList, Integer> {
}
