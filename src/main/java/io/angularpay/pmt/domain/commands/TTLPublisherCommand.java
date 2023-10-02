package io.angularpay.pmt.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.angularpay.pmt.adapters.outbound.RedisAdapter;
import io.angularpay.pmt.domain.PmtRequest;

import java.util.Objects;

public interface TTLPublisherCommand<T extends PmtRequestSupplier> {

    RedisAdapter getRedisAdapter();

    String convertToTTLMessage(PmtRequest pmtRequest, T t) throws JsonProcessingException;

    default void publishTTL(T t) {
        PmtRequest pmtRequest = t.getPmtRequest();
        RedisAdapter redisAdapter = this.getRedisAdapter();
        if (Objects.nonNull(pmtRequest) && Objects.nonNull(redisAdapter)) {
            try {
                String message = this.convertToTTLMessage(pmtRequest, t);
                redisAdapter.publishTTL(message);
            } catch (JsonProcessingException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
