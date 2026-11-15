<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Добавить книгу</title>
</head>
<body>
    <h1>Добавить книгу</h1>

    <c:if test="${param.added == '1'}">
        <p style="color: green">Книга добавлена.</p>
    </c:if>

    <c:if test="${not empty error}">
        <p style="color: red"><c:out value="${error}"/></p>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/admin/books/new">
        <p>
            <label>Название<br>
                <input type="text" name="title" required maxlength="300"
                       value="<c:out value='${title}'/>">
            </label>
        </p>
        <p>
            <label>ISBN (10 или 13 цифр, можно с дефисами)<br>
                <input type="text" name="isbn" value="<c:out value='${isbn}'/>">
            </label>
        </p>
        <p>
            <label>Год издания<br>
                <input type="text" name="publishYear" value="<c:out value='${publishYear}'/>">
            </label>
        </p>
        <p>
            <label>Количество экземпляров<br>
                <input type="number" name="totalCopies" required min="1"
                       value="<c:out value='${totalCopies}'/>">
            </label>
        </p>
        <button type="submit">Добавить</button>
    </form>

    <p><a href="${pageContext.request.contextPath}/admin/home">В панель библиотекаря</a></p>
</body>
</html>