package com.krypc;

import org.json.simple.JSONObject;
/**
 * All the methods in the library shall wrap the output in this class.
 * This is done to enable a structured code flow and as well compiling JSON
 * serialization and DEserialization.
 * If the error code is not zero it is an error and the error shall be reflecting
 * in the error code and more info can be found in error details. In such cases
 * the result object shall be null. If the error code is zero , there is no error
 * and the result object is the result.
 * @author mohit
 *
 */
final public class ResponseWrapper {
	
	public long errorCode = 0;
	public String errorMessage = null;
	public Object resultObject = null;
	public String errorDetails = null;
	public Long total = null;
	public String uuid;

	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		JSONObject object = new JSONObject();
		object.put("errorCode", errorCode);
		object.put("errorMessage", errorMessage);
		object.put("result", resultObject);
		object.put("errorDetails", errorDetails);
		object.put("total", total);
		return object.toJSONString();
	}
	/**
	 * SET ENUM BASED ERROR CODE
	 * @param code
	 */
	public String getErrorDetails() {
		return errorDetails;
	}
	/**
	 * Set custom error details text here
	 * @param errorDetails
	 */
	public void setErrorDetails(String errorDetails) {
		this.errorDetails = errorDetails;
	}
}
