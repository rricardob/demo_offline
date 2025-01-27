package com.example.offline.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.offline.demo.entity.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity, Long>{
    
}
