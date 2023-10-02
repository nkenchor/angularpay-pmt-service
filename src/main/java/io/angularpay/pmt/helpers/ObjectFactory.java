package io.angularpay.pmt.helpers;

import io.angularpay.pmt.domain.Bargain;
import io.angularpay.pmt.domain.PmtRequest;
import io.angularpay.pmt.domain.RequestStatus;

import java.util.ArrayList;
import java.util.UUID;

import static io.angularpay.pmt.common.Constants.SERVICE_CODE;
import static io.angularpay.pmt.util.SequenceGenerator.generateRequestTag;

public class ObjectFactory {

    public static PmtRequest pmtRequestWithDefaults() {
        return PmtRequest.builder()
                .reference(UUID.randomUUID().toString())
                .serviceCode(SERVICE_CODE)
                .verified(false)
                .status(RequestStatus.ACTIVE)
                .requestTag(generateRequestTag())
                .investors(new ArrayList<>())
                .bargain(Bargain.builder()
                        .offers(new ArrayList<>())
                        .build())
                .build();
    }
}