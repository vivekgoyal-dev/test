package com.eshoppingzone.order.service;

import com.eshoppingzone.order.address.Address;
import com.eshoppingzone.order.dto.CartDto;
import com.eshoppingzone.order.dto.OrdersDto;

import java.util.List;

public interface OrderService {

    List<OrdersDto> getAllOrders();

    List<OrdersDto> placeOrder(CartDto cart);

    List<OrdersDto> onlinePayment(CartDto cart);

    OrdersDto changeStatus(String status, int orderId);

    void deleteOrder(int orderId);

    List<OrdersDto> getOrderByCustomerId(int customerId);

    OrdersDto getOrderById(int orderId);

    OrdersDto findMAXByOrderId();

    Address storeAddress(Address address);

    List<Address> getAddressByCustomerId(int customerId);

    List<Address> getAllAddress();
}
