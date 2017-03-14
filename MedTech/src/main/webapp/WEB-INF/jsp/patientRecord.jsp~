<html><head><style type="text/css">@charset "UTF-8";[ng\:cloak],[ng-cloak],[data-ng-cloak],[x-ng-cloak],.ng-cloak,.x-ng-cloak,.ng-hide:not(.ng-hide-animate){display:none !important;}ng\:form{display:block;}.ng-animate-shim{visibility:hidden;}.ng-anchor{position:absolute;}</style>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link href="${pageContext.request.contextPath}/static/css/bootstrap.min.css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/static/css/datepicker/datepicker3.css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/static/font-awesome/css/font-awesome.css" rel="stylesheet">

		
        
         <script src="${pageContext.request.contextPath}/static/js/jquery-2.1.1.js"></script>
         <script src="${pageContext.request.contextPath}/static/js/bootstrap.min.js"></script>
         <script src="${pageContext.request.contextPath}/static/js/datepicker/bootstrap-datepicker.js"></script>
         <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.8/angular.min.js"></script>
<title>Patient Record</title>

<style type="text/css">
.tabactive{border-bottom: 5px solid rgb(49, 112, 143)!important;}
</style>
<script type="text/javascript">
var app = angular.module('myApp', []);
app.controller('myCtrl', function($scope) {
	$scope.generateHash = function(){
		
	
//function generateHash() {
	
	var data = new Object();
	data.patientSSN = $('#form_patientSSN').val();
	data.lastName = $('#form_lastName').val();
	data.dateOfBirth = $('#form_dateOfBirth').val();
	data.dateOfVisit = $('#form_dateOfVisit').val();
	data.doctorLicense = $('#form_doctorLicense').val();
	if(data.patientSSN!="" && data.lastName!="" && data.dateOfBirth!="" && data.dateOfVisit!="" && data.doctorLicense!=""){
		$(".errorMessage").text("");
		
			$.ajax({							
				type : "POST",
				url : "${pageContext.request.contextPath}/generateHash",
				data : {
					"data" : JSON.stringify(data)
				},
				success : function(result) {
					if(result.errorCode==0){
						$("#hashValueDiv").show();
						$("#reportIdDiv").show();
						$("#hashValue").val(result.resultObject);
						$("#submitRecord").show();
						$("#generateHash").hide();
			   			//$('input[class=form-control]').val('');
			    	    
			   		}else if(result.errorDetails != null){
			   			
					}else{
						
					}
				}
			});
			
	
		
	} else{
		var errobj = new Object();
		errobj.form_patientSSN = $('#form_patientSSN').val();
		errobj.form_lastName= $('#form_lastName').val();
		errobj.form_dateOfBirth = $('#form_dateOfBirth').val();
		errobj.form_dateOfVisit= $('#form_dateOfVisit').val();
		errobj.form_doctorLicense= $('#form_doctorLicense').val();
		var obj1 = JSON.stringify(errobj);
		 $.each(JSON.parse(obj1), function(k, v) {
	         var key='#'+k;
	         if(v==""){
	             var label = $(key).closest("div").prev().text();
	             if($("#"+k+"error").text() == null || $("#"+k+"error").text() == ""){
	                if(label == "" || label == null){
	                	$("<span class='errorMessage' style='color:red;' id='"+k+"error'>"+k+" Missing<span>").insertAfter("#"+k);
	                }else{
	                	$("<span class='errorMessage' style='color:red;' id='"+k+"error'>"+label+" Missing<span>").insertAfter("#"+k);
	                }
	            
	             var a = $(key).attr("id");
	             document.getElementById(k).focus();
	                return false;
	           			 
			}
	         }else{
           	 $("#"+k+"error").text("");
            }
        
    });
	
	}	
}

	$scope.submitRecord = function(){
		$("#successMessage").text("");
		$("#errorMessage").text("");
		var data = new Object();
		data.user = $('#user').val();
		data.hash = $('#hashValue').val();
		data.reportId = $('#reportId').val();
		
		$.ajax({							
			type : "POST",
			url : "${pageContext.request.contextPath}/createRecord",
			data : {
				"data" : JSON.stringify(data)
			},
			success : function(result) {
				if(result.errorCode==0){
					$("#successMessage").html("Record Created Successfully.<br> Record transaction Id : "+result.resultObject.transactionID);
					//$scope.successMessage = smessage;
					$('input[class=form-control]').val('');
		   		}else if(result.errorDetails != null){
		   			$("#errorMessage").text(result.errorDetails);
		   			//$scope.errorMessage = emessage;
				}else{
					$("#errorMessage").text(result.errorMessage);
					//$scope.errorMessage = emessage;
				}
				$("#hashValueDiv").hide();
				$("#reportIdDiv").hide();
				$("#submitRecord").hide();
				$("#generateHash").show();
			}
		});
		
	}
	
});
</script>
</head>
<body class="ng-scope" ng-app="myApp">
<ul class="nav nav-tabs" style="text-align: center;margin-bottom: 20px;">
<li class="col-md-3"><a class="tabactive" href="${pageContext.request.contextPath}/labDashboard">Lab Dashboard</a></li>
<li class="col-md-3"><a href="${pageContext.request.contextPath}/physicianDashboard">Physician Dashboard</a></li>
<li class="col-md-3"><a href="${pageContext.request.contextPath}/patientDashboard">Patient Dashboard</a></li>
<li class="col-md-3"><a href="${pageContext.request.contextPath}/report">Report</a></li>
</ul>
<div class="container ng-scope" ng-controller="myCtrl">

            <div class="row">

                <div class="col-lg-10 col-lg-offset-1">
                
                <div class="panel panel-info">
                    <div class="panel-heading">
                        <div class="panel-title">
                        <h5>Medical Report Entry
                        <span class="text-muted ng-binding" style="color:green;float: right;" ng-bind="successMessage" id="successMessage"></span>
                        <span class="text-muted ng-binding" style="color:red;float: right;" ng-bind="errorMessage" id="errorMessage"></span>
                        </h5>
                        </div>
                        
                    </div> 
					 <input id="user" value="admin" type="hidden">
                   <!--  <h1>Generate Hash </h1> -->
                   <div class="panel-body">
                    <form class="ng-pristine ng-valid" id="contact-form" method="post" action="#" role="form">

    <div class="controls">

        <div class="row">
            <div class="col-md-6">
                <div class="form-group">
                    <label for="form_name">Patient SSN *</label>
                    <input id="form_patientSSN" name="form_patientSSN" class="form-control" placeholder="Please provide Patient SSN " required="required" data-error="SSN is required." type="text">
                    <div class="help-block with-errors"></div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="form-group">
                    <label for="form_lastname">Last Name *</label>
                    <input id="form_lastName" name="form_lastName" class="form-control" placeholder="Please provide lastname " required="required" data-error="Lastname is required." type="text">
                    <div class="help-block with-errors"></div>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-md-6">
                <div class="form-group">
                <label for="form_dateOfBirth">Date of Birth *</label>
                <div class=" input-group date">
                    <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
                    <input id="form_dateOfBirth" name="form_dateOfBirth" class="form-control" placeholder="Date of birth " required="required" type="text"></div>
                    <div class="help-block with-errors"></div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="form-group">
                    <label for="form_dateOfVisit">Date of Visit</label>
                    <div class=" input-group date">
                    <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
                    <input id="form_dateOfVisit" name="form_dateOfVisit" class="form-control" placeholder="Date of visit" type="text"></div>
                    <div class="help-block with-errors"></div>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-md-12">
                <div class="form-group">
                    <label for="form_doctorLicense">Physician License No.*</label>
                    <input id="form_doctorLicense" name="form_doctorLicense" class="form-control" placeholder="Please provide doctor license" type="text">
                    <div class="help-block with-errors"></div>
                </div>
            </div>
            <div class="col-md-12">
                <input id="generateHash" class="btn btn-success btn-send" value="Get Hash" ng-click="generateHash();" type="button">
            </div>
           
            <div class="col-md-12">
            
             <div class="form-group" id="hashValueDiv" style="display: none;">
              <label>Generated Hash</label> 
                <input class="form-control" id="hashValue" readonly="readonly" type="text">
              </div>
              <div class="form-group" id="reportIdDiv" style="display: none;"> 
               <label>Report Id</label>
                <input class="form-control" id="reportId" placeholder="Enter Report Id" type="text">
                </div>
                <div class="form-group" id="submitRecord" style="display: none;">
                <input class="btn btn-success btn-send" value="Submit" ng-click="submitRecord();" type="button">
                </div>
            </div>
        </div>
    </div>

</form> 
</div>
</div>


                </div>

            </div>

        </div>
      
<script type="text/javascript">
   $(document).on('focus',".input-group.date", function(){
	    $(this).datepicker({
	        format: 'dd-mm-yyyy',							      
	        autoclose: true,	
	        todayBtn: true,
	        minView: "month" ,
	       orientation: "top",
            todayHighlight : true,
	      });
	});
   </script>  


</body></html>