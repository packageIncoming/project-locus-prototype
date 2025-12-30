package com.locus.projectlocusprototype.Flashcard;

import com.locus.projectlocusprototype.Note.Note;
import com.locus.projectlocusprototype.Auth.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard,Long> {
    Optional<Flashcard> findFlashcardById(Long id);

    Page<Flashcard> findFlashcardsByNote(Note note, Pageable pageable);

    Page<Flashcard> findFlashcardsByUser(User user, Pageable pageable);
}
