package person.djuno.harmony;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.content.Context;

public class PresetFileManager {
	private File presetFile;
	private Context context;
	private String fileName;
	
	public PresetFileManager(Context context, String fileName) {
		this.context= context;
		this.fileName = fileName;
	}
	
	public void savePresets(ArrayList<PresetData> presets) {
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(context.openFileOutput(fileName, Context.MODE_PRIVATE));
			out.writeObject(presets);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<PresetData> loadPresets() {
		ObjectInput in;
		ArrayList<PresetData> result = null;
		try {
			in = new ObjectInputStream(context.openFileInput(fileName));
			result = (ArrayList<PresetData>)in.readObject();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
