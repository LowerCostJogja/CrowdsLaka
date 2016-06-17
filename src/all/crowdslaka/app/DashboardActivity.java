package all.crowdslaka.app;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import all.crowdslaka.app.library.GpsService;
import all.crowdslaka.app.library.JSONParser;
import all.crowdslaka.app.library.UserFunctions;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

public class DashboardActivity extends Activity {
	
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	private static final int MEDIA_TYPE_IMAGE = 1;
	private static final String IMAGE_DIRECTORY_NAME = "Hello camera";
	private Uri fileUri;
	private ImageView imgPreview;
	private Button btnAmbilFoto;
	
	EditText tnkb, nama, alamat, lokasi, kronologi;
	Button submit;
	JSONParser jParser = new JSONParser();
	ProgressDialog pDialog;
	private static String url = "http://10.0.3.2/LakaLantas/insert_laporan.php";
	GpsService gps;
	
	UserFunctions userFunctions;
	Button btnLogout;
	
	RadioGroup Kondisi;
	String type;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        
        tnkb = (EditText)findViewById(R.id.tnkb);
        nama = (EditText)findViewById(R.id.nama);
        alamat = (EditText)findViewById(R.id.alamat);
        lokasi = (EditText)findViewById(R.id.lokasi);
        Kondisi=(RadioGroup) findViewById(R.id.kondisi);
        kronologi = (EditText)findViewById(R.id.kronologi);
        imgPreview = (ImageView)findViewById(R.id.imgView);
        
        /**
         * Dashboard Screen for the application
         * */        
        // Check login status in database
        userFunctions = new UserFunctions();
        if(userFunctions.isUserLoggedIn(getApplicationContext())){
        	btnLogout = (Button) findViewById(R.id.btnLogout);
        	btnLogout.setOnClickListener(new View.OnClickListener() {
    			
    			public void onClick(View arg0) {
    				// TODO Auto-generated method stub
    				userFunctions.logoutUser(getApplicationContext());
    				Intent login = new Intent(getApplicationContext(), LoginActivity.class);
    	        	login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	        	startActivity(login);
    	        	// Closing dashboard screen
    	        	finish();
    			}
    		});
        	
        }else{
        	// user is not logged in show login screen
        	Intent login = new Intent(getApplicationContext(), LoginActivity.class);
        	login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        	startActivity(login);
        	// Closing dashboard screen
        	finish();
        }
        
        submit = (Button)findViewById(R.id.btnsubmit);
        submit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				switch (Kondisi.getCheckedRadioButtonId()) {
	             	case R.id.lukaringan:
	             		setType("Luka ringan");
	             	break;
	             	case R.id.lukaberat:
	             		setType("Luka berat");
	             	break;
	             	case R.id.meninggaldunia:
	             		setType("Meninggal dunia");
	             	break;
				}

				gps = new GpsService(DashboardActivity.this);
				if (gps.canGetLocation()){
					double latitude = gps.getLatitude();
					double longitude = gps.getLongitude();
					new Input(latitude, longitude).execute();
				}else{
					gps.showSettingAlert();
				}
			}
		});
        
        btnAmbilFoto = (Button)findViewById(R.id.btnImage);
        btnAmbilFoto.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) {
				captureImage();
			}
		});
    }
        
	private void captureImage() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
		startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
	}
	
	protected void onActivityForResult(int requestCode, int resultCode, Intent data){
		if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE){
			if (resultCode == RESULT_OK){
				previewCaptureImage();
			}else if (resultCode == RESULT_CANCELED){
				Toast.makeText(getApplicationContext(), "Cancelled images campture", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(getApplicationContext(), "Failed to capture images", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void previewCaptureImage(){
		try{
			imgPreview.setVisibility(View.VISIBLE);
			BitmapFactory.Options option = new BitmapFactory.Options();
			option.inSampleSize = 8;
			final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), option);
			imgPreview.setImageBitmap(bitmap);
		}catch (NullPointerException e){
			e.printStackTrace();
		}
	}
	
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    outState.putParcelable("file_uri", fileUri);
	}
	 
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	 
	    fileUri = savedInstanceState.getParcelable("file_uri");
	}
	
	public Uri getOutputMediaFileUri(int mediaTypeImage) {
	    return Uri.fromFile(getOutputMediaFile(mediaTypeImage));
	}
	
	private static File getOutputMediaFile(int mediaTypeImage) {
		File mediaStorageDir = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        IMAGE_DIRECTORY_NAME);
		// membuat direktori penyimpanan jika belum ada
	    if (!mediaStorageDir.exists()) {
	        if (!mediaStorageDir.mkdirs()) {
	            Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
	                    + IMAGE_DIRECTORY_NAME + " directory");
	            return null;
	        }
	    }
	    // Membuat nama media
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
	            Locale.getDefault()).format(new Date());
	    File mediaFile;
	    if (mediaTypeImage == MEDIA_TYPE_IMAGE) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator
	                + "IMG_" + timeStamp + ".jpg");
	    } else {
	        return null;
	    }
	    return mediaFile;

	}
	
	

    protected void setType(String string) {
		this.type=string;
	}
    
    public String getType(){
    	return this.type;
    }
	
	public class Input extends AsyncTask<String, String, String> {
    	private double latitude, longitude;
    	public Input(double latitude, double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}

    	String success;
		private String[] arg;
		protected void onPreExecute(){
			super.onPreExecute();
			pDialog = new ProgressDialog(DashboardActivity.this);
			pDialog.setMessage("Processing...");
			pDialog.setIndeterminate(false);
			pDialog.show();
		}
		
		@Override
		protected String doInBackground(String... arg0) {
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("tnkb", tnkb.getText().toString()));
            params.add(new BasicNameValuePair("nama", nama.getText().toString()));
            params.add(new BasicNameValuePair("alamat", alamat.getText().toString()));
            params.add(new BasicNameValuePair("lokasi", lokasi.getText().toString()));
            params.add(new BasicNameValuePair("kondisi", getType()));
            params.add(new BasicNameValuePair("latitude", Double.toString(latitude)));
            params.add(new BasicNameValuePair("longitude", Double.toString(longitude)));
            params.add(new BasicNameValuePair("kronologi", kronologi.getText().toString()));
			
            try{
            	JSONObject json = jParser.makeHttpRequest(url, "POST", params);
            	success = json.getString("success");
            }catch (Exception e){
            	Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
            }
			return null;
		}
		
		protected void onPostExecute(String file_url) {
			pDialog.dismiss();
			if (success.equals("1")){
				Toast.makeText(getApplicationContext(), "Success!!", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(getApplicationContext(), "Failed!!", Toast.LENGTH_LONG).show();
			}
		}
    }
}