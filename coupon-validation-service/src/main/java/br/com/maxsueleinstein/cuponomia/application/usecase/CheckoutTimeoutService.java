package br.com.maxsueleinstein.cuponomia.application.usecase;

import br.com.maxsueleinstein.cuponomia.application.dto.ApplyCouponRequest;
import br.com.maxsueleinstein.cuponomia.application.dto.ApplyCouponResponse;
import br.com.maxsueleinstein.cuponomia.domain.exception.CheckoutTimeoutException;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

@Service
public class CheckoutTimeoutService {

    private final ApplyCouponUseCase applyCouponUseCase;
    private final TimeLimiter checkoutTimeLimiter;
    private final ScheduledExecutorService scheduler;

    public CheckoutTimeoutService(ApplyCouponUseCase applyCouponUseCase, TimeLimiterRegistry timeLimiterRegistry) {
        this.applyCouponUseCase = applyCouponUseCase;
        this.checkoutTimeLimiter = timeLimiterRegistry.timeLimiter("checkout-timeout");
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "checkout-timeout-scheduler");
            thread.setDaemon(true);
            return thread;
        });
    }

    public CompletableFuture<ApplyCouponResponse> execute(ApplyCouponRequest request) {
        Supplier<CompletionStage<ApplyCouponResponse>> supplier = () -> CompletableFuture
                .supplyAsync(() -> applyCouponUseCase.execute(request));
        CompletableFuture<ApplyCouponResponse> future = TimeLimiter.decorateCompletionStage(
                checkoutTimeLimiter,
                scheduler,
                supplier).get().toCompletableFuture();

        return future.exceptionallyCompose(throwable -> {
            if (isTimeout(throwable)) {
                return CompletableFuture.failedFuture(new CheckoutTimeoutException(
                        "Tempo limite excedido ao processar a aplicação do cupom"));
            }

            return CompletableFuture.failedFuture(unwrap(throwable));
        });
    }

    private boolean isTimeout(Throwable throwable) {
        String exceptionName = throwable.getClass().getName();
        Throwable cause = throwable.getCause();

        return exceptionName.contains("TimeoutException")
                || (cause != null && cause.getClass().getName().contains("TimeoutException"));
    }

    private Throwable unwrap(Throwable throwable) {
        if (throwable instanceof CompletionException || throwable instanceof ExecutionException) {
            Throwable cause = throwable.getCause();
            if (cause != null) {
                return cause;
            }
        }

        return throwable;
    }
}