package com.clothingrental.tenant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.clothingrental.tenant.exception.StoreDisabledException;
import com.clothingrental.tenant.exception.StoreNotFoundException;
import com.clothingrental.tenant.model.Store;
import com.clothingrental.tenant.repository.StoreRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreService storeService;

    @Test
    void createsStoreWithValidData() {
        when(storeRepository.existsBySlug("loja-aurora")).thenReturn(false);
        when(storeRepository.save(any(Store.class))).thenAnswer(call -> call.getArgument(0));

        Store store = storeService.createStore("Loja Aurora", "loja-aurora");

        assertThat(store.getName()).isEqualTo("Loja Aurora");
        assertThat(store.getSlug()).isEqualTo("loja-aurora");
        assertThat(store.isEnabled()).isTrue();
        assertThat(store.getCreatedAt()).isNotNull();
    }

    @Test
    void rejectsDuplicateSlug() {
        when(storeRepository.existsBySlug("loja-aurora")).thenReturn(true);

        assertThatThrownBy(() -> storeService.createStore("Outra Loja", "loja-aurora"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Já existe um estabelecimento cadastrado com o slug 'loja-aurora'");

        verify(storeRepository).existsBySlug("loja-aurora");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Loja-Aurora",      // letras maiúsculas
            "loja aurora",      // espaços
            "loja_aurora",      // sublinhado / underline
            "loja@aurora",      // caractere especial @
            "loja.aurora",      // ponto
            "-loja-aurora",     // hífen inicial
            "loja-aurora-",     // hífen final
            "loja--aurora"      // hífens consecutivos
    })
    void rejectsInvalidSlugFormat(String invalidSlug) {
        assertThatThrownBy(() -> storeService.createStore("Loja Teste", invalidSlug))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("slug deve conter apenas letras minúsculas, números e hífens");

        verifyNoInteractions(storeRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rejectsBlankSlug(String blankSlug) {
        assertThatThrownBy(() -> storeService.createStore("Loja Teste", blankSlug))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("slug do estabelecimento é obrigatório");

        verifyNoInteractions(storeRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rejectsBlankName(String blankName) {
        assertThatThrownBy(() -> storeService.createStore(blankName, "loja-aurora"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome do estabelecimento é obrigatório");

        verifyNoInteractions(storeRepository);
    }

    @Test
    void findsStoreBySlug() {
        Store store = new Store("Loja Aurora", "loja-aurora");
        when(storeRepository.findBySlug("loja-aurora")).thenReturn(Optional.of(store));

        Optional<Store> result = storeService.findBySlug("loja-aurora");

        assertThat(result).contains(store);
    }

    @Test
    void getActiveStoreBySlugReturnsStoreWhenEnabled() {
        Store store = new Store("Loja Aurora", "loja-aurora");
        when(storeRepository.findBySlug("loja-aurora")).thenReturn(Optional.of(store));

        Store result = storeService.getActiveStoreBySlug("loja-aurora");

        assertThat(result).isEqualTo(store);
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void getActiveStoreBySlugThrowsNotFoundWhenStoreDoesNotExist() {
        when(storeRepository.findBySlug("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeService.getActiveStoreBySlug("inexistente"))
                .isInstanceOf(StoreNotFoundException.class)
                .hasMessageContaining("inexistente");
    }

    @Test
    void getActiveStoreBySlugThrowsDisabledWhenStoreIsDisabled() {
        Store store = new Store("Loja Aurora", "loja-aurora");
        store.disable();
        when(storeRepository.findBySlug("loja-aurora")).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> storeService.getActiveStoreBySlug("loja-aurora"))
                .isInstanceOf(StoreDisabledException.class)
                .hasMessageContaining("loja-aurora");
    }

    @Test
    void disableAndEnableStoreUpdatesStatus() {
        Store store = new Store("Loja Aurora", "loja-aurora");
        ReflectionTestUtils.setField(store, "id", 1L);

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(storeRepository.save(any(Store.class))).thenAnswer(call -> call.getArgument(0));

        storeService.disableStore(1L);
        assertThat(store.isEnabled()).isFalse();

        storeService.enableStore(1L);
        assertThat(store.isEnabled()).isTrue();
    }
}
