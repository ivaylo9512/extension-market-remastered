package com.tick42.quicksilver.repositories.base;

import com.tick42.quicksilver.models.Rating;
import com.tick42.quicksilver.models.RatingPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RatingRepository extends JpaRepository<Rating, RatingPK> {

    @Query(value = "Select rating from Rating where extension like :extensionId and user like :userId")
    int findRatingByUser(int userId, int extensionId);
}
