package io.angularpay.pmt.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.pmt.adapters.outbound.MongoAdapter;
import io.angularpay.pmt.adapters.outbound.RedisAdapter;
import io.angularpay.pmt.domain.PmtRequest;
import io.angularpay.pmt.domain.Role;
import io.angularpay.pmt.exceptions.ErrorObject;
import io.angularpay.pmt.helpers.CommandHelper;
import io.angularpay.pmt.models.GenericCommandResponse;
import io.angularpay.pmt.models.GenericReferenceResponse;
import io.angularpay.pmt.models.UpdateExchangeRateCommandRequest;
import io.angularpay.pmt.validation.DefaultConstraintValidator;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static io.angularpay.pmt.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.pmt.helpers.CommandHelper.validRequestStatusOrThrow;

@Service
public class UpdateExchangeRateCommand extends AbstractCommand<UpdateExchangeRateCommandRequest, GenericReferenceResponse>
        implements UpdatesPublisherCommand<GenericCommandResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;

    public UpdateExchangeRateCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            CommandHelper commandHelper, RedisAdapter redisAdapter) {
        super("UpdateExchangeRateCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
    }

    @Override
    protected String getResourceOwner(UpdateExchangeRateCommandRequest request) {
        return this.commandHelper.getRequestOwner(request.getRequestReference());
    }

    @Override
    protected GenericCommandResponse handle(UpdateExchangeRateCommandRequest request) {
        PmtRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        validRequestStatusOrThrow(found);
        Supplier<GenericCommandResponse> supplier = () -> updateExchangeRate(request);
        return this.commandHelper.executeAcid(supplier);
    }

    private GenericCommandResponse updateExchangeRate(UpdateExchangeRateCommandRequest request) throws OptimisticLockingFailureException {
        PmtRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        PmtRequest response = this.commandHelper.updateProperty(found, request::getExchangeRate, found::setExchangeRate);
        return GenericCommandResponse.builder()
                .requestReference(response.getReference())
                .pmtRequest(response)
                .build();
    }

    @Override
    protected List<ErrorObject> validate(UpdateExchangeRateCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }

    @Override
    public String convertToUpdatesMessage(PmtRequest pmtRequest) throws JsonProcessingException {
        return this.commandHelper.toJsonString(pmtRequest);
    }

    @Override
    public RedisAdapter getRedisAdapter() {
        return this.redisAdapter;
    }
}
