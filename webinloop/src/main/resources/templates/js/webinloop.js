/*******************************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * Copyright 2009 by sprylab technologies GmbH
 * 
 * WebInLoop - a program for testing web applications
 * 
 * This file is part of WebInLoop.
 * 
 * WebInLoop is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * only, as published by the Free Software Foundation.
 * 
 * WebInLoop is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License version 3 for more details
 * (a copy is included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with WebInLoop.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>
 * for a copy of the LGPLv3 License.
 ******************************************************************************/

$(document).ready(function() {

	// sets the corresponding css class depending on visible
	function toggleToggleClass(visible, object) {
		if (visible) {
			object.removeClass('toggle-close');
			object.addClass('toggle-open');
		}
		else {
			object.removeClass('toggle-open');
			object.addClass('toggle-close');
		}
	}
	
	// toggles displaying all screenshots
	var showScreenshots = false;
	$('#screenshot-toggle').click(function(event){
		showScreenshots = !showScreenshots;
		jQuery.each($('table.test input.toggle-button'), function() {$(this).click()});		
		toggleToggleClass(showScreenshots, $(this));
	});
	
	
	// toggle event on click
	$("table.test input.toggle-button").click(function(event){
		var button = $(this);
		var screenshot = $(event.target).next('div.errormessages-wrapper');
		
		if (showScreenshots) {
			// show forced
			screenshot.show('normal', function () {toggleToggleClass(screenshot.is(':visible'), button);});
		}
		else {
			screenshot.toggle('normal', function () {toggleToggleClass(screenshot.is(':visible'), button);});
		}
	});
	
});