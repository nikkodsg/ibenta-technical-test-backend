package au.com.ibenta.test.service;

import au.com.ibenta.test.exception.ResourceNotFoundException;
import au.com.ibenta.test.persistence.UserEntity;
import au.com.ibenta.test.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {
    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Mono<UserEntity> create(UserEntity userEntity) {
        return Mono.just(userRepository.save(userEntity));
    }

    public Mono<UserEntity> get(Long id) {
        return Mono.justOrEmpty(userRepository.findById(id))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(id)));
    }

    public Mono<UserEntity> update(UserEntity userEntity) {
        return Mono.justOrEmpty(userRepository.findById(userEntity.getId()))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(userEntity.getId())))
                .flatMap(userToUpdate -> {
                    userToUpdate.setEmail(userEntity.getEmail());
                    userToUpdate.setFirstName(userEntity.getFirstName());
                    userToUpdate.setLastName(userEntity.getLastName());
                    userToUpdate.setPassword(userEntity.getPassword());

                    return Mono.just(userRepository.save(userEntity));
                });
    }

    public Mono<Void> delete(Long id) {
        if (!userRepository.existsById(id)) {
            return Mono.error(new ResourceNotFoundException(id));
        }

        userRepository.deleteById(id);

        return Mono.empty();
    }

    public Flux<UserEntity> list() {
        return Flux.fromIterable(userRepository.findAll());
    }
}