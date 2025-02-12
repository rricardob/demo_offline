package com.example.offline.demo.controller.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class GetAllSalesResponse {

    private Long id;

    private String serie;

    private LocalDate saleDate;

    private Double totalAmount;

    /*private ClientInfo client;


    @Getter
    @Builder
    public static class ClientInfo {
        private Long id;
        private Integer document;
        private String firstName;
        private String lastName;
        private String email;
    }*/

}
