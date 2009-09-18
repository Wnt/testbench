/* Selenium core extensions for Vaadin */

/* Also in IDE extensions */
function getVaadinConnector(wnd) {
	if (wnd.wrappedJSObject) {
		wnd = wnd.wrappedJSObject;
	}

	var connector = null;
	if (wnd.itmill) {
		connector = wnd.itmill;
	} else if (wnd.vaadin) {
		connector = wnd.vaadin;
	}

	return connector;
}

PageBot.prototype.locateElementByVaadin = function(tkString, inDocument) {

	var connector = getVaadinConnector(this.currentWindow);

	if (!connector) {
		// Not a toolkit application
		return null;
	}

	var parts = tkString.split("::");
	var appId = parts[0];

	try {
		var element = connector.clients[appId].getElementByPath(parts[1]);
		return element;
	} catch (exception) {
		LOG.error('an error occured when locating element for '+tkString+': ' + exception);
	}
	return null;
}

/*
 PageBot.prototype.locateElementByVaadin.is_fuzzy_match = function(
 source, target) {

 if (source.wrappedJSObject) {
 source = source.wrappedJSObject;
 }
 if (target.wrappedJSObject) {
 target = target.wrappedJSObject;
 }
 if (target == source) {
 return true;
 }

 return true;

 }

 */
Selenium.prototype.doWaitForVaadin = function(locator, value) {

	// max time to wait for toolkit to settle
	var timeout = 20000;
	var foundClientOnce = false;
	
	return Selenium.decorateFunctionWithTimeout( function() {
		var wnd = selenium.browserbot.getCurrentWindow();
		var connector = getVaadinConnector(wnd);
		if (!connector) {
			// No connector found == Not a Vaadin application so we don't need to wait
			return true;
		}
		
		var clients = connector.clients;
		if (clients) {
			foundClientOnce = true;
			for ( var client in clients) {
				if (clients[client].isActive()) {
					return false;
				}
			}
			return true;
		} else {
			if (foundClientOnce) {
				// There was a client, so probably there will be again once something has refreshed
				// This happens for instance when the theme is changed on the fly
				return false;
			}
			if (!this.VaadinWarnedNoAppFound) {
				// TODO explain what this means & what to do
					LOG.warn("No testable Vaadin applications found!");
				this.VaadinWarnedNoAppFound = true;
			}
			
			return true;
	}
}, timeout);

}

Selenium.prototype.doScroll = function(locator, scrollString) {
	var element = this.page().findElement(locator);
	element.scrollTop = scrollString;
};

Selenium.prototype.doContextmenu = function(locator) { 
     var element = this.page().findElement(locator); 
     this.page()._fireEventOnElement("contextmenu", element, 0, 0); 
}; 

Selenium.prototype.doContextmenuAt = function(locator, coordString) { 
      if (!coordString) 
    	  coordString = '2, 2'; 
      
      var element = this.page().findElement(locator); 
      var clientXY = getClientXY(element, coordString);
      this.page()._fireEventOnElement("contextmenu", element, clientXY[0], clientXY[1]); 
};

/* Empty screenCapture command for use with export test case Vaadin */
Selenium.prototype.doScreenCapture = function(locator, value){
};

/*Enters a characte so that it gets recognized in comboboxes etc.*/
Selenium.prototype.doEnterCharacter = function(locator, value){
	this.doType(locator, value);
	if(value.length > 1){
		for(i = 0; i < value.length;i++){
			this.doKeyDown(locator, value.charAt(i));
			this.doKeyUp(locator, value.charAt(i));
		}
	}else{
		this.doKeyDown(locator, value);
		this.doKeyUp(locator, value);
	}
};

/*Sends an arrow press recognized by browsers.*/
Selenium.prototype.doPressArrowKey = function(locator, value){
	if(value.toLowerCase() == "left"){
		value="\\37";
	}else if(value.toLowerCase() == "right"){
		value="\\39";
	}else if(value.toLowerCase() == "up"){
		value="\\38";
	}else if(value.toLowerCase() == "down"){
		value="\\40";
	}
	this.doKeyDown(locator, value);
	this.doKeyUp(locator, value);
};