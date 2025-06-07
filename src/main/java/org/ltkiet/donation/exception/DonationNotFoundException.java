package org.ltkiet.donation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DonationNotFoundException extends RuntimeException {
    public DonationNotFoundException(String message) {
        super(message);
    }
}

