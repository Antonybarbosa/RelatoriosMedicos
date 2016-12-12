/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package relatoriomedicos;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import Model.ConexaoEsp;
import java.util.logging.Level;
import java.util.logging.Logger;
import Control.GetDateTime;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import view.JDPrincipal;
import Control.CombForm;
import java.util.List;
import javax.swing.JOptionPane;
/**
 *
 * @author PHAIMBNOT003
 */
public class RelatorioMedicos extends javax.swing.JPanel {

    Map<String, Object> map1 = new HashMap<>();

    String sql;

    GetDateTime gdt = new GetDateTime();

    JDPrincipal jdp = new JDPrincipal(null, true);

    public void RelatorioEsp(String cdEsp, String dtIni, String dtFim, String First, String Esp) throws SQLException, JRException, ClassNotFoundException {

        sql = ("Select first " + First + " count(d.descr) AS QTD, d.descr AS Descricao from fc12100 a, fc12110 b, fc03000 d where a.nrcrm in "
                + " (Select nrcrm from fc04300 where cdesp = " + cdEsp + " and itemid = 1) "
                + " and a.cdfil = b.cdfil "
                + " and a.nrrqu = b.nrrqu "
                + " and b.tpcmp = 'C' "
                + " and b.cdprin=d.cdpro "
                + " and a.dtentr between '" + dtIni + "' and '" + dtFim + "' group by 2 order by 1 desc");

        map1.put("Especialidade", Esp);
        map1.put("dtinicio", dtIni);
        map1.put("dtfinal", dtFim);

        System.out.print(gdt.DateTime());
        jdp.startJDP(true);
        Executa();

    }

    public void Executa() {

        new Thread() {
            @Override

            public void run() {

                ConexaoEsp conecta = new ConexaoEsp();
                conecta.executaSQL(sql);
                JRResultSetDataSource relatResul = new JRResultSetDataSource(conecta.rs);
                JasperPrint print;
                try {
                    if (conecta.rs.next()) {

                        //print = JasperFillManager.fillReport("src/relatoriomedicos/reportEspSub.jasper", map1, relatResul);
                        print = JasperFillManager.fillReport("rel/reportEspSub.jasper", map1, relatResul);
                        JasperViewer ver = new JasperViewer(print, false);
                        ver.setTitle("Relatório Gerenciais");
                        jdp.closeJDP();
                        ver.setVisible(true);
                        ver.toFront();
                        conecta.rs.close();
                        System.out.print(gdt.DateTime());

                    } else {

                        print = JasperFillManager.fillReport("rel/reportEspSub.jasper", map1, relatResul);
                        JasperViewer ver = new JasperViewer(print, false);

                    }
                } catch (JRException | SQLException ex) {
                    Logger.getLogger(RelatorioMedicos.class.getName()).log(Level.SEVERE, null, ex);
                } finally {

                    jdp.closeJDP();

                }

            }
        }.start();

    }
    
    public void RelatorioComb(List<CombForm> la, String dtini, String dtfim, String nomesub) throws JRException{
        
        Map<String, Object> map = new HashMap<>();
        
        map.put("dataini", dtini);
        map.put("datafim", dtfim);
        map.put("nomesub", nomesub);
        
        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(la);
        
        
        try{
        //InputStream fonte = RelatorioMedicos.class.getResourceAsStream("./report1.jasper");
        
        //JasperReport report = JasperCompileManager.compileReport(fonte);
        //JasperPrint print = JasperFillManager.fillReport("src/relatoriomedicos/newReport.jasper",map,ds);
        JasperPrint print = JasperFillManager.fillReport("rel/newReport.jasper",map,ds);
        JasperViewer viewer = new JasperViewer(print, false);
        viewer.setVisible(true);
        viewer.toFront();
        }catch(Exception e){
            
            JOptionPane.showMessageDialog(this,e);
        }
        
    }

}
