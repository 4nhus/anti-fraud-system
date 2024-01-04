package antifraud.fraud;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

public interface CardRepository extends CrudRepository<Card, Long>, ListPagingAndSortingRepository<Card, Long> {
    Card findByNumber(String number);
}