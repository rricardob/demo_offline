package com.example.offline.demo.controller.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class GetSaleInfo {

    private String serie;
    private ClientInfo clientInfo;
    private LocalDate saleDate;
    private List<ProductsInfo> products;
    private Double subTotal;
    private Double total;
    private Double igv;

    @Getter
    @Builder
    public static class ClientInfo {
        private String name;
        private int dni;
    }

    @Getter
    @Builder
    public static class ProductsInfo {
        private Long id;
        private String name;
        private int quantity;
        private double price;
        private double subTotal;
    }

}
