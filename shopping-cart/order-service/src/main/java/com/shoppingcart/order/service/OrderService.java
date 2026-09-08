package com.shoppingcart.order.service;

import com.shoppingcart.order.dto.AddressDto;
import com.shoppingcart.order.dto.OrderDto;
import com.shoppingcart.order.model.OrderStatus;

import java.util.List;

public interface OrderService {

    AddressDto storeAddress(Long userId, AddressDto dto);

    List<AddressDto> getMyAddresses(Long userId);

    List<OrderDto> checkout(Long userId, Long addressId);

    List<OrderDto> getMyOrders(Long userId);

    List<OrderDto> getReceivedOrders(Long ownerId);

    List<OrderDto> getAllOrders();

    OrderDto changeStatus(Long orderId, Long ownerId, OrderStatus status);

    OrderDto cancel(Long orderId, Long userId);
}
