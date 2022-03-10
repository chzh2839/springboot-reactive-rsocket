package com.example.reactiversocketclient.controller;

import com.example.reactiversocketclient.domain.Item;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

import static io.rsocket.metadata.WellKnownMimeType.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.*;

@RestController
public class RSocketController {
    private final Mono<RSocketRequester> requester;

    public RSocketController(RSocketRequester.Builder builder) {
        this.requester = Mono.just(builder
                .dataMimeType(APPLICATION_JSON) // 데이터 미디어 타입 지정
                .metadataMimeType(parseMediaType(MESSAGE_RSOCKET_ROUTING.toString())) // 라우팅 정보 같은 메타데이터 값은 R소켓 표준인 message/x.rsocket.routing.v0로 지정
                .tcp("localhost", 7000)) // tcp 사용
                        .retry(5) // 견고성을 높이기 위해 메시지 처리 실패시 Mono가 5번까지 재시도하도록 지정
                        .cache(); // 요청Mono를 hot source로 전환. 이것은 다수의 클라이언트가 동일한 하나의 데이터를 요구할 때 효율성을 높일 수 있다.
    }

    /** 요청 - 응답 방식 R소켓에서 새 item 추가 전송 */
    @PostMapping("/items/request-response")
    Mono<ResponseEntity<?>> addNewItemUsingRSocketRequestResponse(@RequestBody Item item) {
        return this.requester //
                .flatMap(rSocketRequester -> rSocketRequester.route("newItems.request-response")
                        .data(item) // item 객체 정보를 data() 메서드에 전달
                        .retrieveMono(Item.class)) // Mono<Item>응답을 원한다는 신호를 보냄
                .map(savedItem -> ResponseEntity.created(
                        URI.create("/items/request-response")).body(savedItem));
    }

    @GetMapping(value = "/items/request-stream", produces = MediaType.APPLICATION_NDJSON_VALUE) // <1>
    Flux<Item> findItemsUsingRSocketRequestStream() {
        return this.requester //
                .flatMapMany(rSocketRequester -> rSocketRequester // <2>
                        .route("newItems.request-stream") // <3>
                        .retrieveFlux(Item.class) // <4>
                        .delayElements(Duration.ofSeconds(1))); // <5>
    }

    @PostMapping("/items/fire-and-forget")
    Mono<ResponseEntity<?>> addNewItemUsingRSocketFireAndForget(@RequestBody Item item) {
        return this.requester //
                .flatMap(rSocketRequester -> rSocketRequester //
                        .route("newItems.fire-and-forget") // <1>
                        .data(item) //
                        .send()) // <2>
                .then( // <3>
                        Mono.just( //
                                ResponseEntity.created( //
                                        URI.create("/items/fire-and-forget")).build()));
    }

    @GetMapping(value = "/items", produces = TEXT_EVENT_STREAM_VALUE) // <1>
    Flux<Item> liveUpdates() {
        return this.requester //
                .flatMapMany(rSocketRequester -> rSocketRequester //
                        .route("newItems.monitor") // <2>
                        .retrieveFlux(Item.class)); // <3>
    }
}
