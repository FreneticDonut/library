<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Панель библиотекаря"/>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<h1>Панель библиотекаря</h1>

<div class="card">
    <ul class="link-list">
        <li><a href="${ctx}/admin/books/new">Добавить книгу</a></li>
        <li><a href="${ctx}/admin/loans">Заказы и выдачи</a></li>
        <li><a href="${ctx}/catalog">Каталог (изменение и удаление книг)</a></li>
    </ul>
</div>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>