<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Мои книги</title>
</head>
<body>
    <h1>Мои книги</h1>

    <p>Здравствуйте, <b><c:out value="${sessionScope.currentUser.name}"/></b>!</p>

    <c:if test="${not empty flashMessage}">
        <p style="color: green"><c:out value="${flashMessage}"/></p>
    </c:if>
    <c:if test="${not empty flashError}">
        <p style="color: red"><c:out value="${flashError}"/></p>
    </c:if>

    <c:choose>
        <c:when test="${empty loans}">
            <p>У вас пока нет заказов. Выберите книгу в каталоге.</p>
        </c:when>
        <c:otherwise>
            <table border="1" cellpadding="6">
                <tr>
                    <th>Книга</th>
                    <th>Статус</th>
                    <th>Вернуть до</th>
                    <th></th>
                </tr>
                <c:forEach var="loan" items="${loans}">
                    <tr>
                        <td><c:out value="${loan.bookTitle}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${loan.status == 'ORDERED'}">Заказана, ждёт выдачи</c:when>
                                <c:when test="${loan.status == 'ACTIVE'}">На руках</c:when>
                                <c:otherwise>Возвращена</c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:if test="${loan.status == 'ACTIVE'}">${loan.dueDate}</c:if>
                        </td>
                        <td>
                            <c:if test="${loan.status == 'ORDERED'}">
                                <form method="post"
                                      action="${pageContext.request.contextPath}/my/orders/cancel"
                                      style="display:inline"
                                      onsubmit="return confirm('Отменить заказ?');">
                                    <input type="hidden" name="loanId" value="${loan.loanId}">
                                    <button type="submit">Отменить</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </c:otherwise>
    </c:choose>

    <p><a href="${pageContext.request.contextPath}/catalog">В каталог</a></p>
</body>
</html>