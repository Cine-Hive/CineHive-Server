## 코딩 스타일

### 일반
- 들여쓰기는 4칸 공백을 사용합니다.
- 파일 끝에는 항상 빈 줄을 추가합니다.
- 최대 줄 길이는 120자를 넘지 않도록 합니다.
- 서술형 메서드/변수 이름을 사용하여 코드의 가독성을 높입니다.
- 불필요한 주석은 피하고, 코드가 자체적으로 설명되도록 작성합니다.

### 명명 규칙
- 변수/필드: camelCase (예: `userId`, `orderStatus`)
- 클래스/인터페이스/열거형: PascalCase (예: `UserService`, `OrderRepository`)
- 상수: 대문자와 언더스코어 (예: `MAX_RETRY_COUNT`, `DEFAULT_TIMEOUT`)
- 메서드: camelCase와 설명적인 동사로 시작 (예: `findUserById()`, `processPayment()`)
- 패키지: 모두 소문자, 도메인 역순 (예: `com.company.module.feature`)
- 테스트 클래스: 테스트 대상 클래스 이름 + Test (예: `UserServiceTest`)

### Java 특화 스타일
- 제네릭 사용 시 와일드카드(`?`)를 적절히 활용합니다.
- Lambda 표현식과 메서드 참조를 사용하여 간결한 코드를 작성합니다.
- Optional을 적절히 활용하여 null 안전성을 확보합니다.
- 스트림 API를 사용하여 컬렉션 처리를 명확하게 합니다.
- 불변(immutable) 객체를 선호하고, 가능한 final 키워드를 활용합니다.
- try-with-resources를 사용하여 리소스를 안전하게 관리합니다.

### 주석
- 모든 공개 API에는 JavaDoc 주석을 추가합니다.
- 복잡한 알고리즘이나 비즈니스 로직에는 설명 주석을 추가합니다.
- 주석은 코드가 "무엇"이 아니라 "왜"를 설명해야 합니다.
- `// TODO:`, `// FIXME:` 등의 표준 태그를 사용하여 후속 작업을 표시합니다.

## 아키텍처 원칙 (레이어드 아키텍처)

### 스프링 애플리케이션 구조
- **Controller**: 클라이언트 요청을 처리하고, 적절한 응답을 반환합니다. 비즈니스 로직을 포함하지 않습니다.
- **Service**: 비즈니스 로직을 구현합니다. 트랜잭션 관리가 이루어집니다.
- **Repository**: 데이터 접근 로직을 담당합니다. JPA, JDBC 등을 활용합니다.
- **Entity/DTO**: 데이터 구조를 정의합니다. Entity는 DB 테이블과 매핑되고, DTO는 계층 간 데이터 전송에 사용됩니다.
- **Config**: 애플리케이션 설정 클래스를 포함합니다.
- **Exception**: 커스텀 예외 클래스를 정의합니다.
- **Util**: 공통 유틸리티 클래스를 포함합니다.

### 서비스 계층 설계 원칙
- 서비스는 단일 책임 원칙(SRP)을 준수해야 합니다.
- 서비스 메서드는 가능한 원자적이고 독립적인 기능을 수행해야 합니다.
- 비즈니스 로직은 컨트롤러가 아닌 서비스 계층에 구현되어야 합니다.
- 트랜잭션 경계는 서비스 메서드에 정의합니다.
- 서비스는 다른 서비스에 의존할 수 있지만, 순환 의존성을 피해야 합니다.

### 의존성 주입
- 필드 주입(`@Autowired`)보다 생성자 주입을 선호합니다.
- 의존성은 인터페이스를 통해 주입하여 결합도를 낮추고 테스트 용이성을 높입니다.
- 선택적 의존성은 `Optional`을 사용하여 명시적으로 표현합니다.
- `@ComponentScan` 범위를 명확히 정의하여 의존성 탐색을 최적화합니다.

### REST API 설계
- URL은 명사를 사용하고, HTTP 메서드로 행동을 표현합니다.
- 적절한 HTTP 상태 코드를 사용합니다.
- 버전 관리 전략을 적용합니다. (URL, 헤더, 미디어 타입 등)
- HATEOAS 원칙을 고려하여 하이퍼미디어 링크를 제공합니다.
- 페이징, 필터링, 정렬 등의 기능을 일관되게 구현합니다.

## AI 코드 리뷰 프로세스

### 1. 코드 준비
- 리뷰할 코드를 정리하고 주석을 추가하여 AI가 이해하기 쉽게 준비합니다.
- `pom.xml` 또는 `build.gradle` 파일, 설정 파일도 함께 제공하면 더 정확한 분석이 가능합니다.
- 큰 프로젝트의 경우, 모듈별로 나누어 리뷰를 진행합니다.

