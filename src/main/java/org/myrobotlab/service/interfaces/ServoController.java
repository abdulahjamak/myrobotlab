/**
 *                    
 * @author grog (at) myrobotlab.org
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

package org.myrobotlab.service.interfaces;

import org.myrobotlab.framework.interfaces.Attachable;

public interface ServoController extends Attachable {

  /**
   * The one and only attach which is type specific and does all the work which
   * we expect
   * 
   * @param servo
   *          the servo
   * @throws Exception
   *           e
   */
  void attachServoControl(ServoControl servo) throws Exception;

  /**
   * attach with parameters which will set attributes on ServoControl ??? rules
   * on which attributes in which service can be changed ???
   * 
   * @param servo
   *          the servo
   * @param pin
   *          the pin number
   * @throws Exception
   *           e
   */
  void attach(ServoControl servo, int pin) throws Exception;

  // this is Arduino's servo.attach
  // void servoAttach(ServoControl servo, int pin, Integer targetOutput, Integer
  // velocity);

  /*
   * Arduino's servo.attach(pin) which is just energizing on a pin
   */
  // FIXME should be servoEnable - consistent with ServoControl
  // FIXME - servoEnablePin - which this method should be called, is
  // auto-magically called in the "attach" phase,
  // to get JME to auto-attach on creation of new services, it does not "need"
  // the pin so this throws on auto-attach because
  // the pin is null at that point - but JME (and possibly other
  // servocontrollers) do not need a pin :P
  void servoAttachPin(ServoControl servo, Integer pin); // jme change to pin
                                                        // because it does'nt
                                                        // need it but
                                                        // auto-calls it

  void servoSweepStart(ServoControl servo);

  void servoSweepStop(ServoControl servo);

  void servoMoveTo(ServoControl servo);

  void servoWriteMicroseconds(ServoControl servo, int uS);

  // FIXME should be servoDisable - consistent with ServoControl
  void servoDetachPin(ServoControl servo);

  void servoSetVelocity(ServoControl servo);

  void servoSetAcceleration(ServoControl servo);

  /**
   * @param sensorPin
   * @param i
   */
  void enablePin(Integer sensorPin, Integer i);

  /**
   * @param i
   */
  void disablePin(Integer i);

}
