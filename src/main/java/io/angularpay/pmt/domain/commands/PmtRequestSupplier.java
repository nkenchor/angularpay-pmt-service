package io.angularpay.pmt.domain.commands;

import io.angularpay.pmt.domain.PmtRequest;

public interface PmtRequestSupplier {
    PmtRequest getPmtRequest();
}
