package com.code.touragentbot.repositories;

import com.code.touragentbot.models.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface ActionRepository extends JpaRepository<Action, Long> {

    @Query(value = "SELECT DISTINCT a.next_id FROM actions a WHERE a.action_keyword=:key",nativeQuery = true)
    Long getActionByActionKeyword(String key);

}
