package com.example.product_service.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.hadoop.HadoopCatalog;
import org.apache.iceberg.hadoop.HadoopFileIO;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.types.Types;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@org.springframework.context.annotation.Configuration
@Slf4j
public class IcebergConfig {
    private static final String WAREHOUSE_LOCATION = "s3a://warehouse";

    @Value("${minio.root.user}")
    private String accessKey;

    @Value("${minio.root.password}")
    private String secretKey;

    @Value("${minio.endpoint:http://localhost:9000}")
    private String endpoint;

    @Bean
    public Configuration hadoopConfiguration() {
        Map<String, String> props = new HashMap<>();
        props.put("fs.s3a.access.key", accessKey);
        props.put("fs.s3a.secret.key", secretKey);
        props.put("fs.s3a.endpoint", endpoint);
        props.put("fs.s3a.path.style.access", "true");
        props.put("fs.s3a.disable.fsync", "true");

        Configuration config = new Configuration();
        props.forEach(config::set);
        log.info("Configure hadoopConfiguration");
        return config;
    }

    @Bean
    public HadoopCatalog hadoopCatalog(Configuration hadoopConfiguration) {
        log.info("Create hadoopCatalog");
        return new HadoopCatalog(hadoopConfiguration, WAREHOUSE_LOCATION);
    }

    @Bean
    public FileIO fileIO(Configuration hadoopConfiguration) {
        log.info("Create hadoopConfiguration");
        return new HadoopFileIO(hadoopConfiguration);
    }

    @Bean
    public Table productTable(HadoopCatalog catalog) {
        TableIdentifier id = TableIdentifier.of("iceberg_catalog", "products");

        if (!catalog.tableExists(id)) {
            Schema schema = new Schema(
                    Types.NestedField.required(1, "product_id", Types.LongType.get()),
                    Types.NestedField.required(2, "title", Types.StringType.get()),
                    Types.NestedField.optional(3, "description", Types.StringType.get()),
                    Types.NestedField.required(4, "amount", Types.LongType.get()),
                    Types.NestedField.required(5, "brand", Types.StringType.get()),
                    Types.NestedField.optional(6, "material", Types.StringType.get()),
                    Types.NestedField.optional(7, "color", Types.StringType.get()),
                    Types.NestedField.required(8, "price", Types.DoubleType.get()),
                    Types.NestedField.required(9, "supplier_id", Types.LongType.get()),
                    Types.NestedField.required(10, "supplier_name", Types.StringType.get()),
                    Types.NestedField.optional(11, "supplier_contact", Types.StringType.get()),
                    Types.NestedField.optional(12, "supplier_country", Types.StringType.get())
            );
            log.info("create Table");
            return catalog.createTable(id, schema);
        } else {
            return catalog.loadTable(id);
        }
    }
}