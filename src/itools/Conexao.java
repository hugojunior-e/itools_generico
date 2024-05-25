package itools;

import java.sql.*;

@SuppressWarnings("deprecation")
public class Conexao {
    private String s_base;
    private String s_usuario;
    private String s_senha;
    private String s_aplicativos = "";
    private String s_menu = "";
    public  String last_sql_used = "";
    private Connection con = null;
    private Connection con_other = null;
    private PreparedStatement pst_audit = null;
    private String SQL_AUDITORIA   = "INSERT INTO ITOOLS_LOG(INFO, DETAILS, USERNAME, IP, CREATE_DT) VALUES (?,?,?,?,CURRENT_TIMESTAMP)";
    private String SQL_GET_CONTEXT = "SELECT * FROM ITOOLS_PAGES WHERE PAGE_TYPE ='<1>' AND ID = '<2>'";
    private String SQL_GET_SQL     = "SELECT * FROM ITOOLS_SQL   WHERE ID='<1>'";

    private String SQL_MENU        = "SELECT ia.*                          \n"+
                                     "  FROM ITOOLS_USERS iu,              \n"+
                                     "       ITOOLS_USERS_GROUPS iug,      \n"+
                                     "       ITOOLS_ACTIONS_GROUPS iag,    \n"+
                                     "       ITOOLS_ACTIONS ia             \n"+
                                     " WHERE iu.USERNAME = UPPER('<1>')    \n"+
                                     "   AND iug.USER_ID  = iu.ID          \n"+
                                     "   AND iag.GROUP_ID = iug.GROUP_ID   \n"+
                                     "   AND ia.ID        = iag.ACTION_ID  \n"+ 
                                     "union SELECT -99 ID, 'Sair' ACTION_NAME, 'Sair' action_menu, '/itools/' action_path, '-' action_rule FROM RDB$DATABASE rd";

    // **************************************************************************************************
    // **************************************************************************************************

    public void createConnection() throws Exception {
        this.con_other = null;
        ConexaoProperties prop = new ConexaoProperties(s_base);
        System.out.println(prop.url);
        if (prop.other) {
            ConexaoProperties prop2 = new ConexaoProperties("itools");
            this.con_other = java.sql.DriverManager.getConnection(prop2.url, prop2.user, prop2.pass);
        }

        this.con = java.sql.DriverManager.getConnection(prop.url, prop.user, prop.pass);
        this.pst_audit = (con_other != null ? con_other : con).prepareStatement(SQL_AUDITORIA);
    }



    public String getContextTela(String p_id, String p_tipo) throws Exception {

        if ( p_tipo.toLowerCase().equals("jsp") )
            auditaAcessoItools("PAGE_ID=" + p_id, "");

        System.out.println(p_id);

        java.sql.Statement st = (con_other != null ? con_other : con).createStatement();
        java.sql.ResultSet rs = st.executeQuery(SQL_GET_CONTEXT.replace("<1>",p_tipo).replace("<2>", p_id));
        String ret = "";

        while (rs.next()) {
            ret = rs.getString("PAGE_CODE");
        }

        rs.close();
        st.close();
        return ret;
    }

    public String getMenu() {
        return s_menu;
    }

    public String getAplicativos() {
        return s_aplicativos;
    }

    public void closeConnection() {
        try {
            getConnection().close();
        } catch (Exception exx) {
        }
    }

    public String getBase() {
        System.out.println("chegou");
        return s_base;
    }

    public void setBase(String value) throws Exception {
        s_base = value;
        System.out.println("chegou");
        createConnection();
    }

    public String getUsuario() {
        return s_usuario.toUpperCase();
    }

    public void setUsuario(String value) throws Exception {
        s_usuario = value;
    }

    public String getSenha() {
        return s_senha;
    }

    public void setSenha(String value) throws Exception {
        s_senha = value;
    }

    public java.sql.Connection getConnection() {
        return con;
    }

    private void auditaAcessoItools(String p_acesso, String p_parametros) {
        String callerIpAddress = "";

        try {
            callerIpAddress = uk.ltd.getahead.dwr.WebContextFactory.get().getHttpServletRequest().getRemoteAddr();
        } catch (Exception ex) {
        }

        try {
            pst_audit.setString(1, p_acesso);
            pst_audit.setString(2, p_parametros);
            pst_audit.setString(3, getUsuario());
            pst_audit.setString(4, callerIpAddress);
            pst_audit.execute();
        } catch (Exception ex) {
            UUtils.log_itools("0", "*", "Conexao.pegaSQL(auditoria)/ERRO=" + ex.getMessage());
        }
    }


    public String pegaSQL(String s_sqlname, String[] parameters) throws Exception {
        Statement st = (con_other != null ? con_other : con).createStatement();
        ResultSet rs = st.executeQuery(SQL_GET_SQL.replace("<1>",s_sqlname));
        String sql = "";
        String s_parametros = "";

        if (rs.next()) {
            long t = rs.getClob("SQL_CODE").length();
            sql = rs.getClob("SQL_CODE").getSubString((long) 1, (int) t);
        }

        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                s_parametros = s_parametros + "\n<" + (i + 1) + "> = " + parameters[i];
                sql = sql.replaceAll("<" + (i + 1) + ">", parameters[i]);
            }
        }

        rs.close();
        st.close();

        auditaAcessoItools("SQL_ID=" + s_sqlname, s_parametros);
        last_sql_used = sql.replace((char) 13, (char) 10);
        return sql.replace((char) 13, (char) 10);
    }



    // **************************************************************************************************
    // **************************************************************************************************
    

    public String validaUsuario(javax.servlet.http.HttpServletRequest rq) throws Exception {
        s_menu = criaMenu();
        return "SUCESSO";
    }



    public int validaAplicativo(String s_aplicativo) throws Exception {
        if (s_aplicativos.length() == 0) {
            return -1;

        } else if (s_aplicativos.indexOf("[" + s_aplicativo + "]") < 0) {
            return -2;
        }
        return 0;
    }



    public String criaMenu() {
        try {
            boolean rows = true;
            String acao = "";
            String menu = "";
            int f_categ = 0;
            Statement st = (con_other != null ? con_other : con).createStatement();
            String s_SQL = SQL_MENU.replace("<1>", s_usuario).replace("<2>", getBase() );
            ResultSet rs = st.executeQuery(s_SQL);
            rows = rs.next();
            acao = rs.getString("ACTION_MENU");
            int abertos = 0;
            int fechados = 0;

            menu = "[['" + acao + "', null, null,";
            abertos = 2;
            while (rows == true) {
                if (rs.getString("ACTION_MENU").compareTo(acao) != 0) {
                    acao = rs.getString("ACTION_MENU");
                    if (f_categ > 0) {
                        menu = menu + "]";
                        fechados = fechados + 1;
                    }
                    menu = menu + "],['" + acao + "',null,null,";
                    f_categ = 0;
                    abertos = abertos + 1;
                    fechados = fechados + 1;
                }

                menu = menu + "['" + rs.getString("ACTION_NAME") + "','" + rs.getString("ACTION_PATH") + "'],";
                abertos = abertos + 1;
                fechados = fechados + 1;
                rows = rs.next();
            }

            if (abertos > fechados) {
                abertos = abertos - fechados;
                for (int f = 0; f < abertos; f++)
                    menu = menu + "]";// ]]";
            }
            rs.close();
            st.close();
            return menu;

        } catch (Exception exx) {
            return "['MSG: " + exx.toString() + "']";
        }
    }
}
