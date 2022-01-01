<%-- 
    Author     : Kent Yeh
--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<%--Context path--%>
<c:set var="cp" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html><sec:authorize access="authenticated" var="logined"><sec:authorize access="hasRole('ROLE_ADMIN')" var="isAdmin"/><sec:authentication property="principal.username" var="username"/></sec:authorize>
<html>
    <head>
        <meta charset="utf-8"/><fmt:message key="hello" var="hello"/><fmt:message key="world" var="world"/>
        <title>${hello} ${empty username?world:username}!</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        <link rel="stylesheet" href="${cp}/static/purecss/build/pure-min.css" type="text/css" media="screen"/>
        <script type="text/javascript" src="${cp}/static/jquery/jquery.min.js"></script>
        <script><c:if test="${isAdmin}">
            function ajaxGetUser(path){
                $.ajax({
                    type : "POST",
                    url : "${cp}/admin/"+(path?path:"users"),
                    dataType : "json",
                    headers: {"${_csrf.headerName}":"${_csrf.token}"},
                    data: {},
                    cache: false,
                    error:function(jqXHR,  statusText){
                        alert("Exception prone when fetch users' data with "+statusText+":["+jqXHR.status+"]:"+jqXHR.statusText+"\n\t"+jqXHR.responseText);
                    },
                    success:function(data){
                        if(data.total==0){
                            $("#listuser").html("");
                        }else{
                            var html="<table class=\"pure-table\"><thead><tr><th><fmt:message key="account"/></th><th><fmt:message key="name"/></th><th><fmt:message key="birthday"/></th></tr></thead><tbody>";
                            for(var i=0;i<data.total;i++){
                                var user = data.users[i];
                                <c:if test="${isAdmin}">
                                html = html + "<tr><td><a class=\"pure-button pure-button-primary\" href=\"${cp}/member/edit/"+user.account+"\">&#128736;&nbsp;"+user.account+"</a></td><td>"+user.name+"</td><td>"+user.birthday+"</td></tr>";
                                </c:if>
                                <c:if test="${not isAdmin}">
                                html = html + "<tr><td>"+user.account+"</td><td>"+user.name+"</td><td>"+user.birthday+"</td></tr>";
                                </c:if>
                            }
                            html += "</tbody></table>";
                            $("#listuser").hide().html(html).show();
                        }
                    }
                });
            }</c:if><c:if test="${logined}">
            function like(){
                $.ajax({
                    type : "POST",
                    url : "${cp}/user/like",
                    dataType : "json",
                    headers: {"${_csrf.headerName}":"${_csrf.token}"},
                    data: {},
                    cache: false,
                    error:function(jqXHR,  statusText){
                        alert("Exception prone when fetch users like with "+statusText+":["+jqXHR.status+"]:"+jqXHR.statusText+"\n\t"+jqXHR.responseText);
                    },
                    success:function(data){
                        $("#like").html(data.count);
                    }
                });
            }
            function dislike(){
                $.ajax({
                    type : "POST",
                    url : "${cp}/user/dislike",
                    dataType : "json",
                    headers: {"${_csrf.headerName}":"${_csrf.token}"},
                    data: {},
                    cache: false,
                    error:function(jqXHR,  statusText){
                        alert("Exception prone when fetch users dislike with "+statusText+":["+jqXHR.status+"]:"+jqXHR.statusText+"\n\t"+jqXHR.responseText);
                    },
                    success:function(data){
                        $("#dislike").html(data.count);
                    }
                });
            }
        </c:if></script>
    </head>
    <body><c:if test="${not logined}"><h1 style="text-align:center">Server @ ${pageContext.request.localAddr}</h1></c:if>
        <table style="width:100%;position: fixed;top: 3px"><tbody>
            <c:if test="${logined}">
                <tr>
                    <td style="max-width: 200pt"><a href="${cp}/changePassword" class="pure-button pure-button-primary">
                            &#128273;&nbsp;<fmt:message key="changePassword"/></a>
                            &nbsp;<button class="pure-button pure-button-primary" onclick="like()">&#128077;<span id="like">${like}</span></button>
                    </td>
                    <td style="text-align: right">
                        <button class="pure-button pure-button-primary" onclick="dislike()">&#128078;<span id="dislike">${dislike}</span></button>&nbsp;
                        <form action="${cp}/j_spring_security_logout" method="post" style="display: inline">
                            <!--<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>-->
                            <sec:csrfInput />
                            <button class="pure-button pure-button-primary">
                                <sec:authentication property="principal.username"/>&nbsp;<fmt:message key="logout"/>&nbsp;&#10144;</button>
                        </form>
                    </td>
                </tr>
            </c:if>
            <c:if test="${not logined}">
                <tr>
                    <td align="right"><a href="${cp}/user/myinfo" class="pure-button pure-button-primary"><fmt:message key="myinfo"/></a></td>
                </tr>
            </c:if>
        </tbody></table>

    <center  style="display: block">
        <h1></h1>
        <c:if test="${not empty member}">
            <table id="myinfo" class="pure-table">
                <thead><tr><th colspan="2" style="text-align:center"><fmt:message key="myinfo"/></th></tr></thead>
                <tbody>
                    <tr><td><fmt:message key="account"/>:</td><td>${member.account}</td></tr>
                    <tr><td><fmt:message key="name"/>:</td><td>${member.name}</td></tr>
                    <tr><td><fmt:message key="enabled"/>:</td><td><c:if test="${'Y' eq member.enabled}"><fmt:message key="true"/></c:if>
                        <c:if test="${'Y' ne member.enabled}"><fmt:message key="false"/></c:if></td></tr>
                    <tr><td><fmt:message key="birthday"/>:</td><td><fmt:formatDate value="${member.birthday}" pattern="yyyy/MM/dd"/></td></tr>
                    <tr><td><fmt:message key="role"/>:</td><td>
                        <c:forEach var="authority" items="${member.authorities}">
                            ${authority.authority}&nbsp;
                        </c:forEach>
                    </td></tr>
                </tbody>
            </table>
        </c:if><br/>
        <sec:authorize access="hasRole('ROLE_ADMIN')" var="isAdmin">
        <fmt:message key="adminAjaxList" var="adminAjaxList"/>
        <fmt:message key="adminUserAjaxList" var="adminUserAjaxList"/>
        <input type="button" class="pure-button pure-button-primary" onclick="ajaxGetUser()" value="<c:out value="${adminAjaxList}" escapeXml="true"/>"/>
        <input type="button" class="pure-button pure-button-primary" onclick="ajaxGetUser('adminOrUsers')" value="<c:out value="${adminUserAjaxList}" escapeXml="true"/>"/>
        <div id="listuser" style="margin-top: 10px"></div></sec:authorize>
    </center>
</body>
</html>
