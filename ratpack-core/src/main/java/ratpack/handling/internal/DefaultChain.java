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

package ratpack.handling.internal;

import com.google.common.collect.ImmutableList;
import ratpack.api.Nullable;
import ratpack.handling.Chain;
import ratpack.handling.Handler;
import ratpack.handling.Handlers;
import ratpack.launch.LaunchConfig;
import ratpack.registry.Registry;
import ratpack.util.Action;

import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableList.of;

public class DefaultChain implements Chain {

  private final List<Handler> handlers;
  private final LaunchConfig launchConfig;
  private final Registry registry;

  public DefaultChain(List<Handler> handlers, LaunchConfig launchConfig, @Nullable Registry registry) {
    this.handlers = handlers;
    this.launchConfig = launchConfig;
    this.registry = registry;
  }

  public Chain handler(Handler handler) {
    handlers.add(handler);
    return this;
  }

  public Chain prefix(String prefix, Handler... handlers) {
    return handler(Handlers.prefix(prefix, ImmutableList.copyOf(handlers)));
  }

  public Chain prefix(String prefix, List<Handler> handlers) {
    return handler(Handlers.prefix(prefix, handlers));
  }

  public Chain prefix(String prefix, Action<? super Chain> builder) {
    return handler(Handlers.prefix(prefix, Handlers.chainList(launchConfig, getRegistry(), builder)));
  }

  public Chain handler(String path, Handler handler) {
    return handler(Handlers.path(path, of(handler)));
  }

  public Chain get(String path, Handler handler) {
    return handler(Handlers.path(path, of(Handlers.get(), handler)));
  }

  public Chain get(Handler handler) {
    return get("", handler);
  }

  public Chain post(String path, Handler handler) {
    return handler(Handlers.path(path, of(Handlers.post(), handler)));
  }

  public Chain post(Handler handler) {
    return post("", handler);
  }

  public Chain put(String path, Handler handler) {
    return handler(Handlers.path(path, of(Handlers.put(), handler)));
  }

  public Chain put(Handler handler) {
    return put("", handler);
  }

  public Chain delete(String path, Handler handler) {
    return handler(Handlers.path(path, of(Handlers.delete(), handler)));
  }

  public Chain delete(Handler handler) {
    return delete("", handler);
  }

  public Chain assets(String path, String... indexFiles) {
    return handler(Handlers.assets(path, indexFiles.length == 0 ? launchConfig.getIndexFiles() : copyOf(indexFiles)));
  }

  public Chain register(Object service, List<Handler> handlers) {
    return handler(Handlers.register(service, handlers));
  }

  public <T> Chain register(Class<? super T> type, T service, List<Handler> handlers) {
    return handler(Handlers.register(type, service, handlers));
  }

  public Chain fileSystem(String path, List<Handler> handlers) {
    return handler(Handlers.fileSystem(path, handlers));
  }

  public Registry getRegistry() {
    return registry;
  }

  public LaunchConfig getLaunchConfig() {
    return launchConfig;
  }

  public Chain header(String headerName, String headerValue, Handler handler) {
    return handler(Handlers.header(headerName, headerValue, handler));
  }

}
