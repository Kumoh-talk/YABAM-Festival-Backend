package com.pos.config;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@Import({JpaConfig.class, JpaComponentConfig.class, QueryDSLConfig.class})
@ActiveProfiles("test")
@ContextConfiguration(classes = {JpaConfig.class, JpaComponentConfig.class, QueryDSLConfig.class})
public abstract class RepositoryTest {
}
