
package io.angularpay.pmt.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document("pmt_requests")
public class PmtRequest {

    @Id
    private String id;
    @Version
    private int version;
    @JsonProperty("service_code")
    private String serviceCode;
    private boolean verified;
    @JsonProperty("verified_on")
    private String verifiedOn;
    private Amount amount;
    private Bargain bargain;
    private Beneficiary beneficiary;
    @JsonProperty("created_on")
    private String createdOn;
    @JsonProperty("exchange_rate")
    private ExchangeRate exchangeRate;
    private Investee investee;
    private List<Investor> investors;
    @JsonProperty("last_modified")
    private String lastModified;
    private String reference;
    @JsonProperty("request_tag")
    private String requestTag;
    private RequestStatus status;
}
