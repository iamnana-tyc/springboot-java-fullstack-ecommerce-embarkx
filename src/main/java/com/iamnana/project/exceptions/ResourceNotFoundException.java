package com.iamnana.project.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    // Name of the resource (e.g., "User", "Product")
    String resourceName;

    // The field used to search for the resource (e.g., "email", "id")
    String field;

    // When the field value is a string (e.g., "email@gmail.com")
    String fieldName;

    // When the field value is a numeric ID (e.g., 5)
    Long fieldId;


    public ResourceNotFoundException() {
    }

    /**
     * Constructor for cases when the missing resource is identified
     * using a field that has a text value (String).
     *
     * Example:
     *    new ResourceNotFoundException("User", "email", "test@gmail.com");
     *
     * This will produce a message like:
     *    "User not found with email: test@gmail.com"
     */
    public ResourceNotFoundException(String resourceName, String field, String fieldName) {
        // Set a custom error message that will appear in logs or responses
        super(String.format("%s not found with %s: %s ", resourceName, field, fieldName));
        this.resourceName = resourceName;
        this.field = field;
        this.fieldName = fieldName;
    }

    /**
     * Constructor for cases when the missing resource is identified
     * using a numeric ID value (Long).
     *
     * Example:
     *    new ResourceNotFoundException("Product", "id", 10L);
     *
     * This will produce:
     *    "Product not found with id: 10"
     */
    public ResourceNotFoundException(String resourceName, String field, Long fieldId) {
        super(String.format("%s not found with %s: %d ", resourceName, field, fieldId));
        this.resourceName = resourceName;
        this.field = field;
        this.fieldId = fieldId;
    }
}
