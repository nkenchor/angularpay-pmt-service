package io.angularpay.pmt.adapters.outbound;

import io.angularpay.pmt.domain.PmtRequest;
import io.angularpay.pmt.domain.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PmtRepository extends MongoRepository<PmtRequest, String> {

    Optional<PmtRequest> findByReference(String reference);
    Page<PmtRequest> findAll(Pageable pageable);
    Page<PmtRequest> findByStatusIn(Pageable pageable, List<RequestStatus> statuses);
    Page<PmtRequest> findByVerified(Pageable pageable, boolean verified);
    Page<PmtRequest> findAByInvesteeUserReference(Pageable pageable, String userReference);
    long countByVerified(boolean verified);
    long countByStatus(RequestStatus status);
}
