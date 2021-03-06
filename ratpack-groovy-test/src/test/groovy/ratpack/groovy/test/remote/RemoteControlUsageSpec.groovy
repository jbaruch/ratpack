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

package ratpack.groovy.test.remote

import ratpack.remote.RemoteControlModule
import ratpack.test.internal.RatpackGroovyDslSpec

class RemoteControlUsageSpec extends RatpackGroovyDslSpec {

  def setup() {
    other.put("remoteControl.enabled", "true")
  }

  @javax.inject.Singleton
  static class ValueHolder {
    String value = "initial"
  }

  def "can access application internals"() {
    when:
    app {
      modules {
        register new RemoteControlModule()
        bind ValueHolder
      }
    }
    handlers {
      get { ValueHolder valueHolder ->
        response.send valueHolder.value
      }
    }

    and:
    def remote = new RemoteControl(applicationUnderTest)

    then:
    text == "initial"

    when:
    remote.exec { registry.get(ValueHolder).value = "changed" }

    then:
    text == "changed"
  }

}
