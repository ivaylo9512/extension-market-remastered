package com.tick42.quicksilver.repositories.base;

import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ExtensionRepository extends JpaRepository<Extension, Long> {
    List<Extension> findByFeatured(boolean state);

    @Query(value = "FROM Extension as e WHERE pending = :state AND id > :lastId ORDER BY id ASC")
    Page<Extension> findByPending(@Param("state") boolean state, @Param("lastId") long lastId, Pageable pageRequest);

    @Query("FROM Extension as e WHERE LOWER(name) LIKE LOWER(CONCAT(:name, '%')) AND name > :lastName AND pending = false ORDER BY name ASC, id ASC")
    Page<Extension> findAllByName(@Param("name") String name, @Param("lastName") String lastName, Pageable pageRequest);

    Extension findByName(String name);

    @Query("FROM Extension as e WHERE (file.downloadCount < :lastDownloadCount OR (file.downloadCount = :lastDownloadCount AND id > :lastId)) AND LOWER(name) LIKE LOWER(CONCAT(:name, '%')) AND pending = false ORDER BY file.downloadCount DESC, id ASC")
    Page<Extension> findAllByDownloaded(@Param("lastDownloadCount") int lastDownloadCount, @Param("name") String name, @Param("lastId") long lastId, Pageable pageable);

    @Query("FROM Extension as e WHERE (github.lastCommit < :lastCommit OR (github.lastCommit = :lastCommit AND id > :lastId)) AND LOWER(name) LIKE LOWER(CONCAT(:name, '%')) AND pending = false ORDER BY github.lastCommit DESC, id ASC")
    Page<Extension> findAllByCommitDate(@Param("lastCommit") LocalDateTime lastCommit, @Param("name") String name, @Param("lastId") long lastId, Pageable pageable);

    @Query("FROM Extension as e WHERE (uploadDate < :lastDate OR (uploadDate = :lastDate AND id > :lastId)) AND LOWER(name) LIKE LOWER(CONCAT(:name, '%')) AND pending = false ORDER BY uploadDate DESC, id ASC")
    Page<Extension> findAllByUploadDate(@Param("lastDate") LocalDateTime lastDate, @Param("name") String name, @Param("lastId") long lastId, Pageable pageable);

    @Query(value = "FROM Extension as e WHERE owner LIKE :owner AND id > :lastId AND pending = false")
    Page<Extension> findUserExtensions(@Param("owner") UserModel owner, @Param("lastId") long lastId, Pageable pageRequest);

    @Query(value = "SELECT * FROM extensions as e LEFT JOIN extension_tags on id = extension_tags.extension WHERE extension_tags.tag LIKE :name AND id > :lastId", nativeQuery = true)
    Page<Extension> findByTag(@Param("name") String name, @Param("lastId") long lastId, Pageable pageRequest);
}
