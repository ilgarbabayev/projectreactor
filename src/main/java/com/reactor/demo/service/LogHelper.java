package com.reactor.demo.service;

import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.MDC;

import reactor.core.publisher.Signal;

public class LogHelper {

  public static <T> Consumer<Signal<T>> logOnNext(Consumer<T> logStatement) {
    return signal -> {
      if (!signal.isOnNext()) return;
      Optional<String> toPutInMdc = signal.getContextView().getOrEmpty("requestId");

      toPutInMdc.ifPresentOrElse(tpim -> {
                                   try (MDC.MDCCloseable cMdc = MDC.putCloseable("requestId", tpim)) {
                                     logStatement.accept(signal.get());
                                   }
                                 },
                                 () -> logStatement.accept(signal.get()));
    };
  }

  public static Consumer<Signal<?>> logOnError(Consumer<Throwable> errorLogStatement) {
    return signal -> {
      if (!signal.isOnError()) return;
      Optional<String> toPutInMdc = signal.getContextView().getOrEmpty("requestId");

      toPutInMdc.ifPresentOrElse(tpim -> {
                                   try (MDC.MDCCloseable cMdc = MDC.putCloseable("requestId", tpim)) {
                                     errorLogStatement.accept(signal.getThrowable());
                                   }
                                 },
                                 () -> errorLogStatement.accept(signal.getThrowable()));
    };
  }
}
