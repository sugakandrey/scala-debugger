package org.scaladebugger.api.profiles.pure.info
//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{ArrayInfoProfile, ArrayTypeInfoProfile, TypeInfoProfile, ValueInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a pure implementation of an array profile that adds no custom
 * logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            array
 * @param _arrayReference The reference to the underlying JDI array
 * @param _virtualMachine The virtual machine used to mirror local values on
 *                       the remote JVM
 * @param _threadReference The thread associated with the array (for method
 *                        invocation)
 * @param _referenceType The reference type for this array
 */
class PureArrayInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  private val _arrayReference: ArrayReference
)(
  private val _virtualMachine: VirtualMachine = _arrayReference.virtualMachine(),
  private val _threadReference: ThreadReference = _arrayReference.owningThread(),
  private val _referenceType: ReferenceType = _arrayReference.referenceType()
) extends PureObjectInfoProfile(scalaVirtualMachine, _arrayReference)(
  _virtualMachine = _virtualMachine,
  _threadReference = _threadReference,
  _referenceType = _referenceType
) with ArrayInfoProfile {
  import scala.collection.JavaConverters._
  import org.scaladebugger.api.lowlevel.wrappers.Implicits._

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ArrayReference = _arrayReference

  /**
   * Returns the type information for the array.
   *
   * @return The profile containing type information
   */
  override def typeInfo: ArrayTypeInfoProfile = super.typeInfo.toArrayType

  /**
   * Returns the length of the array.
   *
   * @return The length of the array
   */
  override def length: Int = _arrayReference.length()

  /**
   * Retrieves the value in the array at the specified index.
   *
   * @param index The location in the array to retrieve a value
   * @return The retrieved value
   */
  override def value(index: Int): ValueInfoProfile = {
    newValueProfile(_arrayReference.getValue(index))
  }

  /**
   * Sets the values of the array elements starting at the specified location.
   *
   * @param index    The location in the array to begin overwriting
   * @param values   The new values to use when overwriting elements in the array
   * @param srcIndex The location in the provided value array to begin using
   *                 values to overwrite this array
   * @param length   The total number of elements to overwrite, or -1 to overwrite
   *                 all elements in the array from the beginning of the index
   * @return The updated values
   */
  override def setValues(
    index: Int,
    values: Seq[Any],
    srcIndex: Int,
    length: Int
  ): Seq[Any] = {
    val v = values.map(_virtualMachine.mirrorOf(_: Any)).asJava
    _arrayReference.setValues(index, v, srcIndex, length)

    val sliceIndex = if (length >= 0) srcIndex + length else values.length
    values.slice(srcIndex, sliceIndex)
  }

  /**
   * Sets the values of the array elements to the provided values.
   *
   * @param values The new values to use when overwriting elements in the array
   * @return The updated values
   */
  override def setValues(values: Seq[Any]): Seq[Any] = {
    val v = values.map(_virtualMachine.mirrorOf(_: Any)).asJava
    _arrayReference.setValues(v)
    values
  }

  /**
   * Retrieves the values in the array starting from the specified index and
   * continuing through the specified length of elements.
   *
   * @param index  The location in the array to begin retrieving values
   * @param length The number of values to retrieve, or -1 to retrieve all
   *               remaining values to the end of the array
   * @return The retrieved values
   */
  override def values(
    index: Int,
    length: Int
  ): Seq[ValueInfoProfile] = {
    _arrayReference.getValues(index, length).asScala.map(newValueProfile)
  }

  /**
   * Retrieves all values from the array.
   *
   * @return The retrieved values
   */
  override def values: Seq[ValueInfoProfile] = {
    _arrayReference.getValues.asScala.map(newValueProfile)
  }

  /**
   * Sets the value of the array element at the specified location.
   *
   * @param index The location in the array whose value to overwrite
   * @param value The new value to place in the array
   * @return The updated value
   */
  override def setValue(index: Int, value: Any): Any = {
    _arrayReference.setValue(index, _virtualMachine.mirrorOf(value))
    value
  }

  override protected def newValueProfile(value: Value): ValueInfoProfile =
    new PureValueInfoProfile(scalaVirtualMachine, value)
}