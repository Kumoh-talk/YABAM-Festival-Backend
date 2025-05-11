## UUID v7 Generator JPA에서 사용 방법 가이드

### UUID v7 에 대해

UUID 에는 여러 버전이 존재합니다 <br>
그중에서 UUID v7은 보안적으로 안전하고 순서를 보장하기 때문에 MySQL의 PK의 클러스터링 인덱스 성질에서 성능저하를 유발하지 않습니다.

### JPA uuid v7 자동 생성 어노테이션 활용법

- 해당 모듈 의존

```groovy
dependencies {
    // utils 모듈 uuid
    implementation project(':utils:util-uuid')
}
```

- SequenceStyleGenerator 오버라이딩 클래스 생성

```java
public class UuidV7Generator extends SequenceStyleGenerator {
	@Override
	public Object generate(SharedSessionContractImplementor session, Object object) {
		return UuidUtils.randomV7();
	}
}
```

- 어노테이션 커스텀

```java

@IdGeneratorType(UuidV7Generator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, METHOD})
public @interface GeneratedUuidV7 {
}
```

- 어노테이션 삽입

```java

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TestEntity {
	@Id
	@GeneratedUuidV7
	private UUID id;

	private String name;
}
```

