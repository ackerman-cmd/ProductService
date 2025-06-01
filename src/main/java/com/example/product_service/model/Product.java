package com.example.product_service.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long productId;
    private String title;
    private String description;
    private Long amount;
    private String brand;
    private String material;
    private String color;
    private Double price;
    private Long supplierId;
    private String supplierName;
    private String supplierContact;
    private String supplierCountry;
}
