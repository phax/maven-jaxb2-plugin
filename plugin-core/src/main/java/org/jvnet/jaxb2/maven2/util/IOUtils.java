package org.jvnet.jaxb2.maven2.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.Scanner;
import org.jvnet.jaxb2.maven2.util.CollectionUtils.Function;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.xml.sax.InputSource;

public class IOUtils
{

  /**
   * Creates an input source for the given file.
   *
   * @param file
   *        file to create input source for.
   * @return Created input source object.
   */
  public static InputSource getInputSource (final File file)
  {
    try
    {
      final URL url = file.toURI ().toURL ();
      return getInputSource (url);
    }
    catch (final MalformedURLException e)
    {
      return new InputSource (file.getPath ());
    }
  }

  public static InputSource getInputSource (final URL url)
  {
    return new InputSource (StringUtils.escapeSpace (url.toExternalForm ()));
  }

  public static InputSource getInputSource (final URI uri)
  {
    return new InputSource (StringUtils.escapeSpace (uri.toString ()));
  }

  public static final Function <File, URL> GET_URL = file -> {
    try
    {
      return file.toURI ().toURL ();
    }
    catch (final MalformedURLException muex)
    {
      throw new RuntimeException (muex);
    }
  };

  /**
   * Scans given directory for files satisfying given inclusion/exclusion
   * patterns.
   *
   * @param buildContext
   *        Build context provided by the environment, used to scan for files.
   * @param directory
   *        Directory to scan.
   * @param includes
   *        inclusion pattern.
   * @param excludes
   *        exclusion pattern.
   * @param defaultExcludes
   *        default exclusion flag.
   * @return Files from the given directory which satisfy given patterns. The
   *         files are {@link File#getCanonicalFile() canonical}.
   * @throws IOException
   *         If an I/O error occurs, which is possible because the construction
   *         of the canonical pathname may require filesystem queries.
   */
  public static List <File> scanDirectoryForFiles (final BuildContext buildContext,
                                                   final File directory,
                                                   final String [] includes,
                                                   final String [] excludes,
                                                   final boolean defaultExcludes,
                                                   final Consumer <? super String> aLogger) throws IOException
  {
    if (!directory.exists ())
    {
      return Collections.emptyList ();
    }
    final Scanner scanner;

    if (buildContext != null)
    {
      scanner = buildContext.newScanner (directory, true);
      if (aLogger != null)
      {
        aLogger.accept ("Created scanner from buildContext: " + scanner);
        if (scanner instanceof DirectoryScanner)
          aLogger.accept ("  BaseDir=" + ((DirectoryScanner) scanner).getBasedir ().getAbsolutePath ());
      }
    }
    else
    {
      final DirectoryScanner directoryScanner = new DirectoryScanner ();
      directoryScanner.setBasedir (directory.getAbsoluteFile ());
      scanner = directoryScanner;
      if (aLogger != null)
        aLogger.accept ("Created DirectoryScanner as scanner: " + scanner);
    }
    scanner.setIncludes (includes);
    scanner.setExcludes (excludes);
    if (defaultExcludes)
    {
      scanner.addDefaultExcludes ();
    }

    scanner.scan ();

    if (aLogger != null)
      aLogger.accept ("Scanner included files: " + Arrays.toString (scanner.getIncludedFiles ()));

    final List <File> files = new ArrayList <> ();
    for (final String name : scanner.getIncludedFiles ())
    {
      files.add (new File (directory, name).getCanonicalFile ());
    }

    return files;
  }
}
