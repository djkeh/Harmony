package person.djuno.harmony;

import java.util.ArrayList;
import java.util.Stack;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	private final Util util = new Util();
	private PresetFileManager fileManager;

	// private SeekBar seekVolumn;
	private ImageView imgBack;
	private ImageView imgOnoff;
	private LayoutParams params;
	public static MediaRecorder mRecorder;
	private Thread decibelCheck;
	private TextView tvPreset;
	private TextView tvDebug;
	private SeekBar tvSeek;

	private ListView listView;
	private CustomAdapter adapter;
	private ArrayList<PresetData> list;
	private static double mEMA = 0.0; // EMA 필터가 적용된 데시벨 피크 값. getAmplitudeEMA()의 리턴값이다.
	private static final double EMA_FILTER = 0.6; // EMA 필터 계산에 사용되는 상수, 기본값 0.6
	private static final int DECIBEL_CONST = -5; // 기기간 데시벨 조절을 위한 보정값, 기본값 : 24
	private static final double AMP_CONST = 1.9; // 기기간 데시벨 조절을 위한 보정값, 기본값 : 1.9, 공식값 : 2700
	private static final long THREAD_TIME = 50; // 스레드 동작 주기 -> 소음 스캔 주기
	private static long time = THREAD_TIME;
	private static final int CIRCLE_OFFSET = 40; // 원 크기의 offset.
	private static final double CIRCLE_MULTIPLIER = 12.0; // 원 크기의 배율.
	private static boolean turnedOn = false;
	private static double lastSound = 0;
	private static final int STATIC_INTEGER_VALUE = 1;

	private static final int SAMPLING_RATE = 30; // 볼륨 조절 계산에 사용할 노이즈 샘플 개수를 결정하는 변수. 많을 수록 많은 노이즈를 수집하여 평균을 내므로 스파이크를 피하지만, 시간이 걸린다.
	private static int volumeSampling = SAMPLING_RATE;
	private static int volumeTolerance = 3; // 볼륨 조절 기준을 삼을 오차 변수. 0이면 이전 노이즈와 현재 노이즈를 정확히 비교, 수치가 커질 수록 수치 범위 내의 차이에 반응하지 않는다.
	private static int volumeRate = 1; // 볼륨 조절 단위. 한 번 조절에 얼마나 움직일 지 결정.
	private final Stack<Double> noiseCache = new Stack<Double>();

	private final Handler mHandler = new Handler();

	// 주기적으로 소음값을 가져오는 스레드
	private final Runnable updater = new Runnable() {
		@Override
		public void run() {
			updateData();

			noiseCache.push(updateData());

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		fileManager = new PresetFileManager(getApplicationContext(),
				"presets.serial");

		initTextView();
		initImageView();
		initListView();
		initSeekBar();

		if (decibelCheck == null) {
			decibelCheck = new Thread() {
				@Override
				public void run() {
					while (decibelCheck != null) {
						try {
							Thread.sleep(time);
							// Log.i("Noise", "Tock");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (turnedOn) {
							mHandler.post(updater);
						}
					}
				}
			};

			decibelCheck.start();
			Log.d("Noise", "start runner()");
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case (STATIC_INTEGER_VALUE): {
			stopService(new Intent("person.djuno.harmony"));
			if (resultCode == Activity.RESULT_OK) {
				PresetData preset = (PresetData) data
						.getSerializableExtra("preset");
				int position = data.getIntExtra("position", -1);
				if (position >= 0) {
					list.remove(position);
				}
				if (preset.getCheckPeriod() > 0) {
					list.add(list.size() - 1, preset);
					changePreset(preset);
				}
				fileManager.savePresets(new ArrayList<PresetData>(list.subList(
						2, list.size() - 1)));
				adapter.notifyDataSetChanged();
			}
			startService(new Intent("person.djuno.harmony"));
			break;
		}
		}
	}

	void initListView() {
		listView = (ListView) findViewById(R.id.list);
		list = new ArrayList<PresetData>();
		list.add(new PresetData("민감한 사람", true, 10, 10, 1));
		list.add(new PresetData("둔한 사람", true, 20, 20, 1));

		try {
			list.addAll(fileManager.loadPresets());
		} catch (Exception e) {

		}

		list.add(new PresetData("새 사용자 프리셋...", false, -1, -1, -1));

		adapter = new CustomAdapter(getApplicationContext(), 0, list,
				getAssets());
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				PresetData preset = (PresetData) arg0.getAdapter().getItem(
						position);

				if (preset.getFixed()) {
					changePreset(preset);
				} else {
					if (preset.getCheckPeriod() < 0) {
						position = -1;
					}
					Intent intent = new Intent(MainActivity.this,
							PresetDetailActivity.class);
					intent.putExtra("preset", preset);
					intent.putExtra("position", position);
					startActivityForResult(intent, STATIC_INTEGER_VALUE);
				}
			}
		});
	}

	void initTextView() {
		tvDebug = (TextView) findViewById(R.id.tv_debug);
		tvPreset = (TextView) findViewById(R.id.tv_preset);
		Typeface face = Typeface.createFromAsset(getAssets(),
				"fonts/NanumGothicExtraBold.ttf");
		tvPreset.setTypeface(face);
	}

	private void initImageView() {
		// TODO Auto-generated method stub
		imgBack = (ImageView) findViewById(R.id.img_back);
		imgOnoff = (ImageView) findViewById(R.id.img_onoff);
		imgOnoff.setOnClickListener(this);
		if (util.getSharedPreferences(getApplicationContext(), "harmony",
				"onoff", "off").equals("on")) {
			turnedOn = true;
			imgOnoff.setImageResource(R.drawable.on_btn);
			startService(new Intent("person.djuno.harmony"));
		} else {
			turnedOn = false;
			imgOnoff.setImageResource(R.drawable.off_btn);
			stopService(new Intent("person.djuno.harmony"));
		}

		params = (LayoutParams) imgBack.getLayoutParams();

	}

	private void initSeekBar() {
		tvSeek = (SeekBar) findViewById(R.id.tv_seek);
	}

	@Override
	public void onResume() {
		super.onResume();

		startRecorder();
	}

	@Override
	public void onPause() {
		super.onPause();
		stopRecorder();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_about) {
			Intent intent = new Intent(MainActivity.this, AboutActivity.class);
			startActivity(intent);
			return true;
		} else if (id == R.id.action_donate) {
			Intent intent = new Intent(MainActivity.this, DonateActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.img_onoff:
			if (util.getSharedPreferences(getApplicationContext(), "harmony",
					"onoff", "off").equals("on")) {
				turnedOn = false;
				imgOnoff.setImageResource(R.drawable.off_btn);
				util.writeSharedPreferences(getApplicationContext(), "harmony",
						"onoff", "off");
				params.height = 0;
				params.width = 0;
				imgBack.setLayoutParams(params);
				stopService(new Intent("person.djuno.harmony"));

			} else {
				turnedOn = true;
				imgOnoff.setImageResource(R.drawable.on_btn);
				util.writeSharedPreferences(getApplicationContext(), "harmony",
						"onoff", "on");
				startService(new Intent("person.djuno.harmony"));
			}
			break;

		default:
			break;
		}
	}

	public void startRecorder() {
		if (mRecorder == null) {
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mRecorder.setOutputFile("/dev/null");

			try {
				mRecorder.prepare();

			} catch (java.io.IOException ioe) {
				android.util.Log.e("[Error]", "IOException: "
						+ android.util.Log.getStackTraceString(ioe));

			} catch (java.lang.SecurityException e) {
				android.util.Log.e("[Error]", "SecurityException: "
						+ android.util.Log.getStackTraceString(e));
			}

			try {
				mRecorder.start();
			} catch (java.lang.SecurityException e) {
				android.util.Log.e("[Error]", "SecurityException: "
						+ android.util.Log.getStackTraceString(e));
			}

			// mEMA = 0.0;
		}

	}

	public void stopRecorder() {
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}
	}

	public double updateData() {
		AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		double sound = (soundDb() + DECIBEL_CONST);
		tvDebug.setText(sound + " db");
		double modifiedSound = Math.max(
				(sound + lastSound) / 2 - CIRCLE_OFFSET, 0);
		lastSound = sound;
		params.height = (int) (modifiedSound * CIRCLE_MULTIPLIER);
		params.width = (int) (modifiedSound * CIRCLE_MULTIPLIER);
		imgBack.setLayoutParams(params);

		tvSeek.setProgress((int) (audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC) / 15.0 * 100));

		return sound;
	}

	public void changePreset(PresetData preset) {
		util.showToast(getApplicationContext(),
				preset.getName() + "\n측정 주기: " + preset.getCheckPeriod()
						+ "\n측정 민감도: " + preset.getCheckSensitivity()
						+ "\n변화폭: " + preset.getVolumeChangeRate());
		time = (long) (preset.getCheckPeriod() * 1000);
		volumeTolerance = preset.getCheckSensitivity();
		volumeRate = preset.getVolumeChangeRate();
	}

	public double soundDb() {
		return 20 * Math.log10(getAmplitudeEMA() / AMP_CONST);
	}

	public double getAmplitude() {
		if (mRecorder != null)
			return (mRecorder.getMaxAmplitude());
		else
			return 0;
	}

	public double getAmplitudeEMA() {
		double amp = getAmplitude();
		mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;

		if (mEMA == 0) {
			// 첫 실행 시 마이크 소음 측정 결과가 0이면 이 값이 0이 나오고, 이후 로그 계산에 문제를 일으키므로 1을 반환하기
			// 위해 적절한 수로 대체해준다
			return AMP_CONST;
		} else {
			return mEMA;
		}
	}
}
