package com.jdc.recipe_service.domain.entity;

import com.jdc.recipe_service.domain.entity.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.List;


@Entity
@Table(
        name = "recipes",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Recipe extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 레시피 작성자를 단방향 ManyToOne으로 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "dish_type", length = 50)
    private String dishType;

    @Column(name = "cooking_time")
    private Integer cookingTime;

    @Column(name = "avg_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal avgRating = BigDecimal.valueOf(0.00);

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "youtube_url", length = 255)
    private String youtubeUrl;

    @Column(name = "cooking_tools", columnDefinition = "TEXT")
    private String cookingTools;

    @Column(name = "is_ai_generated", nullable = false)
    @Builder.Default
    private boolean isAiGenerated = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer servings = 1; // 기본값 1인분


//    @Column(nullable = false)
//    @Builder.Default
//    private Boolean isPrivate = false; // 기본값 공개
//
    @Column(nullable = false)
    @Builder.Default
    private Integer totalIngredientCost = 0; // 재료 기준 가격

    @Column(nullable = false)
    @Builder.Default
    private Integer marketPrice = 0; // 시중 평균 가격


    @OneToMany(mappedBy = "recipe", fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    private List<RecipeTag> tags;

    @OneToMany(mappedBy = "recipe", fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private List<RecipeIngredient> ingredients;

    @OneToMany(mappedBy = "recipe", fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    private List<RecipeStep> steps;

    // 도메인 메서드: 레시피 업데이트
    public void update(String title, String description, String dishType, int cookingTime,
                       String imageUrl, String youtubeUrl, String cookingTools, Integer servings,
                       Integer totalIngredientCost, Integer marketPrice) {
        this.title = title;
        this.description = description;
        this.dishType = dishType;
        this.cookingTime = cookingTime;
        this.imageUrl = imageUrl;
        this.youtubeUrl = youtubeUrl;
        this.cookingTools = cookingTools;
        this.servings = servings;
       // this.isAiGenerated = isAiGenerated;
        this.totalIngredientCost = totalIngredientCost;
        this.marketPrice = marketPrice;
    }

}
