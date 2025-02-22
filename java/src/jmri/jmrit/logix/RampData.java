package jmri.jmrit.logix;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * This class holds a list of throttle setting to make a smooth acceleration or
 * deceleration. It supplies iterators to cycle through the settings.
 * Used when speed changes are called for by signaled speeds, block speed limits
 * or user controls for speed halts and resumes.  Also used to make NXWarrants.
 *  
 * @author Pete Cressman Copyright (C) 2019
*/

public class RampData {

    private int _timeInterval;
    private float _throttleInterval;
    private ArrayList<Float> _settings;
    private boolean _upRamp;
    private SpeedUtil _speedUtil;
    private float _fromSpeed;
    private float _toSpeed;

    static float INCRE_RATE = 1.10f;  // multiplier to increase throttle increments

    RampData(SpeedUtil util, float throttleIncre, int timeIncre, float fromSet, float toSet) {
        _throttleInterval = throttleIncre; 
        _timeInterval = timeIncre;
        _speedUtil = util;
        _fromSpeed = fromSet;
        _toSpeed = toSet;
        makeThrottleSettings();
    }
    
    protected boolean isUpRamp() {
        return _upRamp;
    }

    private void makeThrottleSettings() {
        _upRamp = (_toSpeed >= _fromSpeed);
        _settings = new ArrayList<>();
        float lowSetting;
        float highSetting;
        if (_upRamp) {
            lowSetting = _fromSpeed;
            highSetting = _toSpeed;
        } else {
            lowSetting = _toSpeed;
            highSetting = _fromSpeed;
        }
        float low = 0.0f;
        float throttleIncre = _throttleInterval;
        while (low < lowSetting ) {
            throttleIncre *= INCRE_RATE;
            low += throttleIncre;
        }
        while (lowSetting < highSetting) {
            _settings.add(Float.valueOf(lowSetting));
            lowSetting += throttleIncre;
            throttleIncre *= INCRE_RATE;
        }
        _settings.add(Float.valueOf(highSetting));
    }

    protected float getRampLength() {
        float rampLength = 0;
        float nextSetting;
        if (_upRamp) {
            ListIterator<Float> iter = speedIterator(true);
            float prevSetting = 0.0f;
            if (iter.hasNext()) {
                prevSetting = iter.next();
            }
            while (iter.hasNext()) {
                nextSetting = iter.next().floatValue();
                rampLength += _speedUtil.getDistanceOfSpeedChange(prevSetting, nextSetting, _timeInterval);
                prevSetting = nextSetting;
            }
        } else {
            ListIterator<Float> iter =speedIterator(false);
            float prevSetting = 1.0f;
            if (iter.hasPrevious()) {
                prevSetting = iter.previous();
            }
            while (iter.hasPrevious()) {
                nextSetting = iter.previous().floatValue();
                rampLength += _speedUtil.getDistanceOfSpeedChange(prevSetting, nextSetting, _timeInterval);
                prevSetting = nextSetting;
            }
        }
        return rampLength;
    }

    protected int getNumSteps() {
        return _settings.size() - 1;
    }

    protected int getRamptime() {
        return (_settings.size() - 1) * _timeInterval;
    }

    protected float getMaxSpeed() {
        if (_settings == null) {
            throw new IllegalArgumentException("Null array of throttle settings"); 
        }
        return _settings.get(_settings.size() - 1).floatValue();
    }

    protected ListIterator<Float> speedIterator(boolean up) {
        if (up) {
            return _settings.listIterator(0);
        } else {
            return _settings.listIterator(_settings.size());
            
        }
    }

    protected int getRampTimeIncrement() {
        return _timeInterval;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RampData.class);
}
