package com.code.touragentbot.repositories;

import com.code.touragentbot.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT question FROM Question question " +
            "WHERE question.id=:id")
    Question getQuestionsById(Long id);
}
