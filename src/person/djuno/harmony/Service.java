/**
 * 
 */
package person.djuno.harmony;

import java.util.Stack;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class Service extends android.app.Service {

	private AudioManager audioManager;
	private volatile Thread decibelCheck;

	private static double mEMA = 0.0; // EMA 필터가 적용된 데시벨 피크 값. getAmplitudeEMA()의 리턴값이다.
	private static final double EMA_FILTER = 0.6; // EMA 필터 계산에 사용되는 상수, 기본값 0.6
	private static final int DECIBEL_CONST = -5; // 기기간 데시벨 조절을 위한 보정값, 기본값 : 24
	private static final double AMP_CONST = 1.9; // 기기간 데시벨 조절을 위한 보정값, 기본값 : 1.9, 공식값 : 2700
	private static final long THREAD_TIME = 100; // 스레드 동작 주기 -> 소음 스캔 주기
	private static long time = THREAD_TIME;
	private static boolean turnedOn = true;

	private static final int SAMPLING_RATE = 40; // 볼륨 조절 계산에 사용할 노이즈 샘플 개수를 결정하는 변수. 많을 수록 많은 노이즈를 수집하여 평균을 내므로 스파이크를 피하지만, 시간이 걸린다.
	private static int volumeSampling = SAMPLING_RATE;
	private static int volumeTolerance = 6; // 볼륨 조절 기준을 삼을 오차 변수. 0이면 이전 노이즈와 현재 노이즈를 정확히 비교, 수치가 커질 수록 수치 범위 내의 차이에 반응하지 않는다.
	private static int volumeRate = 1; // 볼륨 조절 단위. 한 번 조절에 얼마나 움직일 지 결정.
	private Stack<Double> noiseCache;
	private double averageCache = 0.0;
	private double initialNoise;
	private int initialVolume;

	private final Handler mHandler = new Handler();

	private boolean running = true;

	// 주기적으로 소음값을 가져오는 스레드
	private final Runnable updater = new Runnable() {
		@Override
		public void run() {
			noiseCache.push(updateData());
			if (noiseCache.size() == volumeSampling) {
				double sum = 0.0;
				double average;
				for (int i = 0; i < volumeSampling; i++) {
					sum += noiseCache.pop();
				}
				average = sum / volumeSampling;
				Log.d("volume-", "avaerageCache = " + averageCache
						+ ", average = " + Double.toString(average));

				// 실행 초기, averageCache 값 없고 average 마이크 소음 측정이 없는 초기 상태
				if (averageCache == 0.0 && average <= DECIBEL_CONST) {
					// do nothing
				}
				// averageCache 없고 마이크 소음 측정 중인 상태
				else if (averageCache == 0.0) {
					averageCache = average;
					initialNoise = average;
					initialVolume = audioManager
							.getStreamVolume(AudioManager.STREAM_MUSIC);
					Log.d("volume-", "second init complete");
				} else {
					// int sign = (average - averageCache >= 0) ? 1 : -1;

					if (Math.abs(average - initialNoise) > volumeTolerance) {
						int volume = initialVolume + (int) (average - initialNoise) * 2 * volumeRate / 5 / volumeTolerance;
						if (volume >= audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
							volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
						} else if (volume <= 1) {
							volume = 1;
						}

						// change volume
						audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
								volume, 0);
					}

					averageCache += (average - averageCache) / 2;

					Log.d("volume-",
							"cache = " + String.format("%.1f", averageCache)
							+ ", average = " + String.format("%.1f", average)
							+ ", volume = "	+ audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

				}
			}

		}
	};

	public void startRecorder() {
		if (MainActivity.mRecorder == null) {
			MainActivity.mRecorder = new MediaRecorder();
			MainActivity.mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			MainActivity.mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			MainActivity.mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			MainActivity.mRecorder.setOutputFile("/dev/null");

			try {
				MainActivity.mRecorder.prepare();

			} catch (java.io.IOException ioe) {
				android.util.Log.e("[Error]", "IOException: "
						+ android.util.Log.getStackTraceString(ioe));

			} catch (java.lang.SecurityException e) {
				android.util.Log.e("[Error]", "SecurityException: "
						+ android.util.Log.getStackTraceString(e));
			}

			try {
				MainActivity.mRecorder.start();
			} catch (java.lang.SecurityException e) {
				android.util.Log.e("[Error]", "SecurityException: "
						+ android.util.Log.getStackTraceString(e));
			}

			// mEMA = 0.0;
		}

	}

	public void stopRecorder() {
		if (MainActivity.mRecorder != null) {
			MainActivity.mRecorder.stop();
			MainActivity.mRecorder.release();
			MainActivity.mRecorder = null;
		}
	}

	public double updateData() {
		double sound = (soundDb() + DECIBEL_CONST);

		return sound;
	}

	public double soundDb() {
		return 20 * Math.log10(getAmplitudeEMA() / AMP_CONST);
	}

	public double getAmplitudeEMA() {
		double amp = getAmplitude();
		mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;

		if (mEMA == 0) {
			// 첫 실행 시 마이크 소음 측정 결과가 0이면 이 값이 0이 나오고, 이후 로그 계산에 문제를 일으키므로 1을 반환하기 위해 적절한 수로 대체해준다
			return AMP_CONST;
		} else {
			return mEMA;
		}
	}

	public double getAmplitude() {
		if (MainActivity.mRecorder != null)
			return (MainActivity.mRecorder.getMaxAmplitude());
		else
			return 0;
	}

	public void changePreset(PresetData preset) {
		time = (long) (preset.getCheckPeriod() * 1000);
		volumeTolerance = preset.getCheckSensitivity();
		volumeRate = preset.getVolumeChangeRate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		noiseCache = new Stack<Double>();
		
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		startThread();

	}

	public synchronized void startThread() {
		if (decibelCheck == null) {
			decibelCheck = new Thread() {
				@Override
				public void run() {
					while (Thread.currentThread() == decibelCheck) {
						// while (decibelCheck != null) {
						while (running) {
							try {
								Thread.sleep(time);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							if (turnedOn) {
								mHandler.post(updater);
							} else {
								averageCache = 0.0;
							}
						}
					}
				}
			};
			decibelCheck.start();
		}
	}

	public synchronized void stopThread() {
		if (decibelCheck != null) {
			Thread moribund = decibelCheck;
			decibelCheck = null;
			moribund.interrupt();
		}
	}

	@Override
	public void onDestroy() {
		running = false;
		stopThread();
		super.onDestroy();

	}
}