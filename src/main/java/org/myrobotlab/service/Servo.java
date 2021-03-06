/**
 *                    
 * @author GroG (at) myrobotlab.org
 *  
 * This file is part of MyRobotLab (http://myrobotlab.org).
 *
 * MyRobotLab is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License 2.0 as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version (subject to the "Classpath" exception
 * as provided in the LICENSE.txt file that accompanied this code).
 *
 * MyRobotLab is distributed in the hope that it will be useful or fun,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License 2.0 for more details.
 *
 * All libraries in thirdParty bundle are subject to their own license
 * requirements - please refer to http://myrobotlab.org/libraries for 
 * details.
 * 
 * Enjoy !
 * 
 * */

package org.myrobotlab.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.myrobotlab.framework.Service;
import org.myrobotlab.framework.ServiceType;
import org.myrobotlab.framework.interfaces.Attachable;
import org.myrobotlab.framework.interfaces.NameProvider;
import org.myrobotlab.framework.interfaces.ServiceInterface;
import org.myrobotlab.logging.Level;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.logging.LoggingFactory;
import org.myrobotlab.math.Mapper;
import org.myrobotlab.math.MathUtils;
import org.myrobotlab.service.data.PinData;
import org.myrobotlab.service.interfaces.ServoControl;
import org.myrobotlab.service.interfaces.ServoController;
import org.myrobotlab.service.interfaces.Simulator;
import org.slf4j.Logger;

/**
 * @author GroG
 * 
 *         Servos have both input and output. Input is usually of the range of
 *         integers between 0.0 - 180.0, and output can relay those values
 *         directly to the servo's firmware (Arduino ServoLib, I2C controller,
 *         etc)
 * 
 *         However there can be the occasion that the input comes from a system
 *         which does not have the same range. Such that input can vary from 0.0
 *         to 1.0. For example, OpenCV coordinates are often returned in this
 *         range. When a mapping is needed Servo.map can be used. For this
 *         mapping Servo.map(0.0, 1.0, 0, 180) might be desired. Reversing input
 *         would be done with Servo.map(180, 0, 0, 180)
 * 
 *         outputY - is the values sent to the firmware, and should not
 *         necessarily be confused with the inputX which is the input values
 *         sent to the servo
 * 
 */

public class Servo extends Service implements ServoControl {

  /**
   * Sweeper - TODO - should be implemented in the arduino code for smoother
   * function
   * 
   * 
   */
  public class Sweeper extends Thread {

    public Sweeper(String name) {
      super(String.format("%s.sweeper", name));
    }

    /**
     * Sweeping works on input, a thread is used as the "controller" (this is
     * input) and input sweeps back and forth - the servo parameters know what
     * to do for output
     */

    @Override
    public void run() {

      double sweepMin = 0.0;
      double sweepMax = 0.0;
      // start in the middle
      double sweepPos = mapper.getMinX() + (mapper.getMaxX() - mapper.getMinX()) / 2;
      isSweeping = true;

      try {
        while (isSweeping) {

          // set our range to be inside 'real' min & max input
          sweepMin = mapper.getMinX() + 1;
          sweepMax = mapper.getMaxX() - 1;

          // if pos is too small or too big flip direction
          if (sweepPos >= sweepMax || sweepPos <= sweepMin) {
            sweepStep = sweepStep * -1;
          }

          sweepPos += sweepStep;
          moveTo(sweepPos);
          Thread.sleep(sweepDelay);
        }
      } catch (Exception e) {
        isSweeping = false;
      }
    }

  }

  private static final long serialVersionUID = 1L;
  public final static Logger log = LoggerFactory.getLogger(Servo.class);

  /**
   * Routing Attach - routes ServiceInterface.attach(service) to appropriate
   * methods for this class
   */
  @Override
  public void attach(Attachable service) throws Exception {
    if (ServoController.class.isAssignableFrom(service.getClass())) {
      attachServoController((ServoController) service);
      return;
    }

    error("%s doesn't know how to attach a %s", getClass().getSimpleName(), service.getClass().getSimpleName());
  }

  /**
   * Routing Attach - routes ServiceInterface.attach(service) to appropriate
   * methods for this class
   */
  @Override
  public void detach(Attachable service) {
    if (ServoController.class.isAssignableFrom(service.getClass())) {
      detachServoController((ServoController) service);
      return;
    }

    error("%s doesn't know how to attach a %s", getClass().getSimpleName(), service.getClass().getSimpleName());
  }

  /**
   * This static method returns all the details of the class without it having
   * to be constructed. It has description, categories, dependencies, and peer
   * definitions.
   * 
   * @return ServiceType - returns all the data
   * 
   */
  static public ServiceType getMetaData() {

    ServiceType meta = new ServiceType(Servo.class.getCanonicalName());
    meta.addDescription("Controls a servo");
    meta.addCategory("motor", "control");

    return meta;
  }

  transient ServoController controller;

  String controllerName;

  Mapper mapper;

  /**
   * default rest is 90 default target position will be 90 if not specified
   */
  Double rest;

  /**
   * last time the servo has moved
   */
  long lastActivityTime = 0;

