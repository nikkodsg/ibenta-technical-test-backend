package au.com.ibenta.test.service;

import au.com.ibenta.test.persistence.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping(path = "/api/users")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public Mono<UserEntity> create(@RequestBody UserEntity userEntity) {
        return userService.create(userEntity);
    }

    @GetMapping("/{id}")
    @ResponseStatus(OK)
    public Mono<UserEntity> get(@PathVariable Long id) {
        return userService.get(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(OK)
    public Mono<UserEntity> update(@PathVariable Long id, @RequestBody UserEntity userEntity) {
        userEntity.setId(id);
        return userService.update(userEntity);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return userService.delete(id);
    }

    @GetMapping
    @ResponseStatus(OK)
    public Flux<UserEntity> list() {
        return userService.list();
    }
}
