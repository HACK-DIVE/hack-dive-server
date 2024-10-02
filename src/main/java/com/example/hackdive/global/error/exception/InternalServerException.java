package com.example.hackdive.global.error.exception;


import com.example.hackdive.global.error.ErrorCode;

public class InternalServerException extends BusinessException {
    public InternalServerException(ErrorCode errorCode) {
        super(errorCode);
    }
}