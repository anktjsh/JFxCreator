/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.contact;

import java.util.Properties;

/**
 *
 * @author Aniket
 */
public class GmailEmail extends Email {

    public GmailEmail(String u, String p) {
        super(u, p);
        Properties pro = getProperties();
        pro.put("mail.smtp.auth", "true");
        pro.put("mail.smtp.starttls.enable", "true");
        pro.put("mail.smtp.host", "smtp.gmail.com");
        pro.put("mail.smtp.port", "587");
    }
}
