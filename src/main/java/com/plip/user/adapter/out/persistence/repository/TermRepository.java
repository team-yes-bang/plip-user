package com.plip.user.adapter.out.persistence.repository;

import com.plip.user.adapter.out.persistence.entity.TermEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TermRepository extends JpaRepository<TermEntity, Long> {

	List<TermEntity> findByStatusAndRequiredTrue(String status);

	List<TermEntity> findByIdIn(List<Long> ids);
}
