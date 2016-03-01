package my.app.smssender;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView txt_phone_load, txt_send_status;
    Button btn_load_phones, btn_run_send, btn_clear;
    EditText etxt_message, etxt_log;

    final String LOG_TAG = "SMS_Sender_Logs";

    final String FILENAME = "phone_numbers";
    final String SAVED_MESSAGE = "message";

    SharedPreferences sPref;
    DBHelper dbHelper;
    Set<String> setPhones = new HashSet<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_phone_load = (TextView) findViewById(R.id.txt_PhoneLoad);
        txt_send_status = (TextView) findViewById(R.id.txt_SendStatus);
        btn_load_phones = (Button) findViewById(R.id.btn_LoadPhones);
        btn_run_send = (Button) findViewById(R.id.btn_RunSend);
        btn_clear = (Button) findViewById(R.id.btn_Clear);
        etxt_message = (EditText) findViewById(R.id.etxt_Message);
        etxt_log = (EditText) findViewById(R.id.etxt_Log);

        btn_load_phones.setOnClickListener(this);
        btn_run_send.setOnClickListener(this);
        dbHelper = new DBHelper(this);
        loadText();
    }

    @Override
    public void onClick(View v) {
        Toast toast = Toast.makeText(this, "Empty", Toast.LENGTH_SHORT);
        switch (v.getId()) {
            case R.id.btn_LoadPhones:
                toast = Toast.makeText(this, "Load phones", Toast.LENGTH_SHORT);
                loadPhones();
                break;
            case R.id.btn_RunSend:
                toast = Toast.makeText(this, "Run send", Toast.LENGTH_SHORT);
                break;
            case R.id.btn_Clear:
                toast = Toast.makeText(this, "Clear send", Toast.LENGTH_SHORT);
                break;
        }
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveText();
    }

    void saveText() {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(SAVED_MESSAGE, etxt_message.getText().toString());
        ed.commit();
        Toast.makeText(this, "Text saved", Toast.LENGTH_LONG).show();
        Log.d(LOG_TAG, "Text saved");
    }

    void loadText() {
        sPref = getPreferences(MODE_PRIVATE);
        String savedText = sPref.getString(SAVED_MESSAGE, "");
        etxt_message.setText(savedText);
        Toast.makeText(this, "Text loaded", Toast.LENGTH_LONG).show();
        Log.d(LOG_TAG, "Text loaded");
    }

    void writeFile() {
        try {
            // отрываем поток для записи
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    openFileOutput(FILENAME, MODE_PRIVATE)));
            /*// пишем данные
            bw.write("+79818297998\n");
            bw.write("+79046474587\n");*/
            // закрываем поток
            bw.close();
            Log.d(LOG_TAG, "Файл записан");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void loadPhones() {
        try {
            setPhones.clear();
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    openFileInput(FILENAME)));
            String str = "";
            // читаем содержимое
            while ((str = br.readLine()) != null) {
                setPhones.add(str);
                addLog(str);
                Log.d(LOG_TAG, str);

            }
            txt_phone_load.setText("Phones load: " + setPhones.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void addLog(String txt) {
        etxt_log.setText(etxt_log.getText() + "\n" + txt);
    }

    void addPhonetoDB(String phone) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();


        contentValues.put(DBHelper.KEY_PHONE, phone);

        database.insert(DBHelper.TABLE_CONTACTS, null, contentValues);
        dbHelper.close();
    }

    void readPhoneDB() {

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.query(DBHelper.TABLE_CONTACTS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int phoneIndex = cursor.getColumnIndex(DBHelper.KEY_PHONE);
            do {
                Log.d(LOG_TAG, "ID = " + cursor.getInt(idIndex) +
                        ", phone = " + cursor.getString(phoneIndex));
            } while (cursor.moveToNext());
        } else
            Log.d(LOG_TAG, "0 rows");

        cursor.close();
        dbHelper.close();
    }

    void clearPhoneDB() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(DBHelper.TABLE_CONTACTS, null, null);

        dbHelper.close();
    }
}
