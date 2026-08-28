package com.berk.urlshorten.exceptions;

import java.time.Instant;

public record ErrorResponse (Instant timestamp, int status,
                             String message, String path) { }
