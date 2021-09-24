package com.tick42.quicksilver.repositories.base;

import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ExtensionRepository extends JpaRepository<Extension, Long> {
    @Query(value = "FROM Extension WHERE pending = false AND owner.isActive = true AND lower(name) LIKE lower(concat(:name,'%'))")
    List<Extension> findAllOrderedBy(@Param("name") String name, Pageable pageRequest);

    @Query(value = "SELECT count(*) FROM Extension WHERE pending = false AND owner.isActive = true AND lower(name) LIKE lower(concat(:name,'%'))")
    Long getTotalResults(@Param("name") String name);

    List<Extension> findByFeatured(boolean state);

    @Query(value = "FROM Extension as e WHERE pending LIKE :state AND id > :lastId")
    Page<Extension> findByPending(@Param("state") boolean state,@Param("lastId") long lastId, Pageable pageRequest);

    Extension findByName(String name);

    @Query(value = "FROM Extension as e WHERE owner LIKE :owner AND id > :lastId")
    Page<Extension> findUserExtensions(@Param("owner") UserModel owner, @Param("lastId") long lastId, Pageable pageRequest);

    @Query(value = "SELECT * FROM extensions as e LEFT JOIN extension_tags on id = extension_tags.extension WHERE extension_tags.tag LIKE :name AND id > :lastId", nativeQuery = true)
    Page<Extension> findByTag(@Param("name") String name, @Param("lastId") long lastId, Pageable pageRequest);
}
