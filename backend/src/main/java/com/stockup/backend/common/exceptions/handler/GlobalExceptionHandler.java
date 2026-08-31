package com.stockup.backend.common.exceptions.handler;

import com.stockup.backend.common.exceptions.model.BaseException;
import com.stockup.backend.common.response.ApiError;
import com.stockup.backend.common.response.ApiResponse;
import com.stockup.backend.common.response.ApiResponseFactory;
import com.stockup.backend.common.response.ResponseMessage;
import com.stockup.backend.domain.admin.exception.UserNotFoundException;
import com.stockup.backend.domain.basket.exception.PendingBroadcastAlreadyExistsException;
import com.stockup.backend.domain.broadcast.exception.BroadcastAlreadyExistsException;
import com.stockup.backend.domain.broadcast.exception.BroadcastRecipientNotViewedException;
import com.stockup.backend.domain.broadcast.exception.NoTargetStoresFoundException;
import com.stockup.backend.domain.merchant.exception.MerchantAlreadyExistsException;
import com.stockup.backend.domain.merchant.exception.MerchantNotFoundException;
import com.stockup.backend.domain.notification.exception.NotificationAccessDeniedException;
import com.stockup.backend.domain.notification.exception.NotificationNotFoundException;
import com.stockup.backend.domain.reservation.exception.InvalidCancellationReasonException;
import com.stockup.backend.domain.reservation.exception.InvalidOtpException;
import com.stockup.backend.domain.reservation.exception.InvalidReservationStateException;
import com.stockup.backend.domain.reservation.exception.MerchantCancellationWindowClosedException;
import com.stockup.backend.domain.reservation.exception.MerchantOfferNotFoundException;
import com.stockup.backend.domain.reservation.exception.OtpNotGeneratedException;
import com.stockup.backend.domain.reservation.exception.ReservationAccessDeniedException;
import com.stockup.backend.domain.reservation.exception.ReservationAlreadyExistsException;
import com.stockup.backend.domain.reservation.exception.ReservationNotFoundException;
import com.stockup.backend.domain.store.exception.StoreAlreadyExistsException;
import com.stockup.backend.infrastructure.notification.email.exception.EmailDeliveryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<?> handleBaseException(BaseException exception) {

        return ApiResponseFactory.failure(
                exception.getStatus(),
                ResponseMessage.FAILURE,
                List.of(new ApiError(null, exception.getMessage()))
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException exception) {

        List<ApiError> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ApiError(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        return ApiResponseFactory.failure(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                ResponseMessage.VALIDATION_FAILED,
                errors
        );
    }

    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleEntityNotFound(
            jakarta.persistence.EntityNotFoundException ex
    ) {
        return ApiResponseFactory.failure(
                HttpStatus.NOT_FOUND,
                ResponseMessage.RESOURCE_NOT_FOUND,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalState(IllegalStateException ex) {
        return ApiResponseFactory.failure(
                HttpStatus.CONFLICT,
                ResponseMessage.CONFLICT,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ApiResponseFactory.failure(
                HttpStatus.BAD_REQUEST,
                ResponseMessage.BAD_REQUEST,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpectedException(Exception exception) {

        log.error("Unexpected error occurred.", exception);

        return ApiResponseFactory.failure(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ResponseMessage.INTERNAL_SERVER_ERROR,
                List.of(new ApiError(null, "Unexpected error occurred."))
        );
    }
    @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmailDeliveryException(
            EmailDeliveryException ex
    ) {
        return ApiResponseFactory.failure(
                HttpStatus.SERVICE_UNAVAILABLE,
                ResponseMessage.EMAIL_DELIVERY_FAILED,
                List.of(
                        new ApiError(
                                null,
                                "Unable to send verification email. Please try again later."
                        )
                )
        );
    }

    @ExceptionHandler(MerchantAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleMerchantAlreadyExists(
            MerchantAlreadyExistsException ex
    ) {
        return ApiResponseFactory.failure(
                HttpStatus.CONFLICT,
                ResponseMessage.MERCHANT_ALREADY_EXISTS,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

    @ExceptionHandler(MerchantNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleMerchantNotFound(
            MerchantNotFoundException ex
    ) {
        return ApiResponseFactory.failure(
                HttpStatus.CONFLICT,
                ResponseMessage.MERCHANT_NOT_FOUND,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

    @ExceptionHandler(StoreAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleStoreAlreadyExists(
            StoreAlreadyExistsException ex
    ) {
        return ApiResponseFactory.failure(
                HttpStatus.CONFLICT,
                ResponseMessage.STORE_ALREADY_EXISTS,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

    @ExceptionHandler(PendingBroadcastAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handlePendingBasket(
            PendingBroadcastAlreadyExistsException ex
    ){
        return ApiResponseFactory.failure(
                HttpStatus.CONFLICT,
                ResponseMessage.PENDING_BROADCAST_BASKET_EXISTS,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex
    ) {
        return ApiResponseFactory.failure(
                HttpStatus.FORBIDDEN,
                ResponseMessage.FORBIDDEN,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

    @ExceptionHandler({ReservationNotFoundException.class, MerchantOfferNotFoundException.class})
    public ResponseEntity<ApiResponse<Object>> handleReservationNotFound(RuntimeException ex) {
        return ApiResponseFactory.failure(
                HttpStatus.NOT_FOUND,
                ResponseMessage.RESERVATION_NOT_FOUND,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

    @ExceptionHandler(ReservationAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleReservationAlreadyExists(
            ReservationAlreadyExistsException ex
    ) {
        return ApiResponseFactory.failure(
                HttpStatus.CONFLICT,
                ResponseMessage.RESERVATION_ALREADY_EXISTS,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

    @ExceptionHandler({InvalidReservationStateException.class, OtpNotGeneratedException.class})
    public ResponseEntity<ApiResponse<Object>> handleInvalidReservationState(RuntimeException ex) {
        return ApiResponseFactory.failure(
                HttpStatus.CONFLICT,
                ResponseMessage.INVALID_RESERVATION_STATE,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidOtp(InvalidOtpException ex) {
        return ApiResponseFactory.failure(
                HttpStatus.BAD_REQUEST,
                ResponseMessage.INVALID_OTP,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

    @ExceptionHandler(InvalidCancellationReasonException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidCancellationReason(
            InvalidCancellationReasonException ex
    ) {
        return ApiResponseFactory.failure(
                HttpStatus.BAD_REQUEST,
                ResponseMessage.INVALID_CANCELLATION_REASON,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

    @ExceptionHandler(MerchantCancellationWindowClosedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMerchantCancellationWindowClosed(
            MerchantCancellationWindowClosedException ex
    ) {
        return ApiResponseFactory.failure(
                HttpStatus.CONFLICT,
                ResponseMessage.MERCHANT_CANCELLATION_WINDOW_CLOSED,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

    @ExceptionHandler(ReservationAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleReservationAccessDenied(
            ReservationAccessDeniedException ex
    ) {
        return ApiResponseFactory.failure(
                HttpStatus.FORBIDDEN,
                ResponseMessage.RESERVATION_ACCESS_DENIED,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotificationNotFound(
            NotificationNotFoundException ex
    ) {
        return ApiResponseFactory.failure(
                HttpStatus.NOT_FOUND,
                ResponseMessage.NOTIFICATION_NOT_FOUND,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

    @ExceptionHandler(NotificationAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotificationAccessDenied(
            NotificationAccessDeniedException ex
    ) {
        return ApiResponseFactory.failure(
                HttpStatus.FORBIDDEN,
                ResponseMessage.NOTIFICATION_ACCESS_DENIED,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

    @ExceptionHandler(NoTargetStoresFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoTargetStoresFound(
            NoTargetStoresFoundException ex
    ) {
        return ApiResponseFactory.failure(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ResponseMessage.NO_TARGET_STORES_FOUND,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

    @ExceptionHandler(BroadcastAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBroadcastAlreadyExists(
            BroadcastAlreadyExistsException ex
    ) {
        return ApiResponseFactory.failure(
                HttpStatus.CONFLICT,
                ResponseMessage.BROADCAST_ALREADY_EXISTS,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

    @ExceptionHandler(BroadcastRecipientNotViewedException.class)
    public ResponseEntity<ApiResponse<Object>> handleBroadcastRecipientNotViewed(
            BroadcastRecipientNotViewedException ex
    ) {
        return ApiResponseFactory.failure(
                HttpStatus.CONFLICT,
                ResponseMessage.BROADCAST_RECIPIENT_NOT_VIEWED,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFound(UserNotFoundException ex) {
        return ApiResponseFactory.failure(
                HttpStatus.NOT_FOUND,
                ResponseMessage.USER_NOT_FOUND,
                List.of(new ApiError(null, ex.getMessage()))
        );
    }

}