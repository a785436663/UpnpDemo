package com.cvte.upnp.demo;

import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.transport.SwitchableRouter;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.cvte.upnp.dmc.PlayerActivity;

public class BrowseActivity extends ListActivity {
	private final String DMS = "MediaServer";
	private final String DMR = "MediaRenderer";
	private ArrayAdapter<DeviceDisplay> listAdapter;
	private BrowseRegistryListener registryListener = new BrowseRegistryListener();
	private AndroidUpnpService upnpService;
	private UpnpBrowserApp app;

	private ServiceConnection serviceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			upnpService = (AndroidUpnpService) service;
			// Refresh the list with all known devices
			listAdapter.clear();
			for (Device device : upnpService.getRegistry().getDevices()) {
				if (device.getType().getType().equals(DMR)) {
					registryListener.deviceAdded(device);
				}
			}
			// Getting ready for future device advertisements
			upnpService.getRegistry().addListener(registryListener);
			// Search asynchronously for all devices
			upnpService.getControlPoint().search();
		}

		public void onServiceDisconnected(ComponentName className) {
			upnpService = null;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UpnpBrowserApp app = ((UpnpBrowserApp) getApplication());
		listAdapter = new ArrayAdapter(this,
				android.R.layout.simple_list_item_1);
		setListAdapter(listAdapter);

		getApplicationContext().bindService(
				new Intent(this, BrowserUpnpService.class), serviceConnection,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (upnpService != null) {
			upnpService.getRegistry().removeListener(registryListener);
		}
		getApplicationContext().unbindService(serviceConnection);
	}

	public static Device device;

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// Intent intent = new Intent(this, BrowseDeviceActivity.class);
		// intent.putExtra("device", position);
		// startActivity(intent);
		device = listAdapter.getItem(position).getDevice();
		showToast(device.getType().getType() + "", false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, R.string.search_lan).setIcon(
				android.R.drawable.ic_menu_search);
		menu.add(0, 1, 0, R.string.switch_router).setIcon(
				android.R.drawable.ic_menu_revert);
		menu.add(0, 2, 0, R.string.toggle_debug_logging).setIcon(
				android.R.drawable.ic_menu_info_details);
		menu.add(0, 3, 0, "播放页面").setIcon(android.R.drawable.ic_menu_revert);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			searchNetwork();
			break;
		case 1:
			if (upnpService != null) {
				SwitchableRouter router = (SwitchableRouter) upnpService.get()
						.getRouter();
				if (router.isEnabled()) {
					Toast.makeText(this, R.string.disabling_router,
							Toast.LENGTH_SHORT).show();
					router.disable();
				} else {
					Toast.makeText(this, R.string.enabling_router,
							Toast.LENGTH_SHORT).show();
					router.enable();
				}
			}
			break;
		case 2:
			Logger logger = Logger.getLogger("org.teleal.cling");
			if (logger.getLevel().equals(Level.FINEST)) {
				Toast.makeText(this, R.string.disabling_debug_logging,
						Toast.LENGTH_SHORT).show();
				logger.setLevel(Level.INFO);
			} else {
				Toast.makeText(this, R.string.enabling_debug_logging,
						Toast.LENGTH_SHORT).show();
				logger.setLevel(Level.FINEST);
			}
			break;
		case 3:
			startActivity(new Intent(this, PlayerActivity.class));
			break;
		}
		return false;
	}

	protected void searchNetwork() {
		if (upnpService == null)
			return;
		Toast.makeText(this, R.string.searching_lan, Toast.LENGTH_SHORT).show();
		upnpService.getRegistry().removeAllRemoteDevices();
		upnpService.getControlPoint().search();
	}

	protected class BrowseRegistryListener extends DefaultRegistryListener {

		/* Discovery performance optimization for very slow Android devices! */
		@Override
		public void remoteDeviceDiscoveryStarted(Registry registry,
				RemoteDevice device) {
			deviceAdded(device);
		}

		@Override
		public void remoteDeviceDiscoveryFailed(Registry registry,
				final RemoteDevice device, final Exception ex) {
			showToast("Discovery failed of '"
					+ device.getDisplayString()
					+ "': "
					+ (ex != null ? ex.toString()
							: "Couldn't retrieve device/service descriptors"),
					true);
			deviceRemoved(device);
		}

		/*
		 * End of optimization, you can remove the whole block if your Android
		 * handset is fast (>= 600 Mhz)
		 */

		@Override
		public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
			deviceAdded(device);
		}

		@Override
		public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
			deviceRemoved(device);
		}

		@Override
		public void localDeviceAdded(Registry registry, LocalDevice device) {
			deviceAdded(device);
		}

		@Override
		public void localDeviceRemoved(Registry registry, LocalDevice device) {
			deviceRemoved(device);
		}

		public void deviceAdded(final Device device) {
			if (device.getType().getType().equals(DMR)) {
				runOnUiThread(new Runnable() {
					public void run() {
						app = (UpnpBrowserApp) getApplication();
						app.addDevice(device);
						DeviceDisplay d = new DeviceDisplay(device);
						int position = listAdapter.getPosition(d);
						if (position >= 0) {
							// Device already in the list, re-set new value at
							// same
							// position
							listAdapter.remove(d);
							listAdapter.insert(d, position);
						} else {
							listAdapter.add(d);
						}
					}
				});
				try {
					Log.e("----------", device.toString().indexOf("192.168.1.106")+"");
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}

		public void deviceRemoved(final Device device) {
			runOnUiThread(new Runnable() {
				public void run() {
					app = (UpnpBrowserApp) getApplication();
					listAdapter.remove(new DeviceDisplay(device));
					app.rmDevice(device);
				}
			});
		}
	}

	protected void showToast(final String msg, final boolean longLength) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(BrowseActivity.this, msg,
						longLength ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

	protected class DeviceDisplay {
		Device device;

		public DeviceDisplay(Device device) {
			this.device = device;
		}

		public Device getDevice() {
			return device;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			DeviceDisplay that = (DeviceDisplay) o;
			return device.equals(that.device);
		}

		@Override
		public int hashCode() {
			return device.hashCode();
		}

		@Override
		public String toString() {
			String name = (device.getDetails() != null
					&& device.getDetails().getFriendlyName() != null ? device
					.getDetails().getFriendlyName() : device.getDisplayString())+"\n是否包含192.168.1.106"+(device.toString().indexOf("192.168.1.106")>0);
			// Display a little star while the device is being loaded (see
			// performance optimization earlier)
			return device.isFullyHydrated() ? name : name + " *";
		}
	}

	static final Comparator<DeviceDisplay> DISPLAY_COMPARATOR = new Comparator<DeviceDisplay>() {
		public int compare(DeviceDisplay a, DeviceDisplay b) {
			return a.toString().compareTo(b.toString());
		}
	};
}
