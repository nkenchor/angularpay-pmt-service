package io.angularpay.pmt.adapters.inbound;

import io.angularpay.pmt.domain.commands.PlatformConfigurationsConverterCommand;
import io.angularpay.pmt.models.platform.PlatformConfigurationIdentifier;
import io.angularpay.pmt.ports.inbound.InboundMessagingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static io.angularpay.pmt.models.platform.PlatformConfigurationSource.TOPIC;

@Service
@RequiredArgsConstructor
public class RedisMessageAdapter implements InboundMessagingPort {

    private final PlatformConfigurationsConverterCommand converterCommand;

    @Override
    public void onMessage(String message, PlatformConfigurationIdentifier identifier) {
        this.converterCommand.execute(message, identifier, TOPIC);
    }
}
