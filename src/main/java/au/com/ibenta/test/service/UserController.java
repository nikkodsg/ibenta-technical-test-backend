package au.com.ibenta.test.service;

import au.com.ibenta.test.model.User;
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
    public Mono<User> create(@RequestBody User user) {
        return userService.create(user);
    }

    @GetMapping("/{id}")
    @ResponseStatus(OK)
    public Mono<User> get(@PathVariable Long id) {
        return userService.get(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(OK)
    public Mono<User> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        Mono<User> updatedUserMono = userService.update(user);
        return updatedUserMono;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return userService.delete(id);
    }

    @GetMapping
    @ResponseStatus(OK)
    public Flux<User> list() {
        return userService.list();
    }
}
