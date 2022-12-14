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
import android.text.Editable;
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
    private boolean mWriteMode1 = false;
    private boolean mWriteMode2 = false;
    private boolean mWriteMode3 = false;
    private boolean mWriteMode4 = false;
    private boolean mWriteMode5 = false;
    private boolean mAuthenticationMode1 = false;
    private boolean mAuthenticationMode2 = false;
    private boolean ReadUIDMode = true;
    String[][]mTechList;
    // UI elements
    EditText mTagUID;
    EditText mCardType;
    EditText mHexKeyA;
    EditText mHexKeyB;
    EditText mSector;
    EditText mBloque;
    EditText mDataBloque;
    EditText mDatatoWrite;
    AlertDialog mTagDialog;
    RadioGroup mRadioGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHexKeyA = ((EditText) findViewById(R.id.editTextKeyA));
        mHexKeyB = ((EditText) findViewById(R.id.editTextKeyB));
        mDatatoWrite = ((EditText) findViewById(R.id.editTextBloqueAEscribir));
        mRadioGroup = ((RadioGroup) findViewById(R.id.rBtnGrp));
        findViewById(R.id.buttonauthenticate1).setOnClickListener(mTagAuthenticate1);
        findViewById(R.id.buttonauthenticate2).setOnClickListener(mTagAuthenticate2);
        findViewById(R.id.buttonEscribirBloque4).setOnClickListener(mTagWrite1);
        findViewById(R.id.buttonEscribirBloque5).setOnClickListener(mTagWrite2);
        findViewById(R.id.buttonEscribirBloque4).setOnClickListener(mTagWrite3);
        findViewById(R.id.buttonEscribirBloque5).setOnClickListener(mTagWrite4);
        findViewById(R.id.buttonEscribirBloque4).setOnClickListener(mTagWrite5);
