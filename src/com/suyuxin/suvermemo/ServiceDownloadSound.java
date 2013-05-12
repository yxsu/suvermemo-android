package com.suyuxin.suvermemo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ServiceDownloadSound extends IntentService {

	private static final String WEBSTER_SOUND_HOST = "http://media.merriam-webster.com/soundc11/";
	private static final String WEBSTER_HOST = "http://www.merriam-webster.com/dictionary/";
	private static final String ROOT_PATH = DataActivity.EXTERNAL_ROOT_PATH;
	File root_path;
	public ServiceDownloadSound() {
		super("Name");
		// TODO Auto-generated constructor stub
		//check external storage
		root_path = new File(Environment.getExternalStorageDirectory(), ROOT_PATH);
		if(!root_path.exists())
		{
			root_path.mkdirs();
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		String word = intent.getStringExtra("word");
		int notification_id = intent.getIntExtra("id", 0);
		String word_flag = "";
		//prepare for notification bar
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentText(word);
		builder.setContentTitle("Failed");
		//download
		try {
			//get the correct word flag
			URLConnection connection = new URL(WEBSTER_HOST + word).openConnection();
			Scanner in = new Scanner(connection.getInputStream());
			String temp;
			while(in.hasNext())
			{
				temp = in.nextLine();
				int index = temp.indexOf("return au(");
				if(index != -1)
				{
					temp = temp.substring(index);
					String[] split = temp.split(" ");
					if(split != null && split.length > 2)
						word_flag = split[1].substring(4, split[1].length() - 2);
					break;
				}
			}
			if(!word_flag.equals(""))
			{//download the audio file
				connection = new URL(WEBSTER_SOUND_HOST +
							word_flag.charAt(0) + "/" + word_flag + ".wav").openConnection();
				//copy
				File sound_file = new File(root_path, word + ".wav");
				FileOutputStream output = new FileOutputStream(sound_file, false);
				connection.connect();
				InputStream input = new BufferedInputStream(connection.getInputStream());
				byte[] buffer = new byte[1024];
				int count = 0;
				while((count = input.read(buffer)) != -1)
				{
					output.write(buffer, 0, count);
				}
				output.flush();
				output.close();
				input.close();
				builder.setContentTitle("Sound Ready!");
				notification_id = 0;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NotificationManager manager = 
				(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(notification_id, builder.build());
	}
	
}