  /**
   * the 'pin' for this Servo - it is Integer because it can be in a state of
   * 'not set' or null.
   * 
   * pin is the ONLY value which cannot and will not be 'defaulted'
   */
  Integer pin;

  /**
   * the requested INPUT position of the servo
   */
  Double targetPos;
  Double targetPosBeforeSensorFeebBackCorrection;
  Boolean intialTargetPosChange = true;

  /**
   * the calculated output for the servo
   */
  Double targetOutput;

  /**
   * list of names of possible controllers
   */
  public List<String> controllers;

  boolean isSweeping = false;
  int sweepDelay = 100;

  double sweepStep = 1;
  boolean sweepOneWay = false;

  // sweep types
  // TODO - computer implemented speed control (non-sweep)
  boolean speedControlOnUC = false;

  transient Thread sweeper = null;

  /**
   * feedback of both incremental position and stops. would allow blocking
   * moveTo if desired
   */
  boolean isEventsEnabled = true;
  boolean isIKEventEnabled = false;
  
  // "default" for all subsquent created servos
  static Boolean isEventsEnabledDefault = null;

  Double maxVelocity;

  boolean enabled = false;

  Double velocity;

  double acceleration = -1;

  Double lastPos;

  @Deprecated
  private boolean autoEnable = true;

  /**
   * defaultDisableDelayNoVelocity this make sense if velocity == -1 a timer is
   * launched to delay disable
   */
  public Integer disableDelayNoVelocity;

  /**
   * disableDelayIfVelocity this make sense if velocity &gt; 0 a timer is launched
   * for an extra delay to disable
   */
  public Integer disableDelay;
  private boolean moving;
  double currentPosInput;
  boolean autoDisable;

  private boolean overrideAutoDisable = false;

  private transient Timer autoDisableTimer;
  private int SensorPin = -1;
  private ArrayList<Integer> sensorValues = new ArrayList<Integer>();
  // stored values corresponding to min and max servo sensor feedback
  // generated by autoCalibrate >> WIP
  int autoCalibrateMin = 0;
  int autoCalibrateMax = 0;
  // this var will break a current moveToBlocking to avoid potential conflicts
  transient Object moveToBlocked = new Object();
  private int servoTorque;

  public transient static final int SERVO_EVENT_STOPPED = 1;
  public transient static final int SERVO_EVENT_POSITION_UPDATE = 2;

  /*
   * public static class ServoEventData { public String name; public Double pos;
   * public Integer state; public double velocity; public Double targetPos; //
   * public int type; }
   */
  public static class ServoEventData {
    public String name;
    public Double pos;
    public Integer state;
    public double velocity;
    public Double targetPos;
    Servo src;
    // public int type;
  }

  public Servo(String n) {
    super(n);
    refreshControllers();
    subscribe(Runtime.getInstance().getName(), "registered", this.getName(), "onRegistered");
    lastActivityTime = System.currentTimeMillis();
    if (isEventsEnabledDefault != null) {
      isEventsEnabled = isEventsEnabledDefault;
    }

    // here we define default values if not inside servo.json
    if (mapper == null) {
      mapper = new Mapper(0, 180, 0, 180);
    }
    if (rest == null) {
      rest = 90.0;
    }
    if (lastPos == null) {
      lastPos = rest;
    }
    if (velocity == null) {
      velocity = -1.0;
    }
    if (maxVelocity == null) {
      maxVelocity = -1.0;
    }
    if (disableDelayNoVelocity == null) {
      disableDelayNoVelocity = 10000;
    }
    if (disableDelay == null) {
      disableDelay = 1000;
    }
    if (targetPos == null) {
      targetPos = rest;
    }
  }

  public void onRegistered(ServiceInterface s) {
    refreshControllers();
    broadcastState();
  }

  @Override
  public void addServoEventListener(NameProvider service) {
    isEventsEnabled = true;
    addListener("publishServoEvent", service.getName(), "onServoEvent");
  }

  @Override
  public void removeServoEventListener(NameProvider service) {
    isEventsEnabled = false;
    removeListener("publishServoEvent", service.getName(), "onServoEvent");
  }

  public void addIKServoEventListener(NameProvider service) {
    isIKEventEnabled = true;
    addListener("publishIKServoEvent", service.getName(), "onIKServoEvent");
  }

  public boolean isSweeping() {
    return isSweeping;
  }

  /**
   * Equivalent to the Arduino IDE Servo.attach(). It energizes the servo
   * sending pulses to maintain its current position.
   */
  @Override
  public void enable() {
    enable(pin);
  }

  /**
   * This method will attach to the currently set controller with the specified
   * pin. attach(pin) is deprecated use enable(pin)
   */
  @Deprecated
  @Override
  public void attach(int pin) {
    enable(pin);
  }

  /**
   * Enabling PWM for the Servo. Equivalent to Arduino's Servo.attach(pin). It
   * energizes the servo sending pulses to maintain its current position.
   */
  public void enable(Integer pin) {
    lastActivityTime = System.currentTimeMillis();
    controller.servoAttachPin(this, pin);
    this.pin = pin;
    enabled = true;
    broadcastState();
    invoke("publishServoEnable", getName());
  }

