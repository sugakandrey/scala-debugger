package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestMiscInfoTrait

class PureFieldInfoSpec extends ParallelMockFunSpec
{
  private val mockNewTypeProfile = mockFunction[Type, TypeInfo]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducer]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockObjectReference = mock[ObjectReference]
  private val mockField = mock[Field]
  private val pureFieldInfoProfile = new PureFieldInfo(
    mockScalaVirtualMachine,
    mockInfoProducerProfile,
    Left(mockObjectReference),
    mockField
  )(mockVirtualMachine) {
    override protected def newTypeProfile(_type: Type): TypeInfo =
      mockNewTypeProfile(_type)
  }

  describe("PureFieldInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation when wrapping an object reference") {
        val expected = mock[FieldVariableInfo]

        val offsetIndex = 999
        val pureFieldInfoProfile = new PureFieldInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mockObjectReference),
          mockField,
          offsetIndex
        )(mockVirtualMachine)

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockInfoProducerProfile.newFieldInfoProfile(
          _: ScalaVirtualMachine,
          _: Either[ObjectReference, ReferenceType],
          _: Field,
          _: Int
        )(
          _: VirtualMachine
        )).expects(
          mockScalaVirtualMachine,
          Left(mockObjectReference),
          mockField,
          offsetIndex,
          *
        ).returning(expected).once()

        val actual = pureFieldInfoProfile.toJavaInfo

        actual should be (expected)
      }

      it("should return a new instance of the Java profile representation when wrapping a reference type") {
        val expected = mock[FieldVariableInfo]
        val mockReferenceType = mock[ReferenceType]

        val offsetIndex = 999
        val pureFieldInfoProfile = new PureFieldInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockReferenceType),
          mockField,
          offsetIndex
        )(mockVirtualMachine)

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockInfoProducerProfile.newFieldInfoProfile(
          _: ScalaVirtualMachine,
          _: Either[ObjectReference, ReferenceType],
          _: Field,
          _: Int
        )(
          _: VirtualMachine
        )).expects(
          mockScalaVirtualMachine,
          Right(mockReferenceType),
          mockField,
          offsetIndex,
          *
        ).returning(expected).once()

        val actual = pureFieldInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureFieldInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockField

        val actual = pureFieldInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#name") {
      it("should return the field's name") {
        val expected = "someName"

        (mockField.name _).expects().returning(expected).once()

        val actual = pureFieldInfoProfile.name

        actual should be (expected)
      }
    }

    describe("#typeName") {
      it("should return the field's type name") {
        val expected = "some.type.name"

        (mockField.typeName _).expects().returning(expected).once()

        val actual = pureFieldInfoProfile.typeName

        actual should be (expected)
      }
    }

    describe("#typeInfo") {
      it("should should return a new type info profile wrapping the type") {
        val expected = mock[TypeInfo]

        val mockType = mock[Type]
        (mockField.`type` _).expects().returning(mockType).once()

        mockNewTypeProfile.expects(mockType)
          .returning(expected).once()

        val actual = pureFieldInfoProfile.typeInfo

        actual should be (expected)
      }
    }

    describe("#parent") {
      it("should return Left(object) if the parent of the field is an object") {
        val expected = Left(mock[ObjectInfo])

        val pureFieldInfoProfile = new PureFieldInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mock[ObjectReference]),
          mockField
        )(mockVirtualMachine) {
          override protected def newObjectProfile(
            objectReference: ObjectReference
          ): ObjectInfo = expected.left.get
        }

        val actual = pureFieldInfoProfile.parent

        actual should be (expected)
      }

      it("should return Right(type) if the parent of the field is a type") {
        val expected = Right(mock[ReferenceTypeInfo])

        val pureFieldInfoProfile = new PureFieldInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mock[ReferenceType]),
          mockField
        )(mockVirtualMachine) {
          override protected def newReferenceTypeProfile(
            referenceType: ReferenceType
          ): ReferenceTypeInfo = expected.right.get
        }

        val actual = pureFieldInfoProfile.parent

        actual should be (expected)
      }
    }

    describe("#declaringTypeInfo") {
      it("should return a new type info profile wrapping the type that declared this field") {
        val expected = mock[ReferenceTypeInfo]

        val mockReferenceType = mock[ReferenceType]
        (mockField.declaringType _).expects()
          .returning(mockReferenceType).once()
        (mockInfoProducerProfile.newReferenceTypeInfoProfile _)
          .expects(mockScalaVirtualMachine, mockReferenceType)
          .returning(expected)
          .once()

        val actual = pureFieldInfoProfile.declaringTypeInfo

        actual should be (expected)
      }
    }

    describe("#isField") {
      it("should return true") {
        val expected = true

        val actual = pureFieldInfoProfile.isField

        actual should be (expected)
      }
    }

    describe("#isArgument") {
      it("should return false") {
        val expected = false

        val actual = pureFieldInfoProfile.isArgument

        actual should be (expected)
      }
    }

    describe("#isLocal") {
      it("should return false") {
        val expected = false

        val actual = pureFieldInfoProfile.isLocal

        actual should be (expected)
      }
    }

    describe("#setValueFromInfo") {
      it("should throw an exception if no object reference or class type available") {
        val pureFieldInfoProfile = new PureFieldInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mock[ReferenceType]),
          mockField
        )(mockVirtualMachine)

        // Retrieval of JDI value still happens first
        val mockValueInfoProfile = mock[ValueInfo]
        (mockValueInfoProfile.toJdiInstance _).expects()
          .returning(mock[Value]).once()

        intercept[Exception] {
          pureFieldInfoProfile.setValueFromInfo(mockValueInfoProfile)
        }
      }

      it("should be able to set instance fields") {
        val expected = mock[ValueInfo]

        val mockStringReference = mock[StringReference]

        (expected.toJdiInstance _).expects()
          .returning(mockStringReference).once()

        // Ensure setting the value on the object is verified
        (mockObjectReference.setValue _)
          .expects(mockField, mockStringReference)
          .once()

        pureFieldInfoProfile.setValueFromInfo(expected) should be (expected)
      }

      it("should be able to set static fields") {
        val expected = mock[ValueInfo]

        val mockClassType = mock[ClassType]
        val pureFieldInfoProfile = new PureFieldInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockClassType),
          mockField
        )(mockVirtualMachine)

        val mockStringReference = mock[StringReference]

        (expected.toJdiInstance _).expects()
          .returning(mockStringReference).once()

        // Ensure setting the value on the object is verified
        (mockClassType.setValue _)
          .expects(mockField, mockStringReference)
          .once()

        pureFieldInfoProfile.setValueFromInfo(expected) should be (expected)
      }
    }

    describe("#toValueInfo") {
      it("should return a wrapper around the value of a class' static field") {
        val expected = mock[ValueInfo]
        val mockValue = mock[Value]

        // Retrieving the value of the field returns our mock
        val mockClassType = mock[ClassType]
        (mockClassType.getValue _).expects(mockField)
          .returning(mockValue).once()

        val mockNewValueProfile = mockFunction[Value, ValueInfo]
        val pureFieldInfoProfile = new PureFieldInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Right(mockClassType),
          mockField
        )(mockVirtualMachine) {
          override protected def newValueProfile(value: Value): ValueInfo =
            mockNewValueProfile(value)
        }

        mockNewValueProfile.expects(mockValue).returning(expected).once()
        pureFieldInfoProfile.toValueInfo should be (expected)
      }

      it("should return a wrapper around the value of an object's field instance") {
        val expected = mock[ValueInfo]
        val mockValue = mock[Value]

        // Retrieving the value of the field returns our mock
        (mockObjectReference.getValue _).expects(mockField)
          .returning(mockValue).once()

        val mockNewValueProfile = mockFunction[Value, ValueInfo]
        val pureFieldInfoProfile = new PureFieldInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          Left(mockObjectReference),
          mockField
        )(mockVirtualMachine) {
          override protected def newValueProfile(value: Value): ValueInfo =
            mockNewValueProfile(value)
        }

        mockNewValueProfile.expects(mockValue).returning(expected).once()
        pureFieldInfoProfile.toValueInfo should be (expected)
      }
    }
  }
}
