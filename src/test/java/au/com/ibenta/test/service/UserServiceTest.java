package au.com.ibenta.test.service;

import au.com.ibenta.test.exception.ResourceNotFoundException;
import au.com.ibenta.test.persistence.UserEntity;
import au.com.ibenta.test.persistence.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestMethodOrder(OrderAnnotation.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    private UserEntity userStub;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        userStub = new UserEntity();
        userStub.setId(1L);
        userStub.setFirstName("Nikko");
        userStub.setLastName("Dasig");
        userStub.setEmail("nikkodasig@gmail.com");
        userStub.setPassword("password");
    }

    @Test
    void testCreateNewUser() {
        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(userStub);

        Mono<UserEntity> newUserMono = userService.create(userStub);
        newUserMono.subscribe(newUser -> {
            assertEquals(newUser, userStub);
        });

        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testGetUser() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(userStub));

        Mono<UserEntity> userMono = userService.get(1L);
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

        Mono<UserEntity> userMono = userService.get(NOT_EXISTING_USER_ID);
        StepVerifier
                .create(userMono)
                .expectErrorMatches(ex -> ex instanceof ResourceNotFoundException
                        && ex.getMessage().equals("Resource not found with ID: " + NOT_EXISTING_USER_ID))
                .verify();

        verify(userRepository, times(1)).findById(NOT_EXISTING_USER_ID);
    }

    @Test
    void testUpdateUser() {
        UserEntity updatedUserStub = new UserEntity();
        updatedUserStub.setId(userStub.getId());
        updatedUserStub.setFirstName(userStub.getFirstName());
        updatedUserStub.setLastName(userStub.getFirstName());
        updatedUserStub.setEmail("ndasig@gmail.com");
        updatedUserStub.setPassword(userStub.getPassword());

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(userStub));

        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(updatedUserStub);

        Mono<UserEntity> updatedUserMono = userService.update(updatedUserStub);
        StepVerifier
                .create(updatedUserMono)
                .assertNext(updatedUser -> assertEquals(updatedUser, updatedUserStub))
                .verifyComplete();

        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testUpdateUserNotExists() {
        UserEntity updatedUserStub = new UserEntity();
        updatedUserStub.setId(2L);
        updatedUserStub.setFirstName(userStub.getFirstName());
        updatedUserStub.setLastName(userStub.getFirstName());
        updatedUserStub.setEmail(userStub.getEmail());
        updatedUserStub.setPassword(userStub.getPassword());

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        Mono<UserEntity> userMono = userService.update(updatedUserStub);
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
        UserEntity userStub2 = new UserEntity();
        userStub2.setId(2L);
        userStub2.setFirstName("Sheldon");
        userStub2.setLastName("Cooper");
        userStub2.setEmail("sheldon.cooper@gmail.com");
        userStub2.setPassword("password123");

        List<UserEntity> usersStub = Arrays.asList(userStub, userStub2);

        when(userRepository.findAll()).thenReturn(usersStub);

        Flux<UserEntity> usersFlux = userService.list();
        StepVerifier
                .create(usersFlux)
                .expectNextCount(2)
                .verifyComplete();

        verify(userRepository, times(1)).findAll();
    }
}