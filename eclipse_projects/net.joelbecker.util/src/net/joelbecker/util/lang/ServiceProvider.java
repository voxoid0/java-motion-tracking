/******************************************************************************
* Copyright (c) 2008-2010 Joel Becker. All Rights Reserved.
* http://tech.joelbecker.net
*
*    This is free software; you can redistribute it and/or modify
*    it under the terms of the GNU General Public License
*    version 3, as published by the Free Software Foundation.
*
*    This is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public
*    License along with this source file; if not, write to the Free Software
*    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
******************************************************************************/
package net.joelbecker.util.lang;

import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {
	
	private static ServiceProvider INSTANCE = new ServiceProvider();
	
	private Map<Class<? extends Object>, Object> interfaceToImplMap;
	
	private ServiceProvider() {
		interfaceToImplMap = new HashMap<Class<? extends Object>, Object>();
	}
	
	public static ServiceProvider getInstance() {
		return INSTANCE;
	}
	
	public void register(Class<? extends Object> theClass, Object theImplementation) {
		if (theClass == null) {
			throw new NullPointerException();
		}
		if (theImplementation == null) {
			throw new NullPointerException();
		}
		/*
		if (!theInterface.isInterface()) {
			throw new IllegalArgumentException("theInterface must be an interface, not a class.");
		}*/
		if (!theClass.isInstance(theImplementation)) {
			throw new IllegalArgumentException("The object of class " + theImplementation.getClass() + " is not derived from " + theClass);
		}
		
		if (interfaceToImplMap.get(theClass) != null) {
			throw new IllegalStateException("An object was already registered for the interface " + theClass);
		}
		
		interfaceToImplMap.put(theClass, theImplementation);
	}
	
	public Object getService(Class<? extends Object> theClass) {
//		if (!theClass.isInterface()) {
//			throw new IllegalArgumentException(theClass.getClass().toString() + " is not an interface.");
//		}
		return interfaceToImplMap.get(theClass);
	}
}
