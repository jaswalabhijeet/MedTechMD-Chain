package com.krypc.hl.pr.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.hyperledger.fabric.sdk.ChainCodeResponse;
import org.springframework.stereotype.Component;

import com.krypc.hl.pr.protoapis.HyperledgerStaticProperty;

@Component
public class GeneralUtils {

	private Properties prop = new Properties();
	public ChainCodeResponse chainCodeRes;
	private GeneralUtils(){
    	init();
    }
	public void init(){
		String chaincodename = null;
		String chaincodeid = null;
		InputStream input = null;
		try {
			input = new FileInputStream(HyperledgerStaticProperty.path_chaincode_id);
			prop.load(input);
			chaincodename = prop.getProperty("chaincodename");
			chaincodeid = prop.getProperty("chaincodeid");
			chainCodeRes = new ChainCodeResponse(chaincodeid, chaincodename,null , null);
		} catch (Exception e) {
			 e.printStackTrace();
		}finally{
			if(input != null){
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public void storeChaincodeName(ChainCodeResponse chainCodeResponse ){
		OutputStream output = null;
		try {
			output = new FileOutputStream(HyperledgerStaticProperty.path_chaincode_id);
            prop.setProperty("chaincodename", chainCodeResponse.getTransactionID());
            prop.setProperty("chaincodeid", chainCodeResponse.getTransactionID());
            chainCodeRes = new ChainCodeResponse(chainCodeResponse.getTransactionID(), chainCodeResponse.getTransactionID(),null , null);
            prop.store(output, null);
        } catch (IOException exception) {
            exception.printStackTrace();
        } finally{
			if(output != null){
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public ChainCodeResponse  getChaincodeName(){
		return chainCodeRes;
	}
	
	public boolean verifychaincode(){
		return (getChaincodeName()!=null);
	}
}

