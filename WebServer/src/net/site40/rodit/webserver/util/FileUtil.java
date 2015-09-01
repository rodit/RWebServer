package net.site40.rodit.webserver.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
	
	private static Log log = new Log("FileUtil");

	public static String readString(String file){
		return new String(read(file));
	}

	public static byte[] read(String file){
		return read(new File(file));
	}

	public static String readString(File file){
		return new String(read(file));
	}

	public static byte[] read(File file){
		try{
			FileInputStream fin = new FileInputStream(file);
			byte[] buffer = new byte[fin.available()];
			fin.read(buffer);
			fin.close();
			log.i("Read file " + file.getParent() + ".");
			return buffer;
		}catch(IOException e){
			log.e("Error while reading file '" + file.getPath() + "' - " + e.getMessage());
		}
		return new byte[0];
	}

	public static byte[] read(InputStream in){
		try{
			byte[] data = new byte[in.available()];
			in.read(data);
			return data;
		}catch(IOException e){
			log.e("Error while reading from input stream - " + e.getMessage());
		}
		return new byte[0];
	}

	public static void write(String file, String data){
		write(file, data.getBytes());
	}

	public static void write(String file, byte[] data){
		write(new File(file), data);
	}

	public static void write(File file, String data){
		write(file, data.getBytes());
	}
	
	public static void write(File file, String data, boolean append){
		write(file, data.getBytes(), append);
	}

	public static void write(File file, byte[] data){
		write(file, data, false);
	}

	public static void write(File file, byte[] data, boolean append){
		try{
			FileOutputStream fout = new FileOutputStream(file, append);
			fout.write(data);
			fout.flush();
			fout.close();
		}catch(IOException e){
			log.e("Error while writing file '" + file.getPath() + "' - " + e.getMessage());
		}
	}

	public static void touch(String file){
		touch(new File(file));
	}

	public static void touch(File file){
		try{
			file.createNewFile();
		}catch(IOException e){
			log.e("Error while creating file '" + file.getParent() + "' - " + e.getMessage());
		}
	}

	public static String[] list(String dir){
		return list(new File(dir));
	}

	public static String[] list(File dir){
		return dir.list();
	}

	public static long size(String file){
		return new File(file).length();
	}

	public static boolean exists(String file){
		return new File(file).exists();
	}

	public static boolean isFile(String file){
		return new File(file).isFile();
	}

	public static boolean isDirectory(String file){
		return new File(file).isDirectory();
	}
	
	public static void runAsync(String cmd){
		final String fcmd = cmd;
		new Thread(){
			@Override
			public void run(){
				FileUtil.run(fcmd);
			}
		}.start();
	}
	
	public static String runString(String cmd){
		return new String(run(cmd));
	}

	public static byte[] run(String cmd){
		try{
			Process p = Runtime.getRuntime().exec(cmd);
			InputStream is = p.getInputStream();
			byte[] buffer = new byte[4096];
			byte[] full = new byte[0];
			while(is.read(buffer) > -1){
				byte[] newDat = new byte[buffer.length + full.length];
				System.arraycopy(full, 0, newDat, 0, full.length);
				System.arraycopy(buffer, 0, newDat, full.length, buffer.length);
				full = newDat;
			}
			return full;
		}catch(IOException e){
			log.e("Error while executing command " + e.getMessage());
		}
		return new byte[0];
	}
}
