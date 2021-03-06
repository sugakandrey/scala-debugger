package org.scaladebugger.api.profiles.pure.info.events

import com.sun.jdi.event.ClassPrepareEvent
import com.sun.jdi.{ReferenceType, ThreadReference, VirtualMachine}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.traits.info.events.ClassPrepareEventInfo
import org.scaladebugger.api.profiles.traits.info.{InfoProducer, ReferenceTypeInfo, ThreadInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a class prepare event info
 * profile that adds no custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            array
 * @param infoProducer The producer of info-based profile instances
 * @param classPrepareEvent The class prepare event to wrap in the profile
 * @param jdiArguments The request and event arguments tied to the provided
 *                     event
 * @param _virtualMachine The low-level virtual machine where the event
 *                        originated
 * @param _thread The thread where the event originated
 * @param _threadReferenceType The reference type of the thread where the
 *                             event originated
 * @param _referenceType The type for which the event was generated
 */
class PureClassPrepareEventInfo(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducer,
  private val classPrepareEvent: ClassPrepareEvent,
  private val jdiArguments: Seq[JDIArgument] = Nil
)(
  _virtualMachine: => VirtualMachine,
  _thread: => ThreadReference,
  _threadReferenceType: => ReferenceType,
  _referenceType: => ReferenceType
) extends PureEventInfo(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  event = classPrepareEvent,
  jdiArguments = jdiArguments
) with ClassPrepareEventInfo {
  /**
   * Returns whether or not this info profile represents the low-level Java
   * implementation.
   *
   * @return If true, this profile represents the low-level Java information,
   *         otherwise this profile represents something higher-level like
   *         Scala, Jython, or JRuby
   */
  override def isJavaInfo: Boolean = true

  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: ClassPrepareEventInfo = {
    val jep = infoProducer.eventProducer.toJavaInfo
    jep.newClassPrepareEventInfoProfile(
      scalaVirtualMachine = scalaVirtualMachine,
      classPrepareEvent = classPrepareEvent,
      jdiArguments = jdiArguments
    )(
      virtualMachine = _virtualMachine,
      thread = _thread,
      threadReferenceType = _threadReferenceType,
      referenceType = _referenceType
    )
  }

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ClassPrepareEvent = classPrepareEvent

  /**
   * Returns the reference type for which this event occurred.
   *
   * @return The information profile about the reference type
   */
  override def referenceType: ReferenceTypeInfo = {
    infoProducer.newReferenceTypeInfoProfile(
      scalaVirtualMachine = scalaVirtualMachine,
      referenceType = _referenceType
    )
  }

  /**
   * Returns the thread where this event occurred.
   *
   * @return The information profile about the thread
   */
  override def thread: ThreadInfo = infoProducer.newThreadInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    threadReference = _thread
  )(
    virtualMachine = _virtualMachine,
    referenceType = _threadReferenceType
  )
}
