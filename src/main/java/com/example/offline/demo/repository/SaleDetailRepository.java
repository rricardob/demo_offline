package com.example.offline.demo.repository;

import com.example.offline.demo.entity.SaleDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleDetailRepository extends JpaRepository<SaleDetailEntity, Long> {

    List<SaleDetailEntity> findBySale_Id(Long saleId);
}
