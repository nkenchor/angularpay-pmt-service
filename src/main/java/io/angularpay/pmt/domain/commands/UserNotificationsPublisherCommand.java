package io.angularpay.pmt.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.angularpay.pmt.adapters.outbound.RedisAdapter;
import io.angularpay.pmt.domain.PmtRequest;
import io.angularpay.pmt.models.UserNotificationBuilderParameters;
import io.angularpay.pmt.models.UserNotificationType;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

public interface UserNotificationsPublisherCommand<T extends PmtRequestSupplier> {

    RedisAdapter getRedisAdapter();
    UserNotificationType getUserNotificationType(T commandResponse);
    List<String> getAudience(T commandResponse);
    String convertToUserNotificationsMessage(UserNotificationBuilderParameters<T, PmtRequest> parameters) throws JsonProcessingException;

    default void publishUserNotification(T commandResponse) {
        PmtRequest request = commandResponse.getPmtRequest();
        RedisAdapter redisAdapter = this.getRedisAdapter();
        UserNotificationType type = this.getUserNotificationType(commandResponse);
        List<String> audience = this.getAudience(commandResponse);

        if (Objects.nonNull(request) && Objects.nonNull(redisAdapter)
        && Objects.nonNull(type) && !CollectionUtils.isEmpty(audience)) {
            audience.stream().parallel().forEach(userReference-> {
                try {
                    UserNotificationBuilderParameters<T, PmtRequest> parameters = UserNotificationBuilderParameters.<T, PmtRequest>builder()
                            .userReference(userReference)
                            .request(request)
                            .commandResponse(commandResponse)
                            .type(type)
                            .build();
                    String message = this.convertToUserNotificationsMessage(parameters);
                    redisAdapter.publishUserNotification(message);
                } catch (JsonProcessingException exception) {
                    throw new RuntimeException(exception);
                }
            });
        }
    }
}
