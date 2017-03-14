package com.krypc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.sdk.shim.ChaincodeBase;
import org.hyperledger.fabric.sdk.shim.ChaincodeStub;
import org.hyperledger.protos.TableProto;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.Gson;


public class PatientRecord extends ChaincodeBase {
	
    public static final String table_record = "table_record_";
    public static final String table_verify_record = "table_verify_record_";
  
    enum VerifyStatus{
    	APPROVE,
    	INITIALIZED,
    	INITIALIZATION_FAILED,
    	PENDING,
    	OPEN,
    	RESTRICTED,
    	REJECT
    }
    
    private static Log log = LogFactory.getLog(PatientRecord.class);
    
    
	@Override
	public String getChaincodeID() {
		return "PatientRecord";
	}
	@Override
	public String query(ChaincodeStub stub, String function, String[] args) {
		log.debug("In query, function:"+function);
		log.info("In query, function:"+function);
		
		ResponseWrapper response = new ResponseWrapper();
		switch(function){
		case "getverificationstatuslist":
			response = getverificationstatuslist(stub, function, args);
			break;
		case "getrecordslist":
			response = getrecordslist(stub, function, args);
			break;
		case "getpatientrecordrequest":
			response = getpatientrecordrequest(stub, function, args);
			break;
		default:
			response.errorCode = 1000023;
			response.errorMessage = "Invalid Method Name.";
			break;
		}
		log.info("In run, function:"+function+"---response :"+response);
		return (new Gson().toJson(response));
	}
	@Override
	public String run(ChaincodeStub stub, String function, String[] args) {
		log.debug("In run, function:"+function);
		log.info("In run, function:"+function);
		
		String response = null;
		switch(function){
		case "init":
			response = init(stub, function, args);
			break;
		case "addrecord":
			response = addrecord(stub, function, args);
			break;
		case "requestrecord":
			response = requestrecord(stub, function, args);
			break;
		case "patientapproval":
			response = patientapproval(stub, function, args);
			break;
		default:
			response = "Invalid Function Name.";
			break;
		}
		log.info("In run, function:"+function+"---response :"+response);
		return response;
	}
	
