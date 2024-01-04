package antifraud;

import antifraud.fraud.Card;
import antifraud.fraud.FraudService;
import antifraud.fraud.IP;
import antifraud.fraud.Transaction;
import antifraud.user.User;
import antifraud.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
public class AntiFraudController {
    private final UserService userService;
    private final FraudService fraudService;

    public AntiFraudController(UserService userService, FraudService fraudService) {
        this.userService = userService;
        this.fraudService = fraudService;
    }

    private boolean checkIPIsInvalid(String ip) {
        String[] ipParts = ip.split("\\.");

        if (ipParts.length != 4) {
            return true;
        }

        try {
            if (Arrays.stream(ipParts).mapToInt(Integer::parseInt).anyMatch(ipPart -> ipPart < 0 || ipPart > 255)) {
                return true;
            }
        } catch (NumberFormatException e) {
            return true;
        }

        return false;
    }

    private boolean checkCardNumberIsInvalid(String number) {
        if (number.length() != 16) {
            return true;
        }

        try {
            Long.parseLong(number);
        } catch (NumberFormatException e) {
            return true;
        }

        int checkDigit = number.charAt(15) - '0';
        int sum = 0;
        for (int i = 14; i >= 0; i--) {
            int digit = number.charAt(i) - '0';
            if (i % 2 == 0) {
                digit *= 2;
            }
            if (digit > 9) {
                digit -= 9;
            }
            sum += digit;
        }

        return 10 - (sum % 10) != checkDigit;
    }

    @PostMapping("api/antifraud/transaction")
    public ResponseEntity<TransactionResponse> processTransaction(@Valid @RequestBody Transaction transaction) {
        if (transaction.getAmount() <= 0 || checkIPIsInvalid(transaction.getIp()) || checkCardNumberIsInvalid(transaction.getNumber())) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(fraudService.processTransaction(transaction));
    }

    @PutMapping("api/antifraud/transaction")
    public ResponseEntity<Transaction> updateTransaction(@RequestBody TransactionFeedbackRequest request) {
        if (!request.feedback.matches("(ALLOWED)|(MANUAL_PROCESSING)|(PROHIBITED)")) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(fraudService.addFeedbackForTransaction(request.transactionId, Transaction.Result.valueOf(request.feedback)));
    }

    @PostMapping("api/auth/user")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody User user) {
        user = userService.registerUser(user);

        return new ResponseEntity<>(user.toResponse(), HttpStatus.CREATED);
    }

    @GetMapping("api/auth/list")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(userService.getUsers().stream().map(User::toResponse).toList());
    }

    @DeleteMapping("api/auth/user/{username}")
    public ResponseEntity<DeleteUserResponse> deleteUser(@PathVariable String username) {
        username = username.toLowerCase();
        userService.deleteUser(username);

        return ResponseEntity.ok(new DeleteUserResponse(username, "Deleted successfully!"));
    }

    @PutMapping("/api/auth/role")
    public ResponseEntity<UserResponse> changeUserRole(@Valid @RequestBody ChangeUserRoleRequest request) {
        if (!request.role().matches("(SUPPORT)|(MERCHANT)")) {
            return ResponseEntity.badRequest().build();
        }

        User user = userService.changeUserRole(request.username(), User.Role.valueOf(request.role()));

        return ResponseEntity.ok(user.toResponse());
    }

    @PutMapping("api/auth/access")
    public ResponseEntity<StatusResponse> changeUserAccess(@RequestBody ChangeUserAccessRequest request) {
        userService.changeUserAccess(request.username.toLowerCase(), request.operation.equals("LOCK"));

        return ResponseEntity.ok(new StatusResponse("User " + request.username + " " + (request.operation.equals("LOCK") ? "locked" : "unlocked") + "!"));
    }

    @PostMapping("api/antifraud/suspicious-ip")
    public ResponseEntity<SuspiciousIpResponse> addSuspiciousIp(@Valid @RequestBody SuspiciousIpRequest request) {
        if (checkIPIsInvalid(request.ip)) {
            return ResponseEntity.badRequest().build();
        }

        IP suspiciousIp = fraudService.addSuspiciousIp(request.ip);

        return ResponseEntity.ok(new SuspiciousIpResponse(suspiciousIp.getId(), suspiciousIp.getIp()));
    }

    @GetMapping("api/antifraud/suspicious-ip")
    public ResponseEntity<List<IP>> getSuspiciousIPs() {
        return ResponseEntity.ok(fraudService.getSuspiciousIPs());
    }


    @DeleteMapping("api/antifraud/suspicious-ip/{ip}")
    public ResponseEntity<StatusResponse> deleteSuspiciousIp(@PathVariable String ip) {
        if (checkIPIsInvalid(ip)) {
            return ResponseEntity.badRequest().build();
        }

        fraudService.deleteSuspiciousIP(ip);

        return ResponseEntity.ok(new StatusResponse("IP " + ip + " successfully removed!"));
    }

    @PostMapping("api/antifraud/stolencard")
    public ResponseEntity<StolenCardResponse> addStolenCard(@Valid @RequestBody StolenCardRequest request) {
        if (checkCardNumberIsInvalid(request.number)) {
            return ResponseEntity.badRequest().build();
        }

        Card stolenCard = fraudService.addStolenCard(request.number);

        return ResponseEntity.ok(new StolenCardResponse(stolenCard.getId(), stolenCard.getNumber()));
    }

    @GetMapping("api/antifraud/stolencard")
    public ResponseEntity<List<Card>> getStolenCards() {
        return ResponseEntity.ok(fraudService.getStolenCards());
    }

    @DeleteMapping("api/antifraud/stolencard/{number}")
    public ResponseEntity<StatusResponse> deleteStolenCard(@PathVariable String number) {
        if (checkCardNumberIsInvalid(number)) {
            return ResponseEntity.badRequest().build();
        }

        fraudService.deleteStolenCard(number);

        return ResponseEntity.ok(new StatusResponse("Card " + number + " successfully removed!"));
    }

    @GetMapping("api/antifraud/history")
    public ResponseEntity<List<Transaction>> getTransactionsHistory() {
        return ResponseEntity.ok(fraudService.getTransactions());
    }

    @GetMapping("api/antifraud/history/{number}")
    public ResponseEntity<List<Transaction>> getTransactionsForCardNumber(@PathVariable String number) {
        if (checkCardNumberIsInvalid(number)) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(fraudService.getTransactionsForNumber(number));
    }

    public record TransactionResponse(String result, String info) {
    }

    public record UserResponse(long id, String name, String username, String role) {
    }

    public record DeleteUserResponse(String username, String status) {
    }

    public record ChangeUserRoleRequest(@NotBlank String username, @NotBlank String role) {
    }

    public record ChangeUserAccessRequest(String username, String operation) {
    }

    public record StatusResponse(String status) {
    }

    public record SuspiciousIpRequest(@NotBlank String ip) {
    }

    public record SuspiciousIpResponse(long id, String ip) {
    }

    public record StolenCardRequest(@NotBlank String number) {
    }

    public record StolenCardResponse(long id, String number) {
    }

    public record TransactionFeedbackRequest(long transactionId, String feedback) {
    }
}
