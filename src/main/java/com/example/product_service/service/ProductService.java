package com.example.product_service.service;

import com.example.product_service.dto.ProductRequest;
import com.example.product_service.dto.ProductResponse;
import com.example.product_service.infrastructure.exception.NotFoundException;
import com.example.product_service.model.Product;
import com.example.product_service.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = convertToEntity(request);
        Product savedProduct = productRepository.save(product);
        return convertToResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::convertToResponse)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
    }

    @Transactional
    public void updateProduct(Long id, ProductRequest request) throws IOException {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));

        updateEntityFromRequest(request, existingProduct);
        productRepository.save(existingProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    private Product convertToEntity(ProductRequest request) {
        return Product.builder()
                .productId(request.getProductId())
                .title(request.getTitle())
                .description(request.getDescription())
                .amount(request.getAmount())
                .brand(request.getBrand())
                .material(request.getMaterial())
                .color(request.getColor())
                .price(request.getPrice())
                .supplierId(request.getSupplierId())
                .supplierName(request.getSupplierName())
                .supplierContact(request.getSupplierContact())
                .supplierCountry(request.getSupplierCountry())
                .build();
    }

    private ProductResponse convertToResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .title(product.getTitle())
                .description(product.getDescription())
                .amount(product.getAmount())
                .brand(product.getBrand())
                .material(product.getMaterial())
                .color(product.getColor())
                .price(product.getPrice())
                .supplierId(product.getSupplierId())
                .supplierName(product.getSupplierName())
                .supplierContact(product.getSupplierContact())
                .supplierCountry(product.getSupplierCountry())
                .build();
    }

    private void updateEntityFromRequest(ProductRequest request, Product product) {
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setAmount(request.getAmount());
        product.setBrand(request.getBrand());
        product.setMaterial(request.getMaterial());
        product.setColor(request.getColor());
        product.setPrice(request.getPrice());
        product.setSupplierId(request.getSupplierId());
        product.setSupplierName(request.getSupplierName());
        product.setSupplierContact(request.getSupplierContact());
        product.setSupplierCountry(request.getSupplierCountry());
    }
}