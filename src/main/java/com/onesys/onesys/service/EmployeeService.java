package com.onesys.onesys.service;

import com.onesys.onesys.dto.Employee;
import com.onesys.onesys.entity.EmployeeEntity;
import com.onesys.onesys.entity.UserEntity;
import com.onesys.onesys.repository.EmployeeRepository;
import com.onesys.onesys.util.LoggedInUser;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    @Value("${employee.profile.picture.path}")
    private String profilePictureDirectory;

    private final EmployeeRepository employeeRepository;
    private final LoggedInUser loggedInUser;

    /*--------------- CREATE IMAGE FOLDER -----------------*/
    @PostConstruct
    public void init() {
        try {
            Path directoryPath = Paths.get(profilePictureDirectory);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
                System.out.println("Directory created: " + profilePictureDirectory);
            } else {
                System.out.println("Directory already exists: " + profilePictureDirectory);
            }
        } catch (IOException e) {
            System.err.println("Error creating directory: " + profilePictureDirectory);
            e.printStackTrace();
        }
    }

    /*---------- SCHEDULED FOR UPDATE THE AGE ----------------*/
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateEmployeeAges() {
        List<EmployeeEntity> employeeEntities = employeeRepository.findAll();
        for (EmployeeEntity employeeEntity : employeeEntities) {
            Integer ageInDays = calculateAgeInDays(employeeEntity.getDateOfBirth());
            employeeEntity.setCurrentAgeInDays(ageInDays);
        }
        employeeRepository.saveAll(employeeEntities);
    }

    private Integer calculateAgeInDays(String dateOfBirth) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dob = LocalDate.parse(dateOfBirth, formatter);
        return (int) (LocalDate.now().toEpochDay() - dob.toEpochDay());
    }
    /*-------------------- EMPLOYEE CREATE ---------------------------*/
    @Transactional
    public EmployeeEntity store(Employee employee) throws ParseException {
        EmployeeEntity employeeEntity=new EmployeeEntity();

        if(employee.getFirstName() == null || employee.getFirstName().isEmpty())
        {
            throw new IllegalStateException("Please Enter First Name !");
        }

        if(employee.getLastName() == null || employee.getLastName().isEmpty())
        {
            throw new IllegalStateException("Please Enter Last Name !");
        }

        if(employee.getAddress() == null || employee.getAddress().isEmpty())
        {
            throw new IllegalStateException("Please Enter Address !");
        }

        if(employee.getContactNumber() == null || employee.getContactNumber().isEmpty())
        {
            throw new IllegalStateException("Please Enter Contact Number !");
        }

        if(employee.getEmail() == null || employee.getEmail().isEmpty())
        {
            throw new IllegalStateException("Please Enter Patient E-mail !");
        }

        if(employee.getDateOfBirth() == null || employee.getDateOfBirth().isEmpty())
        {
            throw new IllegalStateException("Please Enter Date Of Birth !");
        }

        if(employee.getGender() == null || employee.getGender().isEmpty())
        {
            throw new IllegalStateException("Please Select Gender !");
        }

        employeeEntity.setFirstName(employee.getFirstName());
        employeeEntity.setLastName(employee.getLastName());
        employeeEntity.setAddress(employee.getAddress());
        employeeEntity.setContactNumber(employee.getContactNumber());
        employeeEntity.setEmail(employee.getEmail());
        employeeEntity.setDateOfBirth(employee.getDateOfBirth());
        employeeEntity.setReferenceNumber(this.getReferencenumber());
        employeeEntity.setGender(employee.getGender());

        return employeeRepository.save(employeeEntity);
    }

    /*------------------------ SET AUTO REFERENCE NUMBER FOR EMPLOYEES ------------------*/
    public String getReferencenumber(){
        String referenceNumber=employeeRepository.findReferenceNumber();
        if(referenceNumber==null){
            return "EMP1";
        }
        else{
            String[] splitString=referenceNumber.split("EMP");
            int newReferenceNumber=Integer.valueOf(splitString[1])+1;
            String finalReferenceNumber="EMP"+newReferenceNumber;
            return finalReferenceNumber;
        }
    }

    /*------------------- EMPLOYEE GET --------------------------------*/
    public Page<EmployeeEntity> getAll(Integer pageNo, Integer pageSize, String orderBy, Employee employee) {

        Pageable pageable = null;
        List<Sort.Order> sorts = new ArrayList<>();
        if (orderBy != null) {
            String[] split = orderBy.split("&");
            for (String s : split) {
                String[] orders = s.split(",");
                sorts.add(new Sort.Order(Sort.Direction.valueOf(orders[1]), orders[0]));
            }
        }
        if (pageNo != null && pageSize != null) {
            if (orderBy != null) {
                pageable = PageRequest.of(pageNo, pageSize, Sort.by(sorts));
            } else {
                pageable = PageRequest.of(pageNo, pageSize);
            }
        } else {
            if (orderBy != null) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(sorts));
            }
        }

        String searchLike = null;
        if(employee.getSearchLike() != null){
            searchLike = "%"+employee.getSearchLike()+"%";
        }
        Page<EmployeeEntity> employeeEntities;

        employeeEntities = employeeRepository.findByColumns(pageable,
                        employee.getId(),
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getAddress(),
                        employee.getEmail(),
                        employee.getCurrentAgeInDays(),
                        employee.getReferenceNumber(),
                        employee.getContactNumber(),
                        employee.getDateOfBirth(),
                        employee.getGender(),
                        searchLike);
        return employeeEntities;
    }

    /*--------------- EMPLOYEE UPDATE --------------------------*/
    @Transactional
    public EmployeeEntity update(Employee employee) throws ParseException {
        EmployeeEntity employeeEntity = employeeRepository.findById(employee.getId()).orElseThrow(()-> new IllegalStateException(
                "Employee with id " + employee.getId() + "Doesn't Exists !"
        ));

        if(employee.getFirstName() == null || employee.getFirstName().isEmpty())
        {
            throw new IllegalStateException("Please Enter First Name !");
        }

        if(employee.getLastName() == null || employee.getLastName().isEmpty())
        {
            throw new IllegalStateException("Please Enter Last Name !");
        }

        if(employee.getAddress() == null || employee.getAddress().isEmpty())
        {
            throw new IllegalStateException("Please Enter Address !");
        }

        if(employee.getContactNumber() == null || employee.getContactNumber().isEmpty())
        {
            throw new IllegalStateException("Please Enter Contact Number !");
        }

        if(employee.getEmail() == null || employee.getEmail().isEmpty())
        {
            throw new IllegalStateException("Please Enter Patient E-mail !");
        }

        if(employee.getDateOfBirth() == null || employee.getDateOfBirth().isEmpty())
        {
            throw new IllegalStateException("Please Enter Date Of Birth !");
        }

        if(employee.getGender() == null || employee.getGender().isEmpty())
        {
            throw new IllegalStateException("Please Select Gender !");
        }

        employeeEntity.setFirstName(employee.getFirstName());
        employeeEntity.setLastName(employee.getLastName());
        employeeEntity.setAddress(employee.getAddress());
        employeeEntity.setContactNumber(employee.getContactNumber());
        employeeEntity.setEmail(employee.getEmail());
        employeeEntity.setDateOfBirth(employee.getDateOfBirth());
        employeeEntity.setGender(employee.getGender());

        return employeeRepository.save(employeeEntity);
    }

    /*------------------- EMPLOYEE PROFILE UPLOAD ------------------*/
    public ResponseEntity<String> upload(Integer id, MultipartFile file) {
        try {
            if (!file.getContentType().startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File must be an Image !");
            }

            Path directoryPath = Paths.get(profilePictureDirectory);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            EmployeeEntity employeeEntity = employeeRepository.findById(id)
                    .orElseThrow(() -> new IllegalStateException("Employee Not found !"));
            String referenceNumber = employeeEntity.getReferenceNumber();

            String fileName = "employee_" + referenceNumber + "_profile" + getFileExtension(file.getOriginalFilename());
            Path filePath = directoryPath.resolve(fileName);
            file.transferTo(filePath.toFile());

            updateProfilePicture(id, fileName);

            return ResponseEntity.ok("Profile picture uploaded successfully !");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading profile picture: " + e.getMessage());
        }
    }

    public void updateProfilePicture(Integer id, String profilePictureFileName) {
        EmployeeEntity employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Employee Not Found !"));
        employee.setAttachment(profilePictureFileName);
        employeeRepository.save(employee);
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /*------------------ EMPLOYEE PROFILE DOWNLOAD ------------------*/
    public Resource getProfilePicture(Integer id) throws Exception {
        EmployeeEntity employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Employee Not Found !"));

        String profilePictureFileName = employee.getAttachment();

        Path filePath = Paths.get(profilePictureDirectory).resolve(profilePictureFileName).normalize();

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            return null;
        }

        return resource;
    }

    /*------------------ EMPLOYEE ALL RECORDS DOWNLOAD ------------------*/
    public byte[] generateEmployeeReport() throws JRException {
        List<EmployeeEntity> employees = employeeRepository.findAll();
        System.out.println("Employees: " + employees);

        JasperReport jasperReport = JasperCompileManager.compileReport("src/main/resources/reports/employee_report.jrxml");

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(employees);

        Map<String, Object> parameters = new HashMap<>();

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

        return outputStream.toByteArray();
    }

    /*--------------------- EMPLOYEE DELETE -----------------------*/
    public String deleteEmployee(Integer id, String token) {
        System.out.println("Token Received: " + token);
        UserEntity currentUser = loggedInUser.validateToken(token);
        System.out.println("User Retrieved: " + currentUser.getUsername() + ", Role ID: " + currentUser.getRoleId());

//        if (!currentUser.getRoleId().equals(1)) {
//            throw new SecurityException("Access denied: You do not have permission to delete an employee record!");
//        }
        if (!Integer.valueOf(1).equals(currentUser.getRoleId())) {
            throw new SecurityException("Access denied: You do not have permission to delete an employee record!");
        }


        if (!employeeRepository.existsById(id)) {
            throw new SecurityException("Employee not found with id: " + id);
        }

        EmployeeEntity employeeEntity = employeeRepository.findById(id).get();
        employeeRepository.delete(employeeEntity);

        return "Employee record deleted successfully!";
    }

}
