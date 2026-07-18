package com.saleticket.exam1.config;

import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.saleticket.exam1.entity.Role;
import com.saleticket.exam1.entity.User;
import com.saleticket.exam1.repository.RoleRepository;
import com.saleticket.exam1.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j // Tự động tạo logger cho lớp này: logger được sử dụng để ghi log thông tin,
       // cảnh báo, lỗi, v.v. trong ứng dụng
public class ApplicationInitConfig {
  @Autowired
  private PasswordEncoder passwordEncoder;

  @Bean // Đánh dấu phương thức này là một bean quản lý bởi Spring
  // @ConditionalOnProperty(prefix = "spring", value = "datasource.driverClassName", havingValue = "com.mysql.cj.jdbc.Driver")
  ApplicationRunner runner(UserRepository userRepository, RoleRepository roleRepository) { // ApplicationRunner:
                                                                                           // interface được sử dụng để
                                                                                           // thực thi mã
    // sau khi ứng dụng Spring Boot đã khởi động hoàn tất
    return args -> {
      if (userRepository.findByUsername("admin").isEmpty()) {
        String adminRoleName = com.saleticket.exam1.enums.Role.ADMIN.name();

        Role adminRole = roleRepository.findByName(adminRoleName)
            .orElseGet(() -> roleRepository.save(Role.builder().name(adminRoleName).build()));

        var roles = new HashSet<Role>();
        roles.add(adminRole);
        User user = User.builder()
            .username("admin")
            .password(passwordEncoder.encode("admin123"))
            .email("admin@example.com")
            .roles(roles)
            .build();
        userRepository.save(user);
        log.warn("Admin user created with username: admin and password: admin123");
      }
    };
  }

}
