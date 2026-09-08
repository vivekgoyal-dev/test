package com.shoppingcart.order.repository;

import com.shoppingcart.order.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Orders, Long> {

    List<Orders> findByUserIdOrderByOrderIdDesc(Long userId);

    List<Orders> findByOwnerIdOrderByOrderIdDesc(Long ownerId);
}
