/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import Control.CombForm;
import Control.LerPropriedade;
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
import javax.swing.JOptionPane;


/**
 *
 * @author PHAIMBNOT003
 */

public class ConexaoEsp {

    public Connection con = null;
    public PreparedStatement ps = null;
    public ResultSet rs = null;
    public Statement stm = null;
    
    public List<CombForm> lista = new ArrayList<CombForm>();
    LerPropriedade lp = new LerPropriedade();
        
    public ConexaoEsp() {
        String config[] = lp.Proprertiesler();
        
        try {

            Class.forName("org.firebirdsql.jdbc.FBDriver");
            con
                    = DriverManager.getConnection(
                           "jdbc:firebirdsql:10.10.1.20/3050:D:/Restore CMC/Banco Fcerta 31.01/E__/BancosDados/FcertaGradar/db/ALTERDB.IB",
                           "SYSDBA",
                            "masterkey");
//                     = DriverManager.getConnection("jdbc:firebirdsql:"+config[0]+"/3050:"+config[3],
//                            config[1],
//                             config[2]);
//
//            
            con.setAutoCommit(false);
        
        } catch (ClassNotFoundException | SQLException e) {
         
            JOptionPane.showInternalMessageDialog(null,"Não foi possível conectar ao banco: " + e.getMessage());
            
        }

    }

 
     public  void buscarMedico(){
            
            int i =0;
            int crm[] = new int[1900];
            int contQtdMedico =0;
            String SqlMed = "Select nrcrm from fc04300 where cdesp = 099 and itemid = 1 ";

            ResultSet rsm;
        try {
            PreparedStatement psm = con.prepareStatement(SqlMed,rs.CONCUR_READ_ONLY, rs.TYPE_SCROLL_INSENSITIVE);
            rsm = psm.executeQuery();
            while(rsm.next()){
            
                crm[i] = rsm.getInt("nrcrm");
                System.out.println(crm[i]);
                contQtdMedico++;
                i++;
            }
            
            System.out.println("Quantidade de medicos com essa Especialidade: "+contQtdMedico);
        } catch (SQLException ex) {
            Logger.getLogger(ConexaoEsp.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
     
     
     public String[] buscarEsp() {
        int n = 0;
        String[] DescEsp;

        int i = 0;

        String SqlEsp = "select subargum, parametro from fc99999 where argumento = 'ESPEC' order by 1 asc";

        ResultSet rsEsp;
        

        try {
            PreparedStatement psEsp = con.prepareStatement(SqlEsp, rs.CONCUR_READ_ONLY, rs.TYPE_SCROLL_INSENSITIVE);
            rsEsp = psEsp.executeQuery();

            while (rsEsp.next()) {

                n++;
            }
            psEsp.close();
            rsEsp.close();
        } catch (SQLException ex) {
            Logger.getLogger(ConexaoEsp.class.getName()).log(Level.SEVERE, null, ex);
        }

        String todos[] = new String[n];

        try {

            PreparedStatement psEsp = con.prepareStatement(SqlEsp, rs.CONCUR_READ_ONLY, rs.TYPE_SCROLL_INSENSITIVE);
            rsEsp = psEsp.executeQuery();

            while (rsEsp.next()) {

                todos[i] = rsEsp.getString("SUBARGUM") + rsEsp.getString("PARAMETRO");
                i++;
            }

        } catch (SQLException ex) {
            Logger.getLogger(ConexaoEsp.class.getName()).log(Level.SEVERE, null, ex);
        }
        DescEsp = todos;
        return DescEsp;
    }
     
     public void executaSQL(String sql){
     
         try {
             
            stm = con.createStatement(rs.TYPE_SCROLL_INSENSITIVE, rs.CONCUR_READ_ONLY);
            rs = stm.executeQuery(sql);
            
        } catch (Exception e) {
            
            Logger.getLogger(InterruptedException.class.getName()).log(Level.SEVERE, null, e);
            
        }
     
     
     }

}

   