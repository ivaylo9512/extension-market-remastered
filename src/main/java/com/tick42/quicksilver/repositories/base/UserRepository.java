package com.tick42.quicksilver.repositories.base;

import com.tick42.quicksilver.models.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Long> {
    Optional<UserModel> findByUsername(String username);

    Optional<UserModel> findFirstByUsernameOrEmail(String username, String email);

    @Query(value = "FROM UserModel as u WHERE lower(username) LIKE lower(concat(:name, '%')) AND username > :lastName")
    Page<UserModel> findByName(@Param("name") String name,
                               @Param("lastName") String lastName,
                               Pageable pageRequest);

    @Query(value = "FROM UserModel as u WHERE lower(username) LIKE lower(concat(:name, '%')) AND isActive = :isActive AND username > :lastName")
    Page<UserModel> findByActive(@Param("isActive") boolean isActive,
                                 @Param("name") String name,
                                 @Param("lastName") String lastName,
                                 Pageable pageRequest);

}
