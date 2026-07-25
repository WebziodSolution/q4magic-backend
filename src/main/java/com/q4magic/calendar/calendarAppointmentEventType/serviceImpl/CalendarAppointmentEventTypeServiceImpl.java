package com.q4magic.calendar.calendarAppointmentEventType.serviceImpl;

import com.q4magic.calendar.calendarAppointmentEventType.service.CalendarAppointmentEventTypeService;
import com.q4magic.common.dto.CalendarAppointmentEventTypeDto;
import com.q4magic.common.models.CalendarAppointmentEventType;
import com.q4magic.common.models.Customers;
import com.q4magic.common.repository.CalendarAppointmentEventTypeRepository;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service(value = "CalendarAppointmentEventTypeService")
public class CalendarAppointmentEventTypeServiceImpl implements CalendarAppointmentEventTypeService {

    @Autowired
    private CalendarAppointmentEventTypeRepository calendarAppointmentEventTypeRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private CommonService commonService;

    @Override
    public List<CalendarAppointmentEventTypeDto> getAllAppointmentEventTypeByUserId(Integer cusId) {
        try {
            List<CalendarAppointmentEventType> calendarAppointmentEventTypeList = this.calendarAppointmentEventTypeRepository.findByCustomerId(cusId);
            List<CalendarAppointmentEventTypeDto> calendarAppointmentEventTypeDtoList = new ArrayList<>();
            if(!calendarAppointmentEventTypeList.isEmpty()){
                for (CalendarAppointmentEventType calendarAppointmentEventType : calendarAppointmentEventTypeList) {
                    calendarAppointmentEventTypeDtoList.add(this.getAppointmentEventType(calendarAppointmentEventType.getId()));
                }
            }
            return calendarAppointmentEventTypeDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CalendarAppointmentEventTypeDto getAppointmentEventType(Integer id) {
        try {
            CalendarAppointmentEventType calendarAppointmentEventType = this.calendarAppointmentEventTypeRepository.findById(id).orElseThrow(() -> new RuntimeException("AppointmentEvent Type Not Found"));
            CalendarAppointmentEventTypeDto calendarAppointmentEventTypeDto = new CalendarAppointmentEventTypeDto();
            calendarAppointmentEventTypeDto.setId(calendarAppointmentEventType.getId());
            calendarAppointmentEventTypeDto.setTitle(calendarAppointmentEventType.getTitle());
            calendarAppointmentEventTypeDto.setDescription(calendarAppointmentEventType.getDescription());
            calendarAppointmentEventTypeDto.setDurationHours(calendarAppointmentEventType.getDurationHours());
            calendarAppointmentEventTypeDto.setDurationMinutes(calendarAppointmentEventType.getDurationMinutes());
            calendarAppointmentEventTypeDto.setCreatedDate(this.commonService.convertDateToString(calendarAppointmentEventType.getCreatedDate()));
            return calendarAppointmentEventTypeDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveAppointmentEventType(Integer id, CalendarAppointmentEventTypeDto calendarAppointmentEventTypeDto) {
        try {
            CalendarAppointmentEventType calendarAppointmentEventType = new CalendarAppointmentEventType();
            if (id != null) {
                calendarAppointmentEventType = this.calendarAppointmentEventTypeRepository.findById(id).orElse(new CalendarAppointmentEventType());
            }
            Customers customers = this.customersRepository.findById(calendarAppointmentEventTypeDto.getCusId()).orElseThrow(() -> new RuntimeException("Customer Not Found"));
            calendarAppointmentEventType.setTitle(calendarAppointmentEventTypeDto.getTitle());
            calendarAppointmentEventType.setDescription(calendarAppointmentEventTypeDto.getDescription());
            calendarAppointmentEventType.setDurationHours(calendarAppointmentEventTypeDto.getDurationHours());
            calendarAppointmentEventType.setDurationMinutes(calendarAppointmentEventTypeDto.getDurationMinutes());
            calendarAppointmentEventType.setCustomers(customers);
            calendarAppointmentEventType.setCreatedDate(new Timestamp(System.currentTimeMillis()));
//            BeanUtils.copyProperties(calendarAppointmentEventType, calendarAppointmentEventTypeDto,"createdDate");
            this.calendarAppointmentEventTypeRepository.save(calendarAppointmentEventType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAppointmentEventType(Integer id) {
        try {
            CalendarAppointmentEventType calendarAppointmentEventType = this.calendarAppointmentEventTypeRepository.findById(id).orElseThrow(() -> new RuntimeException("AppointmentEvent Type Not Found"));
            this.calendarAppointmentEventTypeRepository.delete(calendarAppointmentEventType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
