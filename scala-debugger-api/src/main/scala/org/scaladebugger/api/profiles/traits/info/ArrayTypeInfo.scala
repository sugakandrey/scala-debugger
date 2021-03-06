package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.ArrayType

import scala.util.Try

/**
 * Represents the interface for retrieving interface type-based information.
 */
trait ArrayTypeInfo extends ReferenceTypeInfo with TypeInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: ArrayTypeInfo

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ArrayType

  /**
   * Returns the JNI signature of the common element in the array.
   *
   * @return The declared type of the elements (the runtime type may be a
   *         subclass of this type)
   */
  def elementSignature: String

  /**
   * Returns the name of the type representing the common element in the array.
   *
   * @return The type name as a string
   */
  def elementTypeName: String

  /**
   * Returns the type information for the common element in the array.
   *
   * @return The profile containing type information
   */
  def elementTypeInfo: TypeInfo

  /**
   * Returns the type information for the common element in the array.
   *
   * @return Success containing the profile containing type information,
   *         otherwise a failure
   */
  def tryElementTypeInfo: Try[TypeInfo] = Try(elementTypeInfo)

  /**
   * Creates a new instance of the array with the given length.
   *
   * @param length The total length of the array
   * @return The profile representing the new instance
   */
  def newInstance(length: Int): ArrayInfo

  /**
   * Creates a new instance of the array with the given length.
   *
   * @param length The total length of the array
   * @return Success containing the profile representing the new instance,
   *         otherwise a failure
   */
  def tryNewInstance(length: Int): Try[ArrayInfo] =
    Try(newInstance(length))
}
