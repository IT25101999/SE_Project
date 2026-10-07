package com.texgarment.service;

import com.texgarment.exception.AppException;
import com.texgarment.model.Attendance;
import com.texgarment.model.Department;
import com.texgarment.model.Employee;
import com.texgarment.model.Task;
import com.texgarment.repository.AttendanceRepository;
import com.texgarment.repository.DepartmentRepository;
import com.texgarment.repository.EmployeeRepository;
import com.texgarment.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final TaskRepository taskRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           AttendanceRepository attendanceRepository,
                           TaskRepository taskRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.taskRepository = taskRepository;
    }

    public Map<String, Object> getAllEmployees(String search, String departmentId, String status, int page, int limit) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page - 1), limit, Sort.by(Sort.Direction.ASC, "employeeNumber"));
        Page<Employee> employeePage = employeeRepository.findFiltered(search, departmentId, status, pageRequest);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", employeePage.getTotalElements());
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("totalPages", employeePage.getTotalPages());

        Map<String, Object> response = new HashMap<>();
        response.put("employees", employeePage.getContent());
        response.put("pagination", pagination);
        return response;
    }

    public Employee getEmployeeById(String id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new AppException("Employee not found.", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Employee createEmployee(Map<String, Object> data) {
        String empNum = (String) data.get("employeeNumber");
        if (empNum != null && employeeRepository.findByEmployeeNumber(empNum).isPresent()) {
            throw new AppException("Employee number already exists.", HttpStatus.CONFLICT);
        }

        Employee employee = new Employee();
        employee.setEmployeeNumber(empNum);
        employee.setFirstName((String) data.get("firstName"));
        employee.setLastName((String) data.get("lastName"));

        String email = (String) data.get("email");
        if (email == null || email.trim().isEmpty()) {
            email = empNum != null ? empNum.toLowerCase() + "@texgarment.local" : "emp@texgarment.local";
        }
        employee.setEmail(email);
        employee.setPhone((String) data.get("phone"));

        String desig = (String) data.get("designation");
        if (desig == null) desig = (String) data.get("jobTitle");
        employee.setDesignation(desig != null ? desig : "Technician");

        String deptId = (String) data.get("departmentId");
        if (deptId != null && !deptId.isEmpty()) {
            Department dept = departmentRepository.findById(deptId)
                    .orElseThrow(() -> new AppException("Department not found.", HttpStatus.BAD_REQUEST));
            employee.setDepartment(dept);
            employee.setDepartmentId(deptId);
        }

        if (data.containsKey("salary") && data.get("salary") != null) {
            employee.setSalary(new BigDecimal(data.get("salary").toString()));
        }

        employee.setStatus((String) data.getOrDefault("status", "ACTIVE"));
        employee.setUserId((String) data.get("userId"));

        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(String id, Map<String, Object> data) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException("Employee not found.", HttpStatus.NOT_FOUND));

        if (data.containsKey("firstName") && data.get("firstName") != null) employee.setFirstName((String) data.get("firstName"));
        if (data.containsKey("lastName") && data.get("lastName") != null) employee.setLastName((String) data.get("lastName"));
        if (data.containsKey("email") && data.get("email") != null) employee.setEmail((String) data.get("email"));
        if (data.containsKey("phone")) employee.setPhone((String) data.get("phone"));

        String desig = (String) data.get("designation");
        if (desig == null) desig = (String) data.get("jobTitle");
        if (desig != null) employee.setDesignation(desig);

        if (data.containsKey("departmentId") && data.get("departmentId") != null) {
            String deptId = (String) data.get("departmentId");
            Department dept = departmentRepository.findById(deptId)
                    .orElseThrow(() -> new AppException("Department not found.", HttpStatus.BAD_REQUEST));
            employee.setDepartment(dept);
            employee.setDepartmentId(deptId);
        }

        if (data.containsKey("salary") && data.get("salary") != null) {
            employee.setSalary(new BigDecimal(data.get("salary").toString()));
        }

        if (data.containsKey("status") && data.get("status") != null) employee.setStatus((String) data.get("status"));
        if (data.containsKey("userId")) employee.setUserId((String) data.get("userId"));

        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee deleteEmployee(String id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException("Employee not found.", HttpStatus.NOT_FOUND));
        employeeRepository.delete(employee);
        return employee;
    }

    // ==========================================
    // DEPARTMENTS
    // ==========================================

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Transactional
    public Department createDepartment(Map<String, Object> data) {
        Department dept = new Department();
        dept.setName((String) data.get("name"));
        dept.setCode((String) data.get("code"));
        dept.setDescription((String) data.get("description"));
        dept.setManagerName((String) data.get("managerName"));
        return departmentRepository.save(dept);
    }

    @Transactional
    public Department updateDepartment(String id, Map<String, Object> data) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new AppException("Department not found.", HttpStatus.NOT_FOUND));
        if (data.containsKey("name") && data.get("name") != null) dept.setName((String) data.get("name"));
        if (data.containsKey("code") && data.get("code") != null) dept.setCode((String) data.get("code"));
        if (data.containsKey("description")) dept.setDescription((String) data.get("description"));
        if (data.containsKey("managerName")) dept.setManagerName((String) data.get("managerName"));
        return departmentRepository.save(dept);
    }

    @Transactional
    public Department deleteDepartment(String id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new AppException("Department not found.", HttpStatus.NOT_FOUND));
        departmentRepository.delete(dept);
        return dept;
    }

    // ==========================================
    // ATTENDANCE
    // ==========================================

    public Map<String, Object> getAttendanceRecords(String employeeId, String dateStr, int page, int limit) {
        List<Attendance> list;
        if (dateStr != null && !dateStr.isEmpty()) {
            LocalDate date = LocalDate.parse(dateStr.substring(0, 10));
            list = attendanceRepository.findByDate(date);
        } else if (employeeId != null && !employeeId.isEmpty()) {
            list = attendanceRepository.findByEmployeeIdOrderByDateDesc(employeeId);
        } else {
            list = attendanceRepository.findAll();
        }

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", list.size());
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("totalPages", 1);

        Map<String, Object> response = new HashMap<>();
        response.put("attendances", list);
        response.put("pagination", pagination);
        return response;
    }

    @Transactional
    public Attendance recordAttendance(Map<String, Object> data) {
        String empId = (String) data.get("employeeId");
        if (empId == null || empId.isEmpty()) {
            throw new AppException("employeeId is required.", HttpStatus.BAD_REQUEST);
        }

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new AppException("Employee not found.", HttpStatus.NOT_FOUND));

        LocalDate date = LocalDate.now();
        if (data.containsKey("date") && data.get("date") != null) {
            String d = (String) data.get("date");
            date = LocalDate.parse(d.substring(0, 10));
        }

        Optional<Attendance> existingOpt = attendanceRepository.findByEmployeeIdAndDate(empId, date);
        Attendance attendance = existingOpt.orElse(new Attendance());

        attendance.setEmployee(employee);
        attendance.setEmployeeId(empId);
        attendance.setDate(date);
        attendance.setStatus((String) data.getOrDefault("status", "PRESENT"));
        attendance.setNotes((String) data.get("notes"));

        if (attendance.getCheckInTime() == null) {
            attendance.setCheckInTime(LocalDateTime.now());
        }

        if (data.containsKey("checkOutTime") && data.get("checkOutTime") != null) {
            attendance.setCheckOutTime(LocalDateTime.now());
        }

        return attendanceRepository.save(attendance);
    }

    // ==========================================
    // TASKS
    // ==========================================

    public List<Task> getTasks(String assignedToEmployeeId, String status, String priority) {
        if (assignedToEmployeeId != null && !assignedToEmployeeId.isEmpty()) {
            return taskRepository.findByAssignedToEmployeeId(assignedToEmployeeId);
        }
        if (status != null && !status.isEmpty()) {
            return taskRepository.findByStatus(status);
        }
        return taskRepository.findAll();
    }

    @Transactional
    public Task createTask(Map<String, Object> data) {
        String empId = (String) data.get("assignedToEmployeeId");
        Employee emp = employeeRepository.findById(empId)
                .orElseThrow(() -> new AppException("Assigned employee not found.", HttpStatus.BAD_REQUEST));

        Task task = new Task();
        task.setTitle((String) data.get("title"));
        task.setDescription((String) data.get("description"));
        task.setAssignedToEmployee(emp);
        task.setAssignedToEmployeeId(empId);
        task.setPriority((String) data.getOrDefault("priority", "MEDIUM"));
        task.setStatus((String) data.getOrDefault("status", "PENDING"));

        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(String id, Map<String, Object> data) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new AppException("Task not found.", HttpStatus.NOT_FOUND));

        if (data.containsKey("title") && data.get("title") != null) task.setTitle((String) data.get("title"));
        if (data.containsKey("description")) task.setDescription((String) data.get("description"));
        if (data.containsKey("priority") && data.get("priority") != null) task.setPriority((String) data.get("priority"));
        if (data.containsKey("status") && data.get("status") != null) {
            task.setStatus((String) data.get("status"));
            if ("COMPLETED".equals(data.get("status"))) {
                task.setCompletedAt(LocalDateTime.now());
            }
        }

        return taskRepository.save(task);
    }

    @Transactional
    public Task deleteTask(String id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new AppException("Task not found.", HttpStatus.NOT_FOUND));
        taskRepository.delete(task);
        return task;
    }
}
