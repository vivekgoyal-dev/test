package com.eshoppingzone.order.repository;

import com.eshoppingzone.order.orders.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Orders, Integer> {

    List<Orders> findByCustomerId(Integer customerId);

    Orders findFirstByOrderByOrderIdDesc();
}
