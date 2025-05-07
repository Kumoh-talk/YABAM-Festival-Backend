package com.pos.review.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pos.review.entity.ReviewEntity;
import com.pos.review.repository.dsl.ReviewDslRepository;

@Repository
public interface ReviewJpaRepository extends JpaRepository<ReviewEntity, Long>, ReviewDslRepository {
}
