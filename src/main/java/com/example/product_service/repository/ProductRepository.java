package com.example.product_service.repository;

import com.example.product_service.infrastructure.exception.DataAccessException;
import com.example.product_service.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.iceberg.*;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.IcebergGenerics;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.parquet.GenericParquetWriter;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.*;
import org.apache.iceberg.parquet.Parquet;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductRepository {

   private final Table productTable;
   private final FileIO fileIO;

   public Product save(Product product) {
      validateProduct(product);
      try {
         Record record = buildRecord(product);
         DataFile dataFile = writeRecordToFile(record);
         appendDataFile(dataFile);
         return product;
      } catch (IOException e) {
         log.error("Failed to save product: {}", product.getProductId(), e);
         throw new DataAccessException("Failed to save product", e);
      }
   }

   public List<Product> findAll() {
      try (CloseableIterable<Record> records = IcebergGenerics.read(productTable).build()) {
         return StreamSupport.stream(records.spliterator(), false)
                 .map(this::mapToProduct)
                 .toList();
      } catch (IOException e) {
         log.error("Failed to retrieve products", e);
         throw new DataAccessException("Failed to retrieve products", e);
      }
   }

   public Optional<Product> findById(Long id) {
      try (CloseableIterable<Record> records = IcebergGenerics.read(productTable)
              .where(Expressions.equal("product_id", id))
              .build()) {

         return StreamSupport.stream(records.spliterator(), false)
                 .findFirst()
                 .map(this::mapToProduct);
      } catch (IOException e) {
         log.error("Failed to find product: {}", id, e);
         throw new DataAccessException("Failed to find product", e);
      }
   }

   public boolean existsById(Long id) {
      return findById(id).isPresent();
   }

   public void deleteById(Long id) {
      try {
         productTable.newDelete()
                 .deleteFromRowFilter(Expressions.equal("product_id", id))
                 .commit();
      } catch (Exception e) {
         log.error("Failed to delete product: {}", id, e);
         throw new DataAccessException("Failed to delete product", e);
      }
   }

   private Record buildRecord(Product product) {
      GenericRecord record = GenericRecord.create(productTable.schema());
      record.setField("product_id", product.getProductId());
      record.setField("title", product.getTitle());
      record.setField("description", product.getDescription());
      record.setField("amount", product.getAmount());
      record.setField("brand", product.getBrand());
      record.setField("material", product.getMaterial());
      record.setField("color", product.getColor());
      record.setField("price", product.getPrice());
      record.setField("supplier_id", product.getSupplierId());
      record.setField("supplier_name", product.getSupplierName());
      record.setField("supplier_contact", product.getSupplierContact());
      record.setField("supplier_country", product.getSupplierCountry());
      return record;
   }

   private DataFile writeRecordToFile(Record record) throws IOException {
      String filePath = String.format("%s/data/%s.parquet",
              productTable.location(), UUID.randomUUID());

      OutputFile outputFile = fileIO.newOutputFile(filePath);
      DataWriter<Record> writer = Parquet.writeData(outputFile)
              .schema(productTable.schema())
              .withSpec(productTable.spec())
              .createWriterFunc(GenericParquetWriter::buildWriter)
              .build();

       try (writer) {
           writer.write(record);
       }

      return writer.toDataFile();
   }

   private void appendDataFile(DataFile dataFile) {
      productTable.newAppend()
              .appendFile(dataFile)
              .commit();
   }

   private Product mapToProduct(Record record) {
      return Product.builder()
              .productId((Long) record.getField("product_id"))
              .title((String) record.getField("title"))
              .description((String) record.getField("description"))
              .amount((Long) record.getField("amount"))
              .brand((String) record.getField("brand"))
              .material((String) record.getField("material"))
              .color((String) record.getField("color"))
              .price((Double) record.getField("price"))
              .supplierId((Long) record.getField("supplier_id"))
              .supplierName((String) record.getField("supplier_name"))
              .supplierContact((String) record.getField("supplier_contact"))
              .supplierCountry((String) record.getField("supplier_country"))
              .build();
   }

   private void validateProduct(Product product) {
      if (product.getProductId() == null || product.getTitle() == null ||
              product.getAmount() == null || product.getBrand() == null ||
              product.getPrice() == null || product.getSupplierId() == null ||
              product.getSupplierName() == null) {
         throw new IllegalArgumentException("Required fields cannot be null");
      }
   }
}
