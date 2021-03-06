package com.ship.shipsorder.entity.repository;


import com.ship.shipsorder.trash.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByProductId(UUID id);
}
//