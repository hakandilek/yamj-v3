/*
 *      Copyright (c) 2004-2013 YAMJ Members
 *      https://github.com/organizations/YAMJ/teams
 *
 *      This file is part of the Yet Another Media Jukebox (YAMJ).
 *
 *      The YAMJ is free software: you can redistribute it and/or modify
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
 *      along with the YAMJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 *      Web: https://github.com/YAMJ/yamj-v3
 *
 */
package org.yamj.core.service.plugin;

import org.yamj.core.database.model.type.MetaDataType;

import org.yamj.core.database.model.dto.QueueDTO;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginDatabaseRunner implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(PluginDatabaseRunner.class);
    private final BlockingQueue<QueueDTO> queue;
    private final PluginDatabaseService service;

    public PluginDatabaseRunner(BlockingQueue<QueueDTO> queue, PluginDatabaseService service) {
        this.queue = queue;
        this.service = service;
    }

    @Override
    public void run() {
        QueueDTO queueElement = queue.poll();
        while (queueElement != null) {

            try {
                if (queueElement.isMetadataType(MetaDataType.VIDEODATA)) {
                    service.scanVideoData(queueElement.getId());
                } else if (queueElement.isMetadataType(MetaDataType.SERIES)) {
                    service.scanSeries(queueElement.getId());
                } else if (queueElement.isMetadataType(MetaDataType.PERSON)) {
                    service.scanPerson(queueElement.getId());
                } else {
                    LOG.error("No valid element for scanning metadata '{}'", queueElement);
                }
            } catch (Exception error) {
                LOG.error("Failed to process meta data", error);
                try {
                    service.processingError(queueElement);
                } catch (Exception ignore) {
                    // ignore this error
                }
            }
            queueElement = queue.poll();
        }
    }
}
