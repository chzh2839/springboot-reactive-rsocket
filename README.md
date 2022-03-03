# springboot-reactive-Rsocket
Reactive spring boot for Rsocket - client & server

- spring boot
- mongoDB
- Thymeleaf
- Maven
- Java 1.8

***

### spring-boot-starter-rsocket 의존관계 추가
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-rsocket</artifactId>
    </dependency>

- 위의 의존성 추가로 아래의 기능이 프로젝트에 추가된다.
1. RSocket Core와 Transport Netty
2. Reactor Netty
3. Spring Messaging
4. Jackson

***

####RScoket
자바로 구현된 R소켓 프로토콜


####Reactor Netty
네티는 리액티브 메시지 관리자 역할도 충분히 수행할 수 있다.  
리액터로 감싸져서 더 강력한 서버로 만들어졌다.


####Spring + Jackson
메시지가 선택되고, 직렬화되며 전송되고, 역지렬화되고 라우팅되는 것은 프로토콜의 리액티브 속성만큼이나 중요하다.  
스프링의 입증된 메시지 처리 아키텍처와 잭슨을 함께 사용하는 사례는 무시히 많으며 현장에서 충분히 검증됐다.

