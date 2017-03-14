package com.krypc.hl.pr.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.krypc.api.constants.PageConstants;

@Controller
public class WebController {
    Logger logger = Logger.getLogger(WebController.class);    
    
    @RequestMapping(value = "/labDashboard", method = { RequestMethod.POST, RequestMethod.GET })
    public String getPatientRecord(HttpServletRequest request,HttpServletResponse response){
        
        return PageConstants.PATIENT_RECORD;
    }
    
    @RequestMapping(value = "/physicianDashboard", method = { RequestMethod.POST, RequestMethod.GET })
    public String requestRecord(HttpServletRequest request,HttpServletResponse response){
        
        return PageConstants.REQUEST_RECORD;
    }
    
    @RequestMapping(value = "/patientDashboard", method = { RequestMethod.POST, RequestMethod.GET })
    public String patientApproval(HttpServletRequest request,HttpServletResponse response){
        
        return PageConstants.PATIENT_APPROVAL;
    }
    
    @RequestMapping(value = "/report", method = { RequestMethod.POST, RequestMethod.GET })
    public String patientReport(HttpServletRequest request,HttpServletResponse response){
        
        return PageConstants.PATIENT_REPORT;
    }
}