  /**
   * This method will disable and disconnect the servo from it's controller.
   */
  @Override
  public void detach() {
    enabled = false;
    if (controller != null) {
      detachServoController(controller);
    }
    broadcastState();
    // TODO: this doesn't seem to be consistent depending on how you invoke
    // "detach()" ...
    // invoke("publishServoDetach", getName());
  }

  /**
   * This method will leave the servo connected to the controller, however it
   * will stop sending pwm messages to the servo.
   */
  @Override
  public void disable() {
    enabled = false;
    if (controller != null) {
      controller.servoDetachPin(this);
      // detach(controller);
    }
    broadcastState();
    // TODO: this doesn't seem to be consistent depending on how you invoke
    // "detach()" ...
    invoke("publishServoDisable", getName());
  }

  public boolean eventsEnabled(boolean b) {
    isEventsEnabled = b;
    broadcastState();
    return b;
  }
  
  static public boolean eventsEnabledDefault(boolean b) {
    isEventsEnabledDefault = b;
    return b;
  }

  public boolean isEventsEnabled() {
    return isEventsEnabled;
  }

  // @Override
  public ServoController getController() {
    return controller;
  }

  public long getLastActivityTime() {
    return lastActivityTime;
  }

  @Override
  public double getMax() {
    return mapper.getMaxX();
  }

  @Override
  public double getMin() {
    return mapper.getMinX();
  }

  public double getMaxInput() {
    return mapper.getMaxInput();
  }

  public double getMinInput() {
    return mapper.getMinInput();
  }

  public double getMaxOutput() {
    return mapper.getMaxOutput();
  }

  public double getMinOutput() {
    return mapper.getMinOutput();
  }

  @Override
  public double getRest() {
    return rest;
  }

  public boolean isAttached() {
    // this is not pin attach
    return controller != null;
  }

  public boolean enabled() {
    return enabled;
  }

  public boolean isEnabled() {
    return enabled();
  }

  @Deprecated
  public boolean isAutoDisabled() {
    return autoDisable;
  }

  @Override
  public boolean isInverted() {
    return mapper.isInverted();
  }

  public void map(double minX, double maxX, double minY, double maxY) {
    if (mapper == null) {
      mapper = new Mapper(0, 180, 0, 180);
    }
    if (minX != mapper.getMinX() || maxX != mapper.getMaxX() || minY != mapper.getMinY() || maxY != mapper.getMaxY()) {
      mapper = new Mapper(minX, maxX, minY, maxY);
      broadcastState();
    }
  }

  public ServoEventData publishMoveTo() {
    ServoEventData ret = new ServoEventData();
    ret.src = this;
    return ret;
  }

  public synchronized void moveTo(double pos) {
    // breakMoveToBlocking=true;
    synchronized (moveToBlocked) {
      moveToBlocked.notify(); // Will wake up MoveToBlocked.wait()
    }
    if (controller == null) {
      error(String.format("%s's controller is not set", getName()));
      return;
    }

    if (pos < mapper.getMinX()) {
      pos = mapper.getMinX();
    }
    if (pos > mapper.getMaxX()) {
      pos = mapper.getMaxX();
    }
    targetPos = pos;

    if (!isEnabled()) {
      if (pos != lastPos || overrideAutoDisable || !getAutoDisable()) {
        enable();
      }
    }

    if (intialTargetPosChange) {
      targetPosBeforeSensorFeebBackCorrection = targetPos;
    }

    targetOutput = getTargetOutput();// mapper.calcOutput(targetPos); //

    lastActivityTime = System.currentTimeMillis();

   if (lastPos != pos || !isEventsEnabled) { // recently changed - jme does not want to use ServoEvents 
      // take care if servo will disable soon
      if (autoDisableTimer != null) {
        autoDisableTimer.cancel();
        autoDisableTimer = null;
      }
      controller.servoMoveTo(this);
    }
    if (!isEventsEnabled || lastPos == pos) {
      lastPos = targetPos;
      broadcastState();
    }
  }

  @Override
  public boolean moveToBlocking(double pos) {
    if (velocity < 0) {
      log.info("No effect on moveToBlocking if velocity == -1");
    }
    if (!isEventsEnabled) {
      this.addServoEventListener(this);
    }
    targetPos = pos;
    this.moveTo(pos);
    // breakMoveToBlocking=false;
    waitTargetPos();
    return true;
  }

  @Override
  public void waitTargetPos() {
    {
      if (isMoving() || Math.round(lastPos) != Math.round(targetPos)) {
        if (velocity > 0) {
          synchronized (moveToBlocked) {
            try {
              // Will block until moveToBlocked.notify() is called on another
              // thread.
              moveToBlocked.wait(30000);// 30s timeout security delay
            } catch (InterruptedException e) {
              log.info("servo {} moveToBlocked was interrupted", getName());
            }
          }
        }
      }
    }
  }

