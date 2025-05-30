package com.example.offline.demo.controller.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CreateSaleRequest {

    private Long clientId;
    private List<Product> products;

    @Getter
    public static class Product{
        private Long id;
        private int quantity;

        public Product(Long id, int quantity) {
            this.id = id;
            this.quantity = quantity;
        }
    }

}


