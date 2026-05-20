package com.campus.common.exception;

/**
 * 业务异常类，用于抛出带错误码的业务逻辑异常。
 */
public class BusinessException extends RuntimeException {
    private int code;

    /**
     * 构造业务异常。
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造业务异常，使用默认错误码 400。
     *
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public int getCode() { return code; }
}
