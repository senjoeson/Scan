package com.test;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.barcode.HSMDecodeResult;
import com.honeywell.barcode.HSMDecoder;
import com.honeywell.barcode.Symbology;
import com.honeywell.license.ActivationManager;
import com.honeywell.license.ActivationResult;
import com.honeywell.plugins.PluginResultListener;
import com.honeywell.plugins.decode.DecodeResultListener;
import com.test.plugin.MyCustomPlugin;
import com.test.plugin.MyCustomPluginResultListener;

/* The purpose of this sample code is to show the multiple different ways you can use bar code scanning functionality in your application.
 * Bar code scanning can be achieved in the following 3 ways:
 * 1) You can let HSMDecoder handle everything for you by simply calling hsmDecoder.scanBarcode();
 * 2) You can embed an HSMDecodeComponent into your own activity and size it how you see fit
 * 3) You can create a custom SwiftPlugin to completely control the look and operation of a scan event.  
 *    This custom plug-in must be registered with HSMDecoder and can be used with either method 1 or 2 mentioned above.
 */
public class MainActivity extends Activity implements DecodeResultListener, MyCustomPluginResultListener
{
    private HSMDecoder hsmDecoder;
    private TextView tvResult, tvSymb, tvLength, tvDecTime;
    private ImageView ivDecode;
    private CheckBox cbDefault, cbCustom;
    private static MyCustomPlugin customPlugin;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	try
    	{
	        super.onCreate(savedInstanceState);

	        //initialize GUI
	        initGuiElements();

            //activate the API with your license key
            ActivationResult activationResult = ActivationManager.activate(this, "insert-your-api-key-here");
	        Toast.makeText(this, "Activation Result: " + activationResult, Toast.LENGTH_LONG).show();

	        //get the singleton instance of the decoder
        	hsmDecoder = HSMDecoder.getInstance(this);

	        //set all decoder related settings
        	hsmDecoder.enableSymbology(Symbology.UPCA);
        	hsmDecoder.enableSymbology(Symbology.CODE128);
        	hsmDecoder.enableSymbology(Symbology.CODE39);
        	hsmDecoder.enableSymbology(Symbology.QR);
			hsmDecoder.enableSymbology(Symbology.EAN13);
			hsmDecoder.enableSymbology(Symbology.CODE93);
			hsmDecoder.enableSymbology(Symbology.MAXICODE);
			hsmDecoder.enableSymbology(Symbology.RSS_14);
            hsmDecoder.enableFlashOnDecode(true);
            
            hsmDecoder.enableAimer(true);
            hsmDecoder.setAimerColor(Color.RED);
            hsmDecoder.setOverlayText("Place barcode completely inside viewfinder!");
            hsmDecoder.setOverlayTextColor(Color.WHITE);
            hsmDecoder.addResultListener(this);
	    hsmDecoder.enableSound(true);
	    
            //instantiate custom plug-in and set this activity as a listener
            customPlugin = new MyCustomPlugin(getApplicationContext());
            customPlugin.addResultListener(this);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }

    @Override
    public void onDestroy()
    {
    	super.onDestroy();

    	//dispose of the decoder instance, this stops the underlying camera service and releases all associated resources
    	HSMDecoder.disposeInstance();

    	//dispose of you custom plug-in
    	customPlugin.dispose();
    }

    private void onDefaultClicked(Boolean isChecked)
    {
    	//add or remove a listener to the default decoding functionality.  This implicitly registers a default decoding plug-in behind the scenes.
    	//if no listeners are added, then you have effectively unregistered the default decoding plug-in
    	if( isChecked )
			hsmDecoder.addResultListener(this);
		else
			hsmDecoder.removeResultListener(this);
    }

    private void onCustomClicked(Boolean isChecked)
    {
    	//register/unregister your custom plug-in with the system
    	if( isChecked )
			hsmDecoder.registerPlugin(customPlugin);
		else
			hsmDecoder.unRegisterPlugin(customPlugin);
    }

	@Override
	public void onHSMDecodeResult(HSMDecodeResult[] barcodeData)
	{
		//handle results from the default decoding functionality
		displayBarcodeData(barcodeData);
	}

	@Override
	public void onCustomPluginResult(HSMDecodeResult[] barcodeData)
	{
		//handle results from your custom plug-in
		displayBarcodeData(barcodeData);
	}

	private void displayBarcodeData(HSMDecodeResult[] barcodeData)
	{
		if( barcodeData.length > 0)
		{
			HSMDecodeResult firstResult = barcodeData[0];
			tvResult.setText("Result: " + firstResult.getBarcodeData());
	        tvSymb.setText("Symbology: " + firstResult.getSymbology());
	        tvLength.setText("Length: " + firstResult.getBarcodeDataLength());
	        tvDecTime.setText("Decode Time: " + firstResult.getDecodeTime() + "ms");
	        ivDecode.setImageBitmap(hsmDecoder.getLastBarcodeImage(firstResult.getBarcodeBounds()));
		}
	}

	private void initGuiElements()
	{
		//stop the device from going to sleep and hide the title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //inflate the base UI layer
        setContentView(R.layout.main);

		Button buttonDecode = (Button)findViewById(R.id.buttonDecode);
    	buttonDecode.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
            	//let HSMDecoder handle the decoding UI for you
            	hsmDecoder.scanBarcode();
            }
        });

    	Button buttonDecodeComponent = (Button)findViewById(R.id.buttonDecodeLocal);
    	buttonDecodeComponent.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
            	//create the DecodeComponentActivity intent
    	        Intent decActivityIntent = new Intent(MainActivity.this, DecodeComponentActivity.class);
    	        decActivityIntent.putExtra("DEFAULT_ENABLED", cbDefault.isChecked());
    	        decActivityIntent.putExtra("CUSTOM_ENABLED", cbCustom.isChecked());

    	        //start our own activity with an embedded HSMDecodeComponent for greater customization
            	startActivity(decActivityIntent);
            }
        });

    	cbDefault = (CheckBox)findViewById(R.id.cbDefault);
    	cbDefault.setChecked(true);
    	cbDefault.setOnCheckedChangeListener(new OnCheckedChangeListener()
    	{
    		@Override
    		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    		{
    			onDefaultClicked(isChecked);
    		}
    	});

    	cbCustom = (CheckBox)findViewById(R.id.cbCustom);
    	cbCustom.setOnCheckedChangeListener(new OnCheckedChangeListener()
    	{
    		@Override
    		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    		{
    			onCustomClicked(isChecked);
    		}
    	});

        tvResult = (TextView)findViewById(R.id.textViewRes);
        tvSymb = (TextView)findViewById(R.id.textViewSymb);
        tvLength = (TextView)findViewById(R.id.textViewLength);
        tvDecTime = (TextView)findViewById(R.id.textViewDecTime);
        ivDecode = (ImageView)findViewById(R.id.imageViewDec);
	}

	public static void addCustomPluginListener(PluginResultListener listener)
	{
		customPlugin.addResultListener(listener);
	}

	public static void removeCustomPluginListener(PluginResultListener listener)
	{
		customPlugin.removeResultListener(listener);
	}
}
