package cn.cetasas.db.exception;

public class BusinessException extends RuntimeException{

    private BusinessExceptionCode code;

    public BusinessException(BusinessExceptionCode code) {
        super(code.getDesc());
        this.code = code;
    }

    public BusinessExceptionCode getCode() {
        return code;
    }

    public void setCode(BusinessExceptionCode code) {
        this.code = code;
    }

    /**
     * 不写入堆栈信息，提高性能
     * 所谓堆栈信息，就是控制台打出的那一大堆信息（谁被谁调用）
     * 而自定义异常一般都是比较确认的异常，所以不需要这么多的提示信息
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
