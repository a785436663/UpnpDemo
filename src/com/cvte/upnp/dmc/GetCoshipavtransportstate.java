package com.cvte.upnp.dmc;

import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.TransportInfo;

public interface GetCoshipavtransportstate {
	// 设置URL成功
	public void setavtransportsuccess();

	// 播放成功
	public void mediarenderplaysuccess();

	// 停止成功
	public void mediarenderstopsuccess();

	// seek播放成功
	public void mediarenderseeksuccess();

	// 暂停成功
	public void mediarenderpausesuccess();

	// 获取音量成功
	public void getVolumeSuccess(int volume);

	// 获取mediarender的播放位置信息
	void getmediarenderposinfosuccess(PositionInfo positionInfo);

	// 获取mediarender的媒体信息
	void getmediarendermediainfosuccess(MediaInfo mediaInfo);

	// 当前和客户端连接的DMR设备 功能：获取DMR当前的状态
	void getTransportInfoSuccess(TransportInfo transportInfo);

	// 暂停成功
	public void fail(String info);
}
