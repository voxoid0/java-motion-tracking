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
package net.joelbecker.util.pattern;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

/**
 * <p>A factory that creates instances of P, where the specific sub-class of P
 * depends on the sub-class of the given object of type T (see provideFor()).
 * Thus, it maps sub-classes of T to sub-classes of P.</p>
 * 
 * <p>An example usage would be a UI factory, which provides, e.g., JPanels for
 * GameCharacters.</p>
 * 
 * @author Joel Becker
 *
 * @param <T> Base-class of objects for which P's are provided.
 * @param <P> Base-class of the provided objects.
 */
public class ClassMappingFactory<T, P> {
	
	/** Map of RgbVideoEffect to JPanel. */
	private Map<Class<? extends T>, Class<? extends P>> classToFactoryMap;

	/** Constructor. */
	public ClassMappingFactory() {
		classToFactoryMap = new HashMap<Class<? extends T>, Class<? extends P>>();
	}
	
	/**
	 * Registers the UI class for the given RgbVideoEffect-derived class.
	 * @param rgbVideoEffect Class that extends RgbVideoEffect
	 * @param jpanel Class that extends {@link JPanel} and provides the UI.
	 */
	public void registerClass(Class<? extends T> clss, Class<? extends P> provided) {
		classToFactoryMap.put(clss, provided);
	}
	
	
	/**
	 * Creates an instance of F for the given T.
	 * @param object The object to create an instance F for.
	 * @return A {@link JPanel}, or null if none has been registered.
	 * @throws IllegalArgumentException If the panel class cannot be instantiated.
	 */
	public P provideFor(T object)
			throws IllegalArgumentException {
		Class<? extends P> providedClass = classToFactoryMap.get(object.getClass());
		P provided = null;
		Constructor<? extends P> ctor = null;
		
		if (providedClass != null) {
			try {
				//// Try constructor with single T parameter 
				ctor = providedClass.getConstructor(new Class<?>[] {object.getClass()});
				provided = ctor.newInstance(object);
						//object.getClass().cast(object));
			} catch (NoSuchMethodException e) {
				try {
					//// Try default constructor
					provided = providedClass.newInstance();
					
				} catch (IllegalAccessException e2) {
					throw new IllegalArgumentException("The class " + providedClass.getName() +
							" does not have an accessible default constructor.", e2);
				} catch (InstantiationException e2) {
					throw new IllegalArgumentException("The class " + providedClass.getName() +
							" cannot be instantiated. (Does it have a default constructor?)", e2);				
				}
			} catch (InvocationTargetException e) {
				throw new IllegalArgumentException(e);
			} catch (IllegalAccessException e2) {
				throw new IllegalArgumentException("Class " + providedClass +
						": Found constructor with parameter for source class " +
						object.getClass() + ", but couldn't access it", e2);
			} catch (InstantiationException e2) {
				throw new IllegalArgumentException("Could not instantiate class " +
						providedClass + " using constructor with single " +
						object.getClass() + "parameter.", e2);				
			}
		}
		return provided;
	}
}
