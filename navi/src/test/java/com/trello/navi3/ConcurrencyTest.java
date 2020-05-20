/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trello.navi3;

import com.trello.navi3.Event.Type;
import com.trello.navi3.internal.NaviEmitter;

import org.junit.Test;

import androidx.annotation.NonNull;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public final class ConcurrencyTest {

  private final NaviEmitter emitter = NaviEmitter.createActivityEmitter();

  // Verify that we can handle a listener removing itself due to an event occurring
  @Test public void handleInnerRemovals() {
    final Listener<Object> listener1 = spy(new Listener<Object>() {
      @Override public void call(@NonNull Object __) {
        emitter.removeListener(this);
      }
    });
    final Listener<Object> listener2 = spy(new Listener<Object>() {
      @Override public void call(@NonNull Object __) {
        emitter.removeListener(this);
      }
    });

    emitter.addListener(Event.RESUME, listener1);
    emitter.addListener(Event.RESUME, listener2);
    emitter.onResume();
    verify(listener1).call(any());
    verify(listener2).call(any());
  }

  // Verify that listeners added while emitting an item do not also get the current emission
  // (since they were not registered at the time of the event).
  @Test public void addDuringEmit() {
    final Listener<Object> addedDuringEmit = mock(Listener.class);
    final Listener<Object> listener = spy(new Listener<Object>() {
      @Override public void call(@NonNull Object __) {
        emitter.addListener(Event.RESUME, addedDuringEmit);
      }
    });

    emitter.addListener(Event.RESUME, listener);
    emitter.onResume();

    verify(listener).call(any());
    verifyZeroInteractions(addedDuringEmit);
  }

  // Verify that adding an ALL listener during emission
  // doesn't cause it to get the current emission
  @Test public void addAllDuringEmit() {
    final Listener<Type> addedDuringEmit = mock(Listener.class);
    final Listener<Object> listener = spy(new Listener<Object>() {
      @Override public void call(@NonNull Object aVoid) {
        emitter.addListener(Event.ALL, addedDuringEmit);
      }
    });

    emitter.addListener(Event.RESUME, listener);
    emitter.onResume();

    verify(listener).call(any());
    verifyZeroInteractions(addedDuringEmit);
  }

  // Verify that adding a listener during an ALL emission
  // doesn't cause it to get the current emission
  @Test public void addDuringEmitAll() {
    final Listener<Object> addedDuringEmit = mock(Listener.class);
    final Listener<Type> listener = spy(new Listener<Type>() {
      @Override public void call(@NonNull Type type) {
        emitter.addListener(Event.RESUME, addedDuringEmit);
      }
    });

    emitter.addListener(Event.ALL, listener);
    emitter.onResume();

    verify(listener).call(Type.RESUME);
    verifyZeroInteractions(addedDuringEmit);
  }

  // Verify that listeners removed while emitting an event still receive it (since they were
  // registered at the time of the event).
  @Test public void removeDuringEmit() {
    final Listener<Object> removedDuringEmit = mock(Listener.class);
    final Listener<Object> listener = spy(new Listener<Object>() {
      @Override public void call(@NonNull Object __) {
        emitter.removeListener(removedDuringEmit);
      }
    });

    emitter.addListener(Event.RESUME, listener);
    emitter.addListener(Event.RESUME, removedDuringEmit);
    emitter.onResume();

    verify(listener).call(any());
    verify(removedDuringEmit).call(any());
  }

  // Verify that removing an ALL listener during emission
  // doesn't cause it to lose the current emission
  @Test public void removeAllDuringEmit() {
    final Listener<Type> removedDuringEmit = mock(Listener.class);
    final Listener<Object> listener = spy(new Listener<Object>() {
      @Override public void call(@NonNull Object __) {
        emitter.removeListener(removedDuringEmit);
      }
    });

    emitter.addListener(Event.RESUME, listener);
    emitter.addListener(Event.ALL, removedDuringEmit);
    emitter.onResume();

    verify(listener).call(any());
    verify(removedDuringEmit).call(Type.RESUME);
  }

  // Verify that removing a listener during an ALL emission
  // doesn't cause it to lose the current emission
  @Test public void removeDuringEmitAll() {
    final Listener<Object> removedDuringEmit = mock(Listener.class);
    final Listener<Type> listener = spy(new Listener<Type>() {
      @Override public void call(@NonNull Type type) {
        emitter.removeListener(removedDuringEmit);
      }
    });

    emitter.addListener(Event.ALL, listener);
    emitter.addListener(Event.RESUME, removedDuringEmit);
    emitter.onResume();

    verify(listener).call(Type.RESUME);
    verify(removedDuringEmit).call(any());
  }
}
