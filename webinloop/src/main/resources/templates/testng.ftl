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

<#-- Freemarker template for generating a TestNG Java class from a test model and configured properties. -->

package ${properties.package};

import org.testng.annotations.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import com.thoughtworks.selenium.SeleniumException;

<#list properties.imports?split(";") as import>
	<#if import != "">
		import ${import};
	</#if>
</#list>

public class ${model.name} extends ${properties.baseClass} {
	static {
		System.out.println("${model.name} test class loaded.");
	}
	private void addSourceCode() {
		<#list model.directives as directive>
			this.reporter.addSourceDirective(new com.sprylab.webinloop.directives.Directive("${directive.command}", "${directive.target?j_string}", "${directive.value?j_string}"));
		</#list>		
	}
	
	@Test
	public void ${model.name?uncap_first}() throws Exception {
		addSourceCode();
		<#if properties.windowWidth != "" && properties.windowHeight != "">
			<#if properties.windowWidth == "max" && properties.windowHeight == "max">
				selenium.windowMaximize();
			<#else>
				<#assign windowWidth>
					<#if properties.windowWidth == "max">screen.width<#else>${properties.windowWidth}</#if>
				</#assign>
				<#assign windowHeight>
					<#if properties.windowHeight == "max">screen.height<#else>${properties.windowHeight}</#if>
				</#assign>
				selenium.setWindowSize("${windowWidth?trim},${windowHeight?trim}");
				selenium.setWindowPosition("0,0");
			</#if>
		</#if>
		
			selenium.setTimeout("${properties.milliSecondsToWaitForPageToLoad}");
		
			int _goto = 0;
			while(true) { switch(_goto) {
		<#list model.directives as directive>
			<#assign index>${directive_index?c}</#assign>
			case ${index}:
			this.reporter.setCurrentDirective(${index});
			_goto = command${index}();
			if (_goto >= 0) continue;
		</#list>
			}
			break;
		}
		checkForFailure();
	}
	
