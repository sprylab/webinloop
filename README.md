#-------------------------------------------------------------------------------
#  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#  
#  Copyright 2009-2011 by sprylab technologies GmbH
#  
#  WebInLoop - a program for testing web applications
#  
#  This file is part of WebInLoop.
#  
#  WebInLoop is free software: you can redistribute it and/or modify
#  it under the terms of the GNU Lesser General Public License version 3
#  only, as published by the Free Software Foundation.
#  
#  WebInLoop is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU Lesser General Public License version 3 for more details
#  (a copy is included in the LICENSE file that accompanied this code).
#  
#  You should have received a copy of the GNU Lesser General Public License
#  version 3 along with WebInLoop.  If not, see
#  <http://www.gnu.org/licenses/lgpl-3.0.html>
#  for a copy of the LGPLv3 License.
#
#
#  This program uses icons from: http://www.famfamfam.com/lab/icons/silk/
#-------------------------------------------------------------------------------

WebInLoop
=========

WebInLoop is a test automation tool specialized for running web tests.

Launch (GUI)
************

To start WebInLoop, simply run the provided run scripts for your platform:
 * run.cmd on Windows
 * run.sh on *nix
WebInLoop will start in GUI mode.

Launch (CLI)
************

There is also a CLI mode if you launch WebInLoop from a shell like this:
 * java -jar webinloop.jar -testfiles <file(s)>

To view all available command line options:
 * java -jar webinloop.jar -help

Important note
**************

 You need a JDK >= 1.6 to run WebInLoop successfully.

Customize
*********

Edit the provided *.xml property files to fit your own needs.