package com.example.offline.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.offline.demo.entity.SaleEntity;
import org.springframework.data.jpa.repository.Query;

public interface SaleRepository extends JpaRepository<SaleEntity, Long> {

    @Query(value = "SELECT coalesce(max(id), 0) FROM SaleEntity")
    long getMaxId();

}
