package root.nfc.mifareapp;
import java.io.IOException;
import java.util.Locale;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.nfc.Tag;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "nfcinventory_simple";
    // NFC-related variables
    NfcAdapter mNfcAdapter;
    PendingIntent mNfcPendingIntent;
    IntentFilter[] mReadWriteTagFilters;
    private boolean mWriteMode = false;
    private boolean mAuthenticationMode = false;
    private boolean ReadUIDMode = true;
    String[][]mTechList;
    // UI elements
    EditText mTemperaturaMinima;
    EditText mTemperaturaMaxima;
    EditText mHumedadMinima;
    EditText mHumedadMaxima;
    EditText mHumedadTierraMinima;
    EditText mHexKeyA;
    EditText mHexKeyB;
    EditText mDatatoWrite;
    RadioGroup mRadioGroup;
    AlertDialog mTagDialog;
    EditText mDataBloque;
    /*
    EditText mTagUID;
    EditText mCardType;






     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTemperaturaMinima = ((EditText) findViewById(R.id.temperaturaMinima_id));
        mTemperaturaMaxima = ((EditText) findViewById(R.id.temperaturaMaxima_id));
        mHumedadMinima = ((EditText) findViewById(R.id.humedadMinima_id));
        mHumedadMaxima = ((EditText) findViewById(R.id.humedadMaxima_id));
        mHumedadTierraMinima = ((EditText) findViewById(R.id.humedadTierraMinima_id));
        mHexKeyA = ((EditText) findViewById(R.id.editTextKeyA));
        mHexKeyB = ((EditText) findViewById(R.id.editTextKeyB));
        mRadioGroup = ((RadioGroup) findViewById(R.id.rBtnGrp));

        //findViewById(R.id.escribir_button).setOnClickListener(mTagWrite);
        findViewById(R.id.escribir_button1).setOnClickListener(mTagWrite);
        findViewById(R.id.escribir_button2).setOnClickListener(mTagWrite);
        findViewById(R.id.escribir_button3).setOnClickListener(mTagWrite);
        findViewById(R.id.escribir_button4).setOnClickListener(mTagWrite);
        findViewById(R.id.escribir_button5).setOnClickListener(mTagWrite);

        findViewById(R.id.autentificar_button1).setOnClickListener(mTagAuthenticate);
        findViewById(R.id.autentificar_button2).setOnClickListener(mTagAuthenticate);
        findViewById(R.id.autentificar_button3).setOnClickListener(mTagAuthenticate);
        findViewById(R.id.autentificar_button4).setOnClickListener(mTagAuthenticate);
        findViewById(R.id.autentificar_button5).setOnClickListener(mTagAuthenticate);


// get an instance of the context's cached NfcAdapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
// if null is returned this demo cannot run. Use this check if the
// "required" parameter of <uses-feature> in the manifest is not set
        if (mNfcAdapter == null)
        {
            Toast.makeText(this,
                    "Su dispositivo no soporta NFC. No se puede correr la aplicación.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
// check if NFC is enabled
        checkNfcEnabled();
// Handle foreground NFC scanning in this activity by creating a
// PendingIntent with FLAG_ACTIVITY_SINGLE_TOP flag so each new scan
// is not added to the Back Stack
        mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
// Create intent filter to handle MIFARE NFC tags detected from inside our
// application
        IntentFilter mifareDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        try {
            mifareDetected.addDataType("application/com.e.mifarecontrol");
        } catch (MalformedMimeTypeException e)
        {
            throw new RuntimeException("No se pudo añadir un tipo MIME.", e);
        }
// Create intent filter to detect any MIFARE NFC tag
        mReadWriteTagFilters = new IntentFilter[] { mifareDetected };
// Setup a tech list for all NFC tags
        mTechList = new String[][] { new String[] { MifareClassic.class.getName() } };
    }
    void resolveWriteIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            MifareClassic mfc = MifareClassic.get(tagFromIntent);
            try {
                mfc.connect();
                boolean auth1 = false;
                boolean auth2 = false;
                boolean auth3 = false;
                boolean auth4 = false;
                boolean auth5 = false;
                String hexkey = "";
                int id = mRadioGroup.getCheckedRadioButtonId();
                int bloqueTempMin = 4;
                int bloqueTempMax = 5;
                int bloqueHumAmbMin = 6;
                int bloqueHumAmbMax = 8;
                int bloqueHum = 9;
                int sectorTempMin = mfc.blockToSector(bloqueTempMin);
                int sectorTempMax = mfc.blockToSector(bloqueTempMax);
                int sectorHumAmbMin = mfc.blockToSector(bloqueHumAmbMin);
                int sectorHumAmbMax = mfc.blockToSector(bloqueHumAmbMax);
                int sectorHum = mfc.blockToSector(bloqueHum);
                byte[] datakey;
                if (id == R.id.radioButtonkeyA){
                    hexkey = mHexKeyA.getText().toString();
                    datakey = hexStringToByteArray(hexkey);
                    auth1 = mfc.authenticateSectorWithKeyA(sectorTempMin, datakey);
                    auth2= mfc.authenticateSectorWithKeyA(sectorTempMax, datakey);
                    auth3 = mfc.authenticateSectorWithKeyA(sectorHumAmbMin, datakey);
                    auth4 = mfc.authenticateSectorWithKeyA(sectorHumAmbMax, datakey);
                    auth5 = mfc.authenticateSectorWithKeyA(sectorHum, datakey);
                }
                else if (id == R.id.radioButtonkeyB){
                    hexkey = mHexKeyB.getText().toString();
                    datakey = hexStringToByteArray(hexkey);
                    auth1 = mfc.authenticateSectorWithKeyB(sectorTempMin, datakey);
                    auth2 = mfc.authenticateSectorWithKeyB(sectorTempMax, datakey);
                    auth3 = mfc.authenticateSectorWithKeyB(sectorHumAmbMin, datakey);
                    auth4 = mfc.authenticateSectorWithKeyB(sectorHumAmbMax, datakey);
                    auth5 = mfc.authenticateSectorWithKeyB(sectorHum, datakey);
                }
                else {
//no item selected poner toast
                    Toast.makeText(this,
                            "°Seleccionar llave A o B!",
                            Toast.LENGTH_LONG).show();
                    mfc.close();
                    return;
                }
                if(auth1){ //Auth1
                    String strdata = mDatatoWrite.getText().toString();
                    byte[] datatowrite = hexStringToByteArray(strdata);
                    mfc.writeBlock(bloqueTempMin, datatowrite);
                    Toast.makeText(this,
                            "Escritura a bloque EXITOSA.",
                            Toast.LENGTH_LONG).show();
                }else{ // Authentication failed - Handle it
                    Toast.makeText(this,
                            "Escritura a bloque FALLIDA dado autentificación fallida.",
                            Toast.LENGTH_LONG).show();
                }

                if(auth2){ //Auth2
                    String strdata = mDatatoWrite.getText().toString();
                    byte[] datatowrite = hexStringToByteArray(strdata);
                    mfc.writeBlock(bloqueTempMax, datatowrite);
                    Toast.makeText(this,
                            "Escritura a bloque EXITOSA.",
                            Toast.LENGTH_LONG).show();
                }else{ // Authentication failed - Handle it
                    Toast.makeText(this,
                            "Escritura a bloque FALLIDA dado autentificación fallida.",
                            Toast.LENGTH_LONG).show();
                }

                if(auth3){ //Auth3
                    String strdata = mDatatoWrite.getText().toString();
                    byte[] datatowrite = hexStringToByteArray(strdata);
                    mfc.writeBlock(bloqueHumAmbMin, datatowrite);
                    Toast.makeText(this,
                            "Escritura a bloque EXITOSA.",
                            Toast.LENGTH_LONG).show();
                }else{ // Authentication failed - Handle it
                    Toast.makeText(this,
                            "Escritura a bloque FALLIDA dado autentificación fallida.",
                            Toast.LENGTH_LONG).show();
                }

                if(auth4){ //Auth4
                    String strdata = mDatatoWrite.getText().toString();
                    byte[] datatowrite = hexStringToByteArray(strdata);
                    mfc.writeBlock(bloqueHumAmbMax, datatowrite);
                    Toast.makeText(this,
                            "Escritura a bloque EXITOSA.",
                            Toast.LENGTH_LONG).show();
                }else{ // Authentication failed - Handle it
                    Toast.makeText(this,
                            "Escritura a bloque FALLIDA dado autentificación fallida.",
                            Toast.LENGTH_LONG).show();
                }

                if(auth5){ //Auth5
                    String strdata = mDatatoWrite.getText().toString();
                    byte[] datatowrite = hexStringToByteArray(strdata);
                    mfc.writeBlock(bloqueHum, datatowrite);
                    Toast.makeText(this,
                            "Escritura a bloque EXITOSA.",
                            Toast.LENGTH_LONG).show();
                }else{ // Authentication failed - Handle it
                    Toast.makeText(this,
                            "Escritura a bloque FALLIDA dado autentificación fallida.",
                            Toast.LENGTH_LONG).show();
                }


                mfc.close();
                mTagDialog.cancel();
            }catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }



    void resolveAuthIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            MifareClassic mfc = MifareClassic.get(tagFromIntent);
            try {
                mfc.connect();
                boolean auth1 = false;
                boolean auth2 = false;
                boolean auth3 = false;
                boolean auth4 = false;
                boolean auth5 = false;
                String hexkey = "";
                int id = mRadioGroup.getCheckedRadioButtonId();
                int sectorTempMin = 1;
                int sectorTempMax = 1;
                int sectorHumAmbMin = 1;
                int sectorHumAmbMax = 2;
                int sectorHum = 2;
                byte[] datakey;
                if (id == R.id.radioButtonkeyA){
                    hexkey = mHexKeyA.getText().toString();
                    datakey = hexStringToByteArray(hexkey);
                    auth1 = mfc.authenticateSectorWithKeyA(sectorTempMin, datakey);
                    auth2 = mfc.authenticateSectorWithKeyA(sectorTempMax, datakey);
                    auth3 = mfc.authenticateSectorWithKeyA(sectorHumAmbMin, datakey);
                    auth4 = mfc.authenticateSectorWithKeyA(sectorHumAmbMax, datakey);
                    auth5 = mfc.authenticateSectorWithKeyA(sectorHum, datakey);
                }
                else if (id == R.id.radioButtonkeyB){
                    hexkey = mHexKeyB.getText().toString();
                    datakey = hexStringToByteArray(hexkey);
                    auth1 = mfc.authenticateSectorWithKeyB(sectorTempMin, datakey);
                    auth2 = mfc.authenticateSectorWithKeyB(sectorTempMax, datakey);
                    auth3 = mfc.authenticateSectorWithKeyB(sectorHumAmbMin, datakey);
                    auth4 = mfc.authenticateSectorWithKeyB(sectorHumAmbMax, datakey);
                    auth5 = mfc.authenticateSectorWithKeyB(sectorHum, datakey);
                }
                else {
//no item selected poner toast
                    Toast.makeText(this,
                            "°Seleccionar llave A o B!",
                            Toast.LENGTH_LONG).show();
                    mfc.close();
                    return;
                }

                if(auth1){
                    Toast.makeText(this,
                            "Autentificación de sector EXITOSA.",
                            Toast.LENGTH_LONG).show();
                }else{ // Authentication failed - Handle it
                    Toast.makeText(this,
                            "Autentificación de sector FALLIDA.",
                            Toast.LENGTH_LONG).show();
                }

                if(auth2){
                    Toast.makeText(this,
                            "Autentificación de sector EXITOSA.",
                            Toast.LENGTH_LONG).show();
                }else{ // Authentication failed - Handle it
                    Toast.makeText(this,
                            "Autentificación de sector FALLIDA.",
                            Toast.LENGTH_LONG).show();
                }

                if(auth3){
                    Toast.makeText(this,
                            "Autentificación de sector EXITOSA.",
                            Toast.LENGTH_LONG).show();
                }else{ // Authentication failed - Handle it
                    Toast.makeText(this,
                            "Autentificación de sector FALLIDA.",
                            Toast.LENGTH_LONG).show();
                }

                if(auth4){
                    Toast.makeText(this,
                            "Autentificación de sector EXITOSA.",
                            Toast.LENGTH_LONG).show();
                }else{ // Authentication failed - Handle it
                    Toast.makeText(this,
                            "Autentificación de sector FALLIDA.",
                            Toast.LENGTH_LONG).show();
                }

                if(auth5){
                    Toast.makeText(this,
                            "Autentificación de sector EXITOSA.",
                            Toast.LENGTH_LONG).show();
                }else{ // Authentication failed - Handle it
                    Toast.makeText(this,
                            "Autentificación de sector FALLIDA.",
                            Toast.LENGTH_LONG).show();
                }

                mfc.close();
            }catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }
    /* Called when the activity will start interacting with the user. */
    @Override
    public void onResume()
    {
        super.onResume();
// Double check if NFC is enabled
        checkNfcEnabled();
        Log.d(TAG, "onResume: " + getIntent());
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mReadWriteTagFilters, mTechList);
    }
    /*
     * This is called for activities that set launchMode to "singleTop" or
     * "singleTask" in their manifest package, or if a client used the
     * FLAG_ACTIVITY_SINGLE_TOP flag when calling startActivity(Intent).
     */
    @Override
    public void onNewIntent(Intent intent)
    {
        Log.d(TAG, "onNewIntent: " + intent);
        Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
        if (mAuthenticationMode)
        {
// Currently in tag AUTHENTICATION mode
            resolveAuthIntent(intent);
            mTagDialog.cancel();
        }
         else
        {
// Currently in tag WRITING mode
            resolveWriteIntent(intent);
        }
    }
    /* Called when the system is about to start resuming a previous activity. */
    @Override
    public void onPause()
    {
        super.onPause();
        Log.d(TAG, "onPause: " + getIntent());
        mNfcAdapter.disableForegroundDispatch(this);
    }
    private void enableTagWriteMode()
    {
        mWriteMode = true;
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                mReadWriteTagFilters, mTechList);
    }
    private void enableTagReadMode()
    {
        mWriteMode = false;
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                mReadWriteTagFilters, mTechList);
    }
    private void enableTagAuthMode()
    {
        mAuthenticationMode = true;
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                mReadWriteTagFilters, mTechList);
    }
    /*
     * **** TAG AUTHENTICATE METHODS ****
     */
    private View.OnClickListener mTagAuthenticate = new View.OnClickListener()
    {
        @Override
        public void onClick(View arg0)
        {
            enableTagAuthMode();
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    MainActivity.this)
                    .setTitle(getString(R.string.ready_to_authenticate))
                    .setMessage(getString(R.string.ready_to_authenticate_instructions))
                    .setCancelable(true)
                    .setNegativeButton("Cancelar",
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog,
                                                    int id)
                                {
                                    dialog.cancel();
                                }
                            })
                    .setOnCancelListener(new DialogInterface.OnCancelListener()
                    {
                        @Override
                        public void onCancel(DialogInterface dialog)
                        {
                            mAuthenticationMode = false;
                        }
                    });
            mTagDialog = builder.create();
            mTagDialog.show();
        }
    };
    /*
     * **** TAG READ METHODS ****
     */
    private View.OnClickListener mTagRead = new View.OnClickListener()
    {
        @Override
        public void onClick(View arg0)
        {
            enableTagReadMode();
            ReadUIDMode = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    MainActivity.this)
                    .setTitle(getString(R.string.ready_to_read))
                    .setMessage(getString(R.string.ready_to_read_instructions))
                    .setCancelable(true)
                    .setNegativeButton("Cancelar",
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog,
                                                    int id)
                                {
                                    dialog.cancel();
                                }
                            })
                    .setOnCancelListener(new DialogInterface.OnCancelListener()
                    {
                        @Override
                        public void onCancel(DialogInterface dialog)
                        {
                            enableTagReadMode();
                            ReadUIDMode = true;
                        }
                    });
            mTagDialog = builder.create();
            mTagDialog.show();
        }
    };
    /*
     * **** TAG WRITE METHODS ****
     */
    private View.OnClickListener mTagWrite = new View.OnClickListener()
    {
        @Override
        public void onClick(View arg0)
        {
            enableTagWriteMode();
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    MainActivity.this)
                    .setTitle(getString(R.string.ready_to_write))
                    .setMessage(getString(R.string.ready_to_write_instructions))
                    .setCancelable(true)
                    .setNegativeButton("Cancelar",
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog,
                                                    int id)
                                {
                                    dialog.cancel();
                                }
                            })
                    .setOnCancelListener(new DialogInterface.OnCancelListener()
                    {
                        @Override
                        public void onCancel(DialogInterface dialog)
                        {
                            enableTagReadMode();
                        }
                    });
            mTagDialog = builder.create();
            mTagDialog.show();
        }
    };
    /*
     * **** HELPER METHODS ****
     */
    private void checkNfcEnabled()
    {
        Boolean nfcEnabled = mNfcAdapter.isEnabled();
        if (!nfcEnabled)
        {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.warning_nfc_is_off))
                    .setMessage(getString(R.string.turn_on_nfc))
                    .setCancelable(false)
                    .setPositiveButton("Actualizar Settings",
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog,
                                                    int id)
                                {
                                    startActivity(new Intent(
                                            android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                                }
                            }).create().show();
        }
    }
    public static String getHexString(byte[] b, int length)
    {
        String result = "";
        Locale loc = Locale.getDefault();
        for (int i = 0; i < length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
            result += ""; //Poner espacio si se quiere separar de dos en dos caracteres hex
        }
        return result.toUpperCase(loc);
    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}