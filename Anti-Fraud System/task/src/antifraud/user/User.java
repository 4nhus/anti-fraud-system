package antifraud.user;

import antifraud.AntiFraudController;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotBlank
    private String name;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean locked;

    public AntiFraudController.UserResponse toResponse() {
        return new AntiFraudController.UserResponse(id, name, username, role.name());
    }

    public enum Role {
        MERCHANT, ADMINISTRATOR, SUPPORT
    }
}
