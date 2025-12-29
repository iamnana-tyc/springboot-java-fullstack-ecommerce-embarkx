package com.iamnana.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OderDTO {
    private Long orderId;
    private String email;
    private Double totalAmount;
    private LocalDate orderDate;
    private String orderStatus;
    private List<OrderItemDTO> orderItems;
    private Long addressId;
    private PaymentDTO payment;
}
