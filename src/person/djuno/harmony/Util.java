package person.djuno.harmony;

import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class Util {

	private static final float	DEFAULT_HDIP_DENSITY_SCALE	= 1.5f;

	// 토스트
	public void showToast(Context c, String msg) {
		Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();

	}

	public void removeSharedPreferences(Context c, String file, String key) {
		Editor ed = c.getSharedPreferences(file, Context.MODE_PRIVATE).edit();

		ed.remove(key);
		ed.commit();
	}

	// 프리퍼런스 <쓰기>
	public void writeSharedPreferences(Context c, String file, String key, String value) {
		Editor ed = c.getSharedPreferences(file, Context.MODE_PRIVATE).edit();
		ed.putString(key, value);
		ed.commit();
	}

	// 프리퍼런스<받아오기>
	public String getSharedPreferences(Context c, String file, String key, String deafult) {
		String value = c.getSharedPreferences(file, Context.MODE_PRIVATE).getString(key, deafult);
		return value;
	}

	// 폰 UID 불러오기
	public String getUniqueDeviceId(Context c) {
		final TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);

		final String tmDevice, tmSerial, androidId;
		tmDevice = "" + tm.getDeviceId();
		tmSerial = "" + tm.getSimSerialNumber();
		androidId = ""
				+ android.provider.Settings.Secure.getString(c.getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID);

		UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
		String deviceId = deviceUuid.toString();
		return deviceId;
	}

	// 인터넷연결확인
	public boolean isConnected(Context context) {
		boolean connected = false;
		ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		State mobile = conMan.getNetworkInfo(0).getState(); // mobile
		State wifi = conMan.getNetworkInfo(1).getState(); // wifi
		if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
			connected = true;
		} else if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
			connected = true;
		}
		return connected;
	}

	/**
	 * 픽셀단위를 현재 디스플레이 화면에 비례한 크기로 반환합니다.
	 * 
	 * @param pixel
	 *            픽셀
	 * @return 변환된 값 (DP)
	 */
	public int DPFromPixel(Context context, int pixel) {
		float scale = context.getResources().getDisplayMetrics().density;

		return (int) (pixel / DEFAULT_HDIP_DENSITY_SCALE * scale);
	}

	/**
	 * 현재 디스플레이 화면에 비례한 DP단위를 픽셀 크기로 반환합니다.
	 * 
	 * @param DP
	 *            픽셀
	 * @return 변환된 값 (pixel)
	 */
	public int PixelFromDP(Context context, int DP) {
		float scale = context.getResources().getDisplayMetrics().density;

		return (int) (DP / scale * DEFAULT_HDIP_DENSITY_SCALE);
	}

	public String StringXml(Context context, int id) {
		return context.getResources().getString(id);
	}
}
