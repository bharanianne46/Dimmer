import groovy.json.JsonBuilder
definition(
        name: "Dim Dimmers",
        namespace: "Bharanianne46",
        author: "Bharani Anne",
        description: "Set Level of Dimmers",
        category: "My Apps",
        iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
        iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
        oauth: [displayName: "Thing Layer", displayLink: ""])

preferences {
    section("Allow Endpoint to Control These Things...") {
        input "dimmers", "capability.switchLevel", title: "Which Dimmers?", multiple: true, required: false
    }
}


def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {

}



mappings {

    path("/setLevel") {
        action: [
                POST: "updateAllDimmers"
        ]
    }
    path("/dimmers") {
        action: [
                GET: "listDimmers"
        ]
    }
    path("/dimmers/:id") {
        action: [
                GET: "showDimmer"
        ]
    }
    path("/dimmers/:id/:command") {
        action: [
                POST: "updateDimmer"
        ]
    }
}

//dimmers
def listDimmers() {
    dimmers?.collect{[type: "dimmer", id: it.id, name: it.displayName, level: it.currentValue('level')]}?.sort{it.name}
}
def showDimmer() {
    show(dimmers, "level")
}
void updateDimmer() {
    def level = request.JSON?.level
    update(dimmers,params.id,params.command,level)
}

void updateAllDimmers() {
    def level = request.JSON?.level
    log.debug "level value in updateAllDimmers: $level"
    dimmers?.each {
        update(dimmers,it.id,'level', level)
    }
}

private void update(devices, deviceId, command, level) {
    log.debug "update, request: params: ${params}, devices: $devices.id, ${body}"
    if (command)
    {
        def device = devices.find { it.id == deviceId }
        if (!device) {
            httpError(404, "Device not found")
        } else {

            if(command == "level")
            {
                device.setLevel(level.toInteger())
            }
            else
            {
                device."$command"()
            }
        }
    }
}

private show(devices, type) {
    def device = devices.find { it.id == params.id }
    if (!device) {
        httpError(404, "Device not found")
    }
    else {
        def attributeName = type
        def s = device.currentState(attributeName)
        [id: device.id, label: device.displayName, value: s?.value, unitTime: s?.date?.time, type: type]
    }
}

