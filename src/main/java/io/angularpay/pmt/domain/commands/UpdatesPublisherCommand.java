package io.angularpay.pmt.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.angularpay.pmt.adapters.outbound.RedisAdapter;
import io.angularpay.pmt.domain.PmtRequest;

import java.util.Objects;

public interface UpdatesPublisherCommand<T extends PmtRequestSupplier> {

    RedisAdapter getRedisAdapter();

    String convertToUpdatesMessage(PmtRequest pmtRequest) throws JsonProcessingException;

    default void publishUpdates(T t) {
        PmtRequest pmtRequest = t.getPmtRequest();
        RedisAdapter redisAdapter = this.getRedisAdapter();
        if (Objects.nonNull(pmtRequest) && Objects.nonNull(redisAdapter)) {
            try {
                String message = this.convertToUpdatesMessage(pmtRequest);
                redisAdapter.publishUpdates(message);
            } catch (JsonProcessingException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
