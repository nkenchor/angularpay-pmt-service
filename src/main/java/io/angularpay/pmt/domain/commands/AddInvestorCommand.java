package io.angularpay.pmt.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.pmt.adapters.outbound.MongoAdapter;
import io.angularpay.pmt.adapters.outbound.RedisAdapter;
import io.angularpay.pmt.configurations.AngularPayConfiguration;
import io.angularpay.pmt.domain.*;
import io.angularpay.pmt.exceptions.CommandException;
import io.angularpay.pmt.exceptions.ErrorObject;
import io.angularpay.pmt.helpers.CommandHelper;
import io.angularpay.pmt.models.*;
import io.angularpay.pmt.validation.DefaultConstraintValidator;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static io.angularpay.pmt.exceptions.ErrorCode.TARGET_AMOUNT_BOUNDS_ERROR;
import static io.angularpay.pmt.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.pmt.helpers.CommandHelper.validRequestStatusOrThrow;
import static io.angularpay.pmt.helpers.Helper.getAllPartiesExceptActor;
import static io.angularpay.pmt.models.UserNotificationType.PEER_INVESTOR_ADDED;
import static io.angularpay.pmt.models.UserNotificationType.SOLO_INVESTOR_ADDED;

@Service
public class AddInvestorCommand extends AbstractCommand<AddInvestorCommandRequest, GenericReferenceResponse>
        implements UpdatesPublisherCommand<GenericCommandResponse>,
        ResourceReferenceCommand<GenericCommandResponse, ResourceReferenceResponse>,
        TTLPublisherCommand<GenericCommandResponse>,
        UserNotificationsPublisherCommand<GenericCommandResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;
    private final AngularPayConfiguration configuration;

    public AddInvestorCommand(ObjectMapper mapper, MongoAdapter mongoAdapter, DefaultConstraintValidator validator, CommandHelper commandHelper, RedisAdapter redisAdapter, AngularPayConfiguration configuration) {
        super("AddInvestorCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
        this.configuration = configuration;
    }

    @Override
    protected String getResourceOwner(AddInvestorCommandRequest request) {
        return request.getAuthenticatedUser().getUserReference();
    }

    @Override
    protected GenericCommandResponse handle(AddInvestorCommandRequest request) {
        PmtRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        validRequestStatusOrThrow(found);
        Supplier<GenericCommandResponse> supplier = () -> addInvestor(request);
        return this.commandHelper.executeAcid(supplier);
    }

    private GenericCommandResponse addInvestor(AddInvestorCommandRequest request) throws OptimisticLockingFailureException {
        PmtRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        BigDecimal targetAmount = new BigDecimal( found.getAmount().getValue());
        BigDecimal runningTotal = found.getInvestors().stream()
                .filter(x->!x.isDeleted())
                .map(x -> new BigDecimal( x.getAmount().getValue())).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (runningTotal.compareTo(targetAmount) < 0 &&
                runningTotal.add(new BigDecimal(request.getAddInvestorApiModel().getValue())).compareTo(targetAmount) <= 0) {
            Investor investor = Investor.builder()
                    .amount(Amount.builder()
                            .currency(request.getAddInvestorApiModel().getCurrency())
                            .value(request.getAddInvestorApiModel().getValue())
                            .build())
                    .comment(request.getAddInvestorApiModel().getComment())
                    .reference(UUID.randomUUID().toString())
                    .userReference(request.getAuthenticatedUser().getUserReference())
                    .createdOn(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
                    .investmentStatus(InvestmentStatus.builder()
                            .status(InvestmentTransactionStatus.PENDING)
                            .build())
                    .build();
            PmtRequest response = this.commandHelper.addItemToCollection(found, investor, found::getInvestors, found::setInvestors);
            return GenericCommandResponse.builder()
                    .requestReference(found.getReference())
                    .itemReference(investor.getReference())
                    .pmtRequest(response)
                    .build();
        }
        throw CommandException.builder()
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .errorCode(TARGET_AMOUNT_BOUNDS_ERROR)
                .message(TARGET_AMOUNT_BOUNDS_ERROR.getDefaultMessage())
                .build();
    }

    @Override
    protected List<ErrorObject> validate(AddInvestorCommandRequest request) {
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
    public ResourceReferenceResponse map(GenericCommandResponse genericCommandResponse) {
        return new ResourceReferenceResponse(genericCommandResponse.getItemReference());
    }

    @Override
    public RedisAdapter getRedisAdapter() {
        return this.redisAdapter;
    }

    @Override
    public UserNotificationType getUserNotificationType(GenericCommandResponse commandResponse) {
        PmtRequest request = commandResponse.getPmtRequest();
        Optional<Investor> optionalInvestor = request.getInvestors().stream()
                .filter(x -> x.getReference().equalsIgnoreCase(commandResponse.getItemReference()))
                .findFirst();

        if (optionalInvestor.isEmpty()) {
            return PEER_INVESTOR_ADDED;
        } else {
            String value = optionalInvestor.get().getAmount().getValue();
            BigDecimal investment = new BigDecimal(value);
            BigDecimal targetAmount = new BigDecimal(request.getAmount().getValue());
            int result = investment.compareTo(targetAmount);
            return result == 0 ? SOLO_INVESTOR_ADDED : PEER_INVESTOR_ADDED;
        }
    }

    @Override
    public List<String> getAudience(GenericCommandResponse commandResponse) {
        return getAllPartiesExceptActor(commandResponse.getPmtRequest(), commandResponse.getItemReference());
    }

    @Override
    public String convertToUserNotificationsMessage(UserNotificationBuilderParameters<GenericCommandResponse, PmtRequest> parameters) throws JsonProcessingException {
        Optional<Investor> optional = parameters.getCommandResponse().getPmtRequest().getInvestors().stream()
                .filter(x -> x.getReference().equalsIgnoreCase(parameters.getCommandResponse().getItemReference()))
                .findFirst();

        Amount amount;
        if (optional.isEmpty()) {
            amount = Amount.builder().currency("X").value("Y").build();
        } else {
            amount = optional.get().getAmount();
        }

        String template;
        if (parameters.getCommandResponse().getPmtRequest().getInvestee().getUserReference()
                .equalsIgnoreCase(parameters.getUserReference())) {
            template = "someone wants to invest %s %s on your PMT post";
        } else {
            template = "someone else wants to invest %s %s on a PMT post that you commented on";
        }

        String summary = String.format(template, amount.getValue(), amount.getCurrency());

        UserNotificationInvestmentPayload userNotificationInvestmentPayload = UserNotificationInvestmentPayload.builder()
                .requestReference(parameters.getCommandResponse().getRequestReference())
                .investmentReference(parameters.getCommandResponse().getItemReference())
                .build();
        String payload = mapper.writeValueAsString(userNotificationInvestmentPayload);

        String attributes = mapper.writeValueAsString(parameters.getRequest());

        UserNotification userNotification = UserNotification.builder()
                .reference(UUID.randomUUID().toString())
                .createdOn(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
                .serviceCode(parameters.getRequest().getServiceCode())
                .userReference(parameters.getUserReference())
                .type(parameters.getType())
                .summary(summary)
                .payload(payload)
                .attributes(attributes)
                .build();

        return mapper.writeValueAsString(userNotification);
    }

    @Override
    public String convertToTTLMessage(PmtRequest pmtRequest, GenericCommandResponse genericCommandResponse) throws JsonProcessingException {
        URI deletionLink = UriComponentsBuilder.fromUriString(configuration.getSelfUrl())
                .path("/pmt/requests/")
                .path(genericCommandResponse.getRequestReference())
                .path("/investors/")
                .path(genericCommandResponse.getItemReference())
                .path("/ttl")
                .build().toUri();

        return this.commandHelper.toJsonString(TimeToLiveModel.builder()
                .serviceCode(pmtRequest.getServiceCode())
                .requestReference(pmtRequest.getReference())
                .investmentReference(genericCommandResponse.getItemReference())
                .requestCreatedOn(pmtRequest.getCreatedOn())
                .deletionLink(deletionLink.toString())
                .build());
    }
}
