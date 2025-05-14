package com.pos.call.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pos.call.entity.CallEntity;
import com.pos.call.repository.dsl.CallDslRepository;

@Repository
public interface CallJpaRepository extends JpaRepository<CallEntity, Long>, CallDslRepository {
}
