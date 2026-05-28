package com.historias_de_cafe.backend.service;

import com.historias_de_cafe.backend.DTO.ProductRequestDTO;
import com.historias_de_cafe.backend.DTO.ProductResponseDTO;
import com.historias_de_cafe.backend.model.Categories;
import com.historias_de_cafe.backend.model.Product;
import com.historias_de_cafe.backend.repository.CategoriesRepository;
import com.historias_de_cafe.backend.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    private final ProductRepository productRepository;
    private final CategoriesRepository categoriesRepository;

    public ProductService(ProductRepository productRepository, CategoriesRepository categoriesRepository) {
        this.productRepository = productRepository;
        this.categoriesRepository = categoriesRepository;
    }

    public ProductResponseDTO create(ProductRequestDTO dto) {
        logger.info("Creating product with categoryId: {}, name: {}", dto.getCategoryId(), dto.getName());
        
        if (dto.getCategoryId() == null) {
            logger.error("Category ID is null");
            throw new RuntimeException("El ID de la categoría es obligatorio");
        }
        
        logger.info("Looking for category with ID: {}", dto.getCategoryId());
        Categories category = categoriesRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> {
                    logger.error("Category not found with id: {}", dto.getCategoryId());
                    return new RuntimeException("Category not found with id: " + dto.getCategoryId());
                });

        logger.info("Category found: {}", category.getPresentation());
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(category);
        product.setImagen(dto.getImagen());

        logger.info("Saving product to database");
        Product savedProduct = productRepository.save(product);
        logger.info("Product saved successfully with ID: {}", savedProduct.getId());
        
        return toResponseDto(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return toResponseDto(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAll() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public ProductResponseDTO update(Long id, ProductRequestDTO dto) {
        if (dto.getCategoryId() == null) {
            throw new RuntimeException("El ID de la categoría es obligatorio");
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // 🌟 CORREGIDO: Se envía el Long directo (dto.getCategoryId()) sin .intValue()
        Categories category = categoriesRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(category);
        product.setImagen(dto.getImagen());

        return toResponseDto(productRepository.save(product));
    }

    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        product.setActive(false);
        productRepository.save(product);
    }

    private ProductResponseDTO toResponseDto(Product product) {
        Categories category = product.getCategory();
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                category != null ? category.getId().longValue() : null,
                category != null ? category.getPresentation() : null,
                product.getImagen()
        );
    }
}