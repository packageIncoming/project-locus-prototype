package com.locus.projectlocusprototype.Note;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NoteService {
    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }


    public Optional<Note> getNote(Long noteId) {
        Optional<Note> note =  noteRepository.findNoteById(noteId);
        if (note.isPresent()){
            return note;
        }  else {
            throw new IllegalStateException("Error: no note exists with id " + noteId);
        }
    }

    public void createNote(Note note) {
        // check to make sure note's content length is at least 10 characters
        if (note.getContent().length() < 10){
            throw new IllegalStateException("Content of note must be at least 10 characters");
        }
        noteRepository.save(note);
    }

    public void updateNote(Long noteId, Note noteContent) {
        //make sure note exists
        Optional<Note> note = noteRepository.findNoteById(noteId);
        if (note.isEmpty()) {
            throw new IllegalStateException("Note with id " + noteId + " does not exist; cannot be updated");
        }
        Note obj = note.get();
        // check if noteContent has title and/or content
        if (!noteContent.getTitle().isBlank()){
            obj.setTitle(noteContent.getTitle());
        }
        if(!noteContent.getContent().isBlank() && noteContent.getContent().length()>=10){
            obj.setContent(noteContent.getContent());
        }
        noteRepository.save(obj);
    }

    public void deleteNote(Long noteId) {
        Optional<Note> note = noteRepository.findNoteById(noteId);
        if (note.isEmpty()) {
            throw new IllegalStateException("Note with id " + noteId + " does not exist; cannot be deleted");
        }
        Note obj = note.get();
        noteRepository.delete(obj);
    }

    public List<Note> getAllNotesForUser(Long userId) {
        return noteRepository.getNotesByAssociatedUser(userId);
    }
}
