package com.project.code.Service;

import com.project.code.Model.Customer;
import com.project.code.Model.Inventory;
import com.project.code.Model.OrderDetails;
import com.project.code.Model.OrderItem;
import com.project.code.Model.PlaceOrderRequestDTO;
import com.project.code.Model.PurchaseProductDTO;
import com.project.code.Model.Store;
import com.project.code.Repo.CustomerRepository;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.OrderDetailsRepository;
import com.project.code.Repo.OrderItemRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Repo.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private OrderDetailsRepository orderDetailsRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    public void saveOrder(PlaceOrderRequestDTO placeOrderRequest) {
        Customer existingCustomer = customerRepository.findByEmail(placeOrderRequest.getCustomerEmail());
        Customer customer = new Customer();
        customer.setName(placeOrderRequest.getCustomerName());
        customer.setEmail(placeOrderRequest.getCustomerEmail());
        customer.setPhone(placeOrderRequest.getCustomerPhone());
        if (existingCustomer == null) {
            customer = customerRepository.save(customer);
        } else {
            customer = existingCustomer;
        }
        Store store = storeRepository.findById(placeOrderRequest.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setCustomer(customer);
        orderDetails.setStore(store);
        orderDetails.setTotalPrice(placeOrderRequest.getTotalPrice());
        orderDetails.setDate(java.time.LocalDateTime.now());
        orderDetails = orderDetailsRepository.save(orderDetails);
        List<PurchaseProductDTO> purchaseProducts = placeOrderRequest.getPurchaseProduct();
        for (PurchaseProductDTO productDTO : purchaseProducts) {
            OrderItem orderItem = new OrderItem();
            Inventory inventory = inventoryRepository.findByProductIdandStoreId(productDTO.getId(),
                    placeOrderRequest.getStoreId());
            inventory.setStockLevel(inventory.getStockLevel() - productDTO.getQuantity());
            inventoryRepository.save(inventory);
            orderItem.setOrder(orderDetails);
            orderItem.setProduct(productRepository.findByid(productDTO.getId()));
            orderItem.setQuantity(productDTO.getQuantity());
            orderItem.setPrice(productDTO.getPrice() * productDTO.getQuantity());
            orderItemRepository.save(orderItem);
        }
    }
}
