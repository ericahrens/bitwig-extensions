package com.bitwig.extensions.controllers.mackie.devices;

import com.bitwig.extension.controller.api.CursorDeviceFollowMode;
import com.bitwig.extension.controller.api.CursorRemoteControlsPage;
import com.bitwig.extension.controller.api.CursorTrack;
import com.bitwig.extension.controller.api.Device;
import com.bitwig.extension.controller.api.DeviceBank;
import com.bitwig.extension.controller.api.DrumPadBank;
import com.bitwig.extension.controller.api.Parameter;
import com.bitwig.extension.controller.api.PinnableCursorDevice;

public class CursorDeviceControl {
	private final DeviceBank deviceBank;
	private final PinnableCursorDevice cursorDevice;
	private final CursorRemoteControlsPage remotes;
	private final CursorTrack cursorTrack;
	private final PinnableCursorDevice primaryDevice;
	private final DrumPadBank drumPadBank;

	public CursorDeviceControl(final CursorTrack cursorTrack, final int size, final int totalChannelsAvailable) {
		this.cursorTrack = cursorTrack;
		this.cursorDevice = cursorTrack.createCursorDevice();
		primaryDevice = cursorTrack.createCursorDevice("drumdetection", "Pad Device", 1,
				CursorDeviceFollowMode.FIRST_INSTRUMENT);

		drumPadBank = primaryDevice.createDrumPadBank(size);
		drumPadBank.setSkipDisabledItems(false);

		this.cursorDevice.name().markInterested();
		this.cursorDevice.deviceType().markInterested();
		this.cursorDevice.isPinned().markInterested();

		deviceBank = cursorDevice.deviceChain().createDeviceBank(8);
		deviceBank.canScrollBackwards().markInterested();
		deviceBank.canScrollForwards().markInterested();
		deviceBank.itemCount().markInterested();
		deviceBank.scrollPosition().markInterested();

		this.remotes = cursorDevice.createCursorRemoteControlsPage(8);

		for (int i = 0; i < deviceBank.getSizeOfBank(); i++) {
			final int index = i;
			final Device device = deviceBank.getDevice(index);
			device.deviceType().markInterested();
			device.name().markInterested();
			device.position().markInterested();
		}

		cursorDevice.position().addValueObserver(cp -> {
			if (cp >= 0) {
				deviceBank.scrollPosition().set(cp - 1);
			}
		});
	}

	public void moveDeviceLeft() {
		final Device previousDevice = deviceBank.getDevice(0);
		previousDevice.beforeDeviceInsertionPoint().moveDevices(cursorDevice);
		cursorDevice.selectPrevious();
		cursorDevice.selectNext();
	}

	public void moveDeviceRight() {
		final Device nextDevice = deviceBank.getDevice(2);
		nextDevice.afterDeviceInsertionPoint().moveDevices(cursorDevice);
		cursorDevice.selectPrevious();
		cursorDevice.selectNext();
	}

	public DrumPadBank getDrumPadBank() {
		return drumPadBank;
	}

	public PinnableCursorDevice getCursorDevice() {
		return cursorDevice;
	}

	public DeviceBank getDeviceBank() {
		return deviceBank;
	}

	public CursorTrack getCursorTrack() {
		return cursorTrack;
	}

	public CursorRemoteControlsPage getRemotes() {
		return remotes;
	}

	public Parameter getParameter(final int index) {
		return remotes.getParameter(index);
	}

	public void selectDevice(final Device device) {
		cursorDevice.selectDevice(device);
		cursorDevice.selectInEditor();
	}

	public void navigateDevice(final int direction) {
		if (direction > 0) {
			cursorDevice.selectNext();
		} else {
			cursorDevice.selectPrevious();
		}
	}

}
