<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${errorTitle}"/>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<h1><c:out value="${errorStatus}"/>: <c:out value="${errorTitle}"/></h1>

<div class="card">
    <p><c:out value="${errorText}"/></p>
    <p><a class="btn" href="${ctx}/catalog">В каталог</a></p>
</div>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>