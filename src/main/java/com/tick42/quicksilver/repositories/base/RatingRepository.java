package com.tick42.quicksilver.repositories.base;

import com.tick42.quicksilver.models.Rating;
import com.tick42.quicksilver.models.RatingPK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, RatingPK> {
}
