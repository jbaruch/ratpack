/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ratpack.reload.internal;

import io.netty.buffer.ByteBuf;
import ratpack.util.Factory;
import ratpack.util.internal.IoUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReloadableFileBackedFactory<T> implements Factory<T> {

  // Note: we are blocking for IO on the main thread here, but it only really impacts
  // reloadable mode so it's not worth the complication of jumping off the main thread

  private final File file;
  private final boolean reloadable;
  private final Delegate<T> delegate;

  private final AtomicLong lastModifiedHolder = new AtomicLong(-1);
  private final AtomicReference<ByteBuf> contentHolder = new AtomicReference<>();
  private final AtomicReference<T> delegateHolder = new AtomicReference<>(null);
  private final Lock lock = new ReentrantLock();

  static public interface Delegate<T> {
    T produce(File file, ByteBuf bytes);
  }

  public ReloadableFileBackedFactory(File file, boolean reloadable, Delegate<T> delegate) {
    this.file = file;
    this.reloadable = reloadable;
    this.delegate = delegate;

    if (!reloadable) {
      try {
        refresh();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public T create() {
    if (!reloadable) {
      return delegateHolder.get();
    }

    if (!file.exists()) {
      return null;
    }

    try {
      if (refreshNeeded()) {
        refresh();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return delegateHolder.get();
  }

  private boolean isBytesAreSame() throws IOException {
    lock.lock();
    try {
      ByteBuf existing = contentHolder.get();
      //noinspection SimplifiableIfStatement
      if (existing == null) {
        return false;
      }

      return IoUtils.readFile(file).equals(existing);
    } finally {
      lock.unlock();
    }
  }

  private boolean refreshNeeded() throws IOException {
    return (file.lastModified() != lastModifiedHolder.get()) || !isBytesAreSame();
  }

  private void refresh() throws IOException {
    lock.lock();
    try {
      long lastModifiedTime = file.lastModified();
      ByteBuf bytes = IoUtils.readFile(file);

      if (lastModifiedTime == lastModifiedHolder.get() && bytes.equals(contentHolder.get())) {
        return;
      }

      delegateHolder.set(delegate.produce(file, bytes));

      this.lastModifiedHolder.set(lastModifiedTime);
      this.contentHolder.set(bytes);
    } finally {
      lock.unlock();
    }
  }


}
