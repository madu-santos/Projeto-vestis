package com.clothingrental.tenant.service;

import com.clothingrental.tenant.exception.StoreDisabledException;
import com.clothingrental.tenant.exception.StoreNotFoundException;
import com.clothingrental.tenant.model.Store;
import com.clothingrental.tenant.repository.StoreRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Operações de gestão e consulta de estabelecimentos (lojas/tenants).
 */
@Service
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    /**
     * Cria um novo estabelecimento ativo.
     *
     * @throws IllegalArgumentException se o nome for inválido, o slug for inválido ou já estiver em uso
     */
    @Transactional
    public Store createStore(String name, String slug) {
        return createStore(name, slug, null, null, null, null);
    }

    @Transactional
    public Store createStore(String name, String slug, String logoPath, String contactEmail, String contactPhone, String address) {
        String validatedName = Store.validateName(name);
        String validatedSlug = Store.validateSlug(slug);
        if (storeRepository.existsBySlug(validatedSlug)) {
            throw new IllegalArgumentException("Já existe um estabelecimento cadastrado com o slug '" + validatedSlug + "'.");
        }
        Store store = new Store(validatedName, validatedSlug, logoPath, contactEmail, contactPhone, address);
        return storeRepository.save(store);
    }

    @Transactional(readOnly = true)
    public Optional<Store> findBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }
        return storeRepository.findBySlug(slug.trim());
    }

    @Transactional(readOnly = true)
    public Optional<Store> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return storeRepository.findById(id);
    }

    /**
     * Localiza uma loja ativa pelo seu slug (para vitrines públicas e operações da loja).
     *
     * @throws StoreNotFoundException se a loja não existir
     * @throws StoreDisabledException se a loja existir mas estiver desativada
     */
    @Transactional(readOnly = true)
    public Store getActiveStoreBySlug(String slug) {
        Store store = findBySlug(slug)
                .orElseThrow(() -> new StoreNotFoundException(slug));

        if (!store.isEnabled()) {
            throw new StoreDisabledException(slug);
        }

        return store;
    }

    @Transactional(readOnly = true)
    public boolean isSlugInUse(String slug) {
        if (slug == null || slug.isBlank()) {
            return false;
        }
        return storeRepository.existsBySlug(slug.trim());
    }

    @Transactional(readOnly = true)
    public boolean isStoreActive(String slug) {
        return findBySlug(slug).map(Store::isEnabled).orElse(false);
    }

    @Transactional
    public Store updateBasicInfo(Long storeId, String name, String logoPath, String contactEmail, String contactPhone, String address) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("ID " + storeId));
        store.updateBasicInfo(name, logoPath, contactEmail, contactPhone, address);
        return storeRepository.save(store);
    }

    @Transactional
    public void disableStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("ID " + storeId));
        store.disable();
        storeRepository.save(store);
    }

    @Transactional
    public void enableStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("ID " + storeId));
        store.enable();
        storeRepository.save(store);
    }
}
