/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import Control.CombForm;
import Control.LerPropriedade;
import net.sf.jasperreports.engine.JRException;
import relatoriomedicos.RelatorioMedicos;
import view.JDPrincipal;

/**
 *
 * @author PHAIMBNOT003
 */
public class ConexaoComb {
    
    String SQLRQ1;
    String SQLRQ2;
    String SQLRQ3;
    String SQLRQ4;
    String SQLRQ5;
    int validarelatorio = 0;
        public Connection con = null;
        public PreparedStatement ps = null;
        public ResultSet rs = null;
        public Statement stm = null;
    String dtInicial;
    String dtFinal;
    String Subnomes;
    JDPrincipal jdp = new JDPrincipal(null, true);
    public List<CombForm> lista = new ArrayList<CombForm>();
    LerPropriedade lp = new LerPropriedade();
    public ConexaoComb() {
        String config[] = lp.Proprertiesler();
        try {

            Class.forName("org.firebirdsql.jdbc.FBDriver");
            con
                    = DriverManager.getConnection("jdbc:firebirdsql:"+config[0]+"/3050:"+config[3],
                            config[1],
                            config[2]);

            System.out.println("Conexão ok");
            con.setAutoCommit(false);
        } catch (Exception e) {

            System.out.println("Não foi possível conectar ao banco: " + e.getMessage());

        }

    }

    public void insertSql(int cdfil, int nrrqu, String serier, int itemid, InputStream ficha, String maquina) {

        String sql = "INSERT INTO MONFICHAS (CDFIL, NRRQU, SERIER, ITEMID, FICHA, MAQUINA) VALUES (?, ?, ?, ?, ?, ?)";

        try {

            ps = con.prepareStatement(sql);

            ps.setInt(1, cdfil);
            ps.setInt(2, nrrqu);
            ps.setString(3, serier);
            ps.setInt(4, itemid);
            ps.setBlob(5, ficha);
            ps.setString(6, maquina);

            ps.execute();
            con.commit();

        } catch (SQLException ex) {

            Logger.getLogger(ConexaoComb.class.getName()).log(Level.SEVERE, null, ex);

        } finally {

            try {

                ps.close();
                con.close();

            } catch (SQLException ex) {

                Logger.getLogger(ConexaoComb.class.getName()).log(Level.SEVERE, null, ex);

            }

        }

    }

