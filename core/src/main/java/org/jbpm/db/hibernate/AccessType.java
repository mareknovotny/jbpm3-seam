/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jbpm.db.hibernate;

import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.CharacterStream;
import org.hibernate.engine.jdbc.internal.CharacterStreamImpl;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.DataHelper;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;
import org.jbpm.context.def.Access;

public class AccessType extends AbstractSingleColumnStandardBasicType<Access>
        implements DiscriminatorType<Access>
{

    private static final long serialVersionUID = 1L;

    public static final AccessType INSTANCE = new AccessType();

    public AccessType()
    {
        super( VarcharTypeDescriptor.INSTANCE, AccessTypeDescriptor.INSTANCE );
    }

    @Override
    public String getName()
    {
        return "string";
    }

    @Override
    protected boolean registerUnderJavaType()
    {
        return true;
    }

    @Override
    public String objectToSQLString( Access value, Dialect dialect ) throws Exception
    {
        return '\'' + value.toString() + '\'';
    }

    @Override
    public Access stringToObject( String xml ) throws Exception
    {
        return new Access( xml );
    }

    @Override
    public String toString( Access value )
    {
        return value.toString();
    }

    /**
     * Descriptor for {@link Access} handling.
     *
     */
    public static class AccessTypeDescriptor extends AbstractTypeDescriptor<Access> {

        private static final long serialVersionUID = 1L;

        public static final AccessTypeDescriptor INSTANCE = new AccessTypeDescriptor();

        public AccessTypeDescriptor() {
            super( Access.class );
        }

        @Override
        public String toString(Access access) {
            return access.toString();
        }

        @Override
        public Access fromString(String access) {
            return new Access( access );
        }

        @Override
        @SuppressWarnings({ "unchecked" })
        public <X> X unwrap(Access value, Class<X> type, WrapperOptions options) {
            if ( value == null ) {
                return null;
            }
            if ( String.class.isAssignableFrom( type ) ) {
                return (X) value.toString();
            }
            if ( Reader.class.isAssignableFrom( type ) ) {
                return (X) new StringReader( value.toString() );
            }
            if ( CharacterStream.class.isAssignableFrom( type ) ) {
                return (X) new CharacterStreamImpl( value.toString() );
            }
            if ( Clob.class.isAssignableFrom( type ) ) {
                return (X) options.getLobCreator().createClob( value.toString() );
            }
            if ( DataHelper.isNClob( type ) ) {
                return (X) options.getLobCreator().createNClob( value.toString() );
            }

            throw unknownUnwrap( type );
        }

        @Override
        public <X> Access wrap(X value, WrapperOptions options) {
            if ( value == null ) {
                return null;
            }
            if ( String.class.isInstance( value ) ) {
                return new Access( (String) value);
            }
            if ( Reader.class.isInstance( value ) ) {
                return new Access( DataHelper.extractString( (Reader) value ) );
            }
            if ( Clob.class.isInstance( value ) ) {
                return new Access( DataHelper.extractString( (Clob) value ) );
            }

            throw unknownWrap( value.getClass() );
        }
    }
}