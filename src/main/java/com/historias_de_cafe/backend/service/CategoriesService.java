package com.historias_de_cafe.backend.service;

import com.historias_de_cafe.backend.DTO.CategoriesRequestDTO;
import com.historias_de_cafe.backend.DTO.CategoriesResponseDTO;
import com.historias_de_cafe.backend.model.Categories;
import com.historias_de_cafe.backend.repository.CategoriesRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriesService {

    private final CategoriesRepository categoriesRepository;

    public CategoriesService(CategoriesRepository categoriesRepository) {
        this.categoriesRepository = categoriesRepository;
    }

    public List<CategoriesResponseDTO> findAll() {
        return categoriesRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public CategoriesResponseDTO findById(Long id) {
        Categories category = categoriesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return toResponseDTO(category);
    }

    public CategoriesResponseDTO save(CategoriesRequestDTO dto) {
        Categories category = new Categories();
        category.setToastingType(dto.toastingType());
        category.setRegionOrigin(dto.regionOrigin());
        category.setPresentation(dto.presentation());
        return toResponseDTO(categoriesRepository.save(category));
    }

    public CategoriesResponseDTO update(Long id, CategoriesRequestDTO dto) {
        Categories category = categoriesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        category.setToastingType(dto.toastingType());
        category.setRegionOrigin(dto.regionOrigin());
        category.setPresentation(dto.presentation());
        return toResponseDTO(categoriesRepository.save(category));
    }

    public void delete(Long id) {
        if (!categoriesRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        categoriesRepository.deleteById(id);
    }

    private CategoriesResponseDTO toResponseDTO(Categories category) {
        return new CategoriesResponseDTO(
                category.getId() != null ? category.getId().intValue() : null,
                category.getToastingType(),
                category.getRegionOrigin(),
                category.getPresentation()
        );
    }
}