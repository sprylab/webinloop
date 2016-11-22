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

<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
<title>Test Summary</title>
<link rel="stylesheet" type="text/css" href="css/style.css" />    
</head>

<body>
<div class="testtable-wrapper rounded shadow">
    <h1>Test Summary</h1>
    
    <table class="test">
			<caption>Short summary</caption>
			<tr>
				<th>Passed</th>
				<#assign cellClass>
					<#if reporter.failedTests != 0>
						failed
					<#else>
						success
					</#if>
				</#assign>
				<td class="${cellClass?trim}">${reporter.reporterTests?size - reporter.failedTests} of ${reporter.reporterTests?size}</td>
			</tr>
			<tr>
				<th>Duration</th>
				<td>${reporter.durationString}</td>
			</tr>
		</table>
    
    <table class="test" summary="This table gives an overview over all executed tests and their results.">
    	<caption>Test runs</caption>
    	<thead>
    		<tr>
	    		<th>Test name</th>
	    		<th>Start</th>
	    		<th>Stop</th>
	    		<th>Duration</th>
    		</tr>
    	</thead>
    	<tbody>
        <#list reporter.reporterTests as test>
		<tr class="<#if test.failed>failed<#else>success</#if>">
			<td><a href="${test.name}/index.html">${test.name}</a></td>
			<td>${test.startTime?datetime}</td>
			<td>${test.stopTime?datetime}</td>
			<td>${test.durationString}</td>
		</tr>
        </#list>
        </tbody>
    </table>
</div>
</body>
</html>