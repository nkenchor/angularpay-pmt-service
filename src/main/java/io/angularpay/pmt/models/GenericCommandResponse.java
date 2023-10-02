
package io.angularpay.pmt.models;

import io.angularpay.pmt.domain.PmtRequest;
import io.angularpay.pmt.domain.commands.PmtRequestSupplier;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class GenericCommandResponse extends GenericReferenceResponse implements PmtRequestSupplier {

    private final String requestReference;
    private final String itemReference;
    private final PmtRequest pmtRequest;
}
