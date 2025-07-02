    package com.newsaggregator.exception;

    import org.springframework.http.HttpStatus;
    import org.springframework.web.bind.annotation.ResponseStatus;

    @ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict
    public class UserAlreadyExistsException extends RuntimeException {
        public UserAlreadyExistsException(String message) {
            super(message);
        }

        public UserAlreadyExistsException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
