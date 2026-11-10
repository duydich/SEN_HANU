package com.example.demo.repository;

import com.example.demo.entity.AppUser;
import org.springframework.data.jpa.repository.*;

public interface AppUserRepository
  extends JpaRepository<AppUser, Long>, JpaSpecificationExecutor<AppUser>
{
  java.util.Optional<AppUser> findByUsername(String username);
  boolean existsByUsername(String username);
}
