package com.krypc.hl.pr.controller;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.bouncycastle.jcajce.provider.digest.SHA3.DigestSHA3;
import org.bouncycastle.util.encoders.Hex;
import org.hyperledger.fabric.sdk.ChainCodeResponse;
import org.hyperledger.fabric.sdk.ChainCodeResponse.Status;
import org.hyperledger.fabric.sdk.ChaincodeLanguage;
import org.hyperledger.fabric.sdk.DeployRequest;
import org.hyperledger.fabric.sdk.InvokeRequest;
import org.hyperledger.fabric.sdk.Member;
import org.hyperledger.fabric.sdk.QueryRequest;
import org.hyperledger.fabric.sdk.RegistrationRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.krypc.api.constants.Constants;
import com.krypc.api.constants.ErrorConstants.ERROR_CODES;
import com.krypc.api.responsewrappers.ResponseWrapper;
import com.krypc.hl.pr.bslayerapis.PeerMembershipServicesAPI;
import com.krypc.hl.pr.utils.GeneralUtils;

@Component
@RestController
public class PatientRecordController {

	Logger logger = Logger.getLogger(PatientRecordController.class);
	
	PeerMembershipServicesAPI peerMembershipServicesAPI = PeerMembershipServicesAPI.getInstance();
	
	@Autowired
	GeneralUtils utils;
	
