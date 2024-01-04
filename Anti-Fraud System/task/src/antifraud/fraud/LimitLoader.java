package antifraud.fraud;

import org.springframework.stereotype.Component;

@Component
public class LimitLoader {
    private final LimitRepository limitRepository;

    public LimitLoader(LimitRepository limitRepository) {
        this.limitRepository = limitRepository;

        if (!limitRepository.isInitialisedWithLimits()) {
            initialiseLimits();
        }
    }

    private void initialiseLimits() {
        limitRepository.save(new Limit(Transaction.Result.ALLOWED));
        limitRepository.save(new Limit(Transaction.Result.MANUAL_PROCESSING));
    }
}
