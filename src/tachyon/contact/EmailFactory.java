/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.contact;

import java.util.List;

/**
 *
 * @author Aniket
 */
public class EmailFactory {

    public final static int GMAIL = 0;
    public final static int HOTMAIL = 1;

    private Email email;

    private EmailFactory() {
    }

    public static EmailFactory newEmailFactory() {
        return new EmailFactory();
    }

    public EmailFactory setCredentials(int flag, String us, String pas) {
        if (flag == GMAIL) {
            email = new GmailEmail(us, pas);
        } else if (flag == HOTMAIL) {
            email = new HotmailEmail(us, pas);
        }
        return this;
    }

    public EmailFactory addRecipient(String s) {
        email.addRecipient(s);
        return this;
    }

    public EmailFactory setRecipients(List<String> al) {
        for (String s : al) {
            email.addRecipient(s);
        }
        return this;
    }
    
    public EmailFactory setAttachments(List<String> al) {
        for (String f : al) {
            email.addAttachment(f);
        }
        return this;
    }

    public EmailFactory setSubject(String s) {
        email.setSubject(s);
        return this;
    }

    public EmailFactory setMessage(String s) {
        email.setMessage(s);
        return this;
    }

    public Email construct() {
        return email;
    }
}
