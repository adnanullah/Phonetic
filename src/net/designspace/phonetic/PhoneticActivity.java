package net.designspace.phonetic;

import java.lang.reflect.Field;
import java.util.Locale;

import net.designspace.phonetic.R.string;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;

public class PhoneticActivity extends Activity implements OnInitListener {
	
	private static final String TAG = "Phonetic";
	private static final int DATA_CHECK_CODE = 2381;
	private static final String codePrefix = "code_";
	
	private TextToSpeech tts;
	private static boolean isTTSEnabled = true;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        init();
        
        //addPreferencesFromResource(R.xml.preference);
        
        // Check if a TTS engine is installed
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, DATA_CHECK_CODE);
    }
    
   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(Menu.NONE, 0, 0, "Show current settings");
    	return super.onCreateOptionsMenu(menu);
    } */
    
    /** TTS instantiation **/
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language is not available.");
                isTTSEnabled = false;
            } 
        } else {
            // Initialisation failed.
            Log.e(TAG, "Could not initialise TextToSpeech.");
            isTTSEnabled = false;
        }
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, this);
            } else {
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    /** Called on device rotation. */
    @Override
    public void onConfigurationChanged(Configuration config) {
      super.onConfigurationChanged(config);
      setContentView(R.layout.main);
      init();
    }
    
    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void init() {
    	final EditText txtInput = (EditText)findViewById(R.id.txtInput);
        final TextView lblCodeWord = (TextView)findViewById(R.id.txtCodeWord);
        txtInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable editable) {
				char selected = editable.charAt(editable.length() - 1);
				String codeWord = getLocalisedValue(codePrefix + selected);
				if (codeWord != null) {
					lblCodeWord.setText(codeWord);
					if (isTTSEnabled) {
						tts.speak(codeWord, TextToSpeech.QUEUE_FLUSH, null);
					}
				}
				txtInput.removeTextChangedListener(this);
				txtInput.setText("");
				txtInput.addTextChangedListener(this);
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        });
        
        txtInput.requestFocus();
    }
    
    private String getLocalisedValue(String name) {
    	String value = null;
    	try {
    	    Class<string> res = R.string.class;
    	    Field field = res.getField(name);
    	    final int id = field.getInt(field);
    	    value = getResources().getString(id);
    	} catch (Exception e) {
    	    // Unsupported characters will fail silently.
    	}
    	return value;
    }
}