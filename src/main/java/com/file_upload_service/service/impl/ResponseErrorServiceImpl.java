package com.file_upload_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.file_upload_service.entity.ProcessResult;
import com.file_upload_service.entity.ProcessResultErrorTypeEnum;
import com.file_upload_service.service.ResponseErrorService;
import com.file_upload_service.utils.UtilService;
//import com.iemylife.iot.logging.IotLogger;
import com.iemylife.iot.webtoolkit.ErrorResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Created by tjhgm on 2017/1/22.
 */
@Component
public class ResponseErrorServiceImpl implements ResponseErrorService {

//    @Autowired
//    private IotLogger logger;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ResponseEntity<ErrorEntity> Response(String errorMessage, String errorCode, HttpStatus httpStatus) {
        return Response(new ErrorEntity(errorMessage, errorCode), httpStatus);
    }

    @Override
    public ResponseEntity<ErrorEntity> Response(ErrorEntity errorEntity, HttpStatus httpStatus) {
        if (UtilService.isNullOrEmpty(errorEntity.getErrorCode())) {
            errorEntity.setErrorCode(httpStatus.toString());
        } else {
            errorEntity.setErrorCode(httpStatus.value() + "." + errorEntity.getErrorCode());
        }
        try {
//            logger.warn(objectMapper.writeValueAsString(errorEntity));
            System.out.println("error" + objectMapper.writeValueAsString(errorEntity));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<ErrorEntity>(errorEntity, httpStatus);
    }

    @Override
    public ResponseEntity<ErrorEntity> notFoundResponse(String errorInfo) {
        return Response(ErrorEntity.parse(errorInfo), HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<ErrorEntity> badRequestResponse(String errorInfo) {
        return Response(ErrorEntity.parse(errorInfo), HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<ErrorEntity> unauthorizedResponse(String errorInfo) {
        return Response(ErrorEntity.parse(errorInfo), HttpStatus.UNAUTHORIZED);
    }

    @Override
    public ResponseEntity<ErrorEntity> forbiddenResponse(String errorInfo) {
        return Response(ErrorEntity.parse(errorInfo), HttpStatus.FORBIDDEN);
    }

    @Override
    public ResponseEntity<ErrorEntity> conflictResponse(String errorInfo) {
        return Response(ErrorEntity.parse(errorInfo), HttpStatus.CONFLICT);
    }

    @Override
    public ResponseEntity<ErrorEntity> internalServerErroeResponse(String errorInfo) {
        return Response(ErrorEntity.parse(errorInfo), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public HttpStatus changeHttpStatus(ProcessResult<?> processResult) {
        HttpStatus httpStatus = HttpStatus.OK;
        switch (ProcessResultErrorTypeEnum.values()[processResult.getErrorType()]) {
            case ok:
                break;
            case badRequest:
                httpStatus = HttpStatus.BAD_REQUEST;
                break;
            case unauthorized:
                httpStatus = HttpStatus.UNAUTHORIZED;
                break;
            case forbidden:
                httpStatus = HttpStatus.FORBIDDEN;
                break;
            case notFound:
                httpStatus = HttpStatus.NOT_FOUND;
                break;
            case conflict:
                httpStatus = HttpStatus.CONFLICT;
                break;
            case unsupportedMediaType:
                httpStatus = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
                break;
            case internalServer:
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
            default:
                break;
        }
        return httpStatus;
    }

    @Override
    public ErrorResult changeErrorResult(ProcessResult<?> processResult) {
        HttpStatus httpStatus = changeHttpStatus(processResult);
        double errorCode = 0;
        if (processResult.getError().get().getErrorCode() > 0) {
            errorCode = Double.parseDouble(httpStatus + "." + processResult.getError().get().getErrorCode());
        } else {
            errorCode = httpStatus.value();
        }
        ErrorResult errorResult = new ErrorResult(errorCode,
                processResult.getError().get().getErrorMessage());
        try {
//            logger.warn(objectMapper.writeValueAsString(errorResult));
            System.out.println("error" + objectMapper.writeValueAsString(errorResult));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return errorResult;
    }
}
