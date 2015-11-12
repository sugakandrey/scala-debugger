package org.senkbeil.debugger.api.lowlevel.monitors

import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.request.{EventRequest, EventRequestManager, MonitorContendedEnterRequest}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.requests.{JDIRequestArgument, JDIRequestProcessor}

import scala.util.{Failure, Success}

class MonitorContendedEnterManagerSpec extends FunSpec with Matchers with MockFactory
  with OneInstancePerTest with org.scalamock.matchers.Matchers
{
  private val TestId = java.util.UUID.randomUUID().toString
  private val mockEventRequestManager = mock[EventRequestManager]

  private val monitorContendedEnterManager = new MonitorContendedEnterManager(mockEventRequestManager) {
    override protected def newRequestId(): String = TestId
  }

  describe("MonitorContendedEnterManager") {
    describe("#monitorContendedEnterRequestList") {
      it("should contain all monitor contended enter requests in the form of id -> request stored in the manager") {
        val monitorContendedEnterRequests = Seq(
          java.util.UUID.randomUUID().toString,
          java.util.UUID.randomUUID().toString
        )

        // Create a MonitorContendedEnterManager whose generated request ids match the list
        // above
        val monitorContendedEnterManager = new MonitorContendedEnterManager(mockEventRequestManager) {
          private val counter = new AtomicInteger(0)
          override protected def newRequestId(): String = {
            monitorContendedEnterRequests(counter.getAndIncrement % monitorContendedEnterRequests.length)
          }
        }

        monitorContendedEnterRequests.foreach { case id =>
          (mockEventRequestManager.createMonitorContendedEnterRequest _).expects()
            .returning(stub[MonitorContendedEnterRequest]).once()
          monitorContendedEnterManager.createMonitorContendedEnterRequest()
        }

        monitorContendedEnterManager.monitorContendedEnterRequestList should
          contain theSameElementsAs (monitorContendedEnterRequests)
      }
    }

    describe("#createMonitorContendedEnterRequest") {
      it("should create the monitor contended enter request and return Success(id)") {
        val expected = Success(TestId)

        val mockMonitorContendedEnterRequest = mock[MonitorContendedEnterRequest]
        (mockEventRequestManager.createMonitorContendedEnterRequest _).expects()
          .returning(mockMonitorContendedEnterRequest).once()

        // Should set enabled to true by default, and
        // set the suspend policy to vm level by default
        (mockMonitorContendedEnterRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockMonitorContendedEnterRequest.setEnabled _).expects(true).once()

        val actual = monitorContendedEnterManager.createMonitorContendedEnterRequest()
        actual should be (expected)
      }

      it("should return the exception if unable to create the request") {
        val expected = Failure(new Throwable)

        (mockEventRequestManager.createMonitorContendedEnterRequest _).expects()
          .throwing(expected.failed.get).once()

        val actual = monitorContendedEnterManager.createMonitorContendedEnterRequest()
        actual should be (expected)
      }
    }

    describe("#hasMonitorContendedEnterRequest") {
      it("should return true if it exists") {
        val expected = true

        (mockEventRequestManager.createMonitorContendedEnterRequest _).expects()
          .returning(stub[MonitorContendedEnterRequest]).once()

        val id = monitorContendedEnterManager.createMonitorContendedEnterRequest().get

        val actual = monitorContendedEnterManager.hasMonitorContendedEnterRequest(id)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = monitorContendedEnterManager.hasMonitorContendedEnterRequest(TestId)
        actual should be (expected)
      }
    }

    describe("#getMonitorContendedEnterRequest") {
      it("should return Some(MonitorContendedEnterRequest) if found") {
        val expected = stub[MonitorContendedEnterRequest]

        (mockEventRequestManager.createMonitorContendedEnterRequest _).expects()
          .returning(expected).once()

        val id = monitorContendedEnterManager.createMonitorContendedEnterRequest().get

        val actual = monitorContendedEnterManager.getMonitorContendedEnterRequest(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = monitorContendedEnterManager.getMonitorContendedEnterRequest(TestId)
        actual should be (expected)
      }
    }

    describe("#getMonitorContendedEnterArguments") {
      it("should return Some(Seq(input args)) if found") {
        val expected = Seq(mock[JDIRequestArgument], mock[JDIRequestArgument])
        expected.foreach(a => {
          val mockRequestProcessor = mock[JDIRequestProcessor]
          (mockRequestProcessor.process _).expects(*)
            .onCall((er: EventRequest) => er).once()
          (a.toProcessor _).expects().returning(mockRequestProcessor).once()
        })

        (mockEventRequestManager.createMonitorContendedEnterRequest _).expects()
          .returning(stub[MonitorContendedEnterRequest]).once()

        val id = monitorContendedEnterManager.createMonitorContendedEnterRequest(expected: _*).get

        val actual = monitorContendedEnterManager.getMonitorContendedEnterRequestArguments(id)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = monitorContendedEnterManager.getMonitorContendedEnterRequestArguments(TestId)
        actual should be (expected)
      }
    }

    describe("#removeMonitorContendedEnterRequest") {
      it("should return true if the monitor contended enter request was removed") {
        val expected = true
        val stubRequest = stub[MonitorContendedEnterRequest]

        (mockEventRequestManager.createMonitorContendedEnterRequest _).expects()
          .returning(stubRequest).once()

        val id = monitorContendedEnterManager.createMonitorContendedEnterRequest().get

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual = monitorContendedEnterManager.removeMonitorContendedEnterRequest(id)
        actual should be (expected)
      }

      it("should return false if the monitor contended enter request was not removed") {
        val expected = false

        val actual = monitorContendedEnterManager.removeMonitorContendedEnterRequest(TestId)
        actual should be (expected)
      }
    }
  }
}
