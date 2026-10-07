package com.texgarment.controller;

import com.texgarment.model.ProductionRun;
import com.texgarment.model.ProductionStage;
import com.texgarment.model.QualityCheck;
import com.texgarment.service.ProductionService;
import com.texgarment.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/production")
public class ProductionController {

    private final ProductionService productionService;

    public ProductionController(ProductionService productionService) {
        this.productionService = productionService;
    }

    // ==========================================
    // LINES (Must be before /{id})
    // ==========================================
    @GetMapping("/lines")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getProductionLines() {
        List<Map<String, Object>> lines = productionService.getProductionLines();
        return ResponseEntity.ok(ApiResponse.success(lines, "Production lines retrieved"));
    }

    @PostMapping("/lines")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createLine(@RequestBody Map<String, Object> data) {
        return ResponseEntity.ok(ApiResponse.success(data, "Production line established successfully"));
    }

    @PutMapping("/lines/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateLine(@PathVariable String id, @RequestBody Map<String, Object> data) {
        data.put("id", id);
        return ResponseEntity.ok(ApiResponse.success(data, "Production line updated successfully"));
    }

    @DeleteMapping("/lines/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteLine(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(Map.of("id", id), "Production line removed successfully"));
    }

    // ==========================================
    // TASKS (Must be before /{id})
    // ==========================================
    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductionTasks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = productionService.getAllRuns(search, status, priority, page, limit);
        return ResponseEntity.ok(ApiResponse.success(result, "Production tasks retrieved"));
    }

    @PostMapping("/tasks")
    public ResponseEntity<ApiResponse<ProductionRun>> createProductionTask(@RequestBody Map<String, Object> data) {
        ProductionRun run = productionService.createRun(data);
        return ResponseEntity.ok(ApiResponse.success(run, "Task scheduled successfully"));
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<ProductionRun>> updateProductionTask(
            @PathVariable String id,
            @RequestBody Map<String, Object> data) {
        ProductionRun run = productionService.updateRun(id, data);
        return ResponseEntity.ok(ApiResponse.success(run, "Production task updated"));
    }

    @PutMapping("/tasks/{id}/progress")
    public ResponseEntity<ApiResponse<ProductionRun>> updateTaskProgress(
            @PathVariable String id,
            @RequestBody Map<String, Object> data) {
        ProductionRun run = productionService.updateProgress(id, data);
        return ResponseEntity.ok(ApiResponse.success(run, "Production progress updated"));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<ProductionRun>> deleteProductionTask(@PathVariable String id) {
        ProductionRun run = productionService.deleteRun(id);
        return ResponseEntity.ok(ApiResponse.success(run, "Production task deleted"));
    }

    // ==========================================
    // PRODUCTION RUNS CRUD
    // ==========================================
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllRuns(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = productionService.getAllRuns(search, status, priority, page, limit);
        return ResponseEntity.ok(ApiResponse.success(result, "Production runs retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductionRun>> getRunById(@PathVariable String id) {
        ProductionRun run = productionService.getRunById(id);
        return ResponseEntity.ok(ApiResponse.success(run, "Production run details retrieved"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductionRun>> createRun(@RequestBody Map<String, Object> data) {
        ProductionRun run = productionService.createRun(data);
        return ResponseEntity.ok(ApiResponse.success(run, "Production run created with workflow stages"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductionRun>> updateRun(
            @PathVariable String id,
            @RequestBody Map<String, Object> data) {
        ProductionRun run = productionService.updateRun(id, data);
        return ResponseEntity.ok(ApiResponse.success(run, "Production run updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductionRun>> deleteRun(@PathVariable String id) {
        ProductionRun run = productionService.deleteRun(id);
        return ResponseEntity.ok(ApiResponse.success(run, "Production run deleted successfully"));
    }

    // ==========================================
    // STAGES & QUALITY CHECKS
    // ==========================================
    @PatchMapping("/{id}/stages/{stageName}")
    public ResponseEntity<ApiResponse<ProductionStage>> updateStage(
            @PathVariable String id,
            @PathVariable String stageName,
            @RequestBody Map<String, Object> data) {
        ProductionStage stage = productionService.updateStage(id, stageName.toUpperCase(), data);
        return ResponseEntity.ok(ApiResponse.success(stage, "Production stage updated successfully"));
    }

    @GetMapping("/{id}/quality-checks")
    public ResponseEntity<ApiResponse<List<QualityCheck>>> getQualityChecks(@PathVariable String id) {
        List<QualityCheck> checks = productionService.getQualityChecks(id);
        return ResponseEntity.ok(ApiResponse.success(checks, "Quality checks retrieved"));
    }

    @PostMapping("/quality-checks")
    public ResponseEntity<ApiResponse<QualityCheck>> createQualityCheck(@RequestBody Map<String, Object> data) {
        QualityCheck check = productionService.createQualityCheck(data);
        return ResponseEntity.ok(ApiResponse.success(check, "Quality check recorded successfully"));
    }
}
