package com.contest.notification.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE, reason = "Timestamp must not be null.")
public class TimestampNotFoundException extends RuntimeException {
}