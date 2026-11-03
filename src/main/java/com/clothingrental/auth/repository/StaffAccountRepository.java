package com.clothingrental.auth.repository;

import com.clothingrental.auth.model.StaffAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffAccountRepository extends JpaRepository<StaffAccount, Long> {

    Optional<StaffAccount> findByUsername(String username);
}
