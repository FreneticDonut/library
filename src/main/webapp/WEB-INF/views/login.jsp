<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Вход</title>
</head>
<body>
    <h1>Вход</h1>

    <c:if test="${not empty error}">
        <p style="color: red"><c:out value="${error}"/></p>
    </c:if>

    <form method="post" action="login">
        <p>
            <label>Email<br>
                <input type="email" name="email" required
                       value="<c:out value='${email}'/>">
            </label>
        </p>
        <p>
            <label>Пароль<br>
                <input type="password" name="password" required>
            </label>
        </p>
        <button type="submit">Войти</button>
    </form>

    <p>Нет аккаунта? <a href="register">Зарегистрироваться</a></p>
    <p><a href="catalog">В каталог</a></p>
</body>
</html>