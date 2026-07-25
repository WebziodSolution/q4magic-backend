package com.q4magic.performance.serviceImpl;

import com.q4magic.common.dto.PerformanceDto;
import com.q4magic.common.models.Calendar;
import com.q4magic.common.models.Customers;
import com.q4magic.common.models.Performance;
import com.q4magic.common.repository.CalendarRepository;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.repository.PerformanceRepository;
import com.q4magic.performance.service.PerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service(value = "PerformanceService")
public class PerformanceServiceImpl implements PerformanceService {

    @Autowired
    private PerformanceRepository performanceRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Override
    public PerformanceDto getPerformanceByCustomerId(Integer cusId, String startDateTime, String endDateTime) {
        try {

            Date startDate = null;
            Date endDate = null;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            ZoneId systemZone = ZoneId.systemDefault();

            if (startDateTime != null && !startDateTime.isBlank()) {
                LocalDate localStart = LocalDate.parse(startDateTime, formatter);
                startDate = Date.from(
                        localStart.atStartOfDay()
                                .atZone(systemZone)
                                .withZoneSameInstant(ZoneId.of("UTC"))
                                .toInstant());
            }

            if (endDateTime != null && !endDateTime.isBlank()) {
                LocalDate localEnd = LocalDate.parse(endDateTime, formatter);
                endDate = Date.from(
                        localEnd.atTime(LocalTime.MAX)
                                .atZone(systemZone)
                                .withZoneSameInstant(ZoneId.of("UTC"))
                                .toInstant());
            }

            PerformanceDto dto = new PerformanceDto();

            dto.setTotalMettings(0);
            dto.setNewMettings(0);
            dto.setOldMettings(0);
            dto.setOnsiteCount(0);
            dto.setVirtualCount(0);
            dto.setMeetingQuota(0);

            dto.setCusId(cusId);
            List<Map<String, Object>> data = new ArrayList<>();

            // Logged in user
            calculateUserPerformance(cusId, startDate, endDate, data, dto);

            // Sub users
            List<Customers> customersList = this.customersRepository.getAllSubUsers(cusId);

            if (customersList != null) {
                for (Customers customer : customersList) {
                    calculateUserPerformance(customer.getId(), startDate, endDate, data, dto);
                }
            }

            dto.setData(data);

            return dto;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void calculateUserPerformance(Integer customerId,
                                          Date startDate,
                                          Date endDate,
                                          List<Map<String, Object>> data,
                                          PerformanceDto totalDto) {
        int newMeeting = 0;
        int oldMeeting = 0;
        int totalMeeting = 0;
        int onsite = 0;
        int virtual = 0;
        int meetingQuota = 0;

        Customers customer = this.customersRepository.findById(customerId).orElse(null);

        if (customer != null && customer.getMeetingQuota() != null) {
            meetingQuota = Integer.parseInt(customer.getMeetingQuota());
        }

        totalDto.setMeetingQuota(totalDto.getMeetingQuota() + meetingQuota);
        
        List<Calendar> calendars = this.calendarRepository.findCalendarListFilter(customerId, startDate, endDate);
        if (!calendars.isEmpty()) {
            totalMeeting = calendars.size();
            Performance performance = this.performanceRepository.findByCustomerId(customerId);

            if (performance != null) {
                newMeeting = Optional.ofNullable(performance.getNewMettings()).orElse(0);
                oldMeeting = Optional.ofNullable(performance.getOldMettings()).orElse(0);
            }

            for (Calendar calendar : calendars) {
                if (calendar.getLocation() != null && !calendar.getLocation().isBlank()) {
                    onsite++;
                } else {
                    virtual++;
                }
            }
            // Add into total
            totalDto.setTotalMettings(totalDto.getTotalMettings() + totalMeeting);
            totalDto.setNewMettings(totalDto.getNewMettings() + newMeeting);
            totalDto.setOldMettings(totalDto.getOldMettings() + oldMeeting);
            totalDto.setOnsiteCount(totalDto.getOnsiteCount() + onsite);
            totalDto.setVirtualCount(totalDto.getVirtualCount() + virtual);

            // Per user data
            Map<String, Object> map = new HashMap<>();
            map.put("customerId", customerId);
            map.put("customerName", customer != null ? customer.getFirstName() + " " + customer.getLastName() : "");
            map.put("totalMettings", totalMeeting);
            map.put("newMettings", newMeeting);
            map.put("oldMettings", oldMeeting);
            map.put("onsiteCount", onsite);
            map.put("virtualCount", virtual);

            data.add(map);
        }

    }
}
