package au.com.ibenta.test.service;

import au.com.ibenta.test.exception.ResourceNotFoundException;
import au.com.ibenta.test.persistence.UserEntity;
import au.com.ibenta.test.persistence.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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

        UserEntity newUser = userService.create(userStub);

        verify(userRepository, times(1)).save(any(UserEntity.class));

        assertEquals(newUser, userStub);
    }

    @Test
    void testGetUser() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(userStub));

        UserEntity user = userService.get(1L);

        verify(userRepository, times(1)).findById(1L);

        assertEquals(user, userStub);
    }

    @Test
    void testGetUserNotExists() {
        final long NOT_EXISTING_USER_ID = 2L;

        when(userRepository.findById(userStub.getId()))
                .thenReturn(Optional.empty());

        try {
            userService.get(NOT_EXISTING_USER_ID);
        } catch (Exception ex) {
            assertEquals(ResourceNotFoundException.class, ex.getClass());
        }

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

        UserEntity updatedUser = userService.update(updatedUserStub);

        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).save(any(UserEntity.class));

        assertEquals(updatedUser, updatedUserStub);
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

        try {
            userService.update(updatedUserStub);
        } catch (Exception ex) {
            assertEquals(ResourceNotFoundException.class, ex.getClass());
        }

        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(0)).save(any(UserEntity.class));
    }

    @Test
    void testDeleteUser() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(true);

        userService.delete(userStub.getId());

        verify(userRepository, times(1)).existsById(userStub.getId());
    }

    @Test
    void testDeleteUserNotExists() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(false);

        try {
            userService.delete(userStub.getId());
        } catch (Exception ex) {
            assertEquals(ResourceNotFoundException.class, ex.getClass());
        }

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

        List<UserEntity> users = userService.list();

        verify(userRepository, times(1)).findAll();

        assertEquals(2, users.size());
    }
}