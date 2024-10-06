package com.onesys.onesys.repository;

import com.onesys.onesys.entity.RoleEntity;
import com.onesys.onesys.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    UserEntity findByUsername(String username);

    boolean existsByUsername(String username);

    @Query(value = """
        SELECT u.* FROM users u
        WHERE (:id IS NULL OR u.id = :id)
        AND (:firstName IS NULL OR u.first_name = :firstName)
        AND (:lastName IS NULL OR u.last_name = :lastName)
        AND (:username IS NULL OR u.username = :username)
        AND (:email IS NULL OR u.email = :email)
        AND (:roleId IS NULL OR u.role_id = :roleId)
        AND (:searchLike IS NULL OR 
            u.first_name LIKE :searchLike OR
            u.last_name LIKE :searchLike OR
            u.username LIKE :searchLike OR
            u.email LIKE :searchLike
        ) """,countQuery = """
        SELECT COUNT(u.id) FROM users u
        WHERE (:id IS NULL OR u.id = :id)
        AND (:firstName IS NULL OR u.first_name = :firstName)
        AND (:lastName IS NULL OR u.last_name = :lastName)
        AND (:username IS NULL OR u.username = :username)
        AND (:email IS NULL OR u.email = :email)
        AND (:roleId IS NULL OR u.role_id = :roleId)
        AND (:searchLike IS NULL OR 
            u.first_name LIKE :searchLike OR
            u.last_name LIKE :searchLike OR
            u.username LIKE :searchLike OR
            u.email LIKE :searchLike
    )
        """, nativeQuery = true)
    Page<UserEntity> findByColumns(Pageable pageable, Integer id, String firstName, String lastName,
                                   String username, String email, RoleEntity roleId,String searchLike);
}
