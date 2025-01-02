package com.springcrud.Repo;

import com.springcrud.Model.Hashtable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface HashtableRepo extends JpaRepository<Hashtable, String> {

    @Modifying
    @Query(value = "INSERT INTO hashtable (expired_at, value, my_key) VALUES (:expiredAt, :value, :myKey) " +
                "ON DUPLICATE KEY UPDATE expired_at = VALUES(expired_at), value = VALUES(value)", nativeQuery = true)
    @Transactional
    int upsertHashtable(@Param("myKey") String key, @Param("value") String value, @Param("expiredAt") Long expiredAt);

}
