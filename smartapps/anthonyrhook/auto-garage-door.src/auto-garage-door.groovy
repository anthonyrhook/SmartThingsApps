/**
 *  Auto Garage Door
 *
 *  Copyright 2016 Anthony Hook
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Auto Garage Door",
    namespace: "anthonyrhook",
    author: "Anthony Hook",
    description: "Automatically open my garage door when I return",
    category: "Safety & Security",
    iconUrl: "http://cdn.device-icons.smartthings.com/Transportation/transportation12-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Transportation/transportation12-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Transportation/transportation12-icn@3x.png")

preferences {
	section("Which presence sensor to tie this to?") {
    	input "presence", "capability.presenceSensor", title: "Who is this for?", required: true, multiple: false
    }
    section("Which garage door is theirs?") {
        input "garageDoor", "capability.garageDoorControl", required: true, title: "Which garage door to open?", multiple: false
    }
    section("Close the garage door when this door opens?") {
    	input "houseDoor", "capability.contactSensor", required: false, title: "Opening this door will close your garage", multiple: false
    }
}


def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(presence, "presence", garageToggleHandler)
    subscribe(houseDoor, "contact.open", garageCloserHandler)
}

def garageToggleHandler(evt) {
	def currentState = garageDoor.garageDoorControl.currentValue("door")
	log.debug "garageToggleHandler called: $evt"

    //Presense detected and the door is closed, open it.
	if("present" == evt.value && currentState?.value == "closed") {
		log.debug "Welcome home, opening $garageDoor."
		garageDoor.open()
	}
    //No presense and the door is open, close it.
	else if ("not present" == evt.value && currentState?.value == "open") {
        log.debug "Bon voyage, closing $garageDoor."
        garageDoor.close()
        //Make sure it's closed after 30 seconds
        //I started checking again after 30 seconds because I was getting inconsistent door states (null)
        //This is currently a bandaid to go back and check after 30 seconds
        def now = new Date()
        def runTime = new Date(now.getTime() + (30 * 1000)) //30 seconds
        runOnce(runTime, checkDoor)
  	}
    else {
		log.debug "I didn't make any changes."
	}
}

def garageCloserHandler(evt) {
	def currentState = garageDoor.garageDoorControl.currentValue("door")
	log.debug "garageCloserHandler called: $evt"
    if("open" == evt.value && currentState?.value == "open") {
    	log.debug "Welcome home, closing $garageDoor"
        garageDoor.close()
        //Make sure it's closed after 30 seconds
        //I started checking again after 30 seconds because I was getting inconsistent door states (null)
        //This is currently a bandaid to go back and check after 30 seconds
        def now = new Date()
        def runTime = new Date(now.getTime() + (30 * 1000)) //30 seconds
        runOnce(runTime, checkDoor)
    } else {
		log.debug "I didn't make any changes."
	}
}

//current bandaid - I'd rather not have it if I don't have to
def checkDoor() {
	def currentState = garageDoor.garageDoorControl.currentValue("door")
    log.debug "The garage door is $currentState"
    if (!currentState?.value == "closed" || !currentState?.value) { //attempting to handle the null case
    // if (!currentState.value == "open") {
   		log.debug "Sorry, your door didn't close the first time. I'm trying again."
    	garageDoor.close()
    	def now = new Date()
		def runTime = new Date(now.getTime() + (30 * 1000)) //30 seconds
		runOnce(runTime, checkDoor)
      } else {
      log.debug "I made sure $garageDoor is closed."
	}
}
