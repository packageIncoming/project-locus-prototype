package com.locus.projectlocusprototype.Note;

import com.locus.projectlocusprototype.Auth.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note,Long> {

    @Query("SELECT n FROM Note n WHERE n.id = ?1")
    Optional<Note> findNoteById(Long noteId);

    Page<Note> getNotesByUser(User user, Pageable pageable);

    @Query("SELECT n FROM Note n WHERE n.user = ?1 AND  n.title = ?2")
    Optional<Note> findExistingNoteForUserWithTitle(User user, @NotBlank String title);
}
