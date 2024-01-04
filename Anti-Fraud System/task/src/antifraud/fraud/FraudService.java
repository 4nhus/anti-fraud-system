package antifraud.fraud;

import antifraud.AntiFraudController;
import antifraud.exceptions.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FraudService {
    private final IPRepository ipRepository;
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final LimitRepository limitRepository;

    public FraudService(IPRepository ipRepository, CardRepository cardRepository, TransactionRepository transactionRepository, LimitRepository limitRepository) {
        this.ipRepository = ipRepository;
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
        this.limitRepository = limitRepository;
    }

    public IP addSuspiciousIp(String ip) {
        if (ipRepository.findByIp(ip) != null) {
            throw new SuspiciousIPAlreadyExistsException();
        }

        return ipRepository.save(new IP(ip));
    }

    public void deleteSuspiciousIP(String ip) {
        IP suspiciousIP = ipRepository.findByIp(ip);

        if (suspiciousIP == null) {
            throw new SuspiciousIPNotFoundException();
        }

        ipRepository.delete(suspiciousIP);
    }

    public List<IP> getSuspiciousIPs() {
        return ipRepository.findAll(Sort.by("id"));
    }

    public Card addStolenCard(String number) {
        if (cardRepository.findByNumber(number) != null) {
            throw new StolenCardAlreadyExistsException();
        }

        return cardRepository.save(new Card(number));
    }

    public void deleteStolenCard(String number) {
        Card stolenCard = cardRepository.findByNumber(number);

        if (stolenCard == null) {
            throw new StolenCardNotFoundException();
        }

        cardRepository.delete(stolenCard);
    }

    public List<Card> getStolenCards() {
        return cardRepository.findAll(Sort.by("id"));
    }

    public AntiFraudController.TransactionResponse processTransaction(@Valid Transaction transaction) {
        Transaction.Result result = Transaction.Result.ALLOWED;
        List<String> info = new ArrayList<>();

        if (transaction.getAmount() > limitRepository.getManualProcessingLimit().getLimitAmount()) {
            result = Transaction.Result.PROHIBITED;
            info.add("amount");
        }

        if (ipRepository.findByIp(transaction.getIp()) != null) {
            result = Transaction.Result.PROHIBITED;
            info.add("ip");
        }

        if (cardRepository.findByNumber(transaction.getNumber()) != null) {
            result = Transaction.Result.PROHIBITED;
            info.add("card-number");
        }

        transaction = transactionRepository.save(transaction);

        LocalDateTime oneHourBeforeTransaction = transaction.getDate().minusHours(1);
        int numberOfOutOfRegionTransactions = transactionRepository.findNumberOfUniqueRegionsInBetweenDates(transaction.getNumber(), oneHourBeforeTransaction, transaction.getDate());
        int numberOfUniqueIPTransactions = transactionRepository.findNumberOfUniqueIPsInBetweenDates(transaction.getNumber(), oneHourBeforeTransaction, transaction.getDate());

        if (numberOfOutOfRegionTransactions > 3) {
            result = Transaction.Result.PROHIBITED;
            info.add("region-correlation");
        }

        if (numberOfUniqueIPTransactions > 3) {
            result = Transaction.Result.PROHIBITED;
            info.add("ip-correlation");
        }

        if (result == Transaction.Result.ALLOWED) {
            if (transaction.getAmount() > limitRepository.getAllowedLimit().getLimitAmount() && transaction.getAmount() <= limitRepository.getManualProcessingLimit().getLimitAmount()) {
                result = Transaction.Result.MANUAL_PROCESSING;
                info.add("amount");
            }

            if (numberOfOutOfRegionTransactions == 3) {
                result = Transaction.Result.MANUAL_PROCESSING;
                info.add("region-correlation");
            }

            if (numberOfUniqueIPTransactions == 3) {
                result = Transaction.Result.MANUAL_PROCESSING;
                info.add("ip-correlation");
            }
        }

        transaction.setResult(result);
        transactionRepository.save(transaction);

        return new AntiFraudController.TransactionResponse(result.name(), info.isEmpty() ? "none" : String.join(", ", info.stream().sorted().toList()));
    }

    private void increaseAllowedLimit(long transactionAmount) {
        Limit allowedLimit = limitRepository.getAllowedLimit();
        allowedLimit.setLimitAmount((long) Math.ceil(0.8 * allowedLimit.getLimitAmount() + 0.2 * transactionAmount));
        limitRepository.save(allowedLimit);
    }

    private void increaseManualProcessingLimit(long transactionAmount) {
        Limit manualProcessingLimit = limitRepository.getManualProcessingLimit();
        manualProcessingLimit.setLimitAmount((long) Math.ceil(0.8 * manualProcessingLimit.getLimitAmount() + 0.2 * transactionAmount));
        limitRepository.save(manualProcessingLimit);
    }

    private void decreaseAllowedLimit(long transactionAmount) {
        Limit allowedLimit = limitRepository.getAllowedLimit();
        allowedLimit.setLimitAmount((long) Math.ceil(0.8 * allowedLimit.getLimitAmount() - 0.2 * transactionAmount));
        limitRepository.save(allowedLimit);
    }

    private void decreaseManualProcessingLimit(long transactionAmount) {
        Limit manualProcessingLimit = limitRepository.getManualProcessingLimit();
        manualProcessingLimit.setLimitAmount((long) Math.ceil(0.8 * manualProcessingLimit.getLimitAmount() - 0.2 * transactionAmount));
        limitRepository.save(manualProcessingLimit);
    }

    public Transaction addFeedbackForTransaction(long transactionId, Transaction.Result feedback) {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(TransactionNotFoundException::new);

        if (transaction.getFeedback() != null) {
            throw new TransactionFeedbackAlreadyExistsException();
        }

        switch (feedback) {
            case ALLOWED -> {
                switch (transaction.getResult()) {
                    case ALLOWED -> throw new TransactionFeedbackMatchesResultException();
                    case MANUAL_PROCESSING -> increaseAllowedLimit(transaction.getAmount());
                    default -> {
                        increaseAllowedLimit(transaction.getAmount());
                        increaseManualProcessingLimit(transaction.getAmount());
                    }
                }
            }
            case MANUAL_PROCESSING -> {
                switch (transaction.getResult()) {
                    case ALLOWED -> decreaseAllowedLimit(transaction.getAmount());
                    case MANUAL_PROCESSING -> throw new TransactionFeedbackMatchesResultException();
                    default -> increaseManualProcessingLimit(transaction.getAmount());
                }
            }
            default -> {
                switch (transaction.getResult()) {
                    case ALLOWED -> {
                        decreaseAllowedLimit(transaction.getAmount());
                        decreaseManualProcessingLimit(transaction.getAmount());
                    }
                    case MANUAL_PROCESSING -> decreaseManualProcessingLimit(transaction.getAmount());
                    default -> throw new TransactionFeedbackMatchesResultException();
                }
            }
        }

        transaction.setFeedback(feedback);

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactions() {
        return transactionRepository.findAll(Sort.by("transactionId"));
    }

    public List<Transaction> getTransactionsForNumber(String number) {
        List<Transaction> transactions = transactionRepository.findAllByNumber(number);

        if (transactions.isEmpty()) {
            throw new TransactionsNotFoundException();
        }

        return transactionRepository.findAllByNumber(number);
    }
}
