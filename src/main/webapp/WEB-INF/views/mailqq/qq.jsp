<%@ page language="java" pageEncoding="utf-8" %>
<%@ page language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <%--<script type="text/javascript" src="../statics/jquery.js"></script>--%>
</head>
<body>
<form action="<c:url value="/debug/qq/login"/>" method="post">
    <label for="account">account:</label><input type="text" id="account" name="account"/><br/>
    <label for="pwd">password:</label><input type="text" id="pwd" name="pwd"/><br/>
    <input type="submit" value="login"/>
</form>
</body>
</html>
