package org.senkbeil.debugger.api.lowlevel.methods

import org.senkbeil.debugger.api.lowlevel.RequestInfo
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a method entry request.
 *
 * @param requestId The id of the request
 * @param className The full name of the class containing the method
 * @param methodName The name of the method
 * @param extraArguments The additional arguments provided to the
 *                       method entry request
 */
case class MethodEntryRequestInfo(
  requestId: String,
  className: String,
  methodName: String,
  extraArguments: Seq[JDIRequestArgument] = Nil
) extends RequestInfo

