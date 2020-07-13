package com.example.zebrasample;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import org.apache.http.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private Connection printerConnection;
    private RadioButton btRadioButton;
    private ZebraPrinter printer;
    private TextView statusField;
    private EditText macAddress, ipDNSAddress, portNumber;
    private Button testButton,nexteopage,img_btn;
    private String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    Bitmap selectedImage;
    private ParseBitmap m_BmpParser;
    private Bitmap m_bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        macAddress =  this.findViewById(R.id.macInput);
        macAddress.setText("A4:DA:32:86:F2:71");
        statusField = (TextView) this.findViewById(R.id.txt1);

        testButton =  this.findViewById(R.id.testButton);
        testButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        enableTestButton(false);
                        Looper.prepare();
                        doConnectionTest();
                        Looper.loop();
                        Looper.myLooper().quit();
                    }
                }).start();
            }
        });
        nexteopage =  this.findViewById(R.id.testButton2);
        nexteopage.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,Main3Activity.class);
                startActivity(intent);
            }
        });

        img_btn =  this.findViewById(R.id.chooseimage);
        img_btn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 100);
            }
        });

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inDither = false;
        m_bmp = BitmapFactory.decodeResource(getResources(),R.drawable.printerlogo,options);
        m_BmpParser = new ParseBitmap(m_bmp);

    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                if (data!=null){
                    System.out.println("CHECK_BITMAP1");
                    if (data.getData()!=null){
                        System.out.println("CHECK_BITMAP2");
                        final Uri imageUri = data.getData();
                        System.out.println("CHECK_BITMAP3");
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        selectedImage = BitmapFactory.decodeStream(imageStream);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inScaled = false;
                        options.inDither = false;
                       // m_bmp = BitmapFactory.decodeResource(getResources(),R.drawable.printerlogo,options);
                        m_BmpParser = new ParseBitmap(selectedImage);
                        System.out.println("CHECK_BITMAP4");
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(MainActivity.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    private void enableTestButton(final boolean enabled) {
        runOnUiThread(new Runnable() {
            public void run() {
                testButton.setEnabled(enabled);
            }
        });
    }

    private void doConnectionTest() {
        printer = connect();
        if (printer != null) {
            sendTestLabel();
        } else {
            disconnect();
        }
    }
    public void disconnect() {
        try {
            setStatus("Disconnecting", Color.RED);
            if (printerConnection != null) {
                printerConnection.close();
            }
            setStatus("Not Connected", Color.RED);
        } catch (ConnectionException e) {
            setStatus("COMM Error! Disconnected", Color.RED);
        } finally {
            enableTestButton(true);
        }
    }
    private void setStatus(final String statusMessage, final int color) {
        runOnUiThread(new Runnable() {
            public void run() {
                statusField.setBackgroundColor(color);
                statusField.setText(statusMessage);
            }
        });
        DemoSleeper.sleep(1000);
    }

    private void sendTestLabel() {
        try {
            /*for image printing*/
            //Bitmap logo = BitmapFactory.decodeResource(getResources(),R.drawable.printerlogo);
            //System.out.println("CHECK_LOGO:"+selectedImage.toString());
            //printer.printImage(new ZebraImageAndroid(selectedImage), 0, 0, 550, 412, false);
            //printerConnection.open();
           // printer = ZebraPrinterFactory.getInstance(PrinterLanguage.CPCL, printerConnection);
            //printer.printImage("pathImg", 120, 0, 0, 0, true);
            //printer.printImage(BitMapToString(logo), 0, 0);

            //Bitmap bmp = intent.getExtras().get("data");
            /*Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.icons);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] configLabel = stream.toByteArray();*/

            byte[] configLabel = getConfigLabel();
            printerConnection.write(configLabel);   //for txt print
            setStatus("Sending Data", Color.BLUE);
            DemoSleeper.sleep(1500);
            if (printerConnection instanceof BluetoothConnection) {
                String friendlyName = ((BluetoothConnection) printerConnection).getFriendlyName();
                setStatus(friendlyName, Color.MAGENTA);
                DemoSleeper.sleep(500);
            }
        } catch (ConnectionException e) {
            setStatus(e.getMessage(), Color.RED);
        } finally {
            disconnect();
        }
    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }


    private byte[] getConfigLabel() {
        //PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();

        byte[] configLabel=null ;
        /*if (printerLanguage == PrinterLanguage.ZPL) {
            configLabel = "^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ".getBytes();
        } else if (printerLanguage == PrinterLanguage.CPCL) {
            String cpclConfigLabel = "! 0 200 200 406 1\r\n" + "ON-FEED IGNORE\r\n" + "BOX 20 20 380 380 8\r\n" + "T 0 6 137 177 TEST\r\n" + "PRINT\r\n";
            configLabel = cpclConfigLabel.getBytes();
        }*/
       // String cpclConfigLabel = "! 0 200 200 406 1\r\n" + "ON-FEED IGNORE\r\n" + "BOX 20 20 380 380 8\r\n" + "T 0 6 137 177 TEST\r\n" + "PRINT\r\n";
        String str = m_BmpParser.ExtractGraphicsDataForCPCL(0,0);

        String labelform = "! 0 200 200 700 1\r\n"
                + str+"\n"+
                "\n" +
                "PRINT\r\n";


        /*String labelform="! 0 200 200 799 1\n" +
                "PCX 134 680 !<FR .PCX\n" +
                "T180 4 0 568 467 Name\n" +
                "T180 4 0 568 540 ID\n" +
                "T180 4 0 568 611 Date\n" +
                "T180 4 0 405 611 17-03-2020 19:05:23\n" +
                "T180 4 0 568 392 Code\n" +
                "T180 4 0 405 467 AAAA\n" +
                "T180 4 0 405 688 123456\n" +
                "T180 4 0 405 540 11\n" +
                "T180 4 0 443 467 :\n" +
                "T180 4 0 443 540 :\n" +
                "T180 4 0 383 236 :\n" +
                "T180 4 0 383 311 :\n" +
                "T180 4 0 443 392 :\n" +
                "T180 4 0 443 611 :\n" +
                "T180 4 0 568 688 No\n" +
                "T180 4 0 443 688 :\n" +
                "T180 4 0 405 392 qwerty\n" +
                "T180 4 0 568 236 B No\n" +
                "T180 4 0 270 311 BBBBB\n" +
                "T180 4 0 568 311 F Name\n" +
                "T180 4 0 316 236 22222222\n" +
                "T180 4 0 519 149 P Name\n" +
                "T180 4 0 559 79 AAAA BBBBB CCCCCC DD \n" +
                "PRINT\n";*/

        configLabel = labelform.getBytes();
        return configLabel;
    }


    private String getMacAddressFieldText() {
        return macAddress.getText().toString();
    }
    public ZebraPrinter connect() {
        setStatus("Connecting...", Color.YELLOW);
        printerConnection = null;
        printerConnection = new BluetoothConnection(getMacAddressFieldText());
        SettingsHelper.saveBluetoothAddress(this, getMacAddressFieldText());
        try {
            printerConnection.open();
            setStatus("Connected", Color.GREEN);
        } catch (ConnectionException e) {
            setStatus("Comm Error! Disconnecting", Color.RED);
            DemoSleeper.sleep(1000);
            disconnect();
        }

        ZebraPrinter printer = null;

        if (printerConnection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(printerConnection);
                setStatus("Determining Printer Language", Color.YELLOW);
                PrinterLanguage pl = printer.getPrinterControlLanguage();

                setStatus("Printer Language " + pl, Color.BLUE);
            } catch (ConnectionException | ZebraPrinterLanguageUnknownException e) {
                setStatus("Unknown Printer Language", Color.RED);
                printer = null;
                DemoSleeper.sleep(1000);
                disconnect();
            }
        }

        return printer;
    }

}
