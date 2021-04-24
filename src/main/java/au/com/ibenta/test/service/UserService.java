package au.com.ibenta.test.service;

import au.com.ibenta.test.exception.ResourceNotFoundException;
import au.com.ibenta.test.persistence.UserEntity;
import au.com.ibenta.test.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity create(UserEntity userEntity) {
        return userRepository.save(userEntity);
    }

    public UserEntity get(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public UserEntity update(UserEntity userEntity) {
        UserEntity userToUpdate = userRepository.findById(userEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException(userEntity.getId()));

        userToUpdate.setEmail(userEntity.getEmail());
        userToUpdate.setFirstName(userEntity.getFirstName());
        userToUpdate.setLastName(userEntity.getLastName());
        userToUpdate.setPassword(userEntity.getPassword());

        return userRepository.save(userToUpdate);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException(id);
        }

        userRepository.deleteById(id);
    }

    public List<UserEntity> list() {
        return userRepository.findAll();
    }
}