package au.com.ibenta.test.service;

import au.com.ibenta.test.exception.ResourceNotFoundException;
import au.com.ibenta.test.model.User;
import au.com.ibenta.test.persistence.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

import static org.mockito.Mockito.*;

class UserControllerUnitTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private WebTestClient webTestClient;

    private User userStub;
    private UserEntity userEntityStub;

    final long ID = 1L;
    final String FIRST_NAME = "Nikko";
    final String LAST_NAME = "Dasig";
    final String EMAIL = "nikkodasig@gmail.com";
    final String PASSWORD = "password";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        webTestClient = WebTestClient.bindToController(userController).build();

        userStub = User.builder()
                .id(ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .password(PASSWORD)
                .build();

        userEntityStub = UserEntity.builder()
                .id(ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .password(PASSWORD)
                .build();
    }

    @Test
    void testCreateUser() {
        when(userService.create(any())).thenReturn(Mono.just(userStub));

        webTestClient.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userStub)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(userStub.getId())
                .jsonPath("$.firstName").isEqualTo(userStub.getFirstName())
                .jsonPath("$.lastName").isEqualTo(userStub.getLastName())
                .jsonPath("$.email").isEqualTo(userStub.getEmail());
    }

    @Test
    void testGetUser() {
        when(userService.get(userStub.getId())).thenReturn(Mono.just(userStub));

        webTestClient.get()
                .uri("/api/users/{id}", userStub.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(userStub.getId())
                .jsonPath("$.firstName").isEqualTo(userStub.getFirstName())
                .jsonPath("$.lastName").isEqualTo(userStub.getLastName())
                .jsonPath("$.email").isEqualTo(userStub.getEmail());
    }

    @Test
    void testGetUserNotExists() {
        final long NOT_EXISTING_USER_ID = 123L;

        when(userService.get(NOT_EXISTING_USER_ID))
                .thenReturn(Mono.error(new ResourceNotFoundException(NOT_EXISTING_USER_ID)));

        webTestClient.get()
                .uri("/api/users/{id}", NOT_EXISTING_USER_ID)
                .exchange()
                .expectStatus().isNotFound();

        // verify(userService, times(1)).get(anyLong());
    }

    @Test
    void testUpdateUser() {
        userStub.setEmail("ndasig@gmail.com");
        when(userService.update(any())).thenReturn(Mono.just(userStub));

        webTestClient.put()
                .uri("/api/users/{id}", userStub.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userStub)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(userStub.getId())
                .jsonPath("$.firstName").isEqualTo(userStub.getFirstName())
                .jsonPath("$.lastName").isEqualTo(userStub.getLastName())
                .jsonPath("$.email").isEqualTo(userStub.getEmail());
    }

    @Test
    void testUpdateUserNotExists() {
        final long NOT_EXISTING_USER_ID = 123L;

        userStub.setId(NOT_EXISTING_USER_ID);

        when(userService.update(any()))
                .thenReturn(Mono.error(new ResourceNotFoundException(NOT_EXISTING_USER_ID)));

        webTestClient.put().uri("/api/users/{id}", NOT_EXISTING_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userStub)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void testDeleteUser() {
        when(userService.delete(anyLong())).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/users/{id}", userStub.getId())
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void testDeleteUserNotExists() {
        final long NOT_EXISTING_USER_ID = 123L;

        when(userService.delete(NOT_EXISTING_USER_ID))
                .thenReturn(Mono.error(new ResourceNotFoundException(NOT_EXISTING_USER_ID)));

        webTestClient.delete()
                .uri("api/users/{id}", NOT_EXISTING_USER_ID)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void testGetAllUsers() {
        User userStub2 = User.builder()
                .id(2L)
                .firstName("Sheldon")
                .lastName("Cooper")
                .email("sheldon.cooper@gmail.com")
                .password("password123")
                .build();

        Flux<User> usersStub = Flux.fromIterable(Arrays.asList(userStub, userStub2));

        when(userService.list()).thenReturn(usersStub);

        webTestClient.get()
                .uri("/api/users")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserEntity.class)
                .hasSize(2);
    }
}