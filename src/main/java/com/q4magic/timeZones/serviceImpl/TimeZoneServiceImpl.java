package com.q4magic.timeZones.serviceImpl;

import com.q4magic.common.dto.TimeZoneListDto;
import com.q4magic.common.models.TimeZoneList;
import com.q4magic.common.repository.TimeZoneListRepository;
import com.q4magic.timeZones.service.TimeZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service(value = "TimeZoneService")
public class TimeZoneServiceImpl implements TimeZoneService {
    @Autowired
    private TimeZoneListRepository timeZoneListRepository;

    @Override
    public List<TimeZoneListDto> getTimeZones() {
        try {
            List<TimeZoneListDto> timeZones = new ArrayList<>();
            List<TimeZoneList> timeZoneLists = this.timeZoneListRepository.findAll();
            if (!timeZoneLists.isEmpty()) {
                for (TimeZoneList timeZoneList : timeZoneLists) {
                    TimeZoneList timeZoneList1 = this.timeZoneListRepository.findById(timeZoneList.getTmzId()).orElseThrow(() -> new RuntimeException("Time Zone not found"));
                    TimeZoneListDto timeZoneListDto = new TimeZoneListDto();
                    timeZoneListDto.setTmzId(timeZoneList.getTmzId());
                    timeZoneListDto.setTmzTitle(timeZoneList.getTmzTitle());
                    timeZoneListDto.setTmzValue(timeZoneList.getTmzValue());
                    timeZones.add(timeZoneListDto);
                }
            }
            return timeZones;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
