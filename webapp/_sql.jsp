<jsp:useBean id="conexao" class="itools.Conexao" scope="session" />  

<pre>
<%= conexao.last_sql_used %>
</pre>