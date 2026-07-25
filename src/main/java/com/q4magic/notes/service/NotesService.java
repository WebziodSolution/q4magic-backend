package com.q4magic.notes.service;

import com.q4magic.common.dto.NotesDto;

public interface NotesService {
    NotesDto getNotesByMeetingId(Integer id);

    NotesDto getNotes(Integer id);

    NotesDto createNotes(NotesDto notesDto);

    NotesDto updateNotes(Integer id, NotesDto notesDto);

    void deleteNotes(Integer id);
}