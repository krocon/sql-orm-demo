package dev.krocon.shop.ep036;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Spring Data Auditing: füllt @CreatedDate, @LastModifiedDate, @CreatedBy, @LastModifiedBy.
 * Mit Spring Security käme der Benutzer aus dem SecurityContext:
 *   Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
 *       .filter(Authentication::isAuthenticated).map(Authentication::getName)
 */
@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing(auditorAwareRef = "ep036AuditorAware")
public class AuditConfig {

  @Bean
  AuditorAware<String> ep036AuditorAware() {
    return () -> Optional.ofNullable(CurrentUser.get()).or(() -> Optional.of("system"));
  }
}
