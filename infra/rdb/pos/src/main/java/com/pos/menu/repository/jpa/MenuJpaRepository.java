package com.pos.menu.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pos.menu.entity.MenuEntity;
import com.pos.menu.repository.querydsl.MenuQueryDslRepository;

public interface MenuJpaRepository extends JpaRepository<MenuEntity, Long>, MenuQueryDslRepository {
}
