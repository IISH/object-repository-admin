<%--
  Short view for ajax calls

  Structure:
  show
    - layout _show
  showremote
    - layout _show
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<g:render template="list" model="[instructionInstanceList,instructionInstanceTotal]"/>