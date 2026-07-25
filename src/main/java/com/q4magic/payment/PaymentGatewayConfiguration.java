package com.q4magic.payment;


import lombok.Data;
import net.authorize.Environment;
import net.authorize.api.contract.v1.ValidationModeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class PaymentGatewayConfiguration {

    @Value("${envsys}")
    private String envsys;

    @Value("${authorizenet.loginId}")
    private String apiPaymentGatewayLoginId;

    @Value("${authorizenet.transactionKey}")
    private String apiPaymentGatewayTransactionKey;

    @Value("${authorizenet.productionLoginId}")
    private String apiProductionPaymentGatewayLoginId;

    @Value("${authorizenet.productionTransactionKey}")
    private String apiProductionPaymentGatewayTransactionKey;

    public boolean checkSandBox()
    {
        if(envsys.equals("prod"))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public String authorizenetLonginId()
    {
        if(checkSandBox())
        {
            return apiPaymentGatewayLoginId;
        }
        else
        {
            return apiProductionPaymentGatewayLoginId;
        }
    }

    public String authorizenetTransactionKey()
    {
        if(checkSandBox())
        {
            return apiPaymentGatewayTransactionKey;
        }
        else
        {
            return apiProductionPaymentGatewayTransactionKey;
        }
    }

    public Environment environmentModeCheck()
    {
        if(checkSandBox())
        {
            return Environment.SANDBOX;
        }
        else
        {
            return Environment.PRODUCTION;
        }
    }

    public ValidationModeEnum validationModeEnumCheck()
    {
        if(checkSandBox())
        {
            return ValidationModeEnum.TEST_MODE;
        }
        else
        {
            return ValidationModeEnum.LIVE_MODE;
        }
    }
}
