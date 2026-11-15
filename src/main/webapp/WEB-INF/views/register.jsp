<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Регистрация</title>
</head>
<body>
    <h1>Регистрация</h1>

    <c:if test="${param.registered == '1'}">
        <p style="color: green">Регистрация прошла успешно.</p>
    </c:if>

    <c:if test="${not empty error}">
        <p style="color: red"><c:out value="${error}"/></p>
    </c:if>

    <form method="post" action="register">
        <p>
            <label>Email<br>
                <input type="email" name="email" required
                       value="<c:out value='${email}'/>">
            </label>
        </p>
        <p>
            <label>Имя<br>
                <input type="text" name="name" required maxlength="100"
                       value="<c:out value='${name}'/>">
            </label>
        </p>
        <p>
            <label>Пароль (не менее 8 символов)<br>
                <input type="password" name="password" required minlength="8">
            </label>
        </p>
        <button type="submit">Зарегистрироваться</button>
    </form>
</body>
</html>