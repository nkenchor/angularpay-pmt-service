package io.angularpay.pmt.ports.inbound;

import io.angularpay.pmt.domain.*;
import io.angularpay.pmt.models.*;

import java.util.List;
import java.util.Map;

public interface RestApiPort {
    GenericReferenceResponse createScheduledRequest(String schedule, CreateRequest request, Map<String, String> headers);
    GenericReferenceResponse create(CreateRequest request, Map<String, String> headers);
    void updateAmount(String requestReference, Amount amount, Map<String, String> headers);
    void updateExchangeRate(String requestReference, ExchangeRate exchangeRate, Map<String, String> headers);
    void updateBeneficiary(String requestReference, Beneficiary beneficiary, Map<String, String> headers);
    void updateVerificationStatus(String requestReference, boolean verified, Map<String, String> headers);
    GenericReferenceResponse addInvestor(String requestReference, AddInvestorApiModel addInvestorApiModel, Map<String, String> headers);
    void removeInvestor(String requestReference, String investmentReference, Map<String, String> headers);
    void removeInvestorTTL(String requestReference, String investmentReference, Map<String, String> headers);
    void removeInvestorPlatform(String requestReference, String investmentReference, Map<String, String> headers);
    GenericReferenceResponse addBargain(String requestReference, AddBargainApiModel addBargainApiModel, Map<String, String> headers);
    void acceptBargain(String requestReference, String bargainReference, Map<String, String> headers);
    void rejectBargain(String requestReference, String bargainReference, Map<String, String> headers);
    void deleteBargain(String requestReference, String bargainReference, Map<String, String> headers);
    void updateInvestmentAmount(String requestReference, String investmentReference, UpdateInvestmentApiModel updateInvestmentApiModel, Map<String, String> headers);
    GenericReferenceResponse makePayment(String requestReference, String investmentReference, PaymentRequest paymentRequest, Map<String, String> headers);
    void updateRequestStatus(String requestReference, RequestStatusModel status, Map<String, String> headers);
    PmtRequest getRequestByReference(String requestReference, Map<String, String> headers);
    List<PmtRequest> getNewsfeedModel(int page, Map<String, String> headers);
    List<UserRequestModel> getUserRequests(int page, Map<String, String> headers);
    List<UserInvestmentModel> getUserInvestments(int page, Map<String, String> headers);
    List<PmtRequest> getNewsfeedByStatus(int page, List<RequestStatus> statuses, Map<String, String> headers);
    List<PmtRequest> getRequestListByStatus(int page, List<RequestStatus> statuses, Map<String, String> headers);
    List<PmtRequest> getRequestListByVerification(int page, boolean verified, Map<String, String> headers);
    List<PmtRequest> getRequestList(int page, Map<String, String> headers);
    List<Statistics> getStatistics(Map<String, String> headers);
}