	public String init(ChaincodeStub stub, String function, String[] args){
		log.info("Init generating tables");
		List<TableProto.ColumnDefinition> cols = new ArrayList<TableProto.ColumnDefinition>();

        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("RecordHash")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );

        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("LabId")
                .setKey(false)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("ReportId")
                .setKey(false)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );

        List<TableProto.ColumnDefinition> colsv = new ArrayList<TableProto.ColumnDefinition>();

        colsv.add(TableProto.ColumnDefinition.newBuilder()
                .setName("PatientSSN")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        
        colsv.add(TableProto.ColumnDefinition.newBuilder()
                .setName("hash")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        
        colsv.add(TableProto.ColumnDefinition.newBuilder()
                .setName("DoctorId")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        
        colsv.add(TableProto.ColumnDefinition.newBuilder()
                .setName("ReportId")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        
        colsv.add(TableProto.ColumnDefinition.newBuilder()
                .setName("LabId")
                .setKey(false)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        
        colsv.add(TableProto.ColumnDefinition.newBuilder()
                .setName("Status")
                .setKey(false)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );
        
        colsv.add(TableProto.ColumnDefinition.newBuilder()
                .setName("PaymentStatus")
                .setKey(false)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );

        try {
            try {
                stub.deleteTable(table_record);
                stub.deleteTable(table_verify_record);
            } catch (Exception e) {
                e.printStackTrace();
                return VerifyStatus.INITIALIZATION_FAILED.name();
            }
            stub.createTable(table_record,cols);
            stub.createTable(table_verify_record,colsv);
        } catch (Exception e) {
            e.printStackTrace();
            return VerifyStatus.INITIALIZATION_FAILED.name();
        }
        return VerifyStatus.INITIALIZED.name();
	}
	
	public String addrecord(ChaincodeStub stub, String function, String[] args){
		log.info("In addrecord, args:"+args[0]+"---"+args[1]+"---"+args[2]);
		String response = null;
		try {
			if(args.length<3){
				response = "Invalid Argument Numbers. Required (3) Hash,LabId,ReportId";
				throwErrorException();
			}
			List<TableProto.Column> cols = new ArrayList<TableProto.Column>();
			TableProto.Column col1 = TableProto.Column.newBuilder().setString(args[0]).build();
	        cols.add(col1);
	        TableProto.Row row = stub.getRow(table_record,cols);
	        log.info("Row Info in add record :"+row);
	        if (row.getSerializedSize() > 0) {
	        	response = "Insertion Failed.Duplicate Record.";
	        	throwErrorException();
	        }
	        TableProto.Column col2 = TableProto.Column.newBuilder().setString(args[1].trim()).build();
	        TableProto.Column col3 = TableProto.Column.newBuilder().setString(args[2].trim()).build();
	        cols.add(col2);
	        cols.add(col3);
	        TableProto.Row rrow = TableProto.Row.newBuilder().addAllColumns(cols).build();
	        boolean success = false;
	        try {
				success = stub.insertRow(table_record, rrow);
			} catch (Exception e) {
				response = e.toString();
				throwErrorException();
			}
	        if(success){
	        	response = "Record Successfully Added";
	        }else{
	        	response = "Failed to insert record.";
	        }
	        log.info("Response"+response);
		} catch (Exception e) {
			response = e.toString();
			throwErrorException();
		}
		return response;
	}
	
	public String requestrecord(ChaincodeStub stub, String function, String[] args){
		log.info("In requestrecord, args:"+args[0]+"---"+args[1]+"---"+args[2]);
		String response = null;
		try {
			if(args.length<3){
				response = "Invalid Argument Numbers. Required (3) Hash,DoctorId,PatientSSN";
				throwErrorException();
			}
			List<TableProto.Column> colss = new ArrayList<TableProto.Column>();
			TableProto.Column col1 = TableProto.Column.newBuilder().setString(args[0]).build();
	        colss.add(col1);
	        TableProto.Row row = stub.getRow(table_record,colss);
	        log.info("Row Info in add record :"+row);
	        if (row.getSerializedSize() > 0) {
	        	List<TableProto.Column> colvs = new ArrayList<TableProto.Column>();
	        	TableProto.Column colp = TableProto.Column.newBuilder().setString(args[2]).build();
	        	TableProto.Column colh = TableProto.Column.newBuilder().setString(args[0]).build();
	        	TableProto.Column cold = TableProto.Column.newBuilder().setString(args[1]).build();
	        	TableProto.Column colr = TableProto.Column.newBuilder().setString(row.getColumns(2).getString()).build();
	        	TableProto.Column coll = TableProto.Column.newBuilder().setString(row.getColumns(1).getString()).build();
	        	TableProto.Column cols = TableProto.Column.newBuilder().setString(VerifyStatus.PENDING.toString()).build();
	        	TableProto.Column colps = TableProto.Column.newBuilder().setString(VerifyStatus.PENDING.toString()).build();
	        	colvs.add(colp);
	        	colvs.add(colh);
	        	colvs.add(cold);
	        	colvs.add(colr);
	        	colvs.add(coll);
	        	colvs.add(cols);
	        	colvs.add(colps);
	        	TableProto.Row rrow = TableProto.Row.newBuilder().addAllColumns(colvs).build();
	 	        boolean success = false;
	 	        try {
	 				success = stub.insertRow(table_verify_record, rrow);
	 			} catch (Exception e) {
	 				response = e.toString();
	 				log.info("Response"+response);
	 				throwErrorException();
	 			}
	 	        if(success){
	 	        	response = "Record Successfully Added In Verify Table.";
	 	        }else{
	 	        	throwErrorException();
	 	        }
	        }else{
	        	throwErrorException();
	        }
	        log.info("Response"+response);
		} catch (Exception e) {
			response = e.toString();
			log.info("Response"+response);
			throwErrorException();
		}
		return response;
	}
	
	public String patientapproval(ChaincodeStub stub, String function, String[] args){
		log.info("In patientapproval, args:"+args[0]+"---"+args[1]+"---"+args[2]+"---"+args[3]+"---"+args[4]+"---"+args[5]);
		String response = null;
		try {
			if(args.length<6){
				response = "Invalid Argument Numbers. Required (4) PatientSSN,Hash,DoctorId,RecordID,isApproved,isPaid";
				throwErrorException();
			}
			List<TableProto.Column> colss = new ArrayList<TableProto.Column>();
			TableProto.Column col1 = TableProto.Column.newBuilder().setString(args[0]).build();
			TableProto.Column col2 = TableProto.Column.newBuilder().setString(args[1]).build();
			TableProto.Column col3 = TableProto.Column.newBuilder().setString(args[2]).build();
			TableProto.Column col4 = TableProto.Column.newBuilder().setString(args[3]).build();
	        colss.add(col1);
	        colss.add(col2);
	        colss.add(col3);
	        colss.add(col4);
	        log.info("getting row using values : "+colss.get(0)+"---"+colss.get(1)+"---"+colss.get(2)+"---"+colss.get(3));
	        TableProto.Row row = stub.getRow(table_verify_record,colss);
	        log.info("Row Info in add record :"+row);
	        if (row.getSerializedSize() > 0) {
	        	List<TableProto.Column> colvs = new ArrayList<TableProto.Column>();
	        	TableProto.Column colp = TableProto.Column.newBuilder().setString(row.getColumns(0).getString()).build();
	        	TableProto.Column colh = TableProto.Column.newBuilder().setString(row.getColumns(1).getString()).build();
	        	TableProto.Column cold = TableProto.Column.newBuilder().setString(row.getColumns(2).getString()).build();
	        	TableProto.Column colr = TableProto.Column.newBuilder().setString(row.getColumns(3).getString()).build();
	        	TableProto.Column coll = TableProto.Column.newBuilder().setString(row.getColumns(4).getString()).build();
	        	colvs.add(colp);
	        	colvs.add(colh);
	        	colvs.add(cold);
	        	colvs.add(colr);
	        	colvs.add(coll);
	        	switch(args[4]){
	        	case "0":
	        		TableProto.Column cols0 = TableProto.Column.newBuilder().setString(VerifyStatus.PENDING.toString()).build();
	        		colvs.add(cols0);
	        		break;
	        	case "1":
	        		TableProto.Column cols1 = TableProto.Column.newBuilder().setString(VerifyStatus.OPEN.toString()).build();
	        		colvs.add(cols1);
	        		break;
	        	case "2":
	        		TableProto.Column cols2 = TableProto.Column.newBuilder().setString(VerifyStatus.RESTRICTED.toString()).build();
	        		colvs.add(cols2);
	        		break;
	        	default :
	        		throwErrorException();
	        	}
	        	switch(args[5]){
	        	case "0":
	        		TableProto.Column colps0 = TableProto.Column.newBuilder().setString(VerifyStatus.PENDING.toString()).build();
	        		colvs.add(colps0);
	        		break;
	        	case "1":
	        		TableProto.Column colps1 = TableProto.Column.newBuilder().setString(VerifyStatus.APPROVE.toString()).build();
	        		colvs.add(colps1);
	        		break;
	        	case "2":
	        		TableProto.Column colps2 = TableProto.Column.newBuilder().setString(VerifyStatus.REJECT.toString()).build();
	        		colvs.add(colps2);
	        		break;
	        	default :
	        		throwErrorException();
	        	}
	        	TableProto.Row rrow = TableProto.Row.newBuilder().addAllColumns(colvs).build();
	 	        boolean success = false;
	 	        try {
	 				success = stub.replaceRow(table_verify_record, rrow);
	 			} catch (Exception e) {
	 				response = e.toString();
	 				throwErrorException();
	 			}
	 	        if(success){
	 	        	response = "Record Successfully Updated In Verify Table.";
	 	        }else{
	 	        	throwErrorException();
	 	        }
	        }else{
	        	throwErrorException();
	        }
	        log.info("Response"+response);
		} catch (Exception e) {
			response = e.toString();
			throwErrorException();
		}
		return response;
	}
	
	@SuppressWarnings("unchecked")
	public ResponseWrapper getverificationstatuslist(ChaincodeStub stub, String function, String[] args){
		ResponseWrapper response = new ResponseWrapper();
		try {
			if(args.length!=0){
				response.errorCode=100067;
				response.errorMessage = "Invalid Argument Numbers. Required (0).";
			}
			List<TableProto.Column> key = new ArrayList<>();
			ArrayList<TableProto.Row> records = stub.getRows(table_verify_record,key);
			JSONArray rarray = new JSONArray();
			for(TableProto.Row record:records){
				JSONObject obj = new JSONObject();
				obj.put("PatientSSN", record.getColumns(0).getString());
				obj.put("Hash", record.getColumns(1).getString());
				obj.put("DoctorId", record.getColumns(2).getString());
				obj.put("ReportId", record.getColumns(3).getString());
				obj.put("LabId", record.getColumns(4).getString());
				obj.put("ApproveReport", record.getColumns(5).getString());
				obj.put("PaymentStatus", record.getColumns(6).getString());
				rarray.add(obj);
			}
			response.resultObject = rarray;
			log.info(response);
		} catch (Exception e) {
			response.errorCode = 100087;
			response.errorMessage = e.toString();
		}
		return response;
	}
	
	@SuppressWarnings("unchecked")
	public ResponseWrapper getrecordslist(ChaincodeStub stub, String function, String[] args){
		ResponseWrapper response = new ResponseWrapper();
		try {
			if(args.length!=0){
				response.errorCode=100067;
				response.errorMessage = "Invalid Argument Numbers. Required (0).";
			}
			List<TableProto.Column> key = new ArrayList<>();
			ArrayList<TableProto.Row> records = stub.getRows(table_record,key);
			JSONArray rarray = new JSONArray();
			for(TableProto.Row record:records){
				JSONObject obj = new JSONObject();
				obj.put("Hash", record.getColumns(0).getString());
				obj.put("LabId", record.getColumns(1).getString());
				obj.put("ReportId", record.getColumns(2).getString());
				rarray.add(obj);
			}
			response.resultObject = rarray;
			log.info(response);
		} catch (Exception e) {
			response.errorCode = 100087;
			response.errorMessage = e.toString();
		}
		return response;
	}
	
	@SuppressWarnings("unchecked")
	public ResponseWrapper getpatientrecordrequest(ChaincodeStub stub, String function, String[] args){
		log.info("In patientapproval, args:"+args[0]);
		ResponseWrapper response = new ResponseWrapper();
		try {
			if(args.length<1){
				response.errorCode=100067;
				response.errorMessage = "Invalid Argument Numbers. Required (1). PatientSSN";
			}
			List<TableProto.Column> key = new ArrayList<>();
			TableProto.Column col1 = TableProto.Column.newBuilder().setString(args[0]).build();
			key.add(col1);
			ArrayList<TableProto.Row> records = stub.getRows(table_verify_record,key);
			JSONArray rarray = new JSONArray();
			for(TableProto.Row record:records){
				JSONObject obj = new JSONObject();
				obj.put("PatientSSN", record.getColumns(0).getString());
				obj.put("Hash", record.getColumns(1).getString());
				obj.put("DoctorId", record.getColumns(2).getString());
				obj.put("ReportId", record.getColumns(3).getString());
				obj.put("LabId", record.getColumns(4).getString());
				obj.put("ApproveReport", record.getColumns(5).getString());
				obj.put("PaymentStatus", record.getColumns(6).getString());
				rarray.add(obj);
			}
			response.resultObject = rarray;
			log.info(response);
		} catch (Exception e) {
			response.errorCode = 100087;
			response.errorMessage = e.toString();
		}
		return response;
	}
	
	public void throwErrorException(){
		System.out.println(1/0);
	}
	
	public static void main(String[] args) {
		//172.17.0.3
//		args = new String[]{"--peerAddress=127.0.0.1:7051","--securityEnabled"};
//		args = new String[]{"--peerAddress=127.0.0.1:7051"};
		PatientRecord record = new PatientRecord();
		record.start(args);
	}
}
