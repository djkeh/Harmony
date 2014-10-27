package person.djuno.harmony;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class PresetDetailActivity extends Activity {

	private SeekBar periodSeekBar;
	private SeekBar sensitivitySeekBar;
	private SeekBar rateSeekBar;

	private TextView periodTextView;
	private TextView sensitivityTextView;
	private TextView rateTextView;
	private TextView tvTitle;
	private TextView option1_title, option2_title, option3_title;
	private TextView option1_left, option2_left, option3_left;
	private TextView option1_right, option2_right, option3_right;

	private double checkPeriod;
	private int checkSensitivity;
	private int volumeChangeRate;
	
	private PresetData originalData;

	private Button confirmButton;
	private Button deleteButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_preset_detail);

		originalData = (PresetData)getIntent().getSerializableExtra("preset");

		initTextView();
		initSeekBar();
		initButton();
		
	}

	private void initSeekBar() {
		periodSeekBar = (SeekBar) findViewById(R.id.option1_seekbar);
		sensitivitySeekBar = (SeekBar) findViewById(R.id.option2_seekbar);
		rateSeekBar = (SeekBar) findViewById(R.id.option3_seekbar);

		periodSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				checkPeriod = 0.01 * Math.pow(15, progress * 3.0 / 100);
				if (checkPeriod < 0.3) {
					checkPeriod = (int) (checkPeriod * 100) / 100.0;
				} else if (checkPeriod < 1.0) {
					checkPeriod = (int) (checkPeriod * 10) / 10.0;
				} else if (checkPeriod < 10.0) {
					checkPeriod = (int) checkPeriod;
				} else if (checkPeriod < 30.0) {
					checkPeriod = (int) (checkPeriod / 2) * 2;
				} else if (checkPeriod < 50.0) {
					checkPeriod = (int) (checkPeriod / 5) * 5;
				} else {
					checkPeriod = (int) (checkPeriod / 10) * 10;
				}
				periodTextView.setText(checkPeriod + "초");
			}
		});

		sensitivitySeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				checkSensitivity = (int) Math.pow(10, progress * 2.0 / 100);
				if (checkSensitivity < 20) {
					checkSensitivity = checkSensitivity;
				} else if (checkSensitivity < 50) {
					checkSensitivity = (int) (checkSensitivity / 2.0) * 2;
				} else if (checkSensitivity < 100) {
					checkSensitivity = (int) (checkSensitivity / 5.0) * 5;
				} else if (checkSensitivity < 500) {
					checkSensitivity = (int) (checkSensitivity / 50.0) * 50;
				} else {
					checkSensitivity = (int) (checkSensitivity / 100.0) * 100;
				}
				sensitivityTextView.setText(checkSensitivity + "번");
			}
		});

		rateSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				volumeChangeRate = progress / 25 + 1;
				rateTextView.setText(String.valueOf(volumeChangeRate));
			}
		});

		periodSeekBar.incrementProgressBy(1);
		sensitivitySeekBar.incrementProgressBy(1);
		rateSeekBar.incrementProgressBy(1);
		
		if (originalData.getCheckPeriod() > 0) {
			/*
			checkPeriod = originalData.getCheckPeriod();
			checkSensitivity = originalData.getCheckSensitivity();
			volumeChangeRate = originalData.getVolumeChangeRate();
			periodTextView.setText(checkPeriod + "초");
			sensitivityTextView.setText(checkSensitivity + "번");
			rateTextView.setText(String.valueOf(volumeChangeRate));
			*/
			periodSeekBar.setProgress((int)(100 / 3.0 * Math.log(100 * originalData.getCheckPeriod()) / Math.log(15)) + 1);
			sensitivitySeekBar.setProgress((int)(50 * Math.log10(originalData.getCheckSensitivity())) + 1);
			rateSeekBar.setProgress(originalData.getVolumeChangeRate() * 20);
		}

	}

	private void initTextView() {
		periodTextView = (TextView) findViewById(R.id.option1_value);
		sensitivityTextView = (TextView) findViewById(R.id.option2_value);
		rateTextView = (TextView) findViewById(R.id.option3_value);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		option1_title = (TextView) findViewById(R.id.option1_title);
		option2_title = (TextView) findViewById(R.id.option2_title);
		option3_title = (TextView) findViewById(R.id.option3_title);
		option1_left = (TextView) findViewById(R.id.option1_left_text);
		option2_left = (TextView) findViewById(R.id.option2_left_text);
		option3_left = (TextView) findViewById(R.id.option3_left_text);
		option1_right = (TextView) findViewById(R.id.option1_right_text);
		option2_right = (TextView) findViewById(R.id.option2_right_text);
		option3_right = (TextView) findViewById(R.id.option3_right_text);

		Typeface face = Typeface.createFromAsset(getAssets(), "fonts/NanumGothicExtraBold.ttf");
		tvTitle.setTypeface(face);
		periodTextView.setTypeface(face);
		sensitivityTextView.setTypeface(face);
		rateTextView.setTypeface(face);
		option1_title.setTypeface(face);
		option2_title.setTypeface(face);
		option3_title.setTypeface(face);
		option1_left.setTypeface(face);
		option2_left.setTypeface(face);
		option3_left.setTypeface(face);
		option1_right.setTypeface(face);
		option2_right.setTypeface(face);
		option3_right.setTypeface(face);
	}

	private void initButton() {
		confirmButton = (Button) findViewById(R.id.button_confirm);
		deleteButton = (Button) findViewById(R.id.button_delete);
		Typeface face = Typeface.createFromAsset(getAssets(), "fonts/NanumGothicExtraBold.ttf");
		confirmButton.setTypeface(face);
		deleteButton.setTypeface(face);
		confirmButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("preset", new PresetData("사용자 프리셋 (" + checkPeriod + "초 / " + checkSensitivity + "번 / " + volumeChangeRate + ")",
						false, checkPeriod, checkSensitivity, volumeChangeRate));
				intent.putExtra("position", getIntent().getIntExtra("position", -1));
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		deleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("preset", new PresetData("삭제", false, -1, -1, -1));
				intent.putExtra("position", getIntent().getIntExtra("position", getIntent().getIntExtra("position", -1)));
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}
}
