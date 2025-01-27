package com.example.offline.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.offline.demo.entity.ProductEntity;
import com.example.offline.demo.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/sync")
@CrossOrigin(origins = "http://localhost:3000")
public class SyncController {
    private final ProductService productService;

    public SyncController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/products")
    public void syncProducts(@RequestBody List<Map<String, Object>> actions) {
        for (Map<String, Object> action : actions) {
            String type = (String) action.get("type");
            ProductEntity product = parseProduct(action.get("product"));

            switch (type) {
                case "CREATE":
                    productService.save(product);
                    break;
                case "UPDATE":
                    productService.update(product.getId(), product);
                    break;
                case "DELETE":
                    productService.delete(product.getId());
                    break;
            }
        }
    }

    private ProductEntity parseProduct(Object productObj) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Convertir el Map a un objeto Product
            return objectMapper.convertValue(productObj, ProductEntity.class);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error al convertir el producto: " + e.getMessage());
        }
    }
}
