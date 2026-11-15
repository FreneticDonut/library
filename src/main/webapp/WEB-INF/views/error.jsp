<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><c:out value="${errorTitle}"/></title>
</head>
<body>
    <h1><c:out value="${errorStatus}"/>: <c:out value="${errorTitle}"/></h1>
    <p><c:out value="${errorText}"/></p>

    <p><a href="${pageContext.request.contextPath}/catalog">В каталог</a></p>
</body>
</html>