	<#list model.directives as directive>
		<#-- ${variableX} => " + getTestVariableValue("variableX") + " for target and value -->
		<#assign target>${directive.target?j_string?replace("\\$\\{([^\\}]+)\\}", "\" + getTestVariableValue(\"$1\") + \"", "r")?replace("javascript\\{(.*)\\}", "\" + selenium.getEval(\"$1\") + \"", "r")?replace("storedVars\\['([^']*)'\\]", "'\" + StringEscapeUtils.escapeJavaScript(getTestVariableValue(\"$1\")) + \"'", "r")}</#assign>
		<#assign value>${directive.value?j_string?replace("\\$\\{([^\\}]+)\\}", "\" + getTestVariableValue(\"$1\") + \"", "r")?replace("javascript\\{(.*)\\}", "\" + selenium.getEval(\"$1\") + \"", "r")?replace("storedVars\\['([^']*)'\\]", "'\" + StringEscapeUtils.escapeJavaScript(getTestVariableValue(\"$1\")) + \"'", "r")}</#assign>
	private int command${directive_index?c}() throws Exception {
		try {
			<#switch directive.type>
			<#case "Goto">
					<#if value != "">
					if ("true".equals("" + getTestVariableValue("${value}")))
					</#if>
						{ return ${target} - 1; }
		  			<#break>
			<#case "Action">
					<#if directive.directiveName == "type" || directive.directiveName?ends_with("Cookie") || directive.directiveName == "select" || directive.directiveName?starts_with("add") || directive.directiveName?starts_with("removeSelection") || directive.directiveName == "rollup" || directive.directiveName == "captureEntirePageScreenshot">
						selenium.${directive.directiveName}("${target}", "${value}"); 
		  			<#elseif directive.directiveName == "useXpathLibrary" || directive.directiveName?matches("select(.+)")>
		  				selenium.${directive.directiveName}("${target}");
		  			<#else>
		  				selenium.${directive.directiveName}(<#if directive.target != "">"${target}"<#if directive.value != "">, "${value}"</#if></#if>); 
		  			</#if>
		  			<#if directive.wait>selenium.waitForPageToLoad("${properties.milliSecondsToWaitForPageToLoad}");</#if>
		  			<#break>
		  	<#case "Accessor">
			  	testCaseVariables.put(<#if value != "">"${value}"<#else>"${target}"</#if>, 
			  		<#if directive.directiveName != "">
			  			<#if directive.directiveName?ends_with("Present")>
			  				Boolean.toString(selenium.is${directive.directiveName}(<#if value != "">"${target}"</#if>))
			  			<#else>
			  				selenium.get${directive.directiveName}(<#if value != "">"${target}"</#if>)
			  			</#if>
			  		<#else>
			  			"${target}"
			  		</#if>);
		  		<#break>
		  	<#case "Assertion">
		  		<#if directive.directiveName == "ErrorOnNext">
		  			setAssertErrorOnNext("${target}");
		  			<#break>
		  		</#if>
		  		<#if directive.directiveName == "FailureOnNext">
		  			setAssertFailureOnNext("${target}");
		  			<#break>
		  		</#if>
		  		<#assign command>
		  			<#if directive.negate>!</#if>
		  			<#if directive.directiveName?ends_with("Present") || directive.directiveName?ends_with("Visible") || directive.directiveName?ends_with("SomethingSelected") || directive.directiveName?ends_with("Checked") || directive.directiveName?ends_with("Editable")>
		  				selenium.is${directive.directiveName}(<#if target != "">"${target}"</#if>)
		  			<#elseif directive.directiveName?ends_with("Ordered")>
		  				selenium.is${directive.directiveName}("${target}", "${value}")
		  			<#elseif directive.directiveName?ends_with("Alert") || directive.directiveName?ends_with("Location") || directive.directiveName?ends_with("Title") || directive.directiveName?ends_with("Cookie") || directive.directiveName?contains("AllWindow") || directive.directiveName?ends_with("Source") || directive.directiveName?ends_with("Confirmation") || directive.directiveName?ends_with("Prompt") || directive.directiveName?ends_with("Speed")>
		  				BaseSeleniumTest.seleniumEquals("${target}", selenium.get${directive.directiveName}())
		  			<#elseif directive.directiveName == "Selected">
		  				<#if value?starts_with("value")>
		  					BaseSeleniumTest.seleniumEquals("${value?replace('value=', '', 'f')}", selenium.getSelectedValues("${target}"))
		  				<#elseif value?starts_with("id")>
		  					BaseSeleniumTest.seleniumEquals("${value?replace('id=', '', 'f')}", selenium.getSelectedIds("${target}"))
		  				<#elseif value?starts_with("index")>
		  					BaseSeleniumTest.seleniumEquals("${value?replace('index=', '', 'f')}", selenium.getSelectedIndexes("${target}"))
		  				<#else>
		  					BaseSeleniumTest.seleniumEquals("${value?replace('label=', '', 'f')}", selenium.getSelectedLabels("${target}"))
		  				</#if>
		  			<#else>
		  				BaseSeleniumTest.seleniumEquals("${value}", selenium.get${directive.directiveName}("${target}"))
		  			</#if>	 
		  		</#assign>
		  		<#switch directive.mode>
		  			<#case "WAIT_FOR">
		  				<#if directive.directiveName == "PopUp" || directive.directiveName == "Condition">
		  					selenium.${directive.command}("${target}", "<#if value != "">${value}<#else>${properties.milliSecondsToWaitForPageToLoad}</#if>");
		  				<#elseif directive.directiveName?ends_with("ToLoad")>
		  					selenium.${directive.command}(<#if target != "">"${target}"<#if value != "">, "${value}"</#if></#if>);
		  				<#else>
			  				for (int ms = 0;; ms += 1000) {
								if (ms >= Integer.parseInt(selenium.getTimeout())) {
									throw new Exception("Timed out after " + selenium.getTimeout() + "ms");
								}
								
								try {
									if (${command}) {
										break;
									}
								} catch (SeleniumException e) {
									// SeleniumExceptions may occur while waiting, ignore them
								}

								Thread.sleep(1000);
							}
						</#if>
		  				<#break>
		  			<#default>
		  				<#if directive.mode == "ASSERT">
		  					<#assign mode="assert">
		  				<#else>
		  					<#assign mode="verify">	  				
		  				</#if>					
		  						  				
						${mode}True(${command});
						checkForVerificationErrors();
		  				<#break>
		  		</#switch>
		  		<#break>
		  </#switch>
			} catch (Exception actionException) {
				if (assertFailureOnNext && BaseSeleniumTest.seleniumEquals(assertFailureOnNextMessage, StringUtils.removeStart(actionException.getMessage(), "ERROR: "))) {
				  	reporter.recordStep("INTENTIONALLY FAILED: " + actionException.getMessage());
				  	clearAssertFailureOnNext();
				}
				else if (assertErrorOnNext && BaseSeleniumTest.seleniumEquals(assertErrorOnNextMessage, StringUtils.removeStart(actionException.getMessage(), "ERROR: "))) {
					reporter.recordStep("INTENTIONALLY FAILED: " + actionException.getMessage());
					clearAssertErrorOnNext();
				}
				else {
					fail(actionException);
					throw actionException; 	
				}
			} catch (Error actionError) {
				if (assertErrorOnNext) {
					reporter.recordStep("INTENTIONALLY FAILED: " + assertErrorOnNextMessage);
					clearAssertErrorOnNext();
				}
				else {
					fail(actionError);
					throw actionError;
				}
			}
			return -1;
	}
	
	</#list>
	
	<#if model.baseUrl != "">
	protected String getBaseUrl() {
		return "${model.baseUrl}";
	}
	</#if>
}
