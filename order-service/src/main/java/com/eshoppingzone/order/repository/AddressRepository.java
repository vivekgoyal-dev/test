package com.eshoppingzone.order.repository;

import com.eshoppingzone.order.address.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Integer> {

    List<Address> findByCustomerId(int customerId);
}
