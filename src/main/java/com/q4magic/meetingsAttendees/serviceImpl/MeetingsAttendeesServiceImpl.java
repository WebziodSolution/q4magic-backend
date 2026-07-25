package com.q4magic.meetingsAttendees.serviceImpl;

import com.q4magic.common.dto.MeetingsAttendeesDto;
import com.q4magic.common.models.Contacts;
import com.q4magic.common.models.Meetings;
import com.q4magic.common.models.MeetingsAttendees;
import com.q4magic.common.repository.ContactsRepository;
import com.q4magic.common.repository.MeetingsAttendeesRepository;
import com.q4magic.common.repository.MeetingsRepository;
import com.q4magic.meetingsAttendees.service.MeetingsAttendeesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service(value = "MeetingsAttendeesService")
public class MeetingsAttendeesServiceImpl implements MeetingsAttendeesService {

    @Autowired
    private MeetingsAttendeesRepository meetingsAttendeesRepository;

    @Autowired
    private MeetingsRepository meetingsRepository;

    @Autowired
    private ContactsRepository contactsRepository;

    @Override
    public List<MeetingsAttendeesDto> findByMeetingsId(Integer meetingsId) {
        try {
            List<MeetingsAttendees> meetingsAttendeesList = this.meetingsAttendeesRepository.findByMeetingId(meetingsId);
            List<MeetingsAttendeesDto> meetingsAttendeesDtoList = new ArrayList<>();
            if (!meetingsAttendeesList.isEmpty()) {
                for (MeetingsAttendees meetingsAttendees : meetingsAttendeesList) {
                        meetingsAttendeesDtoList.add(this.findById(meetingsAttendees.getId()));
                }
            }
            return meetingsAttendeesDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MeetingsAttendeesDto findById(Integer id) {
        try {
            MeetingsAttendees meetingsAttendees = this.meetingsAttendeesRepository.findById(id).orElseThrow(() -> new RuntimeException("Meeting Attendees not found"));
            MeetingsAttendeesDto meetingsAttendeesDto = new MeetingsAttendeesDto();
            meetingsAttendeesDto.setId(meetingsAttendees.getId());
            meetingsAttendeesDto.setContactId(meetingsAttendees.getContacts().getId());
            meetingsAttendeesDto.setMeetingId(meetingsAttendees.getMeetings().getId());
            meetingsAttendeesDto.setTitle(meetingsAttendees.getTitle());
            meetingsAttendeesDto.setRole(meetingsAttendees.getRole());
            meetingsAttendeesDto.setNote(meetingsAttendees.getNote());
            meetingsAttendeesDto.setContactName(meetingsAttendees.getContacts().getFirstName() + " " + meetingsAttendees.getContacts().getLastName());

            return meetingsAttendeesDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MeetingsAttendeesDto addMeetingAttendees(MeetingsAttendeesDto meetingsAttendeesDto) {
        try {
            Contacts contacts = this.contactsRepository.findById(meetingsAttendeesDto.getContactId()).orElseThrow(() -> new RuntimeException("Contact not found"));
            Meetings meetings = this.meetingsRepository.findById(meetingsAttendeesDto.getMeetingId()).orElseThrow(() -> new RuntimeException("Meeting not found"));

            MeetingsAttendees meetingsAttendees = new MeetingsAttendees();
            meetingsAttendees.setContacts(contacts);
            meetingsAttendees.setMeetings(meetings);
            meetingsAttendees.setTitle(meetingsAttendeesDto.getTitle());
            meetingsAttendees.setRole(meetingsAttendeesDto.getRole());
            meetingsAttendees.setNote(meetingsAttendeesDto.getNote());

            this.meetingsAttendeesRepository.save(meetingsAttendees);
            return meetingsAttendeesDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MeetingsAttendeesDto updateMeetingAttendees(Integer id, MeetingsAttendeesDto meetingsAttendeesDto) {
        try {
            Contacts contacts = this.contactsRepository.findById(meetingsAttendeesDto.getContactId()).orElseThrow(() -> new RuntimeException("Contact not found"));
            Meetings meetings = this.meetingsRepository.findById(meetingsAttendeesDto.getMeetingId()).orElseThrow(() -> new RuntimeException("Meeting not found"));

            MeetingsAttendees meetingsAttendees = this.meetingsAttendeesRepository.findById(id).orElseThrow(() -> new RuntimeException("Meeting Attendees not found"));
            meetingsAttendees.setContacts(contacts);
            meetingsAttendees.setMeetings(meetings);
            meetingsAttendees.setTitle(meetingsAttendeesDto.getTitle());
            meetingsAttendees.setRole(meetingsAttendeesDto.getRole());
            meetingsAttendees.setNote(meetingsAttendeesDto.getNote());

            this.meetingsAttendeesRepository.save(meetingsAttendees);
            return meetingsAttendeesDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteMeetingAttendees(Integer id) {
        try {
            MeetingsAttendees meetingsAttendees = this.meetingsAttendeesRepository.findById(id).orElseThrow(() -> new RuntimeException("Meeting Attendees not found"));
            this.meetingsAttendeesRepository.delete(meetingsAttendees);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
