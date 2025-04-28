package com.pos.menu.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pos.menu.entity.MenuCategoryEntity;
import com.pos.menu.repository.querydsl.MenuCategoryQueryDslRepository;

public interface MenuCategoryJpaRepository extends JpaRepository<MenuCategoryEntity, Long>,
	MenuCategoryQueryDslRepository {
}
