<%-- 
    Author     : Kent Yeh
--%>
<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<%--Context path--%><sec:authorize access="authenticated" var="logined"><sec:authorize access="hasRole('ROLE_ADMIN')" var="isAdmin"/><sec:authentication property="principal.username" var="username"/></sec:authorize>
<c:if test="${not logined}"><c:redirect url="/login"/></c:if>
<c:if test="${not isAdmin}"><c:redirect url="/user/myinfo"/></c:if>
<c:set var="cp" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html>
    <head>
        <title>&#9998;&nbsp;${member.name}</title>
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="${cp}/static/purecss/build/pure-min.css" type="text/css" media="screen"/>
        <script type="text/javascript" src="${cp}/static/jquery/jquery.min.js"></script>
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
            .roundButton{
                border-radius: 32px;
                text-shadow: 0 1px 1px rgba(0, 0, 0, 0.2);
            }
            <c:if test="${not empty requestScope.hint}">.hint {
                position: absolute;
                left:50%;
                top:33%;
                transform: translate(-50%, -50%);
                opacity:0.75;
                color:#00529B;
                background-color: #BDE5F8;
                font-size: 32px;
                border:1px solid red;
                z-index: 999;
            }</c:if>
        </style>
        <script type="text/javascript">
            var idx = 0;
            function addAuthority(form){
                var html= '<tr><td>'+
                        '<input type="hidden" name="authorities['+idx+'].account" value="${member.account}"/>'+
                        '<input type="text" name="authorities['+(idx++)+'].authority" width="30" maxlength="50"/>'+
                        '</td><td><button type="button" class="pure-button pure-button-primary roundButton" onclick="$(this).closest(\'tr\').remove();">'+
                        '-</button></td></tr>';
                $("tfoot").append($(html));
                form.elements['authorities['+(idx-1)+'].authority'].focus();
            }
            function loaded() {
                <c:if test="${not empty hint or not empty param.hint}">$('<div class="hint">${empty hint?param.hint:hint}</div>').appendTo(document.body).delay(1500).fadeOut("show");</c:if>
            }
        </script>
    </head>
    <body onload="loaded()">
        <table style="width:100%;border: none">
            <c:if test="${logined}">
                <tr>
                    <td><a href="${cp}/user/myinfo" class="pure-button pure-button-primary"><fmt:message key="myinfo"/></a></td>
                    <td align="right">
                        <form action="${cp}/j_spring_security_logout" method="post" style="display: inline">
                            <sec:csrfInput />
                            <button class="pure-button pure-button-primary">${username}&nbsp;<fmt:message key="logout"/>&nbsp;&#10144;</button>
                        </form>
                    </td>
                </tr>
            </c:if>
            <c:if test="${not logined}">
                <tr>
                    <td align="right"><a href="${cp}/user/myinfo" class="mdl-button mdl-js-button mdl-button--raised mdl-js-ripple-effect mdl-button--accent"><fmt:message key="myinfo"/></a></td>
                </tr>
            </c:if>
        </table>
        <form class="pure-form" action="${cp}/member/update" method="POST"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <c:if test="${not empty errorMsg}"><pre style="max-width:100%;text-align:left;color:red;margin: 10px auto;white-space: pre-wrap;word-wrap: break-word;"><fmt:message key='exception'/>:${errorMsg}</pre></c:if>
            <table style="margin-left: auto;margin-right: auto">
                <thead>
                    <tr><th colspan="2" style="text-align: center;font-size:14pt;" class="mdl-shadow--2dp"><input type="hidden" name="account" value="${member.account}">&#128736;&nbsp;${member.account}&nbsp;&#128736;
                        <input type="hidden" name="password" value="${member.password}"></th></tr></thead>
                <tbody>
                    <tr><td colspan="2" style="text-align: center"><input class="pure-button pure-button-primary roundButton" type="submit" value="<fmt:message key="updateUserData"><fmt:param value="${member.name}"/></fmt:message>"/></td></tr>
                    <tr><td colspan="2">
                            <label for="username"><fmt:message key="name"/></label>
                            <input type="text" id="username" name="name" value="${member.name}" maxlength="16" autofocus required/>
                      </td></tr>
                    <tr><td colspan="2">
                            <label for="enabledY"><fmt:message key="enabled"/></label>:
                      <label for="enabledY">
                          <input type="radio" name="enabled" value="Y" id="enabledY" ${"Y" eq member.enabled?"checked":""}/>
                          <span><fmt:message key="true"/></span>
                      </label>
                      <label for="enabledN">
                          <input type="radio" name="enabled" value="N" id="enabledN" ${"Y" ne member.enabled?"checked":""}/>
                          <span><fmt:message key="false"/></span>
                      </label>  
                      </td></tr>
                    <tr><td colspan="2">
                            <label for="birthday"><fmt:message key="birthday"/></label>
                            <input type="date" width="12" max="10" name="birthday" placeholder="YYYY/M/D" value="<fmt:formatDate pattern="yyyy-MM-dd" value="${member.birthday}"/>"/>
                      </td></tr>
                </tbody>
                <tfoot>
                    <tr><td><fmt:message key="role"/></td><td style="text-align: center"><button  type="button" class="pure-button pure-button-primary roundButton" onclick="addAuthority(this.form)"/>+</button></td></tr>
                            <c:forEach var="authority" items="${member.authorities}" varStatus="status">
                        <tr><td>
                                <input type="hidden" name="authorities[${status.index}].account" value="${member.account}"/>
                                <input type="text" name="authorities[${status.index}].authority" value="${authority.authority}" width="30" maxlength="50"/>
                            </td><td>
                                <button  type="button" class="pure-button pure-button-primary roundButton" onclick="$(this).closest('tr').remove();"/>-</button>
                            </td></tr>
                            <c:set var="idx" value="${status.count}"/>
                        </c:forEach>
                    <script type="text/javascript">idx=${idx};</script>
                </tfoot>
            </table>
        </form>
    </body>
</html>
