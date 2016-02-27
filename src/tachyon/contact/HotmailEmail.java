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
public class HotmailEmail extends Email {

    public HotmailEmail(String u, String p) {
        super(u, p);
        Properties pro = getProperties();
        pro.put("mail.smtp.port", "587");
        pro.put("mail.smtp.host", "smtp.live.com");
        pro.put("mail.smtp.starttls.enable", "true");
        pro.put("mail.smtp.auth", "true");
    }
}
