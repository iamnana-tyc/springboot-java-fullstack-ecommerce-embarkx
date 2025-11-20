package com.iamnana.project.exceptions;

import com.iamnana.project.payload.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * This class handles exceptions globally for the entire application.
 * The @RestControllerAdvice annotation tells Spring:
 * "Whenever an exception happens in any controller, check this class
 * to see if we have a method that knows how to handle it."
 */
@RestControllerAdvice
public class MyGlobalExceptionHandler {

    /**
     * This method handles validation errors that occur when
     * the user sends invalid data to an API endpoint.
     *
     * When @Valid fails (due to missing fields, wrong formats, etc.),
     * Spring throws a MethodArgumentNotValidException.
     *
     * This handler catches that exception and converts the validation errors
     * into a simple, readable JSON response.
     *
     * @param ex The exception that contains all validation errors.
     * @return ResponseEntity containing:
     *         - A map where:
     *               key   = the field name that failed validation
     *               value = the validation error message
     *         - HTTP 400 (Bad Request) status code
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> myMethodArgumentNotValidException(MethodArgumentNotValidException ex){
        // Create a map to store field names and their error messages
        Map<String, String> response = new HashMap<>();

        /*
         * getBindingResult() gives us all validation errors.
         * getAllErrors() returns a list of general error objects.
         *
         * For each error:
         *   - Cast it to FieldError so we can get the specific field name
         *   - Extract the default validation message (e.g., "must not be blank")
         *   - Put both into our response map
         */
        ex.getBindingResult().getAllErrors().forEach(error ->
                {
                    String fieldName = ((FieldError)error).getField();
                    String message = error.getDefaultMessage();
                    response.put(fieldName, message);
                }
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse> myResourceNotFoundException(ResourceNotFoundException ex){
        String message = ex.getMessage();
        APIResponse apiResponse = new APIResponse(message, false);
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);

    }

    @ExceptionHandler(APIException.class)
    public ResponseEntity<APIResponse> myAPIException(APIException ex){
        String message = ex.getMessage();
        APIResponse apiResponse = new APIResponse(message, false);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);

    }

}
