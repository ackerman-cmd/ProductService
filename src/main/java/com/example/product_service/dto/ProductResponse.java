package com.example.product_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

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
