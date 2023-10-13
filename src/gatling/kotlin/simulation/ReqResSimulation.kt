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
      // variable to setup the gatling http protocol, set base url to reqres.in
      http.baseUrl("https://reqres.in/")

  private val getUserScn =
      // the string parameter is actually not used in report
      scenario("GetUserScenario")
          // this is the one used to group up the requests in the report
          .group("get user scenario")
          .on(
              exec(
                      // set the request name
                      http("get user list")
                          // method and path of the api to test
                          .get("/api/users?page=2")
                          // asserts the response
                          .check(
                              // for example, asserts if the response code is 200
                              status().`is`(200),
                              // use jmespath to get the response body
                              jmesPath("data[0].id")
                                  // and then save it to a session's attribute
                                  .saveAs("userId")
                                  // this is the error message if there's no user found
                                  .name("no user found")))
                  // read more about session
                  // https://gatling.io/docs/gatling/reference/current/core/session/session_api/#session
                  .exec(
                      http("get user detail")
                          // use the saved attribute from previous api call here using gatling's
                          // expression laguage
                          .get("/api/users/#{userId}")
                          .check(status().`is`(200))))

  private val createUserScn =
      scenario("CreateUserScenario")
          .exec(
              http("create user")
                  // method and path of the api to test
                  .post("/api/users")
                  // we use StringBody() for body request, but there are other methods
                  .body(StringBody("""{ "name": "gavril", "job": "quality" }"""))
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
            // set the scenarion that we want to run and the load model
            // gatling support open and closed models
            getUserScn.injectOpen(constantUsersPerSec(1.0).during(Duration.ofSeconds(15))),
            createUserScn.injectOpen(atOnceUsers(5)),
            editUserScn.injectClosed(constantConcurrentUsers(2).during(10)))
        .protocols(httpProtocol)
  }
}
