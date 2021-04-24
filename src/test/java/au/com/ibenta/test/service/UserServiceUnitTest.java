package au.com.ibenta.test.service;

import au.com.ibenta.test.exception.ResourceNotFoundException;
import au.com.ibenta.test.model.User;
import au.com.ibenta.test.persistence.UserEntity;
import au.com.ibenta.test.persistence.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestMethodOrder(OrderAnnotation.class)
public class UserServiceUnitTest {

    @Mock
    UserRepository userRepository;

    @Spy
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    private User userStub;
    private User updatedUserStub;
    private UserEntity userEntityStub;
    private UserEntity updatedUserEntityStub;

    final long ID = 1L;
    final String FIRST_NAME = "Nikko";
    final String LAST_NAME = "Dasig";
    final String EMAIL = "nikkodasig@gmail.com";
    final String PASSWORD = "password";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

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

        updatedUserStub = User.builder()
                .id(userStub.getId())
                .firstName("NIKKO")
                .lastName("DASIG")
                .email("NIKKODASIG@GMAIL.COM")
                .password(userStub.getPassword())
                .build();

        updatedUserEntityStub = UserEntity.builder()
                .id(updatedUserStub.getId())
                .firstName(updatedUserStub.getFirstName())
                .lastName(updatedUserStub.getLastName())
                .email(updatedUserStub.getEmail())
                .password(updatedUserStub.getPassword())
                .build();
    }

    @Test
    void testCreateNewUser() {
        final String ENCODED_PASSWORD = "encoded-password";
        userEntityStub.setPassword(ENCODED_PASSWORD);

        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(userEntityStub);

        Mono<User> newUserMono = userService.create(userStub);
        newUserMono.subscribe(newUser -> {
            assertEquals(newUser.getId(), ID);
            assertEquals(newUser.getFirstName(), userStub.getFirstName());
            assertEquals(newUser.getLastName(), userStub.getLastName());
            assertEquals(newUser.getEmail(), userStub.getEmail());
        });

        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(passwordEncoder, times(1)).encode(argThat(arg -> arg.equals(PASSWORD)));
    }

    @Test
    void testGetUser() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(userEntityStub));

        Mono<User> userMono = userService.get(1L);
        StepVerifier
                .create(userMono)
                .assertNext(foundUser -> assertEquals(foundUser, userStub))
                .verifyComplete();

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUserNotExists() {
        final long NOT_EXISTING_USER_ID = 123L;

        when(userRepository.findById(userStub.getId()))
                .thenReturn(Optional.empty());

        Mono<User> userMono = userService.get(NOT_EXISTING_USER_ID);
        StepVerifier
                .create(userMono)
                .expectErrorMatches(ex -> ex instanceof ResourceNotFoundException
                        && ex.getMessage().equals("Resource not found with ID: " + NOT_EXISTING_USER_ID))
                .verify();

        verify(userRepository, times(1)).findById(NOT_EXISTING_USER_ID);
    }

    @Test
    void testUpdateUser() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(updatedUserEntityStub));
        when(userRepository.save(any()))
                .thenReturn(updatedUserEntityStub);

        Mono<User> updatedUserMono = userService.update(updatedUserStub);
        StepVerifier
                .create(updatedUserMono)
                .assertNext(updatedUser -> {
                    assertEquals(updatedUser.getFirstName(), updatedUserStub.getFirstName());
                    assertEquals(updatedUser.getLastName(), updatedUserStub.getLastName());
                    assertEquals(updatedUser.getEmail(), updatedUserStub.getEmail());
                })
                .verifyComplete();

        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void testUpdateUserNotExists() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        Mono<User> userMono = userService.update(updatedUserStub);
        StepVerifier
                .create(userMono)
                .expectErrorMatches(ex -> ex instanceof ResourceNotFoundException
                        && ex.getMessage().equals("Resource not found with ID: " + updatedUserStub.getId()))
                .verify();

        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(0)).save(any(UserEntity.class));
    }

    @Test
    void testDeleteUser() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);

        Mono<Void> voidMono = userService.delete(userStub.getId());
        StepVerifier
                .create(voidMono)
                .verifyComplete();

        verify(userRepository, times(1)).existsById(userStub.getId());
        verify(userRepository, times(1)).deleteById(userStub.getId());
    }

    @Test
    void testDeleteUserNotExists() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(false);

        Mono<Void> voidMono = userService.delete(userStub.getId());
        StepVerifier
                .create(voidMono)
                .expectErrorMatches(ex -> ex instanceof ResourceNotFoundException
                        && ex.getMessage().equals("Resource not found with ID: " + userStub.getId()))
                .verify();

        verify(userRepository, times(1)).existsById(userStub.getId());
    }

    @Test
    void testGetAllUsers() {
        UserEntity userEntityStub2 = UserEntity.builder()
                .id(2L)
                .firstName("Sheldon")
                .lastName("Cooper")
                .email("sheldon.cooper@gmail.com")
                .password("password123")
                .build();

        List<UserEntity> usersStub = Arrays.asList(userEntityStub, userEntityStub2);

        when(userRepository.findAll()).thenReturn(usersStub);

        Flux<User> usersFlux = userService.list();
        StepVerifier
                .create(usersFlux)
                .expectNextCount(2)
                .verifyComplete();

        verify(userRepository, times(1)).findAll();
    }
}