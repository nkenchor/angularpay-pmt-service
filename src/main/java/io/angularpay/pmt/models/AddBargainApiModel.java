
package io.angularpay.pmt.models;

import io.angularpay.pmt.domain.ExchangeRateType;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class AddBargainApiModel {

    @NotEmpty
    private String date;

    @NotEmpty
    private String from;

    @NotEmpty
    private String rate;

    @NotEmpty
    private String to;

    @NotNull
    private ExchangeRateType type;

    @NotEmpty
    private String comment;
}
