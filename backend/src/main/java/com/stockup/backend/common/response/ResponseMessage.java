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
    MERCHANT_NOT_FOUND("Merchant Not found"),
    MERCHANT_OFFER_SUBMITTED_SUCCESSFULLY("Merchant offer submitted successfully"),

    INTERNAL_SERVER_ERROR("An unexpected error occurred."),
    MERCHANT_ALREADY_EXISTS("Merchant Already Registered"),
    STORE_CREATED("Store Registered Successfully"),
    STORE_ALREADY_EXISTS("Store already exists"),

    BASKET_CREATED("Basket Created Successfully"),
    BASKET_HISTORY_FETCHED("Found your basket history"),
    BASKET_FETCHED("Basket fetched successfully"),
    RESERVATION_SUCCESS("Reservation created successfully."),
    RESERVATIONS_FETCHED("Reservation fetched successfully"),
    RESERVATION_FETCHED("Fetched required reservation");

    private final String message;

    ResponseMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}