package all.crowdslaka.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Splash extends Activity{
	private int waktuLoading = 2500;
	private Thread thread;
	public void onCreate(Bundle b){
		super.onCreate(b);
		setContentView(R.layout.splash);
		thread = new Thread(){
			int counter = 0;
			public void run(){
				try{
					super.run();
					while(counter < waktuLoading){
						sleep(1000);
						counter += 1000;
					}
				}catch (Exception e){
					
				} finally{
					startActivity(new Intent(Splash.this, LoginActivity.class));
					finish();
				}
			}
		};
		thread.start();
	}
}
