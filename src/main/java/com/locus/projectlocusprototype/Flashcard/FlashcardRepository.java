package com.locus.projectlocusprototype.Flashcard;

import com.locus.projectlocusprototype.Note.Note;
import com.locus.projectlocusprototype.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard,Long> {
    Optional<Flashcard> findFlashcardById(Long id);

    List<Flashcard> findFlashcardsByNote(Note note);

    List<Flashcard> findFlashcardsByUser(User user);
}
