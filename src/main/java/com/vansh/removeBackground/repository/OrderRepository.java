package com.vansh.removeBackground.repository;

import com.vansh.removeBackground.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long>{

    Optional<OrderEntity> findByOrderId(String orderId);
}