	@RequestMapping(value = "/deployChaincode", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseWrapper deployChaincode(HttpServletRequest request,HttpServletResponse response){
		logger.info("PatientRecordController---deployChaincode()--STARTS");
		ResponseWrapper wrapper = new ResponseWrapper();
		String data = request.getParameter("data");
		try {
			if (data != null && !data.isEmpty()) {
				JSONObject deploymentData = (JSONObject) new JSONParser().parse(data);
				String path = ((String) deploymentData.get("path"));
				String user = ((String) deploymentData.get("user"));
				if (!StringUtils.isEmpty(path) && !StringUtils.isEmpty(user)) {
					DeployRequest deployrequest = new DeployRequest();
					deployrequest.setChaincodePath(path);//comment for devmode : uncomment for production
					//deployrequest.setChaincodePath("");//uncomment for devmode : comment for production
					deployrequest.setArgs(new ArrayList<>(Arrays.asList("init")));
					Member member = peerMembershipServicesAPI.getChain().getMember(user);
					if(!member.isEnrolled()){
						RegistrationRequest registrationRequest = new RegistrationRequest();
						registrationRequest.setEnrollmentID(user);
						registrationRequest.setAffiliation("bank_a");
						member = peerMembershipServicesAPI.getChain().registerAndEnroll(registrationRequest);
					}
					deployrequest.setChaincodeName("PatientRecord");//uncomment for devmode : comment for production
					//deployrequest.setChaincodeName("");//comment for devmode : uncomment for production
					deployrequest.setChaincodeLanguage(ChaincodeLanguage.JAVA);
					deployrequest.setConfidential(false);
					ChainCodeResponse chaincoderesponse = member.deploy(deployrequest);
					utils.storeChaincodeName(chaincoderesponse);
					wrapper.resultObject = chaincoderesponse;
				}else {
					wrapper.setError(ERROR_CODES.MANDATORY_FIELDS_MISSING);
				}
			}else {
				wrapper.setError(ERROR_CODES.MANDATORY_FIELDS_MISSING);
			}
		} catch (Exception e) {
			wrapper.setError(ERROR_CODES.INTERNAL_ERROR);
			logger.error("PatientRecordController---deployChaincode()--ERROR " + e);
		}
		logger.info("PatientRecordController---deployChaincode()--ENDS");
		return wrapper;
	}
	
	@RequestMapping(value = "/generateHash", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseWrapper generateHash(HttpServletRequest request,HttpServletResponse response){
		logger.info("PatientRecordController---generateHash()--STARTS");
		ResponseWrapper wrapper = new ResponseWrapper();
		String data = request.getParameter("data");
		try {
			if(utils.verifychaincode()){
				if (data != null && !data.isEmpty()) {
					JSONObject hashData = (JSONObject) new JSONParser().parse(data);
					String patientID = ((String) hashData.get("patientSSN")).toUpperCase().trim();
					String lastName = ((String) hashData.get("lastName")).toUpperCase().trim();
					String dob = ((String) hashData.get("dateOfBirth")).trim();
					String dov = ((String) hashData.get("dateOfVisit")).trim();
					String docLicense = ((String) hashData.get("doctorLicense")).trim();
					if (!StringUtils.isEmpty(patientID) && !StringUtils.isEmpty(lastName) && !StringUtils.isEmpty(dob)&& !StringUtils.isEmpty(dov)&& !StringUtils.isEmpty(docLicense)) {
						String hashdata = patientID.concat(lastName).concat(dob).concat(dov).concat(docLicense);
						MessageDigest digest = DigestSHA3.getInstance("SHA-256");
						digest.update(hashdata.getBytes());
						wrapper.resultObject = Hex.toHexString(digest.digest());
					}else {
						wrapper.setError(ERROR_CODES.MANDATORY_FIELDS_MISSING);
					}
				}else {
					wrapper.setError(ERROR_CODES.MANDATORY_FIELDS_MISSING);
				}
			}else{
				wrapper.setError(ERROR_CODES.CHAINCODE_NOT_DEPLOYED);
			}
		} catch (Exception e) {
			wrapper.setError(ERROR_CODES.INTERNAL_ERROR);
			logger.error("PatientRecordController---generateHash()--ERROR " + e);
		}
		logger.info("PatientRecordController---generateHash()--ENDS");
		return wrapper;
	}
	
	@RequestMapping(value = "/createRecord", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseWrapper createRecord(HttpServletRequest request,HttpServletResponse response){
		logger.info("PatientRecordController---invokeChaincode()--STARTS");
		ResponseWrapper wrapper = new ResponseWrapper();
		String data = request.getParameter("data");
		try {
            if(utils.verifychaincode()){
            	if (data != null && !data.isEmpty()) {
    				JSONObject recordData = (JSONObject) new JSONParser().parse(data);
    				String user = ((String) recordData.get("user")).trim();
    				String hash = ((String) recordData.get("hash")).trim();
    				String labId = "LAB0000001";
    				String reportId = ((String) recordData.get("reportId")).trim();
    				if (!StringUtils.isEmpty(hash) && !StringUtils.isEmpty(labId) && !StringUtils.isEmpty(reportId)  && !StringUtils.isEmpty(user)) {
    					String[] arugs = new String[]{hash,labId,reportId};
    					InvokeRequest invokerequest = new InvokeRequest();
    					ArrayList<String> argss = new ArrayList<>(Arrays.asList(Constants.MethodName.addrecord.toString()));
    					argss.addAll(Arrays.asList(arugs));
    					invokerequest.setArgs(argss);
    					invokerequest.setChaincodeID(utils.getChaincodeName().getChainCodeID());
    					invokerequest.setChaincodeName(utils.getChaincodeName().getTransactionID());
    					invokerequest.setChaincodeLanguage(ChaincodeLanguage.JAVA);
    					Member member = peerMembershipServicesAPI.getChain().getMember(user);
    					wrapper.resultObject = member.invoke(invokerequest);
    				}else {
    					wrapper.setError(ERROR_CODES.MANDATORY_FIELDS_MISSING);
    				}
    			}else {
    				wrapper.setError(ERROR_CODES.MANDATORY_FIELDS_MISSING);
    			}
			}else{
				wrapper.setError(ERROR_CODES.CHAINCODE_NOT_DEPLOYED);
			}
		} catch (Exception e) {
			wrapper.setError(ERROR_CODES.DUPLICATE_HASH);
			logger.error("PatientRecordController---invokeChaincode()--ERROR " + e);
		}
		logger.info("PatientRecordController---invokeChaincode()--ENDS");
		return wrapper;
	}
	
	@RequestMapping(value = "/requestRecord", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseWrapper requestRecord(HttpServletRequest request,HttpServletResponse response){
		logger.info("PatientRecordController---requestRecord()--STARTS");
		ResponseWrapper wrapper = new ResponseWrapper();
		String data = request.getParameter("data");
		try {
            if(utils.verifychaincode()){
            	if (data != null && !data.isEmpty()) {
    				JSONObject recordData = (JSONObject) new JSONParser().parse(data);
    				String user = ((String) recordData.get("user")).trim();
    				String hash = ((String) recordData.get("hash")).trim();
    				String docId = ((String) recordData.get("doctorId")).trim();
    				String patientSSN = ((String) recordData.get("patientSSN")).trim();
    				if (!StringUtils.isEmpty(hash) && !StringUtils.isEmpty(docId) && !StringUtils.isEmpty(patientSSN) && !StringUtils.isEmpty(user)) {
    					String[] arugs = new String[]{hash,docId,patientSSN};
    					InvokeRequest invokerequest = new InvokeRequest();
    					ArrayList<String> argss = new ArrayList<>(Arrays.asList(Constants.MethodName.requestrecord.toString()));
    					argss.addAll(Arrays.asList(arugs));
    					invokerequest.setArgs(argss);
    					invokerequest.setChaincodeID(utils.getChaincodeName().getChainCodeID());
    					invokerequest.setChaincodeName(utils.getChaincodeName().getTransactionID());
    					invokerequest.setChaincodeLanguage(ChaincodeLanguage.JAVA);
    					Member member = peerMembershipServicesAPI.getChain().getMember(user);
    					wrapper.resultObject = member.invoke(invokerequest);
    				}else {
    					wrapper.setError(ERROR_CODES.MANDATORY_FIELDS_MISSING);
    				}
    			}else {
    				wrapper.setError(ERROR_CODES.MANDATORY_FIELDS_MISSING);
    			}
			}else{
				wrapper.setError(ERROR_CODES.CHAINCODE_NOT_DEPLOYED);
			}
		} catch (Exception e) {
			wrapper.setError(ERROR_CODES.RECORD_HASH_NOT_AVAILABLE);
			logger.error("PatientRecordController---requestRecord()--ERROR " + e);
		}
		logger.info("PatientRecordController---requestRecord()--ENDS");
		return wrapper;
	}
	
	@RequestMapping(value = "/approveRecordRequest", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseWrapper approveRecordRequest(HttpServletRequest request,HttpServletResponse response){
		logger.info("PatientRecordController---approveRecordRequest()--STARTS");
		ResponseWrapper wrapper = new ResponseWrapper();
		String data = request.getParameter("data");
		try {
            if(utils.verifychaincode()){
            	if (data != null && !data.isEmpty()) {
    				JSONObject recordData = (JSONObject) new JSONParser().parse(data);
    				String user = ((String) recordData.get("user")).trim();
    				String patientSSN = ((String) recordData.get("patientSSN")).trim();
    				String hash = ((String) recordData.get("hash")).trim();
    				String docId = ((String) recordData.get("doctorId")).trim();
    				String recordId = ((String) recordData.get("recordId")).trim();
    				String isApproved = ((String) recordData.get("isApproved")).trim();
    				String isPaid = ((String) recordData.get("isPaid")).trim();
    				if (!StringUtils.isEmpty(isPaid) && !StringUtils.isEmpty(recordId) && !StringUtils.isEmpty(hash) && !StringUtils.isEmpty(docId) && !StringUtils.isEmpty(patientSSN) && !StringUtils.isEmpty(isApproved) && !StringUtils.isEmpty(user)) {
    					if((isApproved.equals("0")) || (isApproved.equals("1")) || (isApproved.equals("2"))){
    						if((isPaid.equals("0")) || (isPaid.equals("1")) || (isPaid.equals("2"))){
    							String[] arugs = new String[]{patientSSN,hash,docId,recordId,isApproved,isPaid};
    							InvokeRequest invokerequest = new InvokeRequest();
    							ArrayList<String> argss = new ArrayList<>(Arrays.asList(Constants.MethodName.patientapproval.toString()));
    							argss.addAll(Arrays.asList(arugs));
    							invokerequest.setArgs(argss);
    							invokerequest.setChaincodeID(utils.getChaincodeName().getChainCodeID());
    							invokerequest.setChaincodeName(utils.getChaincodeName().getTransactionID());
    							invokerequest.setChaincodeLanguage(ChaincodeLanguage.JAVA);
    							Member member = peerMembershipServicesAPI.getChain().getMember(user);
    							wrapper.resultObject = member.invoke(invokerequest);
    						}else{
    							wrapper.setError(ERROR_CODES.INVALID_PAYMENT_STATUS_PARAMETER);
    						}
    					}else{
    						wrapper.setError(ERROR_CODES.INVALID_RECORD_ACCESS_PARAMETER);
    					}
    				}else {
    					wrapper.setError(ERROR_CODES.MANDATORY_FIELDS_MISSING);
    				}
    			}else {
    				wrapper.setError(ERROR_CODES.MANDATORY_FIELDS_MISSING);
    			}
			}else{
				wrapper.setError(ERROR_CODES.CHAINCODE_NOT_DEPLOYED);
			}
		} catch (Exception e) {
			wrapper.setError(ERROR_CODES.NO_RECORDS_FOUND_FOR_PROVIDED_PARAMETERS);
			logger.error("PatientRecordController---approveRecordRequest()--ERROR " + e);
		}
		logger.info("PatientRecordController---approveRecordRequest()--ENDS");
		return wrapper;
	}
	
	@RequestMapping(value = "/getPatientRecordRequestList", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseWrapper getPatientRecordRequestList(HttpServletRequest request,HttpServletResponse response){
		logger.info("PatientRecordController---getPatientRecordRequestList()--STARTS");
		ResponseWrapper wrapper = new ResponseWrapper();
		String data = request.getParameter("data");
		try {
            if(utils.verifychaincode()){
            	if (data != null && !data.isEmpty()) {
    				JSONObject queryData = (JSONObject) new JSONParser().parse(data);
    				String patientSSN = ((String) queryData.get("patientSSN"));
    				String user = ((String) queryData.get("user"));
    				if (!StringUtils.isEmpty(patientSSN) && !StringUtils.isEmpty(user)) {
    					ArrayList<String> argss = new ArrayList<>(Arrays.asList(Constants.MethodName.getpatientrecordrequest.toString()));
    					argss.addAll(Arrays.asList(patientSSN));
    					QueryRequest queryrequest = new QueryRequest();
    					queryrequest.setArgs(argss);
    					queryrequest.setChaincodeID(utils.getChaincodeName().getChainCodeID());
    					queryrequest.setChaincodeName(utils.getChaincodeName().getTransactionID());
    					queryrequest.setChaincodeLanguage(ChaincodeLanguage.JAVA);
    					Member member = peerMembershipServicesAPI.getChain().getMember(user);
    					wrapper.resultObject = member.query(queryrequest);
    					wrapper = convertQueryResponse(wrapper);
    				}else {
    					wrapper.setError(ERROR_CODES.MANDATORY_FIELDS_MISSING);
    				}
    			}else {
    				wrapper.setError(ERROR_CODES.MANDATORY_FIELDS_MISSING);
    			}
			}else{
				wrapper.setError(ERROR_CODES.CHAINCODE_NOT_DEPLOYED);
			}
		} catch (Exception e) {
			wrapper.setError(ERROR_CODES.INTERNAL_ERROR);
			logger.error("PatientRecordController---getPatientRecordRequestList()--ERROR " + e);
		}
		logger.info("PatientRecordController---getPatientRecordRequestList()--ENDS");
		return wrapper;
	}
	
	@RequestMapping(value = "/getRecordRequestList", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseWrapper getRecordRequestList(HttpServletRequest request,HttpServletResponse response){
		logger.info("PatientRecordController---getRecordRequestList()--STARTS");
		ResponseWrapper wrapper = new ResponseWrapper();
		String data = request.getParameter("data");
		try {
            if(utils.verifychaincode()){
            	if (data != null && !data.isEmpty()) {
    				JSONObject queryData = (JSONObject) new JSONParser().parse(data);
    				String user = ((String) queryData.get("user"));
    				if (!StringUtils.isEmpty(user)) {
    					ArrayList<String> argss = new ArrayList<>(Arrays.asList(Constants.MethodName.getverificationstatuslist.toString()));
    					QueryRequest queryrequest = new QueryRequest();
    					queryrequest.setArgs(argss);
    					queryrequest.setChaincodeID(utils.getChaincodeName().getChainCodeID());
    					queryrequest.setChaincodeName(utils.getChaincodeName().getTransactionID());
    					queryrequest.setChaincodeLanguage(ChaincodeLanguage.JAVA);
    					Member member = peerMembershipServicesAPI.getChain().getMember(user);
    					wrapper.resultObject = member.query(queryrequest);
    					wrapper = convertQueryResponse(wrapper);
    				}else {
    					wrapper.setError(ERROR_CODES.MANDATORY_FIELDS_MISSING);
    				}
    			}else {
    				wrapper.setError(ERROR_CODES.MANDATORY_FIELDS_MISSING);
    			}
			}else{
				wrapper.setError(ERROR_CODES.CHAINCODE_NOT_DEPLOYED);
			}
		} catch (Exception e) {
			wrapper.setError(ERROR_CODES.INTERNAL_ERROR);
			logger.error("PatientRecordController---getRecordRequestList()--ERROR " + e);
		}
		logger.info("PatientRecordController---getRecordRequestList()--ENDS");
		return wrapper;
	}
	
	@RequestMapping(value = "/invokeChaincode", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseWrapper invokeChaincode(HttpServletRequest request,HttpServletResponse response){
		logger.info("PatientRecordController---invokeChaincode()--STARTS");
		ResponseWrapper wrapper = new ResponseWrapper();
		String data = request.getParameter("data");
		try {
            if(utils.verifychaincode()){
            	if (data != null && !data.isEmpty()) {
    				JSONObject invokeData = (JSONObject) new JSONParser().parse(data);
    				String functionName = ((String) invokeData.get("functionName"));
    				String user = ((String) invokeData.get("user"));
    				JSONArray args = ((JSONArray) invokeData.get("args"));
                    String[] arr = Arrays.copyOf(args.toArray(), args.toArray().length, String[].class);
    				if (!StringUtils.isEmpty(functionName) && !StringUtils.isEmpty(user)) {
    					InvokeRequest invokerequest = new InvokeRequest();
    					ArrayList<String> argss = new ArrayList<>(Arrays.asList(functionName));
    					argss.addAll(Arrays.asList(arr));
    					invokerequest.setArgs(argss);
    					invokerequest.setChaincodeID(utils.getChaincodeName().getChainCodeID());
    					invokerequest.setChaincodeName(utils.getChaincodeName().getTransactionID());
    					invokerequest.setChaincodeLanguage(ChaincodeLanguage.JAVA);
    					Member member = peerMembershipServicesAPI.getChain().getMember(user);
    					wrapper.resultObject = member.invoke(invokerequest);
    				}else {
    					wrapper.setError(ERROR_CODES.MANDATORY_FIELDS_MISSING);
    				}
				}else {
					wrapper.setError(ERROR_CODES.MANDATORY_FIELDS_MISSING);
				}
            }else{
				wrapper.setError(ERROR_CODES.CHAINCODE_NOT_DEPLOYED);
			}
		} catch (Exception e) {
			wrapper.setError(ERROR_CODES.INTERNAL_ERROR);
			logger.error("PatientRecordController---invokeChaincode()--ERROR " + e);
		}
		logger.info("PatientRecordController---invokeChaincode()--ENDS");
		return wrapper;
	}
	
	@RequestMapping(value = "/queryChaincode", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseWrapper queryChaincode(HttpServletRequest request,HttpServletResponse response){
		logger.info("PatientRecordController---queryChaincode()--STARTS");
		ResponseWrapper wrapper = new ResponseWrapper();
		String data = request.getParameter("data");
		try {
            if(utils.verifychaincode()){
            	if (data != null && !data.isEmpty()) {
    				JSONObject queryData = (JSONObject) new JSONParser().parse(data);
    				String functionName = ((String) queryData.get("functionName"));
    				String user = ((String) queryData.get("user"));
    				JSONArray args = ((JSONArray) queryData.get("args"));
    				String[] arr = Arrays.copyOf(args.toArray(), args.toArray().length, String[].class);
    				if (!StringUtils.isEmpty(functionName) && !StringUtils.isEmpty(user)) {
    					ArrayList<String> argss = new ArrayList<>(Arrays.asList(functionName));
    					argss.addAll(Arrays.asList(arr));
    					QueryRequest queryrequest = new QueryRequest();
    					queryrequest.setArgs(argss);
    					queryrequest.setChaincodeID(utils.getChaincodeName().getChainCodeID());
    					queryrequest.setChaincodeName(utils.getChaincodeName().getTransactionID());
    					queryrequest.setChaincodeLanguage(ChaincodeLanguage.JAVA);
    					Member member = peerMembershipServicesAPI.getChain().getMember(user);
    					wrapper.resultObject = member.query(queryrequest);
    					wrapper = convertQueryResponse(wrapper);
    				}else {
    					wrapper.setError(ERROR_CODES.MANDATORY_FIELDS_MISSING);
    				}
    			}else {
    				wrapper.setError(ERROR_CODES.MANDATORY_FIELDS_MISSING);
    			}
			}else{
				wrapper.setError(ERROR_CODES.CHAINCODE_NOT_DEPLOYED);
			}
		} catch (Exception e) {
			wrapper.setError(ERROR_CODES.INTERNAL_ERROR);
			logger.error("PatientRecordController---queryChaincode()--ERROR " + e);
		}
		logger.info("PatientRecordController---queryChaincode()--ENDS");
		return wrapper;
	}
	public static ResponseWrapper convertQueryResponse(ResponseWrapper wrapper) throws ParseException{
		if(wrapper.resultObject instanceof ChainCodeResponse){
			ChainCodeResponse res = (ChainCodeResponse) wrapper.resultObject;
			if(res.getStatus()==Status.SUCCESS){
				String plain_json = res.getMessage().toStringUtf8();
				JSONParser parser = new JSONParser();
				JSONObject job = (JSONObject) parser.parse(plain_json);
				wrapper.resultObject = job.get("resultObject");
				wrapper.errorCode = (long) job.get("errorCode");
				wrapper.errorDetails = (String) job.get("errorDetails");
				wrapper.errorMessage = (String) job.get("errorMessage");
				return wrapper;
			}
		}
		return null;
	}
	
	
}

