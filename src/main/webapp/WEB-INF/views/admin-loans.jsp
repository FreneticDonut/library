<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Заказы и выдачи"/>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<h1>Заказы и выдачи</h1>

<h2>Ждут выдачи</h2>
<c:choose>
    <c:when test="${empty orders}">
        <p class="muted">Нет заказов, ожидающих выдачи.</p>
    </c:when>
    <c:otherwise>
        <div class="table-wrap">
            <table>
                <tr>
                    <th>Читатель</th>
                    <th>Книга</th>
                    <th></th>
                </tr>
                <c:forEach var="loan" items="${orders}">
                    <tr>
                        <td>
                            <c:out value="${loan.userName}"/>
                            <span class="muted">(<c:out value="${loan.userEmail}"/>)</span>
                        </td>
                        <td><c:out value="${loan.bookTitle}"/></td>
                        <td class="actions">
                            <form method="post" action="${ctx}/admin/loans" class="inline">
                                <input type="hidden" name="action" value="issue">
                                <input type="hidden" name="loanId" value="${loan.loanId}">
                                <button type="submit">Выдать</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </div>
    </c:otherwise>
</c:choose>

<h2>На руках</h2>
<c:choose>
    <c:when test="${empty active}">
        <p class="muted">Сейчас нет книг на руках.</p>
    </c:when>
    <c:otherwise>
        <div class="table-wrap">
            <table>
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
                            <span class="muted">(<c:out value="${loan.userEmail}"/>)</span>
                        </td>
                        <td><c:out value="${loan.bookTitle}"/></td>
                        <td>
                            ${loan.dueDate}
                            <c:if test="${loan.overdue}">
                                <span class="overdue">просрочено</span>
                            </c:if>
                        </td>
                        <td class="actions">
                            <form method="post" action="${ctx}/admin/loans" class="inline">
                                <input type="hidden" name="action" value="return">
                                <input type="hidden" name="loanId" value="${loan.loanId}">
                                <button type="submit">Принять возврат</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>