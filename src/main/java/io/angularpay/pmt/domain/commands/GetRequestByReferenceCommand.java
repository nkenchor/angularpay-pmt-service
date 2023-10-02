package io.angularpay.pmt.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.pmt.adapters.outbound.MongoAdapter;
import io.angularpay.pmt.domain.PmtRequest;
import io.angularpay.pmt.domain.Role;
import io.angularpay.pmt.exceptions.ErrorObject;
import io.angularpay.pmt.models.GetRequestByReferenceCommandRequest;
import io.angularpay.pmt.validation.DefaultConstraintValidator;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static io.angularpay.pmt.helpers.CommandHelper.getRequestByReferenceOrThrow;

@Service
public class GetRequestByReferenceCommand extends AbstractCommand<GetRequestByReferenceCommandRequest, PmtRequest> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;

    public GetRequestByReferenceCommand(ObjectMapper mapper, MongoAdapter mongoAdapter, DefaultConstraintValidator validator) {
        super("GetRequestByReferenceCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
    }

    @Override
    protected String getResourceOwner(GetRequestByReferenceCommandRequest request) {
        return request.getAuthenticatedUser().getUserReference();
    }

    @Override
    protected PmtRequest handle(GetRequestByReferenceCommandRequest request) {
        return getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
    }

    @Override
    protected List<ErrorObject> validate(GetRequestByReferenceCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }
}
