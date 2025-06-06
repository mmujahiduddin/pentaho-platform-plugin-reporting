/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.reporting.platform.plugin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.reporting.engine.classic.core.util.StagingMode;
import org.pentaho.reporting.libraries.base.util.MemoryByteArrayOutputStream;

/**
 *
 * @deprecated
 * @use AbstractStagingHandler
 */
@Deprecated
public class StagingHandler {
  private static final Log logger = LogFactory.getLog( StagingHandler.class );

  private OutputStream destination;
  private TrackingOutputStream stagingStream;
  private File tmpFile;
  private StagingMode mode;
  private IPentahoSession userSession;

  public StagingHandler( final OutputStream outputStream, final StagingMode stagingMode,
      final IPentahoSession userSession ) throws IOException {
    if ( outputStream == null ) {
      throw new NullPointerException();
    }
    if ( stagingMode == null ) {
      throw new NullPointerException();
    }

    this.userSession = userSession;
    this.destination = outputStream;
    initialize( stagingMode );
  }

  public StagingMode getStagingMode() {
    return this.mode;
  }

  public boolean isFullyBuffered() {
    return mode != StagingMode.THRU;
  }

  public boolean canSendHeaders() {
    if ( ( mode == StagingMode.THRU ) && ( getWrittenByteCount() > 0 ) ) {
      return false;
    } else {
      return true;
    }
  }

  private void initialize( final StagingMode mode ) throws IOException {
    this.mode = mode;
    logger.trace( "Staging mode set - " + mode ); //$NON-NLS-1$
    if ( mode == StagingMode.MEMORY ) {
      createTrackingProxy( new MemoryByteArrayOutputStream() );
    } else if ( mode == StagingMode.TMPFILE ) {
      final IApplicationContext appCtx = PentahoSystem.getApplicationContext();
      // Use the deleter framework for safety...
      tmpFile = appCtx.createTempFile( userSession, "repstg", ".tmp", true ); //$NON-NLS-1$ //$NON-NLS-2$

      createTrackingProxy( new BufferedOutputStream( new FileOutputStream( tmpFile ) ) );
    } else {
      createTrackingProxy( destination );
    }
  }

  public OutputStream getStagingOutputStream() {
    return this.stagingStream;
  }

  private void createTrackingProxy( final OutputStream streamToTrack ) {
    this.stagingStream = new TrackingOutputStream( streamToTrack );
  }

  public void complete() throws IOException {
    if ( mode == StagingMode.MEMORY ) {
      final MemoryByteArrayOutputStream stream = (MemoryByteArrayOutputStream) stagingStream.getWrappedStream();
      final byte[] bytes = stream.getRaw();
      destination.write( bytes, 0, stream.getLength() );
      destination.flush();
    } else if ( mode == StagingMode.TMPFILE ) {
      // Close the stream so we can use the file as input.
      IOUtils.closeQuietly( stagingStream );
      stagingStream = null;
      final BufferedInputStream bis = new BufferedInputStream( new FileInputStream( tmpFile ) );
      try {
        IOUtils.copy( bis, destination );
      } finally {
        IOUtils.closeQuietly( bis );
      }
    }
    // Nothing to do for THRU - the output already has it's stuff

    close();

  }

  public void close() {
    if ( ( this.stagingStream != null ) && ( mode == StagingMode.TMPFILE ) ) {
      IOUtils.closeQuietly( stagingStream );
      stagingStream = null;
    }
    if ( tmpFile != null ) {
      if ( tmpFile.exists() ) {
        try {
          tmpFile.delete();
        } catch ( Exception ignored ) {
          // I can't delete it, perhaps the deleter can delete it.
          CommonUtil.checkStyleIgnore();
        }
      }
      tmpFile = null;
    }
  }

  public int getWrittenByteCount() {
    assert stagingStream != null;
    return stagingStream.getTrackingSize();
  }

}
