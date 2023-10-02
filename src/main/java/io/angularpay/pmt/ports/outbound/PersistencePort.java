package io.angularpay.pmt.ports.outbound;

import io.angularpay.pmt.domain.PmtRequest;
import io.angularpay.pmt.domain.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PersistencePort {
    PmtRequest createRequest(PmtRequest request);
    PmtRequest updateRequest(PmtRequest request);
    Optional<PmtRequest> findRequestByReference(String reference);
    Page<PmtRequest> listRequests(Pageable pageable);
    Page<PmtRequest> findRequestsByStatus(Pageable pageable, List<RequestStatus> statuses);
    Page<PmtRequest> findRequestsByVerification(Pageable pageable, boolean verified);
    Page<PmtRequest> findByInvesteeUserReference(Pageable pageable, String userReference);
    long getCountByVerificationStatus(boolean verified);
    long getCountByRequestStatus(RequestStatus status);
    long getTotalCount();
}
