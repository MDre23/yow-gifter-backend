package cm.yowgifter.yow_gifter_backend.web.controller;

import cm.yowgifter.yow_gifter_backend.domain.Category;
import cm.yowgifter.yow_gifter_backend.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Catégories", description = "Consultation des catégories citoyennes d'objets")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    @Operation(summary = "Lister toutes les catégories officielles d'objets")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }
}
