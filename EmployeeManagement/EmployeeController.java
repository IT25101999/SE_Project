package com.texgarment.controller;

import com.texgarment.model.Attendance;
import com.texgarment.model.Department;
import com.texgarment.model.Employee;
import com.texgarment.model.Task;
import com.texgarment.service.EmployeeService;
import com.texgarment.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // ==========================================
    // DEPARTMENTS (Must be before /{id})
    // ==========================================
    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<Department>>> getAllDepartments() {
        List<Department> departments = employeeService.getAllDepartments();
        return ResponseEntity.ok(ApiResponse.success(departments, "Departments retrieved successfully"));
    }

    @PostMapping("/departments")
    public ResponseEntity<ApiResponse<Department>> createDepartment(@RequestBody Map<String, Object> data) {
        Department dept = employeeService.createDepartment(data);
        return ResponseEntity.ok(ApiResponse.success(dept, "Department created successfully"));
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<ApiResponse<Department>> updateDepartment(@PathVariable String id, @RequestBody Map<String, Object> data) {
        Department dept = employeeService.updateDepartment(id, data);
        return ResponseEntity.ok(ApiResponse.success(dept, "Department updated successfully"));
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<ApiResponse<Department>> deleteDepartment(@PathVariable String id) {
        Department dept = employeeService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success(dept, "Department deleted successfully"));
    }

    // ==========================================
    // ATTENDANCE (Must be before /{id})
    // ==========================================
    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAttendance(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Map<String, Object> result = employeeService.getAttendanceRecords(employeeId, date, page, limit);
        return ResponseEntity.ok(ApiResponse.success(result, "Attendance records retrieved"));
    }

    @PostMapping("/attendance")
    public ResponseEntity<ApiResponse<Attendance>> recordAttendance(@RequestBody Map<String, Object> data) {
        Attendance attendance = employeeService.recordAttendance(data);
        return ResponseEntity.ok(ApiResponse.success(attendance, "Attendance recorded successfully"));
    }

    // ==========================================
    // TASKS (Must be before /{id})
    // ==========================================
    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<List<Task>>> getTasks(
            @RequestParam(required = false) String assignedToEmployeeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {
        List<Task> tasks = employeeService.getTasks(assignedToEmployeeId, status, priority);
        return ResponseEntity.ok(ApiResponse.success(tasks, "Tasks retrieved"));
    }

    @PostMapping("/tasks")
    public ResponseEntity<ApiResponse<Task>> createTask(@RequestBody Map<String, Object> data) {
        Task task = employeeService.createTask(data);
        return ResponseEntity.ok(ApiResponse.success(task, "Task created successfully"));
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<Task>> updateTask(@PathVariable String id, @RequestBody Map<String, Object> data) {
        Task task = employeeService.updateTask(id, data);
        return ResponseEntity.ok(ApiResponse.success(task, "Task updated successfully"));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<Task>> deleteTask(@PathVariable String id) {
        Task task = employeeService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success(task, "Task deleted successfully"));
    }

    // ==========================================
    // EMPLOYEES CRUD
    // ==========================================
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = employeeService.getAllEmployees(search, departmentId, status, page, limit);
        return ResponseEntity.ok(ApiResponse.success(result, "Employees retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Employee>> getEmployeeById(@PathVariable String id) {
        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success(employee, "Employee details retrieved"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Employee>> createEmployee(@RequestBody Map<String, Object> data) {
        Employee employee = employeeService.createEmployee(data);
        return ResponseEntity.ok(ApiResponse.success(employee, "Employee created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Employee>> updateEmployee(@PathVariable String id, @RequestBody Map<String, Object> data) {
        Employee employee = employeeService.updateEmployee(id, data);
        return ResponseEntity.ok(ApiResponse.success(employee, "Employee updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Employee>> deleteEmployee(@PathVariable String id) {
        Employee employee = employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success(employee, "Employee deleted successfully"));
    }
}
