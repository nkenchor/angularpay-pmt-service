package io.angularpay.pmt.ports.outbound;

import io.angularpay.pmt.models.SchedulerServiceRequest;
import io.angularpay.pmt.models.SchedulerServiceResponse;

import java.util.Map;
import java.util.Optional;

public interface SchedulerServicePort {
    Optional<SchedulerServiceResponse> createScheduledRequest(SchedulerServiceRequest request, Map<String, String> headers);
}
