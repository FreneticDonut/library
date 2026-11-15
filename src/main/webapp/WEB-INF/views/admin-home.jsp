<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Панель библиотекаря</title>
</head>
<body>
    <h1>Панель библиотекаря</h1>

    <p>Здравствуйте, <b><c:out value="${sessionScope.currentUser.name}"/></b>!</p>
        <p><a href="${pageContext.request.contextPath}/admin/books/new">Добавить книгу</a></p>

    <p><a href="${pageContext.request.contextPath}/catalog">В каталог</a></p>
</body>
</html>