<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Вход"/>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<h1>Вход</h1>

<div class="card">
    <c:if test="${not empty error}">
        <div class="msg msg-error"><c:out value="${error}"/></div>
    </c:if>

    <form method="post" action="${ctx}/login">
        <label>Email
            <input type="email" name="email" required value="<c:out value='${email}'/>">
        </label>
        <label>Пароль
            <input type="password" name="password" required>
        </label>
        <button type="submit">Войти</button>
    </form>

    <p class="muted">Нет аккаунта? <a href="${ctx}/register">Зарегистрироваться</a></p>
</div>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>