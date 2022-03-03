package com.example.reactiversocketserver.service;

import com.example.reactiversocketserver.domain.Item;
import com.example.reactiversocketserver.repository.ItemRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Service
public class RSocketService {
    private final ItemRepository repository;

    // 새로운 Item 객체가 저장되면 스트림 갱신을 받도록 약속한 사람들에게 자동으로 정보를 제공하게 만든다.
    private final Sinks.Many<Item> itemsSink;

    public RSocketService(ItemRepository repository) {
        this.repository = repository;
        this.itemsSink = Sinks.many().multicast().onBackpressureBuffer();
    }

    /** 요청-응답 방식 */
    @MessageMapping("newItems.request-response") // 도착지가 newItems.request-response로 지정된 R소켓 메시지를 이 메소드로 라우팅한다.
    public Mono<Item> processNewItemsViaRSocketRequestResponse(Item item) {
        return this.repository.save(item)
                .doOnNext(savedItem -> this.itemsSink.tryEmitNext(savedItem));
    }

    /** 요청-스트림 방식 */
    @MessageMapping("newItems.request-stream")
    public Flux<Item> findItemsViaRSocketRequestStream() {
        return this.repository.findAll()
				.doOnNext(this.itemsSink::tryEmitNext);
    }

    /** 실행 후 망각 방식 */
    @MessageMapping("newItems.fire-and-forget")
    public Mono<Void> processNewItemsViaRSocketFireAndForget(Item item) {
        return this.repository.save(item)
                .doOnNext(savedItem -> this.itemsSink.tryEmitNext(savedItem))
                .then();
    }

    /** 채널(양방향) 방식
     * R소켓 익스체인지 채널 모니터링 */
    @MessageMapping("newItems.monitor")
    public Flux<Item> monitorNewItems() {
        // 요청으로 들어오는 파라미터가 없지만, client가 쿼리나 필터링처럼 원하는 것을 요청 데이터에 담아 보낼 수도 있다.
        // 그래서 반환타입은 Mono가 아니라 복수의 Item 객체를 포함하는 Flux이다.
		return this.itemsSink.asFlux();
    }
}
