<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<head>
    <title>MailSrc</title>
    <link href="../../../statics/css/page.css" rel="stylesheet" type="text/css"/>
</head>
<body>
<div id="div_body">
    <div>
        <h1 align="center">邮件查询</h1>
    </div>
    <center>
        <form action="." method="post">
            请出入邮件id:<label><input type="text" name="uuid"/></label>
            <input type="submit" value="查询"/>
        </form>
    </center>
    <c:if test="${display==1}">
        <center>
            查询结果：
                ${message}
        </center>
        <br/>
        <c:if test="${displaydata==1}">
            <div>
                <table class="tablecss">
                    <tr>
                        <td>邮件id</td>
                        <td>${uuid}</td>
                    </tr>

                    <tr>
                        <td>BillID</td>
                        <td>${id}</td>
                    </tr>
                    <tr>
                        <td>用户ID</td>
                        <td>${userid}</td>
                    </tr>
                    <tr>
                        <td>邮件账号</td>
                        <td>${account}</td>
                    </tr>
                    <tr>
                        <td>title</td>
                        <td>${title}</td>
                    </tr>
                    <tr>
                        <td>邮件内容</td>
                        <td>${body}</td>
                    </tr>
                </table>
            </div>
        </c:if>
    </c:if>

</div>

</body>
</html>
