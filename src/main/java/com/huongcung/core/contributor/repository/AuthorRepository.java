package com.huongcung.core.contributor.repository;

import com.huongcung.core.contributor.model.entity.AuthorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<AuthorEntity, Long> {
    List<AuthorEntity> findByIdIn(List<Long> ids);
    org.springframework.data.domain.Page<AuthorEntity> findByNameContainingIgnoreCase(String name, org.springframework.data.domain.Pageable pageable);
}


