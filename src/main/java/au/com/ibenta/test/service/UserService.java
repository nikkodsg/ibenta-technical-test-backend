package au.com.ibenta.test.service;

import au.com.ibenta.test.exception.ResourceNotFoundException;
import au.com.ibenta.test.model.User;
import au.com.ibenta.test.persistence.UserEntity;
import au.com.ibenta.test.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
public class UserService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<User> create(User user) {
        UserEntity userEntity = User.toEntity(user);
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        UserEntity newUser = userRepository.save(userEntity);
        return Mono.just(User.from(newUser));
    }

    public Mono<User> get(Long id) {
        return Mono.justOrEmpty(userRepository.findById(id))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(id)))
                .flatMap(userEntity -> Mono.just(User.from(userEntity)));
    }

    public Mono<User> update(User user) {
        UserEntity userEntity = User.toEntity(user);
        return Mono.justOrEmpty(userRepository.findById(userEntity.getId()))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(userEntity.getId())))
                .flatMap(userToUpdate -> {
                    userToUpdate.setEmail(userEntity.getEmail());
                    userToUpdate.setFirstName(userEntity.getFirstName());
                    userToUpdate.setLastName(userEntity.getLastName());
                    userToUpdate.setPassword(userEntity.getPassword());

                    return Mono.just(User.from(userRepository.save(userEntity)));
                });
    }

    public Mono<Void> delete(Long id) {
        if (!userRepository.existsById(id)) {
            return Mono.error(new ResourceNotFoundException(id));
        }

        userRepository.deleteById(id);

        return Mono.empty();
    }

    public Flux<User> list() {
        return Flux.fromIterable(userRepository.findAll()
                .stream()
                .map(userEntity -> User.from(userEntity))
                .collect(Collectors.toList()));
    }
}