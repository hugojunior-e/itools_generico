package itools;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import javax.servlet.jsp.JspWriter;
import java.io.*;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("deprecation")
public class UUtils {
    public static final int TAM_BUFFER      = 1024;
    public static String    path_root       = "";
    public static String    path_delimiter  = System.getProperty("file.separator");
    public static String    LOGIN_PROPERTIES = System.getProperty("itools.properties");
    

    public static String newFileName(String fn) throws Exception {
        return path_root + path_delimiter + "temp" + path_delimiter + fn;
    }

    public static String getValue(Object value) {
        return (value == null) ? "" : value.toString();
    }

    public static String getValue(Object value, String v_default) {
        return (value == null) ? v_default : value.toString();
    }

    public static String exportToXLS(ResultSet rs) throws Exception {
        return exportToXLS(rs, "Planilha1");
    }

    public static String exportToXLS(ResultSet rs, String nome_sheet) throws Exception {

        int i_linha = 1;
        int i_linha_idx = 1;
        long tm = new java.util.Date().getTime();
        String fnS = "URL_excel_" + tm + ".zip";
        String fn = "URL_excel_" + tm + ".xls";
        String fnf = newFileName(fn);
        int i_type;
        WritableWorkbook workbook = null;
        WritableSheet sheet = null;

        while (rs.next()) {
            if ((i_linha % 60000) == 0) {
                i_linha_idx++;
                i_linha = 1;
                sheet = workbook.createSheet("plan_" + i_linha_idx, i_linha_idx - 1);
            }
            if (i_linha == 1) {
                workbook = Workbook.createWorkbook(new File(fnf));
                sheet = workbook.createSheet("plan_1", 0);
                for (int a = 1; a <= rs.getMetaData().getColumnCount(); a++) {
                    Label label = new Label(a - 1, 0, rs.getMetaData().getColumnName(a));
                    sheet.addCell(label);
                }
            }

            for (int j = 1; j <= rs.getMetaData().getColumnCount(); j++) {
                i_type = rs.getMetaData().getColumnType(j);
                if (i_type == java.sql.Types.FLOAT || i_type == java.sql.Types.REAL
                        || i_type == java.sql.Types.NUMERIC) {
                    jxl.write.Number number = new jxl.write.Number(j - 1, i_linha, rs.getDouble(j));
                    sheet.addCell(number);
                } else {
                    Label label = new Label(j - 1, i_linha, rs.getString(j));
                    sheet.addCell(label);
                }

            }

            i_linha++;
        }
        if (i_linha == 1)
            return "@";
        workbook.write();
        workbook.close();

        zipa(newFileName(fnS), fnf);

        return "temp/" + fnS;
    }

    public static String exportToTXT(ResultSet rs) throws Exception {
        int i_linha = 0;
        long tm = new java.util.Date().getTime();
        String fnS = "URL_txt_" + tm + ".zip";
        String fn = "URL_txt_" + tm + ".csv";
        String fnf = newFileName(fn);
        String s_dados = "";
        String s_info = "";
        BufferedWriter bw = new BufferedWriter(new FileWriter(fnf));

        while (rs.next()) {
            i_linha++;

            if (i_linha == 1) {
                s_dados = "";
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    s_dados += "\"" + rs.getMetaData().getColumnName(i) + "\";";
                }

                bw.write(s_dados + "\n");
            }

            s_dados = "";

            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                int i_type = rs.getMetaData().getColumnType(i);
                s_info = ((rs.getObject(i) == null) ? "" : rs.getString(i));

                if (i_type == java.sql.Types.FLOAT || i_type == java.sql.Types.REAL
                        || i_type == java.sql.Types.NUMERIC) {
                    s_info = ((rs.getObject(i) == null) ? "0" : s_info);
                    s_info = s_info.replaceAll("\\.", ",");
                }
                s_dados += "\"" + s_info + "\";";
            }

            bw.write(s_dados + "\n");
        }
        bw.close();
        zipa(newFileName(fnS), fnf);
        return "temp/" + fnS;
    }

    public static void zipa(String outFileZip, String inFileZip) throws Exception {
        int cont;

        File f = null;

        FileInputStream fileinput = null;
        FileOutputStream fileoutput = null;
        BufferedInputStream buffer = null;
        ZipOutputStream zipar = null;
        ZipEntry entry = null;
        byte[] dados = new byte[TAM_BUFFER];

        fileoutput = new FileOutputStream(outFileZip);
        zipar = new ZipOutputStream(new BufferedOutputStream(fileoutput));

        f = new File(inFileZip);

        fileinput = new FileInputStream(f);
        buffer = new BufferedInputStream(fileinput, TAM_BUFFER);
        String fileInZip = inFileZip.substring(inFileZip.lastIndexOf(path_delimiter) + 1);
        entry = new ZipEntry(fileInZip);
        zipar.putNextEntry(entry);

        while ((cont = buffer.read(dados, 0, TAM_BUFFER)) != -1) {
            zipar.write(dados, 0, cont);
        }
        buffer.close();
        zipar.close();
        new File(inFileZip).delete();
    }

    public static void fillCombo(java.sql.Connection con, JspWriter out, String s_SQL) throws Exception {

        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(s_SQL);

        while (rs.next()) {
            out.println("<option value=\"" + rs.getString(1) + "\">" + rs.getString(2) + "</option>");
        }

        rs.close();
        st.close();
    }

    public static Conexao pegaConexao() {
        System.out.println("pegando conexao");
        return (Conexao) uk.ltd.getahead.dwr.WebContextFactory.get().getSession().getAttribute("conexao");
    }

    public static String pegaMenuPrincipal() throws Exception {
        String retorno = "";

        URL loginFile = new URL(LOGIN_PROPERTIES);
        BufferedReader br = new BufferedReader(new InputStreamReader(loginFile.openStream()));
        String aux = br.readLine();

        while (aux != null) {
            if (aux.startsWith("OPTION")) {
                String[] dados = aux.split("=");
                retorno = retorno + "\n" + "<option value=\"" + dados[0].substring(7) + "\">" + dados[1] + "</option>";
            }
            aux = br.readLine();
        }
        br.close();

        return retorno;
    }

    public static String pegaMensagem() throws Exception {
        String fileName = LOGIN_PROPERTIES;
        URL url = new URL(fileName);
        Properties prop = new Properties();
        prop.load(url.openStream());

        if (prop.containsKey("MENSAGEM"))
            return prop.getProperty("MENSAGEM");

        return "";

    }


    public static String enviaErro(Exception e, String msg) {
        return ("MSG: " + e.toString() + "\n\n" + msg).trim();
    }    

    public static String getUnique() {
        return "" + System.currentTimeMillis();
    }

    public static void log_itools(String i_id, String s_ponto, String msg) {
        System.out.println( "LOG ITOOLS [" + i_id + "] [" + s_ponto + "] - " + msg);
    }

    public static String jsp_name(String p_user, String p_classe) {
        return p_classe + (p_classe.endsWith(".jsp") ? "" : ".jsp");
    }
}
