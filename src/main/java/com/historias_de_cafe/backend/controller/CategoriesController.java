package com.historias_de_cafe.backend.controller;

import com.historias_de_cafe.backend.DTO.CategoriesRequestDTO;
import com.historias_de_cafe.backend.DTO.CategoriesResponseDTO;
import com.historias_de_cafe.backend.service.CategoriesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoriesController {

    private final CategoriesService categoriesService;

    public CategoriesController(CategoriesService categoriesService) {
        this.categoriesService = categoriesService;
    }

    @GetMapping
    public ResponseEntity<List<CategoriesResponseDTO>> findAll() {
        return ResponseEntity.ok(categoriesService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriesResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(categoriesService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CategoriesResponseDTO> create(@Valid @RequestBody CategoriesRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriesService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriesResponseDTO> update(@PathVariable Long id,
                                                        @Valid @RequestBody CategoriesRequestDTO dto) {
        return ResponseEntity.ok(categoriesService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoriesService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
