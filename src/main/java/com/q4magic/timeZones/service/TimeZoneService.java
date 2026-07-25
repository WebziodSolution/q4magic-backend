package com.q4magic.timeZones.service;

import com.q4magic.common.dto.TimeZoneListDto;

import java.util.List;

public interface TimeZoneService {
    List<TimeZoneListDto> getTimeZones();
}
