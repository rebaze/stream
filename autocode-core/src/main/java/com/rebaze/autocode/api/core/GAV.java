package com.rebaze.autocode.api.core;

import java.util.Objects;

/**
 * A Maven style group/artifact/version holder.
 */
public class GAV
{
    private final String groupId;
    private final String artifactId;
    private final String extension;
    private final String classifier;
    private final String version;

    public GAV( String groupId, String artifactId, String extension, String classifier, String version )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.extension = extension;
        this.classifier = classifier;
        this.version = version;
    }

    public GAV( String groupId, String artifactId, String version )
    {
        this( groupId, artifactId, "", "", version );
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getExtension()
    {
        return extension;
    }

    public String getClassifier()
    {
        return classifier;
    }

    public String getVersion()
    {
        return version;
    }

    @Override public boolean equals( Object o )
    {
        if ( this == o )
            return true;
        if ( o == null || getClass() != o.getClass() )
            return false;
        GAV gav = ( GAV ) o;
        return Objects.equals( groupId, gav.groupId ) &&
            Objects.equals( artifactId, gav.artifactId ) &&
            Objects.equals( extension, gav.extension ) &&
            Objects.equals( classifier, gav.classifier ) &&
            Objects.equals( version, gav.version );
    }

    @Override public int hashCode()
    {
        return Objects.hash( groupId, artifactId, extension, classifier, version );
    }

    public static GAV fromString( String s )
    {
        String[] split = s.split( ":" );
        if ( split.length == 3 )
        {
            return new GAV( split[0], split[1], split[2] );
        }
        else
        {
            throw new AutocodeException( "Cannot parse " + s + " to GAV." );
        }
    }

    @Override public String toString()
    {
        return "GAV{" +
            "groupId='" + groupId + '\'' +
            ", artifactId='" + artifactId + '\'' +
            ", extension='" + extension + '\'' +
            ", classifier='" + classifier + '\'' +
            ", version='" + version + '\'' +
            '}';
    }
}
