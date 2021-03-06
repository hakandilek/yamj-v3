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
package org.yamj.core.service;

import java.util.List;
import java.util.concurrent.*;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.yamj.core.configuration.ConfigService;
import org.yamj.core.database.model.dto.QueueDTO;
import org.yamj.core.database.service.ArtworkStorageService;
import org.yamj.core.database.service.MediaStorageService;
import org.yamj.core.database.service.MetadataStorageService;
import org.yamj.core.service.artwork.ArtworkScannerRunner;
import org.yamj.core.service.artwork.ArtworkScannerService;
import org.yamj.core.service.mediainfo.MediaInfoRunner;
import org.yamj.core.service.mediainfo.MediaInfoService;
import org.yamj.core.service.metadata.MetadataScannerRunner;
import org.yamj.core.service.metadata.MetadataScannerService;

@Service
public class ScanningScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(ScanningScheduler.class);
    
    @Autowired
    private ConfigService configService;
    @Autowired
    private MetadataStorageService metadataStorageService;
    @Autowired
    private MetadataScannerService metadataScannerService;
    @Autowired
    private ArtworkStorageService artworkStorageService;
    @Autowired
    private ArtworkScannerService artworkScannerService;
    @Autowired
    private MediaStorageService mediaStorageService;
    @Autowired
    private MediaInfoService mediaInfoService;
    
    private boolean messageDisabledMediaFiles = Boolean.FALSE;   // Have we already printed the disabled message
    private boolean messageDisabledMetaData = Boolean.FALSE;     // Have we already printed the disabled message
    private boolean messageDisabledPeople = Boolean.FALSE;       // Have we already printed the disabled message
    private boolean messageDisabledFilmography = Boolean.FALSE;  // Have we already printed the disabled message
    private boolean messageDisabledArtwork = Boolean.FALSE;      // Have we already printed the disabled message


    @Scheduled(initialDelay = 5000, fixedDelay = 45000)
    public void scanMediaFiles() {
        int maxThreads = configService.getIntProperty("yamj3.scheduler.mediafilescan.maxThreads", 1);
        if (maxThreads <= 0) {
            if (!messageDisabledMediaFiles) {
                messageDisabledMediaFiles = Boolean.TRUE;
                LOG.info("Media file scanning is disabled");
            }
            return;
        }
        if (messageDisabledMediaFiles) {
            LOG.info("Media file scanning is enabled");
        }
        messageDisabledMediaFiles = Boolean.FALSE;

        int maxResults = configService.getIntProperty("yamj3.scheduler.mediafilescan.maxResults", 20);
        List<QueueDTO> queueElements = mediaStorageService.getMediaFileQueueForScanning(maxResults);
        if (CollectionUtils.isEmpty(queueElements)) {
            LOG.trace("No media files found to scan");
            return;
        }

        LOG.info("Found {} media files to process; scan with {} threads", queueElements.size(), maxThreads);
        BlockingQueue<QueueDTO> queue = new LinkedBlockingQueue<>(queueElements);

        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        for (int i = 0; i < maxThreads; i++) {
            MediaInfoRunner worker = new MediaInfoRunner(queue, mediaInfoService);
            executor.execute(worker);
        }
        executor.shutdown();

        // run until all workers have finished
        while (!executor.isTerminated()) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ignore) {
                // ignore error which is expected
            }
        }

        LOG.debug("Finished media file scanning");
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 45000)
    public void scanMetaData() {
        int maxThreads = configService.getIntProperty("yamj3.scheduler.metadatascan.maxThreads", 1);
        if (maxThreads <= 0) {
            if (!messageDisabledMetaData) {
                messageDisabledMetaData = Boolean.TRUE;
                LOG.info("Metadata scanning is disabled");
            }
            return;
        }
        if (messageDisabledMetaData) {
            LOG.info("Metadata scanning is enabled");
        }
        messageDisabledMetaData = Boolean.FALSE;

        int maxResults = configService.getIntProperty("yamj3.scheduler.metadatascan.maxResults", 20);
        List<QueueDTO> queueElements = metadataStorageService.getMetaDataQueueForScanning(maxResults);
        if (CollectionUtils.isEmpty(queueElements)) {
            LOG.trace("No metadata found to scan");
            return;
        }

        LOG.info("Found {} metadata objects to process; scan with {} threads", queueElements.size(), maxThreads);
        BlockingQueue<QueueDTO> queue = new LinkedBlockingQueue<>(queueElements);

        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        for (int i = 0; i < maxThreads; i++) {
            MetadataScannerRunner worker = new MetadataScannerRunner(queue, metadataScannerService);
            executor.execute(worker);
        }
        executor.shutdown();

        // run until all workers have finished
        while (!executor.isTerminated()) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ignore) {
                // ignore error which is expected
            }
        }

        LOG.debug("Finished metadata scanning");
    }

    @Scheduled(initialDelay = 20000, fixedDelay = 45000)
    public void scanPeopleData() {
        int maxThreads = configService.getIntProperty("yamj3.scheduler.peoplescan.maxThreads", 1);
        if (maxThreads <= 0) {
            if (!messageDisabledPeople) {
                messageDisabledPeople = Boolean.TRUE;
                LOG.info("People scanning is disabled");
            }
            return;
        }
        if (messageDisabledPeople) {
            LOG.info("People scanning is enabled");
        }
        messageDisabledPeople = Boolean.FALSE;

        int maxResults = configService.getIntProperty("yamj3.scheduler.peoplescan.maxResults", 50);
        List<QueueDTO> queueElements = metadataStorageService.getPersonQueueForScanning(maxResults);
        if (CollectionUtils.isEmpty(queueElements)) {
            LOG.trace("No people data found to scan");
            return;
        }

        LOG.info("Found {} people objects to process; scan with {} threads", queueElements.size(), maxThreads);
        BlockingQueue<QueueDTO> queue = new LinkedBlockingQueue<>(queueElements);

        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        for (int i = 0; i < maxThreads; i++) {
            MetadataScannerRunner worker = new MetadataScannerRunner(queue, metadataScannerService);
            executor.execute(worker);
        }
        executor.shutdown();

        // run until all workers have finished
        while (!executor.isTerminated()) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ignore) {
                // ignore error which is expected
            }
        }

        LOG.debug("Finished people data scanning");
    }

    @Scheduled(initialDelay = 20000, fixedDelay = 45000)
    public void scanFilmography() {
        int maxThreads = configService.getIntProperty("yamj3.scheduler.filmographyscan.maxThreads", 1);
        if (maxThreads <= 0 || !this.metadataScannerService.isFilmographyScanEnabled()) { 
            if (!messageDisabledFilmography) {
                messageDisabledFilmography = Boolean.TRUE;
                LOG.info("Filmography scanning is disabled");
            }
            return;
        }
        if (messageDisabledFilmography) {
            LOG.info("Filmography scanning is enabled");
        }
        messageDisabledFilmography = Boolean.FALSE;

        int maxResults = configService.getIntProperty("yamj3.scheduler.filmographyscan.maxResults", 50);
        List<QueueDTO> queueElements = metadataStorageService.getFilmographyQueueForScanning(maxResults);
        if (CollectionUtils.isEmpty(queueElements)) {
            LOG.trace("No filmography data found to scan");
            return;
        }

        LOG.info("Found {} filmography objects to process; scan with {} threads", queueElements.size(), maxThreads);
        BlockingQueue<QueueDTO> queue = new LinkedBlockingQueue<>(queueElements);

        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        for (int i = 0; i < maxThreads; i++) {
            MetadataScannerRunner worker = new MetadataScannerRunner(queue, metadataScannerService);
            executor.execute(worker);
        }
        executor.shutdown();

        // run until all workers have finished
        while (!executor.isTerminated()) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ignore) {
                // ignore error which is expected
            }
        }

        LOG.debug("Finished filmography data scanning");
    }

    @Scheduled(initialDelay = 30000, fixedDelay = 45000)
    public void scanArtwork() {
        int maxThreads = configService.getIntProperty("yamj3.scheduler.artworkscan.maxThreads", 1);
        if (maxThreads <= 0) {
            if (!messageDisabledArtwork) {
                messageDisabledArtwork = Boolean.TRUE;
                LOG.info("Artwork scanning is disabled");
            }
            return;
        }
        if (messageDisabledArtwork) {
            LOG.info("Artwork scanning is enabled");
        }
        messageDisabledArtwork = Boolean.FALSE;

        int maxResults = configService.getIntProperty("yamj3.scheduler.artworkscan.maxResults", 30);
        List<QueueDTO> queueElements = artworkStorageService.getArtworkQueueForScanning(maxResults);
        if (CollectionUtils.isEmpty(queueElements)) {
            LOG.trace("No artwork found to scan");
            return;
        }

        LOG.info("Found {} artwork objects to process; scan with {} threads", queueElements.size(), maxThreads);
        BlockingQueue<QueueDTO> queue = new LinkedBlockingQueue<>(queueElements);

        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        for (int i = 0; i < maxThreads; i++) {
            ArtworkScannerRunner worker = new ArtworkScannerRunner(queue, artworkScannerService);
            executor.execute(worker);
        }
        executor.shutdown();

        // run until all workers have finished
        while (!executor.isTerminated()) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ignore) {
                // ignore error which is expected
            }
        }

        LOG.debug("Finished artwork scanning");
    }
}
