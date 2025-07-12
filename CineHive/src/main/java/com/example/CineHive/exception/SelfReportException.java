package com.example.CineHive.exception;

public class SelfReportException extends BusinessException {
    public SelfReportException() {
        super(ErrorCode.SELF_REPORT_NOT_ALLOWED);
    }
}