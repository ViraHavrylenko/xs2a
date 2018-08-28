package de.adorsys.aspsp.xs2a.integtest.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.adorsys.psd2.model.TppMessages;
import de.adorsys.psd2.model.TransactionStatus;
import lombok.Data;

import java.util.ArrayList;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ITMessageError {

    private TransactionStatus transactionStatus;
    private ArrayList<TppMessages> tppMessages;
}
