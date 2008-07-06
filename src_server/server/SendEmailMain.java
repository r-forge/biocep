package server;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import java.util.Properties;
public class SendEmailMain
{
 
	 private class SMTPAuthenticator extends javax.mail.Authenticator {
		 
			public PasswordAuthentication getPasswordAuthentication() {
				String username = "biocep";
				String password = "tara!?ta";
				return new PasswordAuthentication(username, password);
			}
		}
 
     public void sendMail(String mailServer, String from, String to,
                             String subject, String messageBody,
                             String[] attachments) throws
MessagingException, AddressException
     {
         // Setup mail server
    	 

    	 java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
         Properties props = System.getProperties();
         
 		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");		
        props.put("mail.smtp.host", mailServer);
        props.put("mail.smtp.port", "25");
        props.put("mail.smtp.starttls.enable","true");
         
         // Get a mail session
        Authenticator auth = new SMTPAuthenticator();
		Session session = Session.getDefaultInstance(props, auth);
         
         // Define a new mail message
         Message message = new MimeMessage(session);
         message.setFrom(new InternetAddress(from));
         message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
         message.setSubject(subject);
         
         // Create a message part to represent the body text
         BodyPart messageBodyPart = new MimeBodyPart();
         messageBodyPart.setText(messageBody);
         
         //use a MimeMultipart as we need to handle the file attachments
         Multipart multipart = new MimeMultipart();
         
         //add the message body to the mime message
         multipart.addBodyPart(messageBodyPart);
         
         // add any file attachments to the message
         if (attachments!=null) addAtachments(attachments, multipart);
         
         // Put all message parts in the message
         message.setContent(multipart);
         
         // Send the message
         Transport.send(message);
 
 
     }
 
     protected void addAtachments(String[] attachments, Multipart multipart)
                     throws MessagingException, AddressException
     {
         for(int i = 0; i<= attachments.length -1; i++)
         {
             String filename = attachments[i];
             MimeBodyPart attachmentBodyPart = new MimeBodyPart();
             
             //use a JAF FileDataSource as it does MIME type detection
             DataSource source = new FileDataSource(filename);
             attachmentBodyPart.setDataHandler(new DataHandler(source));
             
             //assume that the filename you want to send is the same as the
             //actual file name - could alter this to remove the file path
             attachmentBodyPart.setFileName(filename);
             
             //add the attachment
             multipart.addBodyPart(attachmentBodyPart);
         }
     }
 
     public static void main(String[] args)
     {
         try
         {
        	 SendEmailMain client = new SendEmailMain();
             String server="smtp.gmail.com";
             String from="biocep@gmail.com";
             String to = "karim_chine@voila.fr";
             String subject="Test 3";
             String message="Testing";
             String[] filenames ={"c:/somefile.txt"};
         
             client.sendMail(server,from,to,subject,message,null);
         }
         catch(Exception e)
         {
             e.printStackTrace(System.out);
         }
         
     }
 }
  