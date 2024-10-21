package com.bitwig.extensions.controllers.novation.launchkey_mk4.display;

import com.bitwig.extensions.controllers.novation.launchkey_mk4.MidiProcessor;
import com.bitwig.extensions.framework.di.Component;

@Component
public class DisplayControl {
    private static final byte[] TEXT_CONFIG_COMMAND =
        {(byte) 0xF0, 0x00, 0x20, 0x29, 0x02, 0x14, 0x04, 0x00, 0x00, (byte) 0xF7};
    private final String textCommandHeader;
    private final MidiProcessor midiProcessor;
    
    public DisplayControl(final MidiProcessor processor) {
        this.midiProcessor = processor;
        if (processor.isMiniVersion()) {
            TEXT_CONFIG_COMMAND[5] = 0x13;
        }
        textCommandHeader = processor.getSysexHeader() + "06 ";
    }
    
    public void configureDisplay(final int targetId, final int config) {
        TEXT_CONFIG_COMMAND[7] = (byte) targetId;
        TEXT_CONFIG_COMMAND[8] = (byte) config;
        midiProcessor.sendSysExBytes(TEXT_CONFIG_COMMAND);
    }
    
    public void fixDisplayUpdate(final int lineIndex, final String text) {
        setText(0x20, lineIndex, text);
        configureDisplay(0x21, 0x61);
        setText(0x21, lineIndex, text);
        showDisplay(0x21);
        showDisplay(0x20);
    }
    
    public void show2Line(final String line1, final String line2) {
        configureDisplay(0x21, 0x61);
        setText(0x21, 0, line1);
        setText(0x21, 1, line2);
        showDisplay(0x21);
    }
    
    public void showDisplay(final int targetId) {
        TEXT_CONFIG_COMMAND[7] = (byte) targetId;
        TEXT_CONFIG_COMMAND[8] = 0x7F;
        midiProcessor.sendSysExBytes(TEXT_CONFIG_COMMAND);
    }
    
    public void hideDisplay(final int targetId) {
        TEXT_CONFIG_COMMAND[7] = (byte) targetId;
        TEXT_CONFIG_COMMAND[8] = 0;
        midiProcessor.sendSysExBytes(TEXT_CONFIG_COMMAND);
    }
    
    public void setText(final int target, final int field, final String text) {
        final StringBuilder msg = new StringBuilder(textCommandHeader);
        msg.append("%02X ".formatted(target));
        msg.append("%02X ".formatted(field));
        final String validText = StringUtil.toAsciiDisplay(text, 16);
        for (int i = 0; i < validText.length(); i++) {
            msg.append("%02X ".formatted((int) validText.charAt(i)));
        }
        msg.append("F7");
        midiProcessor.sendSysExString(msg.toString());
    }
    
    public void initTemps() {
        configureDisplay(0x21, 0x61);
        configureDisplay(0x20, 0x61);
        for (int i = 0; i < 16; i++) {
            configureDisplay(0x05 + i, 0x62);
        }
    }
    
}
