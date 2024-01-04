package antifraud.fraud;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

public interface IPRepository extends CrudRepository<IP, Long>, ListPagingAndSortingRepository<IP, Long> {
    IP findByIp(String ip);
}
