package com.historias_de_cafe.backend.service;

import com.historias_de_cafe.backend.DTO.ProductRequestDTO;
import com.historias_de_cafe.backend.DTO.ProductResponseDTO;
import com.historias_de_cafe.backend.model.Categories;
import com.historias_de_cafe.backend.model.Product;
import com.historias_de_cafe.backend.repository.CategoriesRepository;
import com.historias_de_cafe.backend.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoriesRepository categoriesRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createSavesProductWithCategory() {
        Categories category = category();
        ProductRequestDTO request = productRequest();

        when(categoriesRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(10L);
            return product;
        });

        ProductResponseDTO response = productService.create(request);

        // 🌟 CORREGIDO: Uso estricto de getters tradicionales estilo JavaBean
        assertEquals(10L, response.getIdProduct());
        assertEquals("Cafe Huila", response.getName());
        assertEquals(0, BigDecimal.valueOf(35000.0).compareTo(response.getPrice()));
        assertEquals(1L, response.getCategoryId());
        assertEquals("Molido", response.getCategoryName());
        assertEquals("image.jpg", response.getImagen());
    }

    @Test
    void createThrowsWhenCategoryDoesNotExist() {
        ProductRequestDTO request = productRequest();
        when(categoriesRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.create(request));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getAllReturnsOnlyActiveProducts() {
        when(productRepository.findByActiveTrue()).thenReturn(List.of(product()));

        List<ProductResponseDTO> response = productService.getAll();

        assertEquals(1, response.size());
        assertEquals("Cafe Huila", response.get(0).getName()); // 🌟 CORREGIDO: .getName()
    }

    @Test
    void updateChangesProductFieldsAndCategory() {
        Product existing = product();
        Categories category = new Categories(2L, "Tostado Oscuro", "Nariño", "Grano");
        ProductRequestDTO request = productRequest();
        request.setName("Cafe Narino");
        request.setCategoryId(2L);
        request.setImagen("image.jpg");

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoriesRepository.findById(2L)).thenReturn(Optional.of(category));
        when(productRepository.save(existing)).thenReturn(existing);

        ProductResponseDTO response = productService.update(1L, request);

        // 🌟 CORREGIDO: Uso de getters de estilo tradicional
        assertEquals("Cafe Narino", response.getName());
        assertEquals(2L, response.getCategoryId());
        assertEquals("Grano", response.getCategoryName());
    }

    @Test
    void deleteSoftDeletesProduct() {
        Product product = product();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.delete(1L);

        assertFalse(product.isActive());
        verify(productRepository).save(product);
    }

    @Test
    void getByIdThrowsWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.getById(99L));
    }

    private ProductRequestDTO productRequest() {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("Cafe Huila");
        request.setDescription("Cafe especial");
        request.setPrice(BigDecimal.valueOf(35000.0));
        request.setStock(12);
        request.setCategoryId(1L);
        request.setImagen("image.jpg");
        return request;
    }

    private Product product() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Cafe Huila");
        product.setDescription("Cafe especial");
        product.setPrice(BigDecimal.valueOf(35000.0));
        product.setStock(12);
        product.setCategory(category());
        product.setImagen("image.jpg");
        return product;
    }

    private Categories category() {
        return new Categories(1L, "Tostado Medio", "Huila", "Molido");
    }
}