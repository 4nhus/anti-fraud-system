package antifraud.user;

import antifraud.exceptions.AdminAccessChangeException;
import antifraud.exceptions.UserAlreadyExistsException;
import antifraud.exceptions.UserAlreadyHasRoleException;
import antifraud.exceptions.UserNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public User registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername().toLowerCase()) != null) {
            throw new UserAlreadyExistsException();
        }

        user.setUsername(user.getUsername().toLowerCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (userRepository.getAdmin() == null) {
            user.setRole(User.Role.ADMINISTRATOR);
        } else {
            user.setRole(User.Role.MERCHANT);
        }

        user.setLocked(user.getRole() == User.Role.MERCHANT);

        return userRepository.save(user);
    }

    public List<User> getUsers() {
        return userRepository.findAll(Sort.by("id"));
    }

    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UserNotFoundException();
        }

        userRepository.delete(user);
    }

    public User changeUserRole(String username, User.Role role) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UserNotFoundException();
        }

        if (role == user.getRole()) {
            throw new UserAlreadyHasRoleException();
        }

        user.setRole(role);

        return userRepository.save(user);
    }

    public void changeUserAccess(String username, boolean locked) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UserNotFoundException();
        }

        if (user.equals(userRepository.getAdmin())) {
            throw new AdminAccessChangeException();
        }

        user.setLocked(locked);

        userRepository.save(user);
    }
}
