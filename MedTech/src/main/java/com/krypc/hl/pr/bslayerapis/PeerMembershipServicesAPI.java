package com.krypc.hl.pr.bslayerapis;

import java.security.cert.CertificateException;

import org.hyperledger.fabric.sdk.Chain;
import org.hyperledger.fabric.sdk.FileKeyValStore;
import org.hyperledger.fabric.sdk.Member;
import org.hyperledger.fabric.sdk.exception.EnrollmentException;

import com.krypc.hl.pr.protoapis.HyperledgerStaticProperty;

/**
 * This version of class is different from the one in Membership Wallet Project
 * @author mohit
 *
 */
public class PeerMembershipServicesAPI {
	
	private static PeerMembershipServicesAPI Instance = null;
//	private PersistentManager manager = PersistentManager.getInstance();
	private Member workBenchUIMember = null;
	
	public static synchronized PeerMembershipServicesAPI getInstance(){
		return Instance == null?Instance = new PeerMembershipServicesAPI():Instance;
	}
	public Chain getChain(){
		return chain;
	}
    private Chain chain = null;
    
    private PeerMembershipServicesAPI(){
    	init();
    }
    public void init() {
        chain = new Chain("chain1");
        try {
            chain.setMemberServicesUrl("grpc://"+HyperledgerStaticProperty.host_membership+":"+HyperledgerStaticProperty.port_membership, null);
            chain.setKeyValStore(new FileKeyValStore(HyperledgerStaticProperty.path_javask_db));
            chain.addPeer("grpc://"+HyperledgerStaticProperty.host+":"+HyperledgerStaticProperty.port, null);
            chain.setDevMode(HyperledgerStaticProperty.isDevMode);
            chain.eventHubConnect("grpc://"+HyperledgerStaticProperty.host_event_hub+":"+HyperledgerStaticProperty.port_event_hub, null);
            Member registrar = chain.getMember("admin");
            if (!registrar.isEnrolled()) {
                registrar = chain.enroll("admin", "Xurw3yU9zI0l");
            }
            chain.setRegistrar(registrar);
            chain.setDeployWaitTime(300);
            chain.setInvokeWaitTime(20);
        } catch (CertificateException | EnrollmentException cex) {
        	cex.printStackTrace();
        }
    }
    /**
     * No direct way known,as now considering the last node in rest get peer info as the connected peer
     */
    private void loadWorkBenchMember(){
//    	ResponseWrapper wrapper = HyperLedgerRestApis.getInstance().getPeers();
//    	if(wrapper.errorCode==0){
//    		JSONObject peerObj = (JSONObject) wrapper.resultObject;
//    		if(peerObj.containsKey("peers")){
//    			JSONArray peerArray = (JSONArray) peerObj.get("peers");
//    			if(peerArray.size()>0){
//    				String peerId = (String) ((JSONObject)((JSONObject)peerArray.get(peerArray.size()-1)).get("ID")).get("name");
//    				UserEntity ent = manager.getUser(peerId);
//    				if(ent!=null){
//    					workBenchUIMember = ent.getMember();
//    				}
//    			}
//    		}
//    	}
    }
    //@bogus
	public Member getWorkBenchUIMember() {
//		if(workBenchUIMember==null){
//			loadWorkBenchMember();
//		}
//		return workBenchUIMember;
		return null;
	}
	public static void main(String[] args) {
		PeerMembershipServicesAPI.getInstance();
	}

}