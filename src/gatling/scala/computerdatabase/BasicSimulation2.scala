package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._


class BasicSimulation2 extends Simulation {

  val httpProtocol = http
    .baseUrl("http://computer-database.gatling.io") // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  object Search {

    val search = exec(http("Load_HomePage")
      .get("/computers"))
      .pause(1) // Note that Gatling has recorder real time pauses

  }
  val user = scenario("Users").exec(Search.search)

  setUp(
    user.inject(
      atOnceUsers(20),
      rampUsers(10)during (90),
      rampUsers(0) during(20)
      )).protocols(httpProtocol)
}
