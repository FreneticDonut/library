<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Каталог книг</title>
</head>
<body>
    <h1>Каталог книг</h1>

    <table border="1" cellpadding="6">
        <tr>
            <th>Название</th>
            <th>Год</th>
            <th>Свободно</th>
        </tr>
        <c:forEach var="book" items="${books}">
            <tr>
                <td><c:out value="${book.title}"/></td>
                <td><c:out value="${book.publishYear}"/></td>
                <td>${book.availableCopies} из ${book.totalCopies}</td>
            </tr>
        </c:forEach>
    </table>
</body>
</html>