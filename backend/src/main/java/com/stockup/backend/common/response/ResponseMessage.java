package com.stockup.backend.common.response;

public enum ResponseMessage {


    SUCCESS("Request processed successfully."),
    CREATED("Resource created successfully."),
    UPDATED("Resource updated successfully."),
    DELETED("Resource deleted successfully."),
    FETCHED("Resource fetched successfully."),


    FAILURE("Request failed."),
    VALIDATION_FAILED("Validation failed."),
    BAD_REQUEST("Bad request."),
    RESOURCE_NOT_FOUND("Requested resource was not found."),
    CONFLICT("Resource already exists."),
    UNAUTHORIZED("Authentication required."),
    FORBIDDEN("Access denied."),
    EMAIL_DELIVERY_FAILED("Unable to send verification email. Please try again later."),
    MERCHANT_REGISTERED_SUCCESSFULLY("Created Merchant"),

    INTERNAL_SERVER_ERROR("An unexpected error occurred."),
    MERCHANT_ALREADY_EXISTS("Merchant Already Registered");

    private final String message;

    ResponseMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}