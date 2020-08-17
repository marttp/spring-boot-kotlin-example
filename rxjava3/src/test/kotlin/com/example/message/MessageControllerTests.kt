package com.example.message

import com.example.MockUser
import com.example.account.Account
import com.example.any
import com.example.toSingle
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@WebFluxTest(MessageController::class)
@MockUser
internal class MessageControllerTests(private val webTestClient: WebTestClient) {

    @MockBean
    private lateinit var messageService: MessageService


    @Test
    internal fun `message all test`() {

        given(messageService.findAll()).willReturn(
                Flowable.fromIterable(listOf(Message(id = "foo", message = "test message", account = Account("wonwoo", "foo"))))
        )

        webTestClient.get()
                .uri("/message")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<String>()
                .consumeWith {
                    assertThat(it.status).isEqualTo(HttpStatus.OK)
                    assertThat(it.responseBody).contains("test message")
                    assertThat(it.responseBody).contains("wonwoo")
                    assertThat(it.responseBody).contains("just now")
                }

    }

    @Test
    internal fun `message save test`() {

        val message = Message(message = "test message", account = Account(id = "bar", name = "user", passwd = "password"))
        given(messageService.findAll()).willReturn(
                Flowable.fromIterable(listOf(Message(id = "foo", message = "test message", account = Account("wonwoo", "foo"))))
        )
        given(messageService.save(message)).willReturn(message.toSingle())


        webTestClient.mutateWith(csrf())
                .post()
                .uri {
                    it.queryParam("message", "test messsage")
                            .path("/message")
                            .build()
                }

                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus()
                .isSeeOther
                .expectBody<String>()
                .consumeWith {

                    verify(messageService, atLeastOnce()).save(any())
                    verify(messageService, atLeastOnce()).findAll()

                }

    }

    @Test
    internal fun `message delete test`() {

        given(messageService.delete(anyString())).willReturn(Completable.complete())
        given(messageService.findAll()).willReturn(
                Flowable.fromIterable(listOf(Message(id = "foo", message = "test message", account = Account("wonwoo", "foo"))))
        )

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/message/delete/{id}", "foo")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus()
                .isSeeOther
                .expectBody<String>()
                .consumeWith {

                    verify(messageService, atLeastOnce()).delete("foo")
                    verify(messageService, atLeastOnce()).findAll()

                }
    }

}

