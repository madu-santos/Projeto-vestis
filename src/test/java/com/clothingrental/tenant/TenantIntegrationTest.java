package com.clothingrental.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clothingrental.auth.model.StaffAccount;
import com.clothingrental.auth.repository.StaffAccountRepository;
import com.clothingrental.tenant.model.Store;
import com.clothingrental.tenant.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testes de integração da camada de persistência multi-tenant com o PostgreSQL.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TenantIntegrationTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StaffAccountRepository staffAccountRepository;

    @Test
    void persistsStoreAndAssociatesStaffAccountCorrectly() {
        Store store = storeRepository.save(new Store("Loja Primavera", "primavera"));
        assertThat(store.getId()).isNotNull();

        StaffAccount staff = new StaffAccount("primavera.admin", "hash-bcrypt-12345", store);
        StaffAccount savedStaff = staffAccountRepository.save(staff);

        assertThat(savedStaff.getId()).isNotNull();
        assertThat(savedStaff.getStore().getId()).isEqualTo(store.getId());
        assertThat(savedStaff.getStore().getSlug()).isEqualTo("primavera");
    }

    @Test
    void databaseRejectsDuplicateStoreSlug() {
        storeRepository.save(new Store("Loja Alpha", "slug-duplicado"));
        storeRepository.flush();

        Store secondStore = new Store("Loja Beta", "slug-duplicado");

        assertThatThrownBy(() -> {
            storeRepository.save(secondStore);
            storeRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseRejectsDuplicateUsernameAcrossDifferentStores() {
        Store store1 = storeRepository.save(new Store("Loja Um", "loja-um"));
        Store store2 = storeRepository.save(new Store("Loja Dois", "loja-dois"));
        storeRepository.flush();

        staffAccountRepository.save(new StaffAccount("admin.comum", "hash-1", store1));
        staffAccountRepository.flush();

        StaffAccount duplicateUserAnotherStore = new StaffAccount("admin.comum", "hash-2", store2);

        assertThatThrownBy(() -> {
            staffAccountRepository.save(duplicateUserAnotherStore);
            staffAccountRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
