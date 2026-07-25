package com.q4magic.notes.serviceImpl;

import com.q4magic.common.dto.NotesDto;
import com.q4magic.common.models.Meetings;
import com.q4magic.common.models.Notes;
import com.q4magic.common.repository.MeetingsRepository;
import com.q4magic.common.repository.NotesRepository;
import com.q4magic.notes.service.NotesService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value = "NotesService")
public class NotesServiceImpl implements NotesService {
    @Autowired
    private NotesRepository notesRepository;

    @Autowired
    private MeetingsRepository meetingsRepository;

    @Override
    public NotesDto getNotesByMeetingId(Integer id) {
        try {
            Notes notes = this.notesRepository.findByMeetingId(id);
            NotesDto notesDto = new NotesDto();
            if (notes != null) {
                notesDto.setMeetingId(notes.getMeetings().getId());
                BeanUtils.copyProperties(notes, notesDto);
            }
            return notesDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public NotesDto getNotes(Integer id) {
        try {
            Notes notes = this.notesRepository.findById(id).orElseThrow(() -> new RuntimeException("Notes not found"));
            NotesDto notesDto = new NotesDto();
            notesDto.setMeetingId(notes.getMeetings().getId());
            BeanUtils.copyProperties(notes, notesDto);
            return notesDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public NotesDto createNotes(NotesDto notesDto) {
        try {
            Meetings meetings = this.meetingsRepository.findById(notesDto.getMeetingId()).orElseThrow(() -> new RuntimeException("Meeting not found"));
            Notes notes = new Notes();
            notes.setMeetings(meetings);
            BeanUtils.copyProperties(notesDto, notes, "meetings");
            this.notesRepository.save(notes);
            notesDto.setId(notes.getId());
            return notesDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public NotesDto updateNotes(Integer id, NotesDto notesDto) {
        try {
            Meetings meetings = this.meetingsRepository.findById(notesDto.getMeetingId()).orElseThrow(() -> new RuntimeException("Meeting not found"));
            Notes notes = this.notesRepository.findById(id).orElseThrow(() -> new RuntimeException("Notes not found"));
            notes.setMeetings(meetings);
            BeanUtils.copyProperties(notesDto, notes, "meetings");
            this.notesRepository.save(notes);
            notesDto.setId(notes.getId());
            return notesDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteNotes(Integer id) {
        try {
            Notes notes = this.notesRepository.findById(id).orElseThrow(() -> new RuntimeException("Notes not found"));
            this.notesRepository.delete(notes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
