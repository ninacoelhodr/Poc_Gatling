package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._


class BasicSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://computer-database.gatling.io") // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  object Search {

    val searchFeeder = csv("data/search.csv").random

    val search = exec(http("Load_HomePage")
      .get("/computers"))
      .pause(2) // Note that Gatling has recorder real time pauses
      .feed(searchFeeder)
      .exec(http("Search_Computer_${searchCriterion}")
        .get("/computers?f=${searchCriterion}")
        .check(css("a:contains('${searchComputerName}')", "href").saveAs("computerURL")))
      .pause(2)
      .exec(http("Select_Computer_${searchComputerName}")
        .get("${computerURL}"))
      .pause(2)
  }

  object Browse {
    val browse =
      repeat(times = 5, counterName = "i") {
        exec(http("Browse_Page_${i}")
          .get("/computers?p=${i}"))
          .pause(2)
      }
  }

  object Create {
    val computerFeeders = csv("data/computers.csv").circular

    val create = exec(http("Load_Create_Computer_Page")
      .get("/computers/new"))
      .pause(2)
      .feed(computerFeeders)
      .exec(http("Create_Computer${computerName}") // Here's an example of a POST request
        .post("/computers")
        .formParam("name", "${computerName}") // Note the triple double quotes: used in Scala for protecting a whole chain of characters (no need for backslash)
        .formParam("introduced", "${introduced}")
        .formParam("discontinued", "${discontinued}")
        .formParam("company", "${companyId}")
        .check(status.is((200))))
  }

  val admins = scenario("Admins").exec(Search.search, Browse.browse, Create.create)
  val user = scenario("Users").exec(Search.search, Browse.browse, Create.create)

  setUp(admins.inject(atOnceUsers(1)), user.inject(atOnceUsers(1))).protocols(httpProtocol)
}
