package com.tick42.quicksilver.repositories.base;

import com.tick42.quicksilver.models.Extension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExtensionRepository extends JpaRepository<Extension, Long> {

    @Query(value = "from Extension where pending = false and owner.active = true and lower(name) like lower(concat(:name,'%'))")
    List<Extension> findAllOrderedBy(@Param("name") String name, Pageable pageRequest);

    @Query(value = "select count(*) from Extension where pending = false and owner.active = true and lower(name) like lower(concat(:name,'%'))")
    Long getTotalResults(@Param("name") String name);

    List<Extension> findByFeatured(boolean state);

    List<Extension> findByPending(boolean state);

    Extension findByName(String name);
}
