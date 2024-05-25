<jsp:useBean id="conexao" class="itools.Conexao" scope="session"/>


<%
    String tela    = conexao.getContextTela( request.getParameter("sc").toString() , "JSP" );
    String jsp_app = itools.UUtils.jsp_name(conexao.getUsuario(),request.getParameter("sc").toString());
    String pagina  = "temp/" + jsp_app;
    String s_ct    = "";

    java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(itools.UUtils.newFileName(jsp_app)));
    bw.write(tela);
    bw.close();

    if ( request.getParameter("scct") != null )
        response.setContentType(request.getParameter("scct").toString());
%>

<jsp:include page="<%= pagina %>" />
