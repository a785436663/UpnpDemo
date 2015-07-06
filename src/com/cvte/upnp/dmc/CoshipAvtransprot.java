package com.cvte.upnp.dmc;

import java.util.Map;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.controlpoint.SubscriptionCallback;
import org.teleal.cling.model.action.ActionArgumentValue;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.gena.CancelReason;
import org.teleal.cling.model.gena.GENASubscription;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.state.StateVariableValue;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.support.avtransport.callback.GetMediaInfo;
import org.teleal.cling.support.avtransport.callback.GetPositionInfo;
import org.teleal.cling.support.avtransport.callback.GetTransportInfo;
import org.teleal.cling.support.avtransport.callback.Pause;
import org.teleal.cling.support.avtransport.callback.Play;
import org.teleal.cling.support.avtransport.callback.Seek;
import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;
import org.teleal.cling.support.avtransport.callback.Stop;
import org.teleal.cling.support.contentdirectory.DIDLParser;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PersonWithRole;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.ProtocolInfo;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.TransportInfo;
import org.teleal.cling.support.model.item.MusicTrack;
import org.teleal.cling.support.renderingcontrol.callback.GetMute;
import org.teleal.cling.support.renderingcontrol.callback.GetVolume;
import org.teleal.cling.support.renderingcontrol.callback.SetMute;
import org.teleal.cling.support.renderingcontrol.callback.SetVolume;
import org.teleal.common.util.MimeType;

import android.provider.MediaStore;

