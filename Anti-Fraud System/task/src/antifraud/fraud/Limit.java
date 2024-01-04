package antifraud.fraud;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "limits")
@Data
@NoArgsConstructor
public class Limit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Enumerated(value = EnumType.STRING)
    private Transaction.Result result;
    private long limitAmount;

    public Limit(Transaction.Result result) {
        this.result = result;
        this.limitAmount = result.equals(Transaction.Result.ALLOWED) ? 200L : 1500L;
    }
}
