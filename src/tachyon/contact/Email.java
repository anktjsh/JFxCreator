/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.contact;

import java.util.ArrayList;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author Aniket
 */
public abstract class Email {

    private final String username;
    private final String password;
    private final Properties pro;

    private final ArrayList<String> to;
    private final ArrayList<String> attachments;
    private String subject;
    private String text;

    public Email(String us, String p) {
        username = us;
        password = p;
        pro = new Properties();
        to = new ArrayList<>();
        attachments = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void addRecipient(String s) {
        to.add(s);
    }

    public void addAttachment(String f) {
        attachments.add(f);
    }

    public void setSubject(String s) {
        subject = s;
    }

    public String getSubject() {
        return subject;
    }

    public void setMessage(String s) {
        text = s;
    }

    public String getMessage() {
        return text;
    }

    public Status send() {
        try {
            Session session = Session.getInstance(pro, new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            for (String s : to) {
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(s));
            }
            message.setSubject(subject);
            Multipart multipart = new MimeMultipart();

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(text);
            multipart.addBodyPart(messageBodyPart);

            for (String f : attachments) {
                BodyPart temp = new MimeBodyPart();
                String filename = f;
                DataSource source = new FileDataSource(filename);
                temp.setDataHandler(new DataHandler(source));
                temp.setFileName(filename);
                multipart.addBodyPart(temp);
            }
            
            message.setContent(multipart);
            Transport.send(message);
            return new Status(true, "");
        } catch (MessagingException ex) {
            return new Status(false, ex.getMessage());
        }
    }

    public Properties getProperties() {
        return pro;
    }

    public class Status {

        private final boolean success;
        private final String message;

        public Status(boolean b, String m) {
            success = b;
            message = m;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

}
