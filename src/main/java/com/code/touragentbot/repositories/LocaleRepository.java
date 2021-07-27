package com.code.touragentbot.repositories;

import com.code.touragentbot.models.Locale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LocaleRepository extends JpaRepository<Locale, Long> {

    @Query(" SELECT locale FROM Locale locale " +
            " WHERE locale.isAction=true " +
            " AND locale.lang=:lang " +
            " AND locale.key=:key")
    List<Locale> getActionsByLangAndKey(String lang, String key);

    @Query("SELECT locale FROM Locale locale " +
            "WHERE locale.isAction=false " +
            "AND locale.lang=:lang " +
            "AND locale.key=:key")
    List<Locale> getQuestionsByLangAndKey(String lang, String key);

    @Query(value = "SELECT * FROM locales l where l.key=:key and l.lang=:lang and l.is_action=false", nativeQuery = true)
    Locale getLocaleByKeyAndLang(String key, String lang);

}
