## Interceptor

### DeserializingUserPassportInterceptor

- Gateway에서 사용자 정보를 JSON 형식 String으로 Request 헤더에 저장하면, 이를 도메인 모듈에서 접근가능한 객체인 UserPassport로 역직렬화하여
  HttpServletRequest의 attribute에 저장하는 인터셉터입니다.

### AuthorizationInterceptor (@HasRole)

- @HasRole 어노테이션이 붙은 메서드에 대해, 메서드 실행 전에 사용자 권한을 체크하는 인터셉터입니다.
- HttpServletRequest의 attribute에 저장된 UserPassport 객체를 조회하여, 해당 사용자가 어노테이션 옵션에 명시한 권한을 가지고 있는지 확인합니다.
    - 옵션에 권한을 명시하지 않으면, 일반 유저 이상의 권한을 지니는지 확인합니다.

---

## AOP

### AssignUserPassportAspect (@AssignUserPassport)

- @AssignUserPassport 어노테이션이 붙은 메서드에 대해, 메서드 실행 전에 매개변수 중 첫 번째 UserPassport에 사용자 정보를 할당하는 AOP입니다.
    - HttpServletRequest 사용을 위해, HttpServletRequest/Response를 ThreadLocal에 묶어서 보관하는 **RequestContextHolder**를 사용합니다.

### DeadlockRetryAspect (@DeadlockRetry)

- @DeadlockRetry 어노테이션이 붙은 메서드에 대해, Deadlock이 발생하면 재시도하는 AOP입니다.
    - 서비스 메서드 내에서 여러 도메인의 락을 사용해야하거나, Deadlock이 발생할 가능성이 있는 경우에 어노테이션을 붙여 사용합니다.
    - @Transactional AOP보다 먼저 적용되어야 하므로, 우선순위를 1로 지정합니다.