public class CoshipAvtransprot {
	/***********************************************************
	 * @param device
	 * @param Uri
	 * @param Title
	 *            功能：设置mediarender播放的URI
	 ***********************************************************/
	public static void mediaRemendersetAVTransportURI(
			AndroidUpnpService upnpService,
			@SuppressWarnings("rawtypes") Device device, final String Uri,
			final String meta, final String Title,
			final GetCoshipavtransportstate getavtransport) {
		if (device == null)
			return;
		@SuppressWarnings("rawtypes")
		Service service = device.findService(new UDAServiceId("AVTransport"));
		System.out.println("设置URI---" + Uri);
		try {			
			ActionCallback setAVTransportURIAction = new SetAVTransportURI(
					service, Uri, meta) {
				@Override
				public void success(
						@SuppressWarnings("rawtypes") ActionInvocation invocation) {
					super.success(invocation);
					System.out.println("设置URL成功");
					getavtransport.setavtransportsuccess();
				}

				public void failure(
						@SuppressWarnings("rawtypes") ActionInvocation invocation,
						UpnpResponse operation, String defaultMsg) {
					getavtransport.fail("设置URI失败");
				}
			};
			upnpService.getControlPoint().execute(setAVTransportURIAction);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/****************************************************************
	 * @param upnpService
	 * @param device
	 *            mediarender设备名
	 * @param getavtransport
	 *            avtransport事件回调监听函数 功能：控制mediarender设备进行播放
	 */
	public static void mediaRemenderplay(AndroidUpnpService upnpService,
			@SuppressWarnings("rawtypes") final Device device,
			final GetCoshipavtransportstate getavtransport) {
		if (device == null)
			return;
		Service<?, ?> service = device.findService(new UDAServiceId(
				"AVTransport"));
		try {
			ActionCallback playAction = new Play(service) {
				@Override
				public void success(
						@SuppressWarnings("rawtypes") ActionInvocation invocation) {
					super.success(invocation);
					getavtransport.mediarenderplaysuccess();
					System.out.println("播放成功");
				}

				@Override
				public void failure(
						@SuppressWarnings("rawtypes") ActionInvocation invocation,
						UpnpResponse operation, String defaultMsg) {
					getavtransport.fail("播放失败");
				}
			};
			upnpService.getControlPoint().execute(playAction);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/***********************************************
	 * 8
	 * 
	 * @param upnpService
	 * @param device
	 * @param getGENASubscriptionStat
	 */
	public static void MeidaSubscription(AndroidUpnpService upnpService,
			final Device<?, ?, ?> device,
			final GetGENASubscriptionStat getGENASubscriptionStat) {
		if (device == null)
			return;
		Service<?, ?> service = device.findService(new UDAServiceId(
				"AVTransport"));
		SubscriptionCallback callback = new SubscriptionCallback(service, 2000) {

			@Override
			protected void ended(
					@SuppressWarnings("rawtypes") GENASubscription arg0,
					CancelReason arg1, UpnpResponse arg2) {
				System.out.println("ended");
			}

			@Override
			protected void established(
					@SuppressWarnings("rawtypes") GENASubscription arg0) {
				System.out.println("established");
			}

			@Override
			protected void eventReceived(
					@SuppressWarnings("rawtypes") GENASubscription arg0) {
				System.out.println("Event: "
						+ arg0.getCurrentSequence().getValue());
				@SuppressWarnings({ "rawtypes", "unchecked" })
				Map<String, StateVariableValue> values = arg0
						.getCurrentValues();
				StateVariableValue<?> LastChange = values.get("LastChange");
				String result = LastChange.toString();
//				result = CoshipUtils.parseLastChangeInfo(LastChange.toString());
//				if (result.equals(CoshipUtils.REALPLAYING)) {
//					getGENASubscriptionStat.RealPlaying();
//				} else if (result.equals(CoshipUtils.REALSTOPPED)) {
//					getGENASubscriptionStat.RealStopped();
//				} else if (result.equals(CoshipUtils.REALSEEKSUCCESS)) {
//					getGENASubscriptionStat.RealSeekSuccess();
//				}
				System.out.println("the current state is=" + result);
			}

			@Override
			protected void eventsMissed(
					@SuppressWarnings("rawtypes") GENASubscription arg0,
					int arg1) {
				System.out.println("eventsMissed");
			}

			@Override
			protected void failed(
					@SuppressWarnings("rawtypes") GENASubscription arg0,
					UpnpResponse arg1, Exception arg2, String arg3) {
				System.out.println("failed");
			}
		};
		upnpService.getControlPoint().execute(callback);
	}

	/*****************************************************
	 * @param upnpService
	 * @param device
	 *            mediarender设备名
	 * @param volume
	 *            设置声音大小
	 * @param getavtransport
	 *            avtransport事件回调监听函数 功能：设置meidarender设备声音大小
	 */
	public static void setvolume(AndroidUpnpService upnpService,
			@SuppressWarnings("rawtypes") Device device, int volume,
			final GetCoshipavtransportstate getavtransport) {
		if (device == null)
			return;
		Service<?, ?> service = device.findService(new UDAServiceId(
				"RenderingControl"));
		if (service != null) {
			try {
				ActionCallback setvolume = new SetVolume(service, volume) {

					@Override
					public void failure(ActionInvocation arg0,
							UpnpResponse arg1, String arg2) {
						// TODO Auto-generated method stub
						getavtransport.fail("设置当前音量大小失败");
					}
				};
				upnpService.getControlPoint().execute(setvolume);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/*************************************************************
	 * 
	 * @param upnpService
	 * @param device
	 *            mediarender设备名
	 * @param getavtransport
	 *            avtransport事件回调监听函数 功能：获取mediarender设备是否静音
	 */
	public static void getmute(AndroidUpnpService upnpService,
			@SuppressWarnings("rawtypes") Device device,
			final GetCoshipavtransportstate getavtransport) {
		if (device == null)
			return;
		Service<?, ?> service = device.findService(new UDAServiceId(
				"RenderingControl"));
		try {
			ActionCallback getmute = new GetMute(service) {

				@Override
				public void failure(ActionInvocation arg0, UpnpResponse arg1,
						String arg2) {
					// TODO Auto-generated method stub
					System.out.println("获取当前音量状态失败");
				}

				@Override
				public void received(ActionInvocation arg0, boolean arg1) {
					// TODO Auto-generated method stub
					System.out.println("获取当前音量状态成功");
					// 获取当前音量状态成功
					super.success(arg0);
					@SuppressWarnings("rawtypes")
					ActionArgumentValue[] action = arg0.getOutput();
					for (int i = 0; i < action.length; i++) {
						System.out.println(action[i].toString());
					}
				}
			};
			upnpService.getControlPoint().execute(getmute);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**************************************************
	 * @param upnpService
	 * @param device
	 *            mediarender设备名
	 * @param mute
	 *            是否静音标志
	 * @param getavtransport
	 *            avtransport事件回调监听函数 功能： 设置mediarender是否静音
	 */
	public static void setmute(AndroidUpnpService upnpService,
			@SuppressWarnings("rawtypes") Device device, boolean mute,
			final GetCoshipavtransportstate getavtransport) {
		if (device == null)
			return;
		@SuppressWarnings("rawtypes")
		Service service = device.findService(new UDAServiceId(
				"RenderingControl"));
		try {
			ActionCallback setmute = new SetMute(service, mute) {

				@Override
				public void failure(ActionInvocation arg0, UpnpResponse arg1,
						String arg2) {
					// TODO Auto-generated method stub
					System.out.println("设置当前音量状况失败");
				}
			};
			upnpService.getControlPoint().execute(setmute);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/***************************************************
	 * @param upnpService
	 * @param device
	 *            mediarender设备名
	 * @param volume
	 *            声音大小
	 * @param getavtransport
	 *            avtransport事件回调监听函数 功能：获取mediarender设备声音大小
	 */
	public static void getvolume(AndroidUpnpService upnpService,
			@SuppressWarnings("rawtypes") Device device,
			final GetCoshipavtransportstate getavtransport) {
		if (device == null)
			return;
		@SuppressWarnings("rawtypes")
		Service service = device.findService(new UDAServiceId(
				"RenderingControl"));
		try {
			ActionCallback getvolume = new GetVolume(service) {

				@Override
				public void failure(ActionInvocation arg0, UpnpResponse arg1,
						String arg2) {
					// TODO Auto-generated method stub
					System.out.println("设置当设备声音大小失败");
				}

				@Override
				public void received(ActionInvocation arg0, int arg1) {
					// TODO Auto-generated method stub
					System.out.println("设置当设备声音大小成功");
					getavtransport.getVolumeSuccess(arg1);
				}
			};
			upnpService.getControlPoint().execute(getvolume);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/************************************************
	 * 888
	 * 
	 * @param upnpService
	 * @param device
	 *            mediarender设备名
	 * @param getavtransport
	 *            avtransport事件回调监听函数 功能：设置mediarender暂停播放
	 */
	public static void mediaRenderpause(AndroidUpnpService upnpService,
			@SuppressWarnings("rawtypes") Device device,
			final GetCoshipavtransportstate getavtransport) {
		if (device == null)
			return;
		@SuppressWarnings("rawtypes")
		Service service = device.findService(new UDAServiceId("AVTransport"));
		try {
			ActionCallback pauseAction = new Pause(service) {
				@Override
				public void success(
						@SuppressWarnings("rawtypes") ActionInvocation invocation) {
					super.success(invocation);
					getavtransport.mediarenderpausesuccess();
				}

				@Override
				public void failure(
						@SuppressWarnings("rawtypes") ActionInvocation invocation,
						UpnpResponse operation, String defaultMsg) {
					getavtransport.fail("暂停播放失败");
				}
			};
			upnpService.getControlPoint().execute(pauseAction);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*******************************************************
	 * @param upnpService
	 * @param device
	 *            mediarender设备名
	 * @param time
	 *            seektime时间
	 * @param getavtransport
	 *            avtransport事件回调监听函数 功能：设置mediarender进行seek播放
	 */
	public static void mediaRenderSeek(AndroidUpnpService upnpService,
			@SuppressWarnings("rawtypes") Device device, String time,
			final GetCoshipavtransportstate getavtransport) {
		if (device == null)
			return;
		@SuppressWarnings("rawtypes")
		Service service = device.findService(new UDAServiceId("AVTransport"));
		try {
			ActionCallback seekAction = new Seek(service, time) {
				@Override
				public void failure(
						@SuppressWarnings("rawtypes") ActionInvocation arg0,
						UpnpResponse arg1, String arg2) {
					getavtransport.fail("seek播放失败");
				}

				@Override
				public void success(
						@SuppressWarnings("rawtypes") ActionInvocation invocation) {
					super.success(invocation);
					getavtransport.mediarenderseeksuccess();

				}
			};
			upnpService.getControlPoint().execute(seekAction);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/************************************************************
	 * @param device
	 *            功能：停止播放当前音乐或者视频内容
	 ***********************************************************/
	public static void mediaRemenderstop(AndroidUpnpService upnpService,
			@SuppressWarnings("rawtypes") final Device device,
			final GetCoshipavtransportstate getavtransport) {
		if (device == null)
			return;
		@SuppressWarnings("rawtypes")
		Service service = device.findService(new UDAServiceId("AVTransport"));
		try {
			ActionCallback stopAction = new Stop(service) {
				@Override
				public void failure(
						@SuppressWarnings("rawtypes") ActionInvocation arg0,
						UpnpResponse arg1, String arg2) {
					getavtransport.fail("停止播放失败");
				}

				@Override
				public void success(
						@SuppressWarnings("rawtypes") ActionInvocation invocation) {
					super.success(invocation);
					getavtransport.mediarenderstopsuccess();
				}
			};
			upnpService.getControlPoint().execute(stopAction);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/********************************************************************
	 * 8
	 * 
	 * @param upnpService
	 * @param device
	 *            mediarender设备名
	 * @param getmediarenderinfo
	 *            获取mediainfo回调监听函数 功能：获取mediarender的媒体信息
	 */
	public static void GetMediaInfo(AndroidUpnpService upnpService,
			@SuppressWarnings("rawtypes") Device device,
			final GetCoshipavtransportstate getmediarenderinfo) {
		if (device == null)
			return;
		@SuppressWarnings("rawtypes")
		Service service = device.findService(new UDAServiceId("AVTransport"));
		try {
			ActionCallback getmediainfo = new GetMediaInfo(service) {
				@Override
				public void failure(
						@SuppressWarnings("rawtypes") ActionInvocation arg0,
						UpnpResponse arg1, String arg2) {
					getmediarenderinfo.fail("获取媒体信息失败");
				}

				@Override
				public void received(
						@SuppressWarnings("rawtypes") ActionInvocation invocation,
						MediaInfo mediaInfo) {
					getmediarenderinfo
							.getmediarendermediainfosuccess(mediaInfo);
				}
			};
			upnpService.getControlPoint().execute(getmediainfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/***********************************************************************
	 * @param upnpService
	 * @param device
	 *            mediarender设备名
	 * @param getmediarenderinfo
	 *            获取mediainfo回调监听函数 功能：获取mediarender的播放位置信息
	 */
	public static void GetPositionInfo(AndroidUpnpService upnpService,
			@SuppressWarnings("rawtypes") Device device,
			final GetCoshipavtransportstate getmediarenderinfo) {
		if (device == null)
			return;
		@SuppressWarnings("rawtypes")
		Service service = device.findService(new UDAServiceId("AVTransport"));
		try {
			ActionCallback getpositionInfo = new GetPositionInfo(service) {
				@Override
				public void failure(
						@SuppressWarnings("rawtypes") ActionInvocation arg0,
						UpnpResponse arg1, String arg2) {
					System.out.println("GetPositionInfo failure");
					getmediarenderinfo.fail("获取播放位置信息失败");
				}

				@Override
				public void received(
						@SuppressWarnings("rawtypes") ActionInvocation invocation,
						PositionInfo positionInfo) {
					getmediarenderinfo
							.getmediarenderposinfosuccess(positionInfo);
				}
			};
			upnpService.getControlPoint().execute(getpositionInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*********************************************
	 * @param upnpService
	 *            upnp服务
	 * @param device
	 *            当前和客户端连接的DMR设备 功能：获取DMR当前的状态
	 *********************************************/
	public static void GetDmrTransportInfo(AndroidUpnpService upnpService,
			@SuppressWarnings("rawtypes") Device device,
			final GetCoshipavtransportstate getmediarenderinfo) {
		if (device == null)
			return;
		@SuppressWarnings("rawtypes")
		Service service = device.findService(new UDAServiceId("AVTransport"));
		if (service == null) {
			return;
		}
		ActionCallback getTransportInfo = new GetTransportInfo(service) {
			@Override
			public void received(
					@SuppressWarnings("rawtypes") ActionInvocation invocation,
					TransportInfo transportInfo) {
				if (getmediarenderinfo!=null) {
					getmediarenderinfo.getTransportInfoSuccess(transportInfo);
				}
			}

			@Override
			public void failure(
					@SuppressWarnings("rawtypes") ActionInvocation invocation,
					UpnpResponse operation, String defaultMsg) {
				if (getmediarenderinfo!=null) {
					getmediarenderinfo.fail("获取当前DMR设备状态失败");
				}
			}
		};
		upnpService.getControlPoint().execute(getTransportInfo);
	}
}