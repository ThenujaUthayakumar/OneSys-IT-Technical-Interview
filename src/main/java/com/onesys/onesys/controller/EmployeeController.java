package com.onesys.onesys.controller;

import com.onesys.onesys.dto.Employee;
import com.onesys.onesys.entity.EmployeeEntity;
import com.onesys.onesys.service.EmployeeService;
import com.onesys.onesys.util.JwtUtil;
import com.onesys.onesys.util.LoggedInUser;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;

@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;
    private final JwtUtil jwtUtil;
    private final LoggedInUser loggedInUser;

    /*-------------------- EMPLOYEE CREATE ---------------------------*/
    @PostMapping("/store")
    public EmployeeEntity store(@RequestBody Employee employee,@RequestHeader("Authorization") String token) throws ParseException {
        loggedInUser.validateToken(token);
        return employeeService.store(employee);
    }

    /*------------------- EMPLOYEE GET --------------------------------*/
    @GetMapping("/get-all")
    public Page<EmployeeEntity> getEmployee(@RequestParam(required = false) Integer skip, @RequestParam(required = false) Integer limit, @RequestParam(required = false) String orderBy, Employee employee,@RequestHeader("Authorization") String token) {
        loggedInUser.validateToken(token);
        return employeeService.getAll(skip, limit, orderBy, employee);
    }

    /*--------------- EMPLOYEE UPDATE --------------------------*/
    @PutMapping("/update")
    public EmployeeEntity update(@RequestBody Employee employee,@RequestHeader("Authorization") String token) throws ParseException {
        loggedInUser.validateToken(token);
        return employeeService.update(employee);
    }

    /*------------------- EMPLOYEE PROFILE UPLOAD ------------------*/
    @PutMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam Integer id, @RequestParam("file") MultipartFile file,@RequestHeader("Authorization") String token) {
        loggedInUser.validateToken(token);
        return employeeService.upload(id, file);
    }

    /*------------------ EMPLOYEE PROFILE DOWNLOAD ------------------*/
    @GetMapping("/{id}/download-profile")
    public ResponseEntity<Resource> downloadProfilePicture(@PathVariable Integer id,@RequestHeader("Authorization") String token) {
        loggedInUser.validateToken(token);
        try {
            Resource resource = employeeService.getProfilePicture(id);
            if (resource == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    /*------------------ EMPLOYEE ALL RECORDS DOWNLOAD ------------------*/
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadEmployeeReport(@RequestHeader("Authorization") String token) {
        loggedInUser.validateToken(token);
        try {
            byte[] pdfContents = employeeService.generateEmployeeReport();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/pdf");
            headers.add("Content-Disposition", "attachment; filename=employee_report.pdf");

            return new ResponseEntity<>(pdfContents, headers, HttpStatus.OK);
        } catch (JRException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    /*--------------------- EMPLOYEE DELETE -----------------------*/
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Integer id,@RequestHeader("Authorization") String token) {
        loggedInUser.validateToken(token);
        try {
            String responseMessage = employeeService.deleteEmployee(id,token);
            return ResponseEntity.ok(responseMessage);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while trying to delete the employee record !");
        }
    }
}