  private void delayDisable() {
    lastPos = targetPos;
    broadcastState();
    if (!isMoving()) {
      if (autoDisable) {
        int delayBeforeDisable = disableDelayNoVelocity;
        if (velocity > -1) {
          delayBeforeDisable = disableDelay;
        }
        if (autoDisableTimer != null) {
          autoDisableTimer.cancel();
          autoDisableTimer = null;
        }
        autoDisableTimer = new Timer();
        autoDisableTimer.schedule(new TimerTask() {
          @Override
          public void run() {
            if (!overrideAutoDisable && !isMoving()) {
              disable();
            }
            synchronized (moveToBlocked) {
              moveToBlocked.notify(); // Will wake up MoveToBlocked.wait()
            }
          }
        }, (long) delayBeforeDisable);
      } else {
        synchronized (moveToBlocked) {
          moveToBlocked.notify(); // Will wake up MoveToBlocked.wait()
        }
      }
    }
    broadcastState();
  }

  public Double publishServoEvent(Double position) {
    return position;
  }

  public List<String> refreshControllers() {
    controllers = Runtime.getServiceNamesFromInterface(ServoController.class);
    // FIXME should be Runtime.getServiceNamesFromInterface(Simulator.class)
    controllers.addAll(Runtime.getServiceNamesFromInterface(Simulator.class));
    // controllers.add("i01.head.rothead");
    return controllers;
  }

  @Override
  public void releaseService() {
    // disable();
    detachServoController(controller);
    super.releaseService();
  }

  @Override
  public void startService() {
    super.startService();
    if (isEventsEnabled) {
      this.addServoEventListener(this);
    }
  }

  public void rest() {
    moveTo(rest);
  }

  public void setInverted(boolean invert) {
    mapper.setInverted(invert);
    broadcastState();
  }

  @Override
  public void setMinMax(double min, double max) {
    map(min, max, min, max);
  }

  @Override
  public void setRest(double rest) {
    this.rest = rest;
  }

  public void setAnalogSensorPin(Integer pin) {
    // TODO: update the interface with the double version to make all servos
    // implement
    // a double for their positions.
    this.SensorPin = pin;
  }

  /**
   * setSpeed is deprecated, new function for speed control is setVelocity()
   */
  @Deprecated
  public void setSpeed(double speed) {

    // KWATTERS: The realtionship between the old set speed value and actual
    // angular velocity was exponential.
    // To create a model to map these, I took the natural log of the speed
    // values, computed a linear regression line
    // y=mx+b
    // And then convert it back to exponential space with the e^y
    // approximating this with the equation
    // val = e ^ (slope * x + intercept)
    // slope & intercept were fitted by taking a linear regression of the
    // log values
    // of the mapping.

    // Speed,NewFunction,OldMeasured
    // 0.1,3,6
    // 0.2,5,7
    // 0.3,7,9
    // 0.4,9,9
    // 0.5,13,11
    // 0.6,19,13
    // 0.7,26,18
    // 0.8,36,27
    // 0.9,50,54

    // These 2 values can be tweaked for a slightly different curve that
    // fits the observed data.
    double slope = 3.25;
    double intercept = 1;

    double vel = Math.exp(slope * speed + intercept);
    // set velocity to 0.0 if the speed = 1.0.. This skips the velocity
    // calculation logic.
    if (speed >= 1.0) {
      vel = maxVelocity;
    }
    setVelocity((int) vel);
  }

  // choose to handle sweep on arduino or in MRL on host computer thread.
  public void setSpeedControlOnUC(boolean b) {
    speedControlOnUC = b;
  }

