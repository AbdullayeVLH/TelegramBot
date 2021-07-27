package com.code.touragentbot.repositories;

import com.code.touragentbot.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT order_ FROM Order order_ " +
            "WHERE order_.chatId=:chatId " +
            "AND order_.isActive=true ")
    Order findOrderByChatId(Long chatId);
}
