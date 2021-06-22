package de.calitobundo.twitch.desktop.audio;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;


public class AudioFile {

	private static final int BUFFER_SIZE = 2048;// 1024;
	public File file;

	public AudioFile(File file) {
		this.file = file;
	}

	public AudioFile(String fileString) {
		this.file = new File(fileString);
	}

	public AudioFile(URL url) {
		this(url.getFile());
	}

	public AudioFile(String tempFileName, InputStream inputStream) {
		readStreamToTempFile(tempFileName, inputStream);
	}

//	public AudioFile(InputStream inputStream) {
//
//		try {
//			OutputStream out = new FileOutputStream(file);
//			int read = 0;
//			byte[] bytes = new byte[1024];
//			while((read = inputStream.read(bytes))!= -1){
//				out.write(bytes, 0, read);
//			}
//			out.close();
//			inputStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	private void readStreamToTempFile (String tempFileName, InputStream inputStream) {
		
		file = new File("tmp/"+tempFileName);
		try {
			OutputStream out = new FileOutputStream(file);
			int read = 0;
			byte[] bytes = new byte[BUFFER_SIZE];
			while((read = inputStream.read(bytes))!= -1){
				out.write(bytes, 0, read);
			}
			out.close();
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		


	public void play() {
		Sound sound = new Sound(); 
		sound.loadFile();
		sound.play();
	}

	private class Sound {

		AudioFormat audioFormat;
		AudioInputStream audioInputStream;
		SourceDataLine sourceDataLine;

		public void play() {
			new SoundThread().start();
		}

		private void loadFile() {

			try {
				audioInputStream = AudioSystem.getAudioInputStream(file);
				audioFormat = audioInputStream.getFormat();
				if(audioFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {

					audioInputStream = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, audioInputStream);
					audioFormat = audioInputStream.getFormat();
				}
				DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
				sourceDataLine = (SourceDataLine)AudioSystem.getLine(dataLineInfo);

			} catch (Exception e) {
				e.printStackTrace();
			} 
		}

		private class SoundThread extends Thread {

			byte buffer[] = new byte[BUFFER_SIZE]; 

			public void run() {
				try{
					sourceDataLine.open(audioFormat);
					sourceDataLine.start(); 
					int read;
					while((read = audioInputStream.read(buffer,0,buffer.length)) != -1)
					{
						if(read > 0)
						{
							sourceDataLine.write(buffer, 0, read); 
						}
					}
					sourceDataLine.drain();
					sourceDataLine.close();

				}catch(Exception e){
					System.out.println("The audio file cannot be opened! "+e);
				}
			}
		}
	}

}
