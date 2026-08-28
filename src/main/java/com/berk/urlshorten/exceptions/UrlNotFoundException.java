package com.berk.urlshorten.exceptions;

public class UrlNotFoundException extends RuntimeException{
    public UrlNotFoundException(String message) { super(message); }
}
