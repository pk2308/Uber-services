<%-- 
    Author     : Kent Yeh
--%>
<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<%--Context path--%><sec:authorize access="authenticated" var="logined"/>
<c:if test="${logined}"><c:redirect url="/user/myinfo"/></c:if>
<c:set var="cp" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8"/>
        <title><fmt:message key="login"/></title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        <link rel="stylesheet" href="${cp}/static/purecss/build/pure-min.css" type="text/css" media="screen"/>
        <style type="text/css">
            form{
                display: table;
                margin: 10px auto;
            }
            .warnning {
                color:red !important;
                font-weight:bold;
            }
            .heading {
                color:teal !important;
                font-weight:bold;
            }
            table {
                margin: 0 auto;
            }
            th,td{
                white-space: nowrap;
                padding-top: 10px;
                padding-bottom: 10px;
            }
            .center{
                text-align: center;
            }
            .roundButton{
                border-radius: 32px;
                text-shadow: 0 1px 1px rgba(0, 0, 0, 0.2);
            }
        </style><fmt:message key="account" var="account"/><fmt:message key="password" var="password"/>
        <script type="text/javascript">
            window.onload=function(){
                document.getElementById("j_username").focus();
            }
            function doSubmit(form){
                var comp = form.elements["username"];
                if(!comp.value){
                    comp.focus();
                    alert("<fmt:message key="jsp.needInput"><fmt:param value="${account}"/></fmt:message>");
                    return false;
                }
                comp = form.elements["password"];
                if(!comp.value){
                    comp.focus();
                    alert("<fmt:message key="jsp.needInput"><fmt:param value="${password}"/></fmt:message>");
                    return false;
                }
                comp = form.elements["captcha"];
                if(!comp.value){
                    comp.focus();
                    alert("<fmt:message key="jsp.needInput"><fmt:param value="${captcha}"/></fmt:message>");
                    return false;
                }
                return true;
            }
            function refreshCaptcha(xle){
                xle = xle || document.getElementById("captchaImg");
                if(xle)
                    xle.src="${cp}/captcha?"+ new Date().getTime();
            }
        </script>
    </head>
    <body><a href="${pageContext.request.contextPath}" style="position: absolute;left: 2px;top: 2px" class="pure-button pure-button-primary"><fmt:message key="homepage"/></a><center>
        <div align="center" style="color:red;font-weight:bold;text-align: center" id="msgArea">
            <c:if test="${not empty param.cause or not empty param.authfailed or not empty requestScope.errorMessage}">
                <c:if test="${'expired' eq param.cause}">Session expired,Please login again</c:if>
                <c:if test="${'sessionExceed' eq param.cause}">Session expired,Please try later.</c:if>
                <c:if test="${not empty param.authfailed}"><fmt:message key="exception"/>: ${SPRING_SECURITY_LAST_EXCEPTION.message}</c:if>
                <c:if test="${not empty requestScope.errorMessage}">${requestScope.errorMessage}</c:if>
            </c:if>                                        
        </div>
        <form class="pure-form" action="<c:url value='j_spring_security_check'/>" method="post" onsubmit="return doSubmit(this)">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <fieldset>
                <legend class="heading"><fmt:message key="login"/></legend>
                <table>
                    <tbody>
                        <tr><td><label for="j_username">${account}</label></td>
                            <td><input type="text" class="pure-input-rounded uppercase" id="j_username"  name="username" value="${empty param.username?username:param.username}" onfocus="this.select()" autofocus required/></td></tr>
                        <tr><td><label for="j_password">${password}</label></td>
                            <td><input type="password" class="pure-input-rounded" id="j_password" name="password" value="${empty param.password?'':param.password}" placeholder="${password}" required/></td></tr>
                        <tr><td><img id="captchaImg" src="${cp}/captcha" alt="<fmt:message key="captcha"/>" title="<fmt:message key="clickchg"/>" class="pure-img" style="cursor: pointer;vertical-align: middle" onclick="refreshCaptcha(this)"/></td>
                            <td><span style="cursor:pointer;color:blue;text-decoration:underline" onclick="refreshCaptcha()"><fmt:message key="clickchg"/></span></td></tr>
                        <tr><td><label for="captcha"><fmt:message key="captcha"/></label></td>
                            <td><input type="text" class="pure-input-rounded" id="captcha" name="captcha" value="" maxlength="4" required/></td></tr>
                        <tr><td colspan="2"><input type="checkbox" id="rememberMe" name="remember-me" ${not empty param['remember-me']?'checked':''}/>&nbsp;
                            <label for="rememberMe"><fmt:message key="rememberMe.label"/></label></td></tr>
                        <tr><td class="center" colspan="2"><button type="reset" class="pure-button pure-button-primary roundButton"><fmt:message key="reset"/></button>
                            &emsp;<button type="submit" class="pure-button pure-button-primary roundButton"><fmt:message key="login"/></button></td></tr>
                    </tbody>
                </table>
            </fieldset>
        </form><c:if test="${not empty members}"><br/>
        <table class="pure-table">
            <thead><tr><th><fmt:message key="account"/></th><th><fmt:message key="password"/></th></tr></thead>
          <c:forEach var="member" items="${members}"><tbody>
            <tr><td>${member.account}</td><td>${member.password}</td></tr>
          </c:forEach></tbody>
        </table></c:if>
    </center></body>
</html>
