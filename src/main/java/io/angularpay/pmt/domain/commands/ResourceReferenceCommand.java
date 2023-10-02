package io.angularpay.pmt.domain.commands;

public interface ResourceReferenceCommand<T, R> {

    R map(T referenceResponse);
}
