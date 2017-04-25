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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.AbstractStandardBasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.SingleColumnType;
import org.hibernate.type.descriptor.java.StringTypeDescriptor;
import org.hibernate.type.descriptor.sql.LongVarcharTypeDescriptor;
import org.hibernate.usertype.ParameterizedType;

/**
 * Mapping between SQL {@link Types#CLOB clob} and Java {@link String} that truncates parameter
 * values to column size.
 *
 * @see <a href="https://developer.jboss.org/wiki/JBPM3TextColumns">JBPM3TextColumns</a>
 * @see org.hibernate.type.TextType
 *
 * @author Alejandro Guizar
 */
public class LimitedTextType extends AbstractStandardBasicType<String>
  implements DiscriminatorType<String>, SingleColumnType<String>, ParameterizedType {

  private static final long serialVersionUID = 1L;

  private int limit;

  public LimitedTextType() {
    super( LongVarcharTypeDescriptor.INSTANCE, StringTypeDescriptor.INSTANCE );
  }

  public int getLimit() {
    return limit;
  }

  @Override
  public void setParameterValues(Properties parameters) {
    limit = Integer.parseInt(parameters.getProperty("limit"));
  }

  @Override
  public String getName() {
    return "ltdtext";
  }

  @Override
  public String objectToSQLString(String value, Dialect dialect) throws Exception {
    return '\'' + value + '\'';
  }

  @Override
  public String stringToObject(String xml) throws Exception {
    return xml;
  }

  @Override
  public String toString(String value) {
    return value;
  }

  @Override
  public final int sqlType() {
    return getSqlTypeDescriptor().getSqlType();
  }

  @Override
  public final void nullSafeSet(PreparedStatement st, Object value, int index, boolean[] settable, SessionImplementor session)
      throws HibernateException, SQLException {
    if ( settable[0] ) {
        String text = toLimitedString( (String)value );
        nullSafeSet( st, text, index, session );
    }
  }

  protected String toLimitedString( String value ) {
    if (value == null) {
      return null;
    }

    String text = value;
    if (text.length() > limit) {
        text = text.substring(0, limit);
    }
    return text;
  }


}
