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
	section("Who's door to open?") {
    	input "presence", "capability.presenceSensor", title: "presence", required: true, multiple: false 
    }
    section("Which garage door is theirs?") {
        input "garageDoor", "capability.garageDoorControl", required: true, title: "Which Garage Door", multiple: false
    }
    section("What door do you exit the garage from?") {
    	input "houseDoor", "capability.contactSensor", required: false, title: "Opening this door will close your garage", multiple: false
    }
    /*section("Run a routine, too?") {
    log.debug location.helloHome?.getPhrases()*.label
    def actions = location.helloHome?.getPhrases()*.label
      if (actions) {
        log.debug "Phrase list found: ${actions}"
        input "phrase", "enum", title: "Trigger Hello Home Action", required: false, options: phrases
       }
       else log.debug "I can't find phrases"
    }*/
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
	subscribe(presence, "presence", garageOpenerHandler)
    subscribe(houseDoor, "contact.open", garageCloserHandler)
}

def garageOpenerHandler(evt) {
  log.debug "garageHandler called: $evt"
  if("present" == evt.value) {
    log.debug garageDoor
    garageDoor.open()
  }
  else {
    console.log "Not opening it, it's $garageDoor.contact"
  }
 }

def garageCloserHandler(evt) {
  log.debug "exitGarageHandler called: $evt"
  if("open" == evt.value) {
    log.debug "Contact is in ${evt.value} state"
    garageDoor.close()
  }
}

/*
private def changeMode(mode) {
    myDebug("changeMode: $mode, location.mode = $location.mode, location.modes = $location.modes")

    if (location.mode != mode && location.modes?.find { it.name == mode }) {
        myTrace("setLocationMode: ${mode}")
        setLocationMode(mode)
    } else {
        if (location.mode == mode) {
            myTrace("Mode unchanged. Already set to: ${mode}")
        } else {
            myTrace("Mode unchanged. Unable to find defined mode named: ${mode}")
        }
    }
}*/