// get an instance of the context's cached NfcAdapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
// if null is returned this demo cannot run. Use this check if the
// "required" parameter of <uses-feature> in the manifest is not set
        if (mNfcAdapter == null)
        {
            Toast.makeText(this,
                    "Su dispositivo no soporta NFC. No se puede correr la aplicaci??n.",
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
            throw new RuntimeException("No se pudo a??adir un tipo MIME.", e);
        }
// Create intent filter to detect any MIFARE NFC tag
        mReadWriteTagFilters = new IntentFilter[] { mifareDetected };
// Setup a tech list for all NFC tags
        mTechList = new String[][] { new String[] { MifareClassic.class.getName() } };
        resolveReadIntent(getIntent());
    }
    void resolveReadIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            MifareClassic mfc = MifareClassic.get(tagFromIntent);
            if (ReadUIDMode)
            {
                String tipotag = "";
                String tamano = "";
                byte[] tagUID = tagFromIntent.getId();
                String hexUID = getHexString(tagUID, tagUID.length);
                Log.i(TAG, "Tag UID: " + hexUID);
                Editable UIDField = mTagUID.getText();
                UIDField.clear();
                UIDField.append(hexUID);
                switch(mfc.getType())
                {
                    case 0: tipotag = "Mifare Classic"; break;
                    case 1: tipotag = "Mifare Plus"; break;
                    case 2: tipotag = "Mifare Pro"; break;
                    default: tipotag = "Mifare Desconocido"; break;
                }
                switch(mfc.getSize())
                {
                    case 1024: tamano = " (1K Bytes)"; break;
                    case 2048: tamano = " (2K Bytes)"; break;
                    case 4096: tamano = " (4K Bytes)"; break;
                    case 320: tamano = " (MINI - 320 Bytes)"; break;
                    default: tamano = " (Tama??o desconocido)"; break;
                }
                Log.i(TAG, "Card Type: " + tipotag + tamano);
                Editable CardtypeField = mCardType.getText();
                CardtypeField.clear();
                CardtypeField.append(tipotag + tamano);
            } else
            {
                try {
                    mfc.connect();
                    boolean auth = false;
                    String hexkey = "";
                    int id = mRadioGroup.getCheckedRadioButtonId();
                    int sector = mfc.blockToSector(Integer.valueOf(mBloque.getText().toString()));
                    byte[] datakey;
                    if (id == R.id.radioButtonkeyA){
                        hexkey = mHexKeyA.getText().toString();
                        datakey = hexStringToByteArray(hexkey);
                        auth = mfc.authenticateSectorWithKeyA(sector, datakey);
                    }
                    else if (id == R.id.radioButtonkeyB){
                        hexkey = mHexKeyB.getText().toString();
                        datakey = hexStringToByteArray(hexkey);
                        auth = mfc.authenticateSectorWithKeyB(sector, datakey);
                    }
                    else {
//no item selected poner toast
                        Toast.makeText(this,
                                "??Seleccionar llave A o B!",
                                Toast.LENGTH_LONG).show();
                        mfc.close();
                        return;
                    }
                    if(auth){
                        int bloque = Integer.valueOf(mBloque.getText().toString());
                        byte[] dataread = mfc.readBlock(bloque+1);
                        Log.i("Bloques", getHexString(dataread, dataread.length));
                        dataread = mfc.readBlock(bloque);
                        String blockread = getHexString(dataread, dataread.length);
                        Log.i(TAG, "Bloque Leido: " + blockread);
                        Editable BlockField = mDataBloque.getText();
                        BlockField.clear();
                        BlockField.append(blockread);
                        Toast.makeText(this,
                                "Lectura de bloque EXITOSA.",
                                Toast.LENGTH_LONG).show();
                    }else{ // Authentication failed - Handle it
                        Editable BlockField = mDataBloque.getText();
                        BlockField.clear();
                        Toast.makeText(this,
                                "Lectura de bloque FALLIDA dado autentificaci??n fallida.",
                                Toast.LENGTH_LONG).show();
                    }
                    mfc.close();
                    mTagDialog.cancel();
                }catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
        }
    }
    void resolveWriteIntent(Intent intent, int bloque) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            MifareClassic mfc = MifareClassic.get(tagFromIntent);
            try {
                mfc.connect();
                boolean auth = false;
                String hexkey = "";
                int id = mRadioGroup.getCheckedRadioButtonId();
                //int bloque = Integer.valueOf(mBloque.getText().toString());
                int sector = mfc.blockToSector(bloque);
                byte[] datakey;
                if (id == R.id.radioButtonkeyA){
                    hexkey = mHexKeyA.getText().toString();
                    datakey = hexStringToByteArray(hexkey);
                    auth = mfc.authenticateSectorWithKeyA(sector, datakey);
                }
                else if (id == R.id.radioButtonkeyB){
                    hexkey = mHexKeyB.getText().toString();
                    datakey = hexStringToByteArray(hexkey);
                    auth = mfc.authenticateSectorWithKeyB(sector, datakey);
                }
                else {
//no item selected poner toast
                    Toast.makeText(this,
                            "??Seleccionar llave A o B!",
                            Toast.LENGTH_LONG).show();
                    mfc.close();
                    return;
                }
                if(auth){
                    String strdata = mDatatoWrite.getText().toString();
                    byte[] datatowrite = hexStringToByteArray(strdata);
                    mfc.writeBlock(bloque, datatowrite);
                    Toast.makeText(this,
                            "Escritura a bloque EXITOSA.",
                            Toast.LENGTH_LONG).show();
                }else{ // Authentication failed - Handle it
                    Toast.makeText(this,
                            "Escritura a bloque FALLIDA dado autentificaci??n fallida.",
                            Toast.LENGTH_LONG).show();
                }
                mfc.close();
                mTagDialog.cancel();
            }catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }
    void resolveAuthIntent(Intent intent, int sector) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            MifareClassic mfc = MifareClassic.get(tagFromIntent);
            try {
                mfc.connect();
                boolean auth = false;
                String hexkey = "";
                int id = mRadioGroup.getCheckedRadioButtonId();
                //int sector = Integer.valueOf(mSector.getText().toString());
                byte[] datakey;
                if (id == R.id.radioButtonkeyA){
                    hexkey = mHexKeyA.getText().toString();
                    datakey = hexStringToByteArray(hexkey);
                    auth = mfc.authenticateSectorWithKeyA(sector, datakey);
                }
                else if (id == R.id.radioButtonkeyB){
                    hexkey = mHexKeyB.getText().toString();
                    datakey = hexStringToByteArray(hexkey);
                    auth = mfc.authenticateSectorWithKeyB(sector, datakey);
                }
                else {
//no item selected poner toast
                    Toast.makeText(this,
                            "??Seleccionar llave A o B!",
                            Toast.LENGTH_LONG).show();
                    mfc.close();
                    return;
                }
                if(auth){
                    Toast.makeText(this,
                            "Autentificaci??n de sector EXITOSA.",
                            Toast.LENGTH_LONG).show();
                }else{ // Authentication failed - Handle it
                    Toast.makeText(this,
                            "Autentificaci??n de sector FALLIDA.",
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
        if (mAuthenticationMode1)
        {
// Currently in tag AUTHENTICATION mode
            resolveAuthIntent(intent, 1);
            mTagDialog.cancel();
        }
        else if (mAuthenticationMode2)
        {
// Currently in tag AUTHENTICATION mode
            resolveAuthIntent(intent, 2);
            mTagDialog.cancel();
        }
        else if (mWriteMode1)
        {
// Currently in tag READING mode
            resolveWriteIntent(intent, 4);
        }
        else if(mWriteMode2)
        {
// Currently in tag WRITING mode
            resolveWriteIntent(intent, 5);
        }
        else if(mWriteMode3)
        {
// Currently in tag WRITING mode
            resolveWriteIntent(intent, 6);
        }
        else if(mWriteMode4)
        {
// Currently in tag WRITING mode
            resolveWriteIntent(intent, 8);
        }
        else if(mWriteMode5)
        {
// Currently in tag WRITING mode
            resolveWriteIntent(intent, 9);
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
    private void enableTagWriteMode1()
    {
        mWriteMode1 = true;
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                mReadWriteTagFilters, mTechList);
    }
    private void enableTagWriteMode2()
    {
        mWriteMode2 = true;
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                mReadWriteTagFilters, mTechList);
    }
    private void enableTagWriteMode3()
    {
        mWriteMode3 = true;
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                mReadWriteTagFilters, mTechList);
    }
    private void enableTagWriteMode4()
    {
        mWriteMode4 = true;
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                mReadWriteTagFilters, mTechList);
    }
    private void enableTagWriteMode5()
    {
        mWriteMode5 = true;
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                mReadWriteTagFilters, mTechList);
    }
    /*
    private void enableTagReadMode()
    {
        mWriteMode = false;
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                mReadWriteTagFilters, mTechList);
    }*/
    private void disableModes()
    {
        mWriteMode1 = false;
        mWriteMode2 = false;
        mWriteMode3 = false;
        mWriteMode4 = false;
        mWriteMode5 = false;
        mAuthenticationMode1 = false;
        mAuthenticationMode2 = false;
    }
    private void enableTagAuthMode1()
    {
        mAuthenticationMode1 = true;
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                mReadWriteTagFilters, mTechList);
    }
    private void enableTagAuthMode2()
    {
        mAuthenticationMode2 = true;
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                mReadWriteTagFilters, mTechList);
    }
    /*
     * **** TAG AUTHENTICATE METHODS ****
     */
    private View.OnClickListener mTagAuthenticate1 = new View.OnClickListener()
    {
        @Override
        public void onClick(View arg0)
        {
            enableTagAuthMode1();
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
                            mAuthenticationMode1 = false;
                        }
                    });
            mTagDialog = builder.create();
            mTagDialog.show();
        }
    };
    private View.OnClickListener mTagAuthenticate2 = new View.OnClickListener()
    {
        @Override
        public void onClick(View arg0)
        {
            enableTagAuthMode2();
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
                            mAuthenticationMode2 = false;
                        }
                    });
            mTagDialog = builder.create();
            mTagDialog.show();
        }
    };
    /*
     * **** TAG READ METHODS ****
     */

    /*
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
    };*/
    /*
     * **** TAG WRITE METHODS ****
     */
    private View.OnClickListener mTagWrite1 = new View.OnClickListener()
    {
        @Override
        public void onClick(View arg0)
        {
            enableTagWriteMode1();
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
                            disableModes();
                        }
                    });
            mTagDialog = builder.create();
            mTagDialog.show();
        }
    };

    private View.OnClickListener mTagWrite2 = new View.OnClickListener()
    {
        @Override
        public void onClick(View arg0)
        {
            enableTagWriteMode2();
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
                            disableModes();
                        }
                    });
            mTagDialog = builder.create();
            mTagDialog.show();
        }
    };
    private View.OnClickListener mTagWrite3 = new View.OnClickListener()
    {
        @Override
        public void onClick(View arg0)
        {
            enableTagWriteMode3();
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
                            disableModes();
                        }
                    });
            mTagDialog = builder.create();
            mTagDialog.show();
        }
    };
    private View.OnClickListener mTagWrite4 = new View.OnClickListener()
    {
        @Override
        public void onClick(View arg0)
        {
            enableTagWriteMode4();
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
                            disableModes();
                        }
                    });
            mTagDialog = builder.create();
            mTagDialog.show();
        }
    };
    private View.OnClickListener mTagWrite5 = new View.OnClickListener()
    {
        @Override
        public void onClick(View arg0)
        {
            enableTagWriteMode5();
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
                            disableModes();
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