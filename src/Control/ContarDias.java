/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Control;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author PHAIMBNOT003
 * 
 * Recebe duas string no formato de data, para contar a quantidade de dias entre eles.
 */
public class ContarDias {
    
    public long Contardias(String datainicio, String datafinal){
                
        
                
                Date data1 = new Date(), data2 = new Date();
		Calendar c1 = Calendar.getInstance();
		//Pega a primeira data
		c1.set(Integer.parseInt(datainicio.substring(0, 4)), Integer.parseInt(datainicio.substring(4, 6)), Integer.parseInt(datainicio.substring(6, 8)));
		data1.setTime(c1.getTimeInMillis());
		//Pega a segunda data
		c1.set(Integer.parseInt(datafinal.substring(0, 4)), Integer.parseInt(datafinal.substring(4, 6)), Integer.parseInt(datafinal.substring(6, 8)));
		data2.setTime(c1.getTimeInMillis());
		Long dias = 1+((data2.getTime() - data1.getTime()) /1000/60/60/24);
                
                
                return dias;
    }
    
}
