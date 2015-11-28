package org.senkbeil.debugger.api.lowlevel.methods

import com.sun.jdi.request.{EventRequest, EventRequestManager, MethodExitRequest}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

import scala.util.{Failure, Success}

class MethodExitManagerSpec extends FunSpec with Matchers with MockFactory
  with OneInstancePerTest with org.scalamock.matchers.Matchers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockEventRequestManager = mock[EventRequestManager]

  private val methodExitManager = new MethodExitManager(mockEventRequestManager) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("MethodExitManager") {
    describe("#methodExitRequestList") {
      it("should contain all method exit requests in the form of (class, method) stored in the manager") {
        val methodExitRequests = Seq(
          ("class1", "method1"),
          ("class2", "method2")
        )

        // NOTE: Must create a new method exit manager that does NOT override
        //       the request id to always be the same since we do not allow
        //       duplicates of the test id when storing it
        val methodExitManager = new MethodExitManager(mockEventRequestManager)

        methodExitRequests.foreach { case (className, methodName) =>
          (mockEventRequestManager.createMethodExitRequest _).expects()
            .returning(stub[MethodExitRequest]).once()
          methodExitManager.createMethodExitRequest(className, methodName)
        }

        methodExitManager.methodExitRequestList should
          contain theSameElementsAs (methodExitRequests)
      }
    }

    describe("#methodExitRequestListById") {
      it("should contain all method exit request ids") {
        val methodExitRequests = Seq(
          ("id1", "class1", "method1"),
          ("id2", "class2", "method2")
        )

        methodExitRequests.foreach { case (requestId, className, methodName) =>
          (mockEventRequestManager.createMethodExitRequest _).expects()
            .returning(stub[MethodExitRequest]).once()
          methodExitManager.createMethodExitRequestWithId(
            requestId,
            className,
            methodName
          )
        }

        methodExitManager.methodExitRequestListById should
          contain theSameElementsAs (methodExitRequests.map(_._1))
      }
    }

    describe("#createMethodExitRequestWithId") {
      it("should create the method exit request using the provided id") {
        val expected = Success(java.util.UUID.randomUUID().toString)
        val testClassName = "some class name"
        val testMethodName = "some method name"

        val mockMethodExitRequest = mock[MethodExitRequest]
        (mockEventRequestManager.createMethodExitRequest _).expects()
          .returning(mockMethodExitRequest).once()

        // Should apply the class filter, set enabled to true by default, and
        // set the suspend policy to thread level by default
        (mockMethodExitRequest.addClassFilter(_: String))
          .expects(testClassName).once()
        (mockMethodExitRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockMethodExitRequest.setEnabled _).expects(true).once()

        val actual = methodExitManager.createMethodExitRequestWithId(
          expected.get,
          testClassName,
          testMethodName
        )
        actual should be(expected)
      }
    }

    describe("#createMethodExitRequest") {
      it("should create the method exit request with a class inclusion filter for the class name") {
        val expected = Success(TestRequestId)
        val testClassName = "some class name"
        val testMethodName = "some method name"

        val mockMethodExitRequest = mock[MethodExitRequest]
        (mockEventRequestManager.createMethodExitRequest _).expects()
          .returning(mockMethodExitRequest).once()

        // Should apply the class filter, set enabled to true by default, and
        // set the suspend policy to thread level by default
        (mockMethodExitRequest.addClassFilter(_: String))
          .expects(testClassName).once()
        (mockMethodExitRequest.setSuspendPolicy _)
          .expects(EventRequest.SUSPEND_EVENT_THREAD).once()
        (mockMethodExitRequest.setEnabled _).expects(true).once()

        val actual = methodExitManager.createMethodExitRequest(testClassName, testMethodName)
        actual should be (expected)
      }

      it("should return the exception if unable to create the request") {
        val expected = Failure(new Throwable)
        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createMethodExitRequest _).expects()
          .throwing(expected.failed.get).once()

        val actual = methodExitManager.createMethodExitRequest(testClassName, testMethodName)
        actual should be (expected)
      }
    }

    describe("#hasMethodExitRequestWithId") {
      it("should return true if it exists") {
        val expected = true

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createMethodExitRequest _).expects()
          .returning(stub[MethodExitRequest]).once()

        methodExitManager.createMethodExitRequestWithId(
          TestRequestId,
          testClassName,
          testMethodName
        )

        val actual = methodExitManager.hasMethodExitRequestWithId(TestRequestId)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val actual = methodExitManager.hasMethodExitRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#hasMethodExitRequest") {
      it("should return true if it exists") {
        val expected = true

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createMethodExitRequest _).expects()
          .returning(stub[MethodExitRequest]).once()

        methodExitManager.createMethodExitRequest(testClassName, testMethodName)

        val actual = methodExitManager.hasMethodExitRequest(testClassName, testMethodName)
        actual should be (expected)
      }

      it("should return false if it does not exist") {
        val expected = false

        val testClassName = "some class name"
        val testMethodName = "some method name"

        val actual = methodExitManager.hasMethodExitRequest(testClassName, testMethodName)
        actual should be (expected)
      }
    }

    describe("#getMethodExitRequestWithId") {
      it("should return Some(MethodExitRequest) if found") {
        val expected = stub[MethodExitRequest]

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createMethodExitRequest _).expects()
          .returning(expected).once()

        methodExitManager.createMethodExitRequestWithId(
          TestRequestId,
          testClassName,
          testMethodName
        )

        val actual = methodExitManager.getMethodExitRequestWithId(TestRequestId)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val actual = methodExitManager.getMethodExitRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#getMethodExitRequest") {
      it("should return Some(collection of MethodExitRequest) if found") {
        val expected = Seq(stub[MethodExitRequest])

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createMethodExitRequest _).expects()
          .returning(expected.head).once()

        methodExitManager.createMethodExitRequest(testClassName, testMethodName)

        val actual =
          methodExitManager.getMethodExitRequest(testClassName, testMethodName)
        actual should be (Some(expected))
      }

      it("should return None if not found") {
        val expected = None

        val testClassName = "some class name"
        val testMethodName = "some method name"

        val actual =
          methodExitManager.getMethodExitRequest(testClassName, testMethodName)
        actual should be (expected)
      }
    }

    describe("#removeMethodExitRequestWithId") {
      it("should return true if the method exit request was removed") {
        val expected = true
        val stubRequest = stub[MethodExitRequest]

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createMethodExitRequest _).expects()
          .returning(stubRequest).once()

        methodExitManager.createMethodExitRequestWithId(
          TestRequestId,
          testClassName,
          testMethodName
        )

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual =
          methodExitManager.removeMethodExitRequestWithId(TestRequestId)
        actual should be (expected)
      }

      it("should return false if the method exit request was not removed") {
        val expected = false

        val testClassName = "some class name"
        val testMethodName = "some method name"

        val actual =
          methodExitManager.removeMethodExitRequestWithId(TestRequestId)
        actual should be (expected)
      }
    }

    describe("#removeMethodExitRequest") {
      it("should return true if the method exit request was removed") {
        val expected = true
        val stubRequest = stub[MethodExitRequest]

        val testClassName = "some class name"
        val testMethodName = "some method name"

        (mockEventRequestManager.createMethodExitRequest _).expects()
          .returning(stubRequest).once()

        methodExitManager.createMethodExitRequest(testClassName, testMethodName)

        (mockEventRequestManager.deleteEventRequest _)
          .expects(stubRequest).once()

        val actual =
          methodExitManager.removeMethodExitRequest(testClassName, testMethodName)
        actual should be (expected)
      }

      it("should return false if the method exit request was not removed") {
        val expected = false

        val testClassName = "some class name"
        val testMethodName = "some method name"

        val actual =
          methodExitManager.removeMethodExitRequest(testClassName, testMethodName)
        actual should be (expected)
      }
    }
  }
}
