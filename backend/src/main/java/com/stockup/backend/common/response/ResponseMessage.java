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
    PENDING_BROADCAST_BASKET_EXISTS("Basket still in broadcast"),

    RESERVATION_SUCCESS("Reservation created successfully."),
    RESERVATIONS_FETCHED("Reservation fetched successfully"),
    RESERVATION_FETCHED("Fetched required reservation"),

    RESERVATION_NOT_FOUND("Reservation not found"),
    RESERVATION_ALREADY_EXISTS("A reservation already exists"),
    INVALID_RESERVATION_STATE("Reservation is not in a valid state for this action"),
    INVALID_OTP("Invalid OTP"),
    OTP_NOT_GENERATED("OTP has not been generated yet"),
    RESERVATION_ACCESS_DENIED("You are not authorized to access this reservation"),
    INVALID_CANCELLATION_REASON("A valid cancellation reason is required"),
    MERCHANT_CANCELLATION_WINDOW_CLOSED("Reservation can no longer be cancelled"),
    MERCHANT_OFFER_NOT_FOUND("Merchant offer not found"),

    NOTIFICATION_NOT_FOUND("Notification not found"),
    NOTIFICATION_ACCESS_DENIED("You are not authorized to access this notification"),

    NO_TARGET_STORES_FOUND("No target stores found for this basket"),
    BROADCAST_ALREADY_EXISTS("Broadcast already exists"),
    BROADCAST_RECIPIENT_NOT_VIEWED("Broadcast must be marked as viewed first"),

    USER_NOT_FOUND("User not found");


    private final String message;

    ResponseMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}