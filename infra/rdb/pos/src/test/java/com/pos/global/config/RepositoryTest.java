package com.pos.global.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.pos.builder.BuilderSupporter;
import com.pos.builder.TestFixtureBuilder;

@DataJpaTest
@Import({
	com.pos.global.config.JpaConfig.class, com.pos.global.config.JpaComponentConfig.class,
	com.pos.global.config.QueryDSLConfig.class,
	BuilderSupporter.class, TestFixtureBuilder.class})
@ActiveProfiles("test")
@ContextConfiguration(classes = {com.pos.global.config.JpaConfig.class, com.pos.global.config.JpaComponentConfig.class,
	com.pos.global.config.QueryDSLConfig.class})
public abstract class RepositoryTest {
	@Autowired
	protected TestFixtureBuilder testFixtureBuilder;

	@Autowired
	protected TestEntityManager testEntityManager;
}
