<jsp:useBean id="conexao" class="itools.Conexao" scope="session"/>

<% 
	Object p_somente_acesso = request.getParameter("acesso");
	Object p_css            = request.getParameter("p_css");
	Object p_js             = request.getParameter("p_js");
	Object p_no_menu        = request.getParameter("no_menu");
	String p_titulo         = request.getParameter("tit").toString();
	String p_app            = request.getParameter("app").toString();


	String s_css = "";
	String s_js  = "";

	if (p_css != null)
	{ 
		String[] l_css = p_css.toString().split(";");
		for (int i=0; i < l_css.length; i++)
		   s_css += conexao.getContextTela( l_css[i] , "CSS" ) ;
	}
	else
	{
		s_css += conexao.getContextTela( "itools.css" , "CSS" ) ;
	}
	

	s_js = 	conexao.getContextTela( "itools.js" , "JS" ) ;

	if (p_js != null)
	{ 
		String[] l_js = p_js.toString().split(";");
		for (int i=0; i < l_js.length; i++)
	      s_js += conexao.getContextTela( l_js[i] , "JS" );
	}


	if (p_titulo.length() > 0)  
		p_titulo = "<h2><center>" +  p_titulo + "</center></h2>";

	if ( p_app.length() > 0 )
	{
		try
		{
			int ret = conexao.validaAplicativo(p_app);
			if (ret != 0)
				out.println("<script>alert('"+(ret == -1 ? "Acesso ao Interface Tools negado":"Acesso a este menu negado")+"'); window.location.href='/itools/index.jsp'; </script>");
		}
		catch(Exception exx)
		{
			out.println("<script>alert('Sua sessao expirou favor logar novamente'); window.location.href='/itools/index.jsp'; </script>");
			return;
		}
	}
%>
	
<HEAD>
	<title>Raimundo Loc Fest</title>

	<style>  
	    <%= s_css %>
	</style> 

	<script>
	    <%= s_js %>
	</script>
	
	<script type='text/javascript' src='/itools/dwr/engine.js'></script>
	<script type='text/javascript' src='/itools/dwr/util.js'></script>
	<script type='text/javascript' src='/itools/dwr/interface/UUtilsTools.js'></script>
	<% 
		if ( p_no_menu == null )
		{
		%>
			<script language=JavaScript src=/itools/js/menu.js></script>
			<script language=JavaScript> var MENU_ITEMS = <%= conexao.getMenu() %>; </script>
			<script language=JavaScript src=/itools/js/menu_tpl.js></script>
			<script language=JavaScript> new menu (MENU_ITEMS, MENU_POS); </script>
		<%
		}
	%>
</HEAD>

<% 
  if (p_somente_acesso != null) 
    return;
%>
