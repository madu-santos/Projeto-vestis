package com.clothingrental.tenant.repository;

import com.clothingrental.tenant.model.Store;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
