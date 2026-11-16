<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Каталог книг"/>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>
<c:set var="isLibrarian" value="${sessionScope.currentUser.role == 'LIBRARIAN'}"/>
<c:set var="isReader" value="${sessionScope.currentUser.role == 'READER'}"/>

<div class="toolbar">
    <h1>Каталог книг</h1>
    <c:if test="${isLibrarian}">
        <a class="btn" href="${ctx}/admin/books/new">Добавить книгу</a>
    </c:if>
</div>

<form method="get" action="${ctx}/catalog" class="search">
    <input type="text" name="q" value="<c:out value='${q}'/>" placeholder="Название книги">
    <button type="submit">Найти</button>
</form>

<c:choose>
    <c:when test="${empty books}">
        <p class="muted">Ничего не найдено.</p>
    </c:when>
    <c:otherwise>
        <div class="table-wrap">
            <table>
                <tr>
                    <th>Название</th>
                    <th>Авторы</th>
                    <th>Год</th>
                    <th>Свободно</th>
                    <c:if test="${isLibrarian || isReader}">
                        <th></th>
                    </c:if>
                </tr>
                <c:forEach var="book" items="${books}">
                    <tr>
                        <td><c:out value="${book.title}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${empty authors[book.id]}">
                                    <span class="muted">—</span>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="author" items="${authors[book.id]}" varStatus="st">
                                        <c:out value="${author.name}"/><c:if test="${not st.last}">, </c:if>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td><c:out value="${book.publishYear}"/></td>
                        <td>${book.availableCopies} из ${book.totalCopies}</td>

                        <c:if test="${isLibrarian}">
                            <td class="actions">
                                <a href="${ctx}/admin/books/edit?id=${book.id}">Изменить</a>
                                <form method="post" action="${ctx}/admin/books/delete" class="inline"
                                      onsubmit="return confirm('Удалить книгу?');">
                                    <input type="hidden" name="id" value="${book.id}">
                                    <button type="submit" class="btn-danger">Удалить</button>
                                </form>
                            </td>
                        </c:if>

                        <c:if test="${isReader}">
                            <td class="actions">
                                <c:choose>
                                    <c:when test="${book.availableCopies > 0}">
                                        <form method="post" action="${ctx}/my/orders" class="inline">
                                            <input type="hidden" name="bookId" value="${book.id}">
                                            <input type="hidden" name="q" value="<c:out value='${q}'/>">
                                            <input type="hidden" name="page" value="${page}">
                                            <button type="submit">Заказать</button>
                                        </form>
                                    </c:when>
                                    <c:otherwise><span class="muted">Нет в наличии</span></c:otherwise>
                                </c:choose>
                            </td>
                        </c:if>
                    </tr>
                </c:forEach>
            </table>
        </div>

        <c:if test="${totalPages > 1}">
            <div class="pagination">
                <c:if test="${page > 1}">
                    <c:url var="prevUrl" value="/catalog">
                        <c:param name="q" value="${q}"/>
                        <c:param name="page" value="${page - 1}"/>
                    </c:url>
                    <a href="<c:out value='${prevUrl}'/>">&larr; Назад</a>
                </c:if>

                <span>Страница ${page} из ${totalPages}</span>

                <c:if test="${page < totalPages}">
                    <c:url var="nextUrl" value="/catalog">
                        <c:param name="q" value="${q}"/>
                        <c:param name="page" value="${page + 1}"/>
                    </c:url>
                    <a href="<c:out value='${nextUrl}'/>">Вперёд &rarr;</a>
                </c:if>
            </div>
        </c:if>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>