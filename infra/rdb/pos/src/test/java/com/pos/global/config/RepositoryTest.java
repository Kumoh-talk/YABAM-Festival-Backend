package com.pos.global.config;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@Import({
	com.pos.global.config.JpaConfig.class, com.pos.global.config.JpaComponentConfig.class,
	com.pos.global.config.QueryDSLConfig.class})
@ActiveProfiles("test")
@ContextConfiguration(classes = {com.pos.global.config.JpaConfig.class, com.pos.global.config.JpaComponentConfig.class,
	com.pos.global.config.QueryDSLConfig.class})
public abstract class RepositoryTest {
}
