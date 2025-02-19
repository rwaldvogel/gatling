/*
 * Copyright 2011-2021 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class InjectionSampleKotlin : Simulation() {

  private val httpProtocol = http
  private val scn = scenario("scenario")

  init {
//#open-injection
setUp(
  scn.injectOpen(
    nothingFor(4), // 1
    atOnceUsers(10), // 2
    rampUsers(10).during(5), // 3
    constantUsersPerSec(20.0).during(15), // 4
    constantUsersPerSec(20.0).during(15).randomized(), // 5
    rampUsersPerSec(10.0).to(20.0).during(10), // 6
    rampUsersPerSec(10.0).to(20.0).during(10).randomized(), // 7
    stressPeakUsers(1000).during(20) // 8
  ).protocols(httpProtocol)
)
//#open-injection

//#closed-injection
setUp(
  scn.injectClosed(
    constantConcurrentUsers(10).during(10), // 1
    rampConcurrentUsers(10).to(20).during(10) // 2
  )
)
//#closed-injection

//#incrementConcurrentUsers
// generate a closed workload injection profile
// with levels of 10, 15, 20, 25 and 30 concurrent users
// each level lasting 10 seconds
// separated by linear ramps lasting 10 seconds
setUp(
  scn.injectClosed(
    incrementConcurrentUsers(5)
      .times(5)
      .eachLevelLasting(10)
      .separatedByRampsLasting(10)
      .startingFrom(10) // Int
  )
)
//#incrementConcurrentUsers

//#incrementUsersPerSec
// generate an open workload injection profile
// with levels of 10, 15, 20, 25 and 30 arriving users per second
// each level lasting 10 seconds
// separated by linear ramps lasting 10 seconds
setUp(
  scn.injectOpen(
    incrementUsersPerSec(5.0)
      .times(5)
      .eachLevelLasting(10)
      .separatedByRampsLasting(10)
      .startingFrom(10.0) // Double
  )
)
//#incrementUsersPerSec

val scenario1 = scenario("scenario1")
val scenario2 = scenario("scenario2")
val injectionProfile1 = atOnceUsers(1)
val injectionProfile2 = atOnceUsers(1)

//#multiple
setUp(
  scenario1.injectOpen(injectionProfile1),
  scenario2.injectOpen(injectionProfile2)
)
//#multiple

val parent = scenario("parent")
val child1 = scenario("child1")
val child2 = scenario("child2")
val grandChild = scenario("grandChild")
val injectionProfile = constantConcurrentUsers(5).during(5)

//#andThen
setUp(
  parent.injectClosed(injectionProfile)
    // child1 and child2 will start at the same time when last parent user will terminate
    .andThen(
      child1.injectClosed(injectionProfile)
        // grandChild will start when last child1 user will terminate
        .andThen(grandChild.injectClosed(injectionProfile)),
      child2.injectClosed(injectionProfile)
    )
)
//#andThen

//#noShard
setUp(
  // parent load won't be sharded
  parent.injectOpen(atOnceUsers(1)).noShard()
    .andThen(
      // child load will be sharded
      child1.injectClosed(injectionProfile)
    )
)
//#noShard
  }
}
