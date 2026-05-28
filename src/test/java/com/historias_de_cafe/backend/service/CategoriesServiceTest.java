package com.historias_de_cafe.backend.service;

import com.historias_de_cafe.backend.DTO.CategoriesRequestDTO;
import com.historias_de_cafe.backend.DTO.CategoriesResponseDTO;
import com.historias_de_cafe.backend.model.Categories;
import com.historias_de_cafe.backend.repository.CategoriesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriesServiceTest {

    @Mock
    private CategoriesRepository categoriesRepository;

    @InjectMocks
    private CategoriesService categoriesService;

    @Test
    void findAllReturnsCategoryDtos() {
        Categories category = new Categories(1L, "Tostado Medio", "Huila", "Molido");
        when(categoriesRepository.findAll()).thenReturn(List.of(category));

        List<CategoriesResponseDTO> response = categoriesService.findAll();

        assertEquals(1, response.size());
        assertEquals(1, response.get(0).id()); // 🌟 Si tu DTO castea a int, el 1 plano es correcto
        assertEquals("Tostado Medio", response.get(0).toastingType());
        assertEquals("Huila", response.get(0).regionOrigin());
        assertEquals("Molido", response.get(0).presentation());
    }

    @Test
    void saveCreatesCategory() {
        CategoriesRequestDTO request = new CategoriesRequestDTO("Tostado Claro", "Risaralda", "Grano");

        when(categoriesRepository.save(any(Categories.class))).thenAnswer(invocation -> {
            Categories category = invocation.getArgument(0);
            category.setId(10L);
            return category;
        });

        CategoriesResponseDTO response = categoriesService.save(request);

        assertEquals(10, response.id());
        assertEquals("Tostado Claro", response.toastingType());
        assertEquals("Risaralda", response.regionOrigin());
        assertEquals("Grano", response.presentation());
    }

    @Test
    void updateChangesExistingCategory() {
        Categories existing = new Categories(2L, "Tostado Medio", "Antioquia", "Molido");
        CategoriesRequestDTO request = new CategoriesRequestDTO("Tostado Oscuro", "Nariño", "Grano");

        when(categoriesRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(categoriesRepository.save(existing)).thenReturn(existing);

        // 🌟 CORREGIDO: Se pasa 2L en vez del entero plano 2
        CategoriesResponseDTO response = categoriesService.update(2L, request);

        assertEquals(2, response.id());
        assertEquals("Tostado Oscuro", response.toastingType());
        assertEquals("Nariño", response.regionOrigin());
        assertEquals("Grano", response.presentation());
    }

    @Test
    void findByIdThrowsWhenCategoryDoesNotExist() {
        when(categoriesRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> categoriesService.findById(99L));
    }

    @Test
    void deleteThrowsWhenCategoryDoesNotExist() {
        when(categoriesRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> categoriesService.delete(99L));
        verify(categoriesRepository, never()).deleteById(99L);
    }
}