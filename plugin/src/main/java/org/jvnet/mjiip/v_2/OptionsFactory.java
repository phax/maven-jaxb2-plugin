package org.jvnet.mjiip.v_2;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.text.MessageFormat;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.jvnet.jaxb2.maven2.OptionsConfiguration;
import org.jvnet.jaxb2.maven2.util.StringUtils;
import org.xml.sax.InputSource;

import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.api.SpecVersion;

public class OptionsFactory implements org.jvnet.jaxb2.maven2.IOptionsFactory <Options>
{
  /**
   * Creates and initializes an instance of XJC options.
   */
  public Options createOptions (final OptionsConfiguration optionsConfiguration) throws MojoExecutionException
  {
    final Options options = new Options ();

    options.verbose = optionsConfiguration.isVerbose ();
    options.debugMode = optionsConfiguration.isDebugMode ();

    options.classpaths.addAll (optionsConfiguration.getPlugins ());

    options.target = createSpecVersion (optionsConfiguration.getSpecVersion ());

    final String encoding = optionsConfiguration.getEncoding ();

    if (encoding != null)
    {
      options.encoding = createEncoding (encoding);
    }

    options.setSchemaLanguage (createLanguage (optionsConfiguration.getSchemaLanguage ()));

    options.entityResolver = optionsConfiguration.getEntityResolver ();

    for (final InputSource grammar : optionsConfiguration.getGrammars ())
    {
      options.addGrammar (grammar);
    }

    for (final InputSource bindFile : optionsConfiguration.getBindFiles ())
    {
      options.addBindFile (bindFile);
    }

    // Setup Other Options

    options.defaultPackage = optionsConfiguration.getGeneratePackage ();
    options.targetDir = optionsConfiguration.getGenerateDirectory ();

    options.strictCheck = optionsConfiguration.isStrict ();
    options.readOnly = optionsConfiguration.isReadOnly ();
    options.packageLevelAnnotations = optionsConfiguration.isPackageLevelAnnotations ();
    options.noFileHeader = optionsConfiguration.isNoFileHeader ();
    options.enableIntrospection = optionsConfiguration.isEnableIntrospection ();
    options.disableXmlSecurity = optionsConfiguration.isDisableXmlSecurity ();

    if (optionsConfiguration.getAccessExternalSchema () != null)
    {
      System.setProperty ("javax.xml.accessExternalSchema", optionsConfiguration.getAccessExternalSchema ());
    }
    if (optionsConfiguration.getAccessExternalDTD () != null)
    {
      System.setProperty ("javax.xml.accessExternalDTD", optionsConfiguration.getAccessExternalDTD ());
    }
    if (optionsConfiguration.isEnableExternalEntityProcessing ())
    {
      System.setProperty ("enableExternalEntityProcessing", Boolean.TRUE.toString ());
    }
    options.contentForWildcard = optionsConfiguration.isContentForWildcard ();

    if (optionsConfiguration.isExtension ())
    {
      options.compatibilityMode = Options.EXTENSION;
    }

    final List <String> arguments = optionsConfiguration.getArguments ();
    try
    {
      options.parseArguments (arguments.toArray (new String [arguments.size ()]));
    }

    catch (final BadCommandLineException bclex)
    {
      throw new MojoExecutionException ("Error parsing the command line [" + arguments + "]", bclex);
    }

    return options;
  }

  private SpecVersion createSpecVersion (final String specVersion)
  {
    if (specVersion == null)
    {
      return SpecVersion.LATEST;
    }
    final SpecVersion sv = SpecVersion.parse (specVersion);
    return sv == null ? SpecVersion.LATEST : sv;
  }

  private String createEncoding (final String encoding) throws MojoExecutionException
  {
    if (encoding == null)
      return null;
    try
    {
      if (!Charset.isSupported (encoding))
      {
        throw new MojoExecutionException (MessageFormat.format ("Unsupported encoding [{0}].", encoding));
      }
      return encoding;
    }
    catch (final IllegalCharsetNameException icne)
    {
      throw new MojoExecutionException (MessageFormat.format ("Unsupported encoding [{0}].", encoding));
    }

  }

  private Language createLanguage (final String schemaLanguage) throws MojoExecutionException
  {
    if (StringUtils.isEmptyTrimmed (schemaLanguage))
      return null;
    if ("AUTODETECT".equalsIgnoreCase (schemaLanguage))
    {
      // nothing, it is AUTDETECT by default.
      return null;
    }
    if ("XMLSCHEMA".equalsIgnoreCase (schemaLanguage))
      return Language.XMLSCHEMA;
    if ("DTD".equalsIgnoreCase (schemaLanguage))
      return Language.DTD;
    if ("WSDL".equalsIgnoreCase (schemaLanguage))
      return Language.WSDL;
    throw new MojoExecutionException (MessageFormat.format ("Unknown schemaLanguage [{0}].", schemaLanguage));
  }
}
