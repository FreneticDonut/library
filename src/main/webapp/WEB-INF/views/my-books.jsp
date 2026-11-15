<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Мои книги"/>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<h1>Мои книги</h1>

<c:choose>
    <c:when test="${empty loans}">
        <p class="muted">У вас пока нет заказов. Выберите книгу в <a href="${ctx}/catalog">каталоге</a>.</p>
    </c:when>
    <c:otherwise>
        <div class="table-wrap">
            <table>
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
                        <td class="actions">
                            <c:if test="${loan.status == 'ORDERED'}">
                                <form method="post" action="${ctx}/my/orders/cancel" class="inline"
                                      onsubmit="return confirm('Отменить заказ?');">
                                    <input type="hidden" name="loanId" value="${loan.loanId}">
                                    <button type="submit" class="btn-danger">Отменить</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>