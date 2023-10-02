package io.angularpay.pmt.ports.inbound;

import io.angularpay.pmt.models.platform.PlatformConfigurationIdentifier;

public interface InboundMessagingPort {
    void onMessage(String message, PlatformConfigurationIdentifier identifier);
}