  public void setSweepDelay(int delay) {
    sweepDelay = delay;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.myrobotlab.service.interfaces.ServoControl#stopServo()
   */
  @Override
  public void stop() {
    isSweeping = false;
    sweeper = null;
    controller.servoSweepStop(this);
    broadcastState();
  }

  public void sweep() {
    double min = mapper.getMinX();
    double max = mapper.getMaxX();
    sweep(min, max, 50, 1);
  }

  public void sweep(double min, double max) {
    sweep(min, max, 1, 1);
  }

  // FIXME - is it really speed control - you don't currently thread for
  // factional speed values
  public void sweep(double min, double max, int delay, double step) {
    sweep(min, max, delay, step, false);
  }

  public void sweep(double min, double max, int delay, double step, boolean oneWay) {
    mapper.setMinMaxInput(min, max);
    /*
     * THIS IS A BUGG !!! mapper.setMin(min); mapper.setMax(max);
     */

    this.sweepDelay = delay;
    this.sweepStep = step;
    this.sweepOneWay = oneWay;

    // FIXME - CONTROLLER TYPE SWITCH
    if (speedControlOnUC) {
      controller.servoSweepStart(this); // delay &
      // step
      // implemented
    } else {
      if (isSweeping) {
        stop();
      }

      sweeper = new Sweeper(getName());
      sweeper.start();
    }

    isSweeping = true;
    broadcastState();
  }

  /*
   * Writes a value in microseconds (uS) to the servo, controlling the shaft
   * accordingly. On a standard servo, this will set the angle of the shaft. On
   * standard servos a parameter value of 1000 is fully counter-clockwise, 2000
   * is fully clockwise, and 1500 is in the middle.
   * 
   * Note that some manufactures do not follow this standard very closely so
   * that servos often respond to values between 700 and 2300. Feel free to
   * increase these endpoints until the servo no longer continues to increase
   * its range. Note however that attempting to drive a servo past its endpoints
   * (often indicated by a growling sound) is a high-current state, and should
   * be avoided.
   * 
   * Continuous-rotation servos will respond to the writeMicrosecond function in
   * an analogous manner to the write function.
   * 
   */
  public void writeMicroseconds(Integer uS) {
    log.info("writeMicroseconds({})", uS);
    controller.servoWriteMicroseconds(this, uS);
    lastActivityTime = System.currentTimeMillis();
    broadcastState();
  }

  @Override
  public Integer getPin() {
    return pin;
  }

  @Override
  public double getPos() {
    if (targetPos == null) {
      return rest;
    } else {
      return targetPos;
    }
  }

  /**
   * The attachServoController method is used to "attach" 2 services together,
   * the ServoControl and the ServoController. All the necessary data needed to
   * attach this Servo to the servo controller should be available. First we
   * check to see if this controller is already attached. If it is we are done.
   * If not we proceed to attach the controller and at the end of the function
   * we call the controller.attach(this) to give the controller an opportunity
   * to do its attach logic.
   * 
   * https://www.google.com/search?q=attach+myrobotlab and oq=attach+myrobotlab
   */
  public void attachServoController(Attachable attachable) throws Exception {
    
    ServoController sc = null;
    if (attachable == null){
      return;
    }
    
    // find servo controller - 
    if (attachable instanceof ServoController) {
      sc = (ServoController)attachable;
    } else if (attachable instanceof Simulator) {
      sc = ((Simulator)attachable).getServoController();
    } else {
      log.error("{} unsupported type {}", attachable.getName(), Attachable.class.getCanonicalName());
      return;
    }
    
    if (isAttachedServoController((ServoController)sc)) {
      log.info("{} servo is already attached to attachable {}", getName(), this.controller.getName());
      return;
    } else if (this.controller != null && this.controller != sc) {
      // we're switching controllers - need to detach first
      detach();
    }

    targetOutput = getTargetOutput();// mapper.calcOutput(targetPos);

    // now attach the attachable the attachable better have
    // isAttach(ServoControl) to prevent an infinite loop
    // if attachable.attach(this) is not successful, this should throw
    sc.attachServoControl(this);

    // set the controller
    this.controller = sc;
    this.controllerName = sc.getName();

    // the controller is attached now
    // its time to attach the pin
    enable(pin);
    broadcastState();
    
  }

  public void attach() throws Exception {
    if (this.getPin() != null && this.controllerName != null)
      attach(this.controllerName, this.getPin());
  }

  /**
   * "Helper" methods - allows remote attaching of services by using name value
   * 
   * @param controllerName
   *          - controllers name
   * @param pin
   *          - pin to enable pwm
   * @throws Exception
   *           - if attach fails
   */
  public void attach(String controllerName, int pin) throws Exception {
    this.pin = pin;
    attachServoController((ServoController) Runtime.getService(controllerName));
  }

  /**
   * 
   * "Helper" methods - allows remote attaching of services by using name value
   * 
   * @param controllerName
   *          - controllers name
   * @param pin
   *          - pin to enable pwm
   * @param pos
   *          - position to set servo
   * @throws Exception
   */
  public void attach(String controllerName, Integer pin, Double pos) throws Exception {
    this.pin = pin;
    this.targetPos = pos;
    attachServoController(Runtime.getService(controllerName));
  }

  /**
   * attach will default the position to a default reset position since its not
   * specified
   */
  @Override
  public void attach(ServoController controller, int pin) throws Exception {
    this.pin = pin;
    attachServoController(controller);
  }

  public void attach(ServoController controller, int pin, double pos) throws Exception {
    this.pin = pin;
    this.targetPos = pos;
    attachServoController(controller);
  }

  // FIXME - setController is very deficit in its abilities - compared to the
  // complexity of this
  @Override
  public void attach(ServoController controller, int pin, double pos, double velocity) throws Exception {
    this.pin = pin;
    this.targetPos = pos;
    this.velocity = velocity;
    attachServoController(controller);
  }

  public boolean isAttachedServoController(ServoController controller) {
    return this.controller == controller && controller != null;
  }

  @Override
  public void detach(String controllerName) {
    detachServoController(Runtime.getService(controllerName));
  }

  @Override
  public void detachServoController(Attachable controller) {
    ServoController sc = null;
    
    if (controller == null){
      return;
    }
    
    if (controller instanceof ServoController) {
      sc = (ServoController)controller;
    } else if (controller instanceof Simulator) {
      sc = ((Simulator)controller).getServoController();
    } else {
      log.error("{} unsupported type {}", controller.getName(), Attachable.class.getCanonicalName());
      return;
    }
    
    // shutdown pwm
    sc.servoDetachPin(this);
    sc.detach(this);
    
    this.controller = null;
    this.enabled = false;
    broadcastState();
  }
  
  public void setMaxVelocity(double velocity) {
    this.maxVelocity = velocity;
  }

  public void setVelocity(double velocity) {
    if (maxVelocity != -1 && velocity > maxVelocity) {
      velocity = maxVelocity;
      log.info("Trying to set velocity to a value greater than max velocity");
    }
    this.velocity = velocity;
    if (controller != null) {
      controller.servoSetVelocity(this);
      broadcastState();
    }
  }

  /* setAcceleration is not fully implemented **/
  public void setAcceleration(double acceleration) {
    this.acceleration = acceleration;
    if (controller != null) {
      controller.servoSetAcceleration(this);
    }
  }

  @Override
  public double getMaxVelocity() {
    return maxVelocity;
  }

  @Override
  public double getVelocity() {
    return velocity;
  }

  public ServoEventData publishIKServoEvent(ServoEventData data) {
    return data;
  }

  @Override
  public void setPin(int pin) {
    this.pin = pin;
  }

  @Override
  public boolean isAttached(String name) {
    return (controller != null && controller.getName().equals(name));
  }

  @Override
  public Set<String> getAttached() {
    HashSet<String> ret = new HashSet<String>();
    if (controller != null) {
      ret.add(controller.getName());
    }
    return ret;
  }

  @Override
  public double getAcceleration() {
    return acceleration;
  }

  /**
   * getCurrentPos() - return the calculated position of the servo use
   * lastActivityTime and velocity for the computation
   * 
   * @return the current position of the servo
   */
  public Double getCurrentPos() {
    return currentPosInput;
  }

  public double getCurrentPosOutput() {
    return mapper.calcOutput(getCurrentPos());
  }

  /**
   * return time to move the servo to the target position, in ms
   * 
   * @return
   */
  int timeToMove() {
    if (velocity <= 0.0) {
      return 1;
    }
    double delta = Math.abs(mapper.calcOutput(targetPos) - mapper.calcOutput(lastPos));
    double time = delta / velocity * 1000;
    return (int) time;
  }

  /*
   * Deprecated enableAutoAttach will attach a servo when ask to move and it
   * when the move is complete
   * 
   */
  @Deprecated
  public void enableAutoAttach(boolean autoAttach) {
    warn("enableAutoAttach is disabled please use enableAutoEnable");
    this.autoEnable = autoAttach;
  }

  @Deprecated
  public void enableAutoEnable(boolean autoEnable) {
    this.autoEnable = autoEnable;
  }

  // [experimental} generic analog sensor feedback to servo
  // any analog sensor can be used ( finger tip / sensor on servo bed ... )
  // of course you need a good signal, time to play with aref and resistors
  // sensor need to be calibrated, but you can skip the calibration step by
  // setting :
  // autoCalibrateMin + autoCalibrateMax ( range values returned by arduino )
  // TODO javadoc, talk about methods right place

  public boolean autoCalibrateSensor() {
    if (SensorPin == -1) {
      error("Please select sensor source");
      return false;
    }
    overrideAutoDisable = true;
    int min = autoCalibrateSensorMin();
    int max = autoCalibrateSensorMax();
    // extra min calibration because foam is unstable
    int min2 = autoCalibrateSensorMin();
    min = MathUtils.getPercentFromRange(min, min2, 50);
    overrideAutoDisable = false;
    if (max != min && max > min) {
      return true;
    }
    return false;
  }

  public int autoCalibrateSensorMin() {
    if (SensorPin == -1) {
      error("Please select sensor source");
      return 0;
    }
    subscribe(this.getController().getName(), "publishPinArray", getName(), "publishedSensorEventCalibration");
    log.info("Starting " + this.getName() + " calibration");
    this.setVelocity(50);
    log.info("MoveTo MIN, and wait average sensor rest infos");
    this.moveToBlocking(0);
    sensorValues.clear();
    this.getController().enablePin(SensorPin, 10);
    sleep(3000);
    this.getController().disablePin(SensorPin);
    // We need a little error correction, some stats will do the job
    autoCalibrateMin = MathUtils.averageMaxFromArray(10, sensorValues);
    log.info("autoCalibrateMin : " + autoCalibrateMin);
    unsubscribe(this.getController().getName(), "publishPinArray");
    return autoCalibrateMin;
  }

  public int autoCalibrateSensorMax() {
    if (SensorPin == -1) {
      error("Please select sensor source");
      return 0;
    }
    log.info("MoveTo MAX, and wait sensor max");
    subscribe(this.getController().getName(), "publishPinArray", getName(), "publishedSensorEventCalibration");
    this.setVelocity(30);
    sensorValues.clear();
    this.getController().enablePin(SensorPin, 10);
    this.moveToBlocking(180);
    sleep(2000);
    // we need to know the maximum force the servo can do
    log.info(this.currentPosInput + "");
    this.getController().disablePin(SensorPin);
    autoCalibrateMax = MathUtils.averageMaxFromArray(10, sensorValues);
    this.moveToBlocking(0);
    if (autoCalibrateMax - autoCalibrateMin > 100) {
      log.info("autoCalibrateMax : " + autoCalibrateMax);
      return autoCalibrateMax;
    } else {
      error("Calibration error : sensor timeout caused by maxvalues-minvalues<100");
    }
    getController().disablePin(SensorPin);
    unsubscribe(getController().getName(), "publishPinArray");
    return 0;
  }

  public boolean enableSensorFeedback(int torqueInPercent) {
    // 100%=force servo to max
    // 50%=half torque
    // ...
    overrideAutoDisable = true;
    this.servoTorque = torqueInPercent;
    if (autoCalibrateMax <= autoCalibrateMin) {
      error("Problem with magic, sensor attached to " + this.getName() + " is not calibrated");
      return false;
    }
    if (torqueInPercent < 100 && torqueInPercent > 0) {
      unsubscribe(getController().getName(), "publishPinArray");
      subscribe(this.getController().getName(), "publishPinArray", getName(), "publishedSensorEventFeedback");
      this.getController().disablePin(SensorPin);
      this.getController().enablePin(SensorPin, 10);
    } else {
      this.getController().disablePin(SensorPin);
      overrideAutoDisable = false;
      log.info("set servo torque to " + torqueInPercent + " % > feedback disabled");
      unsubscribe(getController().getName(), "publishPinArray");
    }
    log.info("set servo torque to " + torqueInPercent + " % > feedback activated");
    return true;

  }

  public void publishedSensorEventCalibration(PinData[] data) {
    for (int i = 0; i < data.length; i++) {
      log.info("Current " + this.getName() + " calibration sensor value:" + data[i].value);
      sensorValues.add(data[i].value);
    }
  }

  private int[] feedBackCounter = { 0 };

  public void publishedSensorEventFeedback(PinData[] data) {
    for (int i = 0; i < data.length; i++) {
      // log.info("Current "+this.getName()+" sensor value:"+data[i].value);
      sensorValues.add(data[i].value);
      // error correction and smooth signal ( average 10 values by second )
      feedBackCounter[i] += 1;
      if (feedBackCounter[i] >= 5) {
        int lastAverageValue = MathUtils.averageMaxFromArray(5, sensorValues);
        sensorValues.clear();
        sensorValues.add(lastAverageValue);
        feedBackCounter[i] = 0;
      }
    }
    if (targetPosBeforeSensorFeebBackCorrection > 0
        && MathUtils.averageMaxFromArray(5, sensorValues) > MathUtils.getPercentFromRange(autoCalibrateMin, autoCalibrateMax, this.servoTorque)) {

      this.stop();
      intialTargetPosChange = false;
      this.moveTo(this.currentPosInput - 1);
      log.info("correction -1");
      intialTargetPosChange = true;
    }
    if (targetPosBeforeSensorFeebBackCorrection < 180
        && MathUtils.averageMaxFromArray(5, sensorValues) < MathUtils.getPercentFromRange(autoCalibrateMin, autoCalibrateMax, this.servoTorque)) {

      if (Math.round(this.currentPosInput) < Math.round(targetPosBeforeSensorFeebBackCorrection)) {
        log.info("correction +X");
        this.moveTo(targetPosBeforeSensorFeebBackCorrection);
      }
    }
  }

  @Deprecated
  public void enableAutoDetach(boolean autoDetach) {
    warn("enableAutoDetach is disabled please use enableAutoDisable");
    this.autoDisable = autoDetach;
    this.addServoEventListener(this);
  }

  @Deprecated
  public void enableAutoDisable(boolean autoDisable) {
    setAutoDisable(autoDisable);
  }

  @Override
  public void setAutoDisable(boolean autoDisable) {
    this.autoDisable = autoDisable;
    this.addServoEventListener(this);
    log.info("setAutoDisable : " + autoDisable);
    delayDisable();
    broadcastState();
  }

  @Override
  public boolean getAutoDisable() {
    return this.autoDisable;
  }

  public void setDisableDelayIfVelocity(int disableDelay) {
    this.disableDelay = disableDelay;
    broadcastState();
  }

  public void setDefaultDisableDelayNoVelocity(int disableDelayNoVelocity) {
    this.disableDelayNoVelocity = disableDelayNoVelocity;
    broadcastState();
  }

  public double microsecondsToDegree(int microseconds) {
    if (microseconds <= 180)
      return microseconds;
    return (double) (microseconds - 544) * 180 / (2400 - 544);
  }

  public String publishServoEnable(String name) {
    return name;
  }

  public String publishServoDisable(String name) {
    return name;
  }

  /**
   * this output is 'always' be between 0-180
   */
  @Override
  public double getTargetOutput() {
    if (targetPos == null) {
      targetPos = rest;
    }
    if (mapper != null) {
      targetOutput = mapper.calcOutput(targetPos);
    } else {
      targetOutput = targetPos;
    }
    if (targetOutput == null) {
      targetOutput = 0.0;
    }
    return targetOutput;
  }

  @Override
  public String getControllerName() {
    return controllerName;
  }

  public boolean isMoving() {
    return moving;

  }

  @Override
  public void onServoEvent(Integer eventType, double currentPos) {
    currentPosInput = mapper.calcInput(currentPos);
    if (isIKEventEnabled) {
      ServoEventData data = new ServoEventData();
      data.name = getName();
      data.pos = currentPosInput;
      data.state = eventType;
      data.velocity = velocity;
      data.targetPos = this.targetPos;
      invoke("publishIKServoEvent", data);
    }
    if (eventType == SERVO_EVENT_STOPPED) {
      moving = false;
    } else {
      moving = true;
    }
    if (isEventsEnabled) {
      invoke("publishServoEvent", currentPosInput);
    }
  }

  public void onServoEvent(Integer eventType, Integer currentPosUs, double targetPos) {
    double currentPos = microsecondsToDegree(currentPosUs);
    onServoEvent(eventType, currentPos);
  }

  public void onIMAngles(Object[] data) {
    String name = (String) data[0];
    if (name.equals(this.getName())) {
      moveTo((double) data[1]);
    }
  }

  public void onServoEvent(Double position) {
    // log.info("{}.ServoEvent {}", getName(), position);
    if (!isMoving() && enabled()) {
      delayDisable();
    }
  }

  /*
   * Set the controller for this servo but does not attach it. see also attach()
   */
  public void setController(ServoController controller) {
    this.controller = controller;
  }

  /**
   * used to synchronize 2 servos e.g. servo1.sync(servo2) - now they move as
   * one
   */
  public void sync(ServoControl sc) {
    this.addServoEventListener(this);
    sc.addServoEventListener(sc);
    subscribe(sc.getName(), "publishServoEvent", getName(), "moveTo");
  }


  public void unsync(ServoControl sc) {
    // remove
    this.removeServoEventListener(this);
    sc.removeServoEventListener(sc);

    unsubscribe(sc.getName(), "publishServoEvent", getName(), "moveTo");
  }

  public static void main(String[] args) throws InterruptedException {
    String arduinoPort = "COM5";
    LoggingFactory.init(Level.INFO);
    VirtualArduino virtualArduino = (VirtualArduino) Runtime.start("virtualArduino", "VirtualArduino");
    Runtime.start("python", "Python");

    try {
      virtualArduino.connect(arduinoPort);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    Arduino arduino = (Arduino) Runtime.start("arduino", "Arduino");
    arduino.connect(arduinoPort);
    Adafruit16CServoDriver adafruit16CServoDriver = (Adafruit16CServoDriver) Runtime.start("adafruit16CServoDriver", "Adafruit16CServoDriver");
    adafruit16CServoDriver.attach(arduino, "0", "0x40");

    Runtime.start("gui", "SwingGui");
    Servo servo = (Servo) Runtime.start("servo", "Servo");
    try {
      servo.attach(adafruit16CServoDriver, 1);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    servo.setVelocity(20);
    log.info("It should take some time..");
    servo.moveToBlocking(0);
    servo.moveToBlocking(180);
    servo.moveToBlocking(0);
    log.info("Right?");
  }

  // pasted from inmoov1
  /**
   * Export servo configuration to a .py file
   */
  public void saveCalibration(String calibrationFilename) {
    if (calibrationFilename == null) {
      calibrationFilename = this.getName() + ".py";
    }
    File calibFile = new File(calibrationFilename);
    FileWriter calibrationWriter = null;
    try {
      calibrationWriter = new FileWriter(calibFile);
      calibrationWriter.write("##################################\n");
      calibrationWriter.write("# " + this.getSimpleName() + " auto generated calibration \n");
      calibrationWriter.write("# " + new Date() + "\n");
      calibrationWriter.write("##################################\n");

      calibrationWriter.write("\n");
      calibrationWriter.write("# Servo Config : " + this.getName() + "\n");
      calibrationWriter.write(this.getName() + ".detach()\n");
      calibrationWriter.write(this.getName() + ".setVelocity(" + this.getVelocity() + ")\n");
      calibrationWriter.write(this.getName() + ".setRest(" + this.getRest() + ")\n");
      if (this.getPin() != null) {
        calibrationWriter.write(this.getName() + ".setPin(" + this.getPin() + ")\n");
      } else {
        calibrationWriter.write("# " + this.getName() + ".setPin(" + this.getPin() + ")\n");
      }

      // save the servo map
      calibrationWriter.write(this.getName() + ".map(" + this.getMinInput() + "," + this.getMaxInput() + "," + this.getMinOutput() + "," + this.getMaxOutput() + ")\n");
      // if there's a controller reattach it at rest
      if (this.getController() != null) {
        String controller = this.getController().getName();
        calibrationWriter.write(this.getName() + ".attach(\"" + controller + "\"," + this.getPin() + "," + this.getRest() + ")\n");
      }
      String pythonBool = "False";
      if (getAutoDisable()) {
        pythonBool = "True";
      }
      calibrationWriter.write(this.getName() + ".setAutoDisable(" + pythonBool + ")\n");

      pythonBool = "False";
      if (isInverted()) {
        pythonBool = "True";
      }
      calibrationWriter.write(this.getName() + ".setInverted(" + pythonBool + ")\n");

      calibrationWriter.write("\n");
      calibrationWriter.close();
      log.info("Exported calibration file {}", calibrationFilename);

    } catch (IOException e) {
      log.error("Error writing calibration file {}", calibrationFilename);
      e.printStackTrace();
      return;
    }
  }

  public void saveCalibration() {
    saveCalibration(null);
  }

  @Override
  public void setOverrideAutoDisable(boolean overrideAutoDisable) {
    this.overrideAutoDisable = overrideAutoDisable;
    if (!overrideAutoDisable) {
      delayDisable();
    }
  }

  @Override
  public ServoControl publishMoveTo(ServoControl sc) {
    return sc;
  }

  @Override
  public Double getLastPos() {
    return lastPos;
  }

  public void preShutdown() {
    detach();
  }

}