    void relreq(String dtIni, String dtFim, String nomes) {

        RelatorioMedicos rl = new RelatorioMedicos();

        try {

            rl.RelatorioComb(lista, dtIni, dtFim, nomes);

        } catch (JRException ex) {

            Logger.getLogger(ConexaoComb.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro ao enviar dados para o relatorio" );
        }

    }
    
    public void consultaRQ1(int req1, String dtIni, String dtFim, String nomes)throws SQLException, JRException{
        
        dtInicial = dtIni;
        dtFinal = dtFim;
        Subnomes = nomes;
        
        jdp.startJDP(true);
        
        SQLRQ1 = "SELECT NRRQU, CDFIL FROM FC12110 WHERE cdprin = " + req1 + "  and dtentr between '" + dtIni + "' and '" + dtFim + "' and tpcmp in('C','F') group by 1,2 "; 
        
        new Thread() {
            

            public void run() {
                PreparedStatement ps;
                ResultSet rs = null;

                int n = 0;
                int i = 0;
                BigDecimal ValorReal = new BigDecimal("0");

                try {
                   
                    ps = con.prepareStatement(SQLRQ1, rs.CONCUR_READ_ONLY, rs.TYPE_SCROLL_INSENSITIVE);
                    rs = ps.executeQuery();
                    while (rs.next()) {

                        n++;

                    }
                    //n=n+1;
                    System.err.println(n);

                } catch (SQLException ex) {

                    Logger.getLogger(ConexaoComb.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Erro ao percorrer preparedstatement 1" );

                }
                
                int nrrqu[] = new int[n];
                int cdfil[] = new int[n];
                String nomePro;

                try {
                    ps = con.prepareStatement(SQLRQ1, rs.CONCUR_READ_ONLY, rs.TYPE_SCROLL_INSENSITIVE);
                    rs = ps.executeQuery();

                    while (rs.next()) {

                        nrrqu[i] = rs.getInt("nrrqu");
                        cdfil[i] = rs.getInt("cdfil");

                        System.out.println(cdfil[i] + " " + nrrqu[i]);
                        i++;

                    }

                    ps.close();
                    rs.close();

                } catch (SQLException ex) {

                    Logger.getLogger(ConexaoComb.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Erro ao percorrer preparedstatement 2" );
                }

                String SQLMD = "SELECT distinct a.VRLIQDAV, d.NOMEFUN FROM FC12100 a, FC04000 b, fc04200 c, fc08000 d  where a.nrrqu = ?  and a.cdfil = ?"
                        + "and b.nrcrm = c.nrcrm and c.cdfun = d.cdfun and c.CDCON = 100 and a.NRCRM = c.NRCRM and c.CDCON = d.CDCON";
                try {
                    ResultSet rs1;
                    PreparedStatement ps1;

                    ps1 = con.prepareStatement(SQLMD, rs.CONCUR_READ_ONLY, rs.TYPE_SCROLL_INSENSITIVE);

                    for (int t = 0; t < nrrqu.length; t++) {

                        ps1.setInt(1, nrrqu[t]);
                        ps1.setInt(2, cdfil[t]);
                        rs1 = ps1.executeQuery();

                        if (rs1.next()) {
                            CombForm cf = new CombForm();
                            nomePro = rs1.getString("NOMEFUN");

                            BigDecimal recall = new BigDecimal(rs1.getBigDecimal("VRLIQDAV").toString());
                            BigDecimal Valor = new BigDecimal("0");
                            Valor = Valor.add(recall);
                            ValorReal = ValorReal.add(Valor);
                            cf.setNomepro(nomePro);
                            cf.setValor(recall);
                            System.out.println(nomePro + " " + recall);

                            lista.add(cf);

                        }

                    }
                    
                    jdp.closeJDP();
                    
                    relreq(dtInicial, dtFinal, Subnomes);

                } catch (SQLException ex) {

                    Logger.getLogger(ConexaoComb.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Erro ao percorrer preparedstatement 3" );
                }
            }
        }.start();

    
    }

    public void consultaRQ2(int req1, int req2, String dtIni, String dtFim, String nomes) throws SQLException, JRException {

        dtInicial = dtIni;
        dtFinal = dtFim;
        Subnomes = nomes;

        jdp.startJDP(true);

        SQLRQ2
                = "SELECT NRRQU, CDFIL FROM FC12110 WHERE cdprin = " + req1 + " or cdprin = " + req2 + " and dtentr between '" + dtIni + "' and '" + dtFim + "' and tpcmp in('C','F') group by 1,2 HAVING COUNT(NRRQU)>1 ";

        new Thread() {

            public void run() {
                PreparedStatement ps;
                ResultSet rs = null;

                int n = 0;
                int i = 0;
                BigDecimal ValorReal = new BigDecimal("0");

                try {
                    ps = con.prepareStatement(SQLRQ2, rs.CONCUR_READ_ONLY, rs.TYPE_SCROLL_INSENSITIVE);
                    rs = ps.executeQuery();

                    while (rs.next()) {

                        n++;

                    }
                    //n=n+1;
                    System.err.println(n);

                } catch (SQLException ex) {

                    Logger.getLogger(ConexaoComb.class.getName()).log(Level.SEVERE, null, ex);

                }

                int nrrqu[] = new int[n];
                int cdfil[] = new int[n];
                String nomePro;

                try {
                    ps = con.prepareStatement(SQLRQ2, rs.CONCUR_READ_ONLY, rs.TYPE_SCROLL_INSENSITIVE);
                    rs = ps.executeQuery();

                    while (rs.next()) {

                        nrrqu[i] = rs.getInt("nrrqu");
                        cdfil[i] = rs.getInt("cdfil");

                        System.out.println(cdfil[i] + " " + nrrqu[i]);
                        i++;

                    }

                    ps.close();
                    rs.close();

                } catch (SQLException ex) {

                    Logger.getLogger(ConexaoComb.class.getName()).log(Level.SEVERE, null, ex);

                }

                String SQLMD = "SELECT distinct a.VRLIQDAV, d.NOMEFUN FROM FC12100 a, FC04000 b, fc04200 c, fc08000 d  where a.nrrqu = ?  and a.cdfil = ?"
                        + "and b.nrcrm = c.nrcrm and c.cdfun = d.cdfun and c.CDCON = 100 and a.NRCRM = c.NRCRM and c.CDCON = d.CDCON";
                try {
                    ResultSet rs1;
                    PreparedStatement ps1;

                    ps1 = con.prepareStatement(SQLMD, rs.CONCUR_READ_ONLY, rs.TYPE_SCROLL_INSENSITIVE);

                    for (int t = 0; t < nrrqu.length; t++) {

                        ps1.setInt(1, nrrqu[t]);
                        ps1.setInt(2, cdfil[t]);
                        rs1 = ps1.executeQuery();

                        if (rs1.next()) {
                            CombForm cf = new CombForm();
                            nomePro = rs1.getString("NOMEFUN");

                            BigDecimal recall = new BigDecimal(rs1.getBigDecimal("VRLIQDAV").toString());
                            BigDecimal Valor = new BigDecimal("0");
                            Valor = Valor.add(recall);
                            ValorReal = ValorReal.add(Valor);
                            cf.setNomepro(nomePro);
                            cf.setValor(recall);
                            System.out.println(nomePro + " " + recall);

                            lista.add(cf);

                        }

                    }
                    
                    jdp.closeJDP();
                    
                    relreq(dtInicial, dtFinal, Subnomes);

                } catch (SQLException ex) {

                    Logger.getLogger(ConexaoComb.class.getName()).log(Level.SEVERE, null, ex);

                }
            }
        }.start();

    }

    public void consultaRQ3(int req1, int req2, int req3, String dtIni, String dtFim, String nomes) throws SQLException, JRException {

        dtInicial = dtIni;
        dtFinal = dtFim;
        Subnomes = nomes;

        jdp.startJDP(true);

        SQLRQ3
                = "SELECT NRRQU, CDFIL FROM FC12110 WHERE cdprin = " + req1 + " or cdprin = " + req2 + " or cdprin = " + req3 + " and dtentr between '" + dtIni + "' and '" + dtFim + "' and tpcmp in('C','F') group by 1,2 HAVING COUNT(NRRQU)>2 ";

        new Thread() {

            public void run() {

                PreparedStatement ps;
                ResultSet rs = null;

                int n = 0;
                int i = 0;
                BigDecimal ValorReal = new BigDecimal("0");
                //String SQLRQ
                //= "SELECT NRRQU, CDFIL FROM FC12110 WHERE cdprin = 11393 or cdprin = 4647 or cdprin = 5270 and dtentr between '01.06.2016' and '30.06.2016' and tpcmp in('C','F') group by 1,2 HAVING COUNT(NRRQU)>2 ";

                try {
                    ps = con.prepareStatement(SQLRQ3, rs.CONCUR_READ_ONLY, rs.TYPE_SCROLL_INSENSITIVE);
                    rs = ps.executeQuery();

                    while (rs.next()) {

                        n++;

                    }
                    //n=n+1;
                    System.err.println(n);

                } catch (SQLException ex) {

                    Logger.getLogger(ConexaoComb.class.getName()).log(Level.SEVERE, null, ex);

                }

                int nrrqu[] = new int[n];
                int cdfil[] = new int[n];
                String nrcrm;

                try {
                    ps = con.prepareStatement(SQLRQ3, rs.CONCUR_READ_ONLY, rs.TYPE_SCROLL_INSENSITIVE);
                    rs = ps.executeQuery();

                    while (rs.next()) {

                        nrrqu[i] = rs.getInt("nrrqu");
                        cdfil[i] = rs.getInt("cdfil");

                        System.out.println(cdfil[i] + " " + nrrqu[i]);
                        i++;

                    }

                    ps.close();
                    rs.close();

                } catch (SQLException ex) {

                    Logger.getLogger(ConexaoComb.class.getName()).log(Level.SEVERE, null, ex);

                }

                String SQLMD = "SELECT distinct a.VRLIQDAV, d.NOMEFUN FROM FC12100 a, FC04000 b, fc04200 c, fc08000 d  where a.nrrqu = ?  and a.cdfil = ?"
                        + "and b.nrcrm = c.nrcrm and c.cdfun = d.cdfun and c.CDCON = 100 and a.NRCRM = c.NRCRM and c.CDCON = d.CDCON";
                try {
                    ResultSet rs1;
                    PreparedStatement ps1 = con.prepareStatement(SQLMD, rs.CONCUR_READ_ONLY, rs.TYPE_SCROLL_INSENSITIVE);

                    for (int t = 0; t < nrrqu.length; t++) {

                        ps1.setInt(1, nrrqu[t]);
                        ps1.setInt(2, cdfil[t]);
                        rs1 = ps1.executeQuery();

                        if (rs1.next()) {
                            CombForm cf = new CombForm();
                            nrcrm = rs1.getString("NOMEFUN");

                            BigDecimal recall = new BigDecimal(rs1.getBigDecimal("VRLIQDAV").toString());
                            BigDecimal Valor = new BigDecimal("0");
                            Valor = Valor.add(recall);
                            ValorReal = ValorReal.add(Valor);
                            cf.setNomepro(nrcrm);
                            cf.setValor(recall);
                            System.out.println(nrcrm + " " + recall);

                            lista.add(cf);

                        }

                    }
                    
                    jdp.closeJDP();
                    sleep(2000);
                    relreq(dtInicial, dtFinal, Subnomes);

                } catch (SQLException | InterruptedException ex) {

                    Logger.getLogger(ConexaoComb.class.getName()).log(Level.SEVERE, null, ex);

                }
            }
        }.start();

    }

    public void consultaRQ4(int req1, int req2, int req3, int req4, String dtIni, String dtFim, String nomes) throws SQLException, JRException {
        
        dtInicial = dtIni;
        dtFinal = dtFim;
        Subnomes = nomes;
        SQLRQ4 =  "SELECT NRRQU, CDFIL FROM FC12110 WHERE cdprin = " + req1 + " or cdprin = " + req2 + " or cdprin = " + req3 + " or cdprin =" + req4 + " and dtentr between '" + dtIni + "' and '" + dtFim + "' and tpcmp in('C','F') group by 1,2 HAVING COUNT(NRRQU)>3 ";
        jdp.startJDP(true);
        
    new Thread() {

            public void run() {
        PreparedStatement ps;
        ResultSet rs = null;

        int n = 0;
        int i = 0;
        BigDecimal ValorReal = new BigDecimal("0");
        //String SQLRQ
        //       = "SELECT NRRQU, CDFIL FROM FC12110 WHERE cdprin = 11393 or cdprin = 4647 or cdprin = 5270 and dtentr between '01.06.2016' and '30.06.2016' and tpcmp in('C','F') group by 1,2 HAVING COUNT(NRRQU)>2 ";

       
                

        try {
            ps = con.prepareStatement(SQLRQ4, rs.CONCUR_READ_ONLY, rs.TYPE_SCROLL_INSENSITIVE);
            rs = ps.executeQuery();

            while (rs.next()) {

                n++;

            }
            //n=n+1;
            System.err.println(n);

        } catch (SQLException ex) {

            Logger.getLogger(ConexaoComb.class.getName()).log(Level.SEVERE, null, ex);

        }

        int nrrqu[] = new int[n];
        int cdfil[] = new int[n];
        String nrcrm;
       

        try {
            ps = con.prepareStatement(SQLRQ4, rs.CONCUR_READ_ONLY, rs.TYPE_SCROLL_INSENSITIVE);
            rs = ps.executeQuery();

            while (rs.next()) {

                nrrqu[i] = rs.getInt("nrrqu");
                cdfil[i] = rs.getInt("cdfil");

                System.out.println(cdfil[i] + " " + nrrqu[i]);
                i++;

            }

            ps.close();
            rs.close();

        } catch (SQLException ex) {

            Logger.getLogger(ConexaoComb.class.getName()).log(Level.SEVERE, null, ex);

        }

        String SQLMD = "SELECT distinct a.VRLIQDAV, d.NOMEFUN FROM FC12100 a, FC04000 b, fc04200 c, fc08000 d  where a.nrrqu = ?  and a.cdfil = ?"
                + "and b.nrcrm = c.nrcrm and c.cdfun = d.cdfun and c.CDCON = 100 and a.NRCRM = c.NRCRM and c.CDCON = d.CDCON";
        
        try{
        ResultSet rs1;
        PreparedStatement ps1 = con.prepareStatement(SQLMD, rs.CONCUR_READ_ONLY, rs.TYPE_SCROLL_INSENSITIVE);

        for (int t = 0; t < nrrqu.length; t++) {

            ps1.setInt(1, nrrqu[t]);
            ps1.setInt(2, cdfil[t]);
            rs1 = ps1.executeQuery();

            if (rs1.next()) {
                CombForm cf = new CombForm();
                nrcrm = rs1.getString("NOMEFUN");

                BigDecimal recall = new BigDecimal(rs1.getBigDecimal("VRLIQDAV").toString());
                BigDecimal Valor = new BigDecimal("0");
                Valor = Valor.add(recall);
                ValorReal = ValorReal.add(Valor);
                cf.setNomepro(nrcrm);
                cf.setValor(recall);
                System.out.println(nrcrm + " " + recall);

                lista.add(cf);

            }

        }
        
            
            
            jdp.closeJDP();
            sleep(2000);
            relreq(dtInicial, dtFinal, Subnomes);
            
        }catch (SQLException | InterruptedException ex) {

            Logger.getLogger(ConexaoComb.class.getName()).log(Level.SEVERE, null, ex);

        }
            }}.start();
        
        

    }

    public void consultaRQ5(int req1, int req2, int req3, int req4, int req5, String dtIni, String dtFim, String nomes) throws SQLException, JRException {
        
        dtInicial = dtIni;
        dtFinal = dtFim;
        Subnomes = nomes;
        jdp.startJDP(true);
        SQLRQ5 = "SELECT NRRQU, CDFIL FROM FC12110 WHERE cdprin = " + req1 + " or cdprin = " + req2 + " or cdprin = " + req3 + " or cdprin =" + req4 + " or cdprin = " + req5 + " and dtentr between '" + dtIni + "' and '" + dtFim + "' and tpcmp in('C','F') group by 1,2 HAVING COUNT(NRRQU)>4 ";
        
    new Thread() {

            public void run() {    
        
        PreparedStatement ps;
        ResultSet rs = null;

        int n = 0;
        int i = 0;
        BigDecimal ValorReal = new BigDecimal("0");
        //String SQLRQ
        //       = "SELECT NRRQU, CDFIL FROM FC12110 WHERE cdprin = 11393 or cdprin = 4647 or cdprin = 5270 and dtentr between '01.06.2016' and '30.06.2016' and tpcmp in('C','F') group by 1,2 HAVING COUNT(NRRQU)>2 ";

        
                

        try {
            ps = con.prepareStatement(SQLRQ5, rs.CONCUR_READ_ONLY, rs.TYPE_SCROLL_INSENSITIVE);
            rs = ps.executeQuery();

            while (rs.next()) {

                n++;

            }
            //n=n+1;
            System.err.println(n);

        } catch (SQLException ex) {

            Logger.getLogger(ConexaoComb.class.getName()).log(Level.SEVERE, null, ex);

        }

        int nrrqu[] = new int[n];
        int cdfil[] = new int[n];
        String nrcrm;

        try {
            ps = con.prepareStatement(SQLRQ5, rs.CONCUR_READ_ONLY, rs.TYPE_SCROLL_INSENSITIVE);
            rs = ps.executeQuery();

            while (rs.next()) {

                nrrqu[i] = rs.getInt("nrrqu");
                cdfil[i] = rs.getInt("cdfil");

                System.out.println(cdfil[i] + " " + nrrqu[i]);
                i++;

            }

            ps.close();
            rs.close();

        } catch (SQLException ex) {

            Logger.getLogger(ConexaoComb.class.getName()).log(Level.SEVERE, null, ex);

        }

        String SQLMD = "SELECT distinct a.VRLIQDAV, d.NOMEFUN FROM FC12100 a, FC04000 b, fc04200 c, fc08000 d  where a.nrrqu = ?  and a.cdfil = ?"
                + "and b.nrcrm = c.nrcrm and c.cdfun = d.cdfun and c.CDCON = 100 and a.NRCRM = c.NRCRM and c.CDCON = d.CDCON";
        try{
        ResultSet rs1;
        PreparedStatement ps1 = con.prepareStatement(SQLMD, rs.CONCUR_READ_ONLY, rs.TYPE_SCROLL_INSENSITIVE);

        for (int t = 0; t < nrrqu.length; t++) {

            ps1.setInt(1, nrrqu[t]);
            ps1.setInt(2, cdfil[t]);
            rs1 = ps1.executeQuery();

            if (rs1.next()) {
                CombForm cf = new CombForm();
                nrcrm = rs1.getString("NOMEFUN");

                BigDecimal recall = new BigDecimal(rs1.getBigDecimal("VRLIQDAV").toString());
                BigDecimal Valor = new BigDecimal("0");
                Valor = Valor.add(recall);
                ValorReal = ValorReal.add(Valor);
                cf.setNomepro(nrcrm);
                cf.setValor(recall);
                System.out.println(nrcrm + " " + recall);

                lista.add(cf);

            }

        }
        
            jdp.closeJDP();
            sleep(2000);
            relreq(dtInicial, dtFinal, Subnomes);
        
        
        }catch (SQLException | InterruptedException ex) {

            Logger.getLogger(ConexaoComb.class.getName()).log(Level.SEVERE, null, ex);

        }
            }}.start();
        

    }

    public String buscarSubstancia(int cod) throws SQLException {

        String prod;

        String sqlnome = "select descrprd from fc03000 where cdpro = ? ";

        ResultSet rsnome = null;

        PreparedStatement psnome = con.prepareStatement(sqlnome, rs.CONCUR_READ_ONLY, rs.TYPE_SCROLL_INSENSITIVE);

        psnome.setInt(1, cod);
        rsnome = psnome.executeQuery();

        if (rsnome.first()) {

            prod = rsnome.getString("descrprd");

        } else {

            prod = "vazio";

        }

        return prod;
    }
}
