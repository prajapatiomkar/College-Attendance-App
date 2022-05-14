package com.example.attendancenotifierappforcollege;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttendanceActivity extends AppCompatActivity {
    ProgressBar pb;
    TextView pbTxt;
    Button clickBtn;
    String resultString = "";
    String emails = "";
    Button sendBtn;
    EditText subjectEditTxt;
    EditText messageEditTxt;
    Boolean sendValue = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        getSupportActionBar().hide();
//        responseTxt = findViewById(R.id.responseTxt);
//        resultTxt = findViewById(R.id.resultTxt);
        clickBtn = findViewById(R.id.clickBtn);
        sendBtn = findViewById(R.id.sendBtn);
        subjectEditTxt = findViewById(R.id.subjectEditTxt);
        messageEditTxt = findViewById(R.id.messageEditTxt);
        pb = findViewById(R.id.pb);
        pbTxt = findViewById(R.id.pbTxt);

        pb.setVisibility(View.INVISIBLE);
        pbTxt.setText("");

        clickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                data.setType("*/*");
                data = Intent.createChooser(data, "Choose file data");
                intentActivityResultLauncher.launch(data);
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(resultString==""){
                    Toast.makeText(getApplicationContext(),"Please Select File",Toast.LENGTH_SHORT).show();
                    return;
                }
                pb.setVisibility(View.VISIBLE);
                List<String> email = new ArrayList<>();
                List<String> name = new ArrayList<>();
                List<String> percentage = new ArrayList<>();
                List<String> resultEmail = new ArrayList<>();

                Pattern pattern = Pattern.compile("([0-9]+[,][A-Z])\\w+(['\\s']+([A-Z])\\w+)+");
                Pattern pattern1 = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}");
                Pattern pattern2 = Pattern.compile("[0-9][0-9]*[.][0-9]*");
                Matcher matcher = pattern.matcher(resultString);
                Matcher matcher1 = pattern1.matcher(resultString);
                Matcher matcher2 = pattern2.matcher(resultString);

                while (matcher.find()) {
                    name.add(matcher.group());
                }
                while (matcher1.find()) {
                    email.add(matcher1.group());
                }
                while (matcher2.find()) {

                    percentage.add(String.valueOf(Math.round(Float.parseFloat(matcher2.group()))));
                }
                for (int i = 0; i < name.size(); i++) {
                    if (name.get(i) != null && email.get(i) != null && percentage.get(i) != null && Integer.parseInt(percentage.get(i + 1)) < 75) {
                        resultEmail.add(email.get(i));
                    } else {
                        continue;
                    }
                }
                for (int j = 0; j < resultEmail.size(); j++) {
                    emails = emails + resultEmail.get(j) + ",";

                }

                try {
                    String messageText = messageEditTxt.getText().toString();
                    String subjectText = subjectEditTxt.getText().toString();
                    String stringSenderEmail = "testingapps010@gmail.com";
                    String stringSenderPassword = "#testingapps010#";

                    String stringReceiverEmail = emails;

                    String[] recipientList = stringReceiverEmail.split(",");
                    InternetAddress[] recipientAddress = new InternetAddress[recipientList.length];
                    String stringHost = "smtp.gmail.com";

                    Properties properties = System.getProperties();
                    properties.put("mail.smtp.host", stringHost);
                    properties.put("mail.smtp.port", "465");
                    properties.put("mail.smtp.ssl.enable", "true");
                    properties.put("mail.smtp.auth", "true");

                    javax.mail.Session session = Session.getInstance(properties, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(stringSenderEmail, stringSenderPassword);
                        }
                    });
                    MimeMessage message = new MimeMessage(session);
//                    message.addRecipient(Message.RecipientType.TO,new InternetAddress(stringReceiverEmail));
                    //For multiple Email
                    int counter = 0;
                    for (String recipient : recipientList) {
                        recipientAddress[counter] = new InternetAddress(recipient.trim());
                        counter++;
                    }
                    message.setRecipients(Message.RecipientType.TO,recipientAddress);
                    message.setSubject(subjectText);
                    message.setText(messageText);
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Transport.send(message);
                                pb.setVisibility(View.INVISIBLE);
                            } catch (MessagingException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                    Toast.makeText(getApplicationContext(),"Message Sent Successfully",Toast.LENGTH_LONG).show();
                    thread.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        });

    }




    ActivityResultLauncher<Intent> intentActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri uri = data.getData();
                        byte[] bytes = getByteFromUri(getApplicationContext(), uri);
                        resultString = new String(bytes);
                    }
                }

                private byte[] getByteFromUri(Context context, Uri uri) {
                    InputStream inputStream = null;
                    ByteArrayOutputStream byteArrayOutputStream = null;
                    try {
                        inputStream = context.getContentResolver().openInputStream(uri);
                        byteArrayOutputStream = new ByteArrayOutputStream();
                        int bufferSize = 1024;
                        byte[] buffer = new byte[bufferSize];
                        int len = 0;
                        while ((len = inputStream.read(buffer)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, len);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return byteArrayOutputStream.toByteArray();
                }


            }
    );


}