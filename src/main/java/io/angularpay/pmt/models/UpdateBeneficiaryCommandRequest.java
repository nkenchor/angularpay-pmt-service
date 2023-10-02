package io.angularpay.pmt.models;

import io.angularpay.pmt.domain.Beneficiary;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UpdateBeneficiaryCommandRequest extends AccessControl {

    @NotEmpty
    private String requestReference;

    @NotNull
    @Valid
    private Beneficiary beneficiary;

    UpdateBeneficiaryCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
