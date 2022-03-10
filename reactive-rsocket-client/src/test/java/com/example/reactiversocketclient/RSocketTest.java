package com.example.reactiversocketclient;

import com.example.reactiversocketclient.domain.Item;
import com.example.reactiversocketclient.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebTestClient
public class RSocketTest {
    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ItemRepository itemRepository;

    @Test
    void verifyRemoteOperationsThroughRSocketRequestResponse() throws InterruptedException {
        // Clean out the database
        this.itemRepository.deleteAll() // <1> 불필요한 데이터 삭제
                .as(StepVerifier::create) //
                .verifyComplete();

        // Create a new "item"
        this.webTestClient.post().uri("/items/request-response") // <2> controller에서 정의한 경로에 webTestClient를 사용해서 HTTP post 요청
                .bodyValue(new Item("Alf alarm clock", "nothing important", 19.99))
                .exchange()
                .expectStatus().isCreated() // <3> HTTP 201 CREATED 반환되는지 검증
                .expectBody(Item.class)
                .value(item -> {
                    assertThat(item.getItemId()).isNotNull();
                    assertThat(item.getName()).isEqualTo("Alf alarm clock");
                    assertThat(item.getDescription()).isEqualTo("nothing important");
                    assertThat(item.getPrice()).isEqualTo(19.99);
                });

        Thread.sleep(500); // <4> 스레드 잠시 중지해서 새 item이 Rsocket서버를 거쳐 몽고디비에 저장될 시간 여유를 둔다.

        // Verify the "item" has been added to MongoDB
        this.itemRepository.findAll() // <4> 저장 여부 확인
                .as(StepVerifier::create)
                .expectNextMatches(item -> {
                    assertThat(item.getItemId()).isNotNull();
                    assertThat(item.getName()).isEqualTo("Alf alarm clock");
                    assertThat(item.getDescription()).isEqualTo("nothing important");
                    assertThat(item.getPrice()).isEqualTo(19.99);
                    return true;
                }).verifyComplete();
    }
}
