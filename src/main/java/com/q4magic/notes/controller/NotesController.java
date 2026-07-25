package com.q4magic.notes.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.NotesDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.notes.service.NotesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notes")
public class NotesController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private NotesService notesService;

    @GetMapping("/getByMeetingId/{id}")
    public ApiResponse<Map<String, Object>> getNotesByMeetingId(@PathVariable("id") Integer id) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Note fetched successfully", this.notesService.getNotesByMeetingId(id));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch note", "");
        }
    }

    @GetMapping("/get/{id}")
    public ApiResponse<Map<String, Object>> getNote(@PathVariable("id") Integer id) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Note fetched successfully", this.notesService.getNotes(id));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch note", "");
        }
    }

    @PostMapping("/saveNote")
    public ApiResponse<Map<String, Object>> createNotes(@RequestBody NotesDto notesDto) {
        try {
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Note save successfully", this.notesService.createNotes(notesDto));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to save note", "");
        }
    }

    @PatchMapping("/updateNote/{id}")
    public ApiResponse<Map<String, Object>> updateNote(@PathVariable("id") Integer id, @RequestBody NotesDto notesDto) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Note updated successfully", this.notesService.updateNotes(id, notesDto));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to update note", "");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Map<String, Object>> deleteNote(@PathVariable("id") Integer id) {
        try {
            this.notesService.deleteNotes(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Note deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to delete note", "");
        }
    }
}
