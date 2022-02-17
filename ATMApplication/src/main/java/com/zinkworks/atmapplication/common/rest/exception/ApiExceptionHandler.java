package com.zinkworks.atmapplication.common.rest.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This Exception Handler handles all exceptions thrown from the application code that are raised after the
 * {@link DispatcherServlet} starts invoking an API handler method (@{@link Controller}).
 * Any exceptions thrown before that (e.g. during authentication in security filter chain) must be handled there.
 * <p>
 * Additional exception handling can be added by creating a bean annotated with @ControllerAdvice and having @Order
 * value lower than {@link ApiExceptionHandler.DEFAULT_ZINKWORKS_API_EXCEPTION_HANDLER_ORDER}. Any such class can extend
 * this class and add more exception handlers or override some methods in this class or in
 * {@link ResponseEntityExceptionHandler}, or both. It can also be a standalone class providing necessary logic, but it
 * is advised to at least extend {@link ResponseEntityExceptionHandler}.
 * <p>
 * see {@link ResponseEntityExceptionHandler}
 */
@Slf4j
@Order(ApiExceptionHandler.DEFAULT_ZINKWORKS_API_EXCEPTION_HANDLER_ORDER)
@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler
{
    public static final int DEFAULT_ZINKWORKS_API_EXCEPTION_HANDLER_ORDER = 0;
    public static final String UNEXPECTED_EXCEPTION_MESSAGE =
            "Internal error occurred. Please try again later or report an error.";

    private static final String MESSAGE_FIELD_NAME = "message";

    @ExceptionHandler({AccessDeniedException.class})
    @ResponseBody
    public ResponseEntity<Object> handleAccessDeniedException(final AccessDeniedException ex,
                                                              final WebRequest webRequest)
    {
        return handleExceptionInternal(ex, Map.of(MESSAGE_FIELD_NAME, "You are not authorized to invoke this operation."),
                null, HttpStatus.FORBIDDEN, webRequest);
    }

    @ExceptionHandler({RestException.class})
    @ResponseBody
    public ResponseEntity<Object> handleRestException(final RestException ex,
                                                      final WebRequest webRequest)
    {
        return handleExceptionInternal(ex, Map.of(MESSAGE_FIELD_NAME, ex.getErrorMessage()), null, ex.getHttpStatus(),
                webRequest);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    @ResponseBody
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException ex,
                                                                               final WebRequest webRequest)
    {
        log.info("Received request for {} with query params {} with incorrectly formatted parameter {}",
                webRequest.getDescription(false),
                webRequest.getParameterMap().entrySet().stream()
                        .map(entry -> Pair.of(entry.getKey(), List.of(entry.getValue())))
                        .collect(Collectors.toList()),
                ex.getName());
        final Map<String, String> responseBody = Map.of(MESSAGE_FIELD_NAME,
                "Format of " + getParameterType(ex.getParameter()) + " parameter " + ex.getName() + " is incorrect. " +
                        "Please consult the API documentation.");
        return handleExceptionInternal(ex, responseBody, null, HttpStatus.BAD_REQUEST, webRequest);
    }

    private String getParameterType(final MethodParameter parameter)
    {
        if (parameter.getParameterAnnotation(PathVariable.class) != null)
        {
            return "path";
        }
        return "query";
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(final MissingServletRequestParameterException ex,
                                                                          final HttpHeaders headers,
                                                                          final HttpStatus status,
                                                                          final WebRequest webRequest)
    {
        log.info("Received request for {} with query params {} with missing required parameter {}",
                webRequest.getDescription(false),
                webRequest.getParameterMap().entrySet().stream()
                        .map(entry -> Pair.of(entry.getKey(), List.of(entry.getValue())))
                        .collect(Collectors.toList()),
                ex.getParameterName());
        final Map<String, String> responseBody =
                Map.of(MESSAGE_FIELD_NAME, "Parameter " + ex.getParameterName() + " must be provided.");
        return handleExceptionInternal(ex, responseBody, null, HttpStatus.BAD_REQUEST, webRequest);
    }

    @ExceptionHandler({RuntimeException.class})
    @ResponseBody
    public ResponseEntity<Object> handleException(final RuntimeException ex, final WebRequest webRequest)
    {
        log.error("An error occurred during request processing!", ex);
        return handleExceptionInternal(ex, Map.of(MESSAGE_FIELD_NAME, UNEXPECTED_EXCEPTION_MESSAGE), null,
                HttpStatus.INTERNAL_SERVER_ERROR, webRequest);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(final Exception ex, final Object body,
                                                             final HttpHeaders headers, final HttpStatus status,
                                                             final WebRequest webRequest)
    {
        if (body == null)
        {
            log.error("An error occurred during request processing and wasn't handled by handleException!", ex);
            return new ResponseEntity<>(Map.of(MESSAGE_FIELD_NAME, UNEXPECTED_EXCEPTION_MESSAGE), headers, status);
        }
        return new ResponseEntity<>(body, headers, status);
    }
}
