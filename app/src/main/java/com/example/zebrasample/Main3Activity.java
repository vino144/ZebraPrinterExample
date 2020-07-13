package com.example.zebrasample;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import com.zebra.android.comm.BluetoothPrinterConnection;
import com.zebra.android.comm.ZebraPrinterConnection;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

public class Main3Activity extends AppCompatActivity {

    protected ZebraPrinter printer;
    private Bitmap m_bmp;
    private ParseBitmap m_BmpParser;
    private Connection printerConnection;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inDither = false;
        m_bmp = BitmapFactory.decodeResource(getResources(),R.drawable.icons,options);
        m_BmpParser = new ParseBitmap(m_bmp);

        sendCPCLOverBluetooth("00:22:58:36:5C:25");
        //sendCpclOverBluetooth("00:22:58:36:5C:25");
    }


    private void sendCPCLOverBluetooth(final String theBtMacAddress) {
        try {
            // Instantiate connection for given Bluetooth® MAC Address.
            ZebraPrinter printer = ZebraPrinterFactory.getInstance(printerConnection);
            printerConnection = new BluetoothConnection("A4:DA:32:86:F2:71");

            // Initialize
            //Looper.prepare();

            // Open the connection - physical connection is established here.
            printerConnection.open();
            PrinterLanguage pl = printer.getPrinterControlLanguage();
            Toast.makeText(this, pl.toString(), Toast.LENGTH_LONG).show();
            if (pl == PrinterLanguage.ZPL) {
                Toast.makeText(this, "ZPL", Toast.LENGTH_LONG).show();
            } else if (pl == PrinterLanguage.CPCL) {
                Toast.makeText(this, "CPCL", Toast.LENGTH_LONG).show();
            }

            String TicketID = "23948234";
            // This example prints "This is a ZPL test." near the top of the label.
            //String zplData = "^XA^FO20,20^A0N,25,25^FDThis is a ZPL test.^FS^XZ";
            //It is taking 50 units for each line so forumula is
            //(1 blank line on top + number of lines in middle +1 blank line on bottom)* 1st argument of ML command = result
            //put result as 4th argumenet of ! command"

            String str = m_BmpParser.ExtractGraphicsDataForCPCL(0,0);
            String zplData = "! 0 200 200 230 1\r\n"
                    + str
                    +"PRINT\r\n";

            // Send the data to printer as a byte array.
            printerConnection.write(zplData.getBytes());
            //Make sure the data got to the printer before closing the connection
            //Thread.sleep(5000);

            // Close the connection to release resources.
            printerConnection.close();

            //Looper.myLooper().quit();
        } catch (Exception e) {
            // Handle communications error here.
            //e.printStackTrace();
            Toast.makeText(this, e.getMessage() , Toast.LENGTH_LONG).show();
        }
    }

}
