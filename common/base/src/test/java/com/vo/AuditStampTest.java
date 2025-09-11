// package com.vo;
//
// import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
//
// import java.util.Set;
//
// import org.junit.jupiter.api.Test;
//
// import com.tngtech.archunit.base.DescribedPredicate;
// import com.tngtech.archunit.core.domain.JavaClass;
// import com.tngtech.archunit.core.domain.JavaClasses;
// import com.tngtech.archunit.core.domain.JavaMethodCall;
// import com.tngtech.archunit.core.importer.ClassFileImporter;
// import com.tngtech.archunit.core.importer.ImportOption;
// import com.tngtech.archunit.lang.ArchRule;
// import com.tngtech.archunit.lang.syntax.elements.ClassesShouldConjunction;
//
// public class AuditStampTest {
// 	private static final String BASE_PACKAGE = "com";
// 	private static final String AUDIT_STAMP_FQCN = "com.vo.AuditStamp";
//
// 	// TODO : 여러 모듈의 패키지명 수정 후 추가
// 	private static final String[] INFRA_PACKAGES = {
// 		"..infra..",
// 	};
//
// 	private static final Set<String> FACTORY_METHODS = Set.of("create", "update", "delete");
//
// 	@Test
// 	void only_infra_may_call_AuditStamp_static_factories() {
// 		JavaClasses classes = new ClassFileImporter()
// 			.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS) // 테스트 코드 제외
// 			.importPackages(BASE_PACKAGE);
//
// 		// com.vo.AuditStamp의 정적 팩토리 호출만 골라내는 predicate
// 		DescribedPredicate<JavaMethodCall> isAuditStampFactoryCall =
// 			new DescribedPredicate<>("AuditStamp.create/update/delete() 호출") {
// 				@Override
// 				public boolean test(JavaMethodCall call) {
// 					JavaClass owner = call.getTarget().getOwner();
// 					return owner.getName().equals(AUDIT_STAMP_FQCN)
// 						&& FACTORY_METHODS.contains(call.getTarget().getName());
// 				}
// 			};
//
// 		// 인프라 패키지 "밖"의 클래스는 위 호출을 하면 안 된다.
// 		ClassesShouldConjunction ruleBuilder =
// 			noClasses()
// 				.that().resideOutsideOfPackages(INFRA_PACKAGES)
// 				.should().callMethodWhere(isAuditStampFactoryCall);
//
// 		ArchRule rule = ruleBuilder.as("AuditStamp의 메서드는 infra 모듈에서만 호출할 수 있다.");
//
// 		rule.check(classes);
// 	}
// }
