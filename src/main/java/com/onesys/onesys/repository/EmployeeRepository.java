package com.onesys.onesys.repository;

import com.onesys.onesys.entity.EmployeeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity,Integer> {
    @Query(value = """
            SELECT reference_number FROM employees 
            WHERE reference_number IS NOT NULL ORDER BY id DESC LIMIT 1
            """,nativeQuery = true)
    String findReferenceNumber();

    @Query(value = """
        SELECT emp.* FROM employees emp
        WHERE (:id IS NULL OR emp.id = :id)
        AND (:firstName IS NULL OR emp.first_name = :firstName)
        AND (:lastName IS NULL OR emp.last_name = :lastName)
        AND (:address IS NULL OR emp.address = :address)
        AND (:email IS NULL OR emp.email = :email)
        AND (:currentAgeInDays IS NULL OR emp.current_age_in_days = :currentAgeInDays)
        AND (:referenceNumber IS NULL OR emp.reference_number = :referenceNumber)
        AND (:contactNumber IS NULL OR emp.contact_number = :contactNumber)
        AND (:dateOfBirth IS NULL OR emp.date_of_birth = :dateOfBirth)
        AND (:gender IS NULL OR emp.gender = :gender)
        AND (:searchLike IS NULL OR 
            emp.first_name LIKE :searchLike OR
            emp.last_name LIKE :searchLike OR
            emp.address LIKE :searchLike OR
            emp.email LIKE :searchLike OR
            emp.gender LIKE :searchLike OR
            emp.reference_number LIKE :searchLike OR
            emp.contact_number LIKE :searchLike
        ) """,countQuery = """
        SELECT COUNT(emp.id) FROM employees emp
        WHERE (:id IS NULL OR emp.id = :id)
        AND (:firstName IS NULL OR emp.first_name = :firstName)
        AND (:lastName IS NULL OR emp.last_name = :lastName)
        AND (:address IS NULL OR emp.address = :address)
        AND (:email IS NULL OR emp.email = :email)
        AND (:currentAgeInDays IS NULL OR emp.current_age_in_days = :currentAgeInDays)
        AND (:referenceNumber IS NULL OR emp.reference_number = :referenceNumber)
        AND (:contactNumber IS NULL OR emp.contact_number = :contactNumber)
        AND (:dateOfBirth IS NULL OR emp.date_of_birth = :dateOfBirth)
        AND (:gender IS NULL OR emp.gender = :gender)
        AND (:searchLike IS NULL OR 
            emp.first_name LIKE :searchLike OR
            emp.last_name LIKE :searchLike OR
            emp.address LIKE :searchLike OR
            emp.email LIKE :searchLike OR
            emp.gender LIKE :searchLike OR
            emp.reference_number LIKE :searchLike OR
            emp.contact_number LIKE :searchLike
    )
        """, nativeQuery = true)
    Page<EmployeeEntity> findByColumns(Pageable pageable, Integer id, String firstName, String lastName,
                                       String address,String email, String currentAgeInDays, String referenceNumber, String contactNumber,
                                       String dateOfBirth, String gender, String searchLike);
}
