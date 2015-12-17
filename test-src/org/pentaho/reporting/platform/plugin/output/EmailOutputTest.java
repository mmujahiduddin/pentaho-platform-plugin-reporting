/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.reporting.platform.plugin.output;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.MasterReport;

public class EmailOutputTest extends TestCase {
  EmailOutput emailOutput;

  protected void setUp() {
    emailOutput = new EmailOutput();
  }

  public void testPaginate() throws Exception {
    assertEquals( 0, emailOutput.paginate( null, 0 ) );
  }

  public void testSupportsPagination() throws Exception {
    assertEquals( false, emailOutput.supportsPagination() );
  }

  public void testGetReportLock() throws Exception {
    assertEquals( emailOutput, emailOutput.getReportLock() );
  }

  public void testGenerateContextNull() throws Exception {
    assertEquals( -1, emailOutput.generate( null, 0, null, 0 ) );
  }
}
