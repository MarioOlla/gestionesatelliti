<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="today" class="java.util.Date" scope="page" />
<!doctype html>
<html lang="it" class="h-100" >
	 <head>
	 
	 	<!-- Common imports in pages -->
	 	<jsp:include page="../header.jsp" />
	   
	   <title>Pagina dei Risultati</title>
	 </head>
	 
	<body class="d-flex flex-column h-100">
	 
		<!-- Fixed navbar -->
		<jsp:include page="../navbar.jsp"></jsp:include>
	 
	
		<!-- Begin page content -->
		<main class="flex-shrink-0">
		  <div class="container">
		  
		  		<div class="alert alert-success alert-dismissible fade show  ${successMessage==null?'d-none':'' }" role="alert">
				  ${successMessage}
				  <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close" ></button>
				</div>
				<div class="alert alert-danger alert-dismissible fade show d-none" role="alert">
				  Esempio di operazione fallita!
				  <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close" ></button>
				</div>
				<div class="alert alert-info alert-dismissible fade show d-none" role="alert">
				  Aggiungere d-none nelle class per non far apparire
				   <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close" ></button>
				</div>
		  
		  
		  
		  		<div class='card'>
				    <div class='card-header'>
				        <h5>Lista dei risultati</h5> 
				    </div>
				    <div class='card-body'>
				    	<a class="btn btn-primary " href="${pageContext.request.contextPath}/satellite/insert">Add New</a>
				    
				        <div class='table-responsive'>
				            <table class='table table-striped ' >
				                <thead>
				                    <tr>
			                         	<th>Denominazione</th>
				                        <th>Codice</th>
				                        <th>Data di Lancio</th>
				                        <th>Data di Rientro</th>
				                        <th>Stato</th>
				                        <th>Azioni</th>
				                    </tr>
				                </thead>
				                <tbody>
				                	<c:forEach items="${satellite_list_attribute }" var="satelliteItem">
										<tr>
											<td>${satelliteItem.denominazione }</td>
											<td>${satelliteItem.codice }</td>
											<td><fmt:formatDate type = "date" value = "${satelliteItem.dataLancio}" /></td>
											<td><fmt:formatDate type = "date" value = "${satelliteItem.dataRientro}" /></td>
											<td>${satelliteItem.stato }</td>
											<td>
												<a class="btn  btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/satellite/show/${satelliteItem.id }">Visualizza</a>
												
												<c:if test="${satelliteItem.dataRientro==null||satelliteItem.dataRientro.after(today)}">
													<a class="btn  btn-sm btn-outline-primary ml-2 mr-2" href="${pageContext.request.contextPath}/satellite/preUpdate/${satelliteItem.id }">Edit</a>
												</c:if>
												
												<c:if test="${(satelliteItem.dataLancio==null||satelliteItem.dataLancio.after(today))||(satelliteItem.dataRientro!=null&&satelliteItem.dataRientro.before(today))}">
													<a class="btn  btn-outline-danger btn-sm" href="${pageContext.request.contextPath }/satellite/preDelete/${satelliteItem.id}">Delete</a>
												</c:if>
												
												<c:if test="${satelliteItem.dataLancio==null||satelliteItem.dataLancio.after(today) }">
													<a class="btn  btn-sm btn-outline-primary ml-2 mr-2" href="${pageContext.request.contextPath}/satellite/launch/${satelliteItem.id }">Launch</a>
												</c:if>
												
												<c:if test="${(satelliteItem.dataRientro==null&&satelliteItem.dataLancio!=null&&satelliteItem.dataLancio.before(today))||satelliteItem.dataRientro.after(today) }">
													<a class="btn  btn-sm btn-outline-primary ml-2 mr-2" href="${pageContext.request.contextPath}/satellite/rientra/${satelliteItem.id }">Rientra</a>
												</c:if>
											</td>
										</tr>
									</c:forEach>
				                </tbody>
				            </table>
				        </div>
				        
				        <div class="col-12">
							<a href="${pageContext.request.contextPath}/satellite/search" class='btn btn-outline-secondary' style='width:80px'>
					            <i class='fa fa-chevron-left'></i> Back 
					        </a>
						</div>
				   
					<!-- end card-body -->			   
			    </div>
			<!-- end card -->
			</div>	
		 
		   
		 <!-- end container -->  
		  </div>
		  
		</main>
		
		<!-- Footer -->
		<jsp:include page="../footer.jsp" />
		
	</body>
</html>