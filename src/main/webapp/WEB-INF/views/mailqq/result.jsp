<%@ page language="java" pageEncoding="utf-8" %>
<%@ page language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <%--<script type="text/javascript" src="../statics/jquery.js"></script>--%>
</head>
<body>
<c:out value="${result}"/><br/><br/>

<a href="<c:url value="/debug/qq"/>">return</a>
</body>
</html>
