package com.shoppingcart.order.service;

import com.shoppingcart.order.dto.*;
import com.shoppingcart.order.exception.ApiException;
import com.shoppingcart.order.model.Address;
import com.shoppingcart.order.model.OrderStatus;
import com.shoppingcart.order.model.Orders;
import com.shoppingcart.order.repository.AddressRepository;
import com.shoppingcart.order.repository.OrderRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private static final String CASH_ON_DELIVERY = "Cash on delivery";

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final CartClient cartClient;
    private final ProductClient productClient;

    public OrderServiceImpl(OrderRepository orderRepository, AddressRepository addressRepository,
                            CartClient cartClient, ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.cartClient = cartClient;
        this.productClient = productClient;
    }

    @Override
    public AddressDto storeAddress(Long userId, AddressDto dto) {
        Address address = new Address();
        BeanUtils.copyProperties(dto, address);
        address.setAddressId(null);
        address.setUserId(userId);
        return toDto(addressRepository.save(address));
    }

    @Override
    public List<AddressDto> getMyAddresses(Long userId) {
        return addressRepository.findByUserId(userId).stream().map(this::toDto).toList();
    }

    /**
     * One order row per cart line. The cart is read from cart-service rather than accepted from
     * the client, so the prices are the ones cart-service already took from product-service.
     * The cart is emptied last: if anything above fails, the customer still has their cart.
     */
    @Override
    @Transactional
    public List<OrderDto> checkout(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> ApiException.notFound("no address " + addressId));
        if (!address.getUserId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "that address belongs to someone else");
        }

        CartDto cart = readCart(userId);
        if (cart.getItems().isEmpty()) {
            throw ApiException.badRequest("your cart is empty");
        }

        List<OrderDto> placed = new ArrayList<>();
        for (CartItemDto item : cart.getItems()) {
            Orders order = new Orders();
            order.setUserId(userId);
            order.setProductId(item.getProductId());
            order.setProductName(item.getProductName());
            order.setOwnerId(ownerOf(item.getProductId()));
            order.setQuantity(item.getQuantity());
            order.setAmountPaid(item.getPrice() * item.getQuantity());
            order.setModeOfPayment(CASH_ON_DELIVERY);
            order.setOrderStatus(OrderStatus.PLACED);
            order.setOrderDate(LocalDateTime.now());
            order.setAddress(address);
            placed.add(toDto(orderRepository.save(order)));
        }

        cartClient.clearCart(userId);
        return placed;
    }

    @Override
    public List<OrderDto> getMyOrders(Long userId) {
        return orderRepository.findByUserIdOrderByOrderIdDesc(userId).stream().map(this::toDto).toList();
    }

    @Override
    public List<OrderDto> getReceivedOrders(Long ownerId) {
        return orderRepository.findByOwnerIdOrderByOrderIdDesc(ownerId).stream().map(this::toDto).toList();
    }

    @Override
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toDto).toList();
    }

    /** A shopowner may only move an order for a product they own. */
    @Override
    public OrderDto changeStatus(Long orderId, Long ownerId, OrderStatus status) {
        Orders order = findOrThrow(orderId);
        if (!order.getOwnerId().equals(ownerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "this order is for another shopowner's product");
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw ApiException.badRequest("a cancelled order cannot change status");
        }
        order.setOrderStatus(status);
        return toDto(orderRepository.save(order));
    }

    @Override
    public OrderDto cancel(Long orderId, Long userId) {
        Orders order = findOrThrow(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "that order is not yours");
        }
        if (order.getOrderStatus() != OrderStatus.PLACED) {
            throw ApiException.badRequest("only an order still in PLACED can be cancelled, this one is "
                    + order.getOrderStatus());
        }
        order.setOrderStatus(OrderStatus.CANCELLED);
        return toDto(orderRepository.save(order));
    }

    private CartDto readCart(Long userId) {
        try {
            return cartClient.getCart(userId);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "could not read your cart, please try again");
        }
    }

    private Long ownerOf(Long productId) {
        try {
            return productClient.getProductById(productId).getOwnerId();
        } catch (Exception e) {
            throw ApiException.badRequest("product " + productId + " is no longer available");
        }
    }

    private Orders findOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("no order " + orderId));
    }

    private OrderDto toDto(Orders order) {
        OrderDto dto = new OrderDto();
        BeanUtils.copyProperties(order, dto, "address");
        dto.setAddress(toDto(order.getAddress()));
        return dto;
    }

    private AddressDto toDto(Address address) {
        AddressDto dto = new AddressDto();
        BeanUtils.copyProperties(address, dto);
        return dto;
    }
}
