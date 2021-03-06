package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi.{Method, ReferenceType, Type}
import org.scaladebugger.api.profiles.traits.info.{InfoProducer, MethodInfo, ReferenceTypeInfo, TypeInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureMethodInfoSpec extends ParallelMockFunSpec
{
  private val mockNewTypeProfile = mockFunction[Type, TypeInfo]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducer]
  private val mockMethod = mock[Method]
  private val pureMethodInfoProfile = new PureMethodInfo(
    mockScalaVirtualMachine,
    mockInfoProducerProfile,
    mockMethod
  ) {
    override protected def newTypeProfile(_type: Type): TypeInfo =
      mockNewTypeProfile(_type)
  }

  describe("PureMethodInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[MethodInfo]

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        (mockInfoProducerProfile.newMethodInfoProfile _)
          .expects(mockScalaVirtualMachine, mockMethod)
          .returning(expected).once()

        val actual = pureMethodInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureMethodInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockMethod

        val actual = pureMethodInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#name") {
      it("should return the name of the method") {
        val expected = "someName"

        (mockMethod.name _).expects().returning(expected).once()

        val actual = pureMethodInfoProfile.name

        actual should be (expected)
      }
    }

    describe("#returnTypeInfo") {
      it("should return the type information for the return type") {
        val expected = mock[TypeInfo]

        val mockType = mock[Type]
        (mockMethod.returnType _).expects().returning(mockType).once()

        mockNewTypeProfile.expects(mockType).returning(expected).once()

        val actual = pureMethodInfoProfile.returnTypeInfo

        actual should be (expected)
      }
    }

    describe("#returnTypeName") {
      it("should return the name of the method's return type") {
        val expected = "some.return.type"

        (mockMethod.returnTypeName _).expects().returning(expected).once()

        val actual = pureMethodInfoProfile.returnTypeName

        actual should be (expected)
      }
    }

    describe("#declaringTypeInfo") {
      it("should return a new type info profile wrapping the type that declared this method") {
        val expected = mock[ReferenceTypeInfo]

        val mockReferenceType = mock[ReferenceType]
        (mockMethod.declaringType _).expects()
          .returning(mockReferenceType).once()
        (mockInfoProducerProfile.newReferenceTypeInfoProfile _)
          .expects(mockScalaVirtualMachine, mockReferenceType)
          .returning(expected)
          .once()

        val actual = pureMethodInfoProfile.declaringTypeInfo

        actual should be (expected)
      }
    }

    describe("#parameterTypeInfo") {
      it("should return the type information for the parameter types") {
        val expected = Seq(mock[TypeInfo])

        import scala.collection.JavaConverters._
        val mockTypes: Seq[Type] = expected.map(_ => mock[Type])
        (mockMethod.argumentTypes _).expects()
          .returning(mockTypes.asJava).once()

        expected.zip(mockTypes).foreach { case (e, t) =>
          mockNewTypeProfile.expects(t).returning(e).once()
        }

        val actual = pureMethodInfoProfile.parameterTypeInfo

        actual should be (expected)
      }
    }

    describe("#parameterTypeNames") {
      it("should return an ordered collection of parameter names for the method") {
        val expected = Seq("some.parameter.type", "some.other.parameter.type")

        import scala.collection.JavaConverters._
        (mockMethod.argumentTypeNames _).expects()
          .returning(expected.asJava).once()

        val actual = pureMethodInfoProfile.parameterTypeNames

        actual should be (expected)
      }
    }
  }
}
