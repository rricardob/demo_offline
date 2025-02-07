package com.example.offline.demo.controller;

import com.example.offline.demo.controller.request.CreateSaleRequest;
import com.example.offline.demo.controller.response.GetMaxIdResponse;
import com.example.offline.demo.controller.response.GetSaleInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.offline.demo.service.SaleService;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:4173"})
public class SaleController {

    private final SaleService service;

    public SaleController(SaleService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody CreateSaleRequest request) {
        service.create(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get-sale-info/{id}")
    public ResponseEntity<GetSaleInfo> getSaleInfo(@PathVariable Long id) {
        return ResponseEntity.ok(service.getSaleInfo(id));
    }

    @GetMapping("/max-id")
    public ResponseEntity<GetMaxIdResponse> maxId() {
        return ResponseEntity.ok(service.maxId());
    }

}
