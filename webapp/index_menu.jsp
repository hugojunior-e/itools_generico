<jsp:useBean id="conexao" class="itools.Conexao" scope="session"/>
<jsp:setProperty name="conexao" property="*"/> 

<% 
   String v = conexao.validaUsuario(request);
%>

<jsp:include page="index_header.jsp" flush="true" >
      <jsp:param name="app"  value="" />
      <jsp:param name="tit"  value="" />
</jsp:include>

<body>
      <div align="center">
            <pre>
            <%= v %>
            </pre>
      </div>
</body>