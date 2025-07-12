package com.example.CineHive.exception;

public class ReportAlreadyExistsException extends BusinessException {
    public ReportAlreadyExistsException() {
        super(ErrorCode.REPORT_ALREADY_EXISTS);
    }
}