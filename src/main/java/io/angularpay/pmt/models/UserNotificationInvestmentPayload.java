package io.angularpay.pmt.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserNotificationInvestmentPayload {

    @JsonProperty("request_reference")
    private String requestReference;

    @JsonProperty("investment_reference")
    private String investmentReference;
}
