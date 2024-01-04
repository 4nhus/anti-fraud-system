package antifraud.user;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long>, ListPagingAndSortingRepository<User, Long> {
    User findByUsername(String username);

    List<User> findUsersByRole(User.Role role);

    default User getAdmin() {
        return findUsersByRole(User.Role.ADMINISTRATOR).size() == 1 ? findUsersByRole(User.Role.ADMINISTRATOR).get(0) : null;
    }
}
