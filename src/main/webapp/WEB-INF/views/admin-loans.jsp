<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Заказы и выдачи</title>
</head>
<body>
    <h1>Заказы и выдачи</h1>

    <c:if test="${not empty flashMessage}">
        <p style="color: green"><c:out value="${flashMessage}"/></p>
    </c:if>
    <c:if test="${not empty flashError}">
        <p style="color: red"><c:out value="${flashError}"/></p>
    </c:if>

    <h2>Ждут выдачи</h2>
    <c:choose>
        <c:when test="${empty orders}">
            <p>Нет заказов, ожидающих выдачи.</p>
        </c:when>
        <c:otherwise>
            <table border="1" cellpadding="6">
                <tr>
                    <th>Читатель</th>
                    <th>Книга</th>
                    <th></th>
                </tr>
                <c:forEach var="loan" items="${orders}">
                    <tr>
                        <td>
                            <c:out value="${loan.userName}"/>
                            (<c:out value="${loan.userEmail}"/>)
                        </td>
                        <td><c:out value="${loan.bookTitle}"/></td>
                        <td>
                            <form method="post"
                                  action="${pageContext.request.contextPath}/admin/loans"
                                  style="display:inline">
                                <input type="hidden" name="action" value="issue">
                                <input type="hidden" name="loanId" value="${loan.loanId}">
                                <button type="submit">Выдать</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </c:otherwise>
    </c:choose>

    <h2>На руках</h2>
    <c:choose>
        <c:when test="${empty active}">
            <p>Сейчас нет книг на руках.</p>
        </c:when>
        <c:otherwise>
            <table border="1" cellpadding="6">
                <tr>
                    <th>Читатель</th>
                    <th>Книга</th>
                    <th>Вернуть до</th>
                    <th></th>
                </tr>
                <c:forEach var="loan" items="${active}">
                    <tr>
                        <td>
                            <c:out value="${loan.userName}"/>
                            (<c:out value="${loan.userEmail}"/>)
                        </td>
                        <td><c:out value="${loan.bookTitle}"/></td>
                        <td>
                            ${loan.dueDate}
                            <c:if test="${loan.overdue}">
                                <b style="color: red">просрочено</b>
                            </c:if>
                        </td>
                        <td>
                            <form method="post"
                                  action="${pageContext.request.contextPath}/admin/loans"
                                  style="display:inline">
                                <input type="hidden" name="action" value="return">
                                <input type="hidden" name="loanId" value="${loan.loanId}">
                                <button type="submit">Принять возврат</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </c:otherwise>
    </c:choose>

    <p><a href="${pageContext.request.contextPath}/admin/home">В панель библиотекаря</a></p>
</body>
</html>