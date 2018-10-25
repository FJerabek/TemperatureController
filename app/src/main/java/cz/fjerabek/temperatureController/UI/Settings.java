package cz.fjerabek.temperatureController.UI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import cz.fjerabek.temperatureController.R;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent intent = getIntent();

        final EditText ip = (EditText) findViewById(R.id.IP);

        final EditText port = (EditText) findViewById(R.id.port);

        ip.setText(intent.getStringExtra("ip"));
        port.setText(String.valueOf(intent.getIntExtra("port", 10000)));

        Button saveButton = (Button) findViewById(R.id.settingsSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String portS = port.getText().toString();

                final String ipS = ip.getText().toString();
                Intent intent = new Intent();
                intent.putExtra("IP", ipS);

                String[] ipArray = ipS.split("\\."); //split ip into array
                if (ipArray.length != 4) { //check if IP has 4 blocks
                    Snackbar.make(v, "IP musí mít 4 části oddělené tečkou", Snackbar.LENGTH_LONG).show();
                    return;
                }

                for (String block : ipArray) { //for each ip block
                    int number;
                    try {
                        number = Integer.parseInt(block);
                    } catch (Exception e) { //check if IP has only numbers
                        Snackbar.make(v, "Bloky IP adresy musí být čísla", Snackbar.LENGTH_LONG).show();
                        return;
                    }
                    if (number > 256 || number < 0) { //check if each block is in range 0-255
                        Snackbar.make(v, "Bloky IP adresy nesmí být menší než 0 nebo větší než 255", Snackbar.LENGTH_LONG).show();
                        return;

                    }
                }

                try {
                    intent.putExtra("Port", Integer.parseInt(portS));
                } catch (Exception e) { //check if port is a number
                    Snackbar.make(v, "Port musí být celé číslo", Snackbar.LENGTH_LONG).show();
                    return;
                }
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }
}
