package io.angularpay.pmt.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.pmt.adapters.outbound.MongoAdapter;
import io.angularpay.pmt.adapters.outbound.RedisAdapter;
import io.angularpay.pmt.domain.Amount;
import io.angularpay.pmt.domain.InvestmentTransactionStatus;
import io.angularpay.pmt.domain.PmtRequest;
import io.angularpay.pmt.domain.Role;
import io.angularpay.pmt.exceptions.CommandException;
import io.angularpay.pmt.exceptions.ErrorObject;
import io.angularpay.pmt.helpers.CommandHelper;
import io.angularpay.pmt.models.GenericCommandResponse;
import io.angularpay.pmt.models.UpdateInvestmentAmountCommandRequest;
import io.angularpay.pmt.validation.DefaultConstraintValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static io.angularpay.pmt.exceptions.ErrorCode.REQUEST_REMOVED_ERROR;
import static io.angularpay.pmt.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.pmt.helpers.CommandHelper.validRequestStatusAndInvestmentExists;

@Service
public class UpdateInvestmentAmountCommand extends AbstractCommand<UpdateInvestmentAmountCommandRequest, GenericCommandResponse>
        implements UpdatesPublisherCommand<GenericCommandResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;
    
    public UpdateInvestmentAmountCommand(ObjectMapper mapper, MongoAdapter mongoAdapter, DefaultConstraintValidator validator, CommandHelper commandHelper, RedisAdapter redisAdapter) {
        super("UpdateInvestmentAmountCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
    }

    @Override
    protected String getResourceOwner(UpdateInvestmentAmountCommandRequest request) {
        return this.commandHelper.getInvestmentOwner(request.getRequestReference(), request.getInvestmentReference());
    }

    @Override
    protected GenericCommandResponse handle(UpdateInvestmentAmountCommandRequest request) {
        PmtRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        String investmentReference = request.getInvestmentReference();
        validRequestStatusAndInvestmentExists(found, investmentReference);
        Supplier<GenericCommandResponse> supplier = () -> updateInvestmentAmount(request);
        return this.commandHelper.executeAcid(supplier);
    }

    private GenericCommandResponse updateInvestmentAmount(UpdateInvestmentAmountCommandRequest request) {
        PmtRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        found.getInvestors().forEach(x-> {
            if (request.getInvestmentReference().equalsIgnoreCase(x.getReference())) {
                if (x.isDeleted() || (Objects.nonNull(x.getInvestmentStatus()) && x.getInvestmentStatus().getStatus() == InvestmentTransactionStatus.SUCCESSFUL)) {
                    throw CommandException.builder()
                            .status(HttpStatus.UNPROCESSABLE_ENTITY)
                            .errorCode(REQUEST_REMOVED_ERROR)
                            .message(REQUEST_REMOVED_ERROR.getDefaultMessage())
                            .build();
                }
                Amount amount = Amount.builder()
                        .currency(request.getUpdateInvestmentApiModel().getCurrency())
                        .value(request.getUpdateInvestmentApiModel().getValue())
                        .build();
                x.setAmount(amount);
                x.setComment(request.getUpdateInvestmentApiModel().getComment());
            }
        });
        PmtRequest response = this.mongoAdapter.updateRequest(found);
        return GenericCommandResponse.builder()
                .requestReference(response.getReference())
                .pmtRequest(response)
                .build();
    }

    @Override
    protected List<ErrorObject> validate(UpdateInvestmentAmountCommandRequest request) {
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
