package com.jdc.recipe_service.config;

import com.jdc.recipe_service.jwt.JwtAuthenticationFilter;
import com.jdc.recipe_service.security.CustomAuthenticationEntryPoint;
import com.jdc.recipe_service.security.oauth.CustomOAuth2UserService;
import com.jdc.recipe_service.security.oauth.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService oauth2UserService;
    private final OAuth2AuthenticationSuccessHandler successHandler;
    private final JwtAuthenticationFilter jwtFilter;
    private final CustomAuthenticationEntryPoint entryPoint;
    private final Environment env;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF OFF, CORS / 세션 stateless
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfig()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // --- 로컬 프로필: 전부 허용 (H2 콘솔 등) ---
        if (Arrays.asList(env.getActiveProfiles()).contains("local")) {
            http.authorizeHttpRequests(a -> a.anyRequest().permitAll())
                    .headers(h -> h.frameOptions(frame -> frame.disable()));
            return http.build();
        }

        // --- 운영/스테이징 ---
        http
                // HTTPS 강제
                .requiresChannel(ch -> ch.anyRequest().requiresSecure())
                .authorizeHttpRequests(auth -> auth

                        // 1) 공개 엔드포인트
                        .requestMatchers(
                                "/", "/oauth2/**", "/login/**", "/error",
                                "/h2-console/**",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml",
                                "/api/token/refresh", "/api/tags/**"
                        ).permitAll()

                        // 2) GET 중 “마이페이지”만 인증 필요
                        .requestMatchers(HttpMethod.GET,
                                "/api/me", "/api/me/favorites"
                        ).authenticated()

                        // 3) 읽기 전용 GET (모두 허용)
                        .requestMatchers(HttpMethod.GET,
                                "/api/recipes/*/comments",
                                "/api/recipes/*/comments/*/replies",
                                "/api/recipes/*",
                                "/api/recipes/simple",
                                "/api/recipes/search",
                                "/api/recipes/by-tag",
                                "/api/recipes/by-dish-type",
                                "/api/recipes/dish-types",
                                "/api/tags",
                                "/api/users/*",
                                "/api/users/*/recipes"
                        ).permitAll()

                        // 4) 인증 필요 POST
                        .requestMatchers(HttpMethod.POST,
                                "/api/recipes/*/comments",
                                "/api/recipes/*/comments/*/replies",
                                "/api/comments/*/like",
                                "/api/recipes",
                                "/api/recipes/presigned-urls",
                                "/api/recipes/*/like",
                                "/api/recipes/*/favorite"
                        ).authenticated()

                        // 5) 인증 필요 PUT
                        .requestMatchers(HttpMethod.PUT,
                                "/api/recipes/*",
                                "/api/recipes/*/rating",
                                "/api/me"
                        ).authenticated()

                        // 6) 인증 필요 DELETE
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/recipes/*/comments/*",
                                "/api/recipes/*",
                                "/api/recipes/*/rating",
                                "/api/me"
                        ).authenticated()

                        // 7) 나머지 전부 차단
                        .anyRequest().denyAll()
                )
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint))
                .formLogin(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(u -> u.userService(oauth2UserService))
                        .successHandler(successHandler)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfig() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "https://www.haemeok.com"
        ));
        cfg.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(Arrays.asList("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}
