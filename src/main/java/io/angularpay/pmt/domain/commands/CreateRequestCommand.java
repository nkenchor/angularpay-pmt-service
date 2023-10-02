package io.angularpay.pmt.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.pmt.adapters.outbound.MongoAdapter;
import io.angularpay.pmt.adapters.outbound.RedisAdapter;
import io.angularpay.pmt.domain.Beneficiary;
import io.angularpay.pmt.domain.Investee;
import io.angularpay.pmt.domain.PmtRequest;
import io.angularpay.pmt.domain.Role;
import io.angularpay.pmt.exceptions.ErrorObject;
import io.angularpay.pmt.helpers.CommandHelper;
import io.angularpay.pmt.models.CreateRequestCommandRequest;
import io.angularpay.pmt.models.GenericCommandResponse;
import io.angularpay.pmt.models.GenericReferenceResponse;
import io.angularpay.pmt.models.ResourceReferenceResponse;
import io.angularpay.pmt.validation.DefaultConstraintValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static io.angularpay.pmt.helpers.ObjectFactory.pmtRequestWithDefaults;

@Slf4j
@Service
public class CreateRequestCommand extends AbstractCommand<CreateRequestCommandRequest, GenericReferenceResponse>
        implements UpdatesPublisherCommand<GenericCommandResponse>,
        ResourceReferenceCommand<GenericCommandResponse, ResourceReferenceResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;

    public CreateRequestCommand(ObjectMapper mapper, MongoAdapter mongoAdapter, DefaultConstraintValidator validator, CommandHelper commandHelper, RedisAdapter redisAdapter) {
        super("CreateRequestCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
    }

    @Override
    protected String getResourceOwner(CreateRequestCommandRequest request) {
        return request.getAuthenticatedUser().getUserReference();
    }

    @Override
    protected GenericCommandResponse handle(CreateRequestCommandRequest request) {
        PmtRequest pmtRequestWithDefaults = pmtRequestWithDefaults();
        PmtRequest withOtherDetails = pmtRequestWithDefaults.toBuilder()
                .amount(request.getCreateRequest().getAmount())
                .exchangeRate(request.getCreateRequest().getExchangeRate())
                .investee(Investee.builder()
                        .userReference(request.getAuthenticatedUser().getUserReference())
                        .build())
                .beneficiary(Beneficiary.builder()
                        .userReference(request.getCreateRequest().getBeneficiaryReference())
                        .build())
                .build();
        PmtRequest response = this.mongoAdapter.createRequest(withOtherDetails);
        return GenericCommandResponse.builder()
                .requestReference(response.getReference())
                .pmtRequest(response)
                .build();
    }

    @Override
    protected List<ErrorObject> validate(CreateRequestCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }

    @Override
    public RedisAdapter getRedisAdapter() {
        return this.redisAdapter;
    }

    @Override
    public String convertToUpdatesMessage(PmtRequest pmtRequest) throws JsonProcessingException {
        return this.commandHelper.toJsonString(pmtRequest);
    }

    @Override
    public ResourceReferenceResponse map(GenericCommandResponse genericCommandResponse) {
        return new ResourceReferenceResponse(genericCommandResponse.getRequestReference());
    }
}
