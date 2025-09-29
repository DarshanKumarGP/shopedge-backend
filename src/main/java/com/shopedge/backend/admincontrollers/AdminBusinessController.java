package com.shopedge.backend.admincontrollers;

import com.shopedge.backend.adminservices.AdminBusinessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Admin Business Controller
 * Handles HTTP requests for business analytics and reporting
 * Provides endpoints for daily, monthly, yearly, and overall business metrics
 */
@RestController
@CrossOrigin(
		  origins = {"http://localhost:3000", "http://localhost:5174", "http://localhost:5173","http://localhost", "http://localhost:80","http://127.0.0.1:3000", "http://127.0.0.1:5174", "http://127.0.0.1:5173","http://127.0.0.1", "http://127.0.0.1:80"},
		  allowCredentials = "true",
		  allowedHeaders = {"Content-Type", "Authorization", "X-Timestamp", "X-Requested-With"},
		  exposedHeaders = {"Authorization", "X-Timestamp"}
		)
@RequestMapping("/admin/business")
public class AdminBusinessController {

    private final AdminBusinessService adminBusinessService;

    public AdminBusinessController(AdminBusinessService adminBusinessService) {
        this.adminBusinessService = adminBusinessService;
    }

    /**
     * Get monthly business analytics
     * GET /admin/business/monthly?month=12&year=2024
     */
    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyBusiness(@RequestParam int month, @RequestParam int year) {
        try {
            // Validate month and year
            if (month < 1 || month > 12) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Month must be between 1 and 12"));
            }

            if (year < 2020 || year > 2030) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Year must be between 2020 and 2030"));
            }

            Map<String, Object> businessReport = adminBusinessService.calculateMonthlyBusiness(month, year);
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(businessReport);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Something went wrong while calculating monthly business"));
        }
    }

    /**
     * Get daily business analytics
     * GET /admin/business/daily?date=2025-09-22
     */
    @GetMapping("/daily")
    public ResponseEntity<?> getDailyBusiness(@RequestParam String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            
            // Validate date is not in future
            if (localDate.isAfter(LocalDate.now())) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Cannot analyze future dates"));
            }

            Map<String, Object> businessReport = adminBusinessService.calculateDailyBusiness(localDate);
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(businessReport);

        } catch (DateTimeParseException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid date format. Use YYYY-MM-DD format"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Something went wrong while calculating daily business"));
        }
    }

    /**
     * Get yearly business analytics
     * GET /admin/business/yearly?year=2024
     */
    @GetMapping("/yearly")
    public ResponseEntity<?> getYearlyBusiness(@RequestParam int year) {
        try {
            // Validate year
            if (year < 2020 || year > 2030) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Year must be between 2020 and 2030"));
            }

            Map<String, Object> businessReport = adminBusinessService.calculateYearlyBusiness(year);
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(businessReport);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Something went wrong while calculating yearly business"));
        }
    }

    /**
     * Get overall business analytics (all-time)
     * GET /admin/business/overall
     */
    @GetMapping("/overall")
    public ResponseEntity<?> getOverallBusiness() {
        try {
            Map<String, Object> overallBusiness = adminBusinessService.calculateOverallBusiness();
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(overallBusiness);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Something went wrong while calculating overall business"));
        }
    }
}
