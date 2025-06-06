package com.jdc.recipe_service.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "recipe_step_ingredients", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"step_id", "ingredient_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RecipeStepIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private RecipeStep step;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_ingredient_id")
    private RecipeIngredient recipeIngredient;

    @Column(length = 50)
    private String quantity;

    @Column(length = 20)
    private String unit;

    private String customName;
    private String customUnit;
    private BigDecimal customPrice;

    public void updateQuantityAndUnit(String quantity, String unit) {
        this.quantity = quantity;
        this.unit = unit;
    }
}
