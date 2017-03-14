<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html><head><style type="text/css">@charset "UTF-8";[ng\:cloak],[ng-cloak],[data-ng-cloak],[x-ng-cloak],.ng-cloak,.x-ng-cloak,.ng-hide:not(.ng-hide-animate){display:none !important;}ng\:form{display:block;}.ng-animate-shim{visibility:hidden;}.ng-anchor{position:absolute;}</style>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link href="${pageContext.request.contextPath}/static/css/bootstrap.min.css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/static/css/datepicker/datepicker3.css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/static/font-awesome/css/font-awesome.css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/static/css/dataTables/jquery.dataTables.min.css" rel="stylesheet">

		
        
         <script src="${pageContext.request.contextPath}/static/js/jquery-2.1.1.js"></script>
         <script src="${pageContext.request.contextPath}/static/js/bootstrap.min.js"></script>
         <script src="${pageContext.request.contextPath}/static/js/datepicker/bootstrap-datepicker.js"></script>
         <script src="${pageContext.request.contextPath}/static/js/dataTables/jquery.dataTables.min.js"></script>
        <!--  <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.8/angular.min.js"></script> -->
<title>Patient Record</title>

<style type="text/css">
.tabactive{border-bottom: 5px solid rgb(49, 112, 143)!important;}
</style>
<script type="text/javascript">
/* var app = angular.module('myApp', []);
app.controller('myCtrl', function($rootScope) {
	$rootScope.search = function(){ */
		
	
function search() {
	var data = new Object();
	data.user = $('#user').val();
	if(data.user!=""){
		
		$(".errorMessage").text("");
			 $.ajax({							
				type : "POST",
				url : "${pageContext.request.contextPath}/getRecordRequestList",
				data : {
					"data" : JSON.stringify(data)
				},
				success : function(result) {
					$("#recordTable").show();
					$("#dataTabletbody").empty();
					if(result.errorCode==0){
						var obj = result.resultObject;
						for(var i = 0; i < obj.length; i++){
	                        document.getElementById("dataTabletbody").innerHTML+="<tr><td id='patientSSN"+i+"'>"+obj[i].PatientSSN+"</td><td id='hash"+i+"' data-toggle='tooltip' title='"+obj[i].Hash+"' style='word-break: break-all;color:green;cursor:pointer'>"+obj[i].Hash.substring(0,6)+"</td><td id='doctorId"+i+"'>"+obj[i].DoctorId+"</td><td id='recordId"+i+"'>"+obj[i].ReportId+"</td><td id='ispaid"+i+"'>"+obj[i].PaymentStatus+"</td><td><input type='hidden' id='isApproved"+i+"' value='"+obj[i].ApproveReport+"'>"+obj[i].ApproveReport+"</td></tr>";        
	                            }
	                        $('#recordTable').DataTable();
			   		}else if(result.errorDetails != null){
			   			$("#errorMessage").text(result.errorDetails);
					}else{
						$("#errorMessage").text(result.errorMessage);
					}
				}
			}); 
		
	} else{
		var errobj = new Object();
		errobj.form_patientSSN = $('#form_patientSSN').val();
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
/* }
	
}); */
</script>
</head>
<body class="ng-scope" ng-app="myApp">
<ul class="nav nav-tabs" style="text-align: center;margin-bottom: 20px;">
<li class="col-md-3"><a href="${pageContext.request.contextPath}/labDashboard">Lab Dashboard</a></li>
<li class="col-md-3"><a href="${pageContext.request.contextPath}/physicianDashboard">Physician Dashboard</a></li>
<li class="col-md-3"><a href="${pageContext.request.contextPath}/patientDashboard">Patient Dashboard</a></li>
<li class="col-md-3"><a  class="tabactive" href="${pageContext.request.contextPath}/report">Report</a></li>
</ul>
<div class="container ng-scope" ng-controller="myCtrl">

            <div class="row">

                <div class="col-lg-10 col-lg-offset-1">
                
                <div class="panel panel-info">
                    <div class="panel-heading">
                        <div class="panel-title">
                        <h5>Patient Report
                        <span class="text-muted ng-binding" style="color:green;float:right;" ng-bind="successMessage" id="successMessage"></span>
                        <span class="text-muted ng-binding" style="color:red;float:right;" ng-bind="errorMessage" id="errorMessage"></span>
                        </h5>
                        </div>
                    </div> 

                   <!--  <h1>Generate Hash </h1> -->
                   <div class="panel-body">
                    <form class="ng-pristine ng-valid" id="contact-form" method="post" action="#" role="form">

    <div class="controls">

        <div class="row">
            <div class="col-md-6">
                <div class="form-group">
                    <input class="form-control" id="user" value="admin" type="hidden">
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-md-12">
               <table id="recordTable" class="table table-striped table-bordered" style="display: none;">
                <thead><tr><th>Patient SSN</th><th>Hash</th><th>Physician License No</th><th>Lab Report Ref</th><th>Payment Status</th><th>Access Permission</th></tr></thead>
               <tbody id="dataTabletbody"></tbody>
              
               </table> 
               
                
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
$(document).ready(function(){
	search();
	$('#recordTable').DataTable();
	$('[data-toggle="tooltip"]').tooltip(); 
});
</script>

</body></html>