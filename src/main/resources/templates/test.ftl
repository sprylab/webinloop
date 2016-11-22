<#--
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

Copyright 2009-2011 by sprylab technologies GmbH

WebInLoop - a program for testing web applications

This file is part of WebInLoop.

WebInLoop is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License version 3
only, as published by the Free Software Foundation.

WebInLoop is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License version 3 for more details
(a copy is included in the LICENSE file that accompanied this code).

You should have received a copy of the GNU Lesser General Public License
version 3 along with WebInLoop.  If not, see
<http://www.gnu.org/licenses/lgpl-3.0.html>
for a copy of the LGPLv3 License.
-->

<#-- Freemarker template for generating a HTML report from a test result model. -->
<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<!--

This report is created by WebInLoop (http://www.webinloop.org).
Copyright 2009 by sprylab technologies GmbH

Icons taken from: http://www.famfamfam.com/lab/icons/silk/

-->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
<title>Test run overview for test ${reporterTest.name}</title>
<link rel="stylesheet" type="text/css" href="../css/style.css" media="screen" />
<link rel="stylesheet" type="text/css" href="../css/slimbox/slimbox2.css" media="screen" />
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/slimbox2.js"></script>
<script type="text/javascript" src="../js/webinloop.js"></script>
</head>

<body>

<div class="testtable-wrapper rounded shadow">

<h1>Test run overview for test ${reporterTest.name}</h1>

<table class="test">
	<caption>Basic information</caption>
	<tr>
		<th>Passed</th>
		<#assign cellClass>
	 		<#if reporterTest.failed>
				failed
			<#else>
				success
			</#if>
		</#assign>
		<td class="${cellClass?trim}">${reporterTest.nrOfRunEntries - reporterTest.nrOfFailedEntries} of ${reporterTest.reporterEntries?size}</td>
	</tr>
	<tr>
		<th>Start</th>
		<td>${reporterTest.startTime?datetime}</td>
	</tr>
	<tr>
		<th>Stop</th>
		<td>${reporterTest.stopTime?datetime}</td>
	</tr>
	<tr>
		<th>Duration</th>
		<td>${reporterTest.durationString}</td>
	</tr>
	<tr>
		<th>Base URL</th>
		<td><a href="${reporterTest.baseUrl}">${reporterTest.baseUrl}</a></td>
	</tr>		
</table>

<#if screenshotting>
<div class="buttons-wrapper">
	<input id="screenshot-toggle" class="toggle-button toggle-close" type="button" value="Toggle all screenshots" />
</div>
</#if>

<table class="test" summary="This table gives detailed information about all the directives that were executed during this test run with corresponding screenshots for every important directive.">
	<caption>Test result</caption>
	<thead>
		<tr>
			<th>line</th>
			<th>timestamp</th>
			<th>command</th>
			<th>target</th>
			<th>value</th>
			<th><#if screenshotting>screenshots<#else>messages</#if></th>
		</tr>
	</thead>
	<tbody>
	<#list reporterTest.reporterEntries as entry>
	<#assign rowClass>
		<#if entry.failed>
			failed
		<#elseif !entry.run>
			notRun
		<#else>
			success
		</#if>
	</#assign>
	<tr class="${rowClass?trim}">
		<td>${entry_index + 1}</td>
		<td><#if entry.timestamp??>${entry.timestamp?datetime}<#else>N/A</#if></td>
		<td>${entry.directive.command}</td>
		<td>${entry.directive.target?xhtml}</td>
		<td>${entry.directive.value?xhtml}</td>
		<td>
			<#if entry.errorMessages?size != 0>
				<#if screenshotting>
				<input class="toggle-button toggle-close" type="button" value="Toggle screenshot" />
				</#if>
				<div class="errormessages-wrapper" <#if !screenshotting>style="display: block;"</#if>>
				<#list entry.errorMessages as errorMessage>
					<div class="errormessage">
						<#if screenshotting>
						<div class="screenshot">
							<a class="dropshadow" href="${errorMessage.screenshot.screenshotFileName}" title="${errorMessage.errorMessage?xhtml}" rel="lightbox-${reporterTest.name}">
			    				<img src="${errorMessage.screenshot.screenshotFileName}" alt="${errorMessage.errorMessage?xhtml}" width="150" />
							</a>
						</div>
						</#if>
						<div class="description"><pre>${errorMessage.errorMessage?xhtml}</pre></div>
					</div>
				</#list>
				</div>
			</#if>
		</td>
	</tr>
	</#list>
	</tbody>
</table>
</div>

<div class="back">
	<a href="../index.html">&#171; Back to list of run test</a>
</div>

</body>
</html>