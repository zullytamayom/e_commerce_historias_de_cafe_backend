package com.historias_de_cafe.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
@Table(name = "categories")
public class Categories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categorie")
    private Long id;

    @NotBlank(message = "El tipo de tostion es obligatorio")
    @Size(max = 100, message = "El tipo de tostion es demasiado largo")
    @Column(name = "toasting_type", nullable = false)
    private String toastingType;

    @NotBlank(message = "La region de origen es obligatoria")
    @Size(max = 100, message = "La region de origen es demasiado larga")
    @Column(name = "region_origin", nullable = false)
    private String regionOrigin;

    @NotBlank(message = "La presentacion es obligatoria")
    @Size(max = 100, message = "La presentacion es demasiado larga")
    @Column(nullable = false)
    private String presentation;
    @OneToMany(mappedBy = "category")
    @JsonIgnore 
    private List<Product> products;

    public Categories() {
    }

    public Categories(Long id, String toastingType, String regionOrigin, String presentation) {
        this.id = id;
        this.toastingType = toastingType;
        this.regionOrigin = regionOrigin;
        this.presentation = presentation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToastingType() {
        return toastingType;
    }

    public void setToastingType(String toastingType) {
        this.toastingType = toastingType;
    }

    public String getRegionOrigin() {
        return regionOrigin;
    }

    public void setRegionOrigin(String regionOrigin) {
        this.regionOrigin = regionOrigin;
    }

    public String getPresentation() {
        return presentation;
    }

    public void setPresentation(String presentation) {
        this.presentation = presentation;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
