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

  setUp(getMailsScn.inject(atOnceUsers(100000)).protocols(httpConf))

}
