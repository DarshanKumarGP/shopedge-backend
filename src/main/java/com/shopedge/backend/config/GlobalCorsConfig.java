package com.shopedge.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalCorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry
                    .addMapping("/api/**")
                    .allowedOrigins(
                        // Development environments
                        "http://localhost:3000",     // React development server
                        "http://localhost:5174",     // Vite dev server (your existing)
                        "http://localhost:5173",     // Vite dev server (your existing)
                        
                        // Docker environments  
                        "http://localhost",          // Docker frontend (port 80)
                        "http://localhost:80",       // Docker frontend (explicit port 80)
                        
                        // Alternative localhost formats
                        "http://127.0.0.1:3000",    // Alternative localhost format
                        "http://127.0.0.1:5174",    // Alternative localhost format
                        "http://127.0.0.1:5173",    // Alternative localhost format
                        "http://127.0.0.1",         // Alternative localhost format (port 80)
                        "http://127.0.0.1:80"       // Alternative localhost format (explicit port 80)
                    )
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("Content-Type", "Authorization", "X-Timestamp", "X-Requested-With")
                    .exposedHeaders("Authorization", "X-Timestamp")
                    .allowCredentials(true);
            }
        };
    }
}