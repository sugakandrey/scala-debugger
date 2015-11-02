package org.senkbeil.debugger.api.profiles.traits.info

/**
 * Represents the interface that needs to be implemented to provide
 * miscellaneous info functionality for a specific debug profile.
 */
trait MiscInfoProfile {
  /**
   * Retrieves the list of available lines for a specific file.
   *
   * @param fileName The name of the file whose lines to retrieve
   *
   * @return Some list of breakpointable lines if the file exists,
   *         otherwise None
   */
  def availableLinesForFile(fileName: String): Option[Seq[Int]]

  /**
   * Represents the name of the class used as the entrypoint for this vm.
   *
   * @return The main class name as a string
   */
  def mainClassName: String

  /**
   * Represents the command line arguments used to start this VM.
   *
   * @return The command line arguments as a collection of strings
   */
  def commandLineArguments: Seq[String]
}
