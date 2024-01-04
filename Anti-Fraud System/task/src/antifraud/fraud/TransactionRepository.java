package antifraud.fraud;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends CrudRepository<Transaction, Long>, ListPagingAndSortingRepository<Transaction, Long> {
    @Query("SELECT COUNT(DISTINCT t.region) FROM Transaction t WHERE t.number = :number AND t.date BETWEEN :date AND :date2")
    int findNumberOfUniqueRegionsInBetweenDates(@Param("number") String number, @Param("date") LocalDateTime date, @Param("date2") LocalDateTime date2);

    @Query("SELECT COUNT(DISTINCT t.ip) FROM Transaction t WHERE t.number = :number AND t.date BETWEEN :date AND :date2")
    int findNumberOfUniqueIPsInBetweenDates(@Param("number") String number, @Param("date") LocalDateTime date, @Param("date2") LocalDateTime date2);

    List<Transaction> findAllByNumber(String number);
}
