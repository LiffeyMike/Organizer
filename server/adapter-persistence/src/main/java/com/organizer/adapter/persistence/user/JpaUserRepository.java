package com.organizer.adapter.persistence.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface JpaUserRepository extends JpaRepository<JpaUser, UUID> {
  Optional<JpaUser> findByEmail(String email);

  boolean existsByEmail(String email);
}
