package io.angularpay.pmt.ports.outbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.angularpay.pmt.models.VerifySignatureResponseModel;

import java.util.Map;

public interface CipherServicePort {
    VerifySignatureResponseModel verifySignature(String requestBody, Map<String, String> headers) throws JsonProcessingException;
}
