package com.cr.kickstart.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;

/**
 * This abstract class can be used to obtain a list of all classes in a classpath.
 *
 * <em>Caveat:</em> When used in environments which utilize multiple class loaders--such as
 * a J2EE Container like Tomcat--it is important to select the correct classloader
 * otherwise the classes returned, if any, will be incompatible with those declared
 * in the code employing this class lister.
 * to get a reference to your classloader within an instance method use:
 *  <code>this.getClass().getClassLoader()</code> or
 *  <code>Thread.currentThread().getContextClassLoader()</code> anywhere else
 * <p>
 * @author Kris Dover <krisdover@hotmail.com>
 * @version 0.2.0
 * @since   0.1.0
 */
public abstract class ClasspathUtil {
  /**
   * Searches the classpath for all classes matching a specified search criteria,
   * returning them in a map keyed with the interfaces they implement or null if they
   * have no interfaces. The search criteria can be specified via interface, package
   * and jar name filter arguments
   * <p>
   * @param classLoader       The classloader whose classpath will be traversed
   * @param packagePrefix     A Set of fully qualified package names to search for or
   *                          or null to return classes in all packages
   * @return A Map of a Set of Classes keyed to their interface names
   *
   * @throws ClassNotFoundException if the current thread's classloader cannot load
   *                                a requested class for any reason
   */
  public static Set<Class> findClasses(ClassLoader classLoader,
                                                    String packagePrefix)
  throws ClassNotFoundException {
    Set<Class> classList = new HashSet<Class>();
    Object[] classPaths;
    try{
      // get a list of all classpaths
      classPaths = ((java.net.URLClassLoader) classLoader).getURLs();
    }catch(ClassCastException cce){
      // or cast failed; tokenize the system classpath
      classPaths = System.getProperty("java.class.path", "").split(File.pathSeparator);
    }

    for(int h = 0; h < classPaths.length; h++){
      Enumeration files = null;
      JarFile module = null;
      // for each classpath ...
      File classPath = new File( (URL.class).isInstance(classPaths[h]) ?
                                  ((URL)classPaths[h]).getFile() : classPaths[h].toString() );
      if( classPath.isDirectory()){   // is our classpath a directory and jar filters are not active?
        List<String> dirListing = new ArrayList();
        // get a recursive listing of this classpath
        recursivelyListDir(dirListing, classPath, new StringBuffer() );
        // an enumeration wrapping our list of files
        files = Collections.enumeration( dirListing );
      }else if( classPath.getName().endsWith(".jar") ){    // is our classpath a jar?
        // skip any jars not list in the filter
        try{
          // if our resource is a jar, instantiate a jarfile using the full path to resource
          module = new JarFile( classPath );
        }catch (MalformedURLException mue){
          throw new ClassNotFoundException("Bad classpath. Error: " + mue.getMessage());
        }catch (IOException io){
          throw new ClassNotFoundException("jar file '" + classPath.getName() +
            "' could not be instantiate from file path. Error: " + io.getMessage());
        }
        // get an enumeration of the files in this jar
        files = module.entries();
      }

      // for each file path in our directory or jar
      while( files != null && files.hasMoreElements() ){
        // get each fileName
        String fileName = files.nextElement().toString();
        // we only want the class files
        if( fileName.endsWith(".class") ){
          // convert our full filename to a fully qualified class name
          String className = fileName.replaceAll("/", ".").substring(0, fileName.length() - 6);
          // debug class list
          //System.out.println(className);
          // skip any classes in packages not explicitly requested in our package filter
          if (packagePrefix != null && (!className.startsWith(packagePrefix))) {
              continue;
          }
          // get the class for our class name
          Class theClass = null;
          try{
            theClass = Class.forName(className, false, classLoader);
          }catch(NoClassDefFoundError e){
            System.out.println("Skipping class '" + className + "' for reason " + e.getMessage());
            continue;
          }
          // skip interfaces
          if( theClass.isInterface() ){
            continue;
          }

          classList.add(theClass);

        }
      }

      // close the jar if it was used
      if(module != null){
        try{
          module.close();
        }catch(IOException ioe){
          throw new ClassNotFoundException("The module jar file '" + classPath.getName() +
            "' could not be closed. Error: " + ioe.getMessage());
        }
      }

    } // end for loop

    return classList;
  } // end method

  /**
   * Recursively lists a directory while generating relative paths. This is a helper function for findClasses.
   * Note: Uses a StringBuffer to avoid the excessive overhead of multiple String concatentation
   *
   * @param dirListing     A list variable for storing the directory listing as a list of Strings
   * @param dir                 A File for the directory to be listed
   * @param relativePath A StringBuffer used for building the relative paths
   */
  private static void recursivelyListDir(List<String> dirListing, File dir, StringBuffer relativePath){
    int prevLen; // used to undo append operations to the StringBuffer

    // if the dir is really a directory
    if( dir.isDirectory() ){
      // get a list of the files in this directory
      File[] files = dir.listFiles();
      // for each file in the present dir
      for(int i = 0; i < files.length; i++){
        // store our original relative path string length
        prevLen = relativePath.length();
        // call this function recursively with file list from present
        // dir and relateveto appended with present dir
        recursivelyListDir(dirListing, files[i], relativePath.append( prevLen == 0 ? "" : "/" ).append( files[i].getName() ) );
        //  delete subdirectory previously appended to our relative path
        relativePath.delete(prevLen, relativePath.length());
      }
    }else{
      // this dir is a file; append it to the relativeto path and add it to the directory listing
      dirListing.add( relativePath.toString() );
    }
  }
}