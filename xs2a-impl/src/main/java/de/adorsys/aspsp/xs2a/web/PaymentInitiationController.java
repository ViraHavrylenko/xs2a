
package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.aspsp.xs2a.domain.Transactions;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.PaymentService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "api/v1/payments/{product-name}")
public class PaymentInitiationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentInitiationController.class);
    private PaymentService paymentService;

    @Autowired
    public PaymentInitiationController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @ApiOperation(value = "Initialises a new payment ", notes = "debtor account, creditor accout, creditor name, remittance information unstructured")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "transactions_status received, a list of hyperlinks to be recognized by the Tpp."),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(method = RequestMethod.POST)
    public PaymentInitiationResponse createPaymentInitiation(
    @ApiParam(name = "tppRedirectPreferred", value = "If it equals “true”, the TPP prefers a redirect over an embedded SCA approach.")
    @RequestParam(name = "tppRedirectPreferred", required = false) boolean tppRedirectPreferred,
    @RequestBody SinglePayments paymentInitialisationRequest) {
        // TODO according task PIS_01_01. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/9
        return createResponse();
    }

    @ApiOperation(value = "Get information  about the status of a payment initialisation ")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "transactions_status Accepted Customer Profile."),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(value = "/{paymentId}/status", method = RequestMethod.GET)
    public ResponseEntity<Map<String, TransactionStatus>> getPaymentInitiationStatusById(
    @ApiParam(name = "paymentId", value = "Resource Identification of the related payment")
    @PathVariable("paymentId") String paymentId) {
        Map<String, TransactionStatus> paymentStatusResponse = new HashMap<>();
        TransactionStatus transactionStatus = paymentService.getPaymentStatusById(paymentId);
        paymentStatusResponse.put("transactionStatus", transactionStatus);

        LOGGER.debug("getPaymentInitiationStatus(): response {} ", transactionStatus);

        return new ResponseEntity<>(paymentStatusResponse, HttpStatus.OK);
    }

    @ApiOperation(value = "Get information  about all payments ", notes = "the payment ID")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "transactions_status ?????"),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(value = "/{paymentId}", method = RequestMethod.GET)
    public Transactions getPaymentInitiation(@PathVariable String paymentId) {
        // TODO according task PIS_01_01. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/9
        return new Transactions();
    }

    private PaymentInitiationResponse createResponse() {
        // TODO according task PIS_01_01. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/9
        return null;
    }

}


