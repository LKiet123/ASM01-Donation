package org.ltkiet.donation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDonationStatusException extends RuntimeException {
    public InvalidDonationStatusException(String message) {
        super(message);
    }
}

