package com.q4magic.common.dto.payment;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@JsonInclude(JsonInclude.Include. NON_NULL)
public class PaymentGatewayRequestDto {
    private Integer userId;
    @NotBlank(message = "Credit card number is Required")
    private String cardNumber;
    @NotBlank(message = "Expiry date is Required")
    private String expMonthYear;
    @NotBlank(message = "CVV is Required")
    private String cardCode;

    private String businessName;

    @NotBlank(message = "Name is Required")
    private String name;
    @NotBlank(message = "Address is Required")
    private String address;
    @NotBlank(message = "City name is Required")
    private String city;
    @NotBlank(message = "State name is Required")
    private String state;
    @NotBlank(message = "Zipcode is Required")
    private String postCode;
    @NotBlank(message = "Country is Required")
    private String country;
    private String phone;
    @NotBlank(message = "Email is Required")
    private String email;
}

