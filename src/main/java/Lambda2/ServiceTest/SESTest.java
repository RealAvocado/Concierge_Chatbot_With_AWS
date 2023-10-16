package Lambda2.ServiceTest;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import javax.mail.MessagingException;

public class SESTest {
    public static void sendEmail(
            SesClient client,
            String sender,
            String recipient,
            String subject,
            String bodyText
    ) throws MessagingException {
        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(Destination.builder().toAddresses(recipient).build())
                .message(Message.builder()
                        .body(Body.builder().text(Content.builder().data(bodyText).build()).build())
                        .subject(Content.builder().data(subject).build())
                        .build())
                .source(sender)
                .build();

        try {
            System.out.println("Attempting to send an email through Amazon SES " + "using the AWS SDK for Java...");
            client.sendEmail(emailRequest);
            System.out.println("Email sent successfully.");

        } catch (SesException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials
                .create("AKIAXM6FBPUO4QIKOHMX", "ejvTT2KErHmb1SFfMKfYLbfrS93on2OxoKLfO6vy");

        SesClient sesClient = SesClient.builder()
                .region(Region.US_EAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        awsCredentials))
                .build();
        String recipientEmail = "lj2397@nyu.edu";
        String emailContent = "Hi, this is SES service test.";
        try {
            sendEmail(sesClient, "jdcomputing123@gmail.com", recipientEmail, "Restaurants Recommendation", emailContent);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
