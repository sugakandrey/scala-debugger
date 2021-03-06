package org.scaladebugger.api.dsl.info

import com.sun.jdi.VirtualMachine
import org.scaladebugger.api.lowlevel.ManagerContainer
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.traits.DebugProfile
import org.scaladebugger.api.profiles.traits.info.{ObjectInfo, VariableInfo}
import org.scaladebugger.api.virtualmachines.{ObjectCache, ScalaVirtualMachine, ScalaVirtualMachineManager}
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class VariableInfoDSLWrapperSpec extends ParallelMockFunSpec
{
  private val TestUniqueId = 1234L
  private val mockObjectInfoProfile = mock[ObjectInfo]
  private val mockVariableInfoProfile = mock[VariableInfo]

  private val testObjectCache = new ObjectCache()
  private val testScalaVirtualMachine = new Object with ScalaVirtualMachine {
    override val cache: ObjectCache = testObjectCache
    override val lowlevel: ManagerContainer = null
    override val manager: ScalaVirtualMachineManager = null
    override def startProcessingEvents(): Unit = {}
    override def isInitialized: Boolean = false
    override def isProcessingEvents: Boolean = false
    override def suspend(): Unit = {}
    override def stopProcessingEvents(): Unit = {}
    override def resume(): Unit = {}
    override def initialize(
      defaultProfile: String,
      startProcessingEvents: Boolean
    ): Unit = {}
    override val underlyingVirtualMachine: VirtualMachine = null
    override def isStarted: Boolean = false
    override val uniqueId: String = ""
    override protected val profileManager: ProfileManager = null
    override def register(
      name: String,
      profile: DebugProfile
    ): Option[DebugProfile] = None
    override def retrieve(name: String): Option[DebugProfile] = None
    override def unregister(name: String): Option[DebugProfile] = None
  }

  describe("VariableInfoDSLWrapper") {
    describe("#cache") {
      it("should add the value of the variable to the implicit cache if available") {
        import org.scaladebugger.api.dsl.Implicits.VariableInfoDSL

        (mockObjectInfoProfile.uniqueId _).expects()
          .returning(TestUniqueId).once()

        (mockVariableInfoProfile.toValueInfo _).expects()
          .returning(mockObjectInfoProfile).once()

        (mockObjectInfoProfile.isObject _).expects().returning(true).once()
        (mockObjectInfoProfile.toObjectInfo _).expects()
          .returning(mockObjectInfoProfile).once()

        implicit val objectCache: ObjectCache = testObjectCache
        mockVariableInfoProfile.cache()

        objectCache.has(TestUniqueId) should be (true)
      }

      it("should add the value of the variable to underlying JVM's cache if no implicit available") {
        import org.scaladebugger.api.dsl.Implicits.VariableInfoDSL

        (mockVariableInfoProfile.scalaVirtualMachine _).expects()
          .returning(testScalaVirtualMachine).once()

        (mockObjectInfoProfile.uniqueId _).expects()
          .returning(TestUniqueId).once()

        (mockVariableInfoProfile.toValueInfo _).expects()
          .returning(mockObjectInfoProfile).once()

        (mockObjectInfoProfile.isObject _).expects().returning(true).once()
        (mockObjectInfoProfile.toObjectInfo _).expects()
          .returning(mockObjectInfoProfile).once()

        mockVariableInfoProfile.cache()

        testObjectCache.has(TestUniqueId) should be (true)
      }
    }

    describe("#uncache") {
      it("should remove the value of the variable to the implicit cache if available") {
        import org.scaladebugger.api.dsl.Implicits.VariableInfoDSL

        (mockObjectInfoProfile.uniqueId _).expects()
          .returning(TestUniqueId).twice()

        (mockVariableInfoProfile.toValueInfo _).expects()
          .returning(mockObjectInfoProfile).once()

        (mockObjectInfoProfile.isObject _).expects().returning(true).once()
        (mockObjectInfoProfile.toObjectInfo _).expects()
          .returning(mockObjectInfoProfile).once()

        implicit val objectCache: ObjectCache = testObjectCache
        objectCache.save(mockObjectInfoProfile)

        mockVariableInfoProfile.uncache()

        objectCache.has(TestUniqueId) should be (false)
      }

      it("should remove the value of the variable to underlying JVM's cache if no implicit available") {
        import org.scaladebugger.api.dsl.Implicits.VariableInfoDSL

        (mockVariableInfoProfile.scalaVirtualMachine _).expects()
          .returning(testScalaVirtualMachine).once()

        (mockObjectInfoProfile.uniqueId _).expects()
          .returning(TestUniqueId).twice()

        (mockVariableInfoProfile.toValueInfo _).expects()
          .returning(mockObjectInfoProfile).once()

        (mockObjectInfoProfile.isObject _).expects().returning(true).once()
        (mockObjectInfoProfile.toObjectInfo _).expects()
          .returning(mockObjectInfoProfile).once()

        testObjectCache.save(mockObjectInfoProfile)
        mockVariableInfoProfile.uncache()

        testObjectCache.has(TestUniqueId) should be (false)
      }
    }
  }
}
