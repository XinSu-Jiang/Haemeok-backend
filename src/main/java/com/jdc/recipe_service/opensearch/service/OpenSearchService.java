package com.jdc.recipe_service.opensearch.service;

import com.jdc.recipe_service.domain.dto.RecipeSearchCondition;
import com.jdc.recipe_service.domain.dto.recipe.RecipeSimpleDto;
import com.jdc.recipe_service.domain.entity.Recipe;
import com.jdc.recipe_service.domain.repository.RecipeLikeRepository;
import com.jdc.recipe_service.domain.repository.RecipeRepository;
import com.jdc.recipe_service.domain.repository.RefrigeratorItemRepository;
import com.jdc.recipe_service.exception.CustomException;
import com.jdc.recipe_service.exception.ErrorCode;
import com.jdc.recipe_service.opensearch.keyword.KeywordService;
import lombok.RequiredArgsConstructor;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OpenSearchService {

    private final RecipeRepository recipeRepository;
    private final RecipeLikeRepository recipeLikeRepository;
    private final RestHighLevelClient client;
    private final RefrigeratorItemRepository fridgeRepo;
    private final KeywordService keywordService;

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;


    public Page<RecipeSimpleDto> searchRecipes(
            RecipeSearchCondition cond,
            Pageable pg,
            Long uid) {

        try {
            BoolQueryBuilder bool = QueryBuilders.boolQuery();

//            bool.mustNot(QueryBuilders.termQuery("user.id", 7));

            if (cond.getTitle() != null && !cond.getTitle().isBlank()) {
                String title = cond.getTitle().trim();
                keywordService.record(title);

                var titleQuery = QueryBuilders.boolQuery()
                        .should(QueryBuilders.matchPhrasePrefixQuery("title.prefix", title))
                        .should(QueryBuilders.matchQuery("title.infix", title));

                bool.must(titleQuery);
            }

            if (cond.getDishType() != null && !cond.getDishType().isBlank()) {
                bool.filter(QueryBuilders.termQuery("dishType", cond.getDishType()));
            }

            if (cond.getTagNames() != null && !cond.getTagNames().isEmpty()) {
                for (String tag : cond.getTagNames()) {
                    bool.filter(QueryBuilders.termQuery("tags", tag));
                }
            }

            if (cond.getIsAiGenerated() != null) {
                bool.filter(
                        QueryBuilders.termQuery("isAiGenerated", cond.getIsAiGenerated())
                );
            }

            if (bool.must().isEmpty() && bool.filter().isEmpty()) {
                bool.must(QueryBuilders.matchAllQuery());
            }

            var src = new SearchSourceBuilder()
                    .query(bool)
                    .from((int) pg.getOffset())
                    .size(pg.getPageSize());
            pg.getSort().forEach(o ->
                    src.sort(o.getProperty(),
                            o.isAscending() ? SortOrder.ASC : SortOrder.DESC)
            );

            SearchResponse resp = client.search(
                    new SearchRequest("recipes").source(src),
                    RequestOptions.DEFAULT
            );
            SearchHits hits = resp.getHits();

            List<Long> ids = Arrays.stream(hits.getHits())
                    .map(h -> Long.valueOf(h.getId()))
                    .collect(Collectors.toList());
            if (ids.isEmpty()) {
                return new PageImpl<>(Collections.emptyList(), pg, 0);
            }

            List<Recipe> recipes = recipeRepository.findAllById(ids);
            Map<Long, Recipe> recipeMap = recipes.stream()
                    .collect(Collectors.toMap(Recipe::getId, Function.identity()));

            Set<Long> likedIds = uid != null
                    ? recipeLikeRepository
                    .findByUserIdAndRecipeIdIn(uid, ids)
                    .stream()
                    .map(l -> l.getRecipe().getId())
                    .collect(Collectors.toSet())
                    : Collections.emptySet();

            List<RecipeSimpleDto> list = ids.stream()
                    .filter(recipeMap::containsKey)
                    .map(id -> {
                        Recipe r = recipeMap.get(id);
                        return new RecipeSimpleDto(
                                r.getId(),
                                r.getTitle(),
                                r.getImageKey() == null ? null
                                        : String.format("https://%s.s3.%s.amazonaws.com/%s",
                                        bucketName,region,r.getImageKey()),
                                r.getUser().getId(),
                                r.getUser().getNickname(),
                                r.getUser().getProfileImage(),
                                r.getCreatedAt(),
                                r.getLikes().size(),
                                likedIds.contains(r.getId()),
                                r.getAvgRating(),
                                r.getRatingCount(),
                                r.getCookingTime() == null ? 0 : r.getCookingTime()
                        );
                    })
                    .collect(Collectors.toList());

            return new PageImpl<>(list, pg, hits.getTotalHits().value);

        } catch (IOException e) {
            throw new CustomException(
                    ErrorCode.SEARCH_FAILURE,
                    "OpenSearch 조회 실패: " + e.getMessage()
            );
        }
    }


}
