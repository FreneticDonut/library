<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="editMode" value="${not empty bookId}"/>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${editMode ? 'Редактировать книгу' : 'Добавить книгу'}</title>
</head>
<body>
    <h1>${editMode ? 'Редактировать книгу' : 'Добавить книгу'}</h1>

    <c:if test="${param.added == '1'}">
        <p style="color: green">Книга добавлена.</p>
    </c:if>
    <c:if test="${param.saved == '1'}">
        <p style="color: green">Изменения сохранены.</p>
    </c:if>

    <c:if test="${not empty error}">
        <p style="color: red"><c:out value="${error}"/></p>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}${editMode ? '/admin/books/edit' : '/admin/books/new'}">
        <c:if test="${editMode}">
            <input type="hidden" name="id" value="${bookId}">
        </c:if>
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
        <button type="submit">${editMode ? 'Сохранить' : 'Добавить'}</button>
    </form>

    <p><a href="${pageContext.request.contextPath}/admin/home">В панель библиотекаря</a></p>
    <p><a href="${pageContext.request.contextPath}/catalog">В каталог</a></p>
</body>
</html>