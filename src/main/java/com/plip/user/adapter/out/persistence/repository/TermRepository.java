package com.plip.user.adapter.out.persistence.repository;

import com.plip.user.adapter.out.persistence.entity.TermEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<TermEntity, Long> {
}
