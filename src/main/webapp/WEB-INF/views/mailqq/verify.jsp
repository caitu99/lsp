<%@ page language="java" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <%--<script type="text/javascript" src="../statics/jquery.js"></script>--%>
</head>
<body>
<form action="<c:url value="/debug/qq/verify/post"/>" method="post">
    <img src="<c:url value="/debug/qq/verify/img"/>" alt="vcode"/><br/>
    <label for="vcode"></label><input type="text" id="vcode" name="vcode"/><br/>
    <input type="submit" value="verify"/>
</form>
</body>
</html>
