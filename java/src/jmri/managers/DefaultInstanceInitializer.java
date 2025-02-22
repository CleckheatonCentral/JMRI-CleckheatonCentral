package jmri.managers;

import java.util.Arrays;
import java.util.Set;

import jmri.*;
import jmri.implementation.AbstractInstanceInitializer;
import jmri.implementation.DefaultClockControl;
import jmri.jmrit.audio.DefaultAudioManager;
import jmri.jmrit.simpleclock.SimpleTimebase;
import jmri.jmrit.vsdecoder.VSDecoderManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

import org.openide.util.lookup.ServiceProvider;

/**
 * Provide the usual default implementations for the
 * {@link jmri.InstanceManager}.
 * <p>
 * Not all {@link jmri.InstanceManager} related classes are provided by this
 * class. See the discussion in {@link jmri.InstanceManager} of initialization
 * methods.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2014
 * @since 2.9.4
 */
@ServiceProvider(service = InstanceInitializer.class)
public class DefaultInstanceInitializer extends AbstractInstanceInitializer {

    @Override
    public <T> Object getDefault(Class<T> type) {

        InternalSystemConnectionMemo memo = InstanceManager.getDefault(InternalSystemConnectionMemo.class);
        // In order for getDefault() to create a new object, the manager also
        // needs to be added to the method getInitalizes() below.

        if (type == AnalogIOManager.class) {
            return new ProxyAnalogIOManager().init();
        }

        if (type == AudioManager.class) {
            return new DefaultAudioManager(memo);
        }

        if (type == ClockControl.class) {
            return new DefaultClockControl();
        }

        if (type == ConditionalManager.class) {
            return new DefaultConditionalManager(memo);
        }

        if (type == LightManager.class) {
            return new ProxyLightManager();
        }

        if (type == VariableLightManager.class) {
            return new DefaultVariableLightManager(memo).init();
        }

        if (type == LogixManager.class) {
            return new DefaultLogixManager(memo);
        }

        if (type == MemoryManager.class) {
            return new DefaultMemoryManager(memo);
        }

        if (type == MeterManager.class) {
            return new ProxyMeterManager();
        }

        if (type == RailComManager.class) {
            return new DefaultRailComManager();
        }

        if (type == ReporterManager.class) {
            return new ProxyReporterManager();
        }

        if (type == RouteManager.class) {
            return new DefaultRouteManager(memo);
        }

        if (type == SectionManager.class) {
            return new DefaultSectionManager();
        }

        if (type == SensorManager.class) {
            return new ProxySensorManager();
        }

        if (type == SignalGroupManager.class) {
            // ensure signal mast manager exists first
            InstanceManager.getDefault(SignalMastManager.class);
            return new DefaultSignalGroupManager(memo);
        }

        if (type == SignalHeadManager.class) {
            return new AbstractSignalHeadManager(memo);
        }

        if (type == SignalMastLogicManager.class) {
            return new DefaultSignalMastLogicManager(memo);
        }

        if (type == SignalMastManager.class) {
            // ensure signal head manager exists first
            InstanceManager.getDefault(SignalHeadManager.class);
            return new DefaultSignalMastManager(memo);
        }

        if (type == SignalSystemManager.class) {
            return new DefaultSignalSystemManager(memo);
        }

        if (type == StringIOManager.class) {
            return new ProxyStringIOManager();
        }

        if (type == Timebase.class) {
            Timebase timebase = new SimpleTimebase(memo);
            InstanceManager.getOptionalDefault(ConfigureManager.class).ifPresent(cm -> cm.registerConfig(timebase, Manager.TIMEBASE));
            return timebase;
        }

        if (type == TurnoutManager.class) {
            return new ProxyTurnoutManager();
        }

        if (type == TransitManager.class) {
            return new DefaultTransitManager();
        }

        if (type == VSDecoderManager.class) {
            return VSDecoderManager.instance();
        }

        if (type == IdTagManager.class) {
            return new ProxyIdTagManager();
        }

        return super.getDefault(type);
    }

    @Override
    public Set<Class<?>> getInitalizes() {
        Set<Class<?>> set = super.getInitalizes();
        set.addAll(Arrays.asList(
                AnalogIOManager.class,
                AudioManager.class,
                ClockControl.class,
                ConditionalManager.class,
                IdTagManager.class,
                LightManager.class,
                LogixManager.class,
                MemoryManager.class,
                MeterManager.class,
                RailComManager.class,
                ReporterManager.class,
                RouteManager.class,
                SectionManager.class,
                SensorManager.class,
                SignalGroupManager.class,
                SignalHeadManager.class,
                SignalMastLogicManager.class,
                SignalMastManager.class,
                SignalSystemManager.class,
                StringIOManager.class,
                Timebase.class,
                TransitManager.class,
                TurnoutManager.class,
                VariableLightManager.class,
                VSDecoderManager.class
        ));
        return set;
    }

}
