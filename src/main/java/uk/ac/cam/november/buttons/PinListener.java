// This class is an event listener of one button
// Exactly when button state transitions from "not pressed" to "pressed",
// this class will call the message generator method.

// TODO(ml693): figure out the method NAME to be invoked. 

// The method call will be supplied by
// the name of the button that was just pressed.

package uk.ac.cam.november.buttons;

import uk.ac.cam.november.messages.SpeechSynthesis;

import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class PinListener implements GpioPinListenerDigital {

	// Corresponds to the button that this instance of class is listening to.
	final private String buttonName;

	public PinListener(final String buttonNameInput) {
		buttonName = buttonNameInput;
	}	

	@Override
	public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {

		if (event.getState().isHigh()) {
			// TODO(ml693): this message is just for debugging purposes.
			// Remove after the system is fully tested.
			System.out.println(buttonName + " button has just been pressed.");

			// TODO(ml693): figure out the method to be invoked
			// and invoke it.
			SpeechSynthesis.play(buttonName);
		}

	}
	
}
