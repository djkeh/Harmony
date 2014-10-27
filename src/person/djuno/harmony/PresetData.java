/**
 * 
 */
package person.djuno.harmony;

import java.io.Serializable;

/**
 * Created by skplanet on 2014. 4. 26.
 * 
 */
public class PresetData implements Serializable {
	private String name;
	private final boolean fixed; // 기본적으로 제공하는 preset은 true, 사용자 지정은 false.
	private final double checkPeriod; // 확인 주기
	private final int checkSensitivity; // 확인 민감도
	private final int volumeChangeRate; // 볼륨 변경폭

	public PresetData(String name, boolean fixed, double checkPeriod, int checkSensitivity, int volumeChangeRate) {
		super();
		this.name = name;
		this.fixed = fixed;
		this.checkPeriod = checkPeriod;
		this.checkSensitivity = checkSensitivity;
		this.volumeChangeRate = volumeChangeRate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getFixed() {
		return fixed;
	}

	public double getCheckPeriod() {
		return checkPeriod;
	}

	public int getCheckSensitivity() {
		return checkSensitivity;
	}

	public int getVolumeChangeRate() {
		return volumeChangeRate;
	}

}
