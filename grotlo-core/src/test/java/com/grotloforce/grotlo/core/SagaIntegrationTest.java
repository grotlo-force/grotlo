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

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by msavic on 6/29/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class SagaIntegrationTest {

  private static final String TASK1 = "task1";
  private static final String TASK2 = "task2";

  @Mock
  private BiFunction<SagaContext, Object, Object> request1;
  @Mock
  private BiConsumer<Object, Exception> compesatingRequest1;
  @Mock
  private BiFunction<SagaContext, Object, Object> request2;
  @Mock
  private BiConsumer<Object, Exception> compesatingRequest2;

  private Object input;
  private SagaTask<Object, Object> task1;
  private SagaTask<Object, Object> task2;

  @Before
  public void setup() {
    input = new Object();
    task1 = SagaTask.builder()
        .name(TASK1)
        .request(request1)
        .compesatingRequest(compesatingRequest1)
        .build(input);
    task2 = SagaTask.builder()
        .name(TASK2)
        .request(request2)
        .compesatingRequest(compesatingRequest2)
        .build(input);
  }


  @Test
  public void shouldExecuteOneTask() {
    // given
    SagaContext sagaContext = new SagaContext();
    Object output = new Object();
    given(request1.apply(sagaContext, input)).willReturn(output);

    // when
    Saga.startWith(task1).execute(sagaContext);

    // then
    verify(request1, times(1)).apply(sagaContext, input);
    verify(compesatingRequest1, never()).accept(eq(input), any(Exception.class));
    assertTrue(sagaContext.isSuccessful());
    assertEquals(output, sagaContext.getResult(TASK1));
  }

  @Test
  public void shouldExecuteTwoTasksInSequentialOrder() {
    // given
    SagaContext sagaContext = new SagaContext();
    Object output1 = new Object();
    Object output2 = new Object();
    given(request1.apply(sagaContext, input)).willReturn(output1);
    given(request2.apply(sagaContext, input)).willReturn(output2);

    // when
    Saga.startWith(task1).andThen(task2).execute(sagaContext);

    // then
    verify(request1, times(1)).apply(sagaContext, input);
    verify(compesatingRequest1, never()).accept(eq(input), any(Exception.class));
    verify(request2, times(1)).apply(sagaContext, input);
    verify(compesatingRequest2, never()).accept(eq(input), any(Exception.class));
    assertTrue(sagaContext.isSuccessful());
    assertEquals(output1, sagaContext.getResult(TASK1));
    assertEquals(output2, sagaContext.getResult(TASK2));
  }

  @Test
  public void shouldCompensateFirstTaskWhenSecondFails() {
    // given
    RuntimeException exception = new RuntimeException();
    SagaContext sagaContext = new SagaContext();
    Object output1 = new Object();
    given(request1.apply(sagaContext, input)).willReturn(output1);
    given(request2.apply(sagaContext, input)).willThrow(exception);

    // when
    Saga.startWith(task1).andThen(task2).execute(sagaContext);

    // then
    verify(request1, times(1)).apply(sagaContext, input);
    verify(compesatingRequest1, times(1)).accept(eq(input), eq(exception));
    verify(request2, times(1)).apply(sagaContext, input);
    verify(compesatingRequest2, never()).accept(eq(input), any(Exception.class));
    assertFalse(sagaContext.isSuccessful());
    assertEquals(output1, sagaContext.getResult(TASK1));
  }
}
