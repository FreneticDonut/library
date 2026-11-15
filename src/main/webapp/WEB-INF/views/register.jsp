<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Регистрация"/>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<h1>Регистрация</h1>

<div class="card">
    <c:if test="${param.registered == '1'}">
        <div class="msg msg-ok">
            Регистрация прошла успешно. <a href="${ctx}/login">Войти</a>
        </div>
    </c:if>

    <c:if test="${not empty error}">
        <div class="msg msg-error"><c:out value="${error}"/></div>
    </c:if>

    <form method="post" action="${ctx}/register">
        <label>Email
            <input type="email" name="email" required value="<c:out value='${email}'/>">
        </label>
        <label>Имя
            <input type="text" name="name" required maxlength="100" value="<c:out value='${name}'/>">
        </label>
        <label>Пароль (не менее 8 символов)
            <input type="password" name="password" required minlength="8">
        </label>
        <button type="submit">Зарегистрироваться</button>
    </form>
</div>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>