package io.angularpay.pmt.adapters.outbound;

import io.angularpay.pmt.domain.PmtRequest;
import io.angularpay.pmt.domain.RequestStatus;
import io.angularpay.pmt.ports.outbound.PersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MongoAdapter implements PersistencePort {

    private final PmtRepository pmtRepository;

    @Override
    public PmtRequest createRequest(PmtRequest request) {
        request.setCreatedOn(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        request.setLastModified(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        return pmtRepository.save(request);
    }

    @Override
    public PmtRequest updateRequest(PmtRequest request) {
        request.setLastModified(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        return pmtRepository.save(request);
    }

    @Override
    public Optional<PmtRequest> findRequestByReference(String reference) {
        return pmtRepository.findByReference(reference);
    }

    @Override
    public Page<PmtRequest> listRequests(Pageable pageable) {
        return pmtRepository.findAll(pageable);
    }

    @Override
    public Page<PmtRequest> findRequestsByStatus(Pageable pageable, List<RequestStatus> statuses) {
        return pmtRepository.findByStatusIn(pageable, statuses);
    }

    @Override
    public Page<PmtRequest> findRequestsByVerification(Pageable pageable, boolean verified) {
        return pmtRepository.findByVerified(pageable, verified);
    }

    @Override
    public Page<PmtRequest> findByInvesteeUserReference(Pageable pageable, String userReference) {
        return pmtRepository.findAByInvesteeUserReference(pageable, userReference);
    }

    @Override
    public long getCountByVerificationStatus(boolean verified) {
        return pmtRepository.countByVerified(verified);
    }

    @Override
    public long getCountByRequestStatus(RequestStatus status) {
        return pmtRepository.countByStatus(status);
    }

    @Override
    public long getTotalCount() {
        return pmtRepository.count();
    }
}
