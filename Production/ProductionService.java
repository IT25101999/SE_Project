package com.texgarment.service;

import com.texgarment.exception.AppException;
import com.texgarment.model.*;
import com.texgarment.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProductionService {

    private final ProductionRunRepository runRepository;
    private final ProductionStageRepository stageRepository;
    private final QualityCheckRepository qualityCheckRepository;
    private final OrderRepository orderRepository;
    private final InventoryItemRepository itemRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationRepository notificationRepository;

    public ProductionService(ProductionRunRepository runRepository,
                             ProductionStageRepository stageRepository,
                             QualityCheckRepository qualityCheckRepository,
                             OrderRepository orderRepository,
                             InventoryItemRepository itemRepository,
                             EmployeeRepository employeeRepository,
                             NotificationRepository notificationRepository) {
        this.runRepository = runRepository;
        this.stageRepository = stageRepository;
        this.qualityCheckRepository = qualityCheckRepository;
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.employeeRepository = employeeRepository;
        this.notificationRepository = notificationRepository;
    }

    public Map<String, Object> getAllRuns(String search, String status, String priority, int page, int limit) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page - 1), limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProductionRun> runPage = runRepository.findFiltered(search, status, priority, pageRequest);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", runPage.getTotalElements());
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("totalPages", runPage.getTotalPages());

        Map<String, Object> response = new HashMap<>();
        response.put("runs", runPage.getContent());
        response.put("tasks", runPage.getContent().stream().map(this::mapRunToTaskFormat).toList());
        response.put("pagination", pagination);
        return response;
    }

    public ProductionRun getRunById(String id) {
        return runRepository.findById(id)
                .orElseThrow(() -> new AppException("Production run not found.", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public ProductionRun createRun(Map<String, Object> data) {
        String runCode = (String) data.get("runCode");
        if (runCode == null || runCode.isEmpty()) {
            runCode = "RUN-" + (int)(1000 + Math.random() * 9000);
        }

        ProductionRun run = new ProductionRun();
        run.setRunCode(runCode);
        run.setTitle((String) data.get("title"));
        run.setDescription((String) data.get("description"));

        if (data.containsKey("plannedQuantity") && data.get("plannedQuantity") != null) {
            run.setPlannedQuantity(Integer.parseInt(data.get("plannedQuantity").toString()));
        } else if (data.containsKey("targetQuantity") && data.get("targetQuantity") != null) {
            run.setPlannedQuantity(Integer.parseInt(data.get("targetQuantity").toString()));
        } else {
            run.setPlannedQuantity(100);
        }

        run.setPriority((String) data.getOrDefault("priority", "NORMAL"));
        run.setStatus("PLANNED");
        run.setStartDate(LocalDateTime.now());

        String orderId = (String) data.get("orderId");
        if (orderId != null && !orderId.isEmpty()) {
            Order order = orderRepository.findById(orderId).orElse(null);
            run.setOrder(order);
            run.setOrderId(orderId);
            if (order != null) {
                order.setStatus("IN_PRODUCTION");
                orderRepository.save(order);
            }
        }

        String productId = (String) data.get("productId");
        if (productId != null && !productId.isEmpty()) {
            InventoryItem product = itemRepository.findById(productId).orElse(null);
            run.setProduct(product);
            run.setProductId(productId);
        }

        String supervisorId = (String) data.get("supervisorEmployeeId");
        if (supervisorId == null) supervisorId = (String) data.get("assignedToId");
        if (supervisorId != null && !supervisorId.isEmpty()) {
            Employee emp = employeeRepository.findById(supervisorId).orElse(null);
            run.setSupervisorEmployee(emp);
            run.setSupervisorEmployeeId(supervisorId);
        }

        ProductionRun savedRun = runRepository.save(run);

        // Add 3 mandatory stages
        ProductionStage cutting = new ProductionStage();
        cutting.setProductionRun(savedRun);
        cutting.setProductionRunId(savedRun.getId());
        cutting.setStageName("CUTTING");
        cutting.setStageOrder(1);
        cutting.setStatus("PENDING");
        stageRepository.save(cutting);

        ProductionStage stitching = new ProductionStage();
        stitching.setProductionRun(savedRun);
        stitching.setProductionRunId(savedRun.getId());
        stitching.setStageName("STITCHING");
        stitching.setStageOrder(2);
        stitching.setStatus("PENDING");
        stageRepository.save(stitching);

        ProductionStage finishing = new ProductionStage();
        finishing.setProductionRun(savedRun);
        finishing.setProductionRunId(savedRun.getId());
        finishing.setStageName("FINISHING");
        finishing.setStageOrder(3);
        finishing.setStatus("PENDING");
        stageRepository.save(finishing);

        return savedRun;
    }

    @Transactional
    public ProductionRun updateRun(String id, Map<String, Object> data) {
        ProductionRun run = runRepository.findById(id)
                .orElseThrow(() -> new AppException("Production run not found.", HttpStatus.NOT_FOUND));

        if (data.containsKey("title") && data.get("title") != null) run.setTitle((String) data.get("title"));
        if (data.containsKey("description")) run.setDescription((String) data.get("description"));

        if (data.containsKey("plannedQuantity") && data.get("plannedQuantity") != null) {
            run.setPlannedQuantity(Integer.parseInt(data.get("plannedQuantity").toString()));
        } else if (data.containsKey("targetQuantity") && data.get("targetQuantity") != null) {
            run.setPlannedQuantity(Integer.parseInt(data.get("targetQuantity").toString()));
        }

        if (data.containsKey("producedQuantity") && data.get("producedQuantity") != null) {
            run.setProducedQuantity(Integer.parseInt(data.get("producedQuantity").toString()));
        } else if (data.containsKey("completedQuantity") && data.get("completedQuantity") != null) {
            run.setProducedQuantity(Integer.parseInt(data.get("completedQuantity").toString()));
        }

        if (data.containsKey("damagedQuantity") && data.get("damagedQuantity") != null) {
            run.setDamagedQuantity(Integer.parseInt(data.get("damagedQuantity").toString()));
        }

        if (data.containsKey("priority") && data.get("priority") != null) run.setPriority((String) data.get("priority"));
        if (data.containsKey("status") && data.get("status") != null) {
            String newStatus = (String) data.get("status");
            run.setStatus(newStatus);
            if ("COMPLETED".equals(newStatus) && run.getActualCompletionDate() == null) {
                run.setActualCompletionDate(LocalDateTime.now());
            }
        }

        if (data.containsKey("delayReason")) run.setDelayReason((String) data.get("delayReason"));
        if (data.containsKey("materialShortageNotes")) run.setMaterialShortageNotes((String) data.get("materialShortageNotes"));

        return runRepository.save(run);
    }

    @Transactional
    public ProductionRun updateProgress(String id, Map<String, Object> data) {
        ProductionRun run = runRepository.findById(id)
                .orElseThrow(() -> new AppException("Production run not found.", HttpStatus.NOT_FOUND));

        if (data.containsKey("completedQuantity") && data.get("completedQuantity") != null) {
            run.setProducedQuantity(Integer.parseInt(data.get("completedQuantity").toString()));
        }
        if (data.containsKey("status") && data.get("status") != null) {
            run.setStatus((String) data.get("status"));
        }
        if (data.containsKey("notes") && data.get("notes") != null) {
            run.setDescription(run.getDescription() != null ? run.getDescription() + " | " + data.get("notes") : (String) data.get("notes"));
        }

        return runRepository.save(run);
    }

    @Transactional
    public ProductionRun deleteRun(String id) {
        ProductionRun run = runRepository.findById(id)
                .orElseThrow(() -> new AppException("Production run not found.", HttpStatus.NOT_FOUND));
        runRepository.delete(run);
        return run;
    }

    // ==========================================
    // STAGES
    // ==========================================

    @Transactional
    public ProductionStage updateStage(String productionRunId, String stageName, Map<String, Object> data) {
        ProductionStage stage = stageRepository.findByProductionRunIdAndStageName(productionRunId, stageName)
                .orElseThrow(() -> new AppException("Production stage not found.", HttpStatus.NOT_FOUND));

        String status = (String) data.get("status");
        stage.setStatus(status);

        if (data.containsKey("assignedTeam")) stage.setAssignedTeam((String) data.get("assignedTeam"));
        if (data.containsKey("notes")) stage.setNotes((String) data.get("notes"));

        if ("IN_PROGRESS".equals(status) && stage.getStartedAt() == null) {
            stage.setStartedAt(LocalDateTime.now());
            ProductionRun run = stage.getProductionRun();
            if (run != null) {
                run.setStatus("IN_PROGRESS");
                runRepository.save(run);
            }
        }

        if ("COMPLETED".equals(status)) {
            stage.setCompletedAt(LocalDateTime.now());
        }

        return stageRepository.save(stage);
    }

    // ==========================================
    // QUALITY CHECKS
    // ==========================================

    @Transactional
    public QualityCheck createQualityCheck(Map<String, Object> data) {
        String runId = (String) data.get("productionRunId");
        ProductionRun run = runRepository.findById(runId)
                .orElseThrow(() -> new AppException("Production run not found.", HttpStatus.BAD_REQUEST));

        QualityCheck check = new QualityCheck();
        check.setProductionRun(run);
        check.setProductionRunId(runId);
        check.setStageName((String) data.get("stageName"));
        check.setInspectorName((String) data.get("inspectorName"));
        check.setSamplesChecked(Integer.parseInt(data.get("samplesChecked").toString()));
        check.setPassedQuantity(Integer.parseInt(data.get("passedQuantity").toString()));
        check.setRejectedQuantity(data.containsKey("rejectedQuantity") && data.get("rejectedQuantity") != null
                ? Integer.parseInt(data.get("rejectedQuantity").toString()) : 0);
        check.setDefectType((String) data.get("defectType"));
        check.setDefectDescription((String) data.get("defectDescription"));
        check.setStatus((String) data.getOrDefault("status", "PASSED"));
        check.setRemarks((String) data.get("remarks"));

        return qualityCheckRepository.save(check);
    }

    public List<QualityCheck> getQualityChecks(String productionRunId) {
        return qualityCheckRepository.findByProductionRunIdOrderByInspectionDateDesc(productionRunId);
    }

    // ==========================================
    // LINES (For Factory Floor page)
    // ==========================================

    public List<Map<String, Object>> getProductionLines() {
        List<Map<String, Object>> lines = new ArrayList<>();

        Map<String, Object> line1 = new HashMap<>();
        line1.put("id", "line-cutting-01");
        line1.put("code", "LINE-01");
        line1.put("name", "Cutting & Preparation Unit");
        line1.put("description", "High-precision automated CAD fabric cutting tables");
        line1.put("capacityPerDay", 1000);
        line1.put("status", "ACTIVE");

        Map<String, Object> line2 = new HashMap<>();
        line2.put("id", "line-sewing-01");
        line2.put("code", "LINE-02");
        line2.put("name", "Stitching & Assembly Unit");
        line2.put("description", "Heavy duty lockstitch and overlock sewing lines");
        line2.put("capacityPerDay", 750);
        line2.put("status", "ACTIVE");

        Map<String, Object> line3 = new HashMap<>();
        line3.put("id", "line-finishing-01");
        line3.put("code", "LINE-03");
        line3.put("name", "Finishing & Packaging Bay");
        line3.put("description", "Steam iron pressing, tag attachment, and export boxing");
        line3.put("capacityPerDay", 1200);
        line3.put("status", "ACTIVE");

        lines.add(line1);
        lines.add(line2);
        lines.add(line3);
        return lines;
    }

    private Map<String, Object> mapRunToTaskFormat(ProductionRun run) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", run.getId());
        map.put("title", run.getTitle());
        map.put("description", run.getDescription());
        map.put("targetQuantity", run.getPlannedQuantity());
        map.put("completedQuantity", run.getProducedQuantity());
        map.put("status", run.getStatus());
        map.put("startDate", run.getStartDate());
        map.put("endDate", run.getExpectedCompletionDate());
        map.put("order", run.getOrder());
        map.put("orderId", run.getOrderId());
        map.put("assignedTo", run.getSupervisorEmployee());
        map.put("assignedToId", run.getSupervisorEmployeeId());

        Map<String, Object> line = new HashMap<>();
        line.put("id", "line-sewing-01");
        line.put("name", "Assembly Line 01");
        map.put("productionLine", line);
        map.put("productionLineId", "line-sewing-01");

        return map;
    }
}
