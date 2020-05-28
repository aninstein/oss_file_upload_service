package com.file_upload_service.service;

import com.file_upload_service.entity.ProcessResult;
import com.iemylife.iot.webtoolkit.ErrorResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Created by tjhgm on 2017/1/22.
 * <p>
 * IllegalArgumentException -> badRequestReponse 400
 * NullPointerException -> notFoundReponse 404
 * SecurityException -> unauthorizedReponse 401
 * UnsupportedOperationException -> forbiddenReponse 403
 * RejectedExecutionException -> conflictResponse 409
 * <p>
 * 异常定义只能定义RuntimeException的直接子类（例如IllegalAccessException不允许定义）
 * AnnotationTypeMismatchException
 * ArithmeticException
 * ArrayStoreException
 * BufferOverflowException
 * BufferUnderflowException
 * CannotRedoException
 * CannotUndoException
 * ClassCastException
 * CMMException
 * ConcurrentModificationException
 * DataBindingException
 * DOMException
 * EmptyStackException
 * EnumConstantNotPresentException
 * EventException
 * IllegalArgumentException
 * IllegalMonitorStateException
 * IllegalPathStateException
 * IllegalStateException
 * ImagingOpException
 * IncompleteAnnotationException
 * IndexOutOfBoundsException
 * JMRuntimeException
 * LSException
 * MalformedParameterizedTypeException
 * MirroredTypeException
 * MirroredTypesException
 * MissingResourceException
 * NegativeArraySizeException
 * NoSuchElementException
 * NoSuchMechanismException
 * NullPointerException
 * ProfileDataException
 * ProviderException
 * RasterFormatException
 * RejectedExecutionException
 * SecurityException
 * SystemException
 * TypeConstraintException
 * TypeNotPresentException
 * UndeclaredThrowableException
 * UnknownAnnotationValueException
 * UnknownElementException
 * UnknownTypeException
 * UnmodifiableSetException
 * UnsupportedOperationException
 * WebServiceException
 */
public interface ResponseErrorService {

    /**
     * 公共错误信息
     *
     * @param errorEntity
     * @param httpStatus
     * @return
     */
    public ResponseEntity<ErrorEntity> Response(ErrorEntity errorEntity, HttpStatus httpStatus);

    /**
     * 公共错误信息
     *
     * @param errorMessage
     * @param errorCode
     * @param httpStatus
     * @return
     */
    public ResponseEntity<ErrorEntity> Response(String errorMessage, String errorCode, HttpStatus httpStatus);

    /**
     * 资源找不到
     *
     * @param errorInfo
     * @return
     */
    public ResponseEntity<ErrorEntity> notFoundResponse(String errorInfo);

    /**
     * 请求参数不足
     *
     * @param errorInfo
     * @return
     */
    public ResponseEntity<ErrorEntity> badRequestResponse(String errorInfo);

    /**
     * 登录失败
     *
     * @param errorInfo
     * @return
     */
    public ResponseEntity<ErrorEntity> unauthorizedResponse(String errorInfo);

    /**
     * 权限不足
     *
     * @param errorInfo
     * @return
     */
    public ResponseEntity<ErrorEntity> forbiddenResponse(String errorInfo);

    /**
     * 资源已存在
     *
     * @param errorInfo
     * @return
     */
    public ResponseEntity<ErrorEntity> conflictResponse(String errorInfo);

    /**
     * 服务器异常
     *
     * @param errorInfo
     * @return
     */
    public ResponseEntity<ErrorEntity> internalServerErroeResponse(String errorInfo);

    public class ErrorEntity {
        private String errorMessage;
        private String errorCode;

        public ErrorEntity(String errorMessage, String errorCode) {
            this.errorMessage = errorMessage;
            this.errorCode = errorCode;
        }

        @Override
        public String toString() {
            return this.errorCode + "," + this.errorMessage;
        }

        public static ErrorEntity parse(String errorInfo) {
            int a = errorInfo.indexOf(',');
            String errorCodeString = "";
            String errorMsg = errorInfo;
            if (a > -1) {
                errorCodeString = errorInfo.substring(0, a);
                errorMsg = errorInfo.substring(a + 1);
            }
            return new ErrorEntity(errorMsg, errorCodeString);
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }
    }


    /**
     * 转化http状态码
     *
     * @param processResult
     * @return
     */
    HttpStatus changeHttpStatus(ProcessResult<?> processResult);

    ErrorResult changeErrorResult(ProcessResult<?> processResult);
}
