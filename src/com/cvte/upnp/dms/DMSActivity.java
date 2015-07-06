package com.cvte.upnp.dms;

import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.support.contentdirectory.DIDLParser;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.DescMeta;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PersonWithRole;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.TransportInfo;
import org.teleal.cling.support.model.WriteStatus;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.ImageItem;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.MusicTrack;
import org.teleal.cling.support.model.item.VideoItem;
import org.teleal.common.util.MimeType;
import org.xml.sax.Attributes;
import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.cvte.upnp.demo.BrowseActivity;
import com.cvte.upnp.demo.BrowserUpnpService;
import com.cvte.upnp.demo.R;
import com.cvte.upnp.dmc.CoshipAvtransprot;
import com.cvte.upnp.dmc.GetCoshipavtransportstate;

public class DMSActivity extends Activity implements OnItemClickListener,
		GetCoshipavtransportstate {
	private final static String LOGTAG = "DMS";
	private static final Logger log = Logger.getLogger(DMSActivity.class
			.getName());
	private AndroidUpnpService upnpService;
	private MediaServer mediaServer;
	private static boolean serverPrepared = false;
	private ListView listV1, listV2;
	private SeekBar seekbar;
	ContentAdapter adapter1;
	ContentDetailAdapter adapter2;
	List<Container> items = new ArrayList<Container>();
	List<Item> contentItem = new ArrayList<Item>();
	private ServiceConnection serviceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			upnpService = (AndroidUpnpService) service;

			// Register the device when this activity binds to the service for
			// the first time

			if (mediaServer == null) {
				try {
					InetAddress address = getLocalIpAddress();

					mediaServer = new MediaServer(address);
					// upnpService.getRegistry()
					// .addDevice(mediaServer.getDevice());
					prepareMediaServer();
					Toast.makeText(
							DMSActivity.this,
							R.string.registering_demo_device + "----"
									+ mediaServer.getAddress(),
							Toast.LENGTH_SHORT).show();
					Log.i("dms", mediaServer.getAddress() + "----"
							+ mediaServer.getDevice().toString());
					Log.i("dms", "getDetails----"
							+ mediaServer.getDevice().getDetails()
									.getPresentationURI());

				} catch (Exception ex) {
					// TODO: handle exception
					log.log(Level.SEVERE, "Creating demo device failed", ex);
					Toast.makeText(DMSActivity.this,
							R.string.create_demo_failed, Toast.LENGTH_SHORT)
							.show();
					return;
				}
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			upnpService = null;
		}
	};

	public List<Container> getList() {
		List<Container> listRes = ContentTree.getRootNode().getContainer()
				.getContainers();
		return listRes;
	}

	String uri;
	String title;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.demo);

		listV1 = (ListView) findViewById(R.id.list1);
		listV1.setOnItemClickListener(this);

		listV2 = (ListView) findViewById(R.id.list2);
		seekbar = (SeekBar) findViewById(R.id.seekbar);
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				CoshipAvtransprot.setvolume(upnpService, BrowseActivity.device,
						seekBar.getProgress(), DMSActivity.this);
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
		adapter2 = new ContentDetailAdapter(this, contentItem);
		listV2.setAdapter(adapter2);
		listV2.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				try {
					if (BrowseActivity.device != null) {
						uri = "http://" + mediaServer.getAddress() + "/"
								+ contentItem.get(position).getId();
						title = contentItem.get(position).getTitle();
						DIDLContent didl = new DIDLContent();
						didl.addItem(contentItem.get(position));
						String meta = new DIDLParser().generate(didl);
						CoshipAvtransprot.mediaRemendersetAVTransportURI(
								upnpService, BrowseActivity.device, uri, meta,
								contentItem.get(position).getTitle(),
								DMSActivity.this);
						CoshipAvtransprot.GetDmrTransportInfo(upnpService,
								BrowseActivity.device, null);
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		});

		getApplicationContext().bindService(
				new Intent(this, BrowserUpnpService.class), serviceConnection,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		contentItem.clear();
		contentItem.addAll(items.get(position).getItems());
		adapter2.refreshData(items.get(position).getItems());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mediaServer.shutDownService();
		mediaServer = null;
		getApplicationContext().unbindService(serviceConnection);
	}

	private void prepareMediaServer() {

		if (serverPrepared) {
			items.addAll(getList());
			adapter1 = new ContentAdapter(DMSActivity.this, items);
			listV1.setAdapter(adapter1);
			return;
		}

		ContentNode rootNode = ContentTree.getRootNode();
		// Video Container
		Container videoContainer = new Container();
		videoContainer.setClazz(new DIDLObject.Class("object.container"));
		videoContainer.setId(ContentTree.VIDEO_ID);
		videoContainer.setParentID(ContentTree.ROOT_ID);
		videoContainer.setTitle("Videos");
		videoContainer.setRestricted(true);
		videoContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
		videoContainer.setChildCount(0);

		rootNode.getContainer().addContainer(videoContainer);
		rootNode.getContainer().setChildCount(
				rootNode.getContainer().getChildCount() + 1);
		ContentTree.addNode(ContentTree.VIDEO_ID, new ContentNode(
				ContentTree.VIDEO_ID, videoContainer));

		Cursor cursor;
		String[] videoColumns = { MediaStore.Video.Media._ID,
				MediaStore.Video.Media.TITLE, MediaStore.Video.Media.DATA,
				MediaStore.Video.Media.ARTIST,
				MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.SIZE,
				MediaStore.Video.Media.DURATION,
				MediaStore.Video.Media.RESOLUTION };
		cursor = managedQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				videoColumns, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				String id = ContentTree.VIDEO_PREFIX
						+ cursor.getInt(cursor
								.getColumnIndex(MediaStore.Video.Media._ID));
				String title = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
				String creator = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
				String filePath = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
				String mimeType = cursor
						.getString(cursor
								.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
				long size = cursor.getLong(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
				long duration = cursor
						.getLong(cursor
								.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
				String resolution = cursor
						.getString(cursor
								.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION));
				Res res = new Res(new MimeType(mimeType.substring(0,
						mimeType.indexOf('/')), mimeType.substring(mimeType
						.indexOf('/') + 1)), size, "http://"
						+ mediaServer.getAddress() + "/" + id);
				res.setDuration(duration / (1000 * 60 * 60) + ":"
						+ (duration % (1000 * 60 * 60)) / (1000 * 60) + ":"
						+ (duration % (1000 * 60)) / 1000);
				res.setResolution(resolution);

				VideoItem videoItem = new VideoItem(id, ContentTree.VIDEO_ID,
						title, creator, res);
				videoContainer.addItem(videoItem);
				videoContainer
						.setChildCount(videoContainer.getChildCount() + 1);
				ContentTree.addNode(id,
						new ContentNode(id, videoItem, filePath));

				Log.v(LOGTAG, "added video item " + title + "from " + filePath);
			} while (cursor.moveToNext());
		}

		// Audio Container
		Container audioContainer = new Container(ContentTree.AUDIO_ID,
				ContentTree.ROOT_ID, "Audios", "GNaP MediaServer",
				new DIDLObject.Class("object.container"), 0);
		audioContainer.setRestricted(true);
		audioContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
		rootNode.getContainer().addContainer(audioContainer);
		rootNode.getContainer().setChildCount(
				rootNode.getContainer().getChildCount() + 1);
		ContentTree.addNode(ContentTree.AUDIO_ID, new ContentNode(
				ContentTree.AUDIO_ID, audioContainer));

		String[] audioColumns = { MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.SIZE,
				MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ALBUM };
		cursor = managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				audioColumns, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				String id = ContentTree.AUDIO_PREFIX
						+ cursor.getInt(cursor
								.getColumnIndex(MediaStore.Audio.Media._ID));
				String title = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
				String creator = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
				String filePath = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
				String mimeType = cursor
						.getString(cursor
								.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
				long size = cursor.getLong(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
				long duration = cursor
						.getLong(cursor
								.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
				String album = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
				Res res = new Res(new MimeType(mimeType.substring(0,
						mimeType.indexOf('/')), mimeType.substring(mimeType
						.indexOf('/') + 1)), size, "http://"
						+ mediaServer.getAddress() + "/" + id);
				res.setDuration(duration / (1000 * 60 * 60) + ":"
						+ (duration % (1000 * 60 * 60)) / (1000 * 60) + ":"
						+ (duration % (1000 * 60)) / 1000);

				// Music Track must have `artist' with role field, or
				// DIDLParser().generate(didl) will throw nullpointException
				MusicTrack musicTrack = new MusicTrack(id,
						ContentTree.AUDIO_ID, title, creator, album,
						new PersonWithRole(creator, "Performer"), res);
				audioContainer.addItem(musicTrack);
				audioContainer
						.setChildCount(audioContainer.getChildCount() + 1);
				ContentTree.addNode(id, new ContentNode(id, musicTrack,
						filePath));

				Log.v(LOGTAG, "added audio item " + title + "from " + filePath);
			} while (cursor.moveToNext());
		}

		// Image Container
		Container imageContainer = new Container(ContentTree.IMAGE_ID,
				ContentTree.ROOT_ID, "Images", "GNaP MediaServer",
				new DIDLObject.Class("object.container"), 0);
		imageContainer.setRestricted(true);
		imageContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
		rootNode.getContainer().addContainer(imageContainer);
		rootNode.getContainer().setChildCount(
				rootNode.getContainer().getChildCount() + 1);
		ContentTree.addNode(ContentTree.IMAGE_ID, new ContentNode(
				ContentTree.IMAGE_ID, imageContainer));

		String[] imageColumns = { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.TITLE, MediaStore.Images.Media.DATA,
				MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.SIZE };
		cursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				imageColumns, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				String id = ContentTree.IMAGE_PREFIX
						+ cursor.getInt(cursor
								.getColumnIndex(MediaStore.Images.Media._ID));
				String title = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
				String creator = "unkown";
				String filePath = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
				String mimeType = cursor
						.getString(cursor
								.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
				long size = cursor.getLong(cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));

				Res res = new Res(new MimeType(mimeType.substring(0,
						mimeType.indexOf('/')), mimeType.substring(mimeType
						.indexOf('/') + 1)), size, "http://"
						+ mediaServer.getAddress() + "/" + id);

				ImageItem imageItem = new ImageItem(id, ContentTree.IMAGE_ID,
						title, creator, res);
				imageContainer.addItem(imageItem);
				imageContainer
						.setChildCount(imageContainer.getChildCount() + 1);
				ContentTree.addNode(id,
						new ContentNode(id, imageItem, filePath));

				Log.v(LOGTAG, "added image item " + title + "from " + filePath);
			} while (cursor.moveToNext());
		}

		serverPrepared = true;
		items.addAll(getList());
		adapter1 = new ContentAdapter(DMSActivity.this, items);
		listV1.setAdapter(adapter1);
	}

	protected DescMeta createDescMeta(Attributes attributes) {
		DescMeta desc = new DescMeta();
		desc.setId(attributes.getValue("id"));
		if ((attributes.getValue("type") != null))
			desc.setType(attributes.getValue("type"));
		if ((attributes.getValue("nameSpace") != null))
			desc.setNameSpace(URI.create(attributes.getValue("nameSpace")));
		if ((attributes.getValue("duration") != null))
			desc.setNameSpace(URI.create(attributes.getValue("duration")));
		return desc;
	}

	// FIXME: now only can get wifi address
	private InetAddress getLocalIpAddress() throws UnknownHostException {
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		return InetAddress.getByName(String.format("%d.%d.%d.%d",
				(ipAddress & 0xff), (ipAddress >> 8 & 0xff),
				(ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff)));
	}

	@Override
	public void setavtransportsuccess() {
		// TODO Auto-generated method stub
		CoshipAvtransprot.getvolume(upnpService, BrowseActivity.device, this);
		CoshipAvtransprot.mediaRemenderplay(upnpService, BrowseActivity.device,
				this);
	}

	@Override
	public void fail(String f) {
		// TODO Auto-generated method stub
		if (f.equals("设置URI失败")) {
			CoshipAvtransprot.mediaRemenderstop(upnpService,
					BrowseActivity.device, this);
		}
	}

	@Override
	public void mediarenderplaysuccess() {
		// TODO Auto-generated method stub

	}

	@Override
	public void mediarenderstopsuccess() {
		// TODO Auto-generated method stub
		CoshipAvtransprot.mediaRemendersetAVTransportURI(upnpService,
				BrowseActivity.device, uri, "0", title, DMSActivity.this);
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
		seekbar.setProgress(volume);
	}

	@Override
	public void getmediarenderposinfosuccess(PositionInfo positionInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getmediarendermediainfosuccess(MediaInfo mediaInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getTransportInfoSuccess(TransportInfo transportInfo) {
		// TODO Auto-generated method stub

	}

}
