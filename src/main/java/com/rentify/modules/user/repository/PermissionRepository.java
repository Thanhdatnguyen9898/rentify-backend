package com.rentify.modules.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.rentify.modules.user.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>,
    JpaSpecificationExecutor<Permission> {
    boolean existsByApiPathAndMethod(String apiPath, String method);
}