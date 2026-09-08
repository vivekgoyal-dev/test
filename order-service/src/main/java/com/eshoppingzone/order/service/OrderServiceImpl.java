package com.eshoppingzone.order.service;

import com.eshoppingzone.order.address.Address;
import com.eshoppingzone.order.dto.CartDto;
import com.eshoppingzone.order.dto.ItemsDto;
import com.eshoppingzone.order.dto.OrdersDto;
import com.eshoppingzone.order.orders.Orders;
import com.eshoppingzone.order.product.Product;
import com.eshoppingzone.order.repository.AddressRepository;
import com.eshoppingzone.order.repository.OrderRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    public static final String CASH_ON_DELIVERY = "Cash on delivery";
    public static final String WALLET = "Wallet";
    public static final String PLACED = "Order Placed";

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final RestTemplate restTemplate;

    public OrderServiceImpl(OrderRepository orderRepository, AddressRepository addressRepository,
                            RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<OrdersDto> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public List<OrdersDto> placeOrder(CartDto cart) {
        return createOrders(cart, CASH_ON_DELIVERY);
    }

    /**
     * Wallet checkout. The orders are written first so the payment can carry a real order id, then the
     * wallet is debited; if the debit fails this transaction rolls back and no order survives.
     */
    @Override
    @Transactional
    public List<OrdersDto> onlinePayment(CartDto cart) {
        List<OrdersDto> orders = createOrders(cart, WALLET);
        double total = orders.stream().mapToDouble(OrdersDto::getAmmountPaid).sum();
        payFromWallet(cart.getCartId(), total, orders.get(0).getOrderId());
        return orders;
    }

    private List<OrdersDto> createOrders(CartDto cart, String modeOfPayment) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cart is empty");
        }
        Address address = latestAddressOf(cart.getCartId());
        List<OrdersDto> placed = new ArrayList<>();
        for (ItemsDto item : cart.getItems()) {
            if (item.getQuantity() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "quantity must be at least 1 for " + item.getProductName());
            }
            Orders order = new Orders();
            order.setOrderDate(LocalDate.now());
            order.setCustomerId(cart.getCartId());
            order.setAmmountPaid(item.getPrice() * item.getQuantity());
            order.setModeOfPayment(modeOfPayment);
            order.setOrderStatus(PLACED);
            order.setQuantity(item.getQuantity());
            order.setAddress(address);
            order.setProduct(new Product(String.valueOf(item.getProductId()), item.getProductName()));
            placed.add(toDto(orderRepository.save(order)));
        }
        return placed;
    }

    private Address latestAddressOf(int customerId) {
        List<Address> addresses = addressRepository.findByCustomerId(customerId);
        if (addresses.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "no delivery address stored for customer " + customerId);
        }
        return addresses.get(addresses.size() - 1);
    }

    private void payFromWallet(int customerId, double amount, int orderId) {
        try {
            restTemplate.put("http://wallet-service/wallets/{id}/pay?amount={amount}&orderId={orderId}",
                    null, customerId, amount, orderId);
        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "wallet payment failed: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "wallet-service unreachable", e);
        }
    }

    @Override
    public OrdersDto changeStatus(String status, int orderId) {
        Orders order = findOrThrow(orderId);
        order.setOrderStatus(status);
        return toDto(orderRepository.save(order));
    }

    @Override
    public void deleteOrder(int orderId) {
        orderRepository.delete(findOrThrow(orderId));
    }

    @Override
    public List<OrdersDto> getOrderByCustomerId(int customerId) {
        return orderRepository.findByCustomerId(customerId).stream().map(this::toDto).toList();
    }

    @Override
    public OrdersDto getOrderById(int orderId) {
        return toDto(findOrThrow(orderId));
    }

    @Override
    public OrdersDto findMAXByOrderId() {
        Orders order = orderRepository.findFirstByOrderByOrderIdDesc();
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no orders yet");
        }
        return toDto(order);
    }

    @Override
    public Address storeAddress(Address address) {
        if (address.getCustomerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId is required");
        }
        address.setAddressId(null);
        return addressRepository.save(address);
    }

    @Override
    public List<Address> getAddressByCustomerId(int customerId) {
        return addressRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Address> getAllAddress() {
        return addressRepository.findAll();
    }

    private Orders findOrThrow(int orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no order " + orderId));
    }

    private OrdersDto toDto(Orders order) {
        OrdersDto dto = new OrdersDto();
        BeanUtils.copyProperties(order, dto);
        return dto;
    }
}
