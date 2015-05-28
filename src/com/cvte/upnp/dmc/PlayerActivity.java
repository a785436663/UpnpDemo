package com.cvte.upnp.dmc;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.TransportInfo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.cvte.upnp.demo.BrowseActivity;
import com.cvte.upnp.demo.BrowserUpnpService;
import com.cvte.upnp.demo.R;

public class PlayerActivity extends Activity implements OnClickListener,
		GetCoshipavtransportstate {
	TextView txt_name, txt_time;
	SeekBar seekbar;
	SeekBar seekbar_volume;
	Button btn_play;
	Handler seekBarHandler = new Handler();
	private AndroidUpnpService upnpService;
	private ServiceConnection serviceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			upnpService = (AndroidUpnpService) service;
			seekBarHandler.postDelayed(seekR, 500);
		}

		public void onServiceDisconnected(ComponentName className) {
			upnpService = null;
		}
	};

	Runnable seekR = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			CoshipAvtransprot.GetPositionInfo(upnpService,
					BrowseActivity.device, PlayerActivity.this);
			CoshipAvtransprot.GetMediaInfo(upnpService, BrowseActivity.device,
					PlayerActivity.this);
			CoshipAvtransprot.GetDmrTransportInfo(upnpService,
					BrowseActivity.device, PlayerActivity.this);
			CoshipAvtransprot.getvolume(upnpService, BrowseActivity.device,
					PlayerActivity.this);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		txt_name = (TextView) findViewById(R.id.txt_name);
		txt_time = (TextView) findViewById(R.id.txt_time);
		seekbar = (SeekBar) findViewById(R.id.seekbar);
		seekbar_volume = (SeekBar) findViewById(R.id.seekbar_volume);
		btn_play = (Button) findViewById(R.id.btn_play);
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				int time = seekBar.getProgress();
				String timeStr = getTimeByInt(time);
				CoshipAvtransprot.mediaRenderSeek(upnpService,
						BrowseActivity.device, timeStr, PlayerActivity.this);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub

			}
		});
		seekbar_volume
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						CoshipAvtransprot.setvolume(upnpService,
								BrowseActivity.device, seekBar.getProgress(),
								PlayerActivity.this);
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						// TODO Auto-generated method stub

					}
				});
		// BrowseActivity.device
		getApplicationContext().bindService(
				new Intent(this, BrowserUpnpService.class), serviceConnection,
				Context.BIND_AUTO_CREATE);
	}

	private String getTimeByInt(int time) {
		// TODO Auto-generated method stub
		int hour = time / (60 * 60);
		int min = (time % (60 * 60)) / 60;
		int sec = (time % (60 * 60)) % 60;
		return hour + ":" + min + ":" + sec;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_play:
			if (btn_play.getText().toString().equals("点击暂停")) {
				System.out.println("点击了暂停");
				CoshipAvtransprot.mediaRenderpause(upnpService,
						BrowseActivity.device, this);
			} else if (btn_play.getText().toString().equals("点击播放")) {
				System.out.println("点击了播放");
				CoshipAvtransprot.mediaRemenderplay(upnpService,
						BrowseActivity.device, this);
			}
			break;
		case R.id.btn_stop:
			CoshipAvtransprot.mediaRemenderstop(upnpService,
					BrowseActivity.device, this);
			break;
		default:
			break;
		}
	}

	@Override
	public void setavtransportsuccess() {
		// TODO Auto-generated method stub

	}

	@Override
	public void mediarenderplaysuccess() {
		// TODO Auto-generated method stub

	}

	@Override
	public void mediarenderstopsuccess() {
		// TODO Auto-generated method stub

	}

	@Override
	public void mediarenderseeksuccess() {
		// TODO Auto-generated method stub

	}

	@Override
	public void mediarenderpausesuccess() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getVolumeSuccess(int volume) {
		// TODO Auto-generated method stub
		seekbar_volume.setProgress(volume);
	}

	@Override
	public void getmediarenderposinfosuccess(final PositionInfo positionInfo) {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// 获取播放位置
				seekbar.setMax((int) positionInfo.getTrackDurationSeconds());
				seekbar.setProgress((int) positionInfo.getTrackElapsedSeconds());
				// positionInfo.getRelTime()//当前时间
				txt_time.setText(positionInfo.getRelTime() + "/"
						+ positionInfo.getTrackDuration());
				seekBarHandler.postDelayed(seekR, 1000);
			}
		});
	}

	@Override
	public void getmediarendermediainfosuccess(MediaInfo mediaInfo) {
		// TODO Auto-generated method stub
		// 获取mediarender的媒体信息
		// Log.e("mediaInfo",
		// "mediaInfo:" + mediaInfo.getCurrentURI() + "\n"
		// + mediaInfo.getMediaDuration());//获取总时间

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getApplicationContext().unbindService(serviceConnection);
	}

	@Override
	public void getTransportInfoSuccess(final TransportInfo transportInfo) {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (transportInfo.getCurrentTransportState().toString()
						.equals("PAUSED_PLAYBACK")) {
					btn_play.setText("点击播放");
				} else if (transportInfo.getCurrentTransportState().toString()
						.equals("PLAYING")) {
					btn_play.setText("点击暂停");
				} else if (transportInfo.getCurrentTransportState().toString()
						.equals("STOPPED")) {
					btn_play.setText("已停止");
				} else if (transportInfo.getCurrentTransportState().toString()
						.equals("TRANSITIONING")) {
					btn_play.setText("过渡状态");
				}
			}
		});
	}

	@Override
	public void fail(String info) {
		// TODO Auto-generated method stub
		Toast.makeText(this, info, Toast.LENGTH_LONG).show();
	}
}
