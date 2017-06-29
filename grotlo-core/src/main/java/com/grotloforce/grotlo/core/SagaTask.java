/*
 * MIT License
 *
 * Copyright (c) 2017 grotlo-force
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.grotloforce.grotlo.core;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by msavic on 6/28/2017.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class SagaTask<I, O> {

  private String name;

  private BiFunction<SagaContext, I, O> request;

  private I input;

  private BiConsumer<I, ? super Exception> compesatingRequest;

  void execute(SagaContext sagaContext) {
    O result = request.apply(sagaContext, input);
    sagaContext.addResult(name, result);
  }

  void compensate(Exception e) {
    compesatingRequest.accept(input, e);
  }

  public static <I, O> SagaTaskBuilder<I, O> builder() {
    return new SagaTaskBuilder<>();
  }

  public static class SagaTaskBuilder<I, O> {

    private String name;

    private BiFunction<SagaContext, I, O> request;

    private BiConsumer<I, ? super Exception> compesatingRequest;

    public SagaTaskBuilder<I, O> name(String name) {
      this.name = name;
      return this;
    }

    public SagaTaskBuilder<I, O> request(BiFunction<SagaContext, I, O> request) {
      this.request = request;
      return this;
    }

    public SagaTaskBuilder<I, O> compesatingRequest(
        BiConsumer<I, ? super Exception> compesatingRequest) {
      this.compesatingRequest = compesatingRequest;
      return this;
    }

    public SagaTask<I, O> build(I input) {
      return new SagaTask<>(name, request, input, compesatingRequest);
    }
  }
}
