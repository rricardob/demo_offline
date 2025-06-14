package com.example.offline.demo.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.example.offline.demo.entity.ProductEntity;
import com.example.offline.demo.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<ProductEntity> findAll() {
        return repository.findAll();
    }

    public ProductEntity save(ProductEntity product) {
        return repository.save(product);
    }

    public ProductEntity update(Long id, ProductEntity product) {
        product.setId(id);
        return repository.save(product);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<ProductEntity> findByName(String name) {
        if (name == null || name.isEmpty()) return List.of();
        return this.repository.findAll()
                .stream().filter(product -> product.getName()
                        .toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT))).toList();
    }
}
