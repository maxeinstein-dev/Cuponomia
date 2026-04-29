package br.com.maxsueleinstein.cuponomia.interfaces.rest.handler;

import br.com.maxsueleinstein.cuponomia.application.dto.ErrorResponse;
import br.com.maxsueleinstein.cuponomia.domain.exception.CouponAlreadyUsedException;
import br.com.maxsueleinstein.cuponomia.domain.exception.CouponNotFoundException;
import br.com.maxsueleinstein.cuponomia.domain.exception.DuplicateCouponCodeException;
import br.com.maxsueleinstein.cuponomia.domain.exception.InvalidCouponException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Global exception handler for the REST API.
 * 
 * Converts domain exceptions into standardized HTTP error responses
 * with clear, actionable messages for API consumers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(CouponNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(CouponNotFoundException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.of(404, "Não Encontrado", ex.getMessage(),
                                                request.getRequestURI()));
        }

        @ExceptionHandler(DuplicateCouponCodeException.class)
        public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateCouponCodeException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ErrorResponse.of(409, "Conflito", ex.getMessage(), request.getRequestURI()));
        }

        @ExceptionHandler(CouponAlreadyUsedException.class)
        public ResponseEntity<ErrorResponse> handleAlreadyUsed(CouponAlreadyUsedException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ErrorResponse.of(409, "Conflito", ex.getMessage(), request.getRequestURI()));
        }

        @ExceptionHandler(InvalidCouponException.class)
        public ResponseEntity<ErrorResponse> handleInvalidCoupon(InvalidCouponException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(422)
                                .body(ErrorResponse.of(422, "Entidade Não Processável", ex.getMessage(),
                                                ex.getErrors(), request.getRequestURI()));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(400, "Requisição Inválida", ex.getMessage(),
                                                request.getRequestURI()));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                                .toList();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(400, "Requisição Inválida", "Falha na validação dos dados",
                                                errors, request.getRequestURI()));
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                        HttpServletRequest request) {
                log.warn("Violação de integridade de dados: {}", ex.getMessage());
                // This typically catches concurrent single-use coupon violations
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ErrorResponse.of(409, "Conflito",
                                                "Conflito com dados existentes. O cupom pode já ter sido utilizado.",
                                                request.getRequestURI()));
        }

        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.of(404, "Recurso Não Encontrado",
                                                "O recurso solicitado não foi encontrado", request.getRequestURI()));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
                log.error("Erro inesperado", ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ErrorResponse.of(500, "Erro Interno do Servidor",
                                                "Ocorreu um erro inesperado", request.getRequestURI()));
        }
}
