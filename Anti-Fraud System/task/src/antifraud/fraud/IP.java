package antifraud.fraud;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ips")
@Data
@NoArgsConstructor
public class IP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String ip;

    public IP(String ip) {
        this.ip = ip;
    }
}
