package com.q4magic.common.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface Constants {
    final String MSG = "msg";
    final String CODE = "code";
    final String STATUS = "status";
    final String INVALID_TOKEN = "Access Denied";
    final String STATUS_FAILURE = "failure";
    final String CHARACTER_ENCODING_UTF_8 = "UTF-8";
    final String REQUEST_HEADER_AUTHORIZATION = "Authorization";
    final String AUTHORIZATION_BEARER = "Bearer ";
    final String ERROR_MSG = "Whoops! An Expected Error Has Occurred, But We Have Logged This Event And Working On It.";

    final List<String> DEFAULT_CATEGORY_NAMES = new ArrayList<>(
            Arrays.asList(
                    "Account Information",
                    "Sent to Customer",
                    "Shared by Customer",
                    "Internal Documents"
            )
    );

}