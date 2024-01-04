package antifraud.fraud;

import org.springframework.data.repository.CrudRepository;

public interface LimitRepository extends CrudRepository<Limit, Long> {
    Limit findByResult(Transaction.Result result);

    default boolean isInitialisedWithLimits() {
        return getAllowedLimit() != null;
    }

    default Limit getAllowedLimit() {
        return findByResult(Transaction.Result.ALLOWED);
    }

    default Limit getManualProcessingLimit() {
        return findByResult(Transaction.Result.MANUAL_PROCESSING);
    }
}
