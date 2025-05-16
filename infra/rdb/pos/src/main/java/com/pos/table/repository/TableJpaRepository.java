package com.pos.table.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pos.table.entity.TableEntity;
import com.pos.table.repository.querydsl.TableDslRepository;

@Repository
public interface TableJpaRepository extends JpaRepository<TableEntity, UUID>, TableDslRepository {
}
