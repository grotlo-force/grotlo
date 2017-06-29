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

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Created by msavic on 6/28/2017.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Saga {

  private List<SagaTask> tasks;

  public static SagaBuilder startWith(SagaTask task) {
    return new SagaBuilder(task);
  }

  public SagaContext execute() {
    return execute(new SagaContext());
  }

  SagaContext execute(SagaContext sagaContext) {
    for (int i = 0; i < tasks.size(); i++) {
      try {
        tasks.get(i).execute(sagaContext);
      } catch (Exception e) {
        sagaContext.setSuccessful(false);
        compensate(i, e);
      }
    }
    return sagaContext;
  }

  private void compensate(int index, Exception e) {
    for (int i = 0; i < index; i++) {
      tasks.get(i).compensate(e);
    }
  }

  public static class SagaBuilder {

    private List<SagaTask> tasks = new ArrayList<>();

    public SagaBuilder(SagaTask firstTask) {
      tasks.add(firstTask);
    }

    public SagaBuilder andThen(SagaTask task) {
      tasks.add(task);
      return this;
    }

    public SagaContext execute() {
      return new Saga(tasks).execute();
    }

    SagaContext execute(SagaContext sagaContext) {
      return new Saga(tasks).execute(sagaContext);
    }
  }
}
