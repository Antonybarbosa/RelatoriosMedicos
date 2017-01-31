/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Control;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author PHAIMBNOT003
 */
public class GetDateTime {

    public  String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmssmm");
        Date date = new Date();
        return dateFormat.format(date);
    }
    
    public  String DateTime() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy ");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
