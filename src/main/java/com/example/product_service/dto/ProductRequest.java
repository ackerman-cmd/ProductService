package com.example.product_service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be less than 100 characters")
    private String title;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @NotNull(message = "Amount is required")
    @PositiveOrZero(message = "Amount must be positive or zero")
    private Long amount;

    @NotBlank(message = "Brand is required")
    @Size(max = 50, message = "Brand must be less than 50 characters")
    private String brand;

    @Size(max = 50, message = "Material must be less than 50 characters")
    private String material;

    @Size(max = 30, message = "Color must be less than 30 characters")
    private String color;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotBlank(message = "Supplier name is required")
    @Size(max = 100, message = "Supplier name must be less than 100 characters")
    private String supplierName;

    @Size(max = 100, message = "Supplier contact must be less than 100 characters")
    private String supplierContact;

    @Size(max = 50, message = "Supplier country must be less than 50 characters")
    private String supplierCountry;
}