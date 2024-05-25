package itools;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class UUtilsTools {

    HttpServletResponseWrapper responseWrapper =  new HttpServletResponseWrapper(uk.ltd.getahead.dwr.WebContextFactory.get().getHttpServletResponse()) {
        private final java.io.StringWriter sw = new java.io.StringWriter();

        @Override
        public java.io.PrintWriter getWriter() throws java.io.IOException {
            return new java.io.PrintWriter(sw);
        }

        @Override
        public String toString() {
            return sw.toString();
        }
    };


    public String retornaTabelaHtml(String p_sql, String[] p_parametros, String[] p_campos) {
        String s_SQL = "";
        String i_id = UUtils.getUnique();

        UUtils.log_itools( i_id, ">", "UUtilsTools.retornaTabelaHtml/" + p_sql  );

        try {
            Conexao cx = UUtils.pegaConexao();

            Statement st1 = cx.getConnection().createStatement();
            StringBuilder s_ret = new StringBuilder();
            s_SQL = cx.pegaSQL(p_sql, p_parametros);
            ResultSet rs1 = st1.executeQuery(s_SQL);
            String s_f_agrup     = "";
            String s_f_agrup_atu = "";
            String s_f_agrup_ant = "";

            for (int i=0; i < p_campos.length; i++) {
                if (p_campos[i].startsWith("*")) {
                    s_f_agrup = p_campos[i].substring(1);
                }    
            }
            
            while (rs1.next()) {
                if (s_f_agrup.length() > 0)
                {
                    s_f_agrup_atu = s_f_agrup;
                    for (int j = 1; j <= rs1.getMetaData().getColumnCount(); j++)
                    {
                        String s_fnd = "[" + rs1.getMetaData().getColumnName(j) + "]";
                        while (s_f_agrup_atu.contains(s_fnd))
                        
                            s_f_agrup_atu = s_f_agrup_atu.replace(s_fnd, UUtils.getValue(rs1.getString(j)))  ;
                    }
                    if ( !s_f_agrup_atu.equals(s_f_agrup_ant) )
                    {
                        s_ret.append("<tr>");
                        s_ret.append("<td colspan=" + p_campos.length  + ">" + s_f_agrup_atu + "</td>");
                        s_ret.append("</tr>"); 
                        s_f_agrup_ant = s_f_agrup_atu;
                    }
                }

                s_ret.append("<tr>");
                for (int i=0; i < p_campos.length; i++)
                {
                    if (!p_campos[i].startsWith("*"))
                    {
                        String s_valor = "";
                        if (p_campos[i].startsWith("/"))
                        {
                            s_valor = p_campos[i].substring(1);
                            for (int j = 1; j <= rs1.getMetaData().getColumnCount(); j++)
                            {
                                String s_fnd = "[" + rs1.getMetaData().getColumnName(j) + "]";
                                while (s_valor.contains(s_fnd))
                                s_valor = s_valor.replace(s_fnd, UUtils.getValue(rs1.getString(j)))  ;
                            }
                        }
                        else
                            s_valor = UUtils.getValue(rs1.getString(p_campos[i])) ;

                        s_ret.append("<td>" + s_valor + "</td>");
                    }
                }
                s_ret.append("</tr>");
            }
            rs1.close();
            st1.close();
            UUtils.log_itools( i_id, "<", "UUtilsTools.retornaTabelaHtml/" + p_sql  );

            return s_ret.substring(0);

        } catch (Exception exx) {
            UUtils.log_itools( i_id, "E", "UUtilsTools.retornaTabelaHtml/ERRO=" + exx.getMessage()  );
            return UUtils.enviaErro(exx, s_SQL);
        }
    }    


    public String exportarDados(String p_sql, String p_tipo, String[] parametros) {
        String s_SQL = "";
        String i_id = UUtils.getUnique();

        UUtils.log_itools( i_id, ">", "UUtilsTools.exportarDados/" + p_sql  );

        try {

            Conexao ca = UUtils.pegaConexao();
            s_SQL = ca.pegaSQL(p_sql, parametros);
            Connection con = ca.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(s_SQL);
            String fn = "";

            if (p_tipo.equals("1"))
                fn = UUtils.exportToXLS(rs);

            if (p_tipo.equals("2"))
                fn = UUtils.exportToTXT(rs);

            rs.close();
            st.close();
            UUtils.log_itools( i_id, "<", "UUtilsTools.exportarDados/"  );

            return fn;
        } catch (Exception exx) {
            UUtils.log_itools( i_id, "E", "UUtilsTools.exportarDados/ERRO=" + exx.getMessage()  );
            return UUtils.enviaErro(exx, s_SQL);
        }
    }

    // ------------------------------------------------------------------
    // rotina generica que retorna um objeto TABLE
    // ------------------------------------------------------------------

    public String[] retornaRegistro(String p_sql, String[] parametros) {
        String i_id = UUtils.getUnique();

        UUtils.log_itools( i_id, ">", "UUtilsTools.retornaRegistro/" + p_sql );

        String s_SQL = "";
        String[] retorno;
        try {
            Conexao ccc = UUtils.pegaConexao();

            s_SQL = ccc.pegaSQL(p_sql, parametros);
            Connection con = ccc.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(s_SQL);
            retorno = new String[rs.getMetaData().getColumnCount() * 2];

            while (rs.next()) {
                for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                    retorno[2 * i] = rs.getMetaData().getColumnName(i + 1);
                    retorno[2 * i + 1] = rs.getString(i + 1);
                }
            }
            rs.close();
            st.close();
            UUtils.log_itools( i_id, "<", "UUtilsTools.retornaRegistro/" );

        } catch (Exception exx) {
            UUtils.log_itools( i_id, "*", "UUtilsTools.retornaRegistro/ERRO=" + exx );
            return null;
        }
        return retorno;
    }

    public Object[] retornaLinhaDados(String p_sql, String[] parametros) {
        String i_id = UUtils.getUnique();

        UUtils.log_itools( i_id, ">", "UUtilsTools.retornaLinhaDados/" + p_sql );

        String s_SQL = "";
        ArrayList<String> l_retorno = new ArrayList<>();
        
        try {
            Conexao ccc = UUtils.pegaConexao();

            s_SQL = ccc.pegaSQL(p_sql, parametros);
            Connection con = ccc.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(s_SQL);
            while (rs.next()) {
                l_retorno.add( rs.getString(1) );
            }
            rs.close();
            st.close();
            UUtils.log_itools( i_id, "<", "UUtilsTools.retornaLinhaDados/" );

        } catch (Exception exx) {
            UUtils.log_itools( i_id, "*", "UUtilsTools.retornaLinhaDados/ERRO=" + exx );
            return null;
        }

        return l_retorno.toArray();
    }


    // ------------------------------------------------------------------
    // rotina generica que executa um update
    // ------------------------------------------------------------------

    public String executaUpdate(String p_sql, String[] parametros) {
        String i_id = UUtils.getUnique();
        UUtils.log_itools( i_id, ">", "UUtilsTools.executaUpdate/" + p_sql );
        
        String s_SQL = "";
        try {
            Conexao ccc = UUtils.pegaConexao();
            s_SQL = ccc.pegaSQL(p_sql, parametros);
            ccc.getConnection().createStatement().executeUpdate(s_SQL);
            UUtils.log_itools( i_id, "<", "UUtilsTools.executaUpdate/"  );
            return "SUCESSO";

        } catch (Exception exx) {
            UUtils.log_itools( i_id, "*", "UUtilsTools.executaUpdate/ERRO=" + exx );
            return UUtils.enviaErro(exx, s_SQL);
        }
    }

    // ------------------------------------------------------------------
    // rotina generica que executa um java(JSP)
    // ------------------------------------------------------------------

    public String[] executaJava(String p_classe,
            String p_rotina,
            String[] parametros) {

        String i_id = UUtils.getUnique();                
        UUtils.log_itools(i_id, ">" ,"UUtilsTools.executaJava/" + p_classe + "/" + p_rotina  );

        String att_in = p_classe + "." + p_rotina + ".in";
        String att_out = p_classe + "." + p_rotina + ".out";

        try {
            HttpServletRequest request = uk.ltd.getahead.dwr.WebContextFactory.get().getHttpServletRequest();
            HttpServletResponse response = uk.ltd.getahead.dwr.WebContextFactory.get().getHttpServletResponse();

            responseWrapper.setResponse(response);
        
            
            Conexao cx = UUtils.pegaConexao();

            String tela = cx.getContextTela(p_classe, "JAVA");
            String jsp_app = UUtils.jsp_name(  cx.getUsuario(), p_classe + "_" + p_rotina   );

            java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(itools.UUtils.newFileName(jsp_app)));
            int i1 = tela.indexOf("<header>");
            int i2 = tela.indexOf("</header>");
            bw.write(tela.substring(i1 + 8, i2 - 2));

            bw.write("\n");

            i1 = tela.indexOf("<" + p_rotina + ">");
            i2 = tela.indexOf("</" + p_rotina + ">");
            bw.write(tela.substring(i1 + p_rotina.length() + 1, i2 - 1));

            bw.close();


            request.setAttribute(att_in, parametros);

            request.getRequestDispatcher("/temp/" + jsp_app).include(request, responseWrapper);
            
            String[] retorno = (String[])request.getAttribute(att_out);
            
            File f = new File(itools.UUtils.newFileName(jsp_app));
            if (f.exists())
                f.delete();

            UUtils.log_itools(i_id, "<" ,"UUtilsTools.executaJava/" );                

            // -------------------------------------------------
            // limpando objetos nao utilizados
            // -------------------------------------------------

            
            try {
                request.removeAttribute(att_in);
                request.removeAttribute(att_out);
                System.gc();
            } catch(Exception exx) {
                UUtils.log_itools(i_id, "*", "UUtilsTools.executaJava(rm att)/ERRO=" + exx.getMessage() );
            }

            return retorno;
        } catch (Exception exx) {
            UUtils.log_itools(i_id, "*", "UUtilsTools.executaJava/ERRO=" + exx.getMessage() );
            return new String[] { "ERRO:" + exx.getMessage() };
        }
    }

}
