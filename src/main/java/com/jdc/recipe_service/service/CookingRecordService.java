package com.jdc.recipe_service.service;

import com.jdc.recipe_service.domain.entity.CookingRecord;
import com.jdc.recipe_service.domain.repository.CookingRecordRepository;
import com.jdc.recipe_service.domain.dto.calendar.*;
import com.jdc.recipe_service.domain.repository.RecipeRepository;
import com.jdc.recipe_service.domain.repository.UserRepository;
import com.jdc.recipe_service.util.PricingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CookingRecordService {

    private final CookingRecordRepository repo;
    private final UserRepository userRepo;
    private final RecipeRepository recipeRepo;


    /** 별점 작성 시점에 호출하여 기록 생성 */
    @Transactional
    public void createRecordFromRating(Long userId, Long recipeId, Long ratingId) {
        if (repo.existsByRatingId(ratingId)) {
            return;
        }
        var user = userRepo.getReferenceById(userId);
        var recipe = recipeRepo.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid recipeId"));

        int cost = recipe.getTotalIngredientCost() != null
                ? recipe.getTotalIngredientCost()
                : 0;

        Integer rp = recipe.getMarketPrice();
        int market;
        if (rp != null && rp > 0) {
            market = rp;
        } else if (cost > 0) {
            int percent = PricingUtil.randomizeMarginPercent(30);
            market = PricingUtil.applyMargin(cost, percent);
        } else {
            market = 0;
        }

        // 3) 절약액
        int savings = market - cost;

        CookingRecord rec = CookingRecord.builder()
                .user(user)
                .recipe(recipe)
                .ingredientCost(cost)
                .marketPrice(market)
                .savings(savings)
                .ratingId(ratingId)
                .build();

        repo.save(rec);
    }

    /** 별점 삭제 시점에 호출하여 기록 삭제 */
    @Transactional
    public void deleteByRatingId(Long ratingId) {
        repo.deleteByRatingId(ratingId);
    }

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    private String generateImageUrl(String key) {
        return key == null
                ? null
                : String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName, region, key);
    }


    /** 월별 달력 요약(일별 절약액 + 월합계) */
    @Transactional(readOnly = true)
    public CalendarMonthSummaryDto getMonthlySummary(Long userId, int year, int month) {
        List<Object[]> raw = repo.findMonthlySummaryRaw(userId, year, month);

        List<CalendarDaySummaryDto> daily = raw.stream()
                .map(arr -> {
                    LocalDate date = ((java.sql.Date) arr[0]).toLocalDate();
                    long totalSavings    = ((Number) arr[1]).longValue();
                    LocalDateTime start = date.atStartOfDay();
                    LocalDateTime end = start.plusDays(1);
                    List<CookingRecord> records = repo
                            .findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, start, end);
                    long totalCount = records.size();

                    String imageKey = records.stream()
                            .map(r -> r.getRecipe().getImageKey())
                            .filter(key -> key != null && !key.isBlank())
                            .findFirst()
                            .orElse(null);

                    String firstImageUrl = generateImageUrl(imageKey);
                    return new CalendarDaySummaryDto(date, totalSavings, totalCount, firstImageUrl);
                })
                .toList();

        long monthlyTotal = daily.stream()
                .mapToLong(CalendarDaySummaryDto::getTotalSavings)
                .sum();

        return new CalendarMonthSummaryDto(daily, monthlyTotal);
    }

    /** 특정 일자의 생 엔티티 리스트 */
    @Transactional(readOnly = true)
    public List<com.jdc.recipe_service.domain.entity.CookingRecord> getDailyRecordEntities(
            Long userId, LocalDate date) {

        var start = date.atStartOfDay();
        var end   = start.plusDays(1);

        return repo.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                userId, start, end
        );
    }

    /** 개별 기록 상세 */
    @Transactional(readOnly = true)
    public CookingRecordDto getRecordDetail(Long userId, Long recordId) {
        var rec = repo.findById(recordId)
                .filter(r -> r.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Record not found"));
        return CookingRecordDto.from(rec);
    }

    /** 불꽃(연속 요리 일수와 오늘 요리 여부) */
    @Transactional(readOnly = true)
    public CookingStreakDto getCookingStreakInfo(Long userId) {

        var rows = repo.findStreakAndTodayFlag(userId);
        if (rows.isEmpty()) {
            return new CookingStreakDto(0, false);
        }

        Object[] row = rows.get(0);   //
        int streak = ((Number) row[0]).intValue();
        boolean cookedToday = ((Number) row[1]).intValue() == 1;
        return new CookingStreakDto(streak, cookedToday);
    }
}