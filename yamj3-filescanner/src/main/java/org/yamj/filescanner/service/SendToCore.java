/*
 *      Copyright (c) 2004-2015 YAMJ Members
 *      https://github.com/organizations/YAMJ/teams
 *
 *      This file is part of the Yet Another Media Jukebox (YAMJ).
 *
 *      YAMJ is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      any later version.
 *
 *      YAMJ is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YAMJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 *      Web: https://github.com/YAMJ/yamj-v3
 *
 */
package org.yamj.filescanner.service;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteConnectFailureException;
import org.springframework.stereotype.Service;
import org.yamj.common.dto.ImportDTO;
import org.yamj.common.remote.service.FileImportService;
import org.yamj.common.type.StatusType;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SendToCore implements Callable<StatusType> {

    private static final Logger LOG = LoggerFactory.getLogger(SendToCore.class);
    private ImportDTO importDto;
    private AtomicInteger runningCount;

    @Autowired
    private FileImportService fileImportService;

    public SendToCore() {
        // empty initialization
    }

    public SendToCore(ImportDTO importDto) {
        this.importDto = importDto;
    }

    public void setImportDto(ImportDTO importDto) {
        this.importDto = importDto;
    }

    public void setCounter(AtomicInteger runningCount) {
          this.runningCount = runningCount;
    }

    @Override
    public StatusType call() {
        StatusType status;
        String displayPath = importDto.getStageDirectory().getPath();
        try {
            LOG.debug("Sending: {}", displayPath);
            fileImportService.importScanned(importDto);
            LOG.debug("{}: Successfully queued", displayPath);
            status = StatusType.DONE;
        } catch (RemoteConnectFailureException ex) {
            LOG.error("{}: Failed to connect to the core server: {}", displayPath, ex.getMessage());
            LOG.trace("Exception:", ex);
            status = StatusType.ERROR;
        } catch (RemoteAccessException ex) {
            LOG.error("{}: Failed to send object to the core server: {}", displayPath, ex.getMessage());
            LOG.trace("Exception:", ex);
            status = StatusType.ERROR;
        }

        // Whether or not the message was sent, quit
        LOG.info("{}: Exiting with status {}, remaining threads: {}", displayPath, status, runningCount.decrementAndGet());
        return status;
    }
}
