package antifraud.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class AntiFraudExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public void handleUserAlreadyExists() {
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleUserNotFound() {
    }

    @ExceptionHandler(UserAlreadyHasRoleException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public void handleUserAlreadyHasRole() {
    }

    @ExceptionHandler(AdminAccessChangeException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public void handleAdminAccessChange() {
    }

    @ExceptionHandler(SuspiciousIPAlreadyExistsException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public void handleSuspiciousIPAlreadyExists() {
    }

    @ExceptionHandler(SuspiciousIPNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleSuspiciousIPNotFound() {
    }

    @ExceptionHandler(StolenCardAlreadyExistsException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public void handleStolenCardAlreadyExists() {
    }

    @ExceptionHandler(StolenCardNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleStolenCardNotFound() {
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleTransactionNotFound() {
    }

    @ExceptionHandler(TransactionFeedbackAlreadyExistsException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public void handleTransactionFeedbackAlreadyExists() {
    }

    @ExceptionHandler(TransactionFeedbackMatchesResultException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    public void handleTransactionFeedbackMatchesResult() {
    }

    @ExceptionHandler(TransactionsNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleTransactionsNotFound() {
    }
}

