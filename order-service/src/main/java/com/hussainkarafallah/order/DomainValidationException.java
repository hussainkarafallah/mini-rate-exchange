package com.hussainkarafallah.order;

public class DomainValidationException extends IllegalStateException{
    public DomainValidationException(String msg){
        super(msg);
    }
}
