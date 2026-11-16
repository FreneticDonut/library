<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="editMode" value="${not empty bookId}"/>
<c:set var="pageTitle" value="${editMode ? 'Редактировать книгу' : 'Добавить книгу'}"/>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<h1><c:out value="${pageTitle}"/></h1>

<div class="card">
    <c:if test="${param.added == '1'}">
        <div class="msg msg-ok">Книга добавлена.</div>
    </c:if>
    <c:if test="${param.saved == '1'}">
        <div class="msg msg-ok">Изменения сохранены.</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="msg msg-error"><c:out value="${error}"/></div>
    </c:if>

    <form method="post" action="${ctx}${editMode ? '/admin/books/edit' : '/admin/books/new'}">
        <c:if test="${editMode}">
            <input type="hidden" name="id" value="${bookId}">
        </c:if>
        <label>Название
            <input type="text" name="title" required maxlength="300" value="<c:out value='${title}'/>">
        </label>
        <label>ISBN (10 или 13 цифр, можно с дефисами)
            <input type="text" name="isbn" value="<c:out value='${isbn}'/>">
        </label>
        <label>Год издания
            <input type="text" name="publishYear" value="<c:out value='${publishYear}'/>">
        </label>
        <label>Количество экземпляров
            <input type="number" name="totalCopies" required min="1" value="<c:out value='${totalCopies}'/>">
        </label>
        <c:if test="${not editMode}">
            <label>Авторы (через запятую или точку с запятой)
                <input type="text" name="authors" value="<c:out value='${authors}'/>">
            </label>
            <p class="muted">Например: Лев Толстой, Фёдор Достоевский. Каждое имя пишите целиком, без запятой внутри.</p>
        </c:if>
        <button type="submit">${editMode ? 'Сохранить' : 'Добавить'}</button>
    </form>
</div>

<p><a href="${ctx}/catalog">&larr; В каталог</a></p>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>