### 2. AI에 제공할 컨텍스트
- 코드의 목적과 기능에 대한 설명
- 사용 중인 Spring Boot 버전 및 주요 라이브러리 (Spring Data JPA, Security 등)
- 아키텍처 설명 (레이어드, 헥사고날, DDD 등)
- 특별히 중점적으로 검토하고 싶은 부분
- 기존에 발견된 문제점이나 우려사항

### 3. AI 리뷰 요청 포맷

```
[프로젝트 설명]
- 목적: {애플리케이션의 목적}
- Spring Boot 버전: {버전}
- 주요 의존성: {Spring Data JPA, Spring Security 등}
- 아키텍처: {아키텍처 패턴}

[리뷰 요청 사항]
- 코드 품질 및 클린 코드 원칙 준수 여부
- 성능 최적화 가능성
- 보안 취약점
- {기타 특별히 확인하고 싶은 사항}

[코드]
{코드 블록}
```

## 리뷰 포인트

### 1. 코드 구조 및 설계
- **레이어 분리**: 컨트롤러, 서비스, 레포지토리 등의 계층이 적절히 분리되어 있는지
- **의존성 주입**: 의존성 주입이 올바르게 구현되었는지 (생성자 주입 선호)
- **관심사 분리**: 각 클래스와 메서드가 단일 책임 원칙을 준수하는지
- **디자인 패턴**: 적절한 디자인 패턴이 사용되었는지 (Factory, Strategy, Builder 등)

### 2. 성능 최적화
- **데이터베이스 쿼리**: N+1 문제, 불필요한 쿼리, 최적화되지 않은 쿼리
- **캐싱**: 캐싱 전략이 적절한지 (Spring Cache, Redis 등)
- **비동기 처리**: `@Async`, WebFlux, CompletableFuture 등을 활용한 비동기 처리
- **리소스 관리**: 커넥션 풀, 스레드 풀 등의 리소스 설정이 적절한지

### 3. 보안
- **입력 유효성 검사**: Bean Validation을 활용한 입력 검증
- **인증과 권한 부여**: Spring Security 설정이 적절한지
- **CSRF, XSS 방어**: 보안 헤더, CSP 등의 설정
- **민감 정보 처리**: 암호화, 해싱 전략이 적절한지
- **종속성 취약점**: 사용 중인 라이브러리의 알려진 보안 취약점

### 4. 예외 처리
- **예외 전략**: 일관된 예외 처리 전략이 있는지
- **커스텀 예외**: 도메인 특화 예외가 적절히 정의되었는지
- **전역 예외 핸들러**: `@ControllerAdvice`/`@ExceptionHandler`를 활용한 일관된 예외 처리
- **예외 로깅**: 적절한 로그 레벨과 충분한 컨텍스트 정보를 제공하는지

### 5. 테스트
- **테스트 범위**: 단위 테스트, 통합 테스트, 계약 테스트 등의 커버리지
- **테스트 격리**: 테스트가 독립적으로 실행되는지
- **테스트 가독성**: Given-When-Then 패턴 등을 활용한 명확한 테스트 구조
- **목킹 전략**: Mockito, BDDMockito 등을 활용한 적절한 목킹

### 6. 코드 품질
- **명명 규칙**: 변수, 메서드, 클래스 이름이 명확하고 일관되는지
- **중복 코드**: DRY 원칙을 준수하는지
- **코드 복잡성**: 메서드와 클래스의 순환 복잡도가 적절한지
- **가독성**: 코드가 명확하고 이해하기 쉬운지
- **주석/문서화**: JavaDoc이 적절히 작성되었는지

## 스프링 관련 특화 항목

### 스프링 컨텍스트 최적화
- 불필요한 빈 스캔 범위를 줄이기 위한 `@ComponentScan` 설정
- 조건부 빈 등록을 위한 `@Conditional` 애노테이션 활용
- 불필요한 자동 설정 제외 (`@EnableAutoConfiguration(exclude = {...})`)

### 트랜잭션 관리
- `@Transactional` 애노테이션의 적절한 사용
- 전파 속성(propagation)과 격리 수준(isolation) 설정
- 읽기 전용 트랜잭션 최적화 (`@Transactional(readOnly = true)`)
- 트랜잭션 경계 설정의 일관성

### Spring Data JPA 활용
- 엔티티 관계 설정의 적절성 (연관관계, 페치 전략 등)
- 쿼리 메서드, JPQL, 네이티브 쿼리의 적절한 사용
- N+1 문제 해결을 위한 페치 조인 활용
- 페이징 및 정렬 구현의 최적화

### REST API 구현
- `@RestController`, `@RequestMapping` 등의 적절한 사용
- RequestBody/ResponseBody의 명확한 DTO 정의
- REST 원칙 준수 (리소스 중심, 적절한 HTTP 메서드 등)
- API 문서화 (Swagger/OpenAPI, Spring REST Docs)

## AI가 제공할 수 있는 피드백 유형

1. **문제점 식별**: 코드의 오류, 잠재적 버그, 비효율적인 패턴 등을 발견
2. **개선 제안**: 더 효율적이거나 더 깔끔한 구현 방법 제안
3. **리팩토링 아이디어**: 코드 구조를 개선하기 위한 리팩토링 방향 제시
4. **모범 사례 적용**: Spring Boot의 최신 모범 사례 적용 방법 제안
5. **기술적 부채 식별**: 향후 문제가 될 수 있는 기술적 부채 식별

## AI 리뷰의 한계

- AI는 비즈니스 로직의 정확성을 완벽히 평가할 수 없습니다.
- 프로젝트의 전체 컨텍스트를 이해하지 못할 수 있습니다.
- 복잡한 아키텍처 결정의 배경을 파악하기 어려울 수 있습니다.
- 팀의 코딩 컨벤션이나 특별한 요구사항을 알지 못할 수 있습니다.

## AI 리뷰 결과 활용

1. **팀 리뷰 병행**: AI 리뷰는 팀 멤버의 코드 리뷰를 대체하지 않고 보완하는 역할로 활용
2. **학습 자료화**: 반복적으로 지적되는 패턴을 학습 자료로 만들어 팀 교육에 활용
3. **자동화 통합**: CI/CD 파이프라인에 AI 리뷰를 통합하여 자동화
4. **코드 컨벤션 적용**: AI 리뷰 결과를 바탕으로 팀의 코드 컨벤션 개선

## 실제 AI 리뷰 예시

### 개선 전 코드:
```java
@RestController
public class UserController {
    @Autowired
    private UserService userService;
    
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public List<User> getAllUsers() {
        List<User> users = userService.findAll();
        return users;
    }
    
    @RequestMapping(value = "/users", method = RequestMethod.POST)
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }
}
```

### AI 리뷰 의견:
```
1. 의존성 주입: 필드 주입(@Autowired) 대신 생성자 주입을 사용하는 것이 권장됩니다.
2. 응답 형식: REST API에서는 ResponseEntity를 사용하여 HTTP 상태 코드와 헤더를 명시적으로 제어하는 것이 좋습니다.
3. 매핑 애노테이션: @RequestMapping 대신 @GetMapping, @PostMapping 등 HTTP 메서드에 특화된 애노테이션을 사용하세요.
4. DTO 패턴: Entity 대신 DTO를 사용하여 API 계층과 영속성 계층을 분리하세요.
5. 페이징: 모든 사용자를 반환하는 API는 페이징 처리가 필요합니다.
6. 유효성 검사: Bean Validation을 사용하여 입력 데이터 검증이 필요합니다.
```

### 개선 후 코드:
```java
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UserResponseDto> users = userService.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(users);
    }
    
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateRequestDto request) {
        UserResponseDto createdUser = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
}
```

## 스프링 부트 성능 최적화 체크리스트

### 데이터베이스 최적화
- 적절한 인덱스 설정
- N+1 쿼리 문제 해결 (Fetch Join, EntityGraph)
- 페이징 최적화 (Offset 대신 Keyset Pagination 고려)
- 벌크 연산 활용 (`@Modifying` 쿼리)
- 읽기 전용 트랜잭션 사용 (`@Transactional(readOnly = true)`)

### 캐싱 전략
- Spring Cache Abstraction 활용
- 적절한 캐시 제공자 선택 (Caffeine, Redis, Hazelcast 등)
- 캐시 무효화 전략 구현
- 캐시 키 설계

### 애플리케이션 최적화
- 비동기 처리 활용 (`@Async`, WebFlux, CompletableFuture)
- JVM 메모리 설정 최적화
- 적절한 로깅 레벨 설정
- 불필요한 빈 등록 최소화

### 모니터링 설정
- Actuator 엔드포인트 활성화
- Micrometer 통합
- Prometheus, Grafana 연동
- 분산 추적 (Spring Cloud Sleuth, OpenTelemetry)

## PR 컨벤션

### PR 제목 형식
- `[유형] 간결한 변경 내용 설명`
- 유형: feature, bugfix, docs, refactor, test, chore 등

### PR 설명 요구사항
- 변경 사항에 대한 명확한 설명
- 관련 이슈 번호 (있는 경우)
- 테스트 방법
- API 변경 시 예시 요청/응답

### 코드 리뷰 프로세스
- 최소 1명의 리뷰어 승인이 필요합니다.
- 모든 CI 테스트가 통과해야 합니다.
- 리뷰 코멘트는 반드시 해결되어야 합니다.

## 개발 환경

### 권장 도구
- Java 17 이상
- Spring Boot 3.x
- Maven 또는 Gradle
- CheckStyle, SpotBugs, PMD 등의 정적 분석 도구
- SonarQube를 통한 코드 품질 분석

### 환경 설정
- 개발(Development), 테스트(Test), 스테이징(Staging), 프로덕션(Production) 환경 구분
- 프로파일 기반 설정 관리 (`application-{profile}.yml`)
- 환경 변수 또는 외부 설정 서버를 통한 설정 관리 (Spring Cloud Config)