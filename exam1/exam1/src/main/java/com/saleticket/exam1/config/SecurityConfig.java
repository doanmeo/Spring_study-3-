package com.saleticket.exam1.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity // Bật cấu hình bảo mật web của Spring Security
@EnableMethodSecurity // Bật bảo mật cấp phương thức, cho phép sử dụng các chú thích bảo mật trên các
                      // phương thức
public class SecurityConfig {
        @Value("${jwt.signerKey}")
        String secretKey;
        private static final String[] PUBLIC_ENDPOINTS = {
                        "/auth/login",
                        "/users",
                        "/auth/logout",
                        "/auth/refresh"
        };

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity)
                        throws Exception {
                httpSecurity.csrf(csrf -> csrf.disable()) // Vô hiệu hóa CSRF để dễ dàng kiểm thử API
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(PUBLIC_ENDPOINTS).permitAll() // Cho phép truy cập
                                                                                               // không cần xác thực đến
                                                                                               // các endpoint công khai
                                                .requestMatchers(HttpMethod.POST, "/auth/token").permitAll() // Cho phép
                                                // .requestMatchers(HttpMethod.GET, "/users").hasAuthority("ROLE_ADMIN")
                                                // .requestMatchers(HttpMethod.GET,
                                                // "/users").hasRole("Role.ADMIN.name()") // Cách sử dụng hasRole khi đã
                                                // có tiền tố ROLE_ trong authority ma ko quan tâm đến tiền tố
                                                .anyRequest().authenticated() // Yêu cầu xác thực cho tất cả các yêu cầu
                                                                              // khác
                                // .anyRequest().permitAll()
                                )
                                .httpBasic(Customizer.withDefaults()); // Sử dụng xác thực HTTP Basic

                httpSecurity.oauth2ResourceServer(oauth2 -> oauth2
                                .jwt(jwt -> jwt.decoder(jwtDecoder())
                                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                                .authenticationEntryPoint(new MyCustomAuthenticationEntryPoint())); // khi người dùng
                                                                                                    // không có quyền
                                                                                                    // truy cập vào tài
                                                                                                    // nguyên, se dieu
                                                                                                    // huong den
                                                                                                    // MyCustomAuthenticationEntryPoint
                                                                                                    // de tra ve loi
                                                                                                    // dang json thay vi
                                                                                                    // trang login mac
                                                                                                    // dinh cua spring
                                                                                                    // security
                httpSecurity.csrf(AbstractHttpConfigurer::disable); // Vô hiệu hóa CSRF để dễ dàng kiểm thử API
                return httpSecurity.build();
        }

        @Bean // Cấu hình CORS để cho phép truy cập từ các nguồn khác nhau
        public CorsFilter corsFilter() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.addAllowedOriginPattern("*");

                configuration.addAllowedMethod("*");
                configuration.addAllowedHeader("*");

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration); // Áp dụng cấu hình CORS cho tất cả các endpoint

                return new CorsFilter(source);
        }

        @Bean
        JwtAuthenticationConverter jwtAuthenticationConverter() {
                // convert jwt token sang authority, //tu dong them tien to ROLE_ cho cac
                // authority
                JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                grantedAuthoritiesConverter.setAuthorityPrefix("");
                // Tiền tố cho các quyền. Mặc định là "ROLE_", nhưng ở đây chúng ta đặt thành
                // chuỗi rỗng để giữ nguyên tên quyền để phân biệt Role và Permission ở phần
                // service
                // grantedAuthoritiesConverter.setAuthoritiesClaimName("roles"); // Tên của
                // claim chứa các quyền trong JWT

                JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
                jwtConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

                return jwtConverter;
        }

        // @Bean
        // JwtDecoder jwtDecoder() {
        // SecretKeySpec secretKey = new SecretKeySpec(
        // SECRET_KEY.getBytes(java.nio.charset.StandardCharsets.UTF_8),
        // "HS512"); // secretkeyspec : tạo khóa bí mật từ chuỗi ký tự

        // return org.springframework.security.oauth2.jwt.NimbusJwtDecoder
        // .withSecretKey(secretKey)
        // .macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS512)
        // .build(); // Trả về một đối tượng JwtDecoder sử dụng khóa bí mật và thuật
        // toán HS512 để
        // // giải mã JWT
        // }
        // Sử dụng CustomJWTDecoder thay thế JwtDecoder mặc định
        @Bean // bean: có thể được quản lý bởi Spring, có thể được tiêm vào các thành phần
              // khác của ứng dụng
        PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(10);
        }

        @Bean
        JwtDecoder jwtDecoder() {
                SecretKeySpec secretKey1 = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
                return NimbusJwtDecoder.withSecretKey(secretKey1)
                                .macAlgorithm(MacAlgorithm.HS512)
                                .build();
        }

}