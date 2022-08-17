package io.micronaut.transaction.interceptor;

import groovy.lang.Singleton;
import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.kotlin.KotlinInterceptedMethod;
import io.micronaut.core.annotation.Internal;
import io.micronaut.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

@Internal
@Singleton
public class TxCompletionStageDataIntroductionHelper {

    private final CoroutineTxHelper coroutineTxHelper;

    public TxCompletionStageDataIntroductionHelper(CoroutineTxHelper coroutineTxHelper) {
        this.coroutineTxHelper = coroutineTxHelper;
    }

    public CompletionStage<Object> decorate(InterceptedMethod interceptedMethod, Supplier<CompletionStage<Object>> supplier) {
        TransactionSynchronizationManager.TransactionSynchronizationState state = null;
        if (interceptedMethod instanceof KotlinInterceptedMethod) {
            KotlinInterceptedMethod kotlinInterceptedMethod = (KotlinInterceptedMethod) interceptedMethod;
            state = Objects.requireNonNull(coroutineTxHelper).getTxState(kotlinInterceptedMethod);
        }
        return TransactionSynchronizationManager.decorateCompletionStage(state, supplier);
    }
}
