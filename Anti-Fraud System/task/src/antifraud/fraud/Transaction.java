package antifraud.fraud;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.IOException;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long transactionId;
    @NotNull
    private long amount;
    @NotBlank
    private String ip;
    @NotBlank
    private String number;
    @Enumerated(EnumType.STRING)
    private Region region;
    private LocalDateTime date;
    @Enumerated(EnumType.STRING)
    private Result result;
    @Enumerated(EnumType.STRING)
    @JsonSerialize(nullsUsing = NullResultSerializer.class)
    private Result feedback;

    public enum Region {
        EAP, ECA, HIC, LAC, MENA, SA, SSA
    }

    public enum Result {
        ALLOWED, MANUAL_PROCESSING, PROHIBITED
    }

    public static class NullResultSerializer extends StdSerializer<Result> {
        public NullResultSerializer() {
            super(Result.class);
        }

        @Override
        public void serialize(Result value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value == null ? "" : value.name());
        }
    }
}
