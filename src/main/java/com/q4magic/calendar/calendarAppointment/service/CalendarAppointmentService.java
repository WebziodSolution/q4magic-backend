package com.q4magic.calendar.calendarAppointment.service;

import com.q4magic.common.dto.CalendarAppointmentAvailabilitySlotsDto;
import com.q4magic.common.dto.CalendarDto;
import com.q4magic.common.dto.FreeSlotListDto;
import com.q4magic.common.dto.SendAppointmentLinkDto;
import com.q4magic.common.models.Calendar;
import com.q4magic.common.models.Customers;

import java.util.List;
import java.util.Map;

public interface CalendarAppointmentService {
    List<CalendarAppointmentAvailabilitySlotsDto> getAvailabilitySlotsList(Integer cusId);

    Map<String, Object> saveAvailabilitySlots(Integer cusId, List<CalendarAppointmentAvailabilitySlotsDto> list);

    Map<String, Object> freeSlotList(String userTimeZone,FreeSlotListDto freeSlotListDto);

    Map<String, Object> saveAppointment(CalendarDto calendarDto);

    List<Calendar> getMyCalendarAppointmentLis(Integer count, Integer cusId);

    Map<String, Object> sendEmailAppointmentLink(SendAppointmentLinkDto sendAppointmentLinkDto);
}
