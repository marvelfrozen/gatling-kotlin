@file:Suppress("unused")

package simulation

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import io.github.serpro69.kfaker.Faker
import java.time.Duration

class ReqResSimulation : Simulation() {
  private val httpProtocol =
      http.baseUrl( // variable to setup the gatling http protocol,
          "https://reqres.in/") // set base url to reqres.in

  private val getUserScn =
      scenario("GetUserScenario") // the string parameter is actually not used in report
          .group("get user scenario") // this is the one used in report to group up the requests
          .on(
              exec(
                      http("get user list") // set the request name
                          .get("/api/users?page=2") // method and path of the api to test
                          .check( // assert the response
                              status().`is`(200), // for example, assert if the response code is 200
                              jmesPath("data[0].id") // use jmespath to get the response body
                                  .saveAs("userId") // and then save it to a variable
                                  .name( // this is the error message if there's no user found
                                      "no user found")))
                  .exec(
                      http("get user detail")
                          .get(
                              "/api/users/#{userId}") // use the saved variable from previous api
                                                      // call here
                          .check(status().`is`(200))))

  private val createUserScn =
      scenario("CreateUserScenario")
          .exec(
              http("create user")
                  .post("/api/users") // method and path of the api to test
                  .body(
                      StringBody(
                          """{ "name": "gavril", "job": "quality" }""")) // we use StringBody() for
                  // body request, but there
                  // are other methods
                  .check(status().`is`(201)))

  private val editUserScn =
      scenario("EditUserScenario")
          .exec(
              http("edit user")
                  .put("/api/users/2")
                  .body(
                      StringBody(
                          """{ "name": "${Faker().name.name()}", "job": "${Faker().job.field()}" }"""))
                  .check(status().`is`(200)))

  init {
    setUp(
            getUserScn.injectOpen( // set the scenarion that we want to run and the load model
                constantUsersPerSec(1.0).during(Duration.ofSeconds(15))),
            createUserScn.injectOpen(atOnceUsers(5)), // gatling support open and closed models
            editUserScn.injectClosed( // read more https://gatling.io/docs/gatling/reference/current/core/injection/#open-vs-closed-workload-models
                constantConcurrentUsers(2).during(10)))
        .protocols(httpProtocol)
  }
}
