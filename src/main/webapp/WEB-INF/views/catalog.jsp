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
    <p>
        <c:choose>
            <c:when test="${not empty sessionScope.currentUser}">
                Вы вошли как <b><c:out value="${sessionScope.currentUser.name}"/></b>
                <form method="post" action="logout" style="display:inline">
                    <button type="submit">Выйти</button>
                    <a href="my/books">Мои книги</a>
                    <c:if test="${sessionScope.currentUser.role == 'LIBRARIAN'}">
                    <a href="admin/home">Панель библиотекаря</a>
                </c:if>
                </form>
            </c:when>
            <c:otherwise>
                <a href="login">Войти</a> | <a href="register">Регистрация</a>
            </c:otherwise>
        </c:choose>
    </p>
        <c:if test="${not empty flashMessage}">
        <p style="color: green"><c:out value="${flashMessage}"/></p>
    </c:if>
    <c:if test="${not empty flashError}">
        <p style="color: red"><c:out value="${flashError}"/></p>
    </c:if>
    <form method="get" action="catalog">
        <input type="text" name="q" value="<c:out value='${q}'/>" placeholder="Название книги">
        <button type="submit">Найти</button>
    </form>

    <c:choose>
        <c:when test="${empty books}">
            <p>Ничего не найдено.</p>
        </c:when>
        <c:otherwise>
            <table border="1" cellpadding="6">
                <tr>
                    <th>Название</th>
                    <th>Год</th>
                    <th>Свободно</th>
                        <c:if test="${sessionScope.currentUser.role == 'LIBRARIAN'}">
                        <th></th>
                    </c:if>
                </tr>
                <c:forEach var="book" items="${books}">
                    <tr>
                        <td><c:out value="${book.title}"/></td>
                        <td><c:out value="${book.publishYear}"/></td>
                        <td>${book.availableCopies} из ${book.totalCopies}</td>
                        <c:if test="${sessionScope.currentUser.role == 'LIBRARIAN'}">
                            <td>
                                <a href="${pageContext.request.contextPath}/admin/books/edit?id=${book.id}">Изменить</a>
                                <form method="post"
                                      action="${pageContext.request.contextPath}/admin/books/delete"
                                      style="display:inline"
                                      onsubmit="return confirm('Удалить книгу?');">
                                    <input type="hidden" name="id" value="${book.id}">
                                    <button type="submit">Удалить</button>
                                </form>
                            </td>
                        </c:if>
                    </tr>
                </c:forEach>
            </table>

            <c:if test="${totalPages > 1}">
                <p>
                    <c:if test="${page > 1}">
                        <c:url var="prevUrl" value="/catalog">
                            <c:param name="q" value="${q}"/>
                            <c:param name="page" value="${page - 1}"/>
                        </c:url>
                        <a href="<c:out value='${prevUrl}'/>">&larr; Назад</a>
                    </c:if>

                    Страница ${page} из ${totalPages}

                    <c:if test="${page < totalPages}">
                        <c:url var="nextUrl" value="/catalog">
                            <c:param name="q" value="${q}"/>
                            <c:param name="page" value="${page + 1}"/>
                        </c:url>
                        <a href="<c:out value='${nextUrl}'/>">Вперёд &rarr;</a>
                    </c:if>
                </p>
            </c:if>
        </c:otherwise>
    </c:choose>
</body>
</html>