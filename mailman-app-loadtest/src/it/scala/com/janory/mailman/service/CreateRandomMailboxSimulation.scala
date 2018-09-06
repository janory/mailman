package com.janory.mailman.service

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class CreateRandomMailboxSimulation extends Simulation {


  val httpConf = http.baseURL("http://localhost:9000")

  val getMailsScn = scenario("Create random Mailbox")
    .exec(
      http("Create mailbox request").post("/mailboxes")
    )

//  setUp(getMailsScn.inject(constantUsersPerSec(1000) during(30 seconds)).protocols(httpConf))
//  setUp(getMailsScn.inject(rampUsers(100) over(30 seconds)).protocols(httpConf))
//  setUp(getMailsScn.inject(rampUsersPerSec(100) to 2000 during(60 seconds)).protocols(httpConf))
  setUp(getMailsScn.inject(atOnceUsers(2000)).protocols(httpConf